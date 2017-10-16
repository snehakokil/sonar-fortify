package com.vanguard.sonarqube.fortify.model.xml;

/**
 * This enumerated type contains the main elements of a Fortify report that has been converted to an XML document
 * with the Vanguard report template. These are only the top-level children of the XML document, and each of the
 * enumerated elements contains a boolean 'is_subsection' which denotes whether or not the element contains any
 * children elements other than text.
 * 
 * @author Brian Cefali
 */
public enum FortifyReportElement {
	TEMPLATE_NAME("TemplateName", false),
	TEMPLATE_PATH("TemplatePath", false),
	LOGO_PATH("LogoPath", false),
	FOOTNOTE("Footnote", false),
	USER_NAME("UserName", false),
	REPORT_OVERVIEW("Report Overview", true),
	ISSUE_SUMMARY("Issue Summary", true),
	RESULTS_OUTLINE("Results Outline", true);
	
	private String content;
	private boolean is_subsection;

	private FortifyReportElement(String content, boolean is_subsection) {
		this.content = content;
		this.is_subsection = is_subsection;
	}
	
	public boolean isSubsection() {
		return this.is_subsection;
	}
	
	public String getContent() {
		return this.content;
	}
	
	@Override
	public String toString() {
		return this.getContent();
	}
}
