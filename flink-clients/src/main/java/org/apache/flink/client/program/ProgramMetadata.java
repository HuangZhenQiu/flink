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

import org.apache.flink.api.common.JobID;
import org.apache.flink.configuration.ClusterModeOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.ConfigUtils;
import org.apache.flink.configuration.CoreOptions;
import org.apache.flink.configuration.PipelineOptions;
import org.apache.flink.runtime.jobgraph.SavepointRestoreSettings;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Program metadata to construct a job graph.
 */
public class ProgramMetadata {

	private final List<String> args;
	private final List<URL> classpaths;
	private final String mainClassName;
	private SavepointRestoreSettings savepointRestoreSettings;
	private int parallelism;
	private JobID jobID;

	private ProgramMetadata(
		List<String> args,
		List<URL> classpaths,
		String mainClassName,
		SavepointRestoreSettings savepointRestoreSettings,
		int parallelism,
		JobID jobID) {
		this.args = args;
		this.classpaths = classpaths;
		this.mainClassName = mainClassName;
		this.savepointRestoreSettings = savepointRestoreSettings;
		this.parallelism = parallelism;
		this.jobID = jobID;
	}

	public static ProgramMetadata buildProgramMetadata(Configuration configuration) {
		SavepointRestoreSettings savepointRestoreSettings = SavepointRestoreSettings.fromConfiguration(configuration);
		int parallism = configuration.getInteger(CoreOptions.DEFAULT_PARALLELISM);
		String mainClassName = configuration.getString(ClusterModeOptions.MAIN_CLASS);
		String jobId = configuration.getString(ClusterModeOptions.JOB_ID);
		List<String> args = ConfigUtils.decodeListFromConfig(
			configuration, ClusterModeOptions.PROGRAM_ARGS, arg -> arg);

		List<URL> classpaths = ConfigUtils.decodeListFromConfig(configuration, PipelineOptions.JARS, url -> {
			try {
				return new URL(url);
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException("Invalid URL", e);
			}
		});

		return new ProgramMetadata(
			args, classpaths, mainClassName, savepointRestoreSettings, parallism, JobID.fromHexString(jobId));
	}

	public void toConfiguration(Configuration configuration) {
		SavepointRestoreSettings.toConfiguration(savepointRestoreSettings, configuration);
		configuration.setString(ClusterModeOptions.MAIN_CLASS, mainClassName);
		configuration.setString(ClusterModeOptions.JOB_ID, jobID.toHexString());
		ConfigUtils.encodeCollectionToConfig(configuration, ClusterModeOptions.PROGRAM_ARGS, args, arg -> arg);
		ConfigUtils.encodeCollectionToConfig(configuration, PipelineOptions.JARS, classpaths, url -> url.getPath());
		configuration.setInteger(CoreOptions.DEFAULT_PARALLELISM, parallelism);
	}

	public List<String> getArgs() {
		return args;
	}

	public String getMainClassName() {
		return mainClassName;
	}

	public SavepointRestoreSettings getSavepointRestoreSettings() {
		return savepointRestoreSettings;
	}

	public int getParallelism() {
		return parallelism;
	}

	public JobID getJobID() {
		return jobID;
	}

	public List<URL> getClasspaths() {
		return classpaths;
	}
}
