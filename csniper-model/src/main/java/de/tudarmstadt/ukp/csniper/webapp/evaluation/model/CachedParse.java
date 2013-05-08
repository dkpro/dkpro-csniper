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
public class CachedParse
	implements Serializable
{
	private static final long serialVersionUID = -9083195318024780648L;

	public static final int MAX_COLUMN_LENGTH = 4096;

	@Id
	@GeneratedValue
	private long id;

	@Column(nullable = false)
	private String collectionId;

	@Column(nullable = false)
	private String documentId;

	@Column(nullable = false)
	private long beginOffset;

	@Column(nullable = false)
	private long endOffset;

	@Column(nullable = false, length = MAX_COLUMN_LENGTH)
	private String pennTree;

	public CachedParse()
	{
		// For JPA
	}

	public CachedParse(String aCollectionId, String aDocumentId, long aBeginOffset,
			long aEndOffset, String aPennTree)
	{
		collectionId = aCollectionId;
		documentId = aDocumentId;
		beginOffset = aBeginOffset;
		endOffset = aEndOffset;
		setPennTree(aPennTree);
	}

	public CachedParse(EvaluationItem aEvaluationItem, String aPennTree)
	{
		this(aEvaluationItem.getCollectionId(), aEvaluationItem.getDocumentId(), aEvaluationItem
				.getBeginOffset(), aEvaluationItem.getEndOffset(), aPennTree);
	}

	public long getId()
	{
		return id;
	}

	public String getCollectionId()
	{
		return collectionId;
	}

	public void setCollectionId(String aCollectionId)
	{
		collectionId = aCollectionId;
	}

	public String getDocumentId()
	{
		return documentId;
	}

	public void setDocumentId(String aDocumentId)
	{
		documentId = aDocumentId;
	}

	public long getBeginOffset()
	{
		return beginOffset;
	}

	public void setBeginOffset(long aBeginOffset)
	{
		beginOffset = aBeginOffset;
	}

	public long getEndOffset()
	{
		return endOffset;
	}

	public void setEndOffset(long aEndOffset)
	{
		endOffset = aEndOffset;
	}

	public String getPennTree()
	{
		return pennTree;
	}

	public void setPennTree(String aPennTree)
	{
		if (aPennTree.length() >= MAX_COLUMN_LENGTH) {
			throw new IllegalArgumentException("The PennTree length must be smaller than ["
					+ MAX_COLUMN_LENGTH + "] but is [" + aPennTree.length() + "]: [" + aPennTree
					+ "]");
		}
		pennTree = aPennTree;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (beginOffset ^ (beginOffset >>> 32));
		result = prime * result + ((collectionId == null) ? 0 : collectionId.hashCode());
		result = prime * result + ((documentId == null) ? 0 : documentId.hashCode());
		result = prime * result + (int) (endOffset ^ (endOffset >>> 32));
		result = prime * result + ((pennTree == null) ? 0 : pennTree.hashCode());
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
		CachedParse other = (CachedParse) obj;
		if (beginOffset != other.beginOffset)
			return false;
		if (collectionId == null) {
			if (other.collectionId != null)
				return false;
		}
		else if (!collectionId.equals(other.collectionId))
			return false;
		if (documentId == null) {
			if (other.documentId != null)
				return false;
		}
		else if (!documentId.equals(other.documentId))
			return false;
		if (endOffset != other.endOffset)
			return false;
		if (pennTree == null) {
			if (other.pennTree != null)
				return false;
		}
		else if (!pennTree.equals(other.pennTree))
			return false;
		return true;
	}
}
