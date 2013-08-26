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
package de.tudarmstadt.ukp.csniper.webapp.search.page;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.ContextRelativeResource;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.security.core.context.SecurityContextHolder;

import de.tudarmstadt.ukp.csniper.webapp.analysis.ParseTreeResource;
import de.tudarmstadt.ukp.csniper.webapp.analysis.uima.ParsingPipeline;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.EvaluationRepository;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.SortableEvaluationResultDataProvider;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.SortableEvaluationResultDataProvider.ResultFilter;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.CachedParse;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationItem;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationResult;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.page.ContextView;
import de.tudarmstadt.ukp.csniper.webapp.page.ApplicationPageBase;
import de.tudarmstadt.ukp.csniper.webapp.project.ProjectRepository;
import de.tudarmstadt.ukp.csniper.webapp.search.ContextProvider;
import de.tudarmstadt.ukp.csniper.webapp.search.CorpusService;
import de.tudarmstadt.ukp.csniper.webapp.search.PreparedQuery;
import de.tudarmstadt.ukp.csniper.webapp.search.SearchEngine;
import de.tudarmstadt.ukp.csniper.webapp.support.uima.CasHolder;
import de.tudarmstadt.ukp.csniper.webapp.support.wicket.AnalysisPanel;
import de.tudarmstadt.ukp.csniper.webapp.support.wicket.CustomDataTable;
import de.tudarmstadt.ukp.csniper.webapp.support.wicket.EmbeddableImage;
import de.tudarmstadt.ukp.csniper.webapp.support.wicket.ExtendedIndicatingAjaxButton;

/**
 * Evaluation Page
 */
