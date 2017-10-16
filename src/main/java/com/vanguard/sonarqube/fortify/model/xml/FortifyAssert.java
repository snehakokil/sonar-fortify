package com.vanguard.sonarqube.fortify.model.xml;

public class FortifyAssert {
	public static void True(boolean b) throws FortifyXMLParseException {
		if (!b) throw new FortifyXMLParseException();
	}
}
