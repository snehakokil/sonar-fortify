package com.vanguard.sonarqube.fortify.model.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.codehaus.staxmate.in.SMHierarchicCursor;

public class FortifyIssue {
	private final String ruleID, iid, category, folder, kingdom, issue_abstract, priority;
	private final String file_name, snippet, severity;
	private String file_path;
	private final Integer line_start;
	private final List<FortifyTag> tags;
	private final List<FortifyComment> comments;
	
	private FortifyIssue(String ruleID, String iid, String category, String folder, String kingdom, String issue_abstract, String priority,
			String file_name, String file_path, Integer line_start, String snippet, String severity, List<FortifyTag> tags, List<FortifyComment> comments) {
		this.ruleID = ruleID;
		this.iid = iid;
		this.category = category;
		this.folder = folder;
		this.kingdom = kingdom;
		this.issue_abstract = issue_abstract;
		this.priority = priority;
		this.file_name = file_name;
		this.file_path = file_path;
		this.line_start = line_start;
		this.snippet = snippet;
		this.severity = severity;
		this.tags = tags;
		this.comments = comments;
	}
	
	@Override
	public String toString() {
		return String.format("[Fortify Issue |category=%s|ruleID=%s]", this.getCategory(), this.getRuleID());
	}
	
	public String getSeverity() {
		return this.severity;
	}
	
	public String getRuleID() {
		return this.ruleID;
	}
	
	public String getIid() {
		return this.iid;
	}
	
	public String getCategory() {
		return this.category;
	}
	
	public String getFolder() {
		return this.folder;
	}
	
	public String getKingdom() {
		return this.kingdom;
	}
	
	public String getAbstract() {
		return this.issue_abstract;
	}
	
	public String getPriority() {
		return this.priority;
	}
	
	public List<FortifyTag> getTags() {
		return Collections.unmodifiableList(this.tags);
	}
	
	public List<FortifyComment> getComments() {
		return Collections.unmodifiableList(this.comments);
	}
	
	public String getFileName() {
		return this.file_name;
	}
	
	public String getFilePath() {
		return this.file_path;
	}
	
	public FortifyIssue setFilePath(String new_path) {
		this.file_path = new_path;
		return this;
	}
	
	public Integer getLineStart() {
		return this.line_start;
	}
	
	public String getSnippet() {
		return this.snippet;
	}
	
	public static FortifyIssue parseIssue(final SMHierarchicCursor cursor, final FortifySeverityMapping severityMap) throws XMLStreamException, FortifyXMLParseException {
		String ruleID, iid;
		ruleID = cursor.getAttrValue("ruleID");
		iid = cursor.getAttrValue("iid");
		FortifyAssert.True(ruleID != null && iid != null);
		
		SMHierarchicCursor iss_cursor = (SMHierarchicCursor) cursor.childElementCursor();
		String category, folder, kingdom, iss_abstract, priority;
		
		FortifyAssert.True(iss_cursor.advance().getLocalName().equals("Category"));
		category = iss_cursor.getElemStringValue();
		
		FortifyAssert.True(iss_cursor.advance().getLocalName().equals("Folder"));
		folder = iss_cursor.getElemStringValue();
		
		FortifyAssert.True(iss_cursor.advance().getLocalName().equals("Kingdom"));
		kingdom = iss_cursor.getElemStringValue();
		
		FortifyAssert.True(iss_cursor.advance().getLocalName().equals("Abstract"));
		iss_abstract = iss_cursor.getElemStringValue();
		
		FortifyAssert.True(iss_cursor.advance().getLocalName().equals("Friority"));
		priority = iss_cursor.getElemStringValue();
		
		List<FortifyTag> tags = new ArrayList<FortifyTag>();
		List<FortifyComment> comments = new ArrayList<FortifyComment>();

		while (iss_cursor.getNext() != null) {
			if (iss_cursor.getLocalName().equals("Tag")) {
				tags.add(FortifyTag.parseTag(iss_cursor.childElementCursor()));
			} else if (iss_cursor.getLocalName().equals("Comment")) {
				comments.add(FortifyComment.parseComment(iss_cursor.childElementCursor()));
			} else
				break;
		}
		// There is not necessarily a tag
//		if (iss_cursor.advance().getLocalName().equals("Tag")) {
//			SMHierarchicCursor tag_cur = (SMHierarchicCursor) iss_cursor.childElementCursor();
//			
//			FortifyAssert.True(tag_cur.advance().getLocalName().equals("Name"));
//			tag_name = tag_cur.getElemStringValue();
//			
//			FortifyAssert.True(tag_cur.advance().getLocalName().equals("Value"));
//			tag_value = tag_cur.getElemStringValue();
//			
//			FortifyAssert.True(iss_cursor.advance().getLocalName().equals("Primary"));
//		} else {
//			FortifyAssert.True(iss_cursor.getLocalName().equals("Primary"));
//		}
		
		String file_name, file_path, snippet;
		Integer line_start;
		SMHierarchicCursor primary_cur = (SMHierarchicCursor) iss_cursor.childElementCursor();
		
		FortifyAssert.True(primary_cur.advance().getLocalName().equals("FileName"));
		file_name = primary_cur.getElemStringValue();
		
		FortifyAssert.True(primary_cur.advance().getLocalName().equals("FilePath"));
		file_path = primary_cur.getElemStringValue();
		
		FortifyAssert.True(primary_cur.advance().getLocalName().equals("LineStart"));
		line_start = primary_cur.getElemIntValue();
		
		// Snippet is not always present
		if (primary_cur.getNext() != null)
			snippet = primary_cur.getElemStringValue();
		else
			snippet = null;
		//FortifyAssert.True(primary_cur.advance().getLocalName().equals("Snippet"));
		
		
		// FortifyAssert.True(iss_cursor.advance().getLocalName().equals("Source"));
		// Duplicate of Primary, skipping
		
		return new FortifyIssue(ruleID, iid, category, folder, kingdom, iss_abstract,
				priority, file_name, file_path, line_start, snippet,
				severityMap.getSeverity(category, folder), tags, comments);
	}
}
