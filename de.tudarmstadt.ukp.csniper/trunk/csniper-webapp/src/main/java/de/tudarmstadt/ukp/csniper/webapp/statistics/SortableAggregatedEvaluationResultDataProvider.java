/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.csniper.webapp.statistics;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import de.tudarmstadt.ukp.csniper.webapp.statistics.model.AggregatedEvaluationResult;
import de.tudarmstadt.ukp.csniper.webapp.statistics.page.StatisticsPage.TypeStatistics;
import de.tudarmstadt.ukp.csniper.webapp.statistics.page.StatisticsPage2.ItemStatistics;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.AnnotationStudy;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.IAnnotationStudy;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.MultiRaterPiAgreement;

/**
 * Original code from org.apache.wicket.examples.repeater.SortableContactDataProvider, modified and
 * extended.
 * 
 * @author Erik-Lân Do Dinh
 * 
 */
public class SortableAggregatedEvaluationResultDataProvider
	extends SortableDataProvider<AggregatedEvaluationResult, String>
{
	public enum ResultFilter
	{
		CORRECT("Correct", Color.GREEN),
		WRONG("Wrong", Color.RED),
		DISPUTED("Disputed", Color.ORANGE),
		INCOMPLETE("Incomplete", Color.LIGHT_GRAY),
		UNKNOWN("Unknown", Color.DARK_GRAY);

		private String label;
		private Color color;

		private ResultFilter(String aLabel, Color aColor)
		{
			label = aLabel;
			color = aColor;
		}

		public String getLabel()
		{
			return label;
		}

		public Color getColor()
		{
			return color;
		}
	}

	private static final long serialVersionUID = -4680400414358831292L;

	private List<AggregatedEvaluationResult> aerList;
	private List<AggregatedEvaluationResult> limitedResults;
	private Set<String> users;

	private List<ResultFilter> filters;
	private boolean filterChanged;

	private String lastSortProperty;
	private boolean lastSortOrder;

	private SortableAggregatedEvaluationResultDataProvider()
	{
		// set default sort
		setSort("id", SortOrder.ASCENDING);
	}

	public SortableAggregatedEvaluationResultDataProvider(
			List<AggregatedEvaluationResult> aAerList, Collection<String> aUsers)
	{
		this();
		aerList = aAerList;
		users = new HashSet<String>(aUsers);
		filters = new ArrayList<ResultFilter>();
	}

	/**
	 * @see org.apache.wicket.markup.repeater.data.IDataProvider#iterator(int, int)
	 */
	@Override
	public Iterator<AggregatedEvaluationResult> iterator(long aFirst, long aCount)
	{
		// Apply paging
		updateView();
		return limitedResults.subList((int) aFirst, (int) Math.min(aFirst + aCount, limitedResults.size()))
		        .iterator();
	}

	/**
	 * @see org.apache.wicket.markup.repeater.data.IDataProvider#size()
	 */
	@Override
	public long size()
	{
		updateView();
		return limitedResults.size();
	}

	private void updateView()
	{
		final SortParam<String> sp = getSort();

		// filter
		if (!sp.getProperty().equals(lastSortProperty) || (lastSortOrder != sp.isAscending())
				|| filterChanged) {
			limitedResults = new ArrayList<AggregatedEvaluationResult>();
			for (AggregatedEvaluationResult aer : aerList) {
				if (getFilters().contains(aer.getClassification())) {
					limitedResults.add(aer);
				}
			}
		}

		// sort
		Collections.sort(limitedResults, new Comparator<AggregatedEvaluationResult>()
		{
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public int compare(AggregatedEvaluationResult aO1, AggregatedEvaluationResult aO2)
			{
				try {
					Comparable v1 = (Comparable) PropertyUtils.getNestedProperty(aO1,
							sp.getProperty());
					Comparable v2 = (Comparable) PropertyUtils.getNestedProperty(aO2,
							sp.getProperty());

					return sp.isAscending() ? v1.compareTo(v2) : v2.compareTo(v1);
				}
				catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}
		});

		lastSortProperty = sp.getProperty();
		lastSortOrder = sp.isAscending();
		filterChanged = false;
	}

	/**
	 * @see org.apache.wicket.markup.repeater.data.IDataProvider#model(java.lang.Object)
	 */
	@Override
	public IModel<AggregatedEvaluationResult> model(AggregatedEvaluationResult aObject)
	{
		return new Model<AggregatedEvaluationResult>(aObject);
	}

	/**
	 * Counts how many results there are in each ResultFilter category.
	 * 
	 * @return map
	 */
	@Deprecated
	public Map<ResultFilter, Integer> getClassifications()
	{
		Map<ResultFilter, Integer> classifications = new HashMap<ResultFilter, Integer>();
		for (ResultFilter filter : ResultFilter.values()) {
			classifications.put(filter, 0);
		}
		for (AggregatedEvaluationResult aer : aerList) {
			ResultFilter current = aer.getClassification();
			classifications.put(current, classifications.get(current) + 1);
		}
		return classifications;
	}

	public double computeInterAnnotatorAgreement()
	{
		IAnnotationStudy study = new AnnotationStudy(users.size());
		for (AggregatedEvaluationResult aer : aerList) {
			// only use items which are annotated by all users
			if (aer.getUserRatio() == 1) {
				study.addItemAsArray(aer.getOrderedMarks());
			}
		}
		MultiRaterPiAgreement m = new MultiRaterPiAgreement(study);
		return m.calculateAgreement();
	}

	public ItemStatistics getItemStatistics()
	{
		ItemStatistics istats = new ItemStatistics();
		istats.users = users.size();
		istats.items = aerList.size();

		for (AggregatedEvaluationResult aer : aerList) {
			istats.correctVotes += aer.getCorrect();
			istats.wrongVotes += aer.getWrong();

			ResultFilter classification = aer.getClassification();
			if (classification == ResultFilter.CORRECT) {
				istats.truePositives++;
			}
			else if (classification == ResultFilter.WRONG) {
				istats.falsePositive++;
			}
		}
		istats.unknown = istats.items - istats.truePositives - istats.falsePositive;
		istats.fleissKappa = computeInterAnnotatorAgreement();
		return istats;
	}

	public Map<String, TypeStatistics> getTypeStatistics(Map<String, TypeStatistics> aStatsMap,
			Map<String, MutableInt> aUserStats)
	{
		for (AggregatedEvaluationResult aer : aerList) {
			String type = aer.getItem().getType();
			TypeStatistics tstats = aStatsMap.get(type);
			if (tstats == null) {
				tstats = new TypeStatistics();
				aStatsMap.put(type, tstats);
			}

			boolean countForUser = true;
			switch (aer.getClassification()) {
			case CORRECT:
				tstats.correct++;
				countForUser = true;
				break;
			case WRONG:
				tstats.wrong++;
				countForUser = true;
				break;
			case DISPUTED:
				tstats.disputed++;
				countForUser = true;
				break;
			case INCOMPLETE:
				tstats.incomplete++;
				break;
			default:
				// do nothing
			}

			if (countForUser) {
				Set<String> users = aer.getUsers(true);
				for (String user : users) {
					MutableInt count = aUserStats.get(user);
					if (count == null) {
						count = new MutableInt();
						aUserStats.put(user, count);
					}
					count.increment();
				}
			}
		}
		return aStatsMap;
	}

	public List<ResultFilter> getFilters()
	{
		return filters;
	}

	public void setFilters(List<ResultFilter> aFilters)
	{
		if (!(aFilters.containsAll(filters) && filters.containsAll(aFilters))) {
			filters = aFilters;
			filterChanged = true;
		}
	}
}
