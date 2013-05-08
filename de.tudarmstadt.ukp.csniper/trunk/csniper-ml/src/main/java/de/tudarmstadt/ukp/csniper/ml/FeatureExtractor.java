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

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

/**
 * Convenience class for single-feature extractors.
 * 
 * @author Erik-Lân Do Dinh
 * 
 */
public abstract class FeatureExtractor
	extends FeatureExtractorMulti
{
	@Override
	protected Map<String, String> getMap(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		Map<String, String> map = new HashMap<String, String>();
		map.put(getName(), getFeature(aJCas));
		return map;
	}

	protected abstract String getFeature(JCas aJCas)
		throws AnalysisEngineProcessException;
}
