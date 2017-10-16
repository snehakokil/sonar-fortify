package com.vanguard.sonarqube.fortify.model;

import java.util.HashMap;
import java.util.Map;

/**
 * This class serves as constants file for the plugin. 
 * Any change to the string constant values in this file will have effects on settings,
 * filenames and comparisons in decision blocks.
 * 
 * @author Brian Cefali 
 *  */
public class FortifyConstants {
	
	/*
	 * Constants for SonarQube plugin settings.
	 * 
	 * */
	public static final String PLUGIN_ENABLE_KEY = "sq.fortify.enable";
	public static final String MERGE_TIME_KEY = "sq.fortify.mergetime";
	public static final String SSC_URL_KEY = "sq.ssc.url";
	public static final String SSC_TOKEN_KEY = "123abc";
	public static final String SCA_PATH_KEY = "sq.sca.path";
	public static final String REPORT_WHITELIST_KEY = "sq.fortify.whitelist";
	
	public static final String LANGUAGE_NAME = "Fortify";
	public static final String LANGUAGE_KEY = "sq.fortify.lang";
	public static final String JAVA_REPOSITORY_KEY = "sq.fortify.lang.java";
	public static final String PROFILE_NAME = "Fortify Profile";
	public static final String JAVA_RULE_FILE = "/rules-java.xml";
	public static final String JAVA_FILE_EXTS = ".java, .jsp, .html, .xhtml, .htm";
	public static final String JS_REPOSITORY_KEY = "sq.fortify.lang.js";
	public static final String JS_RULE_FILE = "/rules-js.xml";
	public static final String JS_FILE_EXTS = ".js";
	public static final String XML_REPOSITORY_KEY = "sq.fortify.lang.xml";
	public static final String XML_RULE_FILE = "/rules-xml.xml";
	public static final String XML_FILE_EXTS = ".xml";
	public static final String[] REPOSITORY_KEYS = {JAVA_REPOSITORY_KEY, JS_REPOSITORY_KEY, XML_REPOSITORY_KEY};
	public static final Map<String, String> EXT_MAP;
	static {
		EXT_MAP = new HashMap<String, String>();
		for (String s:JAVA_FILE_EXTS.split(","))
			EXT_MAP.put(s.trim(), JAVA_REPOSITORY_KEY);
		for (String s:JS_FILE_EXTS.split(","))
			EXT_MAP.put(s.trim(), JS_REPOSITORY_KEY);
		for (String s:XML_FILE_EXTS.split(","))
			EXT_MAP.put(s.trim(), XML_REPOSITORY_KEY);
	}
	
	public static final String HIGH_CONF_HIGH_KEY = "high_confidence_high";
	public static final String INV_CONF_HIGH_KEY = "investigate_confidence_high";
	public static final String HIGH_CONF_LOW_KEY = "high_confidence_low";
	public static final String INV_CONF_LOW_KEY = "investigate_confidence_low";
	public static final String SEVERITY_EXCEPTIONS_KEY = "severity_mapping_exceptions";
	
	/*
	 * Constants for SonarQube plugin processing logic and metrics population.
	 * 
	 * */
	public static final String FORTIFY_FINDINGS_FILE = "Vanguard-fortify-findings.txt";
	public static final String HIGH_CONF_HIGH = "High Confidence-High";
	public static final String INV_CONF_HIGH = "Investigate-High";
	public static final String HIGH_CONF_LOW = "High Confidence-Low";
	public static final String INV_CONF_LOW = "Investigate-Low";
	
	/*
	 * Constants for file names and report paths.
	 * 
	 * */
	public static final String PART_URL_PDF = "/view/All/job/";
	public static final String PART_URL_XML = "/ws/" + FORTIFY_FINDINGS_FILE;
	public static final String UNK_RULE_LOG_PATH = "unk_rule_log.txt";
	
	public static final String SNAPSHOT = "-SNAPSHOT";
}
