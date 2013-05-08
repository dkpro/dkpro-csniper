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
package de.tudarmstadt.ukp.csniper.webapp.search.cqp;

import static de.tudarmstadt.ukp.csniper.webapp.search.cqp.ContextUnit.ATTR_BEGIN;
import static de.tudarmstadt.ukp.csniper.webapp.search.cqp.ContextUnit.ATTR_END;
import static de.tudarmstadt.ukp.csniper.webapp.search.cqp.ContextUnit.ATTR_ID;
import static de.tudarmstadt.ukp.csniper.webapp.search.cqp.ContextUnit.E_SENTENCE;
import static de.tudarmstadt.ukp.csniper.webapp.search.cqp.ContextUnit.E_TEXT;
import static org.apache.commons.lang.StringUtils.join;
import static org.apache.commons.lang.StringUtils.substringAfterLast;
import static org.apache.commons.lang.StringUtils.substringBeforeLast;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationItem;
import de.tudarmstadt.ukp.csniper.webapp.search.PreparedQuery;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;

/**
 * This class provides a super-slimmed down API for CQP (although you should be able to use most CQP
 * commands via the exec() function).
 * 
 * @author Erik-Lân Do Dinh
 * 
 */
public class CqpQuery
	implements PreparedQuery, Closeable
{
	private final Log log = LogFactory.getLog(getClass());

	private final CqpEngine engine;
	private final String type;
	private final String corpus;

	private String macrosLocation;

	private int leftContext = 5;
	private int rightContext = 5;
	private ContextUnit contextUnit = ContextUnit.WORD;
	private String leftDelim = "--%%%--";
	private String rightDelim = "--%%%--";

	private List<String> error;
	private String version;

	private static final String CQP_VERSION_PREFIX = "CQP version ";
	private static final String CQP_EOL = "-::-EOL-::-";
	private static final String SEP = "/";
	private static final String STD_QUERY_NAME = "Q";

	private boolean querySuccess = true;

	private Process cqpProcess;

	private int maxResults = 1000;
	private int timeout = 10 * 1000;

	/**
	 * Constructs a CQPManager.
	 * 
	 * @param aEngine
	 * @param aType
	 * @param aCorpus
	 */
	public CqpQuery(CqpEngine aEngine, String aType, String aCorpus)
	{
		engine = aEngine;
		type = aType;
		corpus = aCorpus;

		if (corpus == null) {
			throw new InvalidDataAccessResourceUsageException("Corpus cannot be null.");
		}
		error = new ArrayList<String>();

		cqpProcess = getCQPProcess();

		// -- set obligatory options --
		// corpus
		List<String> output = exec(corpus);
		if (output.size() > 0) {
			version = StringUtils.substringAfter(output.get(0), CQP_VERSION_PREFIX);
		}
		// add macro definitions
		if (engine.getMacrosLocation() != null) {
			setMacrosLocation(engine.getMacrosLocation());
		}
		// set default delimiters (can be changed)
		setLeftDelim(leftDelim);
		setRightDelim(rightDelim);
		// show positional attributes
		send("show +" + ATTR_BEGIN);
		send("show +" + ATTR_END);
		send("set PrintStructures \"" + E_TEXT + "_" + ATTR_ID + "\"");
		// activate progressbar (essential, because we stop reading at EOL, which occurs after
		// the progress messages
		send("set ProgressBar on");
	}

	public void setTimeout(int aTimeout)
	{
		timeout = aTimeout;
	}

	public int getTimeout()
	{
		return timeout;
	}

	/**
	 * Sends a query to cqp.
	 * 
	 * @param aQuery
	 *            query which shall be sent to cqp
	 * @throws DataAccessException
	 */
	public void runQuery(String aQuery)
		throws DataAccessException
	{
		exec(STD_QUERY_NAME + " = " + aQuery + " within " + E_SENTENCE);
		querySuccess = true;
	}

	/**
	 * Sends a size command to cqp.
	 * 
	 * @return size of the last query sent to cqp via runQuery()
	 * @throws DataAccessException
	 */
	@Override
	public int size()
		throws DataAccessException
	{
		if (!querySuccess) {
			log.warn("A query has to be run via runQuery() before size() can be called.");
			return 0;
		}
		List<String> output = exec("size " + STD_QUERY_NAME);
		if (output.size() != 1) {
			throw new InvalidDataAccessResourceUsageException(
					"'size' did not output the expected amount of lines [1]; was [" + output.size()
							+ "].");
		}
		return Integer.parseInt(output.get(0));
	}

	/**
	 * Sends a cat command to cqp.
	 * 
	 * @param aSize
	 *            maximum of result lines cat should deliver
	 * @return result of the last query sent to cqp via runQuery()
	 * @throws DataAccessException
	 */
	public List<EvaluationItem> cat(int aSize)
		throws DataAccessException
	{
		List<String> output = exec("cat " + STD_QUERY_NAME + " 0 " + (aSize - 1));
		return parseOutput(output);
	}

	/**
	 * Searches for a sentence (represented by the given EvaluationItem) in cqp, and returns it with
	 * context of a given size.
	 * 
	 * @param aItem
	 *            containing the sentence and its position in the corpus to search for
	 * @param aContextSize
	 *            size of the context window to return (in sentences)
	 * @return a list of sentences
	 */
	public List<String> getContextAround(EvaluationItem aItem, int aContextSize)
	{
		int oldLeftContext = leftContext;
		int oldRightContext = rightContext;
		ContextUnit oldContextUnit = contextUnit;
		String oldLeftDelim = leftDelim;
		String oldRightDelim = rightDelim;

		// context has to have +1 to account for the item/sentence itself
		setContext(aContextSize + 1, aContextSize + 1, ContextUnit.SENTENCE);
		setLeftDelim("");
		setRightDelim("");
		send("show +" + E_SENTENCE);
		send("show -" + ATTR_BEGIN);
		send("show -" + ATTR_END);

		// get the match for the first token of the item in the containing text and expand it to
		// cover its whole sentence
		runQuery("[begin=\"" + aItem.getBeginOffset() + "\"] :: match.text_id=\""
				+ aItem.getDocumentId() + "\" expand to 1 sentence");
		List<String> output = exec("cat " + STD_QUERY_NAME);

		// reset params
		setContext(oldLeftContext, oldRightContext, oldContextUnit);
		setLeftDelim(oldLeftDelim);
		setRightDelim(oldRightDelim);
		send("show -" + E_SENTENCE);
		send("show +" + ATTR_BEGIN);
		send("show +" + ATTR_END);

		return output;
	}

	/**
	 * Sends an exit command to cqp; also destroys the cqp process.<br>
	 * After exiting, this CqpManager cannot be used anymore.
	 */
	@Override
	public void close()
	{
		if (log.isDebugEnabled()) {
			log.debug("Killing CQP backend process");
		}
		send("exit");
		cqpProcess.destroy();
	}

	/**
	 * Executes a cqp command.
	 * 
	 * @param aCmd
	 *            command you want to send to cqp
	 * @return output of cqp triggered by the command
	 * @throws DataAccessException
	 */
	private List<String> exec(String aCmd)
		throws DataAccessException
	{
		String line;
		List<String> output = new ArrayList<String>();
		try {
			// the .EOL. is essential for checking whether we are finished reading
			send(aCmd + ";.EOL.");

			TimeoutReader reader = new TimeoutReader(new InputStreamReader(
					cqpProcess.getInputStream(), engine.getEncoding(corpus)));
			reader.setTimeout(timeout);

			while ((line = reader.readLine()) != null) {
				if (line.equals(CQP_EOL)) {
					if (log.isTraceEnabled()) {
						log.trace(CQP_EOL);
					}
					break;
				}
				if (log.isTraceEnabled()) {
					log.trace("<< " + line);
				}
				output.add(line);
			}
		}
		catch (IOException e) {
			throw new InvalidDataAccessResourceUsageException(e.getMessage());
		}
		checkError();

		return output;
	}

	/**
	 * Checks the stderr for errors thrown by cqp.
	 * 
	 * @throws InvalidDataAccessResourceUsageException
	 */
	private void checkError()
		throws InvalidDataAccessResourceUsageException
	{
		String line;
		try {
			BufferedReader _br = new BufferedReader(new InputStreamReader(
					cqpProcess.getErrorStream(), engine.getEncoding(corpus)));

			while (_br.ready()) {
				line = _br.readLine();
				if (log.isErrorEnabled()) {
					log.error(line);
				}
				error.add(line);
			}
		}
		catch (IOException e) {
			throw new InvalidDataAccessResourceUsageException(e.getMessage());
		}

		if (!error.isEmpty()) {
			throw new InvalidDataAccessResourceUsageException(join(error, "\n"));
		}
	}

	private void send(String aLine)
	{
		PrintWriter pw = new PrintWriter(cqpProcess.getOutputStream());
		pw.println(aLine + (!aLine.endsWith(";") ? ";" : ""));
		pw.flush();
		if (log.isTraceEnabled()) {
			log.trace(">> " + aLine);
		}
	}

	private List<EvaluationItem> parseOutput(List<String> aOutput)
	{
		List<EvaluationItem> items = new ArrayList<EvaluationItem>();
		String regexp = "\\s*(\\d+):\\s*<" + E_TEXT + "_" + ATTR_ID + "\\s(.+)>:\\s*(.*?)"
				+ Pattern.quote(leftDelim) + "(.*?)" + Pattern.quote(rightDelim) + "(.*?)";
		Pattern p = Pattern.compile(regexp);
		Matcher m = p.matcher("");

		// parse results and create EvaluationItems
		for (String line : aOutput) {
			m.reset(line);
			if (m.matches() /* && m.groupCount() == 5 */) {
				int position = Integer.valueOf(m.group(1));
				String documentId = m.group(2).trim();
				String lc = m.group(3).trim();
				String match = m.group(4).trim();
				String rc = m.group(5).trim();
				int begin = getBegin(lc, match);
				int originalMatchBegin = getBegin("", match);
				int end = getEnd(rc, match);
				int originalMatchEnd = getEnd("", match);

				if (!lc.isEmpty()) {
					lc = getText(lc).trim() + " ";
				}
				match = getText(match);
				if (!rc.isEmpty()) {
					rc = " " + getText(rc).trim();
				}

				String coveredText = (lc + match + rc);
				if (coveredText.length() < EvaluationItem.MAX_COLUMN_LENGTH) {
					EvaluationItem item = new EvaluationItem(corpus, documentId, type,
							begin, end, coveredText);
					item.setMatchOnItemText(lc.length(), lc.length() + match.length());
					item.setMatchOnOriginalText(originalMatchBegin, originalMatchEnd);
					items.add(item);
				}
				else {
					log.warn("Ignored oversized match in collection [" + corpus + "] document ["
							+ documentId + "] at [" + begin + "-" + end + "]");
				}
			}
			else {
				log.debug("Regexp [" + regexp + "] did not match on [" + line + "]");
			}
		}
		return items;
	}

	private String getText(String aText)
	{
		String[] tokens = aText.split(" ");
		for (int i = 0; i < tokens.length; i++) {
			// take the string before the penultimate "/"
			tokens[i] = substringBeforeLast(substringBeforeLast(tokens[i], SEP), SEP);
		}
		return StringUtils.join(tokens, " ");
	}

	private int getBegin(String lc, String match)
	{
		// if lc is empty, use match; use the first token
		String l = lc.length() > 0 ? lc.split(" ")[0] : match.split(" ")[0];
		// take the digits between the two last "/"
		return Integer.valueOf(substringAfterLast(substringBeforeLast(l, SEP), SEP));
	}

	private int getEnd(String rc, String match)
	{
		// if rc is empty, use match; just take the digits after the last "/"
		String ll = substringAfterLast(rc.length() > 0 ? rc : match, SEP);
		return Integer.valueOf(ll);
	}

	private Process getCQPProcess()
		throws DataAccessResourceFailureException
	{
		try {
			List<String> cmd = new ArrayList<String>();

			cmd.add(engine.getCqpExecutable().getAbsolutePath());
			cmd.add("-r");
			cmd.add(engine.getRegistryPath().getAbsolutePath());
			// run cqp as child process (-c)
			cmd.add("-c");

			if (log.isTraceEnabled()) {
				log.trace("Invoking [" + StringUtils.join(cmd, " ") + "]");
			}

			final ProcessBuilder pb = new ProcessBuilder(cmd);
			return pb.start();
		}
		catch (IOException e1) {
			throw new DataAccessResourceFailureException("Unable to start CQP process", e1);
		}
	}

	public List<String> getError()
	{
		return error;
	}

	public int getLeftContext()
	{
		return leftContext;
	}

	private void setLeftContext(int aLeftContext)
	{
		leftContext = aLeftContext;
	}

	public int getRightContext()
	{
		return rightContext;
	}

	private void setRightContext(int aRightContext)
	{
		rightContext = aRightContext;
	}

	public ContextUnit getContextUnit()
	{
		return contextUnit;
	}

	private void setContextUnit(ContextUnit aContextUnit)
	{
		contextUnit = aContextUnit;
	}

	public String getLeftDelim()
	{
		return leftDelim;
	}

	public void setLeftDelim(String aLeftDelim)
	{
		leftDelim = aLeftDelim;
		send("set LeftKWICDelim '" + leftDelim + "'");
	}

	public String getRightDelim()
	{
		return rightDelim;
	}

	public void setRightDelim(String aRightDelim)
	{
		rightDelim = aRightDelim;
		send("set RightKWICDelim '" + rightDelim + "'");
	}

	public CqpEngine getEngine()
	{
		return engine;
	}

	public String getCorpus()
	{
		return corpus;
	}

	public String getVersion()
	{
		return version;
	}

	public String getMacrosLocation()
	{
		return macrosLocation;
	}

	public void setMacrosLocation(String aMacrosLocation)
	{
		macrosLocation = aMacrosLocation;
		try {
			send("define macro < '"
					+ ResourceUtils.getUrlAsFile(
							ResourceUtils.resolveLocation(macrosLocation, this, null), true)
							.getAbsolutePath() + "'");
		}
		catch (IOException e) {
			log.warn("Macro file could not be found: " + e);
		}
	}

	/**
	 * Sets the context window of cqp.
	 * 
	 * @param aLeft
	 *            size of left context window
	 * @param aRight
	 *            size of right context window
	 * @param aContextUnit
	 *            unit of context window
	 */
	public void setContext(int aLeft, int aRight, ContextUnit aUnit)
	{
		setLeftContext(aLeft);
		setRightContext(aRight);
		setContextUnit(aUnit);

		if (leftContext >= 0) {
			send("set LeftContext " + leftContext + " " + contextUnit);
		}
		if (rightContext >= 0) {
			send("set RightContext " + rightContext + " " + contextUnit);
		}
	}

	public int getMaxResults()
	{
		return maxResults;
	}

	@Override
	public void setMaxResults(int aMaxResults)
	{
		maxResults = aMaxResults;
	}

	@Override
	public List<EvaluationItem> execute()
	{
		return cat(maxResults);
	}
}
