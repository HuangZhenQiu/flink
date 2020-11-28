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

import org.apache.flink.annotation.VisibleForTesting;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.runtime.execution.ThresholdExceedException;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Supplier;

/**
 * A timestamp queue based failure rater implementation.
 */
public class TimestampBasedFailureRater implements ThresholdMeter {
	private static final Double MILLISECONDS_PER_SECOND = 1000.0;
	private final Supplier<Long> currentTimeMillisSupplier;
	private final double maximumFailureRate;
	private final Time failureInterval;
	private final Queue<Long> failureTimestamps;
	private long failureCounter = 0;

	public TimestampBasedFailureRater(double maximumFailureRate, Time failureInterval) {
		this(maximumFailureRate, failureInterval, System::currentTimeMillis);
	}

	@VisibleForTesting
	public TimestampBasedFailureRater(double maximumFailureRate, Time failureInterval, Supplier<Long> customSupplier) {
		this.maximumFailureRate = maximumFailureRate;
		this.failureInterval = failureInterval;
		this.failureTimestamps = new ArrayDeque<>();
		this.currentTimeMillisSupplier = customSupplier;
	}

	@Override
	public void markEvent() {
		failureTimestamps.add(System.currentTimeMillis());
		failureCounter++;
	}

	@Override
	public void markEvent(long n) {
		for (int i = 0; i < n; i++) {
			failureTimestamps.add(System.currentTimeMillis());
		}
		failureCounter = failureCounter + n;
	}

	@Override
	public double getRate() {
		return getCurrentFailureRate() / (failureInterval.toMilliseconds() / MILLISECONDS_PER_SECOND);
	}

	@Override
	public long getCount() {
		return failureCounter;
	}

	@Override
	public void checkAgainstThreshold() throws ThresholdExceedException {
		if (getCurrentFailureRate() >= maximumFailureRate) {
			throw new ThresholdExceedException(String.format("Maximum number of failed workers %f"
				+ " is detected in Resource Manager", getCurrentFailureRate()));
		}
	}

	private double getCurrentFailureRate() {
		Long currentTimeStamp = System.currentTimeMillis();
		while (!failureTimestamps.isEmpty() &&
			currentTimeStamp - failureTimestamps.peek() > failureInterval.toMilliseconds()) {
			failureTimestamps.remove();
		}

		return failureTimestamps.size();
	}
}
