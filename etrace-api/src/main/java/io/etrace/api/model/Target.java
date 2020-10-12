package io.etrace.api.model;

import lombok.Data;

@Data
public class Target {

    private String entity;
    private String prefix;
    private Boolean display;
    private String measurement;
    private Object groupBy;
    private Object tagFilters;
    private Object fields;
    private Object tagKeys;
    private Object variate;
    private Object functions;
    private String from;
    private String to;
    private String orderBy;
    private String metricType;
}
