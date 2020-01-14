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

package org.apache.flink.client.program;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.entrypoint.component.JobGraphRetrieveDelegator;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.util.FlinkException;

/**
 * Job graph retrieve delegator that uses program metadata for generating job graph.
 */
public class ProgramJobGraphRetrieveDelegator implements JobGraphRetrieveDelegator {

	public ProgramJobGraphRetrieveDelegator() {

	}

	@Override
	public JobGraph retrieveJobGraph(Configuration configuration) throws FlinkException {
		try {
			ProgramMetadata programMetadata = ProgramMetadata.buildProgramMetadata(configuration);
			PackagedProgram packagedProgram = createPackagedProgram(programMetadata, configuration);
			JobGraph jobGraph = PackagedProgramUtils.createJobGraph(
				packagedProgram,
				configuration,
				programMetadata.getParallelism(),
				programMetadata.getJobID(),
				false
			);
			return jobGraph;
		} catch (ProgramInvocationException exception) {
			throw new FlinkException(exception);
		}
	}

	private PackagedProgram createPackagedProgram(
		ProgramMetadata metadata, Configuration configuration) throws FlinkException {
		final String entryClass = metadata.getMainClassName();
		try {
			final Class<?> mainClass = getClass().getClassLoader().loadClass(entryClass);
			return PackagedProgram.newBuilder()
				.setEntryPointClassName(metadata.getMainClassName())
				.setArguments(metadata.getArgs().toArray(new String[0]))
				.setSavepointRestoreSettings(metadata.getSavepointRestoreSettings())
				.setConfiguration(configuration)
				.setUserClassPaths(metadata.getClasspaths())
				.build();
		} catch (ClassNotFoundException | ProgramInvocationException e) {
			throw new FlinkException("Could not load the provided entrypoint class.", e);
		}
	}
}
