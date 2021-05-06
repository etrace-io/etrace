export class DeploySchedule {
    id: number;
    updatedAt: number;
    createdAt: number;
    total: number;
    failRate: number;
    batchRate: number;
    batchSize: number;
    interval: number;
    shuffled: boolean;
    scheduleType: string;
    targetId: number;
    operation: string;
    status: string;
    createdBy: string;
    worker: string;
    nextScheduleTs: number;
    deployPlanId: number;
    psnname: string;
}

export class ScheduleStatistics {
    successCount?: number;
    runningCount?: number;
    failedCount?: number;
    cancelCount?: number;
    waitingCount?: number;
}

export class DeployScheduleResultSet {
    total?: number;
    results?: Array<DeploySchedule>;
}

export class ScheduleSearchParam {
    scheduleType?: string;
    status?: string;
    pageSize?: number;
    pageNum?: number;
}

export class DeployTaskSearchParam {
    scheduleId: number;
    hostName?: string;
    status?: string;
    pageSize?: number;
    pageNum?: number;
}

export class DeployTask {
    id: number;
    updatedAt: number;
    createdAt: number;
    hostName: string;
    eocTaskId: string;
    status: string;
}

export class DeployTaskResultSet {
    total?: number;
    results?: Array<DeployTask>;
}