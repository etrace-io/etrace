import {EnumVariate, HttpVariate, MetricVariate, TargetVariate, Variate, VariateType} from "../models/BoardModel";
import {isEmpty} from "$utils/Util";
import {TagFilter, Target} from "$models/ChartModel";
import * as MetaService from "./MetaService";
import StoreManager from "../store/StoreManager";
import * as LinDBService from ".//LinDBService";

/**
 * 获取变量value的值
 * @param variate 变量配置
 * @param searchValue 前缀过滤
 */
export async function loadVariateValues(variate: Variate, searchValue?: string) {
    if (isEmpty(variate.type) || variate.type == VariateType.METRIC) {
        const metricVariate: MetricVariate = variate as MetricVariate;
        let measurement;
        if (metricVariate.prefixKey) {
            measurement = StoreManager.urlParamStore.getValue(metricVariate.prefixKey) + "." + metricVariate.measurement;
        } else if (metricVariate.prefix) {
            measurement = metricVariate.prefix + "." + metricVariate.measurement;
        } else {
            measurement = metricVariate.measurement;
        }

        // 实现"级联选择"tag
        if (metricVariate.relatedTagKeys) {
            let tagFilters: Array<TagFilter> = [];
            metricVariate.relatedTagKeys.forEach(tagKey => {
                const values = StoreManager.urlParamStore.getValues(tagKey);
                if (!isEmpty(values)) {
                    tagFilters.push({
                        key: tagKey,
                        op: "=",
                        value: values
                    });
                }
            });

            // 若是带searchValue的，在tagFilter中，带上该条件, current variate search filter
            if (searchValue) {
                tagFilters.push({
                    key: variate.name,
                    op: "=",
                    value: [searchValue]
                });
            }

            return await LinDBService.showTagValueByMetricBean(
                {
                    entity: metricVariate.entity,
                    fields: [variate.name],
                    measurement: measurement,
                    tagFilters: tagFilters,
                });
        } else {
            return await LinDBService.showTagValue(metricVariate.entity, measurement, metricVariate.name, searchValue);
        }
    } else if (variate.type == VariateType.HTTP) {
        const httpVariate: HttpVariate = variate as HttpVariate;
        return await MetaService.getMeta(httpVariate.query);
    } else if (variate.type == VariateType.ENUM) {
        const enumVariate: EnumVariate = variate as EnumVariate;
        if (enumVariate.lists) {
            return enumVariate.lists.split(",");
        }
    } else if (variate.type == VariateType.TARGET) {
        const targetVariate: TargetVariate = variate as TargetVariate;
        let target: Target = new Target();
        target.prefix = targetVariate.prefix;
        target.fields = targetVariate.fields;
        target.groupBy = targetVariate.groupBy;
        target.measurement = targetVariate.measurement;
        target.entity = targetVariate.entity;
        return await LinDBService.searchVariateList(target, targetVariate.variateKey);
    }
    return [];
}
