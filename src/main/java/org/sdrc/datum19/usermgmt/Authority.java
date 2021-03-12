package org.sdrc.datum19.usermgmt;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import lombok.Data;

@Entity
@Data
public class Authority implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7383544774278489679L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String authority;

	private String description;

	@OneToMany(mappedBy = "authority", fetch = FetchType.LAZY)
	List<DesignationAuthorityMapping> designationAuthorityMappings;

	public Authority() {
		super();
	}

	public Authority(Integer id) {
		super();
		this.id = id;
	}
}