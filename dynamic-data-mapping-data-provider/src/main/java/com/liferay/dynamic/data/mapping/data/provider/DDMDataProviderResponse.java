/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.dynamic.data.mapping.data.provider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Leonardo Barros
 */
public class DDMDataProviderResponse {

	public DDMDataProviderResponse() {
		_data = null;
	}

	public DDMDataProviderResponse(List<Map<Object, Object>> data) {
		_data = data;
	}

	public void add(DDMDataProviderResponseTuple tuple) {
		_dataMap.put(tuple._name, tuple);
	}

	public DDMDataProviderResponseTuple get(String name) {
		return _dataMap.get(name);
	}

	public List<Map<Object, Object>> getData() {
		return _data;
	}

	public Map<String, DDMDataProviderResponseTuple> getDataMap() {
		return _dataMap;
	}

	public static class DDMDataProviderResponseTuple {

		public static DDMDataProviderResponseTuple of(
			String name, Object value) {

			return new DDMDataProviderResponseTuple(name, value);
		}

		public String getName() {
			return _name;
		}

		public String getType() {
			return _type;
		}

		public <T> T getValue() {
			return (T)_value;
		}

		private DDMDataProviderResponseTuple(String name, Object value) {
			_name = name;
			_type = null;
			_value = value;
		}

		private final String _name;
		private final String _type;
		private final Object _value;

	}

	private final List<Map<Object, Object>> _data;
	private final Map<String, DDMDataProviderResponseTuple> _dataMap =
		new HashMap<>();

}