@SuppressWarnings({ "rawtypes", "unused" })
public class SearchPage
	extends ApplicationPageBase
{
	private static final long serialVersionUID = 1L;

	private List<EvaluationItem> items;

	private QueryForm queryForm;
	private LimitForm limitForm;
	private WebMarkupContainer contextViewsContainer;
	private ListView<ContextView> contextViews;
	private List<IColumn<EvaluationResult>> columns;
	private Component resultTable;
	private ModalWindow analysisModal;
	private ParsingPipeline pp;
	private SortableEvaluationResultDataProvider dataProvider;
	private boolean contextAvailable = false;

	private static final int ROWS_PER_PAGE = 10;
	private static final int MAX_RESULTS = 1000;

	private static final int MIN_ITEMS_ASSESSED = 10;

	@SpringBean(name = "evaluationRepository")
	private EvaluationRepository repository;

	@SpringBean(name = "projectRepository")
	private ProjectRepository projectRepository;

	@SpringBean(name = "corpusService")
	private CorpusService corpusService;

	@SpringBean(name = "contextProvider")
	private ContextProvider contextProvider;

	private class QueryForm
		extends Form<QueryFormModel>
	{
		private static final long serialVersionUID = 1L;

		final TextArea<String> queryInput;
		final ExtendedIndicatingAjaxButton queryButton;
		final DropDownChoice<String> collectionIdInput;
		final DropDownChoice<SearchEngine> engineInput;

		@SuppressWarnings({ "serial" })
		public QueryForm(String id)
		{
			super(id, new CompoundPropertyModel<QueryFormModel>(new QueryFormModel()));

			// Tab contents
			queryInput = (TextArea<String>) new TextArea<String>("query").setRequired(true);

			// submit button
			queryButton = new ExtendedIndicatingAjaxButton("queryButton", new Model<String>(
					"Submit query"), new Model<String>("Running query ..."))
			{
				@Override
				public void onSubmit(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					QueryFormModel model = QueryForm.this.getModelObject();
					String user = SecurityContextHolder.getContext().getAuthentication().getName();

					PreparedQuery query = null;
					try {
						query = model.engine.createQuery("", model.collectionId, model.query);
						query.setMaxResults(MAX_RESULTS);
						items = query.execute();
						int resultCount = query.size();
						limitForm.setResultCount(resultCount);
						// new results: show limitForm if too many results, show saveButton
						limitForm.setVisible(resultCount > MAX_RESULTS);
					}
					catch (NonTransientDataAccessException e) {
						error("Error executing query " + model.query + ": " + e.getMessage());
						items = new ArrayList<EvaluationItem>();
						// error -> no results: hide limitForm, saveButton
						limitForm.setVisible(false);
					}
					finally {
						// IOUtils.closeQuietly(query);
					}
					// new items (or error), so show only item columns, no filters

					// update dataprovider
					dataProvider = new SortableEvaluationResultDataProvider(
							createEvaluationResults(items));
					dataProvider.setSort("item.documentId", SortOrder.ASCENDING);
					dataProvider.setFilter(ResultFilter.ALL);
					// then update the table
					resultTable = resultTable.replaceWith(new CustomDataTable<EvaluationResult>(
							"resultTable", columns, dataProvider, ROWS_PER_PAGE));
					contextAvailable = true;

					updateComponents(aTarget);
				}

				@Override
				public void onError(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					super.onError(aTarget, aForm);
					aTarget.add(getFeedbackPanel());
				}
			};
			add(queryInput);
			add(queryButton);

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
					engineInput.setModelObject(null);
					queryInput.setModelObject("");
				}
			};
			add(collectionIdInput);

			// engine dropdown
			engineInput = (DropDownChoice<SearchEngine>) new DropDownChoice<SearchEngine>("engine",
					new LoadableDetachableModel<List<SearchEngine>>()
					{
						@Override
						protected List<SearchEngine> load()
						{
							QueryFormModel model = QueryForm.this.getModelObject();
							if (model.collectionId != null) {
								return corpusService.listEngines(model.collectionId);
							}
							else {
								return new ArrayList<SearchEngine>();
							}
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
					queryInput.setModelObject("");
				}
			}.setRequired(true);
			add(engineInput);
		}
	}

	private static class QueryFormModel
		implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String query = "";
		private boolean randomize = false;
		private String collectionId = null;
		private SearchEngine engine = null;
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
					PreparedQuery query = null;

					try {
						query = model.engine.createQuery("", model.collectionId, model.query);
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

					// update dataprovider
					dataProvider = new SortableEvaluationResultDataProvider(
							createEvaluationResults(items));
					dataProvider.setSort("item.documentId", SortOrder.ASCENDING);
					dataProvider.setFilter(ResultFilter.ALL);
					// then update the table
					resultTable = resultTable.replaceWith(new CustomDataTable<EvaluationResult>(
							"resultTable", columns, dataProvider, ROWS_PER_PAGE));

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

	/**
	 * Constructor that is invoked when page is invoked without a session.
	 */
	@SuppressWarnings({ "serial" })
	public SearchPage()
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
							error("Unable to load context: " + e.getMessage());
							aTarget.add(getFeedbackPanel());
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
					String aComponentId, final IModel<EvaluationResult> model)
			{
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
		columns.add(new PropertyColumn<EvaluationResult>(new Model<String>("Doc"),
				"item.documentId", "item.documentId"));
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

		add(queryForm = new QueryForm("queryForm"));

		add(limitForm = (LimitForm) new LimitForm("limit").setOutputMarkupPlaceholderTag(true));

		add(resultTable = new Label("resultTable").setOutputMarkupId(true));

		add(analysisModal = new ModalWindow("analysisModal"));
		analysisModal.setTitle("Parse tree");
		analysisModal.setInitialWidth(65 * 16);
		analysisModal.setInitialHeight(65 * 9);
		// autosize does not work...
		// analysisModal.setAutoSize(true);

		// at start, don't show: save button, results columns, filter
		limitForm.setVisible(false);
	}

	private List<EvaluationResult> createEvaluationResults(List<EvaluationItem> aItems)
	{
		String user = SecurityContextHolder.getContext().getAuthentication().getName();
		Set<EvaluationResult> results = new HashSet<EvaluationResult>();
		for (EvaluationItem item : aItems) {
			results.add(new EvaluationResult(item, user, ""));
		}
		return new ArrayList<EvaluationResult>(results);
	}

	private void updateComponents(AjaxRequestTarget aTarget)
	{
		aTarget.add(getFeedbackPanel(), limitForm, resultTable);
	}
}
