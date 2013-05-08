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

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class Renamer
	extends JCasAnnotator_ImplBase
{
	public static final String PARAM_COLLECTION_ID = "CollectionId";
	@ConfigurationParameter(name = PARAM_COLLECTION_ID, mandatory = true)
	protected String collectionId;

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		DocumentMetaData meta;
		try {
			meta = DocumentMetaData.get(aJCas);
		}
		catch (IllegalArgumentException e) {
			// if there is no DocumentMetaData, create it
			meta = DocumentMetaData.create(aJCas);
		}
		if (meta != null) {
			meta.setCollectionId(collectionId);
		}
	}
}
