package com.ecommerce.order.mapper;

import org.springframework.data.domain.Sort;

public final class PaginationConstants {

    private PaginationConstants() {
    }

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 50;
    public static final String DEFAULT_ORDER_SORT_FIELD = "createdAt";
    public static final Sort.Direction DEFAULT_ORDER_SORT_DIRECTION =
            Sort.Direction.DESC;

    public static final String DEFAULT_ORDER_ITEM_SORT_FIELD = "createdAt";
    public static final Sort.Direction DEFAULT_ORDER_ITEM_SORT_DIRECTION =
            Sort.Direction.DESC;

    public static final String DEFAULT_HISTORY_SORT_FIELD = "changedAt";
    public static final Sort.Direction DEFAULT_HISTORY_SORT_DIRECTION =
            Sort.Direction.ASC;
}