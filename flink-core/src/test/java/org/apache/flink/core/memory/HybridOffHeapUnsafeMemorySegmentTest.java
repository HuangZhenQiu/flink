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

package org.apache.flink.core.memory;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Tests for the {@link HybridMemorySegment} in off-heap mode using unsafe memory.
 */
@RunWith(Parameterized.class)
public class HybridOffHeapUnsafeMemorySegmentTest extends HybridOffHeapMemorySegmentTest {

	public HybridOffHeapUnsafeMemorySegmentTest(int pageSize) {
		super(pageSize);
	}

	@Override
	MemorySegment createSegment(int size) {
		return MemorySegmentFactory.allocateOffHeapUnsafeMemory(size, null);
	}

	@Override
	MemorySegment createSegment(int size, Object owner) {
		return MemorySegmentFactory.allocateOffHeapUnsafeMemory(size, owner);
	}
}
