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

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;

/**
 * Adds a sentence annotation which treats the whole CAS as one sentence.
 * 
 * @author Erik-Lân Do Dinh
 * 
 */
public class DummySentenceSplitter
	extends JCasAnnotator_ImplBase
{
	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		Sentence s = new Sentence(aJCas, 0, aJCas.getDocumentText().length());
		s.addToIndexes();
	}

}
