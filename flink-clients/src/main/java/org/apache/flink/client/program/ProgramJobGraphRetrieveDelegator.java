package org.apache.flink.client.program;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.entrypoint.component.JobGraphRetrieveDelegator;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.program.ProgramMetadata;
import org.apache.flink.util.FlinkException;

public class ProgramJobGraphRetrieveDelegator implements JobGraphRetrieveDelegator {

	@Override
	public JobGraph retrieveJobGraph(
		Configuration configuration,
		ProgramMetadata metadata) throws FlinkException {
		final PackagedProgram packagedProgram = createPackagedProgram(metadata);
		final int defaultParallelism = metadata.getParallelism();
		try {
			final JobGraph jobGraph = PackagedProgramUtils.createJobGraph(
				packagedProgram,
				configuration,
				defaultParallelism,
				metadata.getJobID());
			jobGraph.setAllowQueuedScheduling(true);
			jobGraph.setSavepointRestoreSettings(metadata.getSavepointRestoreSettings());
			return jobGraph;
		} catch (Exception e) {
			throw new FlinkException("Could not create the JobGraph from the provided user code jar.", e);
		}
	}

	private PackagedProgram createPackagedProgram(ProgramMetadata metadata) throws FlinkException {
		final String entryClass = metadata.getMainClassName();
		try {
			final Class<?> mainClass = getClass().getClassLoader().loadClass(entryClass);
			return new PackagedProgram(mainClass, metadata.getArgs());
		} catch (ClassNotFoundException | ProgramInvocationException e) {
			throw new FlinkException("Could not load the provided entrypoint class.", e);
		}
	}
}
