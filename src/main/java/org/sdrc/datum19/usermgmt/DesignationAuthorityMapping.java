package org.sdrc.datum19.usermgmt;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Data;

@Entity
@Data
public class DesignationAuthorityMapping implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2445955375570545368L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@ManyToOne
	@JoinColumn(name = "designation_id_fk")
	private Designation designation;

	@ManyToOne
	@JoinColumn(name = "authority_id_fk")
	private Authority authority;

	public DesignationAuthorityMapping() {
		super();
	}

	public DesignationAuthorityMapping(int id) {
		this.id = id;
	}
}