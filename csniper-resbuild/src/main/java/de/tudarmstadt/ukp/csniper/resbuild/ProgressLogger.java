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
package de.tudarmstadt.ukp.csniper.resbuild;

import static org.uimafit.util.JCasUtil.select;

import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class ProgressLogger
	extends JCasAnnotator_ImplBase
{
	public static final String PARAM_BRIEF_OUTPUT = "briefOutput";
	@ConfigurationParameter(name = PARAM_BRIEF_OUTPUT, mandatory = true, defaultValue = "false")
	private boolean briefOutput;
	
	@Override
	public void process(JCas aCAS)
		throws AnalysisEngineProcessException
	{
		DocumentMetaData md = DocumentMetaData.get(aCAS);
		
		if (briefOutput) {
			System.out.printf("ID: %s \t Chars: %d %n", md.getDocumentId(), aCAS
					.getDocumentText().length());
		}
		else {
			Collection<Token> tokens = select(aCAS, Token.class);
			Collection<Sentence> sentences = select(aCAS, Sentence.class);
			System.out.printf("ID: %s \t Chars: %d \t Tokens: %d \t Sentences: %d %n", md.getDocumentId(), aCAS
					.getDocumentText().length(), tokens.size(), sentences.size());
		}
	}
}
