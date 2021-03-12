package org.sdrc.datum19.usermgmt;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Data;

/**
 * @author subham
 *
 */
@Entity
@Data
public class LoginAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String username;

	@ManyToOne
	@JoinColumn(name = "acc_id_fk")
	private Account account;

	private Date loggedInDate;

	private Date logoutDate;

	private boolean active;

	private String userAgent;

	private String actualUserAgent;

	private String ipAddress;

}