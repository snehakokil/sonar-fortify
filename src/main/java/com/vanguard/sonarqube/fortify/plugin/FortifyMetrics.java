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
package com.vanguard.sonarqube.fortify.plugin;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

@SuppressWarnings("rawtypes")
public class FortifyMetrics implements Metrics {
	public static final Metric FINDINGS = new Metric.Builder("Fortify-findings", "Number of Fortify Findings", Metric.ValueType.DISTRIB)
			.setDescription("Number of Fortify findings by Severity")
			.setDomain("Fortify")
			.setDirection(Metric.DIRECTION_WORST)
			.setBestValue(0.0)
			.create();

	public static final Metric JENKINS_URL = new Metric.Builder("jenkinsUrl", "URL for Jenkins project", Metric.ValueType.STRING)
			.setDescription("URL to access reports from Jenkins server")
			.setDomain("Fortify")
			.setDirection(Metric.DIRECTION_NONE)	
			.create();

	public static final Metric BLOCKER = new Metric.Builder("Fortify-blocker", "Blocker findings", Metric.ValueType.INT)
			.setDescription("Blocker findings")
			.setDomain("Fortify")
			.setDirection(Metric.DIRECTION_WORST)
			.setBestValue(0.0)
			.create();

	public static final Metric CRITICAL = new Metric.Builder("Fortify-critical", "Critical findings", Metric.ValueType.INT)
			.setDescription("Critical findings")
			.setDomain("Fortify")
			.setDirection(Metric.DIRECTION_WORST)
			.setBestValue(0.0)
			.create();

	public static final Metric MAJOR = new Metric.Builder("Fortify-major", "Major findings", Metric.ValueType.INT)
			.setDescription("Major findings")
			.setDomain("Fortify")
			.setDirection(Metric.DIRECTION_WORST)
			.setBestValue(0.0)
			.create();

	public static final Metric MINOR = new Metric.Builder("Fortify-minor", "Minor findings", Metric.ValueType.INT)
			.setDescription("Minor findings")
			.setDomain("Fortify")
			.setDirection(Metric.DIRECTION_WORST)
			.setBestValue(0.0)
			.create();

	public static final Metric INFO = new Metric.Builder("Fortify-info", "Info findings", Metric.ValueType.INT)
			.setDescription("Info findings")
			.setDomain("Fortify")
			.setDirection(Metric.DIRECTION_WORST)
			.setBestValue(0.0)
			.create();

	public static final Metric UNIQUE_CATEGORIES = new Metric.Builder("Fortify-categories", "Unique Fortify Categories", Metric.ValueType.INT)
			.setDescription("Total number of different finding categories as reported by Fortify")
			.setDomain("Fortify")
			.setDirection(Metric.DIRECTION_WORST)
			.setBestValue(0.0)
			.create();
	
	public static Metric getMetric(String severity) {
		if (severity.equals("BLOCKER")) return BLOCKER;
		if (severity.equals("CRITICAL")) return CRITICAL;
		if (severity.equals("MAJOR")) return MAJOR;
		if (severity.equals("MINOR")) return MINOR;
		if (severity.equals("INFO")) return INFO;
		return null;
	}

	public List<Metric> getMetrics(){
		return Arrays.asList(FINDINGS,JENKINS_URL,BLOCKER,CRITICAL,MAJOR,MINOR,INFO,UNIQUE_CATEGORIES);
	}

}