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
package de.tudarmstadt.ukp.csniper.webapp.analysis.page;
//package de.tudarmstadt.ukp.csniper.webapp.analysis.page;
//
//import static java.util.Arrays.asList;
//import static org.apache.commons.lang.StringUtils.split;
//import static org.apache.commons.lang.StringUtils.stripAll;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.apache.wicket.ajax.AjaxRequestTarget;
//import org.apache.wicket.markup.html.WebPage;
//import org.apache.wicket.markup.html.basic.Label;
//import org.apache.wicket.markup.html.form.Button;
//import org.apache.wicket.markup.html.form.DropDownChoice;
//import org.apache.wicket.markup.html.form.Form;
//import org.apache.wicket.markup.html.form.TextArea;
//import org.apache.wicket.markup.html.form.TextField;
//import org.apache.wicket.markup.html.image.Image;
//import org.apache.wicket.markup.html.image.NonCachingImage;
//import org.apache.wicket.model.Model;
//import org.apache.wicket.model.PropertyModel;
//import org.apache.wicket.spring.injection.annot.SpringBean;
//
//import de.tudarmstadt.ukp.csniper.webapp.analysis.ExamplesRepository;
//import de.tudarmstadt.ukp.csniper.webapp.analysis.ParseTreeResource;
//import de.tudarmstadt.ukp.csniper.webapp.analysis.uima.ParsingPipeline;
//import de.tudarmstadt.ukp.csniper.webapp.page.ApplicationPageBase;
//import de.tudarmstadt.ukp.csniper.webapp.support.uima.CasHolder;
//import de.tudarmstadt.ukp.csniper.webapp.support.wicket.ExtendedIndicatingAjaxButton;
//
///**
// * Homepage
// */
//@SuppressWarnings({ "rawtypes", "unused" })
//public class AdvAnalysisPage
//	extends ApplicationPageBase
//{
//	private static final long serialVersionUID = 5526646708571776749L;
//	
//	private static final List<String> PARSER = asList(new String[] { "stanfordParser", "maltParser" });
//	private static final List<String> LANGUAGES = asList(new String[] { "de", "en" });
//
//	private final ParsingPipeline pp;
//	private final CasHolder parseOutputHolder = new CasHolder(null);
//
//	private String caption = "";
//	private String result = "";
//
//	private Label resultLabel;
//	private Label captionLabel;
//	
//	@SpringBean(name = "examplesRepository")
//	private ExamplesRepository examplesRepository;
//
//	// TextArea analysisArea;
//
//	private class InputForm
//		extends Form
//	{
//		private String input = "It was they who needed to be changed.\nIt has been my watch that was broken.\nIf he wants to be an actor it's because he wants to be famous.\nIt is my clock which has been repaired.\nWhat I really object to is violence on TV.\nAll he wanted to buy was a new car.";
//		private String parser = "stanfordParser";
//		private String language = "en";
//
//		private ParseTreeResource parseTree;
//		private String tool = "textmarker";
//		private String scriptSelect = "";
//		private String script = " ";
//		private String annotations = "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.NP";
//		private String colors = "blue";
//
//		@SuppressWarnings({ "unchecked", "serial" })
//		public InputForm(String id)
//		{
//			super(id);
//			
//			// Parse tree view
//			parseTree = new ParseTreeResource(parseOutputHolder);
//			
//			final Label currentParseLabel = new Label("currentParse", new PropertyModel(this, "parseTree.currentParse"));
//			currentParseLabel.setOutputMarkupId(true);
//			add(currentParseLabel);
//			
//			final Label parseCountLabel = new Label("parseCount", new PropertyModel(this, "parseTree.maxParse"));
//			parseCountLabel.setOutputMarkupId(true);
//			add(parseCountLabel);
//			
//			final Image treeImage = new NonCachingImage("treeImg", parseTree);
//			treeImage.setOutputMarkupId(true);
//			add(treeImage);
//			
//			add(new Button("previousParseButton")
//			{
//				@Override
//				public void onSubmit()
//				{
//					parseTree.setCurrentParse(parseTree.getCurrentParse() - 1);
//				}
//			});
//			
//			add(new Button("nextParseButton")
//			{
//				@Override
//				public void onSubmit()
//				{
//					parseTree.setCurrentParse(parseTree.getCurrentParse() + 1);
//				}
//			});
//			
//			// Input form view
//			final TextArea scriptArea = new TextArea("script", new PropertyModel(this, "script"));
//			add(scriptArea);
//			final DropDownChoice<String> scriptDropdown = new DropDownChoice<String>(
//					"scriptSelect", new PropertyModel(this, "scriptSelect"), new ArrayList<String>(
//							examplesRepository.getExamplesForTool(tool).keySet()))
//			{
//				@Override
//				protected boolean wantOnSelectionChangedNotifications()
//				{
//					return true;
//				}
//
//				@Override
//				protected void onSelectionChanged(String newSelection)
//				{
//					scriptSelect = newSelection;
//					scriptArea.setModelValue(new String[] { examplesRepository.getExample(tool,
//							newSelection) });
//				}
//			};
//			add(scriptDropdown);
//			add(new DropDownChoice("parser", new PropertyModel(this, "parser"), PARSER));
//			add(new DropDownChoice("language", new PropertyModel(this, "language"), LANGUAGES));
//			add(new DropDownChoice<String>("tool", new PropertyModel(this, "tool"),
//					examplesRepository.getTools())
//			{
//				@Override
//				protected boolean wantOnSelectionChangedNotifications()
//				{
//					return true;
//				}
//
//				@Override
//				protected void onSelectionChanged(String newSelection)
//				{
//					tool = newSelection;
//					info("Tool selection changed to " + tool);
//					scriptSelect = examplesRepository.getDefaultExampleForTool(tool);
//					scriptArea.setModelValue(new String[] { examplesRepository.getExample(tool,
//							scriptSelect) });
//					scriptDropdown.setChoices(new ArrayList<String>(examplesRepository
//							.getExamplesForTool(tool).keySet()));
//				}
//			});
//			TextArea inputText = new TextArea("inputText", new PropertyModel(this, "input"));
//			inputText.setConvertEmptyInputStringToNull(false);
//			add(inputText);
//
//			TextField annotationField = new TextField("annotations", new PropertyModel(this,
//					"annotations"));
//			annotationField.setConvertEmptyInputStringToNull(false);
//			add(annotationField);
//
//			TextField colorField = new TextField("colors", new PropertyModel(this, "colors"));
//			colorField.setConvertEmptyInputStringToNull(false);
//			add(colorField);
//
//			add(new ExtendedIndicatingAjaxButton("parseButton", new Model<String>("Parse"),
//					new Model<String>("Parsing ..."))
//			{
//				@Override
//				protected void onSubmit(AjaxRequestTarget aTarget, Form<?> aForm)
//				{
//					// pre-process input text, populate a casHolder with the resulting CAS
//					parseOutputHolder.setCas(pp.parseInput(parser, language, input));
//					parseTree.setCurrentParse(parseTree.getMaxParse() > 0 ? 1 : 0);
//					info("Parse complete");
//					
//					aTarget.add(currentParseLabel, parseCountLabel, treeImage);
//				}
//			});
//
//			add(new Button("saveButton")
//			{
//				@Override
//				public void onSubmit()
//				{
//					info("Save.onSubmit executed");
//					// TODO save script
//				}
//			});
//
//			add(new ExtendedIndicatingAjaxButton("processButton", new Model<String>("Process"),
//					new Model<String>("Processing ..."))
//			{
//				@Override
//				protected void onSubmit(AjaxRequestTarget aTarget, Form<?> aForm)
//				{
//					String[] markedAnnotations = stripAll(split(annotations, ","));
//					String[] markerColors = stripAll(split(colors, ","));
//
//					if (parseOutputHolder.getCas() == null) {
//						aTarget.appendJavaScript("alert('You have to parse a text before processing it.');");
//						return;
//					}
//
//					pp.runScript(parseOutputHolder.getCas(), tool, script, markedAnnotations,
//							markerColors);
//					// set feedback content
//					result = pp.getHTML();
//					// set caption content
//					caption = pp.getColorMap(markedAnnotations, markerColors);
//
//					aTarget.add(resultLabel, captionLabel);
//				}
//			});
//
//			
//		}
//
//		@Override
//		protected void onInitialize()
//		{
//			super.onInitialize();
//			// set a tool and a script on initialization
//			tool = examplesRepository.getTools().iterator().next();
//			scriptSelect = examplesRepository.getExamplesForTool(tool).keySet().iterator().next();
//			script = examplesRepository.getExample(tool, scriptSelect);
//		}
//	}
//
//	/**
//	 * Constructor that is invoked when page is invoked without a session.
//	 * 
//	 * @param parameters
//	 *            Page parameters
//	 */
//	public AdvAnalysisPage()
//	{
//		pp = new ParsingPipeline();
//
//		add(new InputForm("form"));
//
//		add(new Label("parseMsg2", "See the processed text below:"));
//
//		resultLabel = new Label("result", new PropertyModel(this, "result"));
//		resultLabel.setEscapeModelStrings(false);
//		resultLabel.setOutputMarkupId(true);
//		add(resultLabel);
//
//		captionLabel = new Label("caption", new PropertyModel(this, "caption"));
//		captionLabel.setEscapeModelStrings(false);
//		captionLabel.setOutputMarkupId(true);
//		add(captionLabel);
//	}
//}
