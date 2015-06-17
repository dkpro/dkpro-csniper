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

import static org.apache.commons.lang.StringUtils.substring;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "userId", "item_id" }) })
public class EvaluationResult
	implements Serializable
{
	private static final long serialVersionUID = 513051512608379704L;
	private static final int MAX_COLUMN_LENGTH = 511;

	@Id
	@GeneratedValue
	private long id;

	@ManyToOne(optional = false)
	private EvaluationItem item;

	@Column(nullable = false)
	private String userId;

	@Column(nullable = false)
	private Calendar lastUpdate;

	@Column(length = MAX_COLUMN_LENGTH)
	private String comment;

	@Column(nullable = false, length = MAX_COLUMN_LENGTH)
	private String result;

	@ElementCollection(fetch = FetchType.EAGER)
	private Map<AdditionalColumn, String> additionalColumns = new HashMap<AdditionalColumn, String>();

    @Transient
    private double score;

	public EvaluationResult()
	{
		// For JPA
	}

	public EvaluationResult(EvaluationItem aItem, String aUserId, String aResult)
	{
		super();
		item = aItem;
		userId = aUserId;
		result = aResult;
	}

	/**
	 * Helper method for PropertyUtilsBean - for sorting by additional columns.
	 * 
	 * @return Map[additionalColumnId,columnValue]
	 */
	public Map<String, String> getAdditionalColumnValue()
	{
		Map<String, String> sortMap = new HashMap<String, String>();
		for (Entry<AdditionalColumn, String> e : additionalColumns.entrySet()) {
			sortMap.put(Long.toString(e.getKey().getId()), e.getValue());
		}
		return sortMap;
	}

	public long getId()
	{
		return id;
	}

	public EvaluationItem getItem()
	{
		return item;
	}

	public void setItem(EvaluationItem aItem)
	{
		item = aItem;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String aUserId)
	{
		userId = aUserId;
	}

	public Calendar getLastUpdate()
	{
		return lastUpdate;
	}

	public void setLastUpdate(Calendar aLastUpdate)
	{
		lastUpdate = aLastUpdate;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String aComment)
	{
		comment = substring(aComment, 0, MAX_COLUMN_LENGTH);
	}

	public String getResult()
	{
		return result;
	}

	public void setResult(String aResult)
	{
		result = substring(aResult, 0, MAX_COLUMN_LENGTH);
	}

	public Map<AdditionalColumn, String> getAdditionalColumns()
	{
		return additionalColumns;
	}

	public void setAdditionalColumns(Map<AdditionalColumn, String> aAdditionalColumns)
	{
		additionalColumns = aAdditionalColumns;
	}

	public double getScore()
    {
        return score;
    }

    public void setScore(double aScore)
    {
        score = aScore;
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
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((item == null) ? 0 : item.hashCode());
		result = prime * result + ((lastUpdate == null) ? 0 : lastUpdate.hashCode());
		result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		EvaluationResult other = (EvaluationResult) obj;
		if (comment == null) {
			if (other.comment != null) {
				return false;
			}
		}
		else if (!comment.equals(other.comment)) {
			return false;
		}
		if (item == null) {
			if (other.item != null) {
				return false;
			}
		}
		else if (!item.equals(other.item)) {
			return false;
		}
		if (result == null) {
			if (other.result != null) {
				return false;
			}
		}
		else if (!result.equals(other.result)) {
			return false;
		}
		if (userId == null) {
			if (other.userId != null) {
				return false;
			}
		}
		else if (!userId.equals(other.userId)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "EvaluationResult [id=" + id + ", item=" + item + ", userId=" + userId
				+ ", lastUpdate=" + lastUpdate + ", comment=" + comment + ", result=" + result
				+ ", additionalColumns=" + additionalColumns + "]";
	}
}
