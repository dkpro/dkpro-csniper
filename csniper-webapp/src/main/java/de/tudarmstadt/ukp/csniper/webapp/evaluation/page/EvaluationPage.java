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
package de.tudarmstadt.ukp.csniper.webapp.evaluation.page;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.UIMAException;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.CloseButtonCallback;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
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
import org.apache.wicket.util.lang.PropertyResolver;
import org.odlabs.wiquery.ui.tabs.Tabs;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.engine.BaseRenderEngine;
import org.radeox.engine.context.BaseRenderContext;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.wicketstuff.progressbar.ProgressBar;

import de.tudarmstadt.ukp.csniper.webapp.DefaultValues;
import de.tudarmstadt.ukp.csniper.webapp.analysis.ParseTreeResource;
import de.tudarmstadt.ukp.csniper.webapp.analysis.uima.ParsingPipeline;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.EvaluationRepository;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.MlPipeline;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.SortableEvaluationResultDataProvider;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.SortableEvaluationResultDataProvider.ResultFilter;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.AdditionalColumn;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.CachedParse;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationItem;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationResult;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.Mark;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.Query;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.SampleSet;
import de.tudarmstadt.ukp.csniper.webapp.page.ApplicationPageBase;
import de.tudarmstadt.ukp.csniper.webapp.project.ProjectRepository;
import de.tudarmstadt.ukp.csniper.webapp.project.model.AnnotationType;
import de.tudarmstadt.ukp.csniper.webapp.search.ContextProvider;
import de.tudarmstadt.ukp.csniper.webapp.search.CorpusService;
import de.tudarmstadt.ukp.csniper.webapp.search.PreparedQuery;
import de.tudarmstadt.ukp.csniper.webapp.search.SearchEngine;
import de.tudarmstadt.ukp.csniper.webapp.statistics.model.AggregatedEvaluationResult;
import de.tudarmstadt.ukp.csniper.webapp.support.task.ITaskService;
import de.tudarmstadt.ukp.csniper.webapp.support.task.Task;
import de.tudarmstadt.ukp.csniper.webapp.support.task.TaskProgressionModel;
import de.tudarmstadt.ukp.csniper.webapp.support.task.Task.Message;
import de.tudarmstadt.ukp.csniper.webapp.support.uima.CasHolder;
import de.tudarmstadt.ukp.csniper.webapp.support.wicket.AnalysisPanel;
import de.tudarmstadt.ukp.csniper.webapp.support.wicket.CustomDataTable;
import de.tudarmstadt.ukp.csniper.webapp.support.wicket.DbFieldMaxLengthValidator;
import de.tudarmstadt.ukp.csniper.webapp.support.wicket.EmbeddableImage;
import de.tudarmstadt.ukp.csniper.webapp.support.wicket.ExtendedIndicatingAjaxButton;
import de.tudarmstadt.ukp.csniper.webapp.support.wicket.LocalizerUtil;
import de.tudarmstadt.ukp.csniper.webapp.support.wicket.ThresholdLink;

/**
 * Evaluation Page
 */
