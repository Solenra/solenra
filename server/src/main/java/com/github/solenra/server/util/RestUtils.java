package com.github.solenra.server.util;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public class RestUtils {

	/**
	 * Determine the columns and sort order
	 *
	 * @param sort The list of columns and direction to sort on
	 * @return The sort order to be used
	 */
	public static List<Sort.Order> getSortOrder(List<String> sort, List<String> handleNullOrderForProperties) {
		// determine the columns and sort order
		List<Sort.Order> orders = new ArrayList<Sort.Order>();
		for (String propOrder : sort) {
			String[] propOrderSplit = propOrder.split(":");
			String property = propOrderSplit[0];
			if (propOrderSplit.length == 1) {
				orders.add(Sort.Order.by(property));
			} else {
				Sort.Direction direction = Sort.Direction.ASC;
				if (propOrderSplit[1] != null && "DESC".equalsIgnoreCase(propOrderSplit[1])) {
					direction = Sort.Direction.DESC;
				}
				if (handleNullOrderForProperties != null && handleNullOrderForProperties.contains(property)) {
					/*
					 * special sort order case to handle nulls on specific fields
					 * Note null handling doesn't appear to work
					 * https://stackoverflow.com/questions/43459031/spring-data-jpa-order-with-nullhandling-postgres
					 * https://jira.spring.io/browse/DATAJPA-825
					 * reverse sort order so records with null values are ordered last when DESC and first when ASC
					 */
					if ("DESC".equalsIgnoreCase(propOrderSplit[1])) {
						orders.add(new Sort.Order(direction, property, Sort.NullHandling.NULLS_LAST));
					} else {
						orders.add(new Sort.Order(direction, property, Sort.NullHandling.NULLS_FIRST));
					}
				} else {
					orders.add(new Sort.Order(direction, property));
				}
			}
		}

		return orders;
	}

	public static List<Sort.Order> getSortOrder(List<String> sort) {
		return getSortOrder(sort, null);
	}

}
