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
package de.tudarmstadt.ukp.csniper.webapp.evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;

import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationItem;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationResult;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.Mark;

/**
 * Original code from org.apache.wicket.examples.repeater.SortableContactDataProvider, modified and
 * extended.
 * 
 * @author Erik-Lân Do Dinh
 * 
 */
public class SortableEvaluationResultDataProvider
	extends SortableDataProvider<EvaluationResult>
{
	public enum ResultFilter
	{
		ALL("all"), 
		TODO("to do"),
		ANNOTATED("annotated"),
		TO_CHECK("check");


		private String label;

		private ResultFilter(String aLabel)
		{
			label = aLabel;
		}

		public String getLabel()
		{
			return label;
		}
	}
	
	private static final long serialVersionUID = 4133689174658469377L;

	private List<EvaluationResult> results;
	private List<EvaluationResult> limitedResults;
	
	private ResultFilter filter;
	private boolean filterChanged;
	
//	private String lastSortProperty;
//	private boolean lastSortOrder;
	private boolean resort;

	public SortableEvaluationResultDataProvider()
	{
		// set default sort
		setSort("id", SortOrder.ASCENDING);
		setResults(new ArrayList<EvaluationResult>());
	}
	
	public SortableEvaluationResultDataProvider(List<EvaluationResult> aResults)
	{
		this();
		setResults(aResults);
	}

	/**
	 * @see org.apache.wicket.markup.repeater.data.IDataProvider#iterator(int, int)
	 */
	@Override
	public Iterator<EvaluationResult> iterator(int aFirst, int aCount)
	{
		// Apply paging
		updateView();
		return limitedResults.subList(aFirst, Math.min(aFirst + aCount, results.size())).iterator();
	}

	/**
	 * @see org.apache.wicket.markup.repeater.data.IDataProvider#size()
	 */
	@Override
	public int size()
	{
		updateView();
		return limitedResults.size();
	}
	
	private void updateView()
	{
		final SortParam sp = getSort();

		if (resort || filterChanged) {
			// Apply filter
			if (getFilter() != ResultFilter.ALL) {
				limitedResults = new ArrayList<EvaluationResult>();
				for (EvaluationResult e : results) {
					Mark emark = Mark.fromString(e.getResult());
					switch (getFilter()) {
					case ANNOTATED:
						if (emark == Mark.CORRECT || emark == Mark.WRONG) {
							limitedResults.add(e);
						}
						break;
					case TO_CHECK:
						if (emark == Mark.CHECK) {
							limitedResults.add(e);
						}
						break;
					case TODO:
						if (StringUtils.isBlank(e.getResult()) || emark == Mark.PRED_CORRECT || emark == Mark.PRED_WRONG) {
							limitedResults.add(e);
						}
						break;
					default:
						throw new IllegalArgumentException("Unknown filter setting");
					}
				}
			}
			else {
				limitedResults = results;
			}
			
			// Apply sorting
			Collections.sort(limitedResults, new Comparator<Object>()
			{
				@SuppressWarnings("rawtypes")
				@Override
				public int compare(Object aO1, Object aO2)
				{
					try {
						Comparable v1 = (Comparable) PropertyUtils.getNestedProperty(aO1, sp.getProperty());
						Comparable v2 = (Comparable) PropertyUtils.getNestedProperty(aO2, sp.getProperty());

						if (v1 == null) {
							if (v2 == null) {
								return 0;
							}
							return sp.isAscending() ? -1 : 1;
						}
						if (v2 == null) {
							return sp.isAscending() ? 1 : -1;
						}
						return sp.isAscending() ? v1.compareTo(v2) : v2.compareTo(v1);
					}
					catch (Exception e) {
						throw new IllegalStateException(e);
					}
				}
			});
			
//			lastSortProperty = sp.getProperty();
//			lastSortOrder = sp.isAscending();
			resort = false;
			filterChanged = false;
		}
	}

	/**
	 * @see org.apache.wicket.markup.repeater.data.IDataProvider#model(java.lang.Object)
	 */
	@Override
	public IModel<EvaluationResult> model(EvaluationResult aObject)
	{
		return new DetachableEvaluationResultModel(aObject);
	}

	public List<EvaluationResult> getResults()
	{
		return results;
	}

	public List<EvaluationItem> getItems()
	{
		Set<EvaluationItem> items = new HashSet<EvaluationItem>();
		for (EvaluationResult result : results) {
			items.add(result.getItem());
		}
		return new ArrayList<EvaluationItem>(items);
	}

	public void setResults(List<EvaluationResult> aResults)
	{
		results = aResults;
		// Reset the remembered sort properties so that the limitedResults get updated with the
		// new results in the next rendering iteration
//		lastSortProperty = null;
//		lastSortOrder = false;
		resort = true;
		filterChanged = true;
	}

	public ResultFilter getFilter()
	{
		return filter;
	}

	public void setFilter(ResultFilter aFilter)
	{
		filter = aFilter;
		filterChanged = true;
	}

    @Override
    public void setSort(String aProperty, SortOrder aOrder)
    {
        super.setSort(aProperty, aOrder);
        resort = true;
    }

    @Override
    public void setSort(SortParam aParam)
    {
        super.setSort(aParam);
        resort = true;
    }
}
