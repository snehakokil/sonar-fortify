package com.vanguard.sonarqube.fortify.util;

import java.io.File;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class PDFGenerator {
	private static final Logger LOG = Loggers.get(PDFGenerator.class);

	public static void publish_pdf(String project_key, String username, String password) {
		File file = new File("C:/Users/bcefali/Documents/sts/workspace/Vanguard/sonarqube-fortify-plugin/branches/4.0.0/src/main/resources/"+project_key.replaceAll("[:.]", "-")+".pdf");
		String url = "http://localhost:9000/sonarqubefortifyplugin/store";
		if (file.exists()) {
			FileUploader.upload(file, url, "admin", "admin");
		} else {
			LOG.error("PDF not found!");
		}
	}

}
