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
package de.tudarmstadt.ukp.csniper.webapp.project.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames={"name"})})
public class Project implements Serializable
{
	private static final long serialVersionUID = -5426914078691460011L;
	
	@Id
	@GeneratedValue
	private long id;

	private String name;
	
	@ElementCollection
	private List<String> users = new ArrayList<String>();
	
	@ManyToMany
	private List<AnnotationType> types = new ArrayList<AnnotationType>();

	public Project()
	{
		// Nothing to do
	}
	
	public long getId()
	{
		return id;
	}
	
	public String getName()
	{
		return name;
	}

	public void setName(String aName)
	{
		name = aName;
	}

	public List<String> getUsers()
	{
		return users;
	}

	public void setUsers(List<String> aUsers)
	{
		users = aUsers;
	}

	public List<AnnotationType> getTypes()
	{
		return types;
	}

	public void setTypes(List<AnnotationType> aTypes)
	{
		types = aTypes;
	}
}
