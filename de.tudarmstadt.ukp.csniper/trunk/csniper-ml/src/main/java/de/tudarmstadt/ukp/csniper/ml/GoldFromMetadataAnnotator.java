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
package de.tudarmstadt.ukp.csniper.ml;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;

import de.tudarmstadt.ukp.csniper.ml.type.BooleanClassification;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

/**
 * Extracts the gold value from the document title and saves it in a new BooleanClassification
 * annotation.
 * 
 * @author Erik-Lân Do Dinh
 * 
 */
public class GoldFromMetadataAnnotator
	extends JCasAnnotator_ImplBase
{
	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		BooleanClassification bc = new BooleanClassification(aJCas);
		try {
			bc.setExpectedLabel(getGold(aJCas));
		}
		catch(IllegalArgumentException e) {
			// don't set gold value if it isn't available; create the annotation nonetheless
			// actually, scratch that - why would we want to have disputed items?
			throw new AnalysisEngineProcessException(e);
		}
		bc.setBegin(0);
		bc.setEnd(aJCas.getDocumentText().length());
		bc.addToIndexes();
	}

	/**
	 * Extracts the gold value from the document title.
	 */
	private boolean getGold(JCas aJCas)
	{
		String gold = DocumentMetaData.get(aJCas).getDocumentTitle().toLowerCase();
		if (gold.equals("correct")) {
			return true;
		}
		else if (gold.equals("wrong")) {
			return false;
		}
		else {
			throw new IllegalArgumentException(
					"Document title should be 'correct' or 'wrong', but is '" + gold + "'.");
		}
	}
}
