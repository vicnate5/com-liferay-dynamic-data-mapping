package com.liferay.dynamic.data.mapping.web.internal.taglib;

import com.liferay.portal.kernel.servlet.taglib.BaseDynamicInclude;
import com.liferay.portal.kernel.servlet.taglib.DynamicInclude;
import com.liferay.portal.kernel.servlet.taglib.DynamicInclude.DynamicIncludeRegistry;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.WebKeys;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author harlan.santos
 *
 */
@Component(immediate = true, service = DynamicInclude.class)
public class DDMWebTopHeadDynamicInclude extends BaseDynamicInclude {

	@Activate
	public void activate() {
		_postfix = _portal.getPathProxy();

		if (_postfix.isEmpty()) {
			_postfix = _servletContext.getContextPath();
		}
		else {
			_postfix = _postfix.concat(_servletContext.getContextPath());
		}

		_postfix = _postfix.concat(
			"/css/main.css\" rel=\"stylesheet\" type = \"text/css\" />");
	}

	@Override
	public void include(
			HttpServletRequest request, HttpServletResponse response,
			String key)
		throws IOException {

		ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(
			WebKeys.THEME_DISPLAY);

		PrintWriter printWriter = response.getWriter();

		String content = "<link href=\"".concat(themeDisplay.getPortalURL());

		printWriter.println(content.concat(_postfix));
	}

	@Override
	public void register(DynamicIncludeRegistry dynamicIncludeRegistry) {
		dynamicIncludeRegistry.register("/html/common/themes/top_head.jsp#pre");
	}

	@Reference
	private Portal _portal;

	private String _postfix;

	@Reference(
		target = "(osgi.web.symbolicname=com.liferay.dynamic.data.mapping.web)"
	)
	private ServletContext _servletContext;

}