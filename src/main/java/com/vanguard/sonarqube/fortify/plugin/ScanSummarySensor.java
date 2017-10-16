package com.vanguard.sonarqube.fortify.plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.measures.CountDistributionBuilder;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.ResourceUtils;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rule.Severity;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.vanguard.sonarqube.fortify.model.FortifyConstants;
import com.vanguard.sonarqube.fortify.model.FortifyRules;
import com.vanguard.sonarqube.fortify.model.FortifyService;
import com.vanguard.sonarqube.fortify.model.xml.FortifyIssue;
import com.vanguard.sonarqube.fortify.model.xml.FortifyResultsOutline;
import com.vanguard.sonarqube.fortify.model.xml.FortifySeverityMapping;
import com.vanguard.sonarqube.fortify.model.xml.FortifyXMLReport;
import com.vanguard.sonarqube.fortify.util.FPRDownloader;
import com.vanguard.sonarqube.fortify.util.PDFGenerator;
import com.vanguard.sonarqube.fortify.util.TestFPRDownloader;

public class ScanSummarySensor implements Sensor {
	
	private static final Logger LOG = Loggers.get(ScanSummaryPlugin.class);
	
	private Settings settings;
	private ResourcePerspectives perspectives;
	private ActiveRules rules;
	private FileSystem fileSystem;
	
	public ScanSummarySensor(Settings settings, ResourcePerspectives p, ActiveRules rules, FileSystem fileSystem){
		this.settings = settings;
		this.perspectives = p;
		this.rules = rules;
		this.fileSystem = fileSystem;
	}
	
	public boolean shouldExecuteOnProject(Project project) {
		if(settings != null && !settings.getBoolean(FortifyConstants.PLUGIN_ENABLE_KEY)){
			LOG.info("SonarQube-Fortify Plugin is disabled");
			return false;
		}
		
		if(!ResourceUtils.isRootProject(project)){
			LOG.info("Skipping Analysis - Not a root project " + project.getName());
			return false;
		}
		
		LOG.info("Running Analysis for=>" + project.getName());
		return true;
	}
	
