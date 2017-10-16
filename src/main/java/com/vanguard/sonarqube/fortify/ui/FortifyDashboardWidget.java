/*
 ****************************************************************************
 *
 * Copyright (c)2009 The Vanguard Group of Investment Companies (VGI)
 * All rights reserved.
 *
 * This source code is CONFIDENTIAL and PROPRIETARY to VGI. Unauthorized
 * distribution, adaptation, or use may be subject to civil and criminal
 * penalties.
 *
 ****************************************************************************
 Module Description:

 $HeadURL:$
 $LastChangedRevision:$
 $Author:$
 $LastChangedDate:$
*/
package com.vanguard.sonarqube.fortify.ui;

import org.sonar.api.web.AbstractRubyTemplate;
import org.sonar.api.web.Description;
import org.sonar.api.web.RubyRailsWidget;
import org.sonar.api.web.UserRole;
import org.sonar.api.web.WidgetCategory;
import org.sonar.api.web.WidgetProperties;
import org.sonar.api.web.WidgetProperty;
import org.sonar.api.web.WidgetPropertyType;


@UserRole(UserRole.USER)
@WidgetCategory("Fortify")
@Description("Widget to display Fortify scan findings")
@WidgetProperties(
{
  @WidgetProperty(key = "defaultDisplay", type = WidgetPropertyType.STRING, defaultValue = "files"),
  @WidgetProperty(key = "defaultColors", type = WidgetPropertyType.STRING, defaultValue = "005CB8,FF9900,2E5C00,660066")
})

public class FortifyDashboardWidget extends AbstractRubyTemplate implements RubyRailsWidget{

	public String getId() {
		return "Fortify Vulnerabilities";
	}

	public String getTitle() {
		return "Fortify Vulnerabilities";
	}

	@Override
	protected String getTemplatePath() {
		return "C:/Users/bcefali/Documents/sts/workspace/Vanguard/sonarqube-fortify-plugin/branches/4.0.0/src/main/resources/fortify_dashboard_widget_html.erb";
	}
}
