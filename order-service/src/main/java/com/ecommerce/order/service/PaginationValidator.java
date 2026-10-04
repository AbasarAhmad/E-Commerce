package com.ecommerce.order.service;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.ecommerce.order.exception.InvalidOrderRequestException;
import com.ecommerce.order.mapper.PaginationConstants;

@Component
public class PaginationValidator {

	private static final Logger log = LoggerFactory.getLogger(PaginationValidator.class);

	private static final Set<String> ALLOWED_ORDER_SORT_FIELDS = Set.of("createdAt", "updatedAt", "totalAmount",
			"status");

	private static final Set<String> ALLOWED_HISTORY_SORT_FIELDS = Set.of("changedAt", "status");

	public void validate(Pageable pageable) {

		if (pageable.getPageNumber() < 0) {
			log.warn("Invalid page number: {}", pageable.getPageNumber());

			throw new InvalidOrderRequestException("Page number cannot be negative");
		}

		if (pageable.getPageSize() > PaginationConstants.MAX_PAGE_SIZE) {
			log.warn("Page size exceeds maximum limit. requested={}, max={}", pageable.getPageSize(),
					PaginationConstants.MAX_PAGE_SIZE);

			throw new InvalidOrderRequestException("Page size cannot exceed " + PaginationConstants.MAX_PAGE_SIZE);
		}
	}

	public void validateOrderSorting(Pageable pageable) {

		// Only allow known fields to prevent invalid or unexpected database sorting.
		for (Sort.Order order : pageable.getSort()) {

			if (!ALLOWED_ORDER_SORT_FIELDS.contains(order.getProperty())) {
				log.warn("Invalid order sorting field requested: {}", order.getProperty());

				throw new InvalidOrderRequestException("Sorting by '" + order.getProperty() + "' is not allowed");
			}
		}
	}

	public void validateHistorySorting(Pageable pageable) {

		// History has its own allowed sorting fields.
		for (Sort.Order order : pageable.getSort()) {

			if (!ALLOWED_HISTORY_SORT_FIELDS.contains(order.getProperty())) {
				log.warn("Invalid history sorting field requested: {}", order.getProperty());

				throw new InvalidOrderRequestException("Sorting by '" + order.getProperty() + "' is not allowed");
			}
		}
	}

	public Pageable applyDefaultSort(
			Pageable pageable,
			String defaultSortField,
			Sort.Direction defaultDirection) {

		if (!pageable.getSort().isUnsorted()) {
			return pageable;
		}

		log.debug("Applying default pagination sort. field={}, direction={}",
				defaultSortField, defaultDirection);

		return PageRequest.of(
				pageable.getPageNumber(),
				pageable.getPageSize(),
				Sort.by(defaultDirection, defaultSortField));
	}
}