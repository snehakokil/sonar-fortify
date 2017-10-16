package com.vanguard.sonarqube.fortify.model.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMEvent;
import org.codehaus.staxmate.in.SMHierarchicCursor;

public class FortifyXMLReport {
	private Map<String, Object> report_data;

	private FortifyXMLReport(Map<String, Object> report_data) {
		this.report_data = report_data;
	}

	public FortifyIssueSummary getIssueSummary() {
		return this.report_data.containsKey(FortifyReportElement.ISSUE_SUMMARY.getContent()) ? (FortifyIssueSummary) this.report_data.get(FortifyReportElement.ISSUE_SUMMARY.getContent()) : null;
	}
	
	public FortifyReportOverview getReportOverview() {
		return this.report_data.containsKey(FortifyReportElement.REPORT_OVERVIEW.getContent()) ? (FortifyReportOverview) this.report_data.get(FortifyReportElement.REPORT_OVERVIEW.getContent()) : null;
	}
	
	public FortifyResultsOutline getResultsOutline() {
		return this.report_data.containsKey(FortifyReportElement.RESULTS_OUTLINE.getContent()) ? (FortifyResultsOutline) this.report_data.get(FortifyReportElement.RESULTS_OUTLINE.getContent()) : null;
	}
	
	public String getReportAttr(String attr) {
		return this.report_data.containsKey(attr) ? (String) this.report_data.get(attr) : null;
	}

	public static FortifyXMLReport parseReport(final File f, final String[] reportWhitelist, final FortifySeverityMapping severityMapping) throws XMLStreamException, FortifyXMLParseException, IOException {
		FortifyXMLReport rep = null;
		FileInputStream in = new FileInputStream(f);
		rep = parseReportFromStream(in, reportWhitelist, severityMapping);
		in.close();
		return rep;
	}

	public static FortifyXMLReport parseReport(final URL u, final String[] reportWhitelist, final FortifySeverityMapping severityMapping) throws IOException, XMLStreamException, FortifyXMLParseException {
		FortifyXMLReport rep = null;
		URLConnection conn = u.openConnection();
		conn.connect();
		InputStream in = conn.getInputStream();
		rep = parseReportFromStream(in, reportWhitelist, severityMapping);
		in.close();
		return rep;
	}
//FortifySeverityMapping severityMapping = FortifySeverityMapping.makeSeverityMapper(settings);
	private static FortifyXMLReport parseReportFromStream(final InputStream stream, final String[] whitelistArr, final FortifySeverityMapping severityMapping)
			throws XMLStreamException, FortifyXMLParseException {
		SMInputFactory fact = SMInputFactory.getGlobalSMInputFactory();
		SMHierarchicCursor root_cursor = fact.rootElementCursor(stream);

		SMEvent evt = root_cursor.getNext();

		FortifyAssert.True(evt == SMEvent.START_ELEMENT);
		FortifyAssert.True(root_cursor.getLocalName().equals("ReportDefinition"));

		SMHierarchicCursor cursor = (SMHierarchicCursor) root_cursor.childElementCursor();
		Map<String, Object> map = new HashMap<String, Object>();

		/*
		 * To parse the XML report, we look at the children of ReportDefinition. The top level nodes with no children themselves
		 * (the non-ReportSection nodes) are saved directly, whereas the ReportSections are parsed individually. The data retained
		 * is determined by the settings configuration.
		 */
		
		List<String> reportWhitelist = Arrays.asList(whitelistArr);
		while ((evt = cursor.getNext()) != null) {
			String cur_tag = cursor.getLocalName();
			if (cur_tag.equals("ReportSection")) {
				handleReportSection(cursor, map, reportWhitelist, severityMapping);
			} else if (reportWhitelist.contains(cur_tag)) {
				map.put(cur_tag, cursor.getElemStringValue());
			}
		}

		return new FortifyXMLReport(map);
	}

	/**
	 * Given a report subsection (any of the top-level report elements with non-text children), first check if the subsection is
	 * whitelisted (via the {@code List<String> reportWhitelist} parameter), and then to process and store the subsection if
	 * desired.
	 * 
	 * @param cursor Cursor of the subsection's parent, probably from {@code parseReportFromStream}, that must NOT have been advanced
	 * 			past the subsection, or the {@code cursor.childElementCursor()} call will fail.
	 * @param map The map of report elements to their values (of mixed type) which will serve as the container for all XML data after processing. Do
	 * 			not access values from this map directly, but rather rely on the getter methods that {@code FortifyXMLReport} exports.
	 * @param reportWhitelist A list of XML tags to process and save to the {@code FortifyXMLReport} object. The strings in this list must match the
	 * 			exact case and spelling of the tags in the XML. See {@link FortifyReportElement} for examples.
	 * @param settings The settings object
	 * @throws XMLStreamException
	 * @throws FortifyXMLParseException
	 */
	private static void handleReportSection(SMHierarchicCursor cursor, Map<String, Object> map, final List<String> reportWhitelist, final FortifySeverityMapping severityMapping)
			throws XMLStreamException, FortifyXMLParseException {
		SMHierarchicCursor rep_sec_cur = (SMHierarchicCursor) cursor.childElementCursor();
		SMEvent evt = rep_sec_cur.getNext();
		String title = null;

		/* As it stands, all report sections start with a title, then have two
		 * subsections below the title. We'll grab the title first, and check if
		 * its in the whitelist before proceeding to process subsequent data.
		 */
		FortifyAssert.True(evt == SMEvent.START_ELEMENT);

		String tag = rep_sec_cur.getLocalName();
		if (tag.equals("Title")) {
			title = rep_sec_cur.getElemStringValue();
			if (!reportWhitelist.contains(title))
				return;
		} else {
			FortifyAssert.True(false);
		}

		if (title.equals(FortifyReportElement.REPORT_OVERVIEW.getContent())) {
			map.put(title, FortifyReportOverview.parseReportOverview(rep_sec_cur));
		} else if (title.equals(FortifyReportElement.ISSUE_SUMMARY.getContent())) {
			map.put(title, FortifyIssueSummary.parseIssueSummary(rep_sec_cur));;
		} else if (title.equals(FortifyReportElement.RESULTS_OUTLINE.getContent())) {
			map.put(title, FortifyResultsOutline.parseResultsOutline(rep_sec_cur, severityMapping));;
		} else {
			FortifyAssert.True(false);
		}
	}
}
