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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.tudarmstadt.ukp.csniper.webapp.evaluation.EvaluationRepository;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationItem;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationResult;
import de.tudarmstadt.ukp.csniper.webapp.project.model.AnnotationType;
import de.tudarmstadt.ukp.csniper.webapp.statistics.SortableAggregatedEvaluationResultDataProvider;

/**
 * The expected results were obtained using<br>
 * http://en.wikipedia.org/wiki/Fleiss%27_kappa#Equations<br>
 * http://justusrandolph.net/kappa/
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/databaseContext.xml")
public class FleissKappaTest
{
	private final static String CORRECT = "Correct";
	private final static String WRONG = "Wrong";
	private final static String EMPTY = "";

	private static boolean setup = false;

	@Resource(name = "evaluationRepository")
	private EvaluationRepository service;

	private SortableAggregatedEvaluationResultDataProvider dataProvider;

	@Ignore
	@Test
	public void testOneUser()
	{
		// nothing to compare - only one user
		test("c1", "type2", "user1", Double.NaN);
	}

	@Ignore
	@Test
	public void testTwoUsers()
	{
		// agreement above chance
		// vector is (0-2, 1-1, 2-0) [Correct-Wrong]
		test("c1,c2", "type1,type2", "user3,user4", 0.333);
	}

	@Ignore
	@Test
	public void testThreeUsers()
	{
		// disagreement below chance
		// vector is (2-1, 1-2) [Correct-Wrong]
		test("c2,c3", "type2", "user1,user2,user4", -0.333);
	}

	@Ignore
	@Test
	public void testFourUsers()
	{
		// disagreement below chance
		// vector is (2-2, 3-1, 3-1, 2-2) [Correct-Wrong]
		test("c1,c2,c3", "type1,type2", "user1,user2,user3,user4", -0.244);
	}

	@Ignore
	@Test
	public void testDismissItem()
	{
		// this test should not consider the first item since it is not rated by user5
		// agreement above chance
		// vector is (1-0, 2-0, 0-2, 1-1) [Correct-Wrong]
		test("c1,c2,c3", "type1,type2", "user2,user5", 0.333);
	}

	@Ignore
	@Test
	public void testCompleteDisagreement()
	{
		// vector is (1-1, 1-1) [Correct-Wrong]
		test("c1,c2,c3", "type2", "user4,user5", -1);
	}

	@Ignore
	@Test
	public void testCompleteAgreementOneSided()
	{
		// this is NaN because of a division by zero in Fleiss' Kappa Formula;
		// for these ratings we have P^_e = 1
		// vector is (2-0, 2-0) [Correct-Wrong]
		test("c1", "type1,type2", "user1,user2", Double.NaN);
	}

	@Ignore
	@Test
	public void testCompleteAgreementTwoSided()
	{
		// vector is (2-0, 0-2) [Correct-Wrong]
		test("c1,c2", "type2", "user2,user5", 1);
	}

	private void test(String aCollectionIds, String aTypes, String aUsers, double aExpected)
	{
		List<String> collectionIds = asList(aCollectionIds.split(","));
		List<AnnotationType> types = new ArrayList<AnnotationType>();
		for (String t : asList(aTypes.split(","))) {
			types.add(new AnnotationType(t));
		}
		List<String> users = asList(aUsers.split(","));
		double userThreshold = 0.6;
		double confidenceThreshold = 0;
		
		dataProvider = new SortableAggregatedEvaluationResultDataProvider(
				service.listAggregatedResults(collectionIds, types, users, userThreshold, 
						confidenceThreshold), users);

		double actual = dataProvider.computeInterAnnotatorAgreement();

		assertEquals(aExpected, actual, 0.001);
	}

	@Before
	public void setupResults()
	{
		// run setup only once;
		// can't use @BeforeClass, because Spring cannot inject into static fields
		if (setup) {
			return;
		}
		else {
			setup = true;
		}

		List<EvaluationItem> items = new ArrayList<EvaluationItem>();
		List<EvaluationResult> results = new ArrayList<EvaluationResult>();

		items.add(new EvaluationItem("c1", "Doc1", "type1", 0, 1, "textype1"));
		results.add(new EvaluationResult(items.get(0), "user1", CORRECT));
		results.add(new EvaluationResult(items.get(0), "user2", CORRECT));
		results.add(new EvaluationResult(items.get(0), "user3", WRONG));
		results.add(new EvaluationResult(items.get(0), "user4", WRONG));
		results.add(new EvaluationResult(items.get(0), "user5", EMPTY));

		items.add(new EvaluationItem("c1", "Doc2", "type2", 0, 1, "text2"));
		results.add(new EvaluationResult(items.get(1), "user1", CORRECT));
		results.add(new EvaluationResult(items.get(1), "user2", CORRECT));
		results.add(new EvaluationResult(items.get(1), "user3", CORRECT));
		results.add(new EvaluationResult(items.get(1), "user4", WRONG));
		results.add(new EvaluationResult(items.get(1), "user5", CORRECT));

		items.add(new EvaluationItem("c2", "Doc3", "type2", 0, 1, "textype2"));
		results.add(new EvaluationResult(items.get(2), "user1", CORRECT));
		results.add(new EvaluationResult(items.get(2), "user2", WRONG));
		results.add(new EvaluationResult(items.get(2), "user3", CORRECT));
		results.add(new EvaluationResult(items.get(2), "user4", CORRECT));
		results.add(new EvaluationResult(items.get(2), "user5", WRONG));

		items.add(new EvaluationItem("c3", "Doc4", "type2", 0, 1, "text4"));
		results.add(new EvaluationResult(items.get(3), "user1", CORRECT));
		results.add(new EvaluationResult(items.get(3), "user2", WRONG));
		results.add(new EvaluationResult(items.get(3), "user3", CORRECT));
		results.add(new EvaluationResult(items.get(3), "user4", WRONG));
		results.add(new EvaluationResult(items.get(3), "user5", CORRECT));

		items = service.writeEvaluationItems(items);
		service.writeEvaluationResults(results);
	}

	private List<String> asList(String... aStrings)
	{
		return new ArrayList<String>(Arrays.asList(aStrings));
	}
}
