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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createCollectionReader;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.junit.Test;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;

public class StanfordParserTest
{
//	private String input = "Alternatively, does your employee have a Leaver's statement (SSP1[L ]); from a previous employer?";
	private String input = "Circular LAC(91)112 was issued in August .";

	@Test
	public void pcfg()
		throws UIMAException, IOException
	{
		System.out.println("PCFG");
		CollectionReader cr = createCollectionReader(StringReader.class, StringReader.PARAM_INPUT,
				input, StringReader.PARAM_LANGUAGE, "en");

		AnalysisEngineDescription tok = createPrimitiveDescription(StanfordSegmenter.class);

		AnalysisEngineDescription sp = createPrimitiveDescription(StanfordParser.class,
				StanfordParser.PARAM_WRITE_PENN_TREE, true, StanfordParser.PARAM_LANGUAGE,
				"en", StanfordParser.PARAM_VARIANT, "pcfg");

		AnalysisEngineDescription pc = createPrimitiveDescription(PrintConsumer.class);

		SimplePipeline.runPipeline(cr, tok, sp, pc);
	}

	@Test
	public void factored()
		throws UIMAException, IOException
	{
		System.out.println("FACTORED");
		CollectionReader cr = createCollectionReader(StringReader.class, StringReader.PARAM_INPUT,
				input, StringReader.PARAM_LANGUAGE, "en");

		AnalysisEngineDescription tok = createPrimitiveDescription(StanfordSegmenter.class);

		AnalysisEngineDescription sp = createPrimitiveDescription(StanfordParser.class,
				StanfordParser.PARAM_WRITE_PENN_TREE, true, StanfordParser.PARAM_LANGUAGE,
				"en", StanfordParser.PARAM_VARIANT, "factored");

		AnalysisEngineDescription pc = createPrimitiveDescription(PrintConsumer.class);

		SimplePipeline.runPipeline(cr, tok, sp, pc);
	}
}
