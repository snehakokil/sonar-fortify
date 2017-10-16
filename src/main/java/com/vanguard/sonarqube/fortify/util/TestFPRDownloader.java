package com.vanguard.sonarqube.fortify.util;

import java.util.concurrent.TimeUnit;

import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.vanguard.sonarqube.fortify.model.FortifyConstants;

public class TestFPRDownloader extends FPRDownloader {
	private static final Logger LOG = Loggers.get(TestFPRDownloader.class);

	public TestFPRDownloader(Settings settings, Project project) {
		super(settings, project);
	}
	
	public boolean download_fpr() {
		String projectName, projectVersion, targetFileName;
		
		projectName = "tradeticket";
		projectVersion = "2016.0500.0";
		targetFileName = buildSCAPath("bin", "tradeticket.fpr");
		
		ProcessBuilder pb = new ProcessBuilder(
				"cmd", "/C",
				buildSCAPath("bin", "fortifyclient.bat"),
				"downloadFPR",
				"-file", targetFileName,
				"-project", projectName,
				"-version", projectVersion,
				"-url", settings.getString(FortifyConstants.SSC_URL_KEY),
				"-authtoken", FortifyConstants.SSC_TOKEN_KEY
				);
		LOG.info("Downloading FPR");
		LOG.debug(pb.command().toString());
		try {
			Process download_proc = pb.start();
			int err = download_proc.waitFor();
			download_proc.destroy();
			if (err == 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			LOG.error("Failed to download FPR!", e);
		}
		
		return false;
	}

	public boolean generate_report() {
		String targetFileName = buildSCAPath("bin", "tradeticket.xml");
		String sourceFPR = buildSCAPath("bin", "tradeticket.fpr");
		
		ProcessBuilder pb = new ProcessBuilder(
				"cmd", "/C",
				buildSCAPath("bin", "ReportGenerator.bat"),
				"-template", buildSCAPath("bin", "VanguardTemplate.xml"),
				"-format", "xml",
				"-f", targetFileName,
				"-source", sourceFPR
				);
		LOG.info("Building XML");
		LOG.debug(pb.command().toString());
		try {
			Process xml_gen_proc = pb.start();
			if (!xml_gen_proc.waitFor(settings.getLong(FortifyConstants.MERGE_TIME_KEY), TimeUnit.SECONDS)) {
				xml_gen_proc.destroyForcibly();
				return false;
			}
			int err = xml_gen_proc.waitFor();
			xml_gen_proc.destroy();
			if (err == 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			LOG.error("Failed to generate XML report!", e);
		}
		
		return false;
	}
}
