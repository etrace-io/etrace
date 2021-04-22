import React from "react";
import {dateToString} from "./GlobalConstants";
import {AlertEventVO} from "$models/HolmesModel";

const ReactMarkdown = require("react-markdown/with-html");

export const columnsAlert = [
    {
        title: "一级部门",
        key: "parentDepartment",
        dataIndex: "parentDepartment",
    },
    {
        title: "二级部门",
        key: "department",
        dataIndex: "department",
    },
    {
        title: "规则ID",
        key: "policyId",
        width: "100px",
        render: (text, record: AlertEventVO) => (
            <a href={"/alert/ruleList?policyId=" + JSON.parse(record.payload).policyId}>{JSON.parse(record.payload).policyId}</a>)
    },
    {
        title: "报警消息",
        key: "message",
        render: (text, record: AlertEventVO) => (
            <ReactMarkdown
                className="e-monitor-markdown"
                source={JSON.parse(record.payload).message}
                escapeHtml={false}
            />
        )
    }, {
        title: "报警触发时间",
        key: "createdAt",
        width: "210px",
        render: (text, record: AlertEventVO) => (<span>{dateToString(new Date(record.timestamp))}</span>),
        sorter: (a, b) => a.timestamp - b.timestamp,
    }];

export const columnsEvent = [
    {
        title: "一级部门",
        key: "parentDepartment",
        dataIndex: "parentDepartment",
        width: "100px",
    },
    {
        title: "二级部门",
        key: "department",
        dataIndex: "department",
        width: "120px",
    },
    {
        title: "操作者",
        dataIndex: "operator",
        key: "operator",
        width: "120px",
    }, {
        title: "内容",
        dataIndex: "content",
        key: "content",
    }, {
        title: "描述",
        dataIndex: "description",
        key: "description",
    }, {
        title: "关键路径",
        dataIndex: "isKeyPath",
        key: "isKeyPath",
        width: "80px",
        render: (text, record) => (<span>{JSON.stringify(record.isKeyPath)}</span>)
    }, {
        title: "高危",
        dataIndex: "severity",
        key: "severity",
        width: "80px",
        render: (text, record) => (<span>{JSON.stringify(record.severity)}</span>)
    }, {
        title: "数据源",
        dataIndex: "source",
        key: "source",
        width: "80px",
    }, {
        title: "时间",
        key: "timestamp",
        width: "210px",
        render: (text, record) => (<span>{dateToString(new Date(record.timestamp))}</span>)
    }];
