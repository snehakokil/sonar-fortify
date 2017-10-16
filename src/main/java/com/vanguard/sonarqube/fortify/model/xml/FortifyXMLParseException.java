package com.vanguard.sonarqube.fortify.model.xml;

public class FortifyXMLParseException extends Exception {
	private static final long serialVersionUID = 5203343544631965369L;

	public FortifyXMLParseException() {
	}

	public FortifyXMLParseException(String message) {
		super(message);
	}

	public FortifyXMLParseException(Throwable cause) {
		super(cause);
	}

	public FortifyXMLParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public FortifyXMLParseException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
