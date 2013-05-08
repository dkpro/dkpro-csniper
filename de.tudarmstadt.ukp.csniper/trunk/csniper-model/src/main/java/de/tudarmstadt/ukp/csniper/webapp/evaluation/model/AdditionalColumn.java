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
package de.tudarmstadt.ukp.csniper.webapp.evaluation.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
//@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }) })
public class AdditionalColumn
	implements Serializable
{
	private static final long serialVersionUID = 3295241348714279584L;

	@Id
	@GeneratedValue
	private long id;

	@Column
	private String name;

	@Column
	private boolean showColumn;
	
//	@Lob
//	private String description;

	public AdditionalColumn()
	{
		// Nothing to do
	}

	public AdditionalColumn(String aName, boolean aShowColumn)
	{
		name = aName;
		showColumn = aShowColumn;
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

	public boolean getShowColumn()
	{
		return showColumn;
	}

	public void setShowColumn(boolean aShowColumn)
	{
		showColumn = aShowColumn;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AdditionalColumn other = (AdditionalColumn) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "AdditionalColumn [id=" + id + ", name=" + name + ", showColumn=" + showColumn + "]";
	}
}
