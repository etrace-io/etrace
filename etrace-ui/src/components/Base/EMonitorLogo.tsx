import {reaction} from "mobx";
import {Link} from "react-router-dom";
import {THEME_KEY} from "$utils/Theme";
import {Theme} from "$constants/Theme";
import StoreManager from "$store/StoreManager";
import React, {useEffect, useState} from "react";
import {LocalStorageUtil} from "$utils/LocalStorageUtil";

import EMONITOR_LOGO_LIGHT from "$asset/images/pc-namepng.png";
import EMONITOR_LOGO_DARK from "$asset/images/pc-namepng-dark.png";

interface EMonitorLogoProps {
    height?: string | number;
    width?: string | number;
    light?: string; // Light 模式下图片 Src
    dark?: string;  // Dark 模式下图片 Src
    link?: string;  // 是否外包 Link。默认 true
}

const EMonitorLogo: React.FC<EMonitorLogoProps> = props => {
    const {height, width, link, dark, light} = props;
    const {userStore} = StoreManager;

    const lightSrc = light || EMONITOR_LOGO_LIGHT;
    const darkSrc = dark || EMONITOR_LOGO_DARK;

    const currTheme = StoreManager.userStore.getTheme(LocalStorageUtil.get(THEME_KEY, "Light")) as Theme;

    const getLogoWith = (theme: Theme) => {
        switch (theme) {
            case Theme.Dark:
                return darkSrc;
            case Theme.Light:
            default:
                return lightSrc;
        }
    };

    const [logo, setLogo] = useState(() => getLogoWith(currTheme));

    useEffect(() => {
        const disposer = reaction(
            () => userStore.user,
            () => {
                const _theme = StoreManager.userStore.getTheme(LocalStorageUtil.get(THEME_KEY, "Light")) as Theme;
                setLogo(getLogoWith(_theme));
            });

        return () => disposer();
    }, []);

    const img = <img
        className="emonitor-logo"
        src={logo}
        alt="Logo"
        style={{height: height || "auto", width: width || "auto"}}
    />;

    return link
        ? <Link to={link}>{img}</Link>
        : img;
};

export default EMonitorLogo;
