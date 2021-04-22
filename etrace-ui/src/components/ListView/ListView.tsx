import React from "react";
import {List} from "antd";
import {ListProps} from "antd/lib/list";

import "./ListView.less";
import classNames from "classnames";
import {SPACE_BETWEEN} from "$constants/index";

interface ListViewProps<T> extends ListProps<T> {
}

function ListView<T>(props: ListViewProps<T>) {
    const {
        className,
        loading,
        dataSource,
        renderItem,
        grid = { xxl: 6, xl: 4, lg: 4, md: 4, sm: 3, xs: 2 },
    } = props;

    const classString = classNames("emonitor-list-view", className);

    return (
        <List
            className={classString}
            loading={loading}
            dataSource={dataSource}
            grid={Object.assign({ gutter: SPACE_BETWEEN }, grid)}
            renderItem={(item, index) => (<List.Item>{renderItem(item, index)}</List.Item>)}
        />
    );
}

export default ListView;
