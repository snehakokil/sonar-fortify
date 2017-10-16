package com.vanguard.sonarqube.fortify.model;

import org.sonar.api.resources.AbstractLanguage;

public class FortifyLanguage extends AbstractLanguage {
	public FortifyLanguage() {
		super(FortifyConstants.LANGUAGE_KEY, FortifyConstants.LANGUAGE_NAME);
	}

	public String[] getFileSuffixes() {
		return new String[] {".jsp", ".html", ".xhtml", ".htm"};
	}

}
