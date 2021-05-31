/**
 * grafana document: https://grafana.com/grafana/plugins/grafana-simple-json-datasource
 *
 * 数据如：
 *
 *
 */
 // {
 //        "datapoints": [
 //            [
 //                4.6667,
 //                1580688600000
 //            ],
 //            [
 //                5.4,
 //                1580689200000
 //            ],
 //            [
 //                8.7,
 //                1580689800000
 //            ],
 //            [
 //                7.3,
 //                1580690400000
 //            ],
 //            [
 //                5.2,
 //                1580691000000
 //            ],
 //            [
 //                5.8,
 //                1580691600000
 //            ],
 //            [
 //                4.6,
 //                1580692200000
 //            ]
 //        ],
 //        "target": "(A)CID_marketing-cache-consumer 成功"
 //    },
export interface SimpleJsonResult {
    target: string;
    datapoints: number[];
}
