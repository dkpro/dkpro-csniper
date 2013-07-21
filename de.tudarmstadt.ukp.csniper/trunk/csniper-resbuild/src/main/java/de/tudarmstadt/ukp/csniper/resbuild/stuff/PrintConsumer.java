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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;

public class PrintConsumer
	extends JCasAnnotator_ImplBase
{
	private Set<String> a = new HashSet<String>();
	private Set<String> se = new HashSet<String>();

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		DocumentMetaData meta = DocumentMetaData.get(aJCas);
		if (meta != null) {
			System.out.println("DOCUMENT_META_DATA:");
			System.out.println("col_id = " + meta.getCollectionId());
			System.out.println("doc_base_uri = " + meta.getDocumentBaseUri());
			System.out.println("doc_id = " + meta.getDocumentId());
			System.out.println("doc_title = " + meta.getDocumentTitle());
			System.out.println("doc_uri = " + meta.getDocumentUri());
			System.out.println("\n");
		}
		// for (Candidate c : JCasUtil.select(aJCas, Candidate.class)) {
		// System.out.print(StringUtils.leftPad(Integer.toString(c.getBegin()), 6) + "-");
		// System.out.print(StringUtils.leftPad(Integer.toString(c.getEnd()), 6) + " ");
		// System.out.print(StringUtils.normalizeSpace(c.getCoveredText()));
		// System.out.println();
		// }
		// System.out.println("----");
		//
		// for (Sentence s : JCasUtil.select(aJCas, Sentence.class)) {
		// System.out.println(follow(s));
		// }
		// System.out.println("----");
		for (PennTree p : JCasUtil.select(aJCas, PennTree.class)) {
//			System.out.println(p.getPennTree());
			System.out.println(p.getCoveredText());
			System.out.println(StringUtils.normalizeSpace(p.getPennTree()) + "\n");
		}
		System.out.println("----");

		// System.out.println(aJCas.getDocumentText());
	}
}
