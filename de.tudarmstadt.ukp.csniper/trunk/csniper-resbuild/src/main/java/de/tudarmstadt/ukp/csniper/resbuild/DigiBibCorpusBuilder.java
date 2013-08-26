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
package de.tudarmstadt.ukp.csniper.resbuild;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import de.tudarmstadt.ukp.dkpro.core.treetagger.TreeTaggerPosLemmaTT4J;

public class DigiBibCorpusBuilder
{
	public static void main(String[] args) throws Exception
	{
		CollectionReaderDescription reader = createReaderDescription(TeiReader.class, 
				TeiReader.PARAM_SOURCE_LOCATION, 
				"jar:file:/Users/bluefire/UKP/Library/Corpora/Digitale-Bibliothek-Literatur-nur-Texte.zip!",
				TeiReader.PARAM_PATTERNS, "[+]**/*.xml",
				TeiReader.PARAM_USE_FILENAME_ID, true,
				TeiReader.PARAM_LANGUAGE, "de");
		
		AnalysisEngineDescription tok = createEngineDescription(StanfordSegmenter.class);
		
		AnalysisEngineDescription tt = createEngineDescription(TreeTaggerPosLemmaTT4J.class,
				// POS tags are provided in the corpus, but lemmas are not
				TreeTaggerPosLemmaTT4J.PARAM_WRITE_POS, true,
				TreeTaggerPosLemmaTT4J.PARAM_WRITE_LEMMA, true);

		SimplePipeline.runPipeline(reader, tok, tt, CSniperConsumerUtils.getConsumers("digibib"));
	}
}
