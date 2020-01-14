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

import org.apache.flink.configuration.ClusterModeOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.util.FlinkException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * Implementation of job graph retriever that use reflection to find delegator.
 */
public class ProgramJobGraphRetriever extends AbstractUserClassPathJobGraphRetriever {
	private static final Logger LOG = LoggerFactory.getLogger(ProgramJobGraphRetriever.class);

	public ProgramJobGraphRetriever(@Nullable File userLibJar) throws IOException {
		super(userLibJar);
	}

	@Override
	public JobGraph retrieveJobGraph(Configuration configuration) throws FlinkException {
		try {
			String delegatorClassName = configuration.getString(ClusterModeOptions.DELEGATOR_CLASS);
			Class delegatorClass =  Class.forName(delegatorClassName);
			JobGraphRetrieveDelegator delegator = (JobGraphRetrieveDelegator) delegatorClass.newInstance();
			JobGraph jobGraph =  delegator.retrieveJobGraph(configuration);
			addUserClassPathsToJobGraph(jobGraph);
			return jobGraph;
		} catch (ClassNotFoundException e) {
			LOG.error("Can't find ProgramJobGraphRetrieveDelegator. To use cluster deployment target," +
				" please include flink-client module in classpath", e);
			throw new FlinkException(e);
		} catch (Exception e) {
			throw new FlinkException(e);
		}
	}
}
