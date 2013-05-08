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

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListChoice;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import de.tudarmstadt.ukp.csniper.webapp.evaluation.EvaluationRepository;
import de.tudarmstadt.ukp.csniper.webapp.project.ProjectRepository;
import de.tudarmstadt.ukp.csniper.webapp.project.model.AnnotationType;
import de.tudarmstadt.ukp.csniper.webapp.project.model.Project;
import de.tudarmstadt.ukp.csniper.webapp.support.orm.EntityModel;

public class ProjectPage
	extends SettingsPageBase
{
	private static final long serialVersionUID = -2102136855109258306L;

	@SpringBean(name = "evaluationRepository")
	private EvaluationRepository annotationRepository;

	@SpringBean(name = "projectRepository")
	private ProjectRepository projectRepository;

	private class SelectionForm extends Form<SelectionModel>
	{
		private static final long serialVersionUID = -1L;
		
		public SelectionForm(String id)
		{
			super(id, new CompoundPropertyModel<SelectionModel>(new SelectionModel()));

			add(new Button("create", new ResourceModel("label")) {
				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit()
				{
					SelectionForm.this.getModelObject().project = null;
					detailForm.setModelObject(new Project());
					detailForm.setVisible(true);
					SelectionForm.this.setVisible(false);
				}
			});
			
			add(new ListChoice<Project>("project") {
				private static final long serialVersionUID = 1L;
				
				{
					setChoices(new LoadableDetachableModel<List<Project>>()
					{
						private static final long serialVersionUID = 1L;

						@Override
						protected List<Project> load()
						{
							return projectRepository.listProjects();	
						}
					});
					setChoiceRenderer(new ChoiceRenderer<Project>("name"));
					setNullValid(false);
				}
				
				@Override
				protected void onSelectionChanged(Project aNewSelection)
				{
					if (aNewSelection != null) {
						SelectionForm.this.getModelObject().project = new Project();
//						Project project = projectRepository.readProject(aNewSelection.getId());
						detailForm.setModelObject(aNewSelection);
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
	
	static private class SelectionModel implements Serializable
	{
		private static final long serialVersionUID = -1L;
		
		private Project project;
	}

	private class DetailForm extends Form<Project>
	{
		private static final long serialVersionUID = -1L;
		
		public DetailForm(String id)
		{
			super(id, new CompoundPropertyModel<Project>(new EntityModel<Project>(new Project())));

			add(new TextField<String>("name"));
			
			add(new ListMultipleChoice<String>("users", annotationRepository.listUsers()));

			add(new ListMultipleChoice<AnnotationType>("types",
					projectRepository.listAnnotationTypes(), new ChoiceRenderer<AnnotationType>(
							"name", "id")));

			add(new Button("save", new ResourceModel("label"))
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit()
				{
					Project project = DetailForm.this.getModelObject();
					projectRepository.writeProject(project);
					detailForm.setModelObject(new Project());
					detailForm.setVisible(false);
					selectionForm.setVisible(true);
				}
			});
		}
	}
	
	private SelectionForm selectionForm;
	private DetailForm detailForm;
	
	public ProjectPage()
	{
		selectionForm = new SelectionForm("selectionForm");
		
		detailForm = new DetailForm("detailForm");
		detailForm.setVisible(false);
		
		add(selectionForm);
		add(detailForm);
	}
}
