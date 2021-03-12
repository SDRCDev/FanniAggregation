package org.sdrc.datum19.document;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Subham Ashish(subham@sdrc.co.in) Created Date:26-Jun-2018 2:22:10 PM
 */
@Data
@NoArgsConstructor
@ToString
@Document
public class Type implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	private String typeName;

	private String description;

	private Integer slugId;

	private Integer formId;

	

}
