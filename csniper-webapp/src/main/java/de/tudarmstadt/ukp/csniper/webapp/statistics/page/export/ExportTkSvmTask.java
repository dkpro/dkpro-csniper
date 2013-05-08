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
package de.tudarmstadt.ukp.csniper.webapp.statistics.page.export;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import com.google.common.io.Files;

import de.tudarmstadt.ukp.csniper.webapp.evaluation.EvaluationRepository;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.MlPipeline;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationResult;
import de.tudarmstadt.ukp.csniper.webapp.project.model.AnnotationType;
import de.tudarmstadt.ukp.csniper.webapp.statistics.model.AggregatedEvaluationResult;
import de.tudarmstadt.ukp.csniper.webapp.statistics.page.StatisticsPage.ExportModel;
import de.tudarmstadt.ukp.csniper.webapp.statistics.page.StatisticsPage.StatisticsFormModel;
import de.tudarmstadt.ukp.csniper.webapp.support.task.Task;

public class ExportTkSvmTask
	extends Task implements ExportTask
{
	EvaluationRepository repository;
	
	private StatisticsFormModel formModel;
	private ExportModel exportModel;

	private String language;
	
	private File outputFile;

	public ExportTkSvmTask(StatisticsFormModel aFormModel, ExportModel aExportModel,
			EvaluationRepository aRepository, String aLanguage)
	{
		formModel = aFormModel;
		exportModel = aExportModel;
		
		repository = aRepository;
		language = aLanguage;
	}

	@Override
	public File getOutputFile()
	{
		return new File(outputFile, "training-data.svmlight");
	}
	
	@Override
	public void clean()
	{
		if (outputFile != null) {
			FileUtils.deleteQuietly(outputFile);
			outputFile = null;
		}
	}
	
	@Override
	public String getFilename()
	{
		String innerSeparator = "-";
		String outerSeparator = "_";
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		StringBuilder sb = new StringBuilder();
		sb.append("csniper");
		sb.append(outerSeparator);
		sb.append(dateFormat.format(new Date()));
		sb.append(outerSeparator);
		sb.append(StringUtils.join(formModel.getCollections(), innerSeparator));
		sb.append(outerSeparator);
		for (Iterator<AnnotationType> it = formModel.getTypes().iterator(); it.hasNext();) {
			sb.append(it.next().getName());
			if (it.hasNext()) {
				sb.append(innerSeparator);
			}
		}
		sb.append(outerSeparator);
		sb.append(StringUtils.join(formModel.getUsers(), innerSeparator));
		sb.append(".svmlight");

		return sb.toString();
	}
	@Override
	protected void run()
	{
		outputFile = null;
		try {
			outputFile = Files.createTempDir();

			List<AggregatedEvaluationResult> aggResults = repository.listAggregatedResults(
					formModel.getCollections(), formModel.getTypes(), formModel.getUsers(),
					formModel.getUserThreshold(), formModel.getConfidenceThreshold());

			List<EvaluationResult> results = MlPipeline.convertToSimple(aggResults);
			
			setTotal(results.size());
			
			MlPipeline mlp = new MlPipeline(language);
			mlp.setTask(this);
			mlp.setRepostitory(repository);
			mlp.createTrainingData(outputFile, results);
		}
		catch (Exception e) {
			e.printStackTrace();
			error("Export failed: " + ExceptionUtils.getRootCauseMessage(e));
			cancel();
		}
		finally {
			if (isCancelled()) {
				clean();
			}
		}
	}
}
