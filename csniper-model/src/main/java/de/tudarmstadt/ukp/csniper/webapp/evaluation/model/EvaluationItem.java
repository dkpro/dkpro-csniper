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
import javax.persistence.Transient;

@Entity
public class EvaluationItem
	implements Serializable
{
	private static final long serialVersionUID = 5353447230664231950L;

	public static final int MAX_COLUMN_LENGTH = 2048;

	@Id
	@GeneratedValue
	private long id;

	@Column(nullable = false)
	private String collectionId;

	@Column(nullable = false)
	private String documentId;

	@Column(nullable = false)
	private String type;

	@Column(nullable = false)
	private long beginOffset;

	@Column(nullable = false)
	private long endOffset;

	@Column(nullable = false, length = MAX_COLUMN_LENGTH)
	private String coveredText;

	/**
	 * The difference between these three offsets is as follows:<br>
	 * <b>itemTextMatch*</b> refer to this item's coveredText<br>
	 * <b>originalTextMatch*</b> refer to the match as it is found in the original document<br>
	 * <b>tokenMatch*</b> refer to the indices of the tokens which start and end this match, where a
	 * token with index 0 specifies the token in the original text which starts at beginOffset
	 */
	@Transient
	private int itemTextMatchBegin = -1;
	@Transient
	private int itemTextMatchEnd = -1;
	@Transient
	private int originalTextMatchBegin = -1;
	@Transient
	private int originalTextMatchEnd = -1;
	@Transient
	private int tokenMatchBegin = -1;
	@Transient
	private int tokenMatchEnd = -1;

	public EvaluationItem()
	{
		// For JPA
	}

	/**
	 * THIS CONSTRUCTOR IS MEANT ONLY FOR USE IN NATIVE QUERIES.
	 */
	public EvaluationItem(long aId, String aCollectionId, String aDocumentId,
			String aType, long aBegin, long aEnd, String aCoveredText)
	{
		this(aCollectionId, aDocumentId, aType, aBegin, aEnd, aCoveredText);
		id = aId;
	}

	public EvaluationItem(String aCollectionId, String aDocumentId,
			String aType, long aBegin, long aEnd, String aCoveredText)
	{
		collectionId = aCollectionId;
		documentId = aDocumentId;
		beginOffset = aBegin;
		endOffset = aEnd;
		// use setters for truncating in case strings are too long for column
		setCoveredText(aCoveredText);
		type = aType;
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

	/**
	 * Get the start offset of the item in the original document text.
	 * 
	 * @return the start offset.
	 */
	public long getBeginOffset()
	{
		return beginOffset;
	}

	public void setBeginOffset(long aBeginOffset)
	{
		beginOffset = aBeginOffset;
	}

	/**
	 * Get the end offset of the item in the original document text. This is the offset of the first
	 * character after the covered text.
	 * 
	 * @return the end offset.
	 */
	public long getEndOffset()
	{
		return endOffset;
	}

	public void setEndOffset(long aEndOffset)
	{
		endOffset = aEndOffset;
	}

	public int getItemTextMatchBegin()
	{
		return itemTextMatchBegin;
	}

	public int getItemTextMatchEnd()
	{
		return itemTextMatchEnd;
	}

	public int getOriginalTextMatchBegin()
	{
		return originalTextMatchBegin;
	}

	public int getOriginalTextMatchEnd()
	{
		return originalTextMatchEnd;
	}

	public int getTokenMatchBegin()
	{
		return tokenMatchBegin;
	}

	public int getTokenMatchEnd()
	{
		return tokenMatchEnd;
	}

	public void setMatchOnItemText(int aItemTextMatchBegin, int aItemTextMatchEnd)
	{
		itemTextMatchBegin = Math.min(aItemTextMatchBegin, aItemTextMatchEnd);
		itemTextMatchEnd = Math.max(aItemTextMatchBegin, aItemTextMatchEnd);
	}

	public void setMatchOnOriginalText(int aOriginalTextMatchBegin, int aOriginalTextMatchEnd)
	{
		originalTextMatchBegin = aOriginalTextMatchBegin;
		originalTextMatchEnd = aOriginalTextMatchEnd;
	}

	public void setMatchOnOriginalTextViaTokenIndicesAndLookGoodWhileDoingSo(int aTokenMatchBegin,
			int aTokenMatchEnd)
	{
		tokenMatchBegin = aTokenMatchBegin;
		tokenMatchEnd = aTokenMatchEnd;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String aType)
	{
		type = aType;
	}

	public String getCoveredText()
	{
		return coveredText;
	}

	public void setCoveredText(String aText)
	{
		if (aText.length() >= MAX_COLUMN_LENGTH) {
			throw new IllegalArgumentException("The match plus context must be smaller than ["
					+ MAX_COLUMN_LENGTH + "] but is [" + aText.length() + "]: [" + aText + "]");
		}
		coveredText = aText;
	}

	public String getLeftContext()
	{
		if (isItemMatchSet()) {
			return coveredText.substring(0, itemTextMatchBegin);
		}
		else {
			return "";
		}
	}

	public String getRightContext()
	{
		if (isItemMatchSet()) {
			if (itemTextMatchEnd == coveredText.length()) {
				// in this case, the coveredText stretches completely to the end of the sentence,
				// hence the right context is empty; not checking this leads to a
				// StringIndexOutOfBounds exception because of the (textMatchEnd + 1) below
				return "";
			}
			else {
				return coveredText.substring(itemTextMatchEnd + 1, coveredText.length());
			}
		}
		else {
			return "";
		}
	}

	public String getMatch()
	{
		if (isItemMatchSet()) {
			return coveredText.substring(itemTextMatchBegin, itemTextMatchEnd);
		}
		else {
			return coveredText;
		}
	}

	public boolean isItemMatchSet()
	{
		return (itemTextMatchBegin >= 0) && (itemTextMatchEnd >= 0);
	}

	public boolean isOriginalMatchSet()
	{
		return (originalTextMatchBegin >= 0) && (originalTextMatchEnd >= 0);
	}

	public boolean isTokenMatchSet()
	{
		return (tokenMatchBegin >= 0) && (tokenMatchEnd >= 0);
	}

	public void copyTransientData(EvaluationItem aOtherItem)
	{
		setMatchOnItemText(aOtherItem.itemTextMatchBegin, aOtherItem.itemTextMatchEnd);
		setMatchOnOriginalText(aOtherItem.originalTextMatchBegin, aOtherItem.originalTextMatchEnd);
		setMatchOnOriginalTextViaTokenIndicesAndLookGoodWhileDoingSo(aOtherItem.tokenMatchBegin,
				aOtherItem.tokenMatchEnd);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (beginOffset ^ (beginOffset >>> 32));
		result = prime * result + ((collectionId == null) ? 0 : collectionId.hashCode());
		result = prime * result + ((coveredText == null) ? 0 : coveredText.hashCode());
		result = prime * result + ((documentId == null) ? 0 : documentId.hashCode());
		result = prime * result + (int) (endOffset ^ (endOffset >>> 32));
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		EvaluationItem other = (EvaluationItem) obj;
		if (beginOffset != other.beginOffset)
			return false;
		if (collectionId == null) {
			if (other.collectionId != null)
				return false;
		}
		else if (!collectionId.equals(other.collectionId))
			return false;
		if (coveredText == null) {
			if (other.coveredText != null)
				return false;
		}
		else if (!coveredText.equals(other.coveredText))
			return false;
		if (documentId == null) {
			if (other.documentId != null)
				return false;
		}
		else if (!documentId.equals(other.documentId))
			return false;
		if (endOffset != other.endOffset)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		}
		else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("EvaluationItem [id=");
		builder.append(id);
		builder.append(", collectionId=");
		builder.append(collectionId);
		builder.append(", documentId=");
		builder.append(documentId);
		builder.append(", type=");
		builder.append(type);
		builder.append(", getLeftContext()=");
		builder.append(getLeftContext());
		builder.append(", getMatch()=");
		builder.append(getMatch());
		builder.append(", getRightContext()=");
		builder.append(getRightContext());
		builder.append(", getBeginOffset()=");
		builder.append(getBeginOffset());
		builder.append(", getEndOffset()=");
		builder.append(getEndOffset());
		builder.append("]");
		return builder.toString();
	}
}
