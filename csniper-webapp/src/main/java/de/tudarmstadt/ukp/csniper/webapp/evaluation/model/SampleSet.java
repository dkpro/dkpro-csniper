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
import java.util.Calendar;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames={"name"})})
public class SampleSet
	implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String collectionId;

	@Column(nullable = false)
	private String type;

	private String userId;

	private String comment;

	@ManyToMany
	@JoinTable(name = "set_item",
			joinColumns = { @JoinColumn(name = "samplesetId", referencedColumnName = "id") },
			inverseJoinColumns = { @JoinColumn(name = "evaluationitemId", referencedColumnName = "id") })
	private List<EvaluationItem> items;

	@Column(nullable = false)
	private Calendar lastUpdate;

	public SampleSet()
	{
	}

	public SampleSet(String aName, String aCollectionId, String aType, String aUserId)
	{
		name = aName;
		collectionId = aCollectionId;
		type = aType;
		userId = aUserId;
	}

	public void addItems(List<EvaluationItem> aItems)
	{
		if (items == null) {
			items = aItems;
		}
		else {
			// remove duplicates, then add the rest
			aItems.removeAll(items);
			items.addAll(aItems);
		}
	}

	public long getId()
	{
		return id;
	}

	public void setId(long aId)
	{
		id = aId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String aName)
	{
		name = aName;
	}

	public String getCollectionId()
	{
		return collectionId;
	}

	public void setCollectionId(String aCollectionId)
	{
		collectionId = aCollectionId;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String aType)
	{
		type = aType;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String aUserId)
	{
		userId = aUserId;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String aComment)
	{
		comment = aComment;
	}

	public List<EvaluationItem> getItems()
	{
		return items;
	}

	public void setItems(List<EvaluationItem> aItems)
	{
		items = aItems;
	}

	@PrePersist
	public void updateTimestamp()
	{
		lastUpdate = Calendar.getInstance();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((collectionId == null) ? 0 : collectionId.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
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
		SampleSet other = (SampleSet) obj;
		if (collectionId == null) {
			if (other.collectionId != null)
				return false;
		}
		else if (!collectionId.equals(other.collectionId))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		}
		else if (!type.equals(other.type))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		}
		else if (!userId.equals(other.userId))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "SampleSet [id=" + id + ", name=" + name + ", collectionId=" + collectionId
				+ ", type=" + type + ", userId=" + userId + ", comment=" + comment
				+ ", lastUpdate=" + lastUpdate + "]";
	}
}
