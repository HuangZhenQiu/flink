/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.metrics;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Supplier;

/** A timestamp queue based threshold meter. */
public class ThresholdMeter implements Meter {
    private static final Double MILLISECONDS_PER_SECOND = 1000.0;
    private final Supplier<Long> currentTimeMillisSupplier;
    private final double maxEventsPerInterval;
    private final Duration interval;
    private final Queue<Long> failureTimestamps;
    private long failureCounter = 0;

    public ThresholdMeter(double maximumFailureRate, Duration interval) {
        this(maximumFailureRate, interval, System::currentTimeMillis);
    }

    public ThresholdMeter(
            double maxEventsPerInterval, Duration interval, Supplier<Long> customSupplier) {
        this.maxEventsPerInterval = maxEventsPerInterval;
        this.interval = interval;
        this.failureTimestamps = new ArrayDeque<>();
        this.currentTimeMillisSupplier = customSupplier;
    }

    @Override
    public void markEvent() {
        failureTimestamps.add(currentTimeMillisSupplier.get());
        failureCounter++;
    }

    @Override
    public void markEvent(long n) {
        long timestamp = currentTimeMillisSupplier.get();
        for (int i = 0; i < n; i++) {
            failureTimestamps.add(timestamp);
        }
        failureCounter = failureCounter + n;
    }

    @Override
    public double getRate() {
        return getEventCountsRecentInterval() / (interval.toMillis() / MILLISECONDS_PER_SECOND);
    }

    @Override
    public long getCount() {
        return failureCounter;
    }

    public void checkAgainstThreshold() throws ThresholdExceedException {
        if (getEventCountsRecentInterval() >= maxEventsPerInterval) {
            throw new ThresholdExceedException(
                    String.format(
                            "Maximum number of events %f is detected",
                            getEventCountsRecentInterval()));
        }
    }

    private double getEventCountsRecentInterval() {
        Long currentTimeStamp = System.currentTimeMillis();
        while (!failureTimestamps.isEmpty()
                && currentTimeStamp - failureTimestamps.peek() > interval.toMillis()) {
            failureTimestamps.remove();
        }

        return failureTimestamps.size();
    }

    /** Exception thrown when a threshold exceeds. */
    public static class ThresholdExceedException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ThresholdExceedException(String message) {
            super(message);
        }
    }
}
