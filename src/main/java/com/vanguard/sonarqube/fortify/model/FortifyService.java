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
package com.vanguard.sonarqube.fortify.model;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.vanguard.sonarqube.fortify.model.xml.FortifySeverityMapping;
import com.vanguard.sonarqube.fortify.model.xml.FortifyXMLParseException;
import com.vanguard.sonarqube.fortify.model.xml.FortifyXMLReport;
import com.vanguard.sonarqube.fortify.util.FPRDownloader;


public class FortifyService {

	private static final Logger LOG = Loggers.get(FortifyService.class);

	private String reportUrl;
	
	public FortifyService(String url) {
		reportUrl = url;
	}

	private boolean download(final FPRDownloader dl) {
		if (!dl.download_fpr()) {
			LOG.error("Failed to download FPR!");
			return false;
		}
		if (!dl.generate_report()) {
			LOG.error("Failed to generate XML report!");
			return false;
		}
		return true;
	}
	
	public FortifyXMLReport downloadAndParseReport(final FPRDownloader dl, final String[] reportWhitelist, final FortifySeverityMapping severityMapping) {
		try {
			if (!this.download(dl))
				return null;
			
			File vuln_file = new File(reportUrl);
			FortifyXMLReport report = FortifyXMLReport.parseReport(vuln_file, reportWhitelist, severityMapping);
			return report;
		} catch (IOException ie) {
			LOG.warn("Exception accessing URL", ie);
		} catch (XMLStreamException e) {
			LOG.error("Exception when parsing XML report", e);
		} catch (FortifyXMLParseException e) {
			LOG.error("Exception in expected Fortify XML format", e);
		}
		return null;
	}
}
