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
package de.tudarmstadt.ukp.csniper.resbuild.stuff;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

public class CasCounterProgressLogger
	extends JCasAnnotator_ImplBase
{
	public static final String PARAM_BRIEF_OUTPUT = "briefOutput";
	@ConfigurationParameter(name = PARAM_BRIEF_OUTPUT, mandatory = true, defaultValue = "false")
	private boolean briefOutput;

	private static int counter = 0;
	private static final double startTime = System.currentTimeMillis();

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
//		DocumentMetaData md = DocumentMetaData.get(aJCas);
//
//		if (briefOutput) {
//			System.out.printf("ID: %s \t Chars: %d %n", md.getDocumentId(), aJCas.getDocumentText()
//					.length());
//		}
//		else {
//			Collection<Token> tokens = select(aJCas, Token.class);
//			Collection<Sentence> sentences = select(aJCas, Sentence.class);
//			System.out.printf("ID: %s \t Chars: %d \t Tokens: %d \t Sentences: %d %n",
//					md.getDocumentId(), aJCas.getDocumentText().length(), tokens.size(),
//					sentences.size());
//		}
		counter++;

		if (counter == 100) {
			double stop = System.currentTimeMillis();
			double s = (stop - startTime) / 1000;
			System.out.println("--- First 100 CASes processed, time: " + s + "s");
		}
		
		if (counter % 1000 == 0) {
			double stop = System.currentTimeMillis();
			double s = (stop - startTime) / 1000;
			System.out.println("--- " + counter + " CASes processed, cumulated time: " + (int) s
					/ 60 + "min");
		}
	}
}
