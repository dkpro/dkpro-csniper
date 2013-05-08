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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import au.com.bytecode.opencsv.CSVWriter;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.EvaluationRepository;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.AdditionalColumn;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.ItemContext;
import de.tudarmstadt.ukp.csniper.webapp.project.model.AnnotationType;
import de.tudarmstadt.ukp.csniper.webapp.search.ContextProvider;
import de.tudarmstadt.ukp.csniper.webapp.statistics.SortableAggregatedEvaluationResultDataProvider.ResultFilter;
import de.tudarmstadt.ukp.csniper.webapp.statistics.model.AggregatedEvaluationResult;
import de.tudarmstadt.ukp.csniper.webapp.statistics.page.StatisticsPage.ExportModel;
import de.tudarmstadt.ukp.csniper.webapp.statistics.page.StatisticsPage.StatisticsFormModel;
import de.tudarmstadt.ukp.csniper.webapp.support.task.Task;

public class ExportCsvTask
	extends Task
	implements ExportTask
{
	private static final int COLUMN_COUNT = 13;

	private EvaluationRepository repository;
	private ContextProvider contextProvider;

	private StatisticsFormModel formModel;
	private ExportModel exportModel;

	private File outputFile;

	public ExportCsvTask(StatisticsFormModel aFormModel, ExportModel aExportModel,
			EvaluationRepository aRepository, ContextProvider aContextProvider)
	{
		formModel = aFormModel;
		exportModel = aExportModel;
		contextProvider = aContextProvider;
		repository = aRepository;
	}

	@Override
	public File getOutputFile()
	{
		return outputFile;
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
		sb.append(".csv");

		return sb.toString();
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
	protected void run()
	{
		CSVWriter writer = null;

		contextProvider.setOutputPos(exportModel.includePos);

		outputFile = null;
		try {
			outputFile = File.createTempFile("date", ".csv");

			writer = new CSVWriter(
					new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));

			List<AdditionalColumn> ac = exportModel.additionalColumns;
			String[] row = new String[COLUMN_COUNT + ac.size()];

			List<AggregatedEvaluationResult> results = repository.listAggregatedResults(
					formModel.getCollections(), formModel.getTypes(), formModel.getUsers(),
					formModel.getUserThreshold(), formModel.getConfidenceThreshold());

			Collections.sort(results, new Comparator<AggregatedEvaluationResult>()
			{
				@Override
				public int compare(AggregatedEvaluationResult aO1, AggregatedEvaluationResult aO2)
				{
					String id1 = aO1.getItem().getCollectionId() + "|"
							+ aO1.getItem().getDocumentId();
					String id2 = aO2.getItem().getCollectionId() + "|"
							+ aO2.getItem().getDocumentId();
					return id1.compareTo(id2);
				}
			});

			// Write header row
			row[0] = "User";
			row[1] = "Collection";
			row[2] = "Document";
			row[3] = "Begin";
			row[4] = "End";
			row[5] = "Left";
			row[6] = "Unit";
			row[7] = "Right";
			row[8] = "Type";
			row[9] = "Class";
			row[10] = "Confidence";
			row[11] = "Correct";
			row[12] = "Wrong";
			for (int i = 0; i < ac.size(); i++) {
				row[COLUMN_COUNT + i] = ac.get(i).getName();
			}
			writer.writeNext(row);

			// Write rest
			setTotal(results.size());
			for (AggregatedEvaluationResult aer : results) {
				ResultFilter classification = aer.getClassification();
				if (formModel.getFilters().contains(classification)) {
					ItemContext context = contextProvider.getContext(aer.getItem(),
							exportModel.contextSize, exportModel.contextSize);

					// only differentiate between users if additional columns are being exported
					Set<String> users;
					if (ac.isEmpty()) {
						users = new HashSet<String>(Arrays.asList(""));
					}
					else {
						users = aer.getUsers(false);
					}

					// output the AggregatedEvaluationResult for every user (because the additional
					// columns entries might differ)
					for (String user : users) {
						row[0] = user;
						row[1] = aer.getItem().getCollectionId();
						row[2] = aer.getItem().getDocumentId();
						row[3] = Long.toString(aer.getItem().getBeginOffset());
						row[4] = Long.toString(aer.getItem().getEndOffset());
						row[5] = context.getLeft();
						row[6] = context.getUnit();
						row[7] = context.getRight();
						row[8] = aer.getItem().getType();
						row[9] = classification.toString();
						row[10] = Double.toString(aer.getConfidence());
						row[11] = Integer.toString(aer.getCorrect());
						row[12] = Integer.toString(aer.getWrong());
						for (int i = 0; i < ac.size(); i++) {
							row[COLUMN_COUNT + i] = repository
									.getEvaluationResult(aer.getItem().getId(), user)
									.getAdditionalColumns().get(ac.get(i));
							if (row[COLUMN_COUNT + i] == null) {
								row[COLUMN_COUNT + i] = "";
							}
						}
						writer.writeNext(row);
					}
				}

				// Make sure we do not get to 100% before we did the classification, because
				// otherwise ProgressBar.onFinish() will trigger!!!
				increment();
				if (isCancelled()) {
					break;
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			error("Export failed: " + ExceptionUtils.getRootCauseMessage(e));
			cancel();
		}
		finally {
			IOUtils.closeQuietly(writer);
			if (isCancelled()) {
				clean();
			}
		}
	}
}
