package org.sdrc.datum19.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class AreaModel implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private Integer areaId;

	private String areaName;

	private int parentAreaId;

	private String areaLevel;
	
	private int areaLevelId;

	private boolean isLive;

	private String areaCode;

	private String shortName;

}
