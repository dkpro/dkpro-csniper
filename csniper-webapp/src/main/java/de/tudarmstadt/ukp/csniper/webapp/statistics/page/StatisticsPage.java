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

import static java.util.Arrays.asList;
import static org.apache.commons.lang.WordUtils.capitalizeFully;

import java.awt.Color;
import java.awt.GradientPaint;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.mutable.MutableInt;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.CloseButtonCallback;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.ContextRelativeResource;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.jfree.util.Rotation;
import org.jfree.util.UnitType;
import org.wicketstuff.progressbar.ProgressBar;

import de.tudarmstadt.ukp.csniper.webapp.DefaultValues;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.EvaluationRepository;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.AdditionalColumn;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.page.ContextView;
import de.tudarmstadt.ukp.csniper.webapp.project.ProjectRepository;
import de.tudarmstadt.ukp.csniper.webapp.project.model.AnnotationType;
import de.tudarmstadt.ukp.csniper.webapp.search.ContextProvider;
import de.tudarmstadt.ukp.csniper.webapp.search.CorpusService;
import de.tudarmstadt.ukp.csniper.webapp.statistics.SortableAggregatedEvaluationResultDataProvider;
import de.tudarmstadt.ukp.csniper.webapp.statistics.SortableAggregatedEvaluationResultDataProvider.ResultFilter;
import de.tudarmstadt.ukp.csniper.webapp.statistics.model.AggregatedEvaluationResult;
import de.tudarmstadt.ukp.csniper.webapp.statistics.page.export.ExportCsvTask;
import de.tudarmstadt.ukp.csniper.webapp.statistics.page.export.ExportExcelTask;
import de.tudarmstadt.ukp.csniper.webapp.statistics.page.export.ExportHtmlTask;
import de.tudarmstadt.ukp.csniper.webapp.statistics.page.export.ExportTask;
import de.tudarmstadt.ukp.csniper.webapp.statistics.page.export.ExportTkSvmTask;
import de.tudarmstadt.ukp.csniper.webapp.support.task.ITaskService;
import de.tudarmstadt.ukp.csniper.webapp.support.task.TaskProgressionModel;
import de.tudarmstadt.ukp.csniper.webapp.support.task.Task.Message;
import de.tudarmstadt.ukp.csniper.webapp.support.wicket.AjaxFileDownload;
import de.tudarmstadt.ukp.csniper.webapp.support.wicket.ChartImageResource;
import de.tudarmstadt.ukp.csniper.webapp.support.wicket.CustomDataTable;
import de.tudarmstadt.ukp.csniper.webapp.support.wicket.EmbeddableImage;
import de.tudarmstadt.ukp.csniper.webapp.support.wicket.ExtendedIndicatingAjaxButton;
import de.tudarmstadt.ukp.csniper.webapp.support.wicket.ThresholdLink;

/**
 * Statistics Page
 */
