package com.vanguard.sonarqube.fortify.model.xml;

import javax.xml.stream.XMLStreamException;

import org.codehaus.staxmate.in.SMInputCursor;

public class FortifyTag {
	private final String name, value;
	
	public FortifyTag(String tag_name, String tag_value) {
		this.name = tag_name;
		this.value = tag_value;
	}

	public final String getName() {
		return this.name;
	}
	
	public final String getValue() {
		return this.value;
	}
	
	public static final FortifyTag parseTag(SMInputCursor cursor) throws FortifyXMLParseException, XMLStreamException {
		FortifyAssert.True(cursor.advance().getLocalName().equals("Name"));
		String tag_name = cursor.getElemStringValue();
		
		FortifyAssert.True(cursor.advance().getLocalName().equals("Value"));
		String tag_value = cursor.getElemStringValue();
		
		return new FortifyTag(tag_name, tag_value);
	}
}
