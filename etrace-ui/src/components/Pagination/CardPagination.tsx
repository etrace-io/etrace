import React from "react";
import {Card, Pagination} from "antd";
import {PaginationProps} from "antd/lib/pagination/Pagination";
import {DEFAULT_PAGE_SIZE} from "$components/Pagination/Pagination";

interface PaginationCardProps extends PaginationProps {
}

const CardPagination: React.FC<PaginationCardProps> = props => {
    const {
        defaultPageSize = DEFAULT_PAGE_SIZE,
        onChange,
        total,
        current,
        pageSizeOptions
    } = props;

    return (
        <Card size="small">
            <Pagination
                defaultPageSize={defaultPageSize}
                style={{ textAlign: "right" }}
                current={current}
                total={total}
                showTotal={t => `总共 ${t} 条`}
                onChange={onChange}
                pageSizeOptions={pageSizeOptions || [`${defaultPageSize}`, `${defaultPageSize * 2}`]}
            />
        </Card>
    );
};

export default CardPagination;