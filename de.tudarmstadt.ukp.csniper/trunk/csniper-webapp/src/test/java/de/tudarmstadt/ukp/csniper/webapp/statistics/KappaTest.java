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
package de.tudarmstadt.ukp.csniper.webapp.statistics;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.tudarmstadt.ukp.csniper.webapp.evaluation.EvaluationRepository;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationItem;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationResult;
import de.tudarmstadt.ukp.csniper.webapp.statistics.SortableAggregatedEvaluationResultDataProvider;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/databaseContext.xml")
public class KappaTest
{
	private final static String CORRECT = "Correct";
	private final static String WRONG = "Wrong";
	private final static String EMPTY = "";
	
	private String collectionId = "testCollection";
	private String type = "testType";

	@Resource(name = "evaluationRepository")
	private EvaluationRepository service;

	SortableAggregatedEvaluationResultDataProvider dataProvider;

	// TODO if we need kappa, repair the test first before uncommenting
	@Ignore("Test fails in Maven build - in Eclipse it works...")
	@Test
	public void testKappa()
	{
//		setupResults();
//		dataProvider = new SortableAggregatedEvaluationResultDataProvider(service, collectionId,
//				type);
//		dataProvider.setFilters(ResultFilter.valuesAsSet());
//
//		double[] expecteds = new double[6];
//		expecteds[0] = 0;	// user1,user2: this is 0 because pe = pa (user1 rated all as correct)
//		expecteds[1] = 0;	// user1,user3: this is 0 because pe = pa (user1 rated all as correct)
//		expecteds[2] = 0;	// user1,user4: this is 0 because pe = pa (user1 rated all as correct)
//		expecteds[3] = -0.8;// user2,user3: negative because agreement is even less than by chance
//		expecteds[4] = -0.5;// user2,user4: negative because agreement is even less than by chance
//		expecteds[5] = 0.4;	// user3,user4
//		
//		double[] actuals = dataProvider.getKappaStatistics();
//
//		assertArrayEquals(expecteds, actuals, 0.01);
	}

	private void setupResults()
	{
		List<EvaluationItem> dummyItems = new ArrayList<EvaluationItem>();
		List<EvaluationResult> results = new ArrayList<EvaluationResult>();

		dummyItems.add(new EvaluationItem(collectionId, "Doc1", type, 0, 1, "text1"));
		dummyItems.add(new EvaluationItem(collectionId, "Doc2", type, 0, 1, "text2"));
		dummyItems.add(new EvaluationItem(collectionId, "Doc3", type, 0, 1, "text3"));
		dummyItems.add(new EvaluationItem(collectionId, "Doc4", type, 0, 1, "text4"));

		// user1
		results.add(new EvaluationResult(dummyItems.get(0), "user1", CORRECT));
		// user4 empty for item1, so this should not count
		results.add(new EvaluationResult(dummyItems.get(1), "user1", CORRECT));
		results.add(new EvaluationResult(dummyItems.get(2), "user1", CORRECT));
		results.add(new EvaluationResult(dummyItems.get(3), "user1", CORRECT));

		// user2
		results.add(new EvaluationResult(dummyItems.get(0), "user2", CORRECT));
		// user4 empty for item1, so this should not count
		results.add(new EvaluationResult(dummyItems.get(1), "user2", CORRECT));
		results.add(new EvaluationResult(dummyItems.get(2), "user2", WRONG));
		results.add(new EvaluationResult(dummyItems.get(3), "user2", WRONG));

		// user3
		results.add(new EvaluationResult(dummyItems.get(0), "user3", WRONG));
		// user4 empty for item1, so this should not count
		results.add(new EvaluationResult(dummyItems.get(1), "user3", CORRECT));
		results.add(new EvaluationResult(dummyItems.get(2), "user3", CORRECT));
		results.add(new EvaluationResult(dummyItems.get(3), "user3", CORRECT));

		// user4
		results.add(new EvaluationResult(dummyItems.get(0), "user4", WRONG));
		results.add(new EvaluationResult(dummyItems.get(1), "user4", EMPTY));
		results.add(new EvaluationResult(dummyItems.get(2), "user4", CORRECT));
		results.add(new EvaluationResult(dummyItems.get(3), "user4", WRONG));

		dummyItems = service.writeEvaluationItems(dummyItems);
		service.writeEvaluationResults(results);
	}
}
