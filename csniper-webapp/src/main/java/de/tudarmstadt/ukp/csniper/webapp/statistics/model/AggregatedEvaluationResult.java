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
package de.tudarmstadt.ukp.csniper.webapp.statistics.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationItem;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.Mark;
import de.tudarmstadt.ukp.csniper.webapp.statistics.SortableAggregatedEvaluationResultDataProvider.ResultFilter;

public class AggregatedEvaluationResult
	implements Serializable
{
	private static final long serialVersionUID = -1138969955428071980L;

	private EvaluationItem item;
	private int correct;
	private int wrong;
	private int incomplete;
	private ResultFilter classification;
	private double confidence;
	private double userRatio;
	private SortedMap<String, Mark> userVoteMap;

	public AggregatedEvaluationResult(Object[] aColumns, Collection<String> aUsers)
	{
		this((EvaluationItem) aColumns[0], ((Number) aColumns[1]).intValue(),
				((Number) aColumns[2]).intValue(), ((Number) aColumns[3]).intValue(),
				(String) aColumns[4], ((Number) aColumns[5]).doubleValue(), ((Number) aColumns[6])
						.doubleValue(), toUserVoteMap(aColumns[7], aUsers));
	}

	private static SortedMap<String, Mark> toUserVoteMap(Object aObject, Collection<String> aUsers)
	{
		SortedMap<String, Mark> map = new TreeMap<String, Mark>();
		// default to NA for all users for which this has been requested, regardless if they are in
		// the db or not; this is needed for getOrderedMarks()
		for (String user : aUsers) {
			map.put(user, Mark.NA);
		}
		String[] userVotes = aObject.toString().split(",");
		for (int i = 0; i < userVotes.length; i++) {
			String[] userAndVote = userVotes[i].split("#");
			String user = userAndVote[0];
			String vote = userAndVote.length == 2 ? userAndVote[1] : "";
			map.put(user, Mark.fromString(vote));
		}
		return map;
	}

	public AggregatedEvaluationResult(EvaluationItem aItem, int aCorrect, int aWrong,
			int aIncomplete, String aClassification, double aConfidence, double aUserRatio,
			SortedMap<String, Mark> aUserVoteMap)
	{
		item = aItem;
		correct = aCorrect;
		wrong = aWrong;
		incomplete = aIncomplete;
		classification = ResultFilter.valueOf(aClassification.toUpperCase());
		confidence = aConfidence;
		userRatio = aUserRatio;
		userVoteMap = aUserVoteMap;
	}

	public EvaluationItem getItem()
	{
		return item;
	}

	public int getCorrect()
	{
		return correct;
	}

	public int getWrong()
	{
		return wrong;
	}

	public int getIncomplete()
	{
		return incomplete;
	}

	/**
	 * Get the users of this {@link AggregatedEvaluationResult}.
	 * 
	 * @param aOnlyVoting if true, only return users who voted correct or wrong for this item
	 * @return involved users
	 */
	public Set<String> getUsers(boolean aOnlyVoting)
	{
		if (aOnlyVoting) {
			Set<String> users = new HashSet<String>();
			for (Entry<String, Mark> entry : userVoteMap.entrySet()) {
				String user = entry.getKey();
				Mark vote = entry.getValue();
				if (vote == Mark.CORRECT || vote == Mark.WRONG) {
					users.add(user);
				}
			}
			return users;
		}
		else {
			return userVoteMap.keySet();
		}
	}

	public Mark[] getOrderedMarks()
	{
		return userVoteMap.values().toArray(new Mark[0]);
	}

	public ResultFilter getClassification()
	{
		return classification;
	}

	public double getConfidence()
	{
		return confidence;
	}

	public double getUserRatio()
	{
		return userRatio;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classification == null) ? 0 : classification.hashCode());
		long temp;
		temp = Double.doubleToLongBits(confidence);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (correct ^ (correct >>> 32));
		result = prime * result + (int) (incomplete ^ (incomplete >>> 32));
		result = prime * result + ((item == null) ? 0 : item.hashCode());
		temp = Double.doubleToLongBits(userRatio);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (wrong ^ (wrong >>> 32));
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
		AggregatedEvaluationResult other = (AggregatedEvaluationResult) obj;
		if (classification != other.classification)
			return false;
		if (Double.doubleToLongBits(confidence) != Double.doubleToLongBits(other.confidence))
			return false;
		if (correct != other.correct)
			return false;
		if (incomplete != other.incomplete)
			return false;
		if (item == null) {
			if (other.item != null)
				return false;
		}
		else if (!item.equals(other.item))
			return false;
		if (Double.doubleToLongBits(userRatio) != Double.doubleToLongBits(other.userRatio))
			return false;
		if (wrong != other.wrong)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "AggregatedEvaluationResult [item=" + item + ", correct=" + correct + ", wrong="
				+ wrong + ", incomplete=" + incomplete + ", classification=" + classification
				+ ", confidence=" + confidence + ", userRatio=" + userRatio + "]";
	}

    public SortedMap<String, Mark> getUserVoteMap()
    {
        return userVoteMap;
    }
}
