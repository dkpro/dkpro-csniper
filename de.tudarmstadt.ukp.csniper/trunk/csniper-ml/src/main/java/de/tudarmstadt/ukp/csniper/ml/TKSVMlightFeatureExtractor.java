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

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.apache.uima.fit.util.JCasUtil;

import de.tudarmstadt.ukp.csniper.ml.type.BooleanClassification;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;

public class TKSVMlightFeatureExtractor
	extends CleartkAnnotator<Boolean>
{
	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		// create a new instance for each PennTree
		Collection<PennTree> trees = JCasUtil.select(aJCas, PennTree.class);
		for (PennTree t : trees) {
			Instance<Boolean> instance = new Instance<Boolean>();
			instance.add(new Feature("TK_tree", StringUtils.normalizeSpace(t.getPennTree())));
			
			BooleanClassification bc = JCasUtil.selectSingle(aJCas, BooleanClassification.class);
			instance.setOutcome(bc.getExpectedLabel());

			train(instance);
		}
		if (trees.size() == 0) {
			getLogger().warn("No PennTree found: " + aJCas.getDocumentText());
		}
		if (trees.size() > 1) {
			getLogger().warn("Too many [" + trees.size() + "] PennTrees found: " + aJCas.getDocumentText());
		}
	}

	private void train(Instance<Boolean> instance)
		throws CleartkProcessingException
	{
		dataWriter.write(instance);
	}
}
