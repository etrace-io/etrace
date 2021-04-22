import {ToolKit} from "$utils/Util";
import React, {useEffect, useState} from "react";
import LoginPage from "$containers/Login/LoginPage";
import {APP_BASE_PATHNAME, LOGIN_BACK_URL_PARAM} from "$constants/index";
import {Redirect, Route, Switch, useHistory} from "react-router-dom";

const AuthRoute: React.FC<{
    authorized?: boolean;
    back?: string;
}> = props => {
    const {authorized, back, children} = props;

    const [backPath, setBackPath] = useState<string>();
    const history = useHistory();

    useEffect(() => {
        if (!back) { return; }
        try {
            const url = new URL(back);
            if (window.location.pathname !== url.pathname) {
                const pathname = url.pathname.replace(APP_BASE_PATHNAME, "");
                setBackPath(pathname + url.search);
            }
        } catch (err) {
            console.warn("重定向链接不合法");
        }
    }, [back]);

    useEffect(() => {
        if (backPath && authorized) {
            history.replace(backPath);
        }
    }, [backPath]);

    return (
        <Switch>
            <Route path="/login" exact={true} component={LoginPage}/>
            {!authorized && <Route
                render={({ location }) => (
                    <Redirect
                        push={true}
                        to={{
                            pathname: "/login",
                            search: ToolKit.paramsToURLSearch({[LOGIN_BACK_URL_PARAM]: back}),
                            // state: { from: location }
                        }}
                    />
                )}
            />}

            {children}
        </Switch>
    );
};

export default AuthRoute;
