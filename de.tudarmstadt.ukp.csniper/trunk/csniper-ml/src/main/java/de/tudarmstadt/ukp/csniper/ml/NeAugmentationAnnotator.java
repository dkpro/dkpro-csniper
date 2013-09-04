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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;

import de.tudarmstadt.ukp.csniper.webapp.search.tgrep.PennTreeNode;
import de.tudarmstadt.ukp.csniper.webapp.search.tgrep.PennTreeUtils;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;

public class NeAugmentationAnnotator
	extends JCasAnnotator_ImplBase
{
	private JCas jcas;

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		jcas = aJCas;
		for (PennTree pt : JCasUtil.select(aJCas, PennTree.class)) {
			String tree = StringUtils.normalizeSpace(pt.getPennTree());
			PennTreeNode ptn = PennTreeUtils.parsePennTree(tree);
			if (ptn != null) {
				int augmented = augment(ptn, JCasUtil.selectCovered(NamedEntity.class, pt));
				if (augmented > 0) {
					getLogger().info("ORIGINAL PENNTREE:  [" + tree + "]");
					tree = PennTreeUtils.toPennTree(ptn);
					pt.setPennTree(tree);
					getLogger().info("AUGMENTED PENNTREE: [" + tree + "]");
				}
			}
		}
	}

	private int augment(PennTreeNode aPennTreeNode, List<NamedEntity> sNes)
	{
		int count = 0;
		for (NamedEntity ne : sNes) {
			int tokenStartIndex = JCasUtil.selectCovered(jcas, Token.class, 0, ne.getBegin())
					.size();
			int coveredTokens = JCasUtil.selectCovered(Token.class, ne).size();
			int tokenEndIndex = tokenStartIndex + coveredTokens - 1;
			try {
				aPennTreeNode.insertIndexedNode(tokenStartIndex, tokenEndIndex, ne.getValue());
				count++;
			}
			catch (IllegalArgumentException e) {
				getLogger().warn(
						"Can't insert NE [" + ne + "] into PennTree ["
								+ PennTreeUtils.toPennTree(aPennTreeNode)
								+ "], because it crosses constituent borders.", e);
			}
		}
		return count;
	}
}
