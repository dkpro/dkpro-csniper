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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Simple UIMA reader for strings.
 * 
 * @author Erik-Lân Do Dinh
 */
public class CasFilter
	extends JCasAnnotator_ImplBase
{
	private List<String> docIds;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);
	}

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		for (Token t : JCasUtil.select(aJCas, Token.class)) {
			String text = t.getCoveredText();
			if (t.equals("‘") || t.equals("’")) {
				docIds.add(DocumentMetaData.get(aJCas).getDocumentId() + ".xml");
				break;
			}
		}
	}

	@Override
	public void collectionProcessComplete()
		throws AnalysisEngineProcessException
	{
		try {
			FileUtils.writeLines(new File("target/inclusions.txt"), "UTF-8", docIds);
		}
		catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}
}