	public void analyse(Project project, SensorContext sensorContext) {
		if (settings != null) {
			String projKey = project.key();
			
			FPRDownloader fpr_dl = new TestFPRDownloader(this.settings, project);
			
			String fortify_xml_report_url = fpr_dl.buildSCAPath("bin", "tradeticket.xml");
			LOG.info("Findings report URL=>" + fortify_xml_report_url);
			
			FortifyService service = new FortifyService(fortify_xml_report_url);
			
			LOG.info("Project Key: "+projKey);
			PDFGenerator.publish_pdf(projKey, "admin", "admin");
			
			LOG.info("Parsing Fortify findings...");
			String[] reportWhitelist = settings.getStringArray(FortifyConstants.REPORT_WHITELIST_KEY);
			
			FortifySeverityMapping severityMapping = FortifySeverityMapping.makeSeverityMapper(settings);
			FortifyXMLReport report = service.downloadAndParseReport(fpr_dl, reportWhitelist, severityMapping);
			
			List<String> severities = Severity.ALL;
			CountDistributionBuilder countMeasure = new CountDistributionBuilder(FortifyMetrics.FINDINGS);
			
			Map<String, String> ruleNameMap = FortifyRules.parseRuleNameKeyMap(getClass().getResourceAsStream(FortifyConstants.JAVA_RULE_FILE));
			ruleNameMap.putAll(FortifyRules.parseRuleNameKeyMap(getClass().getResourceAsStream(FortifyConstants.XML_RULE_FILE)));
			ruleNameMap.putAll(FortifyRules.parseRuleNameKeyMap(getClass().getResourceAsStream(FortifyConstants.JS_RULE_FILE)));
			
			File unk_rule_log = new File(FortifyConstants.UNK_RULE_LOG_PATH);
			Map<String, Boolean> unk_rule_map = new HashMap<String, Boolean>();
			FileWriter unk_rule_out = null;
			try {
				unk_rule_out = new FileWriter(unk_rule_log, true);
				unk_rule_out.write(String.format("--%tc--%n", Calendar.getInstance()));
				LOG.info("Unknown rules log: "+unk_rule_log.getAbsolutePath());
			} catch (IOException e) {
				LOG.error("Failed to create unknown rules log file!",  e);
			}
			
			Map<String, Integer> categoryCount = new HashMap<String, Integer>();
			
			String[] cur_repo = {};
			
			String srcpath = this.settings.getString("sonar.sources");
			
			FortifyResultsOutline outline = report.getResultsOutline();
			if (outline == null) {
				LOG.error("The results outline must be whitelisted in order for the plugin to find Fortify issues! Add 'Results Outline' to the whitelist.");
				return;
			}
			int totalCount = 0;
			for(String s:severities) {
				int sev_count = 0;
				List<FortifyIssue> f_issues = outline.getIssues(s);
				for (FortifyIssue i : f_issues) {
					InputFile rsc = this.resourceOf(srcpath, i, project);
					Issuable issuable;
					if (rsc != null) {
						issuable = this.perspectives.as(Issuable.class, (Resource) sensorContext.getResource(rsc));
					} else {
						issuable = this.perspectives.as(Issuable.class, (Resource) project);
					}
					if (issuable == null) {
						LOG.error("Issuable was null!");
						continue;
					}
					
					try {
						cur_repo = FortifyRules.selectRepositoryFromFileName(i.getFileName());
						RuleKey rule_key = null;
						for (String repo:cur_repo) {
							try {
								rule_key = RuleKey.of(repo, ruleNameMap.get(i.getCategory()));
								if (rule_key != null) break;
							} catch(Exception e) {
								continue;
							}
						}
						ActiveRule rule = this.rules.find(rule_key);
						if (rule != null) {
							Integer count = categoryCount.getOrDefault(i.getCategory(), categoryCount.size());
							Issue issue;
							if (rsc != null) {
								issue = issuable.newIssueBuilder()
										.ruleKey(rule_key)
										.severity(i.getSeverity())
										.message(String.format("%02d %s", count.intValue(), i.getCategory()))
										.line(i.getLineStart())
										.build();
							} else {
								issue = issuable.newIssueBuilder()
										.ruleKey(rule_key)
										.severity(i.getSeverity())
										.message(String.format("%02d %s", count.intValue(), i.getCategory()))
										.build();
							}
							issuable.addIssue(issue);
							sev_count++;
							categoryCount.put(i.getCategory(), count);
						} else {
							LOG.warn("Skipping issue "+i.getIid());
						}
					} catch (Exception e) {
						if (unk_rule_out != null && !unk_rule_map.getOrDefault(i.getCategory(), false)) {
							try {
								unk_rule_out.write(String.format("%s%n", i.toString()));
								unk_rule_out.flush();
								unk_rule_map.put(i.getCategory(), true);
							} catch (IOException ioe) {
								LOG.error("Failed to write Fortify issue to missing rules file", ioe);
								try {
									unk_rule_out.close();
								} catch (IOException ex) {}
								unk_rule_out = null;
							}
						}
						if (unk_rule_out == null) {
							LOG.warn("Failed to create an issue due to missing rule");
							LOG.warn("  "+i.toString(), e);
						}
					}
				}
				
				totalCount += sev_count;
				countMeasure.add(s, sev_count);
				sensorContext.saveMeasure(new Measure<Integer>(FortifyMetrics.getMetric(s), (double) sev_count));
			}
			if (unk_rule_out != null) {
				try {
					unk_rule_out.close();
				} catch (IOException e) {}
			}
			
			LOG.info("Total findings => "+totalCount);
			
			Measure<Integer> uniqueCategoryMeasure = new Measure<Integer>(FortifyMetrics.UNIQUE_CATEGORIES);
			uniqueCategoryMeasure.setIntValue(categoryCount.size());
			sensorContext.saveMeasure(uniqueCategoryMeasure);
			
			@SuppressWarnings("rawtypes")
			Measure issueMeasure = countMeasure.build();
			sensorContext.saveMeasure(issueMeasure.setValue((double) totalCount));
			LOG.info("All measures saved");
		} else {
			LOG.error("Settings object was null!");
		}
	}
	
	/**
	 * @param sourceBasePath This string is either a path to a resource stored in src/main/resources (accessible from root) or any other
	 * 			resource on the system if given an absolute path.
	 * @param issue The fortify issue derived from the requested source code resource.
	 * @param project The project object from the Sensor's analyze function.
	 * @return Returns a non-null InputFile if the requested resource with the issue's file path was found, or null if otherwise.
	 */
	private InputFile resourceOf(String sourceBasePath, FortifyIssue issue, Project project) {
		String safepath = issue.getFilePath().replaceAll("[\\\\/]", Matcher.quoteReplacement(""+File.separatorChar));

		String newpath = String.format("file:**%c%s", File.separatorChar, safepath);
		InputFile f = this.fileSystem.inputFile(this.fileSystem.predicates().matchesPathPattern(
				newpath
				));
		
		if (f == null) {
			// This can occur when the path of the issue contains a parent directory of the provided source, like "target/"
			// We'll recursively call on the parent of the issue and try again
			// TODO: review the above logic
			int parent_sep = safepath.indexOf(File.separator);
			if (parent_sep == -1) return null;
			else {
				issue.setFilePath(safepath.substring(parent_sep+1));
				return this.resourceOf(sourceBasePath, issue, project);
			}
		} else {
			return f;
		}
	}
}
