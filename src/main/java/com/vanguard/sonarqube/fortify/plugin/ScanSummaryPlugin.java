package com.vanguard.sonarqube.fortify.plugin;

import java.util.List;

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.api.rule.Severity;

import com.google.common.collect.ImmutableList;
import com.vanguard.sonarqube.fortify.model.FortifyConstants;
import com.vanguard.sonarqube.fortify.model.FortifyProfile;
import com.vanguard.sonarqube.fortify.model.FortifyLanguage;
import com.vanguard.sonarqube.fortify.model.FortifyRules;
import com.vanguard.sonarqube.fortify.ui.FortifyDashboardWidget;

/*
 * Properties to add:
 *	- Which rule packs to use (which language)
 *	- URL/Source for merged FPR
 *	- Path to Report Generator binary/batch file
 */

@Properties({
/*
 * Add setting for merge time limit (in seconds)
 * Add info that exceptions apply to all languages, just matching on rule name
 * Fortify SCA installation path (p&g)
 */
	@Property(
			key = FortifyConstants.PLUGIN_ENABLE_KEY,
			name = "Enable Sonar-Fortify Plugin",
			defaultValue = "true",
			global = true,
			project = true,
			type = PropertyType.BOOLEAN
			),

	@Property(
			key = FortifyConstants.MERGE_TIME_KEY,
			name = "FPR Merge Time Upper Limit (in seconds)",
			description = "The maximum amount of time, in seconds, that the application will wait for Fortify reports to merge",
			defaultValue = "60",
			global = true,
			project = true,
			type = PropertyType.INTEGER
			),
	
	@Property(
			key = FortifyConstants.REPORT_WHITELIST_KEY,
			name = "Comma-separated list of top-level report elements to process. If the element is a ReportSection, its title is used instead of its tag.",
			description  = "A comma-separated list of report elements to process. This does not mean that the report elements will be used,"
				+ "but rather they won't be ignored when the XML is processed. If the element is a ReportSection, its title is used instead of its tag.",
			defaultValue = "TemplateName, TemplatePath, LogoPath, Footnote, Results Outline",
			global = true,
			project = true,
			type = PropertyType.STRING
			),
	
	@Property(
			key = FortifyConstants.SSC_URL_KEY,
			name = "FPR URL on SSC",
			description  = "URL to SSC location where FPR's can be downloaded",
			defaultValue = "C:\\Users\\bcefali\\Documents\\sonar-runner-2.4",
			global = true,
			project = true,
			type = PropertyType.STRING
			),
	
	@Property(
			key = FortifyConstants.SCA_PATH_KEY,
			name = "SCA Installation Path",
			description = "Path to local installation of SCA",
			defaultValue = "C:\\Users\\bcefali\\Documents\\Meera\\testing",
			global = true,
			project = true,
			type = PropertyType.STRING
			),

	@Property(
			key = FortifyConstants.HIGH_CONF_HIGH_KEY,
			name = "SonarQube Severity of "+FortifyConstants.HIGH_CONF_HIGH,
			description = "The SonarQube Severity to correspond to all " + FortifyConstants.HIGH_CONF_HIGH + " Fortify fortify findings",
			defaultValue = Severity.BLOCKER,
			global = true,
			project = true,
			type = PropertyType.SINGLE_SELECT_LIST,
			options = {Severity.BLOCKER, Severity.CRITICAL, Severity.MAJOR, Severity.MINOR, Severity.INFO}
			),

	@Property(
			key = FortifyConstants.INV_CONF_HIGH_KEY,
			name = "SonarQube Severity of "+FortifyConstants.INV_CONF_HIGH,
			description = "The SonarQube Severity to correspond to all " + FortifyConstants.INV_CONF_HIGH + " Fortify fortify findings",
			defaultValue = Severity.MINOR,
			global = true,
			project = true,
			type = PropertyType.SINGLE_SELECT_LIST,
			options = {Severity.BLOCKER, Severity.CRITICAL, Severity.MAJOR, Severity.MINOR, Severity.INFO}
			),

	@Property(
			key = FortifyConstants.HIGH_CONF_LOW_KEY,
			name = "SonarQube Severity of "+FortifyConstants.HIGH_CONF_LOW,
			description = "The SonarQube Severity to correspond to all " + FortifyConstants.HIGH_CONF_LOW + " Fortify fortify findings",
			defaultValue = Severity.MAJOR,
			global = true,
			project = true,
			type = PropertyType.SINGLE_SELECT_LIST,
			options = {Severity.BLOCKER, Severity.CRITICAL, Severity.MAJOR, Severity.MINOR, Severity.INFO}
			),

	@Property(
			key = FortifyConstants.INV_CONF_LOW_KEY,
			name = "SonarQube Severity of "+FortifyConstants.INV_CONF_LOW,
			description = "The SonarQube Severity to correspond to all " + FortifyConstants.INV_CONF_LOW + " Fortify fortify findings",
			defaultValue = Severity.INFO,
			global = true,
			project = true,
			type = PropertyType.SINGLE_SELECT_LIST,
			options = {Severity.BLOCKER, Severity.CRITICAL, Severity.MAJOR, Severity.MINOR, Severity.INFO}
			),

	@Property(
			key = FortifyConstants.SEVERITY_EXCEPTIONS_KEY,
			name = "Comma separated key=value pairs of severity mapping rule exceptions",
			description = "Exceptions to the Fortify to SonarQube severity mappings can be made here by providing a list of pairs of Fortify finding names to the desired severity (ex. Trust Boundary Violation=MAJOR, Race Condition: Singleton Member Field=MINOR, ...)",
			defaultValue = "",
			global = true,
			project = true,
			type = PropertyType.TEXT
			)
})

public class ScanSummaryPlugin extends SonarPlugin {

	@SuppressWarnings("rawtypes")
	public List getExtensions() {	
		return ImmutableList.of(ScanSummarySensor.class,
				FortifyMetrics.class,
				FortifyDashboardWidget.class,
				FortifyLanguage.class,
				FortifyRules.class,
				FortifyProfile.class
				);
	}


}
