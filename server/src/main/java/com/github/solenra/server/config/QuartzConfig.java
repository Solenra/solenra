package com.github.solenra.server.config;

import org.quartz.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.quartz.autoconfigure.QuartzProperties;
import org.springframework.boot.quartz.autoconfigure.SchedulerFactoryBeanCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import com.github.solenra.server.jobs.CommandRunnerJob;
import com.github.solenra.server.jobs.IntegrationLoadJob;
import com.github.solenra.server.jobs.SolaredgeLoadJob;
import com.github.solenra.server.jobs.SolaredgeV2LoadJob;

import java.util.Properties;

@Configuration
public class QuartzConfig {

	public static final String DEFAULT_GROUP = "defaultGroup";

	@Value("${app.solaredge-api-quartz-scheduler-name}")
	private String solaredgeApiQuartzSchedulerName;

	@Value("${app.solaredge-v2-api-quartz-scheduler-name}")
	private String solaredgeV2ApiQuartzSchedulerName;

	@Primary
	@DependsOn("flywayInitializer")
	@Bean(name = "mainQuartzScheduler", destroyMethod = "destroy")
	public SchedulerFactoryBean mainQuartzScheduler(
			QuartzProperties properties,
			ObjectProvider<SchedulerFactoryBeanCustomizer> customizers,
			ObjectProvider<JobDetail> jobDetails,
			ObjectProvider<Trigger> triggers,
			ApplicationContext applicationContext
	) {
		SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
		SpringBeanJobFactory jobFactory = new SpringBeanJobFactory();
		jobFactory.setApplicationContext(applicationContext);
		schedulerFactoryBean.setJobFactory(jobFactory);
		if (properties.getSchedulerName() != null) {
			schedulerFactoryBean.setSchedulerName(properties.getSchedulerName());
		}

		schedulerFactoryBean.setAutoStartup(properties.isAutoStartup());
		schedulerFactoryBean.setStartupDelay((int)properties.getStartupDelay().getSeconds());
		schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(properties.isWaitForJobsToCompleteOnShutdown());
		schedulerFactoryBean.setOverwriteExistingJobs(properties.isOverwriteExistingJobs());
		if (!properties.getProperties().isEmpty()) {
				Properties props = new Properties();
		props.putAll(properties.getProperties());
			schedulerFactoryBean.setQuartzProperties(props);
		}

		schedulerFactoryBean.setJobDetails((JobDetail[])jobDetails.orderedStream().toArray((size) -> {
			return new JobDetail[size];
		}));
		schedulerFactoryBean.setTriggers((Trigger[])triggers.orderedStream().toArray((size) -> {
			return new Trigger[size];
		}));
		customizers.orderedStream().forEach((customizer) -> {
			customizer.customize(schedulerFactoryBean);
		});
		return schedulerFactoryBean;
	}

	private SchedulerFactoryBean quartzScheduler(
			QuartzProperties properties,
			ObjectProvider<SchedulerFactoryBeanCustomizer> customizers,
			int threadCount,
			ApplicationContext applicationContext,
			String schedulerName,
			JobDetail jobDetail
	) {
		SpringBeanJobFactory jobFactory = new SpringBeanJobFactory();
		jobFactory.setApplicationContext(applicationContext);
		SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
		schedulerFactoryBean.setJobFactory(jobFactory);
		schedulerFactoryBean.setSchedulerName(schedulerName);
		schedulerFactoryBean.setAutoStartup(properties.isAutoStartup());
		schedulerFactoryBean.setStartupDelay((int) properties.getStartupDelay().getSeconds());
		schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(properties.isWaitForJobsToCompleteOnShutdown());
		schedulerFactoryBean.setJobDetails(jobDetail);
		Properties propertiesVariant = new Properties();
		propertiesVariant.putAll(properties.getProperties());
		propertiesVariant.setProperty("org.quartz.threadPool.threadCount", Integer.toString(threadCount));
		schedulerFactoryBean.setQuartzProperties(propertiesVariant);
		customizers.orderedStream().forEach((customizer) -> customizer.customize(schedulerFactoryBean));
		return schedulerFactoryBean;
	}

	@DependsOn("flywayInitializer")
	@Bean(name = "solaredgeApiQuartzScheduler", destroyMethod = "destroy")
	public SchedulerFactoryBean solaredgeApiQuartzScheduler(
			QuartzProperties properties,
			ObjectProvider<SchedulerFactoryBeanCustomizer> customizers,
			@Value("${spring.quartz.properties.solaredgeApiQuartzScheduler.org.quartz.threadPool.threadCount:15}") int threadCount,
			ApplicationContext applicationContext) {
		return quartzScheduler(properties, customizers, threadCount, applicationContext, solaredgeApiQuartzSchedulerName, solaredgeLoadJobDetail());
	}

	@DependsOn("flywayInitializer")
	@Bean(name = "solaredgeV2ApiQuartzScheduler", destroyMethod = "destroy")
	public SchedulerFactoryBean solaredgeV2ApiQuartzScheduler(
			QuartzProperties properties,
			ObjectProvider<SchedulerFactoryBeanCustomizer> customizers,
			@Value("${spring.quartz.properties.solaredgeV2ApiQuartzScheduler.org.quartz.threadPool.threadCount:15}") int threadCount,
			ApplicationContext applicationContext) {
		return quartzScheduler(properties, customizers, threadCount, applicationContext, solaredgeV2ApiQuartzSchedulerName, solaredgeV2LoadJobDetail());
	}

	@Bean
	public JobDetail solaredgeLoadJobDetail() {
		return JobBuilder.newJob(SolaredgeLoadJob.class)
				.withIdentity(SolaredgeLoadJob.NAME, DEFAULT_GROUP)
				.requestRecovery()
				.storeDurably()
				.build();
	}

	@Bean
	public JobDetail solaredgeV2LoadJobDetail() {
		return JobBuilder.newJob(SolaredgeV2LoadJob.class)
				.withIdentity(SolaredgeV2LoadJob.NAME, DEFAULT_GROUP)
				.requestRecovery()
				.storeDurably()
				.build();
	}

	@Bean
	public JobDetail integrationLoadJobDetail() {
		return JobBuilder.newJob(IntegrationLoadJob.class)
				.withIdentity(IntegrationLoadJob.NAME, DEFAULT_GROUP)
				.storeDurably()
				.requestRecovery()
				.build();
	}

	@Bean
	public JobDetail commandRunnerJobDetails() {
		return JobBuilder.newJob(CommandRunnerJob.class)
				.withIdentity(CommandRunnerJob.NAME, DEFAULT_GROUP)
				.requestRecovery()
				.storeDurably()
				.build();
	}

	@Bean
	public Trigger integrationLoadJobTrigger() {
		SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
				.withIntervalInSeconds(60)
				.repeatForever();

		return TriggerBuilder.newTrigger()
				.forJob(integrationLoadJobDetail())
				.withIdentity("integrationLoadJobTrigger")
				.withSchedule(scheduleBuilder)
				.build();
	}

}
