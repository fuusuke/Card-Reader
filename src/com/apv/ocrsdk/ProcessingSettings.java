package com.apv.ocrsdk;

public class ProcessingSettings {

	public String asUrlParams() {
		return ("language=" + language + "&exportFormat=" + outputFormat);
	}

	private String language = "English";
	private String outputFormat = "xml";
}
