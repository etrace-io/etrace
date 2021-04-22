import {Theme} from "$constants/Theme";

export const THEME_KEY = "THEME";

export function changeThemeTo(theme: string) {
    // 存到 LocalStorage
    localStorage.setItem(THEME_KEY, theme);

    const {search} = window.location;
    const params = new URLSearchParams(search);
    const urlTheme = params.get("theme");

    urlTheme && (theme = urlTheme);
    // 切换 body 的 class
    switchThemeClassTo(theme);
}

export function switchThemeClassTo(theme: string) {
    const bodyCls = document.body.classList;
    // bodyCls.add("e-monitor-theme");
    const allTheme = Object.keys(Theme).map(k => Theme[k].toLocaleLowerCase());
    if (allTheme.indexOf(theme.toLocaleLowerCase()) === -1) {
        theme = Theme.Light;
    }
    allTheme.forEach(t => {
        if (theme.toLocaleLowerCase() === t) {
            bodyCls.add(`theme-${t}`);
        } else {
            bodyCls.remove(`theme-${t}`);
        }
    });
}

export const getPrefixCls = (suffixCls: string, customizePrefixCls?: string) => {
    if (customizePrefixCls) { return customizePrefixCls; }
    return suffixCls ? `emonitor-${suffixCls}` : "emonitor";
};

export const DARK_THEME_CONFIG = {
    "@menu-bg": "#383639",
    "@btn-default-bg": "#666",
    "@component-background": "#292929", // Modal、subMenu背景
    "@border-color-split": "#444444",
    "@primary-5": "#8e8e8d",
    "@heading-color": "#e3e3e3", // 标题色
    "@text-color": "#e3e3e3",  // 主文本色
    "@text-color-secondary": "#b4b4b4", // 次文本色
    "@shadow-color": "rgba(0, 0, 0, 0.45)",
    "@link-color": "#00a5bf"
};

export const DARK_THEME_CHART_CONFIG = {
    chart: {
        backgroundColor: "transparent"
    },
    xAxis: {
        gridLineColor: "#707073",
        lineColor: "#707073",
        tickColor: "#707073",
        labels: {
            style: {
                color: "#E0E0E3"
            }
        },
        title: {
            style: {
                color: "#A0A0A3"
            }
        }
    },
    yAxis: {
        gridLineColor: "#707073",
        lineColor: "#707073",
        tickColor: "#707073",
        tickWidth: 0,
        labels: {
            style: {
                color: "#E0E0E3"
            }
        },
        title: {
            style: {
                color: "#A0A0A3"
            }
        }
    },
    plotOptions: {
        series: {
            dataLabels: {
                color: "#B0B0B3"
            }
        }
    },
    labels: {
        style: {
            color: "#707073"
        }
    }
};

export const LIGHT_THEME_CHART_CONFIG = {
    chart: {
        backgroundColor: "transparent"
    },
    xAxis: {
        gridLineColor: "#e6e6e6",
        lineColor: "#ccd6eb",
        tickColor: "#ccd6eb",
        labels: {
            style: {
                color: "#666",
            }
        },
        title: {
            style: {
                color: "#666",
            }
        }
    },
    yAxis: {
        gridLineColor: "#e6e6e6",
        lineColor: "#ccd6eb",
        tickColor: "#ccd6eb",
        tickWidth: 0,
        labels: {
            style: {
                color: "#666",
            }
        },
        title: {
            style: {
                color: "#666",
            }
        }
    },
    plotOptions: {
        series: {
            dataLabels: {
                color: undefined
            }
        }
    },
    labels: {
        style: {
            color: "#666"
        }
    },
};
