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

package org.apache.flink.runtime.failurerate;

import org.apache.flink.api.common.time.Time;
import org.apache.flink.runtime.execution.ThresholdExceedException;
import org.apache.flink.util.TestLogger;
import org.apache.flink.util.clock.ManualClock;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Test time stamp based failure rater.
 */
public class TimestampBasedFailureRaterTest extends TestLogger {

	@Test(expected = ThresholdExceedException.class)
	public void testMaximumFailureCheck() {
		TimestampBasedFailureRater rater = new TimestampBasedFailureRater(5, Time.of(10, TimeUnit.SECONDS));

		for (int i = 0; i < 6; i++) {
			rater.markEvent();
		}

		rater.checkAgainstThreshold();
	}

	@Test(expected = ThresholdExceedException.class)
	public void testRateRecordMultipleEvents() throws InterruptedException {
		TimestampBasedFailureRater rater  = new TimestampBasedFailureRater(5, Time.of(500, TimeUnit.MILLISECONDS));

		for (int i = 0; i < 3; i++) {
			rater.markEvent(2);
			Thread.sleep(150);
		}

		rater.checkAgainstThreshold();
	}

	@Test
	public void testMovingRate() throws InterruptedException {
		ManualClock manualClock = new ManualClock();
		TimestampBasedFailureRater rater = new TimestampBasedFailureRater(
			5, Time.of(500, TimeUnit.MILLISECONDS), manualClock::absoluteTimeMillis);

		manualClock.advanceTime(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		for (int i = 0; i < 6; i++) {
			rater.markEvent();
			manualClock.advanceTime(150, TimeUnit.MILLISECONDS);
			Thread.sleep(150);
		}

		rater.checkAgainstThreshold();
	}
}
