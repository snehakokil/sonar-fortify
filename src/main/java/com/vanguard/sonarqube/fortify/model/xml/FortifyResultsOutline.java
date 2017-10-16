package com.vanguard.sonarqube.fortify.model.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class FortifyResultsOutline {
	private static final Logger LOG = Loggers.get(FortifyResultsOutline.class);
	
	private static final String TITLE = FortifyReportElement.RESULTS_OUTLINE.getContent();
	private final String summary_title, summary_desc;
	private final List<FortifyIssue> issues;
	private final Map<String, List<FortifyIssue>> severity_issue_map;

	private FortifyResultsOutline(String summary_title, String summary_desc, List<FortifyIssue> issues, Map<String, List<FortifyIssue>> severity_issue_map) {
		this.summary_title = summary_title;
		this.summary_desc = summary_desc;
		this.issues = Collections.unmodifiableList(issues);
		this.severity_issue_map = Collections.unmodifiableMap(severity_issue_map);
	}

	@Override
	public String toString() {
		return String.format("%s%n%s%n%s", TITLE, this.summary_title, this.issues.toString());
	}
	
	public String getTitle() {
		return TITLE;
	}
	
	public String getSummaryTitle() {
		return this.summary_title;
	}
	
	public String getSummaryDescription() {
		return this.summary_desc;
	}
	
	public List<FortifyIssue> getAllIssues() {
		return Collections.unmodifiableList(this.issues);
	}
	
	public List<FortifyIssue> getIssues(String severity) {
		return Collections.unmodifiableList(this.severity_issue_map.getOrDefault(severity, new ArrayList<FortifyIssue>(0)));
	}
	
	public static FortifyResultsOutline parseResultsOutline(final SMHierarchicCursor cursor, final FortifySeverityMapping severityMap) throws FortifyXMLParseException, XMLStreamException {
		// First subsection, title and other text
		FortifyAssert.True(cursor.advance().getLocalName().equals("SubSection"));

		String sum_title, sum_desc;
		SMInputCursor c = cursor.childElementCursor();

		FortifyAssert.True(c.advance().getLocalName().equals("Title"));
		sum_title = c.getElemStringValue();

		FortifyAssert.True(c.advance().getLocalName().equals("Description"));
		sum_desc = c.getElemStringValue();
		
		FortifyAssert.True(c.advance().getLocalName().equals("IssueListing"));
		c = c.childElementCursor();

		FortifyAssert.True(c.advance().getLocalName().equals("Refinement"));
		// Not saving this

		FortifyAssert.True(c.advance().getLocalName().equals("Chart"));
		c = c.childElementCursor();

		FortifyAssert.True(c.advance().getLocalName().equals("Axis"));
		FortifyAssert.True(c.advance().getLocalName().equals("MajorAttribute"));

		List<FortifyIssue> issues = new ArrayList<FortifyIssue>();
		Map<String, List<FortifyIssue>> severity_issue_map = new HashMap<String, List<FortifyIssue>>();
		
		// Now we iterate through all the issue categories and issues
		while (c.getNext() != null) {
			FortifyAssert.True(c.getLocalName().equals("GroupingSection"));
			Integer count = Integer.parseInt(c.getAttrValue("count"));

			SMHierarchicCursor grp_c = (SMHierarchicCursor) c.childElementCursor();
			
			FortifyAssert.True(grp_c.advance().getLocalName().equals("groupTitle"));
			
			FortifyAssert.True(grp_c.advance().getLocalName().equals("MajorAttributeSummary"));
			/*
			 * MajorAttributeSummary often has a detailed description and remediation writeup as part of the report, however not all grouping sections
			 * have these meta fields. As such, they are not reliable and should not be used. We'll simply ensure that MajorAttributeSummary exists,
			 * and then move on to the issue list.
			 */
			
			int issue_count = 0;
			while (grp_c.getNext() != null) {
				FortifyIssue issue = FortifyIssue.parseIssue(grp_c, severityMap);
				issues.add(issue);
				List<FortifyIssue> list = severity_issue_map.getOrDefault(issue.getSeverity(), new ArrayList<FortifyIssue>());
				list.add(issue);
				severity_issue_map.put(issue.getSeverity(), list);
				issue_count++;
			}
			if (count.intValue() != issue_count) {
				LOG.warn("The number of issues indicated by the GroupingSection count attribute did not match the number of parsed issues!");
				LOG.warn(String.format("Expected %d issues, found %d", count.intValue(), issue_count));
			}
		}
		
		return new FortifyResultsOutline(sum_title, sum_desc, issues, severity_issue_map);
	}
}
