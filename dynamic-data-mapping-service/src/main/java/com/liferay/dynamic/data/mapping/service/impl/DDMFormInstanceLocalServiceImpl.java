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

package com.liferay.dynamic.data.mapping.service.impl;

import com.liferay.dynamic.data.mapping.exception.FormInstanceNameException;
import com.liferay.dynamic.data.mapping.exception.FormInstanceStructureIdException;
import com.liferay.dynamic.data.mapping.model.DDMFormInstance;
import com.liferay.dynamic.data.mapping.model.DDMFormInstanceVersion;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.DDMStructureVersion;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.dynamic.data.mapping.service.base.DDMFormInstanceLocalServiceBaseImpl;
import com.liferay.portal.kernel.bean.BeanReference;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.SystemEventConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.systemevent.SystemEvent;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Leonardo Barros
 */
public class DDMFormInstanceLocalServiceImpl
	extends DDMFormInstanceLocalServiceBaseImpl {

	@Override
	public DDMFormInstance addFormInstance(
			long userId, long groupId, long ddmStructureId,
			Map<Locale, String> nameMap, Map<Locale, String> descriptionMap,
			ServiceContext serviceContext)
		throws PortalException {

		User user = userLocalService.getUser(userId);

		validate(ddmStructureId, nameMap);

		long ddmFormInstanceId = counterLocalService.increment();

		DDMFormInstance ddmFormInstance = ddmFormInstancePersistence.create(
			ddmFormInstanceId);

		ddmFormInstance.setUuid(serviceContext.getUuid());
		ddmFormInstance.setGroupId(groupId);
		ddmFormInstance.setCompanyId(user.getCompanyId());
		ddmFormInstance.setUserId(user.getUserId());
		ddmFormInstance.setUserName(user.getFullName());
		ddmFormInstance.setStructureId(ddmStructureId);
		ddmFormInstance.setVersion(_VERSION_DEFAULT);
		ddmFormInstance.setNameMap(nameMap);
		ddmFormInstance.setDescriptionMap(descriptionMap);

		DDMFormInstance updatedDDMFormInstance =
			ddmFormInstancePersistence.update(ddmFormInstance);

		if (serviceContext.isAddGroupPermissions() ||
			serviceContext.isAddGuestPermissions()) {

			addFormInstanceResources(
				ddmFormInstance, serviceContext.isAddGroupPermissions(),
				serviceContext.isAddGuestPermissions());
		}
		else {
			addFormInstanceResources(
				ddmFormInstance, serviceContext.getGroupPermissions(),
				serviceContext.getGuestPermissions());
		}

		long structureVersionId = getStructureVersionId(ddmStructureId);

		addFormInstanceVersion(
			structureVersionId, user, ddmFormInstance, _VERSION_DEFAULT,
			serviceContext);

		return updatedDDMFormInstance;
	}

	@Override
	public void addFormInstanceResources(
			DDMFormInstance ddmFormInstance, boolean addGroupPermissions,
			boolean addGuestPermissions)
		throws PortalException {

		resourceLocalService.addResources(
			ddmFormInstance.getCompanyId(), ddmFormInstance.getGroupId(),
			ddmFormInstance.getUserId(), DDMFormInstance.class.getName(),
			ddmFormInstance.getFormInstanceId(), false, addGroupPermissions,
			addGuestPermissions);
	}

	@Override
	public void addFormInstanceResources(
			DDMFormInstance ddmFormInstance, String[] groupPermissions,
			String[] guestPermissions)
		throws PortalException {

		resourceLocalService.addModelResources(
			ddmFormInstance.getCompanyId(), ddmFormInstance.getGroupId(),
			ddmFormInstance.getUserId(), DDMFormInstance.class.getName(),
			ddmFormInstance.getFormInstanceId(), groupPermissions,
			guestPermissions);
	}

	@Override
	@SystemEvent(
		action = SystemEventConstants.ACTION_SKIP,
		type = SystemEventConstants.TYPE_DELETE
	)
	public void deleteFormInstance(DDMFormInstance ddmFormInstance)
		throws PortalException {

		deleteDDMFormInstance(ddmFormInstance);

		resourceLocalService.deleteResource(
			ddmFormInstance.getCompanyId(), DDMFormInstance.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			ddmFormInstance.getFormInstanceId());

		ddmFormInstanceRecordLocalService.deleteFormInstanceRecords(
			ddmFormInstance.getFormInstanceId());

		ddmFormInstanceVersionLocalService.deleteByFormInstanceId(
			ddmFormInstance.getFormInstanceId());

		workflowDefinitionLinkLocalService.deleteWorkflowDefinitionLink(
			ddmFormInstance.getCompanyId(), ddmFormInstance.getGroupId(),
			DDMFormInstance.class.getName(),
			ddmFormInstance.getFormInstanceId(), 0);
	}

	@Override
	public void deleteFormInstance(long ddmFormInstanceId)
		throws PortalException {

		DDMFormInstance ddmFormInstance =
			ddmFormInstancePersistence.findByPrimaryKey(ddmFormInstanceId);

		deleteFormInstance(ddmFormInstance);
	}

	@Override
	public void deleteFormInstances(long groupId) throws PortalException {
		List<DDMFormInstance> ddmFormInstances =
			ddmFormInstancePersistence.findByGroupId(groupId);

		for (DDMFormInstance ddmFormInstance : ddmFormInstances) {
			deleteFormInstance(ddmFormInstance);
		}
	}

	@Override
	public DDMFormInstance fetchFormInstance(long ddmFormInstanceId) {
		return ddmFormInstancePersistence.fetchByPrimaryKey(ddmFormInstanceId);
	}

	@Override
	public DDMFormInstance getFormInstance(long ddmFormInstanceId)
		throws PortalException {

		return ddmFormInstancePersistence.findByPrimaryKey(ddmFormInstanceId);
	}

	@Override
	public DDMFormInstance getFormInstance(String uuid, long ddmFormInstanceId)
		throws PortalException {

		return ddmFormInstancePersistence.findByUUID_G(uuid, ddmFormInstanceId);
	}

	@Override
	public List<DDMFormInstance> getFormInstances(long groupId) {
		return ddmFormInstancePersistence.findByGroupId(groupId);
	}

	@Override
	public int getFormInstancesCount(long groupId) {
		return ddmFormInstancePersistence.countByGroupId(groupId);
	}

	@Override
	public List<DDMFormInstance> search(
		long companyId, long groupId, String keywords, int start, int end,
		OrderByComparator<DDMFormInstance> orderByComparator) {

		return ddmFormInstanceFinder.findByKeywords(
			companyId, groupId, keywords, start, end, orderByComparator);
	}

	@Override
	public List<DDMFormInstance> search(
		long companyId, long groupId, String[] names, String[] descriptions,
		boolean andOperator, int start, int end,
		OrderByComparator<DDMFormInstance> orderByComparator) {

		return ddmFormInstanceFinder.findByC_G_N_D(
			companyId, groupId, names, descriptions, andOperator, start, end,
			orderByComparator);
	}

	@Override
	public int searchCount(long companyId, long groupId, String keywords) {
		return ddmFormInstanceFinder.countByKeywords(
			companyId, groupId, keywords);
	}

	@Override
	public int searchCount(
		long companyId, long groupId, String[] names, String[] descriptions,
		boolean andOperator) {

		return ddmFormInstanceFinder.countByC_G_N_D(
			companyId, groupId, names, descriptions, andOperator);
	}

	@Override
	public DDMFormInstance updateFormInstance(
			long ddmFormInstanceId, long ddmStructureId,
			Map<Locale, String> nameMap, Map<Locale, String> descriptionMap,
			ServiceContext serviceContext)
		throws PortalException {

		DDMFormInstance ddmFormInstance =
			ddmFormInstancePersistence.findByPrimaryKey(ddmFormInstanceId);

		return doUpdateFormInstance(
			serviceContext.getUserId(), ddmStructureId, nameMap, descriptionMap,
			serviceContext, ddmFormInstance);
	}

	protected DDMFormInstanceVersion addFormInstanceVersion(
			long ddmStructureVersionId, User user,
			DDMFormInstance ddmFormInstance, String version,
			ServiceContext serviceContext)
		throws PortalException {

		long ddmFormInstanceVersionId = counterLocalService.increment();

		DDMFormInstanceVersion ddmFormInstanceVersion =
			ddmFormInstanceVersionPersistence.create(ddmFormInstanceVersionId);

		ddmFormInstanceVersion.setGroupId(ddmFormInstance.getGroupId());
		ddmFormInstanceVersion.setCompanyId(ddmFormInstance.getCompanyId());
		ddmFormInstanceVersion.setUserId(ddmFormInstance.getUserId());
		ddmFormInstanceVersion.setUserName(ddmFormInstance.getUserName());
		ddmFormInstanceVersion.setCreateDate(ddmFormInstance.getModifiedDate());
		ddmFormInstanceVersion.setFormInstanceId(
			ddmFormInstance.getFormInstanceId());
		ddmFormInstanceVersion.setStructureVersionId(ddmStructureVersionId);
		ddmFormInstanceVersion.setVersion(version);
		ddmFormInstanceVersion.setName(ddmFormInstance.getName());
		ddmFormInstanceVersion.setDescription(ddmFormInstance.getDescription());

		int status = GetterUtil.getInteger(
			serviceContext.getAttribute("status"),
			WorkflowConstants.STATUS_APPROVED);

		ddmFormInstanceVersion.setStatus(status);

		ddmFormInstanceVersion.setStatusByUserId(user.getUserId());
		ddmFormInstanceVersion.setStatusByUserName(user.getFullName());
		ddmFormInstanceVersion.setStatusDate(ddmFormInstance.getModifiedDate());

		ddmFormInstanceVersionPersistence.update(ddmFormInstanceVersion);

		return ddmFormInstanceVersion;
	}

	protected DDMFormInstance doUpdateFormInstance(
			long userId, long ddmStructureId, Map<Locale, String> nameMap,
			Map<Locale, String> descriptionMap, ServiceContext serviceContext,
			DDMFormInstance ddmFormInstance)
		throws PortalException {

		validate(ddmStructureId, nameMap);

		User user = userLocalService.getUser(userId);

		long oldDDMStructureId = ddmFormInstance.getStructureId();

		ddmFormInstance.setStructureId(ddmStructureId);

		DDMFormInstanceVersion latestDDMFormInstanceVersion =
			ddmFormInstanceVersionLocalService.getLatestFormInstanceVersion(
				ddmFormInstance.getFormInstanceId());

		int status = GetterUtil.getInteger(
			serviceContext.getAttribute("status"),
			WorkflowConstants.STATUS_APPROVED);

		boolean updateVersion = false;

		if ((latestDDMFormInstanceVersion.getStatus() ==
				WorkflowConstants.STATUS_DRAFT) &&
			(status == WorkflowConstants.STATUS_DRAFT)) {

			updateVersion = true;
		}

		boolean majorVersion = GetterUtil.getBoolean(
			serviceContext.getAttribute("majorVersion"));

		String version = getNextVersion(
			latestDDMFormInstanceVersion.getVersion(), majorVersion);

		if (!updateVersion) {
			ddmFormInstance.setVersion(version);

			ddmFormInstance.setVersionUserId(user.getUserId());
			ddmFormInstance.setVersionUserName(user.getFullName());
		}

		ddmFormInstance.setNameMap(nameMap);
		ddmFormInstance.setDescriptionMap(descriptionMap);

		DDMFormInstance updatedDDMFormInstance =
			ddmFormInstancePersistence.update(ddmFormInstance);

		long ddmStructureVersionId = getStructureVersionId(ddmStructureId);

		if (updateVersion) {
			updateFormInstanceVersion(
				ddmStructureVersionId, user, ddmFormInstance);
		}
		else {
			addFormInstanceVersion(
				ddmStructureVersionId, user, ddmFormInstance, version,
				serviceContext);
		}

		if (oldDDMStructureId != ddmStructureId) {
			ddmFormInstanceRecordLocalService.deleteFormInstanceRecords(
				ddmFormInstance.getFormInstanceId());
		}

		return updatedDDMFormInstance;
	}

	protected String getNextVersion(String version, boolean majorVersion) {
		int[] versionParts = StringUtil.split(version, StringPool.PERIOD, 0);

		if (majorVersion) {
			versionParts[0]++;
			versionParts[1] = 0;
		}
		else {
			versionParts[1]++;
		}

		return versionParts[0] + StringPool.PERIOD + versionParts[1];
	}

	protected long getStructureVersionId(long ddmStructureId)
		throws PortalException {

		DDMStructure ddmStructure = ddmStructureLocalService.getStructure(
			ddmStructureId);

		DDMStructureVersion ddmStructureVersion =
			ddmStructure.getStructureVersion();

		return ddmStructureVersion.getStructureVersionId();
	}

	protected void updateFormInstanceVersion(
			long ddmStructureVersionId, User user,
			DDMFormInstance ddmFormInstance)
		throws PortalException {

		DDMFormInstanceVersion ddmFormInstanceVersion =
			ddmFormInstanceVersionLocalService.getLatestFormInstanceVersion(
				ddmFormInstance.getFormInstanceId());

		ddmFormInstanceVersion.setUserId(ddmFormInstance.getUserId());
		ddmFormInstanceVersion.setUserName(ddmFormInstance.getUserName());
		ddmFormInstanceVersion.setStructureVersionId(ddmStructureVersionId);
		ddmFormInstanceVersion.setName(ddmFormInstance.getName());
		ddmFormInstanceVersion.setDescription(ddmFormInstance.getDescription());
		ddmFormInstanceVersion.setStatusByUserId(user.getUserId());
		ddmFormInstanceVersion.setStatusByUserName(user.getFullName());
		ddmFormInstanceVersion.setStatusDate(ddmFormInstance.getModifiedDate());

		ddmFormInstanceVersionPersistence.update(ddmFormInstanceVersion);
	}

	protected void validate(long ddmStructureId, Map<Locale, String> nameMap)
		throws PortalException {

		validateStructureId(ddmStructureId);

		validateName(nameMap);
	}

	protected void validateName(Map<Locale, String> nameMap)
		throws PortalException {

		Locale locale = LocaleUtil.getSiteDefault();

		String name = nameMap.get(locale);

		if (Validator.isNull(name)) {
			throw new FormInstanceNameException(
				"Name is null for locale " + locale.getDisplayName());
		}
	}

	protected void validateStructureId(long ddmStructureId)
		throws PortalException {

		DDMStructure ddmStructure = ddmStructureLocalService.fetchStructure(
			ddmStructureId);

		if (ddmStructure == null) {
			throw new FormInstanceStructureIdException(
				"No DDM structure exists with the DDM structure ID " +
					ddmStructureId);
		}
	}

	@BeanReference(type = DDMStructureLocalService.class)
	protected DDMStructureLocalService ddmStructureLocalService;

	private static final String _VERSION_DEFAULT = "1.0";

}