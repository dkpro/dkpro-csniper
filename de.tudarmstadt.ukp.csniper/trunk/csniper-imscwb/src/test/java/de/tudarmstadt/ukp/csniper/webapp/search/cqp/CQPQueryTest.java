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
package de.tudarmstadt.ukp.csniper.webapp.search.cqp;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationItem;

// FIXME fix the tests
@Ignore("Does not run on Jenkins (yet)")
public class CQPQueryTest
{
	private CqpEngine engine;
	
	@Before
	public void setup()
	{
		engine = new CqpEngine();
		engine.setCqpExecutable(new File("/Users/bluefire/bin/cwb-3.2.0-snapshot-282/cqp"));
//		engine.setRegistryPath(new File("/Users/bluefire/UKP/Library/IMSCWB/loewe-ncc/registry"));
		engine.setMacrosLocation("classpath:/BNC_macros.txt");
	}
	
	@Test
	public void testBncOffsets()
		throws IOException
	{
		CqpQuery query = engine.createQuery("type", "BNC",
				"\"unattractive\" \"girls\"");
		query.setContext(1, 1, ContextUnit.SENTENCE);
		
		List<EvaluationItem> actual = query.execute();
		List<EvaluationItem> expected = new ArrayList<EvaluationItem>();
		
		expected.add(new EvaluationItem("BNC", "A05", "type", 155175, 155239,  
				"Because unattractive men do n't want unattractive girls , you see ."));
		expected.add(new EvaluationItem("BNC", "A05", "type", 155268, 155303,
				"They merely get unattractive girls ."));
		
		assertEquals(expected.size(), actual.size());

		for (int i = 0; i < actual.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
	}

	@Test
	public void testCorrect()
		throws IOException
	{
		CqpQuery query = engine.createQuery("type", "TUEBADZ5",
				"\"auf\" \"dem\" [pos=\"NN\"] [lemma=\"stehen\"]");
		query.setContext(1, 1, ContextUnit.SENTENCE);
		
		List<EvaluationItem> actual = query.execute();
		List<EvaluationItem> expected = new ArrayList<EvaluationItem>();
		
		expected.add(new EvaluationItem("TUEBADZ5", "T990430.148", "type", 1985, 2156,  
				"Gewohnt trocken hat Weiner zweisprachig den Satz \" What is set upon the table sits upon the table - was auf dem Tisch steht steht auf dem Tisch \" an die Wand geschrieben ."));
		expected.add(new EvaluationItem("TUEBADZ5", "T920713.79", "type", 588, 898,
				"Aber nun , da unter dem Titel \" Zukunft der Gegenwart - neues Bauen im historischen Kontext \" keine politische Geste , sondern höchst praktische Fragen von einiger Dringlichkeit auf dem Programm standen , herrschte betretenes Schweigen zwischen den Systemfronten , die es offiziell doch nicht mehr geben darf ."));
		expected.add(new EvaluationItem("TUEBADZ5", "T951014.95", "type", 3181, 3405,
				"Und obwohl weit und breit keiner ernsthaft glaubt , daß tatsächlich irgendeine Ehre auf dem Spiel steht , ist da ein Mensch , der sich bei jeder Gelegenheit bemüßigt fühlt zu betonen , daß er \" keine Ehre verlieren \" könne ."));
		
		assertEquals(expected.size(), query.size());
		assertEquals(expected.size(), actual.size());

		for (int i = 0; i < actual.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
		query.close();
	}
	
	@Test
	public void testLimitedResult()
		throws IOException
	{
		int expectedCount = 4;
		int customLimit = 2;
		
		CqpQuery query = engine.createQuery("type", "TUEBADZ5", "\"er\" \"war\"");
		query.setContext(1, 1, ContextUnit.SENTENCE);
		query.setMaxResults(customLimit);
		
		List<EvaluationItem> actual = query.execute();
		List<EvaluationItem> expected = new ArrayList<EvaluationItem>();
		
		expected.add(new EvaluationItem("TUEBADZ5", "T990506.255", "type", 5161, 5224,
				"Es war , als ob der Krieg zu Ende wäre , aber er war es nicht ."));
		expected.add(new EvaluationItem("TUEBADZ5", "T990430.292", "type", 928, 1070,
				"Zu dieser Zeit war Drakovic natürlich nicht in der Regierung , er war der bekannteste Andersdenkende und zugleich Miloevic' härtester Gegner ."));
		
		assertEquals(expectedCount, query.size());
		assertEquals(expected.size(), actual.size());
		
		for (int i = 0; i < actual.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
	}

	@Test(expected = InvalidDataAccessResourceUsageException.class)
	public void testError()
		throws IOException
	{
		// query error: missing closing "
		CqpQuery query = engine.createQuery("type", "TUEBADZ5", "\"Drachen");
		query.setContext(1, 1, ContextUnit.SENTENCE);
		query.execute();
	}
	
	@Test
	public void testVersion()
	{
		CqpQuery m = new CqpQuery(engine, "type", "TUEBADZ5");
		String expectedVersion = "3.4.1";
		String actualVersion = m.getVersion();
		assertEquals(expectedVersion, actualVersion);
		m.close();
	}
}
