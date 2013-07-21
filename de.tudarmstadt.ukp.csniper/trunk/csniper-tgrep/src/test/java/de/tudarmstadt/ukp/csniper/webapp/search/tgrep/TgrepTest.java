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
package de.tudarmstadt.ukp.csniper.webapp.search.tgrep;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.junit.Ignore;
import org.junit.Test;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.io.text.StringReader;
import de.tudarmstadt.ukp.dkpro.core.io.tgrep.TGrepWriter;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpParser;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;

//FIXME fix the tests
@Ignore("Does not run on Jenkins (yet)")
public class TgrepTest
{
//	private TgrepEngine engine;
//
//	@Before
//	public void setup()
//	{
//		engine = new TgrepEngine();
//		engine.setTgrepExecutable(new File("D:\\bin\\tgrep2.exe"));
//		engine.setTgrepExecutable(new File("/Users/bluefire/UKP/Workspaces/dkpro-juno/de.tudarmstadt.ukp.loewe.ncc/de.tudarmstadt.ukp.loewe.ncc.tgrep/src/main/resources/tgrep2/osx-x86_32/tgrep2"));
//
//		CorpusService service = new CorpusServiceImpl();
//		engine.setCorpusService(service);
//	}
//
//	@Test
//	public void testBncOffsets()
//		throws IOException
//	{
//		TgrepQuery query = engine.createQuery("type", "test", "NP << reason");
//
//		List<EvaluationItem> actual = query.execute();
//		query.close();
//
//		List<EvaluationItem> expected = new ArrayList<EvaluationItem>();
//		expected.add(new EvaluationItem("BNC", "A1A", -1, "type", 213401, 213555, "It is for this reason that deconstruction remains a fundamental threat to Marxism , and by implication to other culturalist and contextualizing approaches .", 1.0));
//
//		assertEquals(expected.size(), actual.size());
//		for (int i = 0; i < actual.size(); i++) {
//			assertEquals(expected.get(i), actual.get(i));
//		}
//	}

	@Test
	public void testWriter()
		throws IOException, UIMAException
	{
//		engine.setBasePath(new File("target"));
//		engine.setRelativePath("");

		CollectionReader cr = CollectionReaderFactory.createCollectionReader(StringReader.class,
				StringReader.PARAM_COLLECTION_ID, "BNC",
				StringReader.PARAM_DOCUMENT_ID, "A1A",
				StringReader.PARAM_DOCUMENT_TEXT, "It is for this reason that deconstruction remains a " +
						"fundamental threat to Marxism, and by implication to other culturalist " +
						"and contextualizing approaches.",
				StringReader.PARAM_LANGUAGE, "en");

		AnalysisEngine seg = AnalysisEngineFactory.createPrimitive(OpenNlpSegmenter.class);

		AnalysisEngine par = AnalysisEngineFactory.createPrimitive(OpenNlpParser.class,
				OpenNlpParser.PARAM_WRITE_PENN_TREE, true,
				OpenNlpParser.PARAM_LANGUAGE, "en");

		AnalysisEngine tcw = AnalysisEngineFactory.createPrimitive(TGrepWriter.class,
				TGrepWriter.PARAM_TARGET_LOCATION, "target/BNC",
				TGrepWriter.PARAM_WRITE_COMMENTS, true);

		SimplePipeline.runPipeline(cr, seg, par, tcw);

//		TgrepQuery query = engine.createQuery("type", "BNC", "NP << reason");
//
//		List<EvaluationItem> actual = query.execute();
//		query.close();
//
//		List<EvaluationItem> expected = new ArrayList<EvaluationItem>();
//		expected.add(new EvaluationItem("BNC", "A1A", -1, "type", 0, 154, "It is for this reason " +
//				"that deconstruction remains a fundamental threat to Marxism , and by " +
//				"implication to other culturalist and contextualizing approaches .", 1.0));
//
//		assertEquals(expected.size(), query.size());
//		assertEquals(expected.size(), actual.size());
//		for (int i = 0; i < actual.size(); i++) {
//			assertEquals(expected.get(i), actual.get(i));
//		}
	}

//	@Test
//	public void testLimitedResult()
//		throws IOException
//	{
//		int expectedCount = 8;
//		int customLimit = 5;
//
//		TgrepQuery query = engine.createQuery("type", "test", "NP << VP");
//		query.setMaxResults(customLimit);
//
//		List<EvaluationItem> actual = query.execute();
//		query.close();
//
//		List<EvaluationItem> expected = new ArrayList<EvaluationItem>();
//		expected.add(new EvaluationItem("BNC", "A13", -1, "type", 8916, 8973, 
//				"It is in the pub cellar that the beers reaches maturity .", 1.0));
//		expected.add(new EvaluationItem("BNC", "A1A", -1, "type", 213401, 213555, 
//				"It is for this reason that deconstruction remains a fundamental threat to " +
//				"Marxism , and by implication to other culturalist and contextualizing " +
//				"approaches .", 1.0));
//		expected.add(new EvaluationItem("BNC", "A5Y", -1, "type", 161995, 162224, 
//				"It is in the initial anticipation that the middle class will show respect that " +
//				"the bias lies , for where this deference is lacking , even from someone who " +
//				"appears middle class , a variant of the gouger typification comes into force .", 
//				1.0));
//		expected.add(new EvaluationItem("BNC", "A0Y", -1, "type", 50694, 50778, "For more " +
//				"information see HEATING COSTS , INCOME SUPPORT AND THE SOCIAL FUND  Page 2  .", 
//				1.0));
//		expected.add(new EvaluationItem("BNC", "A0X", -1, "type", 68107, 68275, "Amongst the " +
//				"alternatives are tungsten , diamond and ruby carvers offering a range of " +
//				"benefits from longer life to precise detailing , which is vital for wildfowl " +
//				"carving .", 1.0));		
//
//		assertEquals(expectedCount, query.size());
//		assertEquals(expected.size(), actual.size());
//		for (int i = 0; i < actual.size(); i++) {
//			assertEquals(expected.get(i), actual.get(i));
//		}
//	}
//
//	@Test(expected = DataAccessResourceFailureException.class)
//	public void testError()
//		throws IOException
//	{
//		// query error: unrecognized operator (typo)
//		TgrepQuery query = engine.createQuery("type", "test", "NP <y VP");
//		query.execute();
//	}
}
