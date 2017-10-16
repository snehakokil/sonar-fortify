package com.vanguard.sonarqube.fortify.util;

import java.nio.file.Paths;

import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;

import com.vanguard.sonarqube.fortify.model.FortifyConstants;

public abstract class FPRDownloader {
	protected Settings settings;
	protected Project project;
	
	public FPRDownloader(Settings settings, Project project) {
		this.settings = settings;
		this.project = project;
	}
	
	public String buildSCAPath(String...parts) {
		return Paths.get(this.settings.getString(FortifyConstants.SCA_PATH_KEY), parts).toString();
	}
	
	public abstract boolean download_fpr();
	public abstract boolean generate_report();
}
