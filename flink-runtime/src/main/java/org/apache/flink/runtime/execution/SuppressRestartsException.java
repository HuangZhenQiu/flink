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

package org.apache.flink.runtime.execution;

import org.apache.flink.runtime.throwable.ThrowableAnnotation;
import org.apache.flink.runtime.throwable.ThrowableType;

/**
 * Exception thrown in order to suppress job restarts.
 *
 * <p>This exception acts as a wrapper around the real cause and suppresses
 * job restarts. The JobManager will <strong>not</strong> restart a job, which
 * fails with this Exception.
 */
@ThrowableAnnotation(ThrowableType.NonRecoverableError)
public class SuppressRestartsException extends RuntimeException {

	private static final long serialVersionUID = 221873676920848349L;

	public SuppressRestartsException(Throwable cause) {
		super("Unrecoverable failure. This suppresses job restarts. Please check the " +
			"stack trace for the root cause.", cause);
	}

	public SuppressRestartsException(String message, Throwable cause) {
		super(message, cause);
	}
}
