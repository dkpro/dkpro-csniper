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
package de.tudarmstadt.ukp.csniper.webapp.search.tgrep;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessResourceFailureException;

import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationItem;
import de.tudarmstadt.ukp.csniper.webapp.search.PreparedQuery;

/**
 * This class provides a super-slimmed down API for Tgrep2.
 * 
 * @author Erik-Lân Do Dinh
 * 
 */
public class TgrepQuery
	implements PreparedQuery
{
	private final Log log = LogFactory.getLog(getClass());

	private final static int META_DOCUMENT_ID = 0;
	private final static int META_BEGIN_OFFSET = 1;
	private final static int META_END_OFFSET = 2;

	private final static int LINES_PER_MATCH = 4;

	private static final String LEFT_BRACKET = "-LRB-";
	private static final String RIGHT_BRACKET = "-RRB-";

	private final TgrepEngine engine;
	private final String type;
	private final String corpus;
	private final String query;

	private Process tgrep;

	private int size = -1;
	private int maxResults = -1;

	public TgrepQuery(TgrepEngine aEngine, String aType, String aCorpus, String aQuery)
	{
		engine = aEngine;
		type = aType;
		corpus = aCorpus;
		query = aQuery;
	}

	@Override
	public int size()
	{
		return size;
	}

	@Override
	public void close()
	{
		if (log.isDebugEnabled()) {
			log.debug("Killing Tgrep2 process.");
		}
		if (tgrep != null) {
			tgrep.destroy();
		}
	}

	@Override
	public void setMaxResults(int aMaxResults)
	{
		maxResults = aMaxResults;
	}

	@Override
	public List<EvaluationItem> execute()
	{
		BufferedReader brInput = null;
		BufferedReader brError = null;
		List<String> output = new ArrayList<String>();
		List<String> error = new ArrayList<String>();

		try {
			List<String> cmd = new ArrayList<String>();

			File exe = engine.getTgrepExecutable();
			if (!exe.canExecute()) {
				exe.setExecutable(true);
			}

			cmd.add(exe.getAbsolutePath());
			// specify corpus
			cmd.add("-c");
			cmd.add(engine.getCorpusPath(corpus));
			// only one match per sentence
			cmd.add("-f");
			// print options
			cmd.add("-m");
			// comment
			// full sentence
			// match begin token index
			// match end token index
			cmd.add("%c\\n%tw\\n%ym\\n%zm\\n");
			// pattern to search for
			cmd.add(query);
			if (log.isTraceEnabled()) {
				log.trace("Invoking [" + StringUtils.join(cmd, " ") + "]");
			}

			final ProcessBuilder pb = new ProcessBuilder(cmd);
			tgrep = pb.start();

			brInput = new BufferedReader(new InputStreamReader(tgrep.getInputStream(), "UTF-8"));
			brError = new BufferedReader(new InputStreamReader(tgrep.getErrorStream(), "UTF-8"));

			String line;
			while ((line = brInput.readLine()) != null) {
				if (log.isTraceEnabled()) {
					log.trace("<< " + line);
				}
				output.add(line);
			}

			while ((line = brError.readLine()) != null) {
				if (log.isErrorEnabled()) {
					log.error(line);
				}
				error.add(line);
			}

			if (!error.isEmpty()) {
				throw new IOException(StringUtils.join(error, " "));
			}
		}
		catch (IOException e) {
			throw new DataAccessResourceFailureException("Unable to start Tgrep process.", e);
		}
		finally {
			IOUtils.closeQuietly(brInput);
			IOUtils.closeQuietly(brError);
		}

		size = output.size() / LINES_PER_MATCH;
		if (maxResults >= 0 && size > maxResults) {
			return parseOutput(output.subList(0, LINES_PER_MATCH * maxResults));
		}
		else {
			return parseOutput(output);
		}
	}

	private List<EvaluationItem> parseOutput(List<String> aOutput)
	{
		List<EvaluationItem> items = new ArrayList<EvaluationItem>();

		if (aOutput.size() % LINES_PER_MATCH > 0) {
			throw new DataAccessResourceFailureException("Tgrep2 produced [" + aOutput.size()
					+ "] output lines, but should have produced a multiple of [" + LINES_PER_MATCH
					+ "].");
		}
		else {
			String[] comment;
			String text;
			int tokenBeginIndex;
			int tokenEndIndex;

			for (Iterator<String> it = aOutput.iterator(); it.hasNext();) {
				// comment - split into documentId, beginOffset, endOffset
				comment = it.next().substring(2).split(TgrepEngine.COMMENT_SEPARATOR);
				if (comment.length < 3) {
					throw new DataAccessResourceFailureException(
							"The corpus contains a malformed comment line ["
									+ StringUtils.join(comment, " ,") + "].");
				}
				String documentId = comment[META_DOCUMENT_ID];
				int beginOffset = Integer.parseInt(comment[META_BEGIN_OFFSET]);
				int endOffset = Integer.parseInt(comment[META_END_OFFSET]);

				// text string - trim and replace bracket placeholders
				text = it.next().trim();
				text = StringUtils.replace(text, LEFT_BRACKET, "(");
				text = StringUtils.replace(text, RIGHT_BRACKET, ")");

				// token index of first token in match (tgrep indices are 1-based, make them
				// 0-based)
				tokenBeginIndex = Integer.parseInt(it.next()) - 1;

				// token index of last token in match (tgrep indices are 1-based, make them 0-based)
				tokenEndIndex = Integer.parseInt(it.next()) - 1;

				// set corpus position to -1; this is cqp specific and we don't use it atm
				EvaluationItem item = new EvaluationItem(corpus, documentId, type, beginOffset,
						endOffset, text);

				// text-based (i.e. sentence-based) offsets (+1 to skip the whitespace itself)
				int matchBegin = StringUtils.ordinalIndexOf(text, " ", tokenBeginIndex) + 1;
				int matchEnd = StringUtils.ordinalIndexOf(text, " ", tokenEndIndex + 1);

				item.setMatchOnItemText(matchBegin, matchEnd);
				item.setMatchOnOriginalTextViaTokenIndicesAndLookGoodWhileDoingSo(tokenBeginIndex,
						tokenEndIndex);
				items.add(item);
			}
		}
		return items;
	}
}
