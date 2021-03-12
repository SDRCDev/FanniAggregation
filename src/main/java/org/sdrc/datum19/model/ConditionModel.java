package org.sdrc.datum19.model;

import lombok.Data;

@Data
public class ConditionModel {
	private Integer question;
	private String operator;
	private Double value;
}
