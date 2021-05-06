export class Host {
    id?: number;
    env?: string;
    idc?: string;
    nic0Ip?: string;
    cpu?: string;
    mem?: string;
    hd?: string;
    host_type?: number;  //tslint:disable-line
    hostname?: string;
    os_ver?: string; //tslint:disable-line
    use_status?: string; //tslint:disable-line
}

export class HostResultSet {
    total?: number;
    results?: Array<Host>;
}
export class HostRuntime {
    host?: Host;
    agentRuntime?: AgentRuntime;
}

export class AgentRuntime {
    ip?: string;
    uptime?: number;
    host_name?: string; //tslint:disable-line
    agent_version?: string; //tslint:disable-line
    start_time?: string; //tslint:disable-line
}

export class PluginRuntime {
    name?: string;
    tagName?: string;
    metricKey?: string;
    config?: string;
    pluginStates?: Array<PluginState>;
}

export class PluginState {
    fileName?: string;
    interval?: number;
    errorMsg?: string;
    outputMsg?: string;
    lastRunTime?: string;
}

export class PluginStateItem {
    name?: string;
    tagName?: string;
    metricKey?: string;
    config?: string;
    span?: number;
    pluginState: PluginState;
}