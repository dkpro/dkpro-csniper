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

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.split;
import static org.apache.commons.lang.StringUtils.stripAll;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import de.tudarmstadt.ukp.csniper.webapp.analysis.ExamplesRepository;
import de.tudarmstadt.ukp.csniper.webapp.analysis.ParseTreeResource;
import de.tudarmstadt.ukp.csniper.webapp.analysis.uima.ParsingPipeline;
import de.tudarmstadt.ukp.csniper.webapp.page.ApplicationPageBase;
import de.tudarmstadt.ukp.csniper.webapp.support.uima.CasHolder;
import de.tudarmstadt.ukp.csniper.webapp.support.wicket.ExtendedIndicatingAjaxButton;

/**
 * Homepage
 */
@SuppressWarnings({ "rawtypes", "unused" })
public class AnalysisPage
	extends ApplicationPageBase
{
	private static final long serialVersionUID = 5526646708571776749L;
	
	private static final List<String> PARSER = asList(new String[] { "stanfordParser" });
	private static final List<String> LANGUAGES = asList(new String[] { "de", "en" });

	private InputForm form;
	private ParsingPipeline pp;
	private final CasHolder parseOutputHolder = new CasHolder(null);

	private class InputForm
		extends Form
	{
		private static final long serialVersionUID = 6502542275443597057L;
		
		private String input = "It was they who needed to be changed.\nIt has been my watch that was broken.\nIf he wants to be an actor it's because he wants to be famous.\nIt is my clock which has been repaired.\nWhat I really object to is violence on TV.\nAll he wanted to buy was a new car.";
		private String parser = "stanfordParser";
		private String language = "en";

		private ParseTreeResource parseTree;

		@SuppressWarnings({ "unchecked", "serial" })
		public InputForm(String id)
		{
			super(id);
			
			// Parse tree view
			parseTree = new ParseTreeResource(parseOutputHolder);
			
			final Label currentParseLabel = new Label("currentParse", new PropertyModel(this, "parseTree.currentParse"));
			currentParseLabel.setOutputMarkupId(true);
			add(currentParseLabel);
			
			final Label parseCountLabel = new Label("parseCount", new PropertyModel(this, "parseTree.maxParse"));
			parseCountLabel.setOutputMarkupId(true);
			add(parseCountLabel);
			
			final Image treeImage = new NonCachingImage("treeImg", parseTree);
			treeImage.setOutputMarkupId(true);
			add(treeImage);
			
			add(new Button("previousParseButton")
			{
				@Override
				public void onSubmit()
				{
					parseTree.setCurrentParse(parseTree.getCurrentParse() - 1);
				}
			});
			
			add(new Button("nextParseButton")
			{
				@Override
				public void onSubmit()
				{
					parseTree.setCurrentParse(parseTree.getCurrentParse() + 1);
				}
			});
			
			add(new DropDownChoice("parser", new PropertyModel(this, "parser"), PARSER));
			add(new DropDownChoice("language", new PropertyModel(this, "language"), LANGUAGES));
			TextArea inputText = new TextArea("inputText", new PropertyModel(this, "input"));
			inputText.setConvertEmptyInputStringToNull(false);
			add(inputText);

			add(new ExtendedIndicatingAjaxButton("parseButton", new Model<String>("Parse"),
					new Model<String>("Parsing ..."))
			{
				@Override
				protected void onSubmit(AjaxRequestTarget aTarget, Form<?> aForm)
				{
					doParse();
					aTarget.add(getFeedbackPanel());
					aTarget.add(currentParseLabel, parseCountLabel, treeImage);
				}
			});
		}
		
		public void doParse()
		{
			// pre-process input text, populate a casHolder with the resulting CAS
			parseOutputHolder.setCas(pp.parseInput(parser, language, input));
			parseTree.setCurrentParse(parseTree.getMaxParse() > 0 ? 1 : 0);
		}
	}

	public AnalysisPage()
	{
		super();
		commonInit();
	}

	public AnalysisPage(PageParameters aParams)
	{
		super(aParams);
		commonInit();
		if (getPageParameters().get("sentence") != null) {
			form.input = getPageParameters().get("sentence").toString();
			form.doParse();
		}
	}

	public AnalysisPage(String aSentence, String aLanguage)
	{
		super();
		commonInit();
		form.input = aSentence;
		form.language = aLanguage;
		form.doParse();
	}

	private void commonInit()
	{
		pp = new ParsingPipeline();
		form = new InputForm("form");
		add(form);
	}
}
