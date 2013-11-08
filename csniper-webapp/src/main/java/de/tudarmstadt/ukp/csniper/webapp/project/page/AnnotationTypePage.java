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
package de.tudarmstadt.ukp.csniper.webapp.project.page;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeAction;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListChoice;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.engine.BaseRenderEngine;
import org.radeox.engine.context.BaseRenderContext;
import org.springframework.security.core.context.SecurityContextHolder;

import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.AdditionalColumn;
import de.tudarmstadt.ukp.csniper.webapp.project.ProjectRepository;
import de.tudarmstadt.ukp.csniper.webapp.project.model.AnnotationType;
import de.tudarmstadt.ukp.csniper.webapp.support.orm.EntityModel;
import de.tudarmstadt.ukp.csniper.webapp.support.wicket.DbFieldMaxLengthValidator;
import de.tudarmstadt.ukp.csniper.webapp.support.wicket.RepeatingEditor;
import de.tudarmstadt.ukp.csniper.webapp.support.wicket.RepeatingItem;

@AuthorizeAction(action = Action.RENDER, roles = { "ROLE_ADMIN" })
public class AnnotationTypePage
	extends SettingsPageBase
{
	private static final long serialVersionUID = -2102136855109258306L;

	@SpringBean(name = "projectRepository")
	private ProjectRepository projectRepository;

	private class SelectionForm
		extends Form<SelectionModel>
	{
		private static final long serialVersionUID = -1L;

		public SelectionForm(String id)
		{
			super(id, new CompoundPropertyModel<SelectionModel>(new SelectionModel()));

			add(new Button("create", new ResourceModel("label"))
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit()
				{
					SelectionForm.this.getModelObject().type = null;
					detailForm.setModelObject(new AnnotationType());
					detailForm.setVisible(true);
					SelectionForm.this.setVisible(false);
				}
			});

			add(new ListChoice<AnnotationType>("type")
			{
				private static final long serialVersionUID = 1L;

				{
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
					setNullValid(false);
				}

				@Override
				protected void onSelectionChanged(AnnotationType aNewSelection)
				{
					if (aNewSelection != null) {
						SelectionForm.this.getModelObject().type = null;
						AnnotationType type = projectRepository.readAnnotationType(aNewSelection
								.getId());
						detailForm.setModelObject(type);

						detailForm.setVisible(true);
						SelectionForm.this.setVisible(false);
					}
				}

				@Override
				protected boolean wantOnSelectionChangedNotifications()
				{
					return true;
				}

				@Override
				protected CharSequence getDefaultChoice(String aSelectedValue)
				{
					return "";
				}
			});
		}
	}

	static private class SelectionModel
		implements Serializable
	{
		private static final long serialVersionUID = -1L;

		private AnnotationType type;
	}

	private class DetailForm
		extends Form<AnnotationType>
	{
		private static final long serialVersionUID = -1L;

		public DetailForm(String id)
		{
			super(id, new CompoundPropertyModel<AnnotationType>(new EntityModel<AnnotationType>(
					new AnnotationType())));

			setOutputMarkupId(true);

			add(new TextField<String>("name").add(new DbFieldMaxLengthValidator(projectRepository,
					"AnnotationType", "name")));

			add(new NumberTextField<Integer>("goal"));

			add(new NumberTextField<Integer>("goalWrong"));

			add(new TextArea<String>("description").add(
					new DbFieldMaxLengthValidator(projectRepository, "AnnotationType",
							"description")).setOutputMarkupPlaceholderTag(true));

			add(new RepeatingEditor<AdditionalColumn>("additionalColumns")
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onPopulateItem(final RepeatingItem<AdditionalColumn> aItem)
				{
					aItem.add(new TextField<String>("name", new PropertyModel<String>(aItem
							.getModelObject(), "name")).setConvertEmptyInputStringToNull(false)
							.add(new DbFieldMaxLengthValidator(projectRepository,
									"AdditionalColumn", "name")));

					aItem.add(new CheckBox("showColumn", new PropertyModel<Boolean>(aItem
							.getModelObject(), "showColumn")));

					aItem.add(new AjaxButton("removeButton", new ResourceModel("label"))
					{
						private static final long serialVersionUID = 1L;

						@Override
						protected void onSubmit(AjaxRequestTarget aTarget, Form<?> aForm)
						{
							int idx = aItem.getIndex();
							AdditionalColumn currCol = aItem.getModelObject();
							String user = SecurityContextHolder.getContext().getAuthentication()
									.getName();

							int entryCount = projectRepository.countEntriesWithAdditionalColumn(
									user, detailForm.getModelObject(), currCol);

							if (entryCount > 0) {
								// TODO popup dialog, on YES delete all associated entries
								getFeedbackPanel().error(
										"Currently it is not possible to remove columns for which entries exist. ["
												+ entryCount + "]");
								aTarget.add(getFeedbackPanel());
							}
							else {
								// TODO do this also in case of YES on popup dialog
								@SuppressWarnings("unchecked")
								RepeatingEditor<AdditionalColumn> re = (RepeatingEditor<AdditionalColumn>) DetailForm.this
										.get("additionalColumns");
								// TODO grey out instead of removing the text field
								re.getModelObject().remove(idx);
								aTarget.add(detailForm);
							}
						}

						@Override
						protected void onError(AjaxRequestTarget aTarget, Form<?> aForm)
						{
		                    super.onError(aTarget, aForm);
		                    // Make sure the feedback messages are rendered
		                    aTarget.add(getFeedbackPanel());
						}
					});
				}
			}.setOutputMarkupPlaceholderTag(true));

			add(new AjaxButton("addColumnButton", new ResourceModel("label"))
			{
				private static final long serialVersionUID = 1L;

				@SuppressWarnings("unchecked")
				@Override
				protected void onSubmit(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					((RepeatingEditor<AdditionalColumn>) detailForm.get("additionalColumns"))
							.addItem(new AdditionalColumn("", false));
					// can't ajax update a repeater component, so update the whole form
					aTarget.add(detailForm);
				}

				@Override
				protected void onError(AjaxRequestTarget aTarget, Form<?> aForm)
				{
                    super.onError(aTarget, aForm);
                    // Make sure the feedback messages are rendered
                    aTarget.add(getFeedbackPanel());
				}
			});

			Label previewPane;
			add(previewPane = new Label("previewPane", new LoadableDetachableModel<String>()
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected String load()
				{
					String value = detailForm.getModelObject().getDescription();

					if (value != null) {
						RenderContext context = new BaseRenderContext();
						RenderEngine engine = new BaseRenderEngine();
						return engine.render(value, context);
					}
					else {
						return "";
					}
				}
			}));
			previewPane.setOutputMarkupPlaceholderTag(true).setVisible(false);
			previewPane.setEscapeModelStrings(false);

			add(new AjaxButton("previewButton", new ResourceModel("previewLabel"))
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					Component preview = detailForm.get("previewPane");
					toggleVisibility(!preview.isVisible());
					aTarget.add(this, preview.getParent());
				}

				@Override
				protected void onError(AjaxRequestTarget aTarget, Form<?> aForm)
				{
                    super.onError(aTarget, aForm);
                    // Make sure the feedback messages are rendered
                    aTarget.add(getFeedbackPanel());
				}
			}).setOutputMarkupId(true);

			add(new Button("save", new ResourceModel("label"))
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit()
				{
					// set visibility to default
					toggleVisibility(false);

					projectRepository.writeAnnotationType(detailForm.getModelObject());
					detailForm.setModelObject(new AnnotationType());
					detailForm.setVisible(false);
					selectionForm.setVisible(true);
				}
			});

			add(new Button("cancel", new ResourceModel("label"))
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit()
				{
					// set visibility to default
					toggleVisibility(false);

					projectRepository.refreshEntity(detailForm.getModelObject());
					detailForm.setModelObject(new AnnotationType());
					detailForm.setVisible(false);
					selectionForm.setVisible(true);
				}
			});
		}

		/**
		 * Toggles visibility of description input and preview pane. Also changes preview button
		 * text to match function.
		 * 
		 * @param showPreview
		 *            whether to show the preview (and thus not show the input)
		 */
		private void toggleVisibility(boolean showPreview)
		{
			Component textarea = detailForm.get("description");
			Component preview = detailForm.get("previewPane");
			Component button = detailForm.get("previewButton");

			String hideClass = "hideCol";
			String cssClasses = String.valueOf(textarea.getMarkupAttributes().get("class"));

			// show preview, hide input, set button label to input
			if (showPreview) {
				button.setDefaultModel(new ResourceModel("inputLabel"));
				preview.setVisible(true);
				cssClasses = cssClasses.equals("null") ? "" : cssClasses;
				cssClasses = cssClasses + " " + hideClass;
			}
			// hide preview, show input, set button label to preview
			else {
				button.setDefaultModel(new ResourceModel("previewLabel"));
				preview.setVisible(false);
				cssClasses = cssClasses.replaceFirst(hideClass, "").trim();
			}
			// use css display:none instead of wicket setVisible(), because otherwise the model
			// reloads the AnnotationType object from the database
			textarea.add(new AttributeModifier("class", cssClasses));
		}
	}

	private SelectionForm selectionForm;
	private DetailForm detailForm;

	public AnnotationTypePage()
	{
		selectionForm = new SelectionForm("selectionForm");

		detailForm = new DetailForm("detailForm");
		detailForm.setVisible(false);

		add(selectionForm);
		add(detailForm);
	}
}
