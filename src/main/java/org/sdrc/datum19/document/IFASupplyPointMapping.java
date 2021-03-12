package org.sdrc.datum19.document;

import org.springframework.data.annotation.Id;

import lombok.Data;

/**
 * @author subham
 *
 */
@Data
public class IFASupplyPointMapping {

	@Id
	private String id;

	private String name;

	private String type;
	
	private String desgId;
	
	private Integer slugId;
	
	private Integer typeId;

}
