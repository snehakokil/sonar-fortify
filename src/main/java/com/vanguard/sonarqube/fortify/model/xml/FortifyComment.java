package com.vanguard.sonarqube.fortify.model.xml;

import javax.xml.stream.XMLStreamException;

import org.codehaus.staxmate.in.SMInputCursor;

public class FortifyComment {
	private final String user_info, comment;
	
	public FortifyComment(String user_info, String comment) {
		this.user_info = user_info;
		this.comment = comment;
	}
	
	public final String getUserInfo() {
		return this.user_info;
	}

	public final String getComment() {
		return this.comment;
	}
	
	public static final FortifyComment parseComment(SMInputCursor cursor) throws XMLStreamException, FortifyXMLParseException {
		FortifyAssert.True(cursor.advance().getLocalName().equals("UserInfo"));
		String user_info = cursor.getElemStringValue();
		
		FortifyAssert.True(cursor.advance().getLocalName().equals("Comment"));
		String comment_msg = cursor.getElemStringValue();
		
		return new FortifyComment(user_info, comment_msg);
	}
}
