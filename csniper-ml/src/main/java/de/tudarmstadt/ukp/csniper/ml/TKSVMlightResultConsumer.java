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
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.csniper.ml.type.BooleanClassification;

public class TKSVMlightResultConsumer
	extends JCasAnnotator_ImplBase
{
	private static double tp = 0;
	private static double tn = 0;
	private static double fp = 0;
	private static double fn = 0;

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		BooleanClassification bc = JCasUtil.selectSingle(aJCas, BooleanClassification.class);
		boolean gold = bc.getExpectedLabel();
		boolean predicted = bc.getPredictedLabel();

		if (gold == true) {
			if (predicted == true) {
				tp++;
			}
			else {
				fn++;
			}
		}
		else {
			if (predicted == true) {
				fp++;
			}
			else {
				tn++;
			}
		}
	}

	@Override
	public void collectionProcessComplete()
		throws AnalysisEngineProcessException
	{
		super.collectionProcessComplete();
		double accuracy = (tp + tn) / (tp + tn + fp + fn);
		double precision = tp / (tp + fp);
		double recall = tp / (tp + fn);
		double fmeasure = 2 * precision * recall / (precision + recall);

		System.out.println("\nTest results ------");
		System.out.println("Total instances classified: " + (int) (tp + tn + fp + fn));
		System.out.println("TP[" + (int) tp + "] FP[" + (int) fp + "] FN[" + (int) fn + "] TN["
				+ (int) tn + "]");
		System.out.println("Accuracy:  " + accuracy);
		System.out.println("Precision: " + precision);
		System.out.println("Recall:    " + recall);
		System.out.println("F-Measure: " + fmeasure);
	}
}
