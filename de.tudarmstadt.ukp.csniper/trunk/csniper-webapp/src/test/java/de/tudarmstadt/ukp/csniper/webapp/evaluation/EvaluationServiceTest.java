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
package de.tudarmstadt.ukp.csniper.webapp.evaluation;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.tudarmstadt.ukp.csniper.webapp.evaluation.EvaluationRepository;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.SortableEvaluationResultDataProvider.ResultFilter;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationItem;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationResult;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.Mark;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.Query;
import de.tudarmstadt.ukp.csniper.webapp.statistics.SortableAggregatedEvaluationResultDataProvider;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/databaseContext.xml")
public class EvaluationServiceTest
{
	private final Log log = LogFactory.getLog(getClass());

	@Resource(name = "evaluationRepository")
	private EvaluationRepository evaluationService;

	private SortableAggregatedEvaluationResultDataProvider dataProvider;
	
	@Test
	public void test()
	{
		String collectionId = "BNC";
		long position = 1234;
		String type = "Cleft";
		String user1 = "Bert";
		String user2 = "Earnie";

		Query query = new Query("cqp", "myQuery", collectionId, type, user1);
		evaluationService.writeQuery(query);

		List<EvaluationItem> itemsIn = new ArrayList<EvaluationItem>();
		itemsIn.add(new EvaluationItem(collectionId, "", type, position, position, "la le la"));
		itemsIn = evaluationService.writeEvaluationItems(itemsIn);

		List<EvaluationItem> itemsOut = evaluationService.listEvaluationItems(collectionId, type);
		for (EvaluationItem i : itemsOut) {
			log.info(i);
		}
		Assert.assertEquals(itemsIn, itemsOut);

		List<Query> queries = evaluationService.listQueries();
		for (Query q : queries) {
			log.info(q);
		}
		Assert.assertEquals(1, queries.size());
		Assert.assertEquals(query, queries.get(0));

		List<EvaluationResult> resultsIn = new ArrayList<EvaluationResult>();
		resultsIn.add(new EvaluationResult(itemsOut.get(0), user1, Mark.CORRECT.getTitle()));
		evaluationService.writeEvaluationResults(resultsIn);

		List<EvaluationResult> resultsOut = evaluationService.listEvaluationResults(user1, type, 0,
				1, ResultFilter.ALL);
		log.info("Results found:" + resultsOut.size());
		for (EvaluationResult r : resultsOut) {
			log.info(r);
		}
		Assert.assertEquals(resultsIn, resultsOut);

		resultsIn.add(new EvaluationResult(itemsOut.get(0), user2, Mark.WRONG.getTitle()));
		evaluationService.writeEvaluationResults(resultsIn);

		System.out.println(itemsOut);
		System.out.println(resultsOut);
		
//		dataProvider = new SortableAggregatedEvaluationResultDataProvider(evaluationService.listAggregatedResults(itemsOut, Arrays.asList(user1), 0, 0.0), Arrays.asList(user1));
//		log.info("ItemStatistics: " + dataProvider.getItemStatistics());
//		dataProvider = new SortableAggregatedEvaluationResultDataProvider(evaluationService.listAggregatedResults(itemsOut, Arrays.asList(user1), 0, 0.5), Arrays.asList(user1));
//		log.info("ItemStatistics: " + dataProvider.getItemStatistics());
//		dataProvider = new SortableAggregatedEvaluationResultDataProvider(evaluationService.listAggregatedResults(itemsOut, Arrays.asList(user1), 0, 1.0), Arrays.asList(user1));
//		log.info("ItemStatistics: " + dataProvider.getItemStatistics());
		
	}
}
