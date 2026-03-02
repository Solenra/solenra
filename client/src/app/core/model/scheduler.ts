export interface Scheduler {
  currentlyExecutingJobs: CurrentlyExecutingJob[]
  jobDetails: JobDetail[]
  schedulerInstanceId: string
  schedulerName: string
  summary: string
}

export interface CurrentlyExecutingJob {
  fireInstanceId: string
  fireTime: string
  jobDetail: JobDetail
  jobRunTime: number
  mergedJobDataMap: Object
  nextFireTime: any
  previousFireTime: any
  recovering: boolean
  refireCount: number
  scheduledFireTime: string
  trigger: Trigger
}

export interface JobDetail {
  concurrentExecutionDisallowed: boolean
  description: any
  durable: boolean
  group: string
  jobDataMap: Object
  name: string
  persistJobDataAfterExecution: boolean
  requestsRecovery: boolean
  triggers: Trigger[]
}

export interface Trigger {
  description: any
  endTime: any
  finalFireTime: string
  group: string
  jobDataMap: Object
  mayFireAgain: boolean
  misfireInstruction: number
  name: string
  nextFireTime: any
  previousFireTime: string
  priority: number
  startTime: Date
}
