package com.github.solenra.server.model.quartz;

import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SchedulerDto {
	private String schedulerName;
	private String schedulerInstanceId;
	private String summary;
	private List<JobDetailDto> jobDetails;
	private List<JobExecutionContextDto> currentlyExecutingJobs;

	public SchedulerDto(Scheduler scheduler) throws SchedulerException {
		this.schedulerName = scheduler.getSchedulerName();
		this.schedulerInstanceId = scheduler.getSchedulerInstanceId();
		this.summary = scheduler.getMetaData().toString();

		this.currentlyExecutingJobs = new ArrayList<>();
		if (scheduler.getCurrentlyExecutingJobs() != null) {
			for (JobExecutionContext jobExecutionContext : scheduler.getCurrentlyExecutingJobs()) {
				this.currentlyExecutingJobs.add(new JobExecutionContextDto(jobExecutionContext));
			}
		}

		this.jobDetails = new ArrayList<>();
		if (scheduler.getJobGroupNames() != null) {
			for (String groupName : scheduler.getJobGroupNames()) {
				for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
					JobDetail jobDetail = scheduler.getJobDetail(jobKey);
					List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
					JobDetailDto jobDetailDto = new JobDetailDto(jobDetail, triggers);
					this.jobDetails.add(jobDetailDto);
				}
			}
		}
	}

	public String getSchedulerName() {
		return schedulerName;
	}

	public void setSchedulerName(String schedulerName) {
		this.schedulerName = schedulerName;
	}

	public String getSchedulerInstanceId() {
		return schedulerInstanceId;
	}

	public void setSchedulerInstanceId(String schedulerInstanceId) {
		this.schedulerInstanceId = schedulerInstanceId;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public List<JobDetailDto> getJobDetails() {
		return jobDetails;
	}

	public void setJobDetails(List<JobDetailDto> jobDetails) {
		this.jobDetails = jobDetails;
	}

	public List<JobExecutionContextDto> getCurrentlyExecutingJobs() {
		return currentlyExecutingJobs;
	}

	public void setCurrentlyExecutingJobs(List<JobExecutionContextDto> currentlyExecutingJobs) {
		this.currentlyExecutingJobs = currentlyExecutingJobs;
	}

}

class JobExecutionContextDto {

	TriggerDto trigger;
	boolean isRecovering;
	int refireCount;
	JobDataMap mergedJobDataMap;
	JobDetailDto jobDetail;
	Date fireTime;
	Date scheduledFireTime;
	Date previousFireTime;
	Date nextFireTime;
	String fireInstanceId;
	long jobRunTime;

	public JobExecutionContextDto(JobExecutionContext jobExecutionContext) {
		this.trigger = new TriggerDto(jobExecutionContext.getTrigger());
		this.isRecovering = jobExecutionContext.isRecovering();
		this.refireCount = jobExecutionContext.getRefireCount();
		this.mergedJobDataMap = jobExecutionContext.getMergedJobDataMap();
		this.jobDetail = new JobDetailDto(jobExecutionContext.getJobDetail(), Collections.singletonList(jobExecutionContext.getTrigger()));
		this.fireTime = jobExecutionContext.getFireTime();
		this.scheduledFireTime = jobExecutionContext.getScheduledFireTime();
		this.previousFireTime = jobExecutionContext.getPreviousFireTime();
		this.nextFireTime = jobExecutionContext.getNextFireTime();
		this.fireInstanceId = jobExecutionContext.getFireInstanceId();
		this.jobRunTime = jobExecutionContext.getJobRunTime();
	}

	public TriggerDto getTrigger() {
		return trigger;
	}

	public void setTrigger(TriggerDto trigger) {
		this.trigger = trigger;
	}

	public boolean isRecovering() {
		return isRecovering;
	}

	public void setRecovering(boolean recovering) {
		isRecovering = recovering;
	}

	public int getRefireCount() {
		return refireCount;
	}

	public void setRefireCount(int refireCount) {
		this.refireCount = refireCount;
	}

	public JobDataMap getMergedJobDataMap() {
		return mergedJobDataMap;
	}

	public void setMergedJobDataMap(JobDataMap mergedJobDataMap) {
		this.mergedJobDataMap = mergedJobDataMap;
	}

	public JobDetailDto getJobDetail() {
		return jobDetail;
	}

	public void setJobDetail(JobDetailDto jobDetail) {
		this.jobDetail = jobDetail;
	}

	public Date getFireTime() {
		return fireTime;
	}

	public void setFireTime(Date fireTime) {
		this.fireTime = fireTime;
	}

	public Date getScheduledFireTime() {
		return scheduledFireTime;
	}

	public void setScheduledFireTime(Date scheduledFireTime) {
		this.scheduledFireTime = scheduledFireTime;
	}

	public Date getPreviousFireTime() {
		return previousFireTime;
	}

	public void setPreviousFireTime(Date previousFireTime) {
		this.previousFireTime = previousFireTime;
	}

