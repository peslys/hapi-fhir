package ca.uhn.fhir.batch2.maintenance;

/*-
 * #%L
 * HAPI FHIR JPA Server - Batch2 Task Processor
 * %%
 * Copyright (C) 2014 - 2022 Smile CDR, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import ca.uhn.fhir.batch2.api.IJobMaintenanceService;
import ca.uhn.fhir.batch2.api.IJobPersistence;
import ca.uhn.fhir.batch2.channel.BatchJobSender;
import ca.uhn.fhir.batch2.coordinator.JobDefinitionRegistry;
import ca.uhn.fhir.jpa.model.sched.HapiJob;
import ca.uhn.fhir.jpa.model.sched.ISchedulerService;
import ca.uhn.fhir.jpa.model.sched.ScheduledJobDefinition;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

/**
 * This class performs regular polls of the stored jobs in order to
 * perform maintenance. This includes two major functions.
 *
 * <p>
 * First, we calculate statistics and delete expired tasks. This class does
 * the following things:
 * <ul>
 *    <li>For instances that are IN_PROGRESS, calculates throughput and percent complete</li>
 *    <li>For instances that are IN_PROGRESS where all chunks are COMPLETE, marks instance as COMPLETE</li>
 *    <li>For instances that are COMPLETE, purges chunk data</li>
 *    <li>For instances that are IN_PROGRESS where at least one chunk is FAILED, marks instance as FAILED and propagates the error message to the instance, and purges chunk data</li>
 *    <li>For instances that are IN_PROGRESS with an error message set where no chunks are ERRORED or FAILED, clears the error message in the instance (meaning presumably there was an error but it cleared)</li>
 *    <li>For instances that are IN_PROGRESS and isCancelled flag is set marks them as ERRORED and indicating the current running step if any</li>
 *    <li>For instances that are COMPLETE or FAILED and are old, delete them entirely</li>
 * </ul>
 * 	</p>
 *
 * 	<p>
 * Second, we check for any job instances where the job is configured to
 * have gated execution. For these instances, we check if the current step
 * is complete (all chunks are in COMPLETE status) and trigger the next step.
 * </p>
 */
public class JobMaintenanceServiceImpl implements IJobMaintenanceService {
	private static final Logger ourLog = LoggerFactory.getLogger(JobMaintenanceServiceImpl.class);

	private final IJobPersistence myJobPersistence;
	private final ISchedulerService mySchedulerService;
	private final JobDefinitionRegistry myJobDefinitionRegistry;
	private final BatchJobSender myBatchJobSender;

	/**
	 * Constructor
	 */
	public JobMaintenanceServiceImpl(@Nonnull ISchedulerService theSchedulerService, @Nonnull IJobPersistence theJobPersistence, @Nonnull JobDefinitionRegistry theJobDefinitionRegistry, @Nonnull BatchJobSender theBatchJobSender) {
		Validate.notNull(theSchedulerService);
		Validate.notNull(theJobPersistence);
		Validate.notNull(theJobDefinitionRegistry);
		Validate.notNull(theBatchJobSender);

		myJobPersistence = theJobPersistence;
		mySchedulerService = theSchedulerService;
		myJobDefinitionRegistry = theJobDefinitionRegistry;
		myBatchJobSender = theBatchJobSender;
	}

	@PostConstruct
	public void start() {
		ScheduledJobDefinition jobDefinition = new ScheduledJobDefinition();
		jobDefinition.setId(JobMaintenanceScheduledJob.class.getName());
		jobDefinition.setJobClass(JobMaintenanceScheduledJob.class);
		mySchedulerService.scheduleClusteredJob(DateUtils.MILLIS_PER_MINUTE, jobDefinition);
	}

	@Override
	public void runMaintenancePass() {
		JobMaintenanceRun jobMaintenanceRun = new JobMaintenanceRun(myJobPersistence, myJobDefinitionRegistry, myBatchJobSender);
		jobMaintenanceRun.updateInstances();
	}

	public static class JobMaintenanceScheduledJob implements HapiJob {
		@Autowired
		private IJobMaintenanceService myTarget;

		@Override
		public void execute(JobExecutionContext theContext) {
			myTarget.runMaintenancePass();
		}
	}
}
