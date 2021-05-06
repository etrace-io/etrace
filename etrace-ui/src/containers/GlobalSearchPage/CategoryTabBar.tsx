import React from "react";
import classNames from "classnames";
import {SearchResultCategory, SuggestResultGroup} from "../../services/GlobalSearchService";

interface CategoryTabBarProps {
    loading?: boolean;
    className?: string;
    groups?: SuggestResultGroup[];
    currCategory?: SearchResultCategory;
    onChange?: (group: SuggestResultGroup) => void;
}

const CategoryTabBar: React.FC<CategoryTabBarProps> = props => {
    const { className, groups, currCategory, loading, onChange } = props;
    const classString = classNames("search-page__category-tab", {
        "no-result": !groups || groups.length === 0 || loading,
    }, className);

    /**
     * Tab 卡切换触发句柄
     */
    const handleTabChange = (group: SuggestResultGroup) => {
        onChange && onChange(group);
    };

    return (
        <ul className={classString}>
            {groups && groups.map(group => {
                const itemCls = classNames("search-page__tab-item", {
                    "active": group.category === currCategory,
                });
                return (
                    <li key={group.label} className={itemCls} onClick={() => handleTabChange(group)}>
                        {group.label} <span className="search-page__tab-item__summary">{group.category}</span>
                    </li>
                );
            })}
        </ul>
    );
};

export default CategoryTabBar;
