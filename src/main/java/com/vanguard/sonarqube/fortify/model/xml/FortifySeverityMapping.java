package com.vanguard.sonarqube.fortify.model.xml;

import java.util.HashMap;
import java.util.Map;

import org.sonar.api.config.Settings;
import org.sonar.api.rule.Severity;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.vanguard.sonarqube.fortify.model.FortifyConstants;

/**
 * Helper class to manage the storage of severity mapping exceptions along with the mappings
 * themselves. This class is to be constructed with the static factory method only. Once the
 * class object is populated with rules and exceptions, it is immutable.
 * 
 * @author Brian Cefali
 */
public class FortifySeverityMapping {
	private static final Logger LOG = Loggers.get(FortifySeverityMapping.class);
	
	private final Map<String, String> exceptions, rules;
	
	private FortifySeverityMapping() {
		this.exceptions = new HashMap<String, String>();
		this.rules = new HashMap<String, String>();
	}
	
	/**
	 * Returns the SonarQube severity for a Fortify issue given its category (finding type) and it's severity as categorized by
	 * Fortify (finding folder), using the exceptions list and default rules as the basis for said judgment.
	 * 
	 * @param category Category of the issue from the Fortify XML report, Cross-Site Scripting, SQL Injection, etc.
	 * @param f_severity Fortify severity string from the Fortify XML report, High Investigate-High, etc.
	 * @return Returns the SonarQube severity for a given issue; matching the values in {@link Severity}
	 */
	public String getSeverity(String category, String f_severity) {
		if (this.exceptions.containsKey(category))
			return this.exceptions.get(category);
		else if(this.rules.containsKey(f_severity))
			return this.rules.get(f_severity);
		LOG.error(String.format("Unable to determine the severity of %s with Fortify severity %s", category, f_severity));
		return null;
	}
	
	private boolean addException(String exceptionName, String severity) {
		return this.exceptions.put(exceptionName, severity) == null;
	}
	
	private boolean setRule(String fortifySeverity, String severity) {
		return this.rules.put(fortifySeverity, severity) == null;
	}
	
	/**
	 * @param settings The settings object as provided by the sensor which provides the user-configurable settings from SonarQube.
	 * @return Returns a {@link FortifySeverityMapping} instance with all exceptions and rules populated using the project settings.
	 */
	public static FortifySeverityMapping makeSeverityMapper(Settings settings) {
		FortifySeverityMapping fsm = new FortifySeverityMapping();
		
		String[] exceptions = settings.getStringArrayBySeparator(FortifyConstants.SEVERITY_EXCEPTIONS_KEY, ",");
		for (String s:exceptions) {
			String[] kv = s.split("=");
			if (kv.length != 2 || !Severity.ALL.contains(kv[1])) {
				LOG.error(String.format("Rule exception %s was not valid!", s));
				continue;
			}
			if (!fsm.addException(kv[0], kv[1])) {
				LOG.error(String.format("An exception for the rule %s was already present. The older severity mapping was overwritten.", kv[0]));
			}
		}
		
		
		if (!(fsm.setRule(FortifyConstants.HIGH_CONF_HIGH, settings.getString(FortifyConstants.HIGH_CONF_HIGH_KEY)) &&
			fsm.setRule(FortifyConstants.INV_CONF_HIGH, settings.getString(FortifyConstants.INV_CONF_HIGH_KEY)) &&
			fsm.setRule(FortifyConstants.HIGH_CONF_LOW, settings.getString(FortifyConstants.HIGH_CONF_LOW_KEY)) &&
			fsm.setRule(FortifyConstants.INV_CONF_LOW, settings.getString(FortifyConstants.INV_CONF_LOW_KEY)))){
			LOG.error("There was an unexpected error when creating a fortify severity mapping!");
		}
		
		return fsm;
	}
}
