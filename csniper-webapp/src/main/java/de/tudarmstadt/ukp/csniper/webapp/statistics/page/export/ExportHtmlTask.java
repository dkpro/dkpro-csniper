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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

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

public class ExportHtmlTask
	extends Task
	implements ExportTask
{
	private static final int COLUMN_COUNT = 12;

	private EvaluationRepository repository;
	private ContextProvider contextProvider;

	private StatisticsFormModel formModel;
	private ExportModel exportModel;

	private File outputFile;

	public ExportHtmlTask(StatisticsFormModel aFormModel, ExportModel aExportModel,
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
		sb.append(".html");

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
		HtmlWriter writer = null;

		contextProvider.setOutputPos(exportModel.includePos);

		outputFile = null;
		try {
			outputFile = File.createTempFile("date", ".html");

			writer = new HtmlWriter(new OutputStreamWriter(new FileOutputStream(outputFile),
					"UTF-8"));

			List<AdditionalColumn> ac = exportModel.additionalColumns;
			String[] row = new String[COLUMN_COUNT];

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

			// Write rest
			setTotal(results.size());
			for (AggregatedEvaluationResult aer : results) {
				ResultFilter classification = aer.getClassification();
				if (formModel.getFilters().contains(classification)) {
					ItemContext context = contextProvider.getContext(aer.getItem(),
							exportModel.contextSize, exportModel.contextSize);

					row[0] = aer.getItem().getCollectionId();
					row[1] = aer.getItem().getDocumentId();
					row[2] = Long.toString(aer.getItem().getBeginOffset());
					row[3] = Long.toString(aer.getItem().getEndOffset());
					row[4] = context.getLeft();
					row[5] = context.getUnit();
					row[6] = context.getRight();
					row[7] = aer.getItem().getType();
					row[8] = classification.toString();
					row[9] = Double.toString(aer.getConfidence());
					row[10] = Integer.toString(aer.getCorrect());
					row[11] = Integer.toString(aer.getWrong());
					writer.writeHtml(row, aer.getItem().getId(), ac, aer.getUsers(false));
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
			writer.close();
			if (isCancelled()) {
				clean();
			}
		}
	}

	private class HtmlWriter
	{
		private OutputStreamWriter out;

		public HtmlWriter(OutputStreamWriter aOutputStreamWriter)
			throws IOException
		{
			out = aOutputStreamWriter;
			write("<html><head>");
			write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
			write("<style type=\"text/css\">");
			write("div div { border:solid 1px #777; padding:5px; display:block; color:#777; }");
			write("p, div div { margin:5 0px; }");
			write("p.context { color:#333; }");
			write("p.item { margin: 13px 0px 13px 30px; }");
			write("table { color:#777; border-collapse: collapse; }");
			write("td, th { padding: 3px 6px 3px 6px; }");
			write("th { font-weight:bold; border-bottom: 1px solid #000; }");
			write("</style>");
			write("</head><body>");
			write("<h1>" + getFilename() + "</h1>");
		}

		public void writeHtml(String[] row, long aItemId,
				List<AdditionalColumn> aAdditionalColumns, Collection<String> aUsers)
			throws IOException
		{
			write("<div>");
			write("<div><strong>" + row[0] + "</strong>, textid[" + row[1] + "], offsets[" + row[2]
					+ "," + row[3] + "], type[<strong>" + row[7]
					+ "</strong>], classification[<strong>" + row[8] + "</strong> (" + row[9]
					+ ")], correct|wrong[" + row[10] + "|" + row[11] + "]");
			if (!aUsers.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				boolean columnsHaveEntries = false;
				sb.append("<table>");
				sb.append("<tr>");
				sb.append("<th>User</th>");
				for (AdditionalColumn ac : aAdditionalColumns) {
					sb.append("<th>" + ac.getName() + "</th>");
				}
				sb.append("</tr>");
				for (String user : aUsers) {
					boolean userHasEntries = false;
					StringBuilder userRow = new StringBuilder();
					userRow.append("<tr>");
					userRow.append("<td>" + user + "</td>");
					for (AdditionalColumn ac : aAdditionalColumns) {
						String value = repository.getEvaluationResult(aItemId, user)
								.getAdditionalColumns().get(ac);
						if (value == null) {
							value = "";
						}
						else {
							userHasEntries = true;
							columnsHaveEntries = true;
						}
						userRow.append("<td>" + value + "</td>");
					}
					userRow.append("</tr>");
					if (userHasEntries) {
						sb.append(userRow);
					}
				}
				sb.append("</table>");
				if (columnsHaveEntries) {
					write(sb.toString());
				}
			}
			write("</div>");
			write("<p class=\"context\">[...] " + nl2br(row[4].trim()) + "</p>");
			write("<p class=\"item\">" + nl2br(row[5].trim()) + "</p>");
			write("<p class=\"context\">" + nl2br(row[6].trim()) + " [...]</p>");
			write("</div>");
			write("<hr /><br />");
		}

		public void close()
		{
			try {
				write("</body></html>");
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				IOUtils.closeQuietly(out);
			}
		}

		private void write(String aStr)
			throws IOException
		{
			out.write(aStr + "\n");
		}

		private String nl2br(String aStr)
		{
			return StringUtils.replace(aStr, "\n", "<br />\n");
		}
	}
}
