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
package de.tudarmstadt.ukp.csniper.webapp.statistics.page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import de.tudarmstadt.ukp.csniper.webapp.DefaultValues;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.EvaluationRepository;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationItem;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.Query;
import de.tudarmstadt.ukp.csniper.webapp.search.CorpusService;
import de.tudarmstadt.ukp.csniper.webapp.search.PreparedQuery;
import de.tudarmstadt.ukp.csniper.webapp.search.SearchEngine;
import de.tudarmstadt.ukp.csniper.webapp.search.cqp.CqpEngine;
import de.tudarmstadt.ukp.csniper.webapp.search.cqp.CqpQuery;
import de.tudarmstadt.ukp.csniper.webapp.search.tgrep.TgrepEngine;
import de.tudarmstadt.ukp.csniper.webapp.statistics.SortableAggregatedEvaluationResultDataProvider;
import de.tudarmstadt.ukp.csniper.webapp.support.wicket.ExtendedIndicatingAjaxButton;
import de.tudarmstadt.ukp.csniper.webapp.support.wicket.ThresholdLink;

/**
 * Statistics Page
 */
public class StatisticsPage2
	extends StatisticsPageBase
{
	private static final long serialVersionUID = 1L;

	@SpringBean(name = "evaluationRepository")
	private EvaluationRepository repository;

	@SpringBean(name = "corpusService")
	private CorpusService corpusService;

	private SortableAggregatedEvaluationResultDataProvider dataProvider;

	private StatsTable statsTable;

	private SettingsForm settings;

	public StatisticsPage2()
	{
		super();

		settings = new SettingsForm("settingsForm");
		add(settings);

		statsTable = new StatsTable("statsTable");
		statsTable.setOutputMarkupId(true);
		add(statsTable);
	}

	private class StatsTable
		extends Panel
	{
		private static final long serialVersionUID = 1L;

		ListView<QueryStatistics> list;

		public StatsTable(String aId)
		{
			super(aId);

			list = new ListView<QueryStatistics>("list", new ArrayList<QueryStatistics>())
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(ListItem<QueryStatistics> item)
				{
					QueryStatistics e = item.getModelObject();
					item.add(new Label("type", e.type));
					item.add(new Label("collectionId", e.collectionId));
					item.add(new Label("query", e.query));
					item.add(new Label("userCount", String.valueOf(e.itemStats.users)));
					item.add(new Label("itemCount", String.valueOf(e.itemStats.items)));
					item.add(new Label("itemComplete", String.format("%.1f%%",
							((double) e.itemStats.getComplete() / e.itemStats.items) * 100.0)));
					item.add(new Label("fleissKappa", String
							.format("%.2f", e.itemStats.fleissKappa)));
					item.add(new Label("correctCount", String.valueOf(e.itemStats.correctVotes)));
					item.add(new Label("wrongCount", String.valueOf(e.itemStats.wrongVotes)));
					item.add(new Label("TP", String.valueOf(e.itemStats.truePositives)));
					item.add(new Label("FP", String.valueOf(e.itemStats.falsePositive)));
					item.add(new Label("UNK", String.valueOf(e.itemStats.unknown)));
					item.add(new Label("precision", String.format("%.4f",
							e.itemStats.getPrecision(true))));
					item.add(new Label("precision2", String.format("%.4f",
							e.itemStats.getPrecision(false))));
				}
			};
			add(list);
		}
	}

	private class SettingsForm
		extends Form
	{
		private static final long serialVersionUID = 1L;

		private double userThreshold = DefaultValues.DEFAULT_USER_THRESHOLD;
		private double confidenceThreshold = DefaultValues.DEFAULT_CONFIDENCE_THRESHOLD;
		private Set<String> users = new HashSet<String>();
		private List<Query> queries;

		public SettingsForm(String aId)
		{
			super(aId);
			ListMultipleChoice<String> userSelect = new ListMultipleChoice<String>("userSelect",
					new PropertyModel<Set<String>>(this, "users"), repository.listUsers());
			userSelect.setRequired(true);
			add(userSelect);

			ListMultipleChoice<Query> querySelect = new ListMultipleChoice<Query>("querySelect",
					new PropertyModel<List<Query>>(this, "queries"), repository.listUniqueQueries());
			querySelect.setChoiceRenderer(new ChoiceRenderer<Query>() {
				private static final long serialVersionUID = 1L;

				@Override
				public Object getDisplayValue(Query aObject)
				{
					if (aObject.getType() == null) {
						return aObject.getQuery();
					}
					else {
						return String.format("%s - %s", aObject.getType(), aObject.getQuery());
					}
				}
			});
			querySelect.setRequired(true);
			add(querySelect);

			add(new NumberTextField<Double>("userThreshold", new PropertyModel<Double>(this,
					"userThreshold")).setMinimum(0.0).setMaximum(1.0));
			add(new NumberTextField<Double>("confidenceThreshold", new PropertyModel<Double>(this,
					"confidenceThreshold")).setMinimum(0.0).setMaximum(1.0));
			add(new ThresholdLink("thresholdHelp"));
			add(new ExtendedIndicatingAjaxButton("thresholdButton", new Model<String>("Apply"),
					new Model<String>("Calculating..."))
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					statsTable.list.setModelObject(calculate());
					aTarget.add(getFeedbackPanel(), statsTable);
				}

				@Override
				public void onError(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					super.onError(aTarget, aForm);
					// Make sure the feedback messages are rendered
					aTarget.add(getFeedbackPanel());
				}
			});
		}

		private List<QueryStatistics> calculate()
		{
			List<QueryStatistics> someList = new ArrayList<QueryStatistics>();
			for (Query q : queries) {
				List<EvaluationItem> items = new ArrayList<EvaluationItem>();
				for (SearchEngine engine : corpusService.listEngines(q.getCollectionId())) {
					if (engine.getName().equals(q.getEngine())) {
						PreparedQuery query = engine.createQuery(q.getType(), q.getCollectionId(), q.getQuery());
						query.setMaxResults(Integer.MAX_VALUE);
						try {
							items = query.execute();
						}
						finally {
							IOUtils.closeQuietly(query);
						}
						break;
					}
				}
				items = repository.writeEvaluationItems(items, false);
				dataProvider = new SortableAggregatedEvaluationResultDataProvider(
						repository.listAggregatedResults(items, users, userThreshold,
								confidenceThreshold), users);

				QueryStatistics qstats = new QueryStatistics();
				qstats.type = q.getType();
				qstats.collectionId = q.getCollectionId();
				qstats.query = q.getQuery();
				qstats.itemStats = dataProvider.getItemStatistics();

				someList.add(qstats);
			}
			return someList;
		}
	}

	private static class QueryStatistics
		implements Serializable
	{
		private static final long serialVersionUID = 1L;

		public String type;
		public String collectionId;
		public String query;
		public ItemStatistics itemStats;
	}

	public static class ItemStatistics
		implements Serializable
	{
		private static final long serialVersionUID = 1L;

		public int users;
		public int items;
		public int correctVotes;
		public int wrongVotes;
		public int truePositives;
		public int falsePositive;
		public int unknown;
		public double fleissKappa;

		public double getPrecision(boolean aUnknownAsFalsePositives)
		{
			if (aUnknownAsFalsePositives) {
				return (double) truePositives / (falsePositive + truePositives + unknown);
			}
			else {
				return (double) truePositives / (truePositives + falsePositive);
			}
		}

		public int getComplete()
		{
			return truePositives + falsePositive;
		}

		@Override
		public String toString()
		{
			return "ItemStatistics [users=" + users + ", items=" + items + ", correctVotes="
					+ correctVotes + ", wrongVotes=" + wrongVotes + ", truePositives="
					+ truePositives + ", falsePositive=" + falsePositive + ", unknown=" + unknown
					+ ", fleissKappa=" + fleissKappa + "]";
		}
	}
}
