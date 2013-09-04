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

import java.util.Map;
import java.util.Map.Entry;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.csniper.ml.type.ClearTkFeature;

/**
 * This serves as a base class for feature extractor annotators. Extending classes have to implement
 * {@code getMap()}, which should provide the names and corresponding values of the features which
 * are being extracted.
 * 
 * @author Erik-Lân Do Dinh
 * 
 */
public abstract class FeatureExtractorMulti
	extends JCasAnnotator_ImplBase
{
	public static final String FEATURE_EXTRACTOR_NAME = "featureExtractorName";
	@ConfigurationParameter(name = FEATURE_EXTRACTOR_NAME, mandatory = false)
	private String featureExtractorName = getName();

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);
	}

	@Override
	public final void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		for (Entry<String, String> e : getMap(aJCas).entrySet()) {
			if (e.getValue() != null) {
				ClearTkFeature ctkf = new ClearTkFeature(aJCas);
				ctkf.setBegin(0);
				ctkf.setEnd(aJCas.getDocumentText().length() - 1);
				ctkf.setClearTkFeatureName(e.getKey());
				ctkf.setClearTkFeatureValue(e.getValue());
				aJCas.addFsToIndexes(ctkf);
				// System.out.println("Adding feature: " + e.getKey() + "[" + e.getValue() + "] - "
				// + aJCas.getDocumentText());
			}
		}
	}

	protected abstract Map<String, String> getMap(JCas aJCas)
		throws AnalysisEngineProcessException;

	protected abstract String getName();

	@Override
	public String toString()
	{
		return getName();
	}
}
