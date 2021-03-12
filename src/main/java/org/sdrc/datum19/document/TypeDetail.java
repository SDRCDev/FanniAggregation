package org.sdrc.datum19.document;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Subham Ashish(subham@sdrc.co.in) Created Date:26-Jun-2018 2:22:59 PM
 */
@Data
@AllArgsConstructor
@ToString
@Document
@NoArgsConstructor
public class TypeDetail implements Serializable {

	private static final long serialVersionUID = 7158994858633568625L;

	@Id
	private String id;

	private Integer slugId;

	private String name;

	private Type type;

	private Integer orderLevel;
	
	private Integer formId;
	
//	private String score;

//	@Override
//	public int compareTo(TypeDetail o) {
//		// TODO Auto-generated method stub
//		return this.slugId.compareTo(o.getSlugId());
//	}
	
	
	
}