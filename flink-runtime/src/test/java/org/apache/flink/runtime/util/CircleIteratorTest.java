/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.runtime.util;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;

/** Test for {@link CircleIterator}. */
public class CircleIteratorTest {

    @Test
    public void testMerge() {
        validateBuckets(Lists.newArrayList(times("1", 10), times("0", 3)));
        validateBuckets(
                Lists.newArrayList(times("0", 1), times("1", 11), times("2", 9), times("3", 9)));

        Random rnd = new Random();
        int maxSize = 120;
        int maxNumLists = 15;

        long start = System.nanoTime();
        int numRuns = 10000;
        double totalViolations = 0;
        for (int i = 0; i < numRuns; i++) {
            if (runTest(rnd, maxSize, maxNumLists)) {
                totalViolations++;
            }
        }
        System.out.println("Violations: " + totalViolations / numRuns);
        System.out.println("Ms: " + (System.nanoTime() - start) / 1000000);
    }

    private boolean runTest(Random rnd, int maxSize, int maxNumLists) {
        int numLists = rnd.nextInt(maxNumLists) + 1;
        List<List<String>> input = new ArrayList<>();
        for (int i = 0; i < numLists; i++) {
            input.add(times(Integer.toString(i), rnd.nextInt(maxSize) + 1));
        }

        return validateBuckets(input);
    }

    private boolean validateBuckets(List<List<String>> input) {
        List<String> merged = new ArrayList<>();
        new CircleIterator<>(input).forEachRemaining(merged::add);

        boolean violated = false;
        for (int bucketSize = 2; bucketSize < Math.min(32, merged.size() / 2); bucketSize++) {
            if (merged.size() % bucketSize != 0) {
                continue;
            }

            List<List<String>> buckets = new ArrayList<>();
            List<String> currentBucket = new ArrayList<>();
            for (int i = 0; i < merged.size(); i++) {
                currentBucket.add(merged.get(i));
                if (currentBucket.size() == bucketSize) {
                    buckets.add(currentBucket);
                    currentBucket = new ArrayList<>();
                }
            }

            Map<String, Long> maxCounts = new HashMap<>();

            for (List<String> bucket : buckets) {
                Map<String, Long> maxInBucket =
                        bucket.stream()
                                .collect(
                                        Collectors.groupingBy(
                                                Function.identity(), Collectors.counting()));

                for (Map.Entry<String, Long> e : maxInBucket.entrySet()) {
                    if (!maxCounts.containsKey(e.getKey())) {
                        maxCounts.put(e.getKey(), e.getValue());
                    } else {
                        long prevMax = maxCounts.get(e.getKey());
                        long diff = Math.abs(e.getValue() - prevMax);
                        if (diff > 3) {
                            fail(
                                    String.format(
                                            "Violation: %s, Buckets: %s, Input: %s, Merged: %s",
                                            e.getKey(), buckets, input, merged));
                        } else if (diff > 1) {
                            violated = true;
                        }

                        maxCounts.put(e.getKey(), Math.max(prevMax, e.getValue()));
                    }
                }
            }
        }
        return violated;
    }

    private List<String> times(String s, int size) {
        List<String> out = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            out.add(s);
        }
        return out;
    }
}
