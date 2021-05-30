import {get} from "lodash";
import React from "react";
import {Helmet} from "react-helmet";
import {ToolKit} from "$utils/Util";
import {EMONITOR_LOGO_LIGHT} from "$constants/index";

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
            <meta property="og:image" content={EMONITOR_LOGO_LIGHT}/>
            <meta property="og:title" content={wrappedTitle} />
            <meta property="og:description" content={desc} />
        </Helmet>
    );
};

export default EMonitorMeta;
