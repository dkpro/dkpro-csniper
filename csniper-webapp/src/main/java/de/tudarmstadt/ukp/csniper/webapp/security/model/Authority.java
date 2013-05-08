/*******************************************************************************
 * Copyright 2013
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.csniper.webapp.security.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;

/**
 * Authority (role) assigned to a user. Compatible with the default settings of {@link JdbcDaoImpl}.
 * 
 * @author Richard Eckart de Castilho
 * 
 * @see <a
 *      href="http://static.springsource.org/spring-security/site/docs/3.0.x/reference/appendix-schema.html">Spring
 *      standard schema</a>.
 */
@Entity
@Table(name = "authorities", uniqueConstraints = { @UniqueConstraint(columnNames = { "authority",
		"username" }) })
public class Authority
	implements Serializable
{
	private static final long serialVersionUID = -1490540239189868920L;

	@Id
	@GeneratedValue
	private long id;

	@ManyToOne
	@JoinColumn(name = "username")
	private User user;

	@Column(nullable = false, length = 150)
	private String authority;

	public long getId()
	{
		return id;
	}

	public void setId(long aId)
	{
		id = aId;
	}

	public User getUser()
	{
		return user;
	}

	public void setUser(User aUser)
	{
		user = aUser;
	}

	public String getAuthority()
	{
		return authority;
	}

	public void setAuthority(String aAuthority)
	{
		authority = aAuthority;
	}
}
