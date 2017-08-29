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

package com.liferay.dynamic.data.mapping.form.evaluator.internal.functions;

import com.liferay.dynamic.data.mapping.expression.DDMExpressionFunction;
import com.liferay.portal.kernel.util.Validator;

import org.osgi.service.component.annotations.Component;

/**
 * @author Harlan Bruno
 */
@Component(
	immediate = true,
	property = "ddm.form.evaluator.function.name=isEmailAddresses",
	service = DDMExpressionFunction.class
)
public class IsEmailAddressesFunction implements DDMExpressionFunction {

	@Override
	public Object evaluate(Object... parameters) {
		
		if (parameters.length != 1) {
			throw new IllegalArgumentException("One parameter is expected");
		}
		
		if(Validator.isNull(parameters[0].toString())) {
			return Boolean.FALSE;
		}
		
		String[] emails = parameters[0].toString().split(",", -1);
		
		for (String email : emails) {
			
			if (!Validator.isEmailAddress(email)) {
				return Boolean.FALSE;
			}
		}
		
		return Boolean.TRUE;
	}

}