public class StatisticsPage
	extends StatisticsPageBase
{
	private static final long serialVersionUID = 1L;

	private ModalWindow exportModal;
	private AjaxFileDownload exportDownload;
	private StatisticsForm statisticsForm;
	private WebMarkupContainer displayOptions;
	private WebMarkupContainer contextViewsContainer;
	private ListView<ContextView> contextViews;
	private List<IColumn<AggregatedEvaluationResult>> columns = new ArrayList<IColumn<AggregatedEvaluationResult>>();
	private Component resultTable;
	private SortableAggregatedEvaluationResultDataProvider dataProvider;

	@SpringBean(name = "evaluationRepository")
	private EvaluationRepository repository;

	@SpringBean(name = "projectRepository")
	private ProjectRepository projectRepository;

	@SpringBean(name = "corpusService")
	private CorpusService corpusService;

	@SpringBean(name = "contextProvider")
	private ContextProvider contextProvider;

	private static final int ROWS_PER_PAGE = 10;
	private static final int CHART_WIDTH = 300;

	public static class StatisticsFormModel
		implements Serializable
	{
		private static final long serialVersionUID = 1L;

		// Settings
		protected Set<String> collections = new HashSet<String>();
		protected Set<AnnotationType> types = new HashSet<AnnotationType>();
		protected Set<String> users = new HashSet<String>();
		protected double userThreshold = DefaultValues.DEFAULT_CONFIDENCE_THRESHOLD;
		protected double confidenceThreshold = DefaultValues.DEFAULT_USER_THRESHOLD;
		protected List<ResultFilter> filters = new ArrayList<ResultFilter>();

		// Statistics
		private double fleissKappa = 0;

		private TypeStatistics allTypes = new TypeStatistics();
		private Map<String, TypeStatistics> perTypeStats = new HashMap<String, TypeStatistics>();

		private Map<String, MutableInt> perUserStats = new HashMap<String, MutableInt>();

		public int getComplete()
		{
			int value = 0;
			for (TypeStatistics stats : perTypeStats.values()) {
				value += Math.min(stats.goalCorrect, stats.correct);
				value += Math.min(stats.goalWrong, stats.wrong);
			}
			return value;
		}

		public int getTotal()
		{
			int value = 0;
			for (TypeStatistics stats : perTypeStats.values()) {
				value += stats.correct;
				value += stats.wrong;
				value += stats.disputed;
				value += stats.incomplete;
			}
			return value;
		}

		public int getGoal()
		{
			return allTypes.goalCorrect + allTypes.goalWrong;
		}

		public double getCompleteness()
		{
			return (double) getComplete() / getGoal();
		}

		private TypeStatistics getStatistics(String aType)
		{
			TypeStatistics s = perTypeStats.get(aType);
			if (s == null) {
				s = new TypeStatistics();
				perTypeStats.put(aType, s);
			}
			return s;
		}

		public void reset()
		{
			allTypes = new TypeStatistics();
			perTypeStats = new HashMap<String, TypeStatistics>();
			perUserStats = new HashMap<String, MutableInt>();
		}

		public Set<String> getCollections()
		{
			return collections;
		}

		public Set<AnnotationType> getTypes()
		{
			return types;
		}

		public Set<String> getUsers()
		{
			return users;
		}

		public double getUserThreshold()
		{
			return userThreshold;
		}

		public double getConfidenceThreshold()
		{
			return confidenceThreshold;
		}

		public List<ResultFilter> getFilters()
		{
			return filters;
		}
	}

	public static class TypeStatistics
		implements Serializable
	{
		private static final long serialVersionUID = 1L;

		public int goalCorrect;
		public int goalWrong;
		public int correct;
		public int wrong;
		public int disputed;
		public int incomplete;

		public double getCompleteness()
		{
			return (((double) Math.min(correct, goalCorrect) / goalCorrect) + ((double) Math.min(
					wrong, goalWrong) / goalWrong)) / 2.0;
		}

		@Override
		public String toString()
		{
			return "TypeStatistics [goalCorrect=" + goalCorrect + ", goalWrong=" + goalWrong
					+ ", correct=" + correct + ", wrong=" + wrong + ", disputed=" + disputed
					+ ", incomplete=" + incomplete + "]";
		}
	}

	private class StatisticsForm
		extends Form<StatisticsFormModel>
	{
		private static final long serialVersionUID = 1L;

		private Image pieChart;
		private Image progressChart;
		private Image usersChart;
		private Label fleissKappaLabel;

		public StatisticsForm(String id)
		{
			super(id, new CompoundPropertyModel<StatisticsFormModel>(new StatisticsFormModel()));

			StatisticsFormModel model = getModelObject();

			// CSNIPER-120 - Misleading information about "unknown" instances on evaluation page
			List<ResultFilter> availableFilters = new ArrayList<ResultFilter>(
					asList(ResultFilter.values()));
			availableFilters.remove(ResultFilter.UNKNOWN);
			model.filters = new ArrayList<ResultFilter>(availableFilters);

			add(new Label("goal"));
			add(new Label("complete"));

			// collection select
			add(new ListMultipleChoice<String>("collections", corpusService.listCorpora()));

			// type select
			add(new ListMultipleChoice<AnnotationType>("types",
					projectRepository.listAnnotationTypes(), new ChoiceRenderer<AnnotationType>(
							"name")));

			// user select
			add(new ListMultipleChoice<String>("users", sort(repository.listUsers())));

			// threshold link
			add(new ThresholdLink("thresholdHelp"));

			// vote field
			add(new NumberTextField<Double>("userThreshold").setMinimum(0.0).setMaximum(1.0));

			// confidence field
			add(new NumberTextField<Double>("confidenceThreshold").setMinimum(0.0).setMaximum(1.0));

			// filter
			add(new CheckBoxMultipleChoice<ResultFilter>("filters", availableFilters,
					new ChoiceRenderer<ResultFilter>("label")).setPrefix("<li>").setSuffix(
					"</li>\n"));

			// submit button
			add(new ExtendedIndicatingAjaxButton("statisticsButton", new Model<String>(
					"Show statistics"), new Model<String>("Calculating..."))
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					StatisticsFormModel model = StatisticsForm.this.getModelObject();

					// update resultTable
					dataProvider = new SortableAggregatedEvaluationResultDataProvider(
							repository.listAggregatedResults(model.collections, model.types,
									model.users, model.userThreshold, model.confidenceThreshold),
							model.users);
					dataProvider.setSort("item.documentId", SortOrder.ASCENDING);
					dataProvider.setFilters(model.filters);
					resultTable = resultTable
							.replaceWith(new CustomDataTable<AggregatedEvaluationResult>(
									"resultTable", columns, dataProvider, ROWS_PER_PAGE));

					calculateStatistics();

					// update progress chart
					progressChart.setImageResource(createProgressChart());
					progressChart.setVisible(true);

					// update users chart
					usersChart.setImageResource(createUsersChart());
					usersChart.setVisible(true);

					// update pie chart
					pieChart.setImageResource(new ChartImageResource(createPieChart(), CHART_WIDTH,
							140));
					pieChart.setVisible(true);

					// show displayOptions
					displayOptions.setVisible(true);
					// aTarget.add(pieChart, progressChart, resultTable, displayOptions,
					// fleissKappaLabel);
					aTarget.add(StatisticsForm.this, resultTable, displayOptions);
				}

				@Override
				public void onError(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					super.onError(aTarget, aForm);
					aTarget.add(getFeedbackPanel());
				}
			});

			add(new AjaxSubmitLink("exportButton")
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					aTarget.appendJavaScript("Wicket.Window.unloadConfirmation = false;");
					// exportModal.get("content/settingsForm/progress").setVisible(false);
					exportModal.show(aTarget);
				}

				@Override
				protected void onError(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					aTarget.add(getFeedbackPanel());
				}
			});

			pieChart = new NonCachingImage("pieChart");
			pieChart.setOutputMarkupPlaceholderTag(true).setVisible(false);
			add(pieChart);

			progressChart = new NonCachingImage("progressChart");
			progressChart.setOutputMarkupPlaceholderTag(true).setVisible(false);
			add(progressChart);

			usersChart = new NonCachingImage("usersChart");
			usersChart.setOutputMarkupPlaceholderTag(true).setVisible(false);
			add(usersChart);

			fleissKappaLabel = new Label("fleissKappa")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onComponentTagBody(final MarkupStream markupStream,
						final ComponentTag openTag)
				{
					double kappa = (Double) getDefaultModelObject();
					String label;
					if (Double.isNaN(kappa)) {
						label = "not computable";
					}
					else {
						label = new DecimalFormat("#.##").format(kappa);
					}
					replaceComponentTagBody(markupStream, openTag, label);
				}
			};
			fleissKappaLabel.setOutputMarkupId(true);
			add(fleissKappaLabel);

			setOutputMarkupId(true);
		}

		/**
		 * Creates a pie chart from the classifications obtained from the dataProvider.
		 * 
		 * @return a PieChart to use as a parameter for ChartImageResource
		 */
		private JFreeChart createPieChart()
		{
			StatisticsFormModel model = getModelObject();

			// fill dataset
			DefaultPieDataset dataset = new DefaultPieDataset();
			dataset.setValue("Correct", model.allTypes.correct);
			dataset.setValue("Wrong", model.allTypes.wrong);
			// dataset.setValue("Incomplete", model.allTypes.incomplete);
			dataset.setValue("Disputed", model.allTypes.disputed);

			// create chart
			JFreeChart chart = ChartFactory.createPieChart3D(null, dataset, false, true, false);
			PiePlot3D plot = (PiePlot3D) chart.getPlot();
			plot.setInsets(RectangleInsets.ZERO_INSETS);
			plot.setStartAngle(290);
			plot.setDirection(Rotation.CLOCKWISE);
			plot.setIgnoreZeroValues(true);
			plot.setOutlineVisible(false);
			plot.setBackgroundPaint(null);
			// plot.setInteriorGap(0.05);
			plot.setInteriorGap(0.0);
			plot.setMaximumLabelWidth(0.22);
			plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} {1} ({2})"));
			plot.setDepthFactor(0.25);
			plot.setCircular(true);
			plot.setDarkerSides(true);
			for (ResultFilter filter : ResultFilter.values()) {
				plot.setSectionPaint(capitalizeFully(filter.toString()), filter.getColor());
			}
			return chart;
		}

		private void calculateStatistics()
		{
			StatisticsFormModel model = getModelObject();
			model.reset();

			for (AnnotationType t : model.types) {
				model.allTypes.goalCorrect += t.getGoal();
				model.allTypes.goalWrong += t.getGoalWrong();
				model.getStatistics(t.getName()).goalCorrect += t.getGoal();
				model.getStatistics(t.getName()).goalWrong += t.getGoalWrong();
			}

			// get statistics for each type
			dataProvider.getTypeStatistics(model.perTypeStats, model.perUserStats);
			for (TypeStatistics tstats : model.perTypeStats.values()) {
				model.allTypes.correct += tstats.correct;
				model.allTypes.wrong += tstats.wrong;
				model.allTypes.disputed += tstats.disputed;
				model.allTypes.incomplete += tstats.incomplete;
			}

			// compute Fleiss' Kappa
			model.fleissKappa = dataProvider.computeInterAnnotatorAgreement();
		}

		private ChartImageResource createProgressChart()
		{
			StatisticsFormModel model = getModelObject();

			// fill dataset
			DefaultCategoryDataset dataset = new DefaultCategoryDataset();
			dataset.setValue(model.getCompleteness(), "Completeness", "Overall");

			List<String> types = new ArrayList<String>(model.perTypeStats.keySet());
			Collections.sort(types);
			for (String type : types) {
				TypeStatistics stats = model.getStatistics(type);
				dataset.setValue(stats.getCompleteness(), "Completeness", type);
			}

			// create chart
			JFreeChart chart = ChartFactory.createBarChart(null, null, null, dataset,
					PlotOrientation.HORIZONTAL, false, false, false);

			CategoryPlot plot = chart.getCategoryPlot();
			plot.setInsets(new RectangleInsets(UnitType.ABSOLUTE, 0, 20, 0, 20));
			plot.getRangeAxis().setRange(0.0, 1.0);
			((NumberAxis) plot.getRangeAxis()).setNumberFormatOverride(new DecimalFormat("0%"));
			plot.setOutlineVisible(false);
			plot.setBackgroundPaint(null);

			BarRenderer renderer = new BarRenderer();
			renderer.setBarPainter(new StandardBarPainter());
			renderer.setShadowVisible(false);
			renderer.setGradientPaintTransformer(new StandardGradientPaintTransformer(
					GradientPaintTransformType.HORIZONTAL));
			renderer.setSeriesPaint(0, new GradientPaint(0f, 0f, Color.RED, 0f, 0f, Color.GREEN));
			chart.getCategoryPlot().setRenderer(renderer);

			return new ChartImageResource(chart, CHART_WIDTH, 30 + (types.size() * 18));
		}

		private ChartImageResource createUsersChart()
		{
			StatisticsFormModel model = getModelObject();

			// fill dataset
			DefaultCategoryDataset dataset = new DefaultCategoryDataset();

			List<String> users = new ArrayList<String>(model.perUserStats.keySet());
			Collections.sort(users);
			for (String user : users) {
				dataset.setValue(model.perUserStats.get(user).intValue(), "Votes", user);
			}

			// create chart
			JFreeChart chart = ChartFactory.createBarChart(null, null, null, dataset,
					PlotOrientation.HORIZONTAL, false, false, false);

			CategoryPlot plot = chart.getCategoryPlot();
			plot.setInsets(new RectangleInsets(UnitType.ABSOLUTE, 0, 20, 0, 20));
			plot.getRangeAxis().setRange(0.0, model.getTotal());
			((NumberAxis) plot.getRangeAxis()).setNumberFormatOverride(new DecimalFormat("0"));
			plot.setOutlineVisible(false);
			plot.setBackgroundPaint(null);

			BarRenderer renderer = new BarRenderer();
			renderer.setBarPainter(new StandardBarPainter());
			renderer.setShadowVisible(false);
			// renderer.setGradientPaintTransformer(new StandardGradientPaintTransformer(
			// GradientPaintTransformType.HORIZONTAL));
			renderer.setSeriesPaint(0, Color.BLUE);
			chart.getCategoryPlot().setRenderer(renderer);

			return new ChartImageResource(chart, CHART_WIDTH, 30 + (users.size() * 18));
		}

		private List<String> sort(List<String> aList)
		{
			Collections.sort(aList);
			return aList;
		}
	}

	/**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public StatisticsPage()
	{
		super();
		contextViewsContainer = new WebMarkupContainer("contextViewsContainer")
		{
			{
				contextViews = new ListView<ContextView>("contextViews")
				{
					@Override
					protected void populateItem(ListItem aItem)
					{
						aItem.add((Component) aItem.getModelObject());
					}
				};
				add(contextViews);
			}
		};
		contextViewsContainer.setOutputMarkupId(true);
		add(contextViewsContainer);

		columns.add(new AbstractColumn<AggregatedEvaluationResult>(new Model<String>(""))
		{
			@Override
			public void populateItem(
					final Item<ICellPopulator<AggregatedEvaluationResult>> aCellItem,
					String aComponentId, final IModel<AggregatedEvaluationResult> model)
			{
				EmbeddableImage iconContext = new EmbeddableImage(aComponentId,
						new ContextRelativeResource("images/context.png"));
				iconContext.add(new AjaxEventBehavior("onclick")
				{
					@Override
					protected void onEvent(AjaxRequestTarget aTarget)
					{
						try {
							contextViews.setList(asList(new ContextView(contextProvider, model
									.getObject().getItem())));
							aTarget.add(contextViewsContainer);
						}
						catch (IOException e) {
							error("Unable to load context: " + e.getMessage());
						}
					}
				});
				iconContext.add(new AttributeModifier("class",
						new Model<String>("clickableElement")));
				aCellItem.add(iconContext);
			}
		});
		columns.add(new PropertyColumn<AggregatedEvaluationResult>(new Model<String>("Type"),
				"item.type", "item.type")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getCssClass()
			{
				return super.getCssClass() + " nowrap";
			}
		});
		columns.add(new PropertyColumn<AggregatedEvaluationResult>(new Model<String>("Collection"),
				"item.collectionId", "item.collectionId")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getCssClass()
			{
				return super.getCssClass() + " nowrap";
			}

		});
		columns.add(new PropertyColumn<AggregatedEvaluationResult>(new Model<String>("Document"),
				"item.documentId", "item.documentId")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getCssClass()
			{
				return super.getCssClass() + " nowrap";
			}
		});
		columns.add(new PropertyColumn<AggregatedEvaluationResult>(new Model<String>("Item"),
				"item.coveredText", "item.coveredText"));
		columns.add(new PropertyColumn<AggregatedEvaluationResult>(new Model<String>("#Correct"),
				"correct", "correct"));
		columns.add(new PropertyColumn<AggregatedEvaluationResult>(new Model<String>("#Wrong"),
				"wrong", "wrong"));
		columns.add(new PropertyColumn<AggregatedEvaluationResult>(
				new Model<String>("#Incomplete"), "incomplete", "incomplete"));
		columns.add(new PropertyColumn<AggregatedEvaluationResult>(new Model<String>("Aggregated"),
				"classification", "classification"));
		// {
		// private static final long serialVersionUID = 1L;
		//
		// @Override
		// public void populateItem(Item<ICellPopulator<AggregatedEvaluationResult>> aCellItem,
		// String aComponentId, IModel<AggregatedEvaluationResult> aRowModel)
		// {
		// StatisticsFormModel sModel = statisticsForm.getModelObject();
		// ResultFilter aggregated = aRowModel.getObject().getClassification(sModel.users,
		// sModel.userThreshold, sModel.confidenceThreshold);
		// aCellItem.add(new Label(aComponentId, aggregated.getLabel()));
		// }
		// });
		columns.add(new PropertyColumn<AggregatedEvaluationResult>(new Model<String>("Confidence"),
				"confidence", "confidence"));
		// {
		// private static final long serialVersionUID = 1L;
		//
		// @Override
		// public void populateItem(Item<ICellPopulator<AggregatedEvaluationResult>> aCellItem,
		// String aComponentId, IModel<AggregatedEvaluationResult> aRowModel)
		// {
		// StatisticsFormModel sModel = statisticsForm.getModelObject();
		// double confidence = aRowModel.getObject().getConfidence(sModel.users,
		// sModel.userThreshold);
		// aCellItem.add(new Label(aComponentId, Double.toString(confidence)));
		// }
		// });

		add(exportModal = new ModalWindow("exportModal"));
		final ExportPanel exportPanel = new ExportPanel(exportModal.getContentId());
		exportModal.setContent(exportPanel);
		exportModal.setTitle("Export");
		exportModal.setInitialWidth(550);
		exportModal.setInitialHeight(350);
		exportModal.setCloseButtonCallback(new CloseButtonCallback()
		{
			@Override
			public boolean onCloseButtonClicked(AjaxRequestTarget aTarget)
			{
				exportPanel.cancel(aTarget);
				return true;
			}
		});

		add(exportDownload = new AjaxFileDownload());

		add(statisticsForm = new StatisticsForm("statisticsForm"));
		add(displayOptions = (WebMarkupContainer) new WebMarkupContainer("displayOptions")
		{
			private static final long serialVersionUID = 1L;
			{
				add(new Label("filterLabel", new PropertyModel<List<ResultFilter>>(statisticsForm,
						"modelObject.filters")));
				add(new Label("collectionIdLabel", new PropertyModel<Set<String>>(statisticsForm,
						"modelObject.collections")));
				add(new Label("typeLabel", new PropertyModel<Set<AnnotationType>>(statisticsForm,
						"modelObject.types"))
				{
					private static final long serialVersionUID = 1L;

					@Override
					public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
					{
						Set<AnnotationType> types = (Set<AnnotationType>) getDefaultModelObject();
						List<String> typeNames = new ArrayList<String>();
						for (AnnotationType t : types) {
							typeNames.add(t.getName());
						}
						Collections.sort(typeNames);
						replaceComponentTagBody(markupStream, openTag, typeNames.toString());
					};
				});
			}
		}.setOutputMarkupPlaceholderTag(true).setVisible(false));
		add(resultTable = new Label("resultTable").setOutputMarkupId(true));
	}

	public class ExportPanel
		extends Panel
	{
		private static final long serialVersionUID = 1L;

		private Form<ExportModel> form;
		private ProgressBar progressBar;
		private TaskProgressionModel progressionModel;
		private AjaxButton exportHtmlButton;
		private AjaxButton exportCsvButton;
		private AjaxButton exportXlsButton;
		private AjaxButton exportTkSvmButton;

		public ExportPanel(String aId)
		{
			super(aId);

			progressionModel = new TaskProgressionModel()
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected ITaskService getTaskService()
				{
					return StatisticsPage.this.getTaskService();
				}
			};

			add(form = new Form<ExportModel>("settingsForm",
					new CompoundPropertyModel<ExportModel>(new ExportModel())));

			form.add(new CheckBox("includePos").setOutputMarkupId(true));

			form.add(new NumberTextField<Integer>("contextSize").setMinimum(0).setMaximum(10000)
					.setOutputMarkupId(true));

			form.add(new ListMultipleChoice<AdditionalColumn>("additionalColumns",
					new LoadableDetachableModel<List<AdditionalColumn>>()
					{
						private static final long serialVersionUID = 1L;

						@Override
						protected List<AdditionalColumn> load()
						{
							List<AdditionalColumn> acs = new ArrayList<AdditionalColumn>();
							for (AnnotationType at : statisticsForm.getModelObject().types) {
								acs.addAll(at.getAdditionalColumns());
							}
							return acs;
						}
					}, new ChoiceRenderer<AdditionalColumn>("name")));

			form.add(exportHtmlButton = new AjaxButton("exportHtmlButton")
			{
				@Override
				protected void onSubmit(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					final ExportHtmlTask task = new ExportHtmlTask(statisticsForm.getModelObject(),
							form.getModelObject(), repository, contextProvider);

					// Schedule and start a new task
					Long taskId = StatisticsPage.this.getTaskService().scheduleAndStart(task);

					// Set taskId for model
					progressionModel.setTaskId(taskId);

					// disable buttons
					exportHtmlButton.setVisible(false);
					exportCsvButton.setVisible(false);
					exportXlsButton.setVisible(false);
					exportTkSvmButton.setVisible(false);
					aTarget.add(form);

					// Start the progress bar, will set visibility to true
					progressBar.start(aTarget);
				}

				@Override
				protected void onError(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					// Make sure the feedback messages are rendered since we have a modal window,
					// we don't want to show the messages on the regular feedback panel
					for (FeedbackMessage fm : getFeedbackPanel().getFeedbackMessagesModel()
							.getObject()) {
						aTarget.appendJavaScript("alert('"
								+ fm.getMessage().toString().replace("'", "\\'") + "');");
					}
				}
			});

			form.add(exportCsvButton = new AjaxButton("exportCsvButton")
			{
				@Override
				protected void onSubmit(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					final ExportCsvTask task = new ExportCsvTask(statisticsForm.getModelObject(),
							form.getModelObject(), repository, contextProvider);

					// Schedule and start a new task
					Long taskId = StatisticsPage.this.getTaskService().scheduleAndStart(task);

					// Set taskId for model
					progressionModel.setTaskId(taskId);

					// disable buttons
					exportHtmlButton.setVisible(false);
					exportCsvButton.setVisible(false);
					exportXlsButton.setVisible(false);
					exportTkSvmButton.setVisible(false);
					aTarget.add(form);

					// Start the progress bar, will set visibility to true
					progressBar.start(aTarget);
				}

				@Override
				protected void onError(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					// Make sure the feedback messages are rendered since we have a modal window,
					// we don't want to show the messages on the regular feedback panel
					for (FeedbackMessage fm : getFeedbackPanel().getFeedbackMessagesModel()
							.getObject()) {
						aTarget.appendJavaScript("alert('"
								+ fm.getMessage().toString().replace("'", "\\'") + "');");
					}
				}
			});

			form.add(exportXlsButton = new AjaxButton("exportXlsButton")
			{
				@Override
				protected void onSubmit(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					final ExportExcelTask task = new ExportExcelTask(statisticsForm
							.getModelObject(), form.getModelObject(), repository, contextProvider);

					// Schedule and start a new task
					Long taskId = StatisticsPage.this.getTaskService().scheduleAndStart(task);

					// Set taskId for model
					progressionModel.setTaskId(taskId);

					// disable buttons
					exportHtmlButton.setVisible(false);
					exportCsvButton.setVisible(false);
					exportXlsButton.setVisible(false);
					exportTkSvmButton.setVisible(false);
					aTarget.add(form);

					// Start the progress bar, will set visibility to true
					progressBar.start(aTarget);
				}

				@Override
				protected void onError(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					// Make sure the feedback messages are rendered since we have a modal window,
					// we don't want to show the messages on the regular feedback panel
					for (FeedbackMessage fm : getFeedbackPanel().getFeedbackMessagesModel()
							.getObject()) {
						aTarget.appendJavaScript("alert('"
								+ fm.getMessage().toString().replace("'", "\\'") + "');");
					}
				}
			});

			form.add(exportTkSvmButton = new AjaxButton("exportTkSvmButton")
			{
				@Override
				protected void onSubmit(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					if (statisticsForm.getModelObject().collections.size() == 0) {
						error("Cannot export empty collection");
						aTarget.add(getFeedbackPanel());
						cancel(aTarget);
						return;
					}

					String language = corpusService.getCorpus(
							statisticsForm.getModelObject().collections.iterator().next())
							.getLanguage();

					for (String cId : statisticsForm.getModelObject().collections) {
						if (!language.equals(corpusService.getCorpus(cId))) {
							error("Can not export collections with different languages");
							aTarget.add(getFeedbackPanel());
							cancel(aTarget);
							return;
						}
					}

					final ExportTkSvmTask task = new ExportTkSvmTask(statisticsForm
							.getModelObject(), form.getModelObject(), repository, language);

					// Schedule and start a new task
					Long taskId = StatisticsPage.this.getTaskService().scheduleAndStart(task);

					// Set taskId for model
					progressionModel.setTaskId(taskId);

					// disable buttons
					exportHtmlButton.setVisible(false);
					exportCsvButton.setVisible(false);
					exportXlsButton.setVisible(false);
					exportTkSvmButton.setVisible(false);
					aTarget.add(form);

					// Start the progress bar, will set visibility to true
					progressBar.start(aTarget);
				}

				@Override
				protected void onError(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					// Make sure the feedback messages are rendered since we have a modal window,
					// we don't want to show the messages on the regular feedback panel
					for (FeedbackMessage fm : getFeedbackPanel().getFeedbackMessagesModel()
							.getObject()) {
						aTarget.appendJavaScript("alert('"
								+ fm.getMessage().toString().replace("'", "\\'") + "');");
					}
				}
			});

			form.add(progressBar = new ProgressBar("progress", progressionModel)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onFinished(AjaxRequestTarget aTarget)
				{
					finishTask(aTarget);
				}
			});
			progressBar.setVisible(false);

			form.add(new AjaxLink("cancelButton")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget aTarget)
				{
					cancel(aTarget);
				}
			});
		}

		protected void cancel(AjaxRequestTarget aTarget)
		{
			getTaskService().cancel(progressionModel.getTaskId());
			form.detach();
			finishTask(aTarget);
		}

		protected void finishTask(AjaxRequestTarget aTarget)
		{
			ExportTask task = (ExportTask) getTaskService().getTask(progressionModel.getTaskId());

			// show errors which might have occurred
			for (Message m : getTaskService().getMessages(progressionModel.getTaskId())) {
				error(m.messageKey);
			}

			// finish the task!
			getTaskService().finish(progressionModel.getTaskId());

			progressBar.setVisible(false);

			// re-enable button
			form.get("exportHtmlButton").setVisible(true);
			form.get("exportCsvButton").setVisible(true);
			form.get("exportXlsButton").setVisible(true);
			form.get("exportTkSvmButton").setVisible(true);
			aTarget.add(form, getFeedbackPanel());
			exportModal.close(aTarget);

			if (task != null && !task.isCancelled()) {
				exportDownload.initiate(aTarget, task.getOutputFile(), task.getFilename(),
						"binary/octet-stream");
			}
		}
	}

	public static class ExportModel
		implements Serializable
	{
		private static final long serialVersionUID = 1L;

		public boolean includePos = false;
		public int contextSize = 130;
		public List<AdditionalColumn> additionalColumns = new ArrayList<AdditionalColumn>();
	}
}
