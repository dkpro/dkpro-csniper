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

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.wicket.spring.injection.annot.SpringBean;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.tudarmstadt.ukp.csniper.webapp.evaluation.EvaluationRepository;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationItem;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationResult;
import de.tudarmstadt.ukp.csniper.webapp.project.model.AnnotationType;
import de.tudarmstadt.ukp.csniper.webapp.statistics.SortableAggregatedEvaluationResultDataProvider.ResultFilter;
import de.tudarmstadt.ukp.csniper.webapp.statistics.model.AggregatedEvaluationResult;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/databaseContextTest.xml")
public class ListAggregatedTest
{
	private final static String CORRECT = "Correct";
	private final static String WRONG = "Wrong";
	private final static String EMPTY = "";

	@Resource(name = "evaluationRepository")
	@SpringBean(name = "evaluationRepository")
	private EvaluationRepository service;

	private SortableAggregatedEvaluationResultDataProvider dataProvider;
	private List<String> collectionIds;
	private List<AnnotationType> types;
	private List<String> users;
	private double userThreshold = 0;
	private double confidenceThreshold = 0;
	/**
	 * C, W, D, I, U
	 */
	private int[] expectedResults;
	/**
	 * userRatio, confidence
	 */
	private List<double[]> expectedRatios;

	private boolean started;

	@Ignore("Ignore until we find a way to test the mysql stuff on jenkins.")
	@Test
	public void tests()
	{
		setupResults();

		collectionIds = asList("c1");
		types = asList(new AnnotationType("t3"));
		users = asList("user1");
		expectedResults = new int[] { 1, 0, 0, 0, 0 };
		expectedRatios = Arrays.asList(new double[] { 1, 1 });
		testIndividualResults();
		testRatios();

		collectionIds = asList("c1");
		types = asList(new AnnotationType("t1"), new AnnotationType("t3"));
		users = asList("user1", "user3");
		expectedResults = new int[] { 1, 0, 1, 0, 0 };
		expectedRatios = Arrays.asList(new double[] { 1, 0 }, new double[] { 1, 1 });
		testIndividualResults();
		testRatios();

		collectionIds = asList("c2", "c3");
		types = asList(new AnnotationType("t3"));
		users = asList("user1", "user2");
		expectedResults = new int[] { 0, 0, 2, 0, 0 };
		expectedRatios = Arrays.asList(new double[] { 1, 0 }, new double[] { 1, 0 });
		testIndividualResults();
		testRatios();

		collectionIds = asList("c1");
		types = asList(new AnnotationType("t1"), new AnnotationType("t3"));
		users = asList("user3", "user4");
		expectedResults = new int[] { 1, 1, 0, 0, 0 };
		expectedRatios = Arrays.asList(new double[] { 1, 1 }, new double[] { 0.5, 1 });
		testIndividualResults();
		testRatios();

		collectionIds = asList("c1", "c2", "c3");
		types = asList(new AnnotationType("t1"), new AnnotationType("t3"));
		users = asList("user1", "user2", "user3", "user4");
		expectedResults = new int[] { 2, 0, 2, 0, 0 };
		expectedRatios = Arrays.asList(new double[] { 1, 0 }, new double[] { 0.75, 1 },
				new double[] { 1, 0.666 }, new double[] { 1, 0 });
		testIndividualResults();
		testRatios();
	}

	/**
	 * Test the classification of each {@link AggregatedEvaluationResult} which is returned.
	 */
	public void testIndividualResults()
	{
		List<AggregatedEvaluationResult> agg = service.listAggregatedResults(collectionIds, types,
				users, userThreshold, confidenceThreshold);
		dataProvider = new SortableAggregatedEvaluationResultDataProvider(agg, users);

		Map<ResultFilter, Integer> actuals = dataProvider.getClassifications();
		Map<ResultFilter, Integer> expected = new HashMap<ResultFilter, Integer>();
		expected.put(ResultFilter.CORRECT, expectedResults[0]);
		expected.put(ResultFilter.WRONG, expectedResults[1]);
		expected.put(ResultFilter.DISPUTED, expectedResults[2]);
		expected.put(ResultFilter.INCOMPLETE, expectedResults[3]);
		expected.put(ResultFilter.UNKNOWN, expectedResults[4]);

		assertEquals(expected.size(), actuals.size());
		for (ResultFilter filter : ResultFilter.values()) {
			assertEquals(expected.get(filter), actuals.get(filter));
		}
	}

	/**
	 * Test user ratio and confidence of the {@link AggregatedEvaluationResult}s in question.
	 */
	public void testRatios()
	{
		List<AggregatedEvaluationResult> agg = service.listAggregatedResults(collectionIds, types,
				users, userThreshold, confidenceThreshold);
		Iterator<double[]> it = expectedRatios.iterator();

		for (AggregatedEvaluationResult aer : agg) {
			double[] ratios = it.next();
			assertEquals(ratios[0], aer.getUserRatio(), 0.001);
			assertEquals(ratios[1], aer.getConfidence(), 0.001);
		}
	}

	public void setupResults()
	{
		if (started) {
			return;
		}
		else {
			started = true;
		}

		List<EvaluationItem> items = new ArrayList<EvaluationItem>();
		List<EvaluationResult> results = new ArrayList<EvaluationResult>();

		items.add(new EvaluationItem("c1", "Doc1", "t1", 0, 1, "text1"));
		results.add(new EvaluationResult(items.get(0), "user1", CORRECT));
		results.add(new EvaluationResult(items.get(0), "user2", CORRECT));
		results.add(new EvaluationResult(items.get(0), "user3", WRONG));
		results.add(new EvaluationResult(items.get(0), "user4", WRONG));

		items.add(new EvaluationItem("c1", "Doc2", "t3", 0, 1, "text2"));
		results.add(new EvaluationResult(items.get(1), "user1", CORRECT));
		results.add(new EvaluationResult(items.get(1), "user2", CORRECT));
		results.add(new EvaluationResult(items.get(1), "user3", CORRECT));
		results.add(new EvaluationResult(items.get(1), "user4", EMPTY));

		items.add(new EvaluationItem("c2", "Doc3", "t3", 0, 1, "text3"));
		results.add(new EvaluationResult(items.get(2), "user1", CORRECT));
		results.add(new EvaluationResult(items.get(2), "user2", WRONG));
		results.add(new EvaluationResult(items.get(2), "user3", CORRECT));
		results.add(new EvaluationResult(items.get(2), "user4", CORRECT));

		items.add(new EvaluationItem("c3", "Doc4", "t3", 0, 1, "text4"));
		results.add(new EvaluationResult(items.get(3), "user1", CORRECT));
		results.add(new EvaluationResult(items.get(3), "user2", WRONG));
		results.add(new EvaluationResult(items.get(3), "user3", CORRECT));
		results.add(new EvaluationResult(items.get(3), "user4", WRONG));

		items = service.writeEvaluationItems(items);
		service.writeEvaluationResults(results);
	}
}
