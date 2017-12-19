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

package com.liferay.dynamic.data.mapping.form.analytics.internal.model.listener;

import com.liferay.dynamic.data.mapping.form.analytics.internal.Event;
import com.liferay.dynamic.data.mapping.model.DDMFormInstanceVersion;
import com.liferay.dynamic.data.mapping.model.DDMStructureVersion;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.ModelListener;

import java.time.OffsetDateTime;
import java.time.ZoneId;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * @author Inácio Nery
 */
@Component(immediate = true, service = ModelListener.class)
public class DDMFormInstanceVersionModelListener
	extends DDMFormBaseModelListener<DDMFormInstanceVersion> {

	@Override
	public void onAfterCreate(DDMFormInstanceVersion ddmFormInstanceVersion)
		throws ModelListenerException {

		try {
			Map<String, String> properties = createProperties(
				ddmFormInstanceVersion);

			sendAnalytics(
				Event.FORM_STRUCTURE.name(),
				String.valueOf(ddmFormInstanceVersion.getUserId()), properties);
		}
		catch (Exception e) {
			throw new ModelListenerException(e);
		}
	}

	protected Map<String, String> createProperties(
			DDMFormInstanceVersion ddmFormInstanceVersion)
		throws PortalException {

		Map<String, String> properties = new HashMap<>();

		Date date = ddmFormInstanceVersion.getCreateDate();

		OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(
			date.toInstant(), ZoneId.systemDefault());

		properties.put("date", offsetDateTime.toString());

		DDMStructureVersion ddmStructureVersion =
			ddmFormInstanceVersion.getStructureVersion();

		properties.put("definition", ddmStructureVersion.getDefinition());

		properties.put(
			"description",
			String.valueOf(ddmFormInstanceVersion.getDescription()));
		properties.put(
			"formId",
			String.valueOf(ddmFormInstanceVersion.getFormInstanceId()));
		properties.put("name", ddmFormInstanceVersion.getName());
		properties.put(
			"version", String.valueOf(ddmFormInstanceVersion.getVersion()));

		return properties;
	}

}