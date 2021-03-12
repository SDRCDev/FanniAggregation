package org.sdrc.datum19.model;

public enum Language {
	EN_UK("en_uk"), TAMIL("tamil"), HINDI("hi"),ORIYA("or"),EN_US("en_us"),SPANISH("es");

	private String code;

	Language(String code) {
		this.code = code;
	}

	public String getCode() {
		return this.code;
	}
}
