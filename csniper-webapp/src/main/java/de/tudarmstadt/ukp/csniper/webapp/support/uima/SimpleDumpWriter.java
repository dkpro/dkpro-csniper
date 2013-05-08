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
package de.tudarmstadt.ukp.csniper.webapp.support.uima;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.CasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;


/**
 * Writes CAS dump to a file for debugging purposes.
 */
public class SimpleDumpWriter extends CasAnnotator_ImplBase {

	public static final String PARAM_OUTPUT_FILE = "OutputFile";
	@ConfigurationParameter(name = PARAM_OUTPUT_FILE, mandatory = true)
	private File outputFile;
	
	private StringBuffer sb;
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		sb = new StringBuffer();
	}
	
	@Override
	public void process(CAS cas) throws AnalysisEngineProcessException {
		Iterator<CAS> viewIterator = cas.getViewIterator();
		while(viewIterator.hasNext()) {
			CAS view = viewIterator.next();
			AnnotationIndex<AnnotationFS> ai = view.getAnnotationIndex();
			FSIterator<AnnotationFS> fsi = ai.iterator();
			while(fsi.hasNext()) {
				AnnotationFS a = fsi.next();
				String text, addInfo;
				try {
					text = a.getCoveredText();
				} catch(Exception e) {
					text = "ERROR";
				}
				if(a instanceof Constituent) {
					addInfo = " {constituentType=" + ((Constituent)a).getConstituentType() + "}";
				} else if(a instanceof Lemma) {
					addInfo = " {lemma=" + ((Lemma)a).getValue() + "}";
				} else {
					addInfo = "";
				}
				sb.append(a.getType().getName() + "(" + a.getBegin() + ","
						+ a.getEnd() + ") [" + text + "] " + addInfo + "\n");
			}
		}
	}
	
	@Override
	public void collectionProcessComplete() {
		try {
			FileUtils.writeStringToFile(outputFile, sb.toString(), "UTF-8");
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
