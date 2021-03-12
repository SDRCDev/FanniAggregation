package org.sdrc.datum19.usermgmt;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Entity
@Data
public class Designation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8750658507782677711L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String name;

	private String code;

	@JsonIgnore
	@OneToMany(mappedBy = "designation", fetch = FetchType.LAZY)
	List<DesignationAuthorityMapping> designationAuthorityMapping;

	@JsonIgnore
	@OneToMany(mappedBy = "designation", fetch = FetchType.LAZY)
	List<AccountDesignationMapping> accountDesignationMappings;

	public Designation() {
		super();
	}

	public Designation(Integer id) {
		super();
		this.id = id;
	}
}