@SuppressWarnings({ "rawtypes", "unused" })
public class EvaluationPage
	extends ApplicationPageBase
{
	private static final long serialVersionUID = 1L;

	private Tabs tabs;
	private ParentOptionsForm parentOptionsForm;
	private QueryForm queryForm;
	private ReviewForm reviewForm;
	private SamplesetForm samplesetForm;
	private FindForm findForm;
	private FilterForm filterForm;
	private ShowColumnsForm showColumnsForm;
	private LimitForm limitForm;
	private WebMarkupContainer contextViewsContainer;
	private ListView<ContextView> contextViews;
	private ExtendedIndicatingAjaxButton saveButton;
	private ExtendedIndicatingAjaxButton predictButton;
	private ExtendedIndicatingAjaxButton samplesetButton;
	private List<IColumn<EvaluationResult>> columns;
	private boolean showResultColumns;
	private Component resultTable;
	private ModalWindow samplesetModal;
	private ModalWindow predictionModal;
	private ModalWindow analysisModal;
	private ParsingPipeline pp;
	private SortableEvaluationResultDataProvider dataProvider;
	private boolean contextAvailable = false;
	private Map<AdditionalColumn, Boolean> showColumns;

	private static final int ROWS_PER_PAGE = 10;
	private static final int MAX_RESULTS = 1000;

	private static final int MIN_ITEMS_ANNOTATED = 10;

	@SpringBean(name = "evaluationRepository")
	private EvaluationRepository repository;

	@SpringBean(name = "projectRepository")
	private ProjectRepository projectRepository;

	@SpringBean(name = "corpusService")
	private CorpusService corpusService;

	@SpringBean(name = "contextProvider")
	private ContextProvider contextProvider;

	private class ReviewForm
		extends Form<ReviewFormModel>
	{
		private static final long serialVersionUID = 1L;

		public ReviewForm(String aId)
		{
			super(aId, new CompoundPropertyModel<ReviewFormModel>(new ReviewFormModel()));

			add(new CheckBox("disputedOnly"));

			add(new ExtendedIndicatingAjaxButton("reviewButton", new Model<String>(
					"Review evaluations"), new Model<String>("Running query ..."))
			{
				private static final long serialVersionUID = 1L;

				// {
				// setDefaultFormProcessing(false);
				// }

				@Override
				public void onSubmit(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					AnnotationType type = parentOptionsForm.typeInput.getModelObject();
					if (type == null) {
						error(LocalizerUtil.getString(parentOptionsForm.typeInput, "Required"));
						aTarget.add(getFeedbackPanel());
						return;
					}

					ParentOptionsFormModel pModel = parentOptionsForm.getModelObject();
					String user = SecurityContextHolder.getContext().getAuthentication().getName();

					List<EvaluationResult> results;

					if (ReviewForm.this.getModelObject().disputedOnly) {
						results = repository.listDisputedEvaluationResults(pModel.collectionId,
								pModel.type, user);
					}
					else {
						results = repository.listEvaluationResults(pModel.collectionId,
								pModel.type, user);
					}

					// persis. results: hide saveButton, show result columns and filter options
					limitForm.setVisible(false);
					filterForm.setChoices(ResultFilter.values());
					// only show
					showColumnsForm.setVisible(true && !pModel.type.getAdditionalColumns()
							.isEmpty());
					showResultColumns(true);
					saveButton.setVisible(false);
					predictButton.setVisible(true);
					samplesetButton.setVisible(true);

					// update dataprovider
					dataProvider = new SortableEvaluationResultDataProvider(results);
					dataProvider.setSort("item.documentId", SortOrder.ASCENDING);
					dataProvider.setFilter(ResultFilter.ALL);
					// then update the table
					resultTable = resultTable
							.replaceWith(new CustomDataTable<EvaluationResult>("resultTable",
									getAllColumns(pModel.type), dataProvider, ROWS_PER_PAGE));
					contextAvailable = false;

					updateComponents(aTarget);
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
	}

	private static class ReviewFormModel
		implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private boolean disputedOnly = false;
	}

	private class QueryForm
		extends Form<QueryFormModel>
	{
		private static final long serialVersionUID = 1L;

		final DropDownChoice<SearchEngine> engineInput;
		final TextArea<String> queryInput;
		final TextField<String> commentInput;
		final DropDownChoice<Query> historyQueryInput;
		final ExtendedIndicatingAjaxButton queryButton;

		@SuppressWarnings({ "serial" })
		public QueryForm(String id)
		{
			super(id, new CompoundPropertyModel<QueryFormModel>(new QueryFormModel()));

			// Tab contents
			engineInput = new DropDownChoice<SearchEngine>("engine",
					new LoadableDetachableModel<List<SearchEngine>>()
					{
						@Override
						protected List<SearchEngine> load()
						{
							ParentOptionsFormModel pModel = parentOptionsForm.getModelObject();
							return corpusService.listEngines(pModel.collectionId);
						}
					}, new IChoiceRenderer<SearchEngine>()
					{
						@Override
						public Object getDisplayValue(SearchEngine aObject)
						{
							return aObject.getName();
						}

						@Override
						public String getIdValue(SearchEngine aObject, int aIndex)
						{
							return aObject.getName();
						}
					})
			{
				@Override
				protected boolean wantOnSelectionChangedNotifications()
				{
					return true;
				}

				@Override
				protected void onSelectionChanged(SearchEngine aEngine)
				{
					historyQueryInput.setModelObject(null);
					queryInput.setModelObject("");
					commentInput.setModelObject("");
				}
			};
			engineInput.setRequired(true);

			queryInput = new TextArea<String>("query");
			queryInput.setRequired(true);
			queryInput.add(new DbFieldMaxLengthValidator(projectRepository, "Query", "query"));

			commentInput = new TextField<String>("comment");
			commentInput.add(new DbFieldMaxLengthValidator(projectRepository, "Query", "comment"));

			historyQueryInput = (QueryDropDown) new QueryDropDown("historyQuery");

			// submit button
			queryButton = new ExtendedIndicatingAjaxButton("queryButton", new Model<String>(
					"Submit query"), new Model<String>("Running query ..."))
			{
				@Override
				public void onSubmit(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					AnnotationType type = parentOptionsForm.typeInput.getModelObject();
					if (type == null) {
						error(LocalizerUtil.getString(parentOptionsForm.typeInput, "Required"));
						aTarget.add(getFeedbackPanel());
						return;
					}

					QueryFormModel model = QueryForm.this.getModelObject();
					ParentOptionsFormModel pModel = parentOptionsForm.getModelObject();
					String user = SecurityContextHolder.getContext().getAuthentication().getName();
					List<EvaluationItem> items;

					// only execute query if it was set
					if (!StringUtils.isBlank(model.query)) {
						PreparedQuery query = null;
						try {
							query = model.engine.createQuery(pModel.type.getName(),
									pModel.collectionId, model.query);
							query.setMaxResults(MAX_RESULTS);
							items = query.execute();
							int resultCount = query.size();
							limitForm.setResultCount(resultCount);
							// new results: show limitForm if too many results, show saveButton
							limitForm.setVisible(resultCount > MAX_RESULTS);
							saveButton.setVisible(true);
						}
						catch (NonTransientDataAccessException e) {
							error("Error executing query " + model.query + ": " + e.getMessage());
							items = new ArrayList<EvaluationItem>();
							// error -> no results: hide limitForm, saveButton
							limitForm.setVisible(false);
							saveButton.setVisible(false);
						}
						finally {
							// IOUtils.closeQuietly(query);
						}
						// new items (or error), so show only item columns, no filters
						filterForm.setChoices();
						showColumnsForm.setVisible(false);
						showResultColumns(false);
						predictButton.setVisible(false);
						samplesetButton.setVisible(false);
					}
					// else do not execute cqp, instead fetch all results for given type from db
					// TODO dead code (query is a required field) - do we want this functionality?
					else {
						items = repository.listEvaluationItems(pModel.collectionId,
								pModel.type.getName());

						// persis. results: hide saveButton, show result columns and filter options
						limitForm.setVisible(false);
						filterForm.setChoices(ResultFilter.values());
						showColumnsForm.setVisible(true && !pModel.type.getAdditionalColumns()
								.isEmpty());
						showResultColumns(true);
						saveButton.setVisible(false);
						predictButton.setVisible(true);
						samplesetButton.setVisible(true);
					}
					// update dataprovider
					dataProvider = new SortableEvaluationResultDataProvider(
							createEvaluationResults(items));
					dataProvider.setSort("item.documentId", SortOrder.ASCENDING);
					dataProvider.setFilter(ResultFilter.ALL);
					// then update the table
					resultTable = resultTable
							.replaceWith(new CustomDataTable<EvaluationResult>("resultTable",
									getAllColumns(pModel.type), dataProvider, ROWS_PER_PAGE));
					contextAvailable = true;

					updateComponents(aTarget);
				}

				@Override
				public void onError(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					super.onError(aTarget, aForm);
					// Make sure the feedback messages are rendered
					aTarget.add(getFeedbackPanel());
				}
			};
			add(engineInput);
			add(historyQueryInput);
			add(queryInput);
			add(commentInput);
			add(queryButton);
		}

		private class QueryDropDown
			extends DropDownChoice<Query>
		{
			private static final long serialVersionUID = 1L;

			private final Query examplesHeader = new Query(null, "-- Examples --", null, null);
			private final Query historyHeader = new Query(null, "-- History --", null, null);

			public QueryDropDown(String aId)
			{
				super(aId);
				setChoiceRenderer(new ChoiceRenderer<Query>()
				{
					private static final long serialVersionUID = 1L;

					@Override
					public Object getDisplayValue(Query aObject)
					{
						return aObject.getQuery();
					}
				});
				setChoices(new LoadableDetachableModel<List<Query>>()
				{
					private static final long serialVersionUID = 1L;

					@Override
					protected List<Query> load()
					{
						ParentOptionsFormModel pModel = parentOptionsForm.getModelObject();
						QueryFormModel model = QueryForm.this.getModelObject();
						List<Query> queries = new ArrayList<Query>();

						if (model.engine != null) {
							String user = SecurityContextHolder.getContext().getAuthentication()
									.getName();
							List<Query> exampleQueries = getExampleQueries(model.engine.getName(),
									pModel.collectionId, pModel.type);

							if (!exampleQueries.isEmpty()) {
								queries.add(examplesHeader);
								queries.addAll(exampleQueries);
								queries.add(historyHeader);
							}
							queries.addAll(repository.listQueries(model.engine.getName(),
									pModel.collectionId, pModel.type, user));
						}

						return queries;
					}
				});
			}

			@Override
			protected boolean wantOnSelectionChangedNotifications()
			{
				return true;
			}

			@Override
			protected void onSelectionChanged(Query newSelection)
			{
				if (newSelection.getType() != null) {
					queryInput.setModelObject(newSelection.getQuery());
					commentInput.setModelObject(newSelection.getComment());
				}
			}
		}
	}

	private static class QueryFormModel
		implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private SearchEngine engine = null;
		private String query = "";
		private String comment = "";
		private Query historyQuery;
		private boolean randomize = false;
	}

	private class SamplesetForm
		extends Form<SamplesetFormModel>
	{
		private static final long serialVersionUID = 1L;

		public SamplesetForm(String aId)
		{
			super(aId, new CompoundPropertyModel<SamplesetFormModel>(new SamplesetFormModel()));

			SamplesetDropdown samplesetDropdown = new SamplesetDropdown("sampleset");
			samplesetDropdown.setRequired(true);
			add(samplesetDropdown);

			add(new ExtendedIndicatingAjaxButton("samplesetButton", new Model<String>(
					"Load sampleset"), new Model<String>("Loading ..."))
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					ParentOptionsFormModel pModel = parentOptionsForm.getModelObject();
					SamplesetFormModel model = samplesetForm.getModelObject();
					String user = SecurityContextHolder.getContext().getAuthentication().getName();

					List<EvaluationResult> results = repository.listEvaluationResults(user,
							model.sampleset);

					// query results: show saveButton, hide result columns and filter options
					limitForm.setVisible(false);
					filterForm.setChoices(ResultFilter.values());
					showColumnsForm.setVisible(true && !pModel.type.getAdditionalColumns()
							.isEmpty());
					showResultColumns(true);
					saveButton.setVisible(false);
					predictButton.setVisible(true);
					samplesetButton.setVisible(true);

					// update dataprovider
					dataProvider = new SortableEvaluationResultDataProvider(results);
					dataProvider.setSort("item.documentId", SortOrder.ASCENDING);
					dataProvider.setFilter(ResultFilter.ALL);
					// then update the table
					resultTable = resultTable
							.replaceWith(new CustomDataTable<EvaluationResult>("resultTable",
									getAllColumns(pModel.type), dataProvider, ROWS_PER_PAGE));

					updateComponents(aTarget);
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
	}

	class SamplesetDropdown
		extends DropDownChoice<SampleSet>
	{
		private static final long serialVersionUID = 1L;

		public SamplesetDropdown(String aId)
		{
			super(aId);
			setChoices(new LoadableDetachableModel<List<SampleSet>>()
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected List<SampleSet> load()
				{
					ParentOptionsFormModel pModel = parentOptionsForm.getModelObject();
					String user = SecurityContextHolder.getContext().getAuthentication().getName();
					List<SampleSet> samplesets = new ArrayList<SampleSet>();

					// TODO what exactly? next time it would be helpful if i leave myself a note...
					if (pModel.collectionId == null || pModel.type == null)
						return samplesets;

					samplesets.addAll(repository.listSampleSets(pModel.collectionId,
							pModel.type.getName(), user));
					return samplesets;
				}
			});
			setChoiceRenderer(new ChoiceRenderer<SampleSet>("name"));
		}
	}

	public static class SamplesetFormModel
		implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private boolean createNew = true;
		private String newname = "";
		private String newcomment = "";
		private SampleSet sampleset;
	}

    private class FindForm
        extends Form<Void>
    {
        private static final long serialVersionUID = 1L;
        private ProgressBar progressBar;
        private TaskProgressionModel progressionModel;
        private ExtendedIndicatingAjaxButton findButton;
        private AjaxLink stopButton;

        public FindForm(String aId)
        {
            super(aId);

            progressionModel = new TaskProgressionModel()
            {
                private static final long serialVersionUID = 1L;

                @Override
                protected ITaskService getTaskService()
                {
                    return EvaluationPage.this.getTaskService();
                }
            };

            add(findButton = new ExtendedIndicatingAjaxButton("findButton", new Model<String>(
                    "Find"), new Model<String>("Finding ..."))
            {
                private static final long serialVersionUID = 1L;

                @Override
                public void onSubmit(AjaxRequestTarget aTarget, Form<?> aForm)
                {
                    final ParentOptionsFormModel pModel = parentOptionsForm.getModelObject();
                    final String user = SecurityContextHolder.getContext().getAuthentication()
                            .getName();

                    // update dataprovider
                    dataProvider = new SortableEvaluationResultDataProvider();
                    dataProvider.setSort("item.documentId", SortOrder.ASCENDING);
                    dataProvider.setFilter(ResultFilter.ALL);
                    // then update the table
                    resultTable = resultTable
                            .replaceWith(new CustomDataTable<EvaluationResult>("resultTable",
                                    getAllColumns(pModel.type), dataProvider, ROWS_PER_PAGE));
                    resultTable.setOutputMarkupPlaceholderTag(true);
                    aTarget.add(resultTable.setVisible(false));
                    contextAvailable = false;

                    // disable button
                    setVisible(false);
                    aTarget.add(stopButton.setVisible(true));
                    aTarget.add(saveButton.setVisible(false));
                    aTarget.add(predictButton.setVisible(false));
                    aTarget.add(samplesetButton.setVisible(false));
                    aTarget.add(showColumnsForm.setVisible(false));
                    filterForm.setChoices();
                    aTarget.add(filterForm);
                    aTarget.add(limitForm.setVisible(false));
                    aTarget.add(FindForm.this);
                    showResultColumns(false);
                    updateComponents(aTarget);

                    // Schedule and start a new task
                    Long taskId = EvaluationPage.this.getTaskService().scheduleAndStart(new Task()
                    {
                        @Override
                        protected void run()
                        {
                            try {
                                // get aggregated results
                                List<AggregatedEvaluationResult> aggregatedResults = repository
                                        .listAggregatedResults(singleton(pModel.collectionId),
                                                singleton(pModel.type), new HashSet<String>(
                                                        repository.listUsers()), 0.0, 0.0);

                                if (aggregatedResults.isEmpty()) {
                                    return;
                                }

                                // create training list
                                List<EvaluationResult> trainingList = MlPipeline
                                        .convertToSimple(aggregatedResults);

                                File modelDir = MlPipeline.train(trainingList, repository);

                                String language = corpusService.getCorpus(pModel.collectionId)
                                        .getLanguage();
                                int max = (int) repository.getCachedParsesCount(pModel.collectionId);

                                // Create list of pages
                                int pageSize = 1000;
                                int[][] pages = repository.listCachedParsesPages(
                                        pModel.collectionId, pageSize);
                                
                                // Shuffle pages
                                Random random = new Random();
                                for (int i = 0; i < pages.length; i++) {
                                    int randomPosition = random.nextInt(pages.length);
                                    int[] temp = pages[i];
                                    pages[i] = pages[randomPosition];
                                    pages[randomPosition] = temp;                                    
                                }

                                int goal = 1000;
                                setTotal(goal);
                                
                                for (int p = 0; p < pages.length; p++) {
                                    checkCanceled();

                                    List<CachedParse> parses = repository.listCachedParses(
                                            pModel.collectionId, pages[p][0], pages[p][1]);
                                    // In the last iteration, the page size is cropped to the size
                                    // of the last page.
                                    pageSize = parses.size();

                                    List<EvaluationResult> results = MlPipeline.classifyPreParsed(
                                            modelDir, parses, pModel.type.getName(), user);

                                    // Keep only the correct ones
                                    ListIterator<EvaluationResult> ri = results.listIterator();
                                    while (ri.hasNext()) {
                                        EvaluationResult r = ri.next();
                                        Mark mark = Mark.fromString(r.getResult());
                                        if (mark != Mark.PRED_CORRECT) {
                                            ri.remove();
                                        }
                                    }
                                    
                                    // Putting this here to avoid adding results of a still
                                    // running iteration to the table because this could tigger
                                    // a concurrent modification problem. Checking for cancelled
                                    // here again should reduce this risk to a minimum.
                                    checkCanceled();

                                    // Add to table
                                    dataProvider.getResults().addAll(results);
                                    
                                    setCurrent(dataProvider.getResults().size());
                                    
                                    // Check if goal was reached
                                    if (dataProvider.getResults().size() >= goal) {
                                        break;
                                    }
                                }
                            }
                            catch (UIMAException e) {
                                e.printStackTrace();
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    
                    // Set taskId for model
                    progressionModel.setTaskId(taskId);

                    // Start the progress bar, will set visibility to true
                    progressBar.start(aTarget);
                }

                @Override
                public void onError(AjaxRequestTarget aTarget, Form<?> aForm)
                {
                    super.onError(aTarget, aForm);
                    // Make sure the feedback messages are rendered
                    aTarget.add(getFeedbackPanel());
                }
            });

            add(progressBar = new ProgressBar("progress", progressionModel)
            {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onFinished(AjaxRequestTarget aTarget)
                {
                    finishTask(aTarget);
                }
            });
            // Hide progress bar initially
            progressBar.setVisible(false);

            add(stopButton = new AjaxLink("stopButton")
            {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(AjaxRequestTarget aTarget)
                {
                    cancel(aTarget);
                }
            });
            stopButton.setVisible(false);

        }

        protected void cancel(AjaxRequestTarget aTarget)
        {
            getTaskService().cancel(progressionModel.getTaskId());
            finishTask(aTarget);
        }

        protected void finishTask(AjaxRequestTarget aTarget)
        {
            for (Message m : getTaskService().getMessages(progressionModel.getTaskId())) {
                error(m.messageKey);
            }

            // finish the task!
            getTaskService().finish(progressionModel.getTaskId());

            // Hide progress bar after finish
            progressBar.setVisible(false);

            // re-enable button
            aTarget.add(findButton.setVisible(true));
            aTarget.add(stopButton.setVisible(false));
            aTarget.add(saveButton.setVisible(true));
            aTarget.add(resultTable.setVisible(true));
            aTarget.add(FindForm.this);
            updateComponents(aTarget);
        }
    }

    public static class PredictionFormModel
        implements Serializable
    {
        private static final long serialVersionUID = 1L;

		private Set<String> users = new HashSet<String>();
		private Double userThreshold = DefaultValues.DEFAULT_USER_THRESHOLD;
		private Double confidenceThreshold = DefaultValues.DEFAULT_CONFIDENCE_THRESHOLD;
		private boolean onlycurrent = true;
	}

	private class ParentOptionsForm
		extends Form<ParentOptionsFormModel>
	{
		private static final long serialVersionUID = 1L;

		final DropDownChoice<String> collectionIdInput;
		final DropDownChoice<AnnotationType> typeInput;

		@SuppressWarnings({ "serial" })
		public ParentOptionsForm(String id)
		{
			super(id, new CompoundPropertyModel<ParentOptionsFormModel>(
					new ParentOptionsFormModel()));

			// collection dropdown
			collectionIdInput = new DropDownChoice<String>("collectionId",
					new LoadableDetachableModel<List<String>>()
					{
						@Override
						protected List<String> load()
						{
							return corpusService.listCorpora();
						}
					}, new IChoiceRenderer<String>()
					{
						@Override
						public Object getDisplayValue(String aObject)
						{
							return corpusService.getCorpus(aObject).getName();
						}

						@Override
						public String getIdValue(String aObject, int aIndex)
						{
							return corpusService.getCorpus(aObject).getId();
						}
					})
			{

				@Override
				protected boolean wantOnSelectionChangedNotifications()
				{
					return true;
				}

				@Override
				protected void onSelectionChanged(String aNewSelection)
				{
					queryForm.engineInput.setModelObject(null);
					queryForm.historyQueryInput.setModelObject(null);
					queryForm.queryInput.setModelObject("");
					queryForm.commentInput.setModelObject("");

					tabs.setVisible(aNewSelection != null
							&& ParentOptionsForm.this.getModelObject().type != null);
				}
			};

			typeInput = new DropDownChoice<AnnotationType>("type")
			{
				{
					setRequired(true);
					setChoices(new LoadableDetachableModel<List<AnnotationType>>()
					{
						private static final long serialVersionUID = 1L;

						@Override
						protected List<AnnotationType> load()
						{
							return projectRepository.listAnnotationTypes();
						}
					});
					setChoiceRenderer(new ChoiceRenderer<AnnotationType>("name"));
				}

				@Override
				protected boolean wantOnSelectionChangedNotifications()
				{
					return true;
				}

				@Override
				protected void onSelectionChanged(AnnotationType aNewSelection)
				{
					queryForm.engineInput.setModelObject(null);
					queryForm.historyQueryInput.setModelObject(null);
					queryForm.queryInput.setModelObject("");
					queryForm.commentInput.setModelObject("");

					// get default visibility settings for additional columns
					// projectRepository.refreshEntity(aNewSelection);

					// type changed: hide everything
					limitForm.setVisible(false);
					filterForm.setChoices();
					showColumnsForm.setVisible(false);
					showResultColumns(false);
					saveButton.setVisible(false);
					predictButton.setVisible(false);
					samplesetButton.setVisible(false);

					tabs.setVisible(aNewSelection != null
							&& ParentOptionsForm.this.getModelObject().collectionId != null);
				}
			};

			add(typeInput).add(collectionIdInput);
		}
	}

	private static class ParentOptionsFormModel
		implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String collectionId;
		private AnnotationType type;
	}

	private class FilterForm
		extends Form
	{
		private static final long serialVersionUID = 1L;

		private ResultFilter filter = ResultFilter.ALL;
		private RadioChoice<ResultFilter> filterGroup;

		public FilterForm(String id)
		{
			super(id);

			filterGroup = new RadioChoice<ResultFilter>("filterGroup",
					new PropertyModel<ResultFilter>(this, "filter"), Arrays.asList(ResultFilter
							.values()), new ChoiceRenderer<ResultFilter>("label"))
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected boolean wantOnSelectionChangedNotifications()
				{
					return true;
				}

				@Override
				protected void onSelectionChanged(final Object newSelection)
				{
					ParentOptionsFormModel pModel = parentOptionsForm.getModelObject();
					filter = (ResultFilter) newSelection;

					// update results based on new filter
					dataProvider.setFilter(filter);
					resultTable = resultTable
							.replaceWith(new CustomDataTable<EvaluationResult>("resultTable",
									getAllColumns(pModel.type), dataProvider, ROWS_PER_PAGE));
				}
			};
			filterGroup.setSuffix("\n");
			add(filterGroup);
		}

		public void setChoices(ResultFilter... aChoices)
		{
			if (aChoices.length == 0) {
				filter = ResultFilter.ALL;
				setVisible(false);
				filterGroup.setChoices(Collections.EMPTY_LIST);
			}
			else {
				setVisible(true);
				if (!Arrays.asList(aChoices).contains(filter)) {
					filter = aChoices[0];
				}
				filterGroup.setChoices(Arrays.asList(aChoices));
			}
		}
	}

	private class ShowColumnsForm
		extends Form<List<AdditionalColumn>>
	{
		private static final long serialVersionUID = 1L;

		private CheckGroup<AdditionalColumn> showColumnsGroup;
		private ListView<AdditionalColumn> lv;

		public ShowColumnsForm(String id)
		{
			super(id);

			showColumnsGroup = new CheckGroup<AdditionalColumn>("showColumnsGroup");
			lv = new ListView<AdditionalColumn>("showColumnsList",
					new LoadableDetachableModel<List<AdditionalColumn>>()
					{
						private static final long serialVersionUID = 1L;

						@Override
						protected List<AdditionalColumn> load()
						{
							return parentOptionsForm.getModelObject().type.getAdditionalColumns();
						}
					})
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(final ListItem<AdditionalColumn> aItem)
				{
					AdditionalColumn ac = aItem.getModelObject();

					aItem.add(new AjaxCheckBox("checkbox", new PropertyModel<Boolean>(ac,
							"showColumn"))
					{
						private static final long serialVersionUID = 1L;

						@Override
						protected void onUpdate(AjaxRequestTarget aTarget)
						{
							aTarget.add(resultTable);
						}
					}.setLabel(new Model<String>(ac.getName())));
				}
			};
			showColumnsGroup.add(lv);
			showColumnsGroup.setOutputMarkupId(true);
			add(showColumnsGroup);
		}
	}

	private class LimitForm
		extends Form
	{
		private static final long serialVersionUID = 1L;

		private int resultLimit = 0;
		private Label limitLabel;

		public LimitForm(String id)
		{
			super(id);

			add(limitLabel = new Label("limitLabel", ""));
			add(new TextField<Integer>("limitInput",
					new PropertyModel<Integer>(this, "resultLimit")));
			add(new ExtendedIndicatingAjaxButton("limitButton", new Model<String>("Show results"),
					new Model<String>("Retrieving results"))
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					QueryFormModel model = queryForm.getModelObject();
					ParentOptionsFormModel pModel = parentOptionsForm.getModelObject();
					PreparedQuery query = null;
					List<EvaluationItem> items;

					try {
						query = model.engine.createQuery(pModel.type.getName(),
								pModel.collectionId, model.query);
						query.setMaxResults(resultLimit);
						items = query.execute();
					}
					catch (NonTransientDataAccessException e) {
						error("Error executing query " + model.query + ": " + e.getMessage());
						items = new ArrayList<EvaluationItem>();
					}
					finally {
						// IOUtils.closeQuietly(query);
					}

					// query results: show saveButton, hide result columns and filter options
					limitForm.setVisible(false);
					filterForm.setChoices();
					showColumnsForm.setVisible(false);
					showResultColumns(false);
					saveButton.setVisible(true);
					predictButton.setVisible(false);
					samplesetButton.setVisible(false);

					// update dataprovider
					dataProvider = new SortableEvaluationResultDataProvider(
							createEvaluationResults(items));
					dataProvider.setSort("item.documentId", SortOrder.ASCENDING);
					dataProvider.setFilter(ResultFilter.ALL);
					// then update the table
					resultTable = resultTable
							.replaceWith(new CustomDataTable<EvaluationResult>("resultTable",
									getAllColumns(pModel.type), dataProvider, ROWS_PER_PAGE));

					updateComponents(aTarget);
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

		public void setResultCount(int aResultCount)
		{
			resultLimit = aResultCount;
			limitLabel.setDefaultModelObject("There are " + aResultCount
					+ " results in total. How many results shall be shown? ");
		}
	}

	private class PredictionPanel
		extends Panel
	{
		private static final long serialVersionUID = 1L;
		private Form<PredictionFormModel> form;
		private ProgressBar progressBar;
		private TaskProgressionModel progressionModel;

		public PredictionPanel(String aId)
		{
			super(aId);

			progressionModel = new TaskProgressionModel()
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected ITaskService getTaskService()
				{
					return EvaluationPage.this.getTaskService();
				}
			};

			add(form = new Form<PredictionFormModel>("predictionForm",
					new CompoundPropertyModel<PredictionFormModel>(new PredictionFormModel())));

			form.add(new AjaxCheckBox("onlycurrent")
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onUpdate(AjaxRequestTarget aTarget)
				{
					boolean greyOut = !form.getModelObject().onlycurrent;
					form.get("users").setEnabled(greyOut);
					form.get("userThreshold").setEnabled(greyOut);
					form.get("confidenceThreshold").setEnabled(greyOut);
					aTarget.add(form.get("users"), form.get("userThreshold"),
							form.get("confidenceThreshold"));
				}
			});

			List<String> users = repository.listUsers();
			Collections.sort(users);
			form.add(new ListMultipleChoice<String>("users", users).setOutputMarkupId(true)
					.setEnabled(false));
			form.add(new ThresholdLink("thresholdHelp"));
			form.add(new NumberTextField<Double>("userThreshold").setMinimum(0.0).setMaximum(1.0)
					.setOutputMarkupId(true).setEnabled(false));
			form.add(new NumberTextField<Double>("confidenceThreshold").setMinimum(0.0)
					.setMaximum(1.0).setOutputMarkupId(true).setEnabled(false));

			form.add(new ExtendedIndicatingAjaxButton("predictionOkButton", new Model<String>(
					"Predict results"), new Model<String>("Predicting..."))
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					final ParentOptionsFormModel pModel = parentOptionsForm.getModelObject();
					final PredictionFormModel model = form.getModelObject();

					// Schedule and start a new task
					Long taskId = EvaluationPage.this.getTaskService().scheduleAndStart(new Task()
					{
						@Override
						protected void run()
						{
							List<EvaluationResult> results = dataProvider.getResults();
							if (results.isEmpty()) {
								return;
							}
							String language = corpusService.getCorpus(
									results.get(0).getItem().getCollectionId()).getLanguage();

							try {
								boolean result;
								String errorMsg;

								MlPipeline mlp = new MlPipeline(language);
								mlp.setTask(this);
								mlp.setRepostitory(repository);

								// if taking only current results into account, ignore user list
								if (model.onlycurrent) {
									// parse, obtain Penn trees, predict results
									result = mlp.predict(results, MIN_ITEMS_ANNOTATED);
									errorMsg = "You have not annotated enough items to use the "
									        + "prediction feature. Please annotate at least ["
											+ MIN_ITEMS_ANNOTATED + "] items manually.";
								}
								else {
									// parse, obtain Penn trees, predict results
									boolean b = result = mlp.predictAggregated(results,
											pModel.collectionId, pModel.type, model.users,
											model.userThreshold, model.userThreshold);
									errorMsg = "The options you selected did not lead to any results. Please respecify the options.";
								}
								if (result == false) {
									error(errorMsg);
								}
							}
							catch (UIMAException e) {
								e.printStackTrace();
							}
							catch (IOException e) {
								e.printStackTrace();
							}
						}
					});

					// Set taskId for model
					progressionModel.setTaskId(taskId);

					// disable button
					setEnabled(false);
					setVisible(false);

					aTarget.add(form);
					aTarget.add(this);

					// Start the progress bar, will set visibility to true
					progressBar.start(aTarget);
				}

				@Override
				public void onError(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					super.onError(aTarget, aForm);
					// Make sure the feedback messages are rendered
					// since we have a modal window, we don't want to show the messages on the
					// regular feedbackpanel
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
			// Hide progress bar initially
			progressBar.setVisible(false);

			form.add(new AjaxLink("predictionCancelButton")
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
			for (Message m : getTaskService().getMessages(progressionModel.getTaskId())) {
				error(m.messageKey);
			}

			// finish the task!
			getTaskService().finish(progressionModel.getTaskId());

			// Hide progress bar after finish
			progressBar.setVisible(false);

			// re-enable button
			Component button = form.get("predictionOkButton");
			button.setEnabled(true);
			button.setVisible(true);
			aTarget.add(button);
			aTarget.add(form);
			dataProvider.setSort(new SortParam("score", false));
			updateComponents(aTarget);
			predictionModal.close(aTarget);
		}
	}

	private class SamplesetPanel
		extends Panel
	{
		private static final long serialVersionUID = 1L;
		private Form<SamplesetFormModel> form;

		public SamplesetPanel(String aId)
		{
			super(aId);

			add(form = new Form<SamplesetFormModel>("samplesetForm",
					new CompoundPropertyModel<SamplesetFormModel>(new SamplesetFormModel()))
			{
				private static final long serialVersionUID = 1L;

				{
					final RadioGroup group = new RadioGroup<Boolean>("createNew");
					final TextField<String> newnameField = new TextField<String>("newname");
					newnameField.add(new DbFieldMaxLengthValidator(projectRepository, "SampleSet",
							"name"));

					final TextField<String> newcommentField = new TextField<String>("newcomment");
					newcommentField.add(new DbFieldMaxLengthValidator(projectRepository,
							"SampleSet", "comment"));

					final SamplesetDropdown samplesetDD = new SamplesetDropdown("sampleset");

					group.add(newnameField.setOutputMarkupId(true));
					group.add(newcommentField.setOutputMarkupId(true));
					group.add(samplesetDD.setOutputMarkupId(true));
					samplesetDD.setEnabled(false);
					group.add(new Radio<Boolean>("true", new Model<Boolean>(true))
							.add(new AjaxEventBehavior("onclick")
							{
								private static final long serialVersionUID = 1L;

								@Override
								protected void onEvent(AjaxRequestTarget aTarget)
								{
									newnameField.setEnabled(true);
									newcommentField.setEnabled(true);
									samplesetDD.setEnabled(false);
									aTarget.add(newnameField, newcommentField, samplesetDD);
								}
							}));
					group.add(new Radio<Boolean>("false", new Model<Boolean>(false))
							.add(new AjaxEventBehavior("onclick")
							{
								private static final long serialVersionUID = 1L;

								@Override
								protected void onEvent(AjaxRequestTarget aTarget)
								{
									newnameField.setEnabled(false);
									newcommentField.setEnabled(false);
									samplesetDD.setEnabled(true);
									aTarget.add(newnameField, newcommentField, samplesetDD);
								}
							}));
					add(group);
					add(new AjaxSubmitLink("samplesetOkButton")
					{
						private static final long serialVersionUID = 1L;

						@Override
						public void onSubmit(AjaxRequestTarget aTarget, Form<?> aForm)
						{
							ParentOptionsFormModel pModel = parentOptionsForm.getModelObject();
							SamplesetFormModel model = form.getModelObject();
							String user = SecurityContextHolder.getContext().getAuthentication()
									.getName();
							List<EvaluationItem> items = dataProvider.getItems();
							boolean setExists = false;

							if (model.createNew) {
								if (StringUtils.isEmpty(model.newname)) {
									aTarget.appendJavaScript("alert('Choose a name for your sampleset.');");
									return;
								}

								for (SampleSet s : repository.listSampleSets()) {
									if (s.getName().equals(model.newname)) {
										setExists = true;
										break;
									}
								}

								if (!setExists) {
									repository.recordSampleSet(model.newname, pModel.collectionId,
											pModel.type.getName(), model.newcomment, user, items);

									form.setModelObject(new SamplesetFormModel());
									samplesetModal.close(aTarget);
									aTarget.add(form, samplesetForm);
								}
								else {
									aTarget.appendJavaScript("alert('A sampleset with this name already exists.');");
								}
							}
							else {
								if (model.sampleset == null) {
									aTarget.appendJavaScript("alert('Choose a sampleset from the dropdownmenu.');");
									return;
								}

								repository.updateSampleSet(model.sampleset, items);

								form.setModelObject(new SamplesetFormModel());
								samplesetModal.close(aTarget);
								aTarget.add(form);
							}
						}

						@Override
						protected void onError(AjaxRequestTarget aTarget, Form<?> aForm)
						{
							error("Error closing the modal sampleset window.");
						}
					});
					add(new AjaxLink("samplesetCancelButton")
					{
						private static final long serialVersionUID = 1L;

						@Override
						public void onClick(AjaxRequestTarget aTarget)
						{
							form.detach();
							samplesetModal.close(aTarget);
						}
					});
				}
			});
		}
	}

	/**
	 * Constructor that is invoked when page is invoked without a session.
	 */
	@SuppressWarnings({ "serial" })
	public EvaluationPage()
	{
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

		columns = new ArrayList<IColumn<EvaluationResult>>();
		columns.add(new AbstractColumn<EvaluationResult>(new Model<String>(""))
		{
			@Override
			public void populateItem(final Item<ICellPopulator<EvaluationResult>> aCellItem,
					String aComponentId, final IModel<EvaluationResult> model)
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
						    aTarget.add(getFeedbackPanel());
							error("Unable to load context: " + e.getMessage());
						}
					}
				});
				iconContext.add(new AttributeModifier("class",
						new Model<String>("clickableElement")));
				aCellItem.add(iconContext);
			}
		});
		columns.add(new AbstractColumn<EvaluationResult>(new Model<String>(""))
		{
			@Override
			public void populateItem(final Item<ICellPopulator<EvaluationResult>> aCellItem,
					final String aComponentId, final IModel<EvaluationResult> model)
			{
				// PopupLink pl = new PopupLink(aComponentId, new AnalysisPage(model.getObject()
				// .getItem()), "analysis", "Analyse", 800, 600);
				// pl.add(new AttributeModifier("class", new Model<String>("clickableElement")));
				// aCellItem.add(pl);

				EmbeddableImage iconAnalysis = new EmbeddableImage(aComponentId,
						new ContextRelativeResource("images/analysis.png"));
				iconAnalysis.add(new AjaxEventBehavior("onclick")
				{
					@Override
					protected void onEvent(AjaxRequestTarget aTarget)
					{
						EvaluationItem item = model.getObject().getItem();
						CachedParse cachedTree = repository.getCachedParse(item);
						ParseTreeResource ptr;

						if (cachedTree != null) {
							ptr = new ParseTreeResource(cachedTree.getPennTree());
						}
						else {
							if (pp == null) {
								pp = new ParsingPipeline();
							}
							CasHolder ch = new CasHolder(pp.parseInput("stanfordParser",
									corpusService.getCorpus(item.getCollectionId()).getLanguage(),
									item.getCoveredText()));
							ptr = new ParseTreeResource(ch);
						}
						analysisModal.setContent(new AnalysisPanel(analysisModal.getContentId(),
								ptr));
						analysisModal.show(aTarget);
					}
				});
				iconAnalysis.add(new AttributeModifier("class", new Model<String>(
						"clickableElement")));
				aCellItem.add(iconAnalysis);
			}
		});
		// columns.add(new PropertyColumn(new Model<String>("ID"), "id", "id"));
		// columns.add(new PropertyColumn(new Model<String>("Collection"), "item.collectionId",
		// "item.collectionId"));
		columns.add(new PropertyColumn<EvaluationResult>(new Model<String>("Doc"),
				"item.documentId", "item.documentId"));
		// columns.add(new PropertyColumn(new Model<String>("Begin"), "item.beginOffset",
		// "item.beginOffset"));
		// columns.add(new PropertyColumn(new Model<String>("End"), "item.endOffset",
		// "item.endOffset"));
		columns.add(new PropertyColumn<EvaluationResult>(new Model<String>("Left"),
				"item.leftContext", "item.leftContext")
		{
			@Override
			public String getCssClass()
			{
				return contextAvailable ? "leftContext" : " hideCol";
			}
		});
		columns.add(new PropertyColumn<EvaluationResult>(new Model<String>("Match"), "item.match",
				"item.match")
		{
			@Override
			public String getCssClass()
			{
				return contextAvailable ? "match nowrap" : null;
			}
		});
		columns.add(new PropertyColumn<EvaluationResult>(new Model<String>("Right"),
				"item.rightContext", "item.rightContext")
		{
			@Override
			public String getCssClass()
			{
				return contextAvailable ? "rightContext" : " hideCol";
			}
		});
        columns.add(new PropertyColumn<EvaluationResult>(new Model<String>("Score"), "score", "score")
        {            
            @Override
            public String getCssClass()
            {
                return (showResultColumns ? "" : " hideCol");
            }
        });
		columns.add(new AbstractColumn<EvaluationResult>(new Model<String>("Label"), "result")
		{
			@Override
			public void populateItem(final Item<ICellPopulator<EvaluationResult>> aCellItem,
					String aComponentId, final IModel<EvaluationResult> model)
			{
				final Label resultLabel = new Label(aComponentId,
						new PropertyModel(model, "result"));
				resultLabel.setOutputMarkupId(true);
				aCellItem.add(resultLabel);
				if (showResultColumns) {
					aCellItem.add(AttributeModifier.replace("class", new Model<String>("editable "
							+ model.getObject().getResult().toLowerCase())));
				}

				aCellItem.add(new AjaxEventBehavior("onclick")
				{
					@Override
					protected void onEvent(AjaxRequestTarget aTarget)
					{
						EvaluationResult result = model.getObject();

						// cycle to next result
						Mark newResult = Mark.fromString(result.getResult()).next();

						// update database
						result.setResult(newResult.getTitle());
						repository.updateEvaluationResult(result);

						// update DataTable
						aCellItem.add(AttributeModifier.replace("class", new Model<String>(
								"editable " + newResult.getTitle().toLowerCase())));
						aTarget.add(resultLabel, aCellItem);
					}
				});
			}

			@Override
			public String getCssClass()
			{
				return (showResultColumns ? "" : " hideCol");
			}
		});
		columns.add(new AbstractColumn<EvaluationResult>(new Model<String>("Comment"), "comment")
		{
			@Override
			public void populateItem(Item<ICellPopulator<EvaluationResult>> cellItem,
					String componentId, final IModel<EvaluationResult> model)
			{
				cellItem.add(new AjaxEditableLabel<String>(componentId, new PropertyModel<String>(
						model, "comment"))
				{
					@Override
					public void onSubmit(final AjaxRequestTarget aTarget)
					{
						super.onSubmit(aTarget);

						EvaluationResult result = model.getObject();

						// get new comment
						String newComment = getEditor().getInput();

						// update database
						result.setComment(newComment);
						repository.updateEvaluationResult(result);
					}

					@Override
					public void onError(AjaxRequestTarget aTarget)
					{
						super.onError(aTarget);
						aTarget.add(getFeedbackPanel());
					}
				}.add(new DbFieldMaxLengthValidator(projectRepository, "EvaluationResult",
						"comment")));
			}

			@Override
			public String getCssClass()
			{
				return "editable" + (showResultColumns ? "" : " hideCol");
			}
		});

		// collection and type
		add(parentOptionsForm = new ParentOptionsForm("parentOptions"));

		tabs = new Tabs("tabs");
		tabs.setVisible(false);
		// query tab
		tabs.add(queryForm = new QueryForm("queryForm"));
		// revision tab
		tabs.add(reviewForm = new ReviewForm("reviewForm"));
		// completion tab
		tabs.add(new Form("completeForm")
		{
			{
				add(new ExtendedIndicatingAjaxButton("completeButton",
						new Model<String>("Complete"), new Model<String>("Running query ..."))
				{
					{
						setDefaultFormProcessing(false);
					}

					@Override
					public void onSubmit(AjaxRequestTarget aTarget, Form<?> aForm)
					{
						AnnotationType type = parentOptionsForm.typeInput.getModelObject();
						if (type == null) {
							error(LocalizerUtil.getString(parentOptionsForm.typeInput, "Required"));
							aTarget.add(getFeedbackPanel());
							return;
						}

						ParentOptionsFormModel pModel = parentOptionsForm.getModelObject();
						String user = SecurityContextHolder.getContext().getAuthentication()
								.getName();
						List<String> otherUsers = new ArrayList<String>(repository.listUsers());
						otherUsers.remove(user);

						// get items, create/persist results
						List<EvaluationItem> items = repository.listEvaluationResultsMissing(
								pModel.collectionId, pModel.type.getName(), user, otherUsers);
						List<EvaluationResult> results = createEvaluationResults(items);
						repository.writeEvaluationResults(results);

						// persis. results: hide saveButton, show result columns and filter options
						limitForm.setVisible(false);
						filterForm.setChoices(ResultFilter.values());
						showColumnsForm.setVisible(true && !pModel.type.getAdditionalColumns()
								.isEmpty());
						showResultColumns(true);
						saveButton.setVisible(false);
						predictButton.setVisible(true);
						samplesetButton.setVisible(true);

						// update dataprovider
						dataProvider = new SortableEvaluationResultDataProvider(results);
						dataProvider.setSort("item.documentId", SortOrder.ASCENDING);
						dataProvider.setFilter(ResultFilter.ALL);
						// then update the table
						resultTable = resultTable
								.replaceWith(new CustomDataTable<EvaluationResult>("resultTable",
										getAllColumns(pModel.type), dataProvider, ROWS_PER_PAGE));
						contextAvailable = false;

						updateComponents(aTarget);
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
		});
		// sampleset tab
		tabs.add(samplesetForm = new SamplesetForm("samplesetForm"));
        // sampleset tab
        tabs.add(findForm = new FindForm("findForm"));
		add(tabs);

		add(new Label("description", new LoadableDetachableModel<String>()
		{
			@Override
			protected String load()
			{
				Object value = PropertyResolver.getValue("type.description",
						parentOptionsForm.getModelObject());
				if (value != null) {
					RenderContext context = new BaseRenderContext();
					RenderEngine engine = new BaseRenderEngine();
					return engine.render(String.valueOf(value), context);
				}
				else {
					return getString("page.selectTypeHint");
				}
			}
		}).setEscapeModelStrings(false));
		add(filterForm = (FilterForm) new FilterForm("filterForm")
				.setOutputMarkupPlaceholderTag(true));
		add(showColumnsForm = (ShowColumnsForm) new ShowColumnsForm("showColumnsForm")
				.setOutputMarkupPlaceholderTag(true));
		add(resultTable = new Label("resultTable").setOutputMarkupId(true));

		add(predictionModal = new ModalWindow("predictionModal"));
		final PredictionPanel predictionPanel = new PredictionPanel(predictionModal.getContentId());
		predictionModal.setContent(predictionPanel);
		predictionModal.setTitle("Predict results");
		predictionModal.setAutoSize(false);
		predictionModal.setInitialWidth(550);
		predictionModal.setInitialHeight(350);
		predictionModal.setCloseButtonCallback(new CloseButtonCallback()
		{
			@Override
			public boolean onCloseButtonClicked(AjaxRequestTarget aTarget)
			{
				predictionPanel.cancel(aTarget);
				return true;
			}
		});

		add(samplesetModal = new ModalWindow("samplesetModal"));
		samplesetModal.setContent(new SamplesetPanel(samplesetModal.getContentId()));
		samplesetModal.setTitle("Create / Extend sampleset");
		samplesetModal.setAutoSize(true);

		add(analysisModal = new ModalWindow("analysisModal"));
		analysisModal.setTitle("Parse tree");
		analysisModal.setInitialWidth(65 * 16);
		analysisModal.setInitialHeight(65 * 9);
		// autosize does not work...
		// analysisModal.setAutoSize(true);

		add(new Form("saveForm")
		{
			{
				add(saveButton = (ExtendedIndicatingAjaxButton) new ExtendedIndicatingAjaxButton(
						"saveButton", new Model<String>("Start annotating"), new Model<String>(
								"Preparing ..."))
				{
					@Override
					protected void onSubmit(AjaxRequestTarget aTarget, Form<?> aForm)
					{
						// persist items and results
						List<EvaluationItem> items = dataProvider.getItems();
						items = repository.writeEvaluationItems(items);
						List<EvaluationResult> results = createEvaluationResults(items);
						dataProvider.setResults(results);
                        repository.writeEvaluationResults(results);

                        // save results, query
						ParentOptionsFormModel pModel = parentOptionsForm.getModelObject();
						String user = SecurityContextHolder.getContext().getAuthentication()
								.getName();
						QueryFormModel model = queryForm.getModelObject();
						if (model.engine != null && !StringUtils.isBlank(model.query)) {
    						repository.recordQuery(model.engine.getName(), model.query,
    								pModel.collectionId, pModel.type.getName(), model.comment, user);
						}

						// hide saveButton, show result columns and filter options
						limitForm.setVisible(false);
						filterForm.setChoices(ResultFilter.values());
						showColumnsForm.setVisible(true && !pModel.type.getAdditionalColumns()
								.isEmpty());
						showResultColumns(true);
						saveButton.setVisible(false);
						predictButton.setVisible(true);
						samplesetButton.setVisible(true);

						updateComponents(aTarget);
					}
				}.setOutputMarkupPlaceholderTag(true));

				add(predictButton = (ExtendedIndicatingAjaxButton) new ExtendedIndicatingAjaxButton(
						"predictButton", new Model<String>("Predict results"), new Model<String>(
								"Predicting ..."))
				{
					@Override
					protected void onSubmit(AjaxRequestTarget aTarget, Form<?> aForm)
					{
						aTarget.appendJavaScript("Wicket.Window.unloadConfirmation = false;");
						predictionModal.show(aTarget);
					}
				}.setOutputMarkupPlaceholderTag(true));

				add(samplesetButton = (ExtendedIndicatingAjaxButton) new ExtendedIndicatingAjaxButton(
						"samplesetButton", new Model<String>("Save results as sampleset"),
						new Model<String>("Saving..."))
				{
					@Override
					public void onSubmit(AjaxRequestTarget aTarget, Form<?> aForm)
					{
						samplesetModal.show(aTarget);
					}
				}.setOutputMarkupPlaceholderTag(true));
			}
		});
		add(limitForm = (LimitForm) new LimitForm("limit").setOutputMarkupPlaceholderTag(true));

		// at start, don't show: save button, results columns, filter
		limitForm.setVisible(false);
		filterForm.setChoices();
		showColumnsForm.setVisible(false);
		showResultColumns(false);
		saveButton.setVisible(false);
		predictButton.setVisible(false);
		samplesetButton.setVisible(false);
	}

	/**
	 * Set whether to show the result columns.
	 */
	private void showResultColumns(boolean doShow)
	{
		showResultColumns = doShow;
	}

	// TODO load from db instead of hardcoding here
	private List<Query> getExampleQueries(String aEngine, String aCollectionId, AnnotationType aType)
	{
		String user = SecurityContextHolder.getContext().getAuthentication().getName();
		List<Query> queries = new ArrayList<Query>();

		// add BNC examples
		if (aCollectionId.equals("BNC") && aEngine.equals("cqp")) {
			if (aType.getName().equals("It-cleft")) {
				queries.add(new Query("cqp", "\"It\" /VCC[] /PP[] /RC[]", "BNC", "It-cleft", user));
				queries.add(new Query("cqp", "\"It\" /VCC[] /NP[] /RC[]", "BNC", "It-cleft", user));
				queries.add(new Query("cqp", "\"It\" /VCC[] /RC[] \",\" /PP[]", "BNC", "It-cleft",
						user));
			}
			if (aType.getName().equals("There-cleft")) {
				queries.add(new Query("cqp", "[pos=\"EX0\"] /VCF[] /NP[] [pos=\"V.*\"]* /PP[]",
						"BNC", "There-cleft", user));
				queries.add(new Query("cqp", "[pos=\"EX0\"] /VCF[] /PP[] /NP[]", "BNC",
						"There-cleft", user));
			}
			if (aType.getName().equals("Wh-cleft")) {
				queries.add(new Query("cqp", "/RC[] /VCC[] /PP[]", "BNC", "Wh-cleft", user));
				queries.add(new Query("cqp", "/RC[] /VCC[] /NP[]", "BNC", "Wh-cleft", user));
			}
		}
		return queries;
	}

	private List<EvaluationResult> createEvaluationResults(List<EvaluationItem> aItems)
	{
		String user = SecurityContextHolder.getContext().getAuthentication().getName();
        List<EvaluationResult> results = new ArrayList<EvaluationResult>();
        for (EvaluationItem item : new HashSet<EvaluationItem>(aItems)) {
            results.add(new EvaluationResult(item, user, ""));
        }
        return results;
	}

	private void updateComponents(AjaxRequestTarget aTarget)
	{
		aTarget.add(getFeedbackPanel(), limitForm, filterForm, showColumnsForm, resultTable,
				saveButton, predictButton, samplesetButton);
	}

	/**
	 * Get additional result table columns for the specified type.
	 * 
	 * @param aType
	 *            the type to get additional columns for
	 * @return additional columns
	 */
	private List<IColumn<EvaluationResult>> getAllColumns(AnnotationType aType)
	{
		List<IColumn<EvaluationResult>> ac = new ArrayList<IColumn<EvaluationResult>>();

		// add "standard" columns
		ac.addAll(columns);

		// add type dependent columns
		for (final AdditionalColumn ad : aType.getAdditionalColumns()) {
			ac.add(new AbstractColumn<EvaluationResult>(new Model<String>(ad.getName()),
					"additionalColumnValue(" + ad.getId() + ")")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void populateItem(Item<ICellPopulator<EvaluationResult>> aCellItem,
						String aComponentId, final IModel<EvaluationResult> aRowModel)
				{
					aCellItem.add(new AjaxEditableLabel<String>(aComponentId,
							new LoadableDetachableModel<String>()
							{
								private static final long serialVersionUID = 1L;

								@Override
								protected String load()
								{
									EvaluationResult result = aRowModel.getObject();
									return result.getAdditionalColumns().get(ad);
								}
							})
					{
						private static final long serialVersionUID = 1L;

						@Override
						public void onSubmit(AjaxRequestTarget aTarget)
						{
							super.onSubmit(aTarget);

							EvaluationResult result = aRowModel.getObject();

							// get new value
							String newValue = getEditor().getInput();

							// update database
							if (newValue.isEmpty()) {
								result.getAdditionalColumns().remove(ad);
							}
							else {
								result.getAdditionalColumns().put(ad, newValue);
							}
							repository.updateEvaluationResult(result);
						}

						@Override
						public void onError(AjaxRequestTarget aTarget)
						{
							super.onError(aTarget);
							aTarget.add(getFeedbackPanel());
						}
					}.setType(String.class).add(
							new DbFieldMaxLengthValidator(projectRepository,
									"EvaluationResult_additionalColumns", "additionalColumns")));
				}

				@Override
				public String getCssClass()
				{
					List<String> classes = new ArrayList<String>();
					classes.add("editable");
					if (!showResultColumns || !ad.getShowColumn()) {
						classes.add("hideCol");
					}
					return StringUtils.join(classes, " ");
				}
			});
		}
		return ac;
	}
}
