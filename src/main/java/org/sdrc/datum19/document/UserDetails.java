package org.sdrc.datum19.document;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Subham Ashish(subham@sdrc.co.in)
 */

@Data
@NoArgsConstructor
public class UserDetails implements Serializable {

	private static final long serialVersionUID = 325706468522771026L;

	private Long mobileNumber;

	private String fullName;

	private List<String> awcs;

	private List<String> vhdnImmunizationPoints;

	private List<String> subCenters;

	private List<String> phcs;

	private List<String> chcs;

	private List<String> sdhs;
	
	private List<String> ifaSupplyPoints;
	
	private Boolean isIFAuser=false;
}
