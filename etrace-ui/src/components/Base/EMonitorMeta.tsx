import {get} from "lodash";
import React from "react";
import {Helmet} from "react-helmet";
import {ToolKit} from "$utils/Util";

interface EmonitorHeaderProps {
    title?: string;
    description?: string;
}

const EMonitorMeta: React.FC<EmonitorHeaderProps> = props => {
    const { title, description } = props;

    const env = get(window, "CONFIG.ENV", "Prod");
    const titleSuffix = `ETrace UI ${ToolKit.firstUpperCase(env)}`;
    const wrappedTitle = [title, titleSuffix].filter(Boolean).join(" | ");
    const defaultDesc = "ETrace UI";

    const desc = description ? description.trim() || defaultDesc : defaultDesc;

    return (
        <Helmet>
            <meta charSet="utf-8" />
            <title>{wrappedTitle}</title>
            <meta name="description" content={desc} />
            <meta property="og:image" content="https://shadow.elemecdn.com/app/monitor/e-monitor-logo_light.bdf2d831-3995-11e9-ba1a-55bba1877129.png" />
            <meta property="og:title" content={wrappedTitle} />
            <meta property="og:description" content={desc} />
        </Helmet>
    );
};

export default EMonitorMeta;
