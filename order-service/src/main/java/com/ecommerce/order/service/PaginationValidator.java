package com.ecommerce.order.service;

import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.ecommerce.order.exception.InvalidOrderRequestException;
import com.ecommerce.order.mapper.PaginationConstants;

@Component
public class PaginationValidator {

	public void validate(Pageable pageable) {

		if (pageable.getPageNumber() < 0) {
			throw new InvalidOrderRequestException("Page number cannot be negative");
		}

		if (pageable.getPageSize() > PaginationConstants.MAX_PAGE_SIZE) {
			throw new InvalidOrderRequestException("Page size cannot exceed " + PaginationConstants.MAX_PAGE_SIZE);
		}
	}

	private static final Set<String> ALLOWED_ORDER_SORT_FIELDS = Set.of("createdAt", "updatedAt", "totalAmount",
			"status");

	public void validateOrderSorting(Pageable pageable) {

		for (Sort.Order order : pageable.getSort()) {

			if (!ALLOWED_ORDER_SORT_FIELDS.contains(order.getProperty())) {
				throw new InvalidOrderRequestException("Sorting by '" + order.getProperty() + "' is not allowed");
			}
		}
	}

	private static final Set<String> ALLOWED_HISTORY_SORT_FIELDS = Set.of("changedAt", "status");

	public void validateHistorySorting(Pageable pageable) {

		for (Sort.Order order : pageable.getSort()) {

			if (!ALLOWED_HISTORY_SORT_FIELDS.contains(order.getProperty())) {
				throw new InvalidOrderRequestException("Sorting by '" + order.getProperty() + "' is not allowed");
			}
		}
	}

	public Pageable applyDefaultSort(Pageable pageable, String defaultSortField, Sort.Direction defaultDirection) {

		if (!pageable.getSort().isUnsorted()) {
			return pageable;
		}

		return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
				Sort.by(defaultDirection, defaultSortField));
	}

	
}