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
package de.tudarmstadt.ukp.csniper.resbuild;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.Corpus;
import de.tudarmstadt.ukp.csniper.webapp.search.CorpusService;
import de.tudarmstadt.ukp.csniper.webapp.search.SearchEngine;
import de.tudarmstadt.ukp.csniper.webapp.search.cqp.ContextUnit;
import de.tudarmstadt.ukp.csniper.webapp.search.cqp.CqpEngine;
import de.tudarmstadt.ukp.csniper.webapp.search.cqp.CqpQuery;

public class EvaluationItemFixer
{
	private static final Log log = LogFactory.getLog(EvaluationItemFixer.class);

	private static final String HOST = "jdbc:mysql://loewe-ncc.ukp.informatik.tu-darmstadt.de/";
	private static final String DATABASE = "csniper";
	private static final String USER = "csniper";
	private static final String PASSWORD = "csniper";

	// private static final String CQP_EXECUTABLE = "/opt/imscwb/cqp";
	// private static final String CORPUS_REPOSITORY = "/srv/csniper";

	// private static final String HOST = "jdbc:mysql://127.0.0.1/";
	// private static final String DATABASE = "csniper";
	// private static final String USER = "root";
	// private static final String PASSWORD = "gugaguga";

	private static final String CQP_EXECUTABLE = "D:\\ukp\\cwb-3.4.3\\bin\\cqp.exe";
	private static final String REPOSITORY = "D:\\ukp\\data\\csniper";

	private static final String LOG_SUCCESSFUL = "C:\\users\\dodinh\\desktop\\successful.txt";
	private static final String LOG_FAILED = "C:\\users\\dodinh\\desktop\\failed.txt";

	private static final String LRB = "-LRB-";
	private static final String RRB = "-RRB-";

	private static Connection connection;

	private static CqpEngine engine = new CqpEngine()
	{
		private static final long serialVersionUID = 1L;
		{
			setCorpusService(new CorpusService()
			{
				public List<SearchEngine> listEngines(String aCorpusId)
				{
					return null;
				}

				public List<String> listCorpora()
				{
					return null;
				}

				public File getRepositoryPath()
				{
					return new File(REPOSITORY);
				}

				public Corpus getCorpus(String aCorpusId)
				{
					return null;
				}
			});
			setCqpExecutable(new File(CQP_EXECUTABLE));
		}
	};

