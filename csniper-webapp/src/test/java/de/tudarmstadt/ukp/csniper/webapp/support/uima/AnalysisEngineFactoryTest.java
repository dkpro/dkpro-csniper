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
package de.tudarmstadt.ukp.csniper.webapp.support.uima;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.uimafit.factory.JCasFactory;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.csniper.webapp.support.uima.AnalysisEngineFactory;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/applicationContext.xml")
public class AnalysisEngineFactoryTest
{
	@Test
	public void parserLoadTest()
		throws UIMAException, ClassNotFoundException
	{
		String language = "en";
		String inputText = "Just a short sentence.";

		JCas jcas = JCasFactory.createJCas();
		jcas.setDocumentText(inputText);
		jcas.setDocumentLanguage(language);

		AnalysisEngine tokenizer = createPrimitive(OpenNlpSegmenter.class,
				OpenNlpSegmenter.PARAM_LANGUAGE, language);
		AnalysisEngine parser = AnalysisEngineFactory
				.createAnalysisEngine(AnalysisEngineFactory.PARSER, "language", language);
		tokenizer.process(jcas);
		parser.process(jcas);

		String[] constituentMapped = new String[] { "ADVP 0,4", "NP 5,21", "ROOT 0,22", "S 0,22" };
		String[] constituentOriginal = constituentMapped;
		AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
				JCasUtil.select(jcas, Constituent.class));
	}
}
