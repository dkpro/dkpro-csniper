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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.csniper.resbuild.stuff.DummySentenceSplitter;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.Corpus;
import de.tudarmstadt.ukp.csniper.webapp.search.CorpusService;
import de.tudarmstadt.ukp.csniper.webapp.search.SearchEngine;
import de.tudarmstadt.ukp.csniper.webapp.search.cqp.ContextUnit;
import de.tudarmstadt.ukp.csniper.webapp.search.cqp.CqpEngine;
import de.tudarmstadt.ukp.csniper.webapp.search.cqp.CqpQuery;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;

public class EvaluationItemFixer2
{
	private static final Log log = LogFactory.getLog(EvaluationItemFixer2.class);

	private static final String HOST = "jdbc:mysql://localhost/";
	private static final String DATABASE = "csniper";
	private static final String USER = "root";
	private static final String PASSWORD = "gugaguga";

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
				@Override
                public List<SearchEngine> listEngines(String aCorpusId)
				{
					return null;
				}

				@Override
                public List<String> listCorpora()
				{
					return null;
				}

				@Override
                public File getRepositoryPath()
				{
					return new File(REPOSITORY);
				}

				@Override
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
		PreparedStatement update = null;
		try {
			StringBuilder selectQuery = new StringBuilder();
			selectQuery.append("SELECT * FROM cachedparse WHERE pennTree = 'ERROR' OR pennTree = ''");

			select = connection.prepareStatement(selectQuery.toString());
			log.info("Running query [" + selectQuery.toString() + "].");
			ResultSet rs = select.executeQuery();

			
//			CSVWriter writer;
			String text;
			JCas jcas = JCasFactory.createJCas();
			String updateQuery = "UPDATE CachedParse SET pennTree = ? WHERE collectionId = ? AND documentId = ? AND beginOffset = ? AND endOffset = ?";
			update = connection.prepareStatement(updateQuery);
//			File base = new File("");

			AnalysisEngine sentences = createEngine(DummySentenceSplitter.class);
			AnalysisEngine tokenizer = createEngine(StanfordSegmenter.class,
					StanfordSegmenter.PARAM_CREATE_SENTENCES, false,
					StanfordSegmenter.PARAM_CREATE_TOKENS, true);
			AnalysisEngine parser = createEngine(StanfordParser.class,
					StanfordParser.PARAM_WRITE_CONSTITUENT, true,
//					StanfordParser.PARAM_CREATE_DEPENDENCY_TAGS, true,
					StanfordParser.PARAM_WRITE_PENN_TREE, true,
					StanfordParser.PARAM_LANGUAGE, "en",
					StanfordParser.PARAM_VARIANT, "factored");
			
			while (rs.next()) {
				String collectionId = rs.getString("collectionId");
				String documentId = rs.getString("documentId");
				int beginOffset = rs.getInt("beginOffset");
				int endOffset = rs.getInt("endOffset");
				text = retrieveCoveredText(collectionId, documentId, beginOffset, endOffset);

				jcas.setDocumentText(text);
				jcas.setDocumentLanguage("en");
				sentences.process(jcas);
				tokenizer.process(jcas);
				parser.process(jcas);

//				writer = new CSVWriter(new FileWriter(new File(base, documentId + ".csv"));
				
				System.out.println("Updating " + text);
				for (PennTree p : JCasUtil.select(jcas, PennTree.class)) {
					String tree = StringUtils.normalizeSpace(p.getPennTree());
					update.setString(1, tree);
					update.setString(2, collectionId);
					update.setString(3, documentId);
					update.setInt(4, beginOffset);
					update.setInt(5, endOffset);
					update.executeUpdate();
					System.out.println("with tree " + tree);
					break;
				}
				jcas.reset();
			}
		}
		catch (SQLException e) {
			log.error("Exception while selecting: " + e.getMessage());
		}
		catch (UIMAException e) {
			e.printStackTrace();
		}
		finally {
			closeQuietly(select);
			closeQuietly(update);
		}

		// write logs
//		BufferedWriter bwf = null;
//		BufferedWriter bws = null;
//		try {
//			bwf = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(
//					LOG_FAILED)), "UTF-8"));
//			for (Entry<Integer, String> e : failed.entrySet()) {
//				bwf.write(e.getKey() + " - " + e.getValue() + "\n");
//			}
//
//			bws = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(
//					LOG_SUCCESSFUL)), "UTF-8"));
//			for (Entry<Integer, String> e : items.entrySet()) {
//				bws.write(e.getKey() + " - " + e.getValue() + "\n");
//			}
//		}
//		catch (IOException e) {
//			log.error("Got an IOException while writing the log files.");
//		}
//		finally {
//			IOUtils.closeQuietly(bwf);
//			IOUtils.closeQuietly(bws);
//		}

		log.info("Texts for [" + items.size() + "] items need to be cleaned up.");

		// update the dubious items with the cleaned coveredText
//		PreparedStatement update = null;
//		try {
//			String updateQuery = "UPDATE EvaluationItem SET coveredText = ? WHERE id = ?";
//
//			update = connection.prepareStatement(updateQuery);
//			int i = 0;
//			for (Entry<Integer, String> e : items.entrySet()) {
//				int id = e.getKey();
//				String coveredText = e.getValue();
//
//				// update item in database
//				update.setString(1, coveredText);
//				update.setInt(2, id);
//				update.executeUpdate();
//				log.debug("Updating " + id + " with [" + coveredText + "]");
//
//				// show percentage of updated items
//				i++;
//				int part = (int) Math.ceil((double) items.size() / 100);
//				if (i % part == 0) {
//					log.info(i / part + "% finished (" + i + "/" + items.size() + ").");
//				}
//			}
//		}
//		catch (SQLException e) {
//			log.error("Exception while updating: " + e.getMessage());
//		}
//		finally {
//			closeQuietly(update);
//		}

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