	public Date getNextFireTime() {
		return nextFireTime;
	}

	public void setNextFireTime(Date nextFireTime) {
		this.nextFireTime = nextFireTime;
	}

	public String getFireInstanceId() {
		return fireInstanceId;
	}

	public void setFireInstanceId(String fireInstanceId) {
		this.fireInstanceId = fireInstanceId;
	}

	public long getJobRunTime() {
		return jobRunTime;
	}

	public void setJobRunTime(long jobRunTime) {
		this.jobRunTime = jobRunTime;
	}

}

class JobDetailDto {

	private String name;
	private String group;
	private String description;
	private boolean concurrentExecutionDisallowed;
	private boolean persistJobDataAfterExecution;
	private boolean durable;
	private boolean requestsRecovery;
	private JobDataMap jobDataMap;
	private List<TriggerDto> triggers;

	public JobDetailDto(JobDetail jobDetail, List<? extends Trigger> triggers) {
		this.name = jobDetail.getKey().getName();
		this.group = jobDetail.getKey().getGroup();
		this.description = jobDetail.getDescription();
		this.concurrentExecutionDisallowed = jobDetail.isConcurrentExecutionDisallowed();
		this.persistJobDataAfterExecution = jobDetail.isPersistJobDataAfterExecution();
		this.durable = jobDetail.isDurable();
		this.requestsRecovery = jobDetail.requestsRecovery();
		this.jobDataMap = jobDetail.getJobDataMap();

		this.triggers = new ArrayList<>();
		if (triggers != null) {
			for (Trigger trigger : triggers) {
				this.triggers.add(new TriggerDto(trigger));
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isConcurrentExecutionDisallowed() {
		return concurrentExecutionDisallowed;
	}

	public void setConcurrentExecutionDisallowed(boolean concurrentExecutionDisallowed) {
		this.concurrentExecutionDisallowed = concurrentExecutionDisallowed;
	}

	public boolean isPersistJobDataAfterExecution() {
		return persistJobDataAfterExecution;
	}

	public void setPersistJobDataAfterExecution(boolean persistJobDataAfterExecution) {
		this.persistJobDataAfterExecution = persistJobDataAfterExecution;
	}

	public boolean isDurable() {
		return durable;
	}

	public void setDurable(boolean durable) {
		this.durable = durable;
	}

	public boolean isRequestsRecovery() {
		return requestsRecovery;
	}

	public void setRequestsRecovery(boolean requestsRecovery) {
		this.requestsRecovery = requestsRecovery;
	}

	public JobDataMap getJobDataMap() {
		return jobDataMap;
	}

	public void setJobDataMap(JobDataMap jobDataMap) {
		this.jobDataMap = jobDataMap;
	}

	public List<TriggerDto> getTriggers() {
		return triggers;
	}

	public void setTriggers(List<TriggerDto> triggers) {
		this.triggers = triggers;
	}

}

class TriggerDto {

	private String name;
	private String group;
	private String description;
	private JobDataMap jobDataMap;
	private int priority;
	private boolean mayFireAgain;
	private Date startTime;
	private Date endTime;
	private Date nextFireTime;
	private Date previousFireTime;
	private Date finalFireTime;
	private int misfireInstruction;

	public TriggerDto(Trigger trigger) {
		this.name = trigger.getKey().getName();
		this.group = trigger.getKey().getGroup();
		this.description = trigger.getDescription();
		this.jobDataMap = trigger.getJobDataMap();
		this.priority = trigger.getPriority();
		this.mayFireAgain = trigger.mayFireAgain();
		this.startTime = trigger.getStartTime();
		this.endTime = trigger.getEndTime();
		this.nextFireTime = trigger.getNextFireTime();
		this.previousFireTime = trigger.getPreviousFireTime();
		this.finalFireTime = trigger.getFinalFireTime();
		this.misfireInstruction = trigger.getMisfireInstruction();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public JobDataMap getJobDataMap() {
		return jobDataMap;
	}

	public void setJobDataMap(JobDataMap jobDataMap) {
		this.jobDataMap = jobDataMap;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public boolean isMayFireAgain() {
		return mayFireAgain;
	}

	public void setMayFireAgain(boolean mayFireAgain) {
		this.mayFireAgain = mayFireAgain;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getNextFireTime() {
		return nextFireTime;
	}

	public void setNextFireTime(Date nextFireTime) {
		this.nextFireTime = nextFireTime;
	}

	public Date getPreviousFireTime() {
		return previousFireTime;
	}

	public void setPreviousFireTime(Date previousFireTime) {
		this.previousFireTime = previousFireTime;
	}

	public Date getFinalFireTime() {
		return finalFireTime;
	}

	public void setFinalFireTime(Date finalFireTime) {
		this.finalFireTime = finalFireTime;
	}

	public int getMisfireInstruction() {
		return misfireInstruction;
	}

	public void setMisfireInstruction(int misfireInstruction) {
		this.misfireInstruction = misfireInstruction;
	}

}