	public static void main(String[] args)
	{
		connect(HOST, DATABASE, USER, PASSWORD);

		Map<Integer, String> items = new HashMap<Integer, String>();
		Map<Integer, String> failed = new HashMap<Integer, String>();

		// fetch coveredTexts of dubious items and clean it
		PreparedStatement select = null;
		try {
			StringBuilder selectQuery = new StringBuilder();
			selectQuery.append("SELECT * FROM EvaluationItem ");
			selectQuery.append("WHERE LOCATE(coveredText, '  ') > 0 ");
			selectQuery.append("OR LOCATE('" + LRB + "', coveredText) > 0 ");
			selectQuery.append("OR LOCATE('" + RRB + "', coveredText) > 0 ");
			selectQuery.append("OR LEFT(coveredText, 1) = ' ' ");
			selectQuery.append("OR RIGHT(coveredText, 1) = ' ' ");

			select = connection.prepareStatement(selectQuery.toString());
			log.info("Running query [" + selectQuery.toString() + "].");
			ResultSet rs = select.executeQuery();

			while (rs.next()) {
				int id = rs.getInt("id");
				String coveredText = rs.getString("coveredText");

				try {
					// special handling of double whitespace: in this case, re-fetch the text
					if (coveredText.contains("  ")) {
						coveredText = retrieveCoveredText(rs.getString("collectionId"),
								rs.getString("documentId"), rs.getInt("beginOffset"),
								rs.getInt("endOffset"));
					}

					// replace bracket placeholders and trim the text
					coveredText = StringUtils.replace(coveredText, LRB, "(");
					coveredText = StringUtils.replace(coveredText, RRB, ")");
					coveredText = coveredText.trim();

					items.put(id, coveredText);
				}
				catch (IllegalArgumentException e) {
					failed.put(id, e.getMessage());
				}
			}
		}
		catch (SQLException e) {
			log.error("Exception while selecting: " + e.getMessage());
		}
		finally {
			closeQuietly(select);
		}

		// write logs
		BufferedWriter bwf = null;
		BufferedWriter bws = null;
		try {
			bwf = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(
					LOG_FAILED)), "UTF-8"));
			for (Entry<Integer, String> e : failed.entrySet()) {
				bwf.write(e.getKey() + " - " + e.getValue() + "\n");
			}

			bws = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(
					LOG_SUCCESSFUL)), "UTF-8"));
			for (Entry<Integer, String> e : items.entrySet()) {
				bws.write(e.getKey() + " - " + e.getValue() + "\n");
			}
		}
		catch (IOException e) {
			log.error("Got an IOException while writing the log files.");
		}
		finally {
			IOUtils.closeQuietly(bwf);
			IOUtils.closeQuietly(bws);
		}

		log.info("Texts for [" + items.size() + "] items need to be cleaned up.");

		// update the dubious items with the cleaned coveredText
		PreparedStatement update = null;
		try {
			String updateQuery = "UPDATE EvaluationItem SET coveredText = ? WHERE id = ?";

			update = connection.prepareStatement(updateQuery);
			int i = 0;
			for (Entry<Integer, String> e : items.entrySet()) {
				int id = e.getKey();
				String coveredText = e.getValue();

				// update item in database
				update.setString(1, coveredText);
				update.setInt(2, id);
				update.executeUpdate();
				log.debug("Updating " + id + " with [" + coveredText + "]");

				// show percentage of updated items
				i++;
				int part = (int) Math.ceil((double) items.size() / 100);
				if (i % part == 0) {
					log.info(i / part + "% finished (" + i + "/" + items.size() + ").");
				}
			}
		}
		catch (SQLException e) {
			log.error("Exception while updating: " + e.getMessage());
		}
		finally {
			closeQuietly(update);
		}

		closeQuietly(connection);
	}

	private static void connect(String aHost, String aDatabase, String aUser, String aPassword)
	{
		String url = aHost + aDatabase + "?user=" + aUser + "&password=" + aPassword;

		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection(url);
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException("Failed to load the specified database driver.", e);
		}
		catch (SQLException e) {
			throw new RuntimeException(
					"There was an unrecoverable error while connecting to the database.", e);
		}
	}

	/**
	 * Using a workaround for java < 1.7.
	 * 
	 * @param aAutoCloseable
	 */
	private static void closeQuietly(Object aAutoCloseable)
	{
		try {
			if (aAutoCloseable != null) {
				if (aAutoCloseable instanceof Connection) {
					((Connection) aAutoCloseable).close();
				}
				if (aAutoCloseable instanceof Statement) {
					((Statement) aAutoCloseable).close();
				}
			}
		}
		catch (Exception e) {
			log.error("There was an unrecoverable error while closing [" + aAutoCloseable + "].", e);
		}
	}

	private static String retrieveCoveredText(final String aCollectionId, String aDocumentId,
			int aBeginOffset, int aEndOffset)
	{
		String coveredText;

		CqpQuery query = new CqpQuery(engine, "", aCollectionId);
		query.setContext(0, 0, ContextUnit.CHARACTER);
		String queryString = "[begin=\"" + aBeginOffset + "\"] []* [end=\"" + aEndOffset
				+ "\"] :: match.text_id=\"" + aDocumentId + "\"";
		log.trace(queryString);

		query.runQuery(queryString);

		if (query.size() > 1) {
			log.warn("More than 1 entry found: " + query);
		}
		if (query.size() == 0) {
			log.error("Nothing found for: " + queryString);
			query.close();
			throw new IllegalArgumentException(queryString);
		}
		coveredText = query.cat(1).get(0).getCoveredText();
		query.close();

		return coveredText;
	}
}
