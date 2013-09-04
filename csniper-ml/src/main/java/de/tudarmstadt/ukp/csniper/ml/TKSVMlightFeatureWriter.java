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
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.apache.uima.fit.util.JCasUtil;

import de.tudarmstadt.ukp.csniper.ml.type.BooleanClassification;
import de.tudarmstadt.ukp.csniper.ml.type.ClearTkFeature;

public class TKSVMlightFeatureWriter
	extends CleartkAnnotator<Boolean>
{
	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		BooleanClassification bc = JCasUtil.selectSingle(aJCas, BooleanClassification.class);
		Instance<Boolean> instance = new Instance<Boolean>();

		for (ClearTkFeature ctkf : JCasUtil.select(aJCas, ClearTkFeature.class)) {
			instance.add(new Feature(ctkf.getClearTkFeatureName(), ctkf.getClearTkFeatureValue()));
//			System.out.println(ctkf.getClearTkFeatureName()+":["+ctkf.getClearTkFeatureValue()+"]");
		}
		instance.setOutcome(bc.getExpectedLabel());

		dataWriter.write(instance);
	}
}
