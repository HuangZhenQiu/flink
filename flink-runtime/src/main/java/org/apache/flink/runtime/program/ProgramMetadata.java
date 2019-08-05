package org.apache.flink.runtime.program;

import org.apache.flink.api.common.JobID;
import org.apache.flink.runtime.jobgraph.SavepointRestoreSettings;

/**
 * Program metadata that is needed for delayed job graph generation.
 */
public class ProgramMetadata {

	private final String[] args;

	private final String mainClassName;

	private SavepointRestoreSettings savepointRestoreSettings;

	private int parallelism;

	private JobID jobID;

	public ProgramMetadata(String mainClass, String[] args,
						   SavepointRestoreSettings savepointRestoreSettings, int parallelism) {
		this.mainClassName = mainClass;
		this.args = args;
		this.savepointRestoreSettings = savepointRestoreSettings;
		this.parallelism = parallelism;
		this.jobID = new JobID();
	}

	public String[] getArgs() {
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

	/**
	 * Returns the ID of the job.
	 *
	 * @return the ID of the job
	 */
	public JobID getJobID() {
		return this.jobID;
	}
}
