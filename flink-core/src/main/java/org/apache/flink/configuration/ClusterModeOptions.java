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

package org.apache.flink.configuration;

import java.util.List;

/**
 * Options for cluster mode deployment.
 */
public class ClusterModeOptions {

	/**
	 * This option configures whether to use cluster mode for deployment.
	 */
	public static final ConfigOption<Boolean> ENABLE = ConfigOptions
		.key("clustermode.enable")
		.booleanType()
		.defaultValue(false)
		.withDescription("The option configures whether to enable the cluster mode of deployment");

	/**
	 * This option configures the main class of user program for cluster deployment mode.
	 */
	public static final ConfigOption<String> MAIN_CLASS = ConfigOptions
		.key("clustermode.mainClass")
		.stringType()
		.defaultValue("")
		.withDescription("This option configures the main class of user program for cluster deployment mode. " +
			"So that the job graph can be generated in application master.");


	/**
	 * This option configures for the job id generated in client side. It is used only internally.
	 */
	public static final ConfigOption<String> JOB_ID = ConfigOptions
		.key("clustermode.jobId")
		.stringType()
		.defaultValue("")
		.withDescription("The option configures the job ID of the target job that will be deployment by cluster mode");


	/**
	 * This option configures the program args for the main class.
	 */
	public static final ConfigOption<List<String>> PROGRAM_ARGS = ConfigOptions
		.key("clustermode.program.args")
		.stringType()
		.asList()
		.noDefaultValue()
		.withDescription("A semicolon-separated list of the jars to package with the job jars to be sent to the" +
			" cluster. These have to be valid paths.");

	/**
	 * This option configures the delegator class will be used for generating job graph.
	 */
	public static final ConfigOption<String> DELEGATOR_CLASS = ConfigOptions
		.key("clustermode.delegator.class")
		.defaultValue("org.apache.flink.client.program.ProgramJobGraphRetrieveDelegator")
		.withDescription("The delegator will be used for generating job graph for cluster mode.");

}
