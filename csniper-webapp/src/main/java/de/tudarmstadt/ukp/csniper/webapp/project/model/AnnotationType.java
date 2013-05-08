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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.AdditionalColumn;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }) })
public class AnnotationType
	implements Serializable
{
	private static final long serialVersionUID = 3295241348714279584L;

	@Id
	@GeneratedValue
	private long id;

	private String name;

	private int goal;

	private int goalWrong;

	@Lob
	private String description;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<AdditionalColumn> additionalColumns = new ArrayList<AdditionalColumn>();

	public AnnotationType()
	{
		// Nothing to do
	}

	public AnnotationType(String aName)
	{
		name = aName;
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

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String aDescription)
	{
		description = aDescription;
	}

	public void setGoal(int aGoal)
	{
		goal = aGoal;
	}

	public int getGoal()
	{
		return goal;
	}

	public int getGoalWrong()
	{
		return goalWrong;
	}

	public void setGoalWrong(int aGoalWrong)
	{
		goalWrong = aGoalWrong;
	}

	public List<AdditionalColumn> getAdditionalColumns()
	{
		return additionalColumns;
	}

	public void setAdditionalColumns(List<AdditionalColumn> aAdditionalColumns)
	{
		additionalColumns.clear();
		additionalColumns.addAll(aAdditionalColumns);
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
		AnnotationType other = (AnnotationType) obj;
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
		return "AnnotationType [id=" + id + ", name=" + name + ", goal=" + goal + ", goalWrong="
				+ goalWrong + ", description=" + description + ", additionalColumns="
				+ additionalColumns + "]";
	}
}
