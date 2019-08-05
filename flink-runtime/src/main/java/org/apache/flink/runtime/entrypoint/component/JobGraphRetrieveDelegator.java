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

package org.apache.flink.runtime.entrypoint.component;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.program.ProgramMetadata;
import org.apache.flink.util.FlinkException;


public interface JobGraphRetrieveDelegator {

	/**
	 * Retrieve the {@link JobGraph}.
	 *
	 * @param configuration cluster configuration
	 * @param metadata program metadata
	 * @return the retrieved {@link JobGraph}.
	 * @throws FlinkException if the {@link JobGraph} could not be retrieved
	 */
	JobGraph retrieveJobGraph(Configuration configuration, ProgramMetadata metadata) throws FlinkException;
}
