package org.sdrc.datum19.document;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
public class AreaLevel implements Serializable {

	private static final long serialVersionUID = 1519381375815795764L;
	
	@Id
	private String id;

	private Integer areaLevelId;

	private String areaLevelName;

	public AreaLevel() {
		super();
		// TODO Auto-generated constructor stub
	}

	public AreaLevel(Integer areaLevelId) {
		super();
		this.areaLevelId = areaLevelId;
	}

}
