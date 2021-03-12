package org.sdrc.datum19.document;

import java.io.Serializable;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * This Entity class will keep all the area details
 * 
 * @since version 1.0.0.0
 *
 */
@Document
@Data
public class Area implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1558538811474305739L;

	@Id
	private String id;

	private Integer areaId;

	private String areaName;

	private String areaCode;

	private Integer parentAreaId;

	private Boolean live;

	private AreaLevel areaLevel;

	private Integer facilitytId;
	
	private Integer blockId;

	private Integer districtId;

	private Integer stateId;

	private String createdBy;
	
	private TypeDetail facilityType;
	
	private TypeDetail facilityLevel;
	
	private Map<String, Object> phcChcType;
	
	private Boolean hwc;
	
	private Map<String, Object> formIdTypeDetails;
	
	public Area() {
		super();
	}

	public Area(int areaId) {
		this.areaId = areaId;
	}

	
	
}

