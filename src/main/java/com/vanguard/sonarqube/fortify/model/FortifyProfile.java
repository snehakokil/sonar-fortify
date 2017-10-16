package com.vanguard.sonarqube.fortify.model;

import java.util.Collection;

import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

@SuppressWarnings("deprecation")
public class FortifyProfile extends ProfileDefinition {
	private static final Logger LOG = Loggers.get(FortifyProfile.class);
	
	private RuleFinder rules;
	
	public FortifyProfile(RuleFinder activeRules) {
		this.rules = activeRules;
	}

	@Override
	public RulesProfile createProfile(ValidationMessages vm) {
		LOG.info("Creating a new Fortify Quality Profile");
		RulesProfile profile = RulesProfile.create(FortifyConstants.PROFILE_NAME, FortifyConstants.LANGUAGE_KEY);
		profile.setDefaultProfile(true);
		
		Collection<String> all_keys = FortifyRules.parseRuleNameKeyMap(getClass().getResourceAsStream(FortifyConstants.JAVA_RULE_FILE)).values();
		for (String rule_key:all_keys) {
			Rule rule = rules.findByKey(RuleKey.of(FortifyConstants.JAVA_REPOSITORY_KEY, rule_key));
			profile.activateRule(rule, RulePriority.CRITICAL);
		}
		
		all_keys = FortifyRules.parseRuleNameKeyMap(getClass().getResourceAsStream(FortifyConstants.JS_RULE_FILE)).values();
		for (String rule_key:all_keys) {
			Rule rule = rules.findByKey(RuleKey.of(FortifyConstants.JS_REPOSITORY_KEY, rule_key));
			profile.activateRule(rule, RulePriority.CRITICAL);
		}
		
		all_keys = FortifyRules.parseRuleNameKeyMap(getClass().getResourceAsStream(FortifyConstants.XML_RULE_FILE)).values();
		for (String rule_key:all_keys) {
			Rule rule = rules.findByKey(RuleKey.of(FortifyConstants.XML_REPOSITORY_KEY, rule_key));
			profile.activateRule(rule, RulePriority.CRITICAL);
		}
		
		return profile;
	}

}
