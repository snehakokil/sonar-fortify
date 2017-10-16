package com.vanguard.sonarqube.fortify.model.xml;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;

public class FortifyIssueSummary {
	private static final String TITLE = FortifyReportElement.ISSUE_SUMMARY.getContent();
	private final String summary_title, summary_desc, summary_text;
	private final String issues_title;
	private final Map<String, Integer> issue_counts;

	private FortifyIssueSummary(String summary_title, String summary_desc, String summary_text,
			String issues_title, Map<String, Integer> issue_counts) {
		this.summary_title = summary_title;
		this.summary_desc = summary_desc;
		this.summary_text = summary_text;
		this.issues_title = issues_title;
		this.issue_counts = Collections.unmodifiableMap(issue_counts);
	}

	@Override
	public String toString() {
		return TITLE+"\n"+this.summary_text+"\n"+this.issue_counts.toString();
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

	public String getSummaryText() {
		return this.summary_text;
	}

	public String getIssuesTitle() {
		return this.issues_title;
	}

	public final Map<String, Integer> getIssueCounts() {
		return this.issue_counts;
	}

	public static FortifyIssueSummary parseIssueSummary(SMHierarchicCursor cursor) throws FortifyXMLParseException, XMLStreamException {
		// First subsection, title and other text
		FortifyAssert.True(cursor.advance().getLocalName().equals("SubSection"));

		String sum_title, sum_desc, sum_txt;
		SMInputCursor c = cursor.childElementCursor();

		FortifyAssert.True(c.advance().getLocalName().equals("Title"));
		sum_title = c.getElemStringValue();

		FortifyAssert.True(c.advance().getLocalName().equals("Description"));
		sum_desc = c.getElemStringValue();

		FortifyAssert.True(c.advance().getLocalName().equals("Text"));
		sum_txt = c.getElemStringValue();

		// Second subsection, issue summary and counts
		FortifyAssert.True(cursor.advance().getLocalName().equals("SubSection"));

		String iss_title;
		Map<String, Integer> iss_counts = new HashMap<String, Integer>();
		c = cursor.childElementCursor();

		FortifyAssert.True(c.advance().getLocalName().equals("Title"));
		iss_title = c.getElemStringValue();

		FortifyAssert.True(c.advance().getLocalName().equals("IssueListing"));
		c = c.childElementCursor();

		FortifyAssert.True(c.advance().getLocalName().equals("Refinement"));
		// Not saving this

		FortifyAssert.True(c.advance().getLocalName().equals("Chart"));
		c = c.childElementCursor();

		FortifyAssert.True(c.advance().getLocalName().equals("Axis"));
		FortifyAssert.True(c.advance().getLocalName().equals("MajorAttribute"));
		// Not saving either of those
		
		// Now we iterate through all the issue categories
		while (c.getNext() != null) {
			FortifyAssert.True(c.getLocalName().equals("GroupingSection"));
			Integer count = Integer.parseInt(c.getAttrValue("count"));

			SMInputCursor grp_c = c.childElementCursor();
			FortifyAssert.True(grp_c.advance().getLocalName().equals("groupTitle"));

			iss_counts.put(grp_c.getElemStringValue(), count);
		}

		return new FortifyIssueSummary(sum_title, sum_desc, sum_txt, iss_title, iss_counts);
	}
}
