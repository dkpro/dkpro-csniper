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

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createDescription;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.uimafit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.io.negra.NegraExportReader;
import de.tudarmstadt.ukp.dkpro.core.treetagger.TreeTaggerPosLemmaTT4J;

public class TuebaCorpusBuilder
{
	public static void main(String[] args) throws Exception
	{
		CollectionReaderDescription reader = createDescription(NegraExportReader.class, 
				NegraExportReader.PARAM_SOURCE_LOCATION, "/Users/bluefire/UKP/Library/Corpora/TueBaDZ/Version5/tuebadz-5.0.export.bz2",
				NegraExportReader.PARAM_LANGUAGE, "de",
				NegraExportReader.PARAM_ENCODING, "ISO-8859-15");

		AnalysisEngineDescription tt = createPrimitiveDescription(TreeTaggerPosLemmaTT4J.class,
				// POS tags are provided in the corpus, but lemmas are not
				TreeTaggerPosLemmaTT4J.PARAM_WRITE_POS, false,
				TreeTaggerPosLemmaTT4J.PARAM_WRITE_LEMMA, true);

		SimplePipeline.runPipeline(reader, tt, CSniperConsumerUtils.getConsumers("tuebadz5"));
	}
}
