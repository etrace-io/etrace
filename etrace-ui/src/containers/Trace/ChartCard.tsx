import {Col} from "antd";
import React from "react";
import {ColProps} from "antd/lib/grid/col";
import Metric from "../../components/Metric/Metric";
import MetaLink from "../../components/Metric/MetaLink";

interface ChartCardProps extends ColProps {
    chart?: any;
    style?: React.CSSProperties;
    span?: number;
    uniqueId?: string;
    awaitLoad?: boolean;
    editFunction?: () => void;
    overlay?: React.ReactNode; // 遮罩层
}

const ChartCard: React.FC<ChartCardProps> = props => {
    const {style, span, awaitLoad, chart, uniqueId, editFunction, overlay} = props;

    return (
        <Col style={style} span={span || 8}>
            <Metric
                awaitLoad={awaitLoad}
                chart={chart}
                key={uniqueId}
                uniqueId={uniqueId}
                editFunction={editFunction}
                extraLinks={<MetaLink hiddenApp={true} key={4} targets={chart.targets}/>}
                overlay={overlay}
            />
        </Col>
    );
};

export default ChartCard;
