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
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

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

public class ExportExcelTask
	extends Task
	implements ExportTask
{
	private EvaluationRepository repository;
	private ContextProvider contextProvider;

	private StatisticsFormModel formModel;
	private ExportModel exportModel;

	private File outputFile;

	public ExportExcelTask(StatisticsFormModel aFormModel, ExportModel aExportModel,
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
		sb.append(".xls");

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
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("Summary");

		PrintSetup printSetup = sheet.getPrintSetup();
		printSetup.setLandscape(true);
		sheet.setFitToPage(true);
		sheet.setHorizontallyCenter(true);

		contextProvider.setOutputPos(exportModel.includePos);

		outputFile = null;
		OutputStream os = null;
		try {
			List<AggregatedEvaluationResult> results = repository.listAggregatedResults(
					formModel.getCollections(), formModel.getTypes(), formModel.getUsers(),
					formModel.getUserThreshold(), formModel.getConfidenceThreshold());

			List<AdditionalColumn> ac = exportModel.additionalColumns;

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
			List<String> colIds = new ArrayList<String>(Arrays.asList("User", "Collection",
					"Document", "Begin", "End", "Left", "Unit", "Right", "Type", "Class",
					"Confidence", "Correct", "Wrong"));
			for (int i = 0; i < ac.size(); i++) {
				colIds.add(ac.get(i).getName());
			}
			Row headerRow = sheet.createRow(0);
			for (int i = 0; i < colIds.size(); i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(colIds.get(i));
			}

			// Write rest
			setTotal(results.size());
			int rowNum = 1;
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
						Row row = sheet.createRow(rowNum);
						row.createCell(0).setCellValue(user);
						row.createCell(1).setCellValue(aer.getItem().getCollectionId());
						row.createCell(2).setCellValue(aer.getItem().getDocumentId());
						row.createCell(3).setCellValue(aer.getItem().getBeginOffset());
						row.createCell(4).setCellValue(aer.getItem().getEndOffset());
						row.createCell(5).setCellValue(context.getLeft());
						row.createCell(6).setCellValue(context.getUnit());
						row.createCell(7).setCellValue(context.getRight());
						row.createCell(8).setCellValue(aer.getItem().getType());
						row.createCell(9).setCellValue(classification.toString());
						row.createCell(10).setCellValue(aer.getConfidence());
						row.createCell(11).setCellValue(aer.getCorrect());
						row.createCell(12).setCellValue(aer.getWrong());
						for (int i = 0; i < ac.size(); i++) {
							String cellValue = repository
									.getEvaluationResult(aer.getItem().getId(), user)
									.getAdditionalColumns().get(ac.get(i));
							if (cellValue == null) {
								cellValue = "";
							}
							row.createCell(colIds.size() - ac.size() + i).setCellValue(cellValue);

						}
						rowNum++;
					}
				}

				// Make sure we do not get to 100% before we did the classification, because
				// otherwise ProgressBar.onFinish() will trigger!!!
				increment();
				if (isCancelled()) {
					break;
				}
			}

			outputFile = File.createTempFile("date", ".csv");
			os = new FileOutputStream(outputFile);
			wb.write(os);
		}
		catch (IOException e) {
			e.printStackTrace();
			error("Export failed: " + ExceptionUtils.getRootCauseMessage(e));
			cancel();
		}
		finally {
			IOUtils.closeQuietly(os);
			if (isCancelled()) {
				clean();
			}
		}
	}
}
