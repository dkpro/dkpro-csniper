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
package de.tudarmstadt.ukp.csniper.webapp.search.xmi;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.fit.factory.TypeSystemDescriptionFactory.createTypeSystemDescription;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.internal.util.Timer;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.fit.util.JCasUtil;
import org.xml.sax.SAXException;

import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationItem;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.ItemContext;
import de.tudarmstadt.ukp.csniper.webapp.search.ContextProvider;
import de.tudarmstadt.ukp.csniper.webapp.search.CorpusService;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class XmiContextProvider
	implements ContextProvider
{
	private static final ThreadLocal<JCasState> jcasThreadLocal = new ThreadLocal<JCasState>()
	{
		@Override
		protected JCasState initialValue()
		{
			try {
				JCasState state = new JCasState();

				// Try to get a bit more initial heap to improve performance.
				// See: https://issues.apache.org/jira/browse/UIMA-2385
				Properties props = new Properties();
				props.setProperty(UIMAFramework.CAS_INITIAL_HEAP_SIZE,
						String.valueOf(CASImpl.DEFAULT_INITIAL_HEAP_SIZE * 4));
				state.jcas = CasCreationUtils.createCas(createTypeSystemDescription(), null, null,
						props).getJCas();
				return state;
			}
			catch (UIMAException e) {
				throw new IllegalStateException(e);
			}
		};
	};

	private static class JCasState
	{
		private String collectionId;
		private String documentId;
		private JCas jcas;
	}

	private static Log log = LogFactory.getLog(XmiContextProvider.class);

	private static final String XMI = "xmi";

	private boolean outputPos = true;
	private CorpusService corpusService;

	@Override
	public void setCorpusService(CorpusService aCorpusService)
	{
		corpusService = aCorpusService;
	}

	@Override
	public void setOutputPos(boolean showPos)
	{
		outputPos = showPos;
	}

	@Override
	public ItemContext getContext(EvaluationItem aItem, int aLeftSize, int aRightSize)
		throws IOException
	{
		Timer timer = new Timer();

		File base = new File(new File(corpusService.getRepositoryPath(), aItem.getCollectionId()
				.toUpperCase()), XMI);
		String docId = aItem.getDocumentId();
		JCasState state = jcasThreadLocal.get();
		// FIXME sometimes cas is not being reused (because of state.documentId==null - is this only
		// available for a limited timed?)
		if ((state.documentId == null) || (state.collectionId == null)
				|| !StringUtils.equals(state.documentId, docId)
				|| !StringUtils.equals(state.collectionId, aItem.getCollectionId())) {
			timer.start();

			InputStream is = null;
			try {
				// No need to reset the CAS - the XmiCasDeserializer does that
				is = new GZIPInputStream(new FileInputStream(new File(base, docId + ".xmi.gz")));
				XmiCasDeserializer.deserialize(is, state.jcas.getCas());
				state.documentId = aItem.getDocumentId();
				state.collectionId = aItem.getCollectionId();
			}
			catch (IllegalStateException e) {
				throw new IOException(e);
			}
			catch (SAXException e) {
				throw new IOException(e);
			}
			finally {
				closeQuietly(is);
			}

			timer.stop();
			log.debug("Reading the XMI took " + timer.getTime() + "ms");
		}
		else {
			log.debug("Reusing CAS");
		}

		timer.reset();
		timer.start();

		// text offset based
		String text = state.jcas.getDocumentText();

		// Absolute offsets
		int windowBegin = Math.max(0, (int) aItem.getBeginOffset() - aLeftSize);
		int windowEnd = Math.min(text.length(), (int) aItem.getEndOffset() + aRightSize);

		// Relative offsets
		int unitBegin = (int) aItem.getBeginOffset() - windowBegin;
		int unitEnd = (int) aItem.getEndOffset() - windowBegin;

		StringBuilder windowText = new StringBuilder(text.substring(windowBegin, windowEnd));

		List<Token> tokens = JCasUtil.selectCovered(state.jcas, Token.class,
				(int) aItem.getBeginOffset(), (int) aItem.getEndOffset());
		int unitEndDisplacement = 0;
		int matchEndDisplacement = 0;
		int matchBeginDisplacement = 0;

		boolean anyMatchSet = false;
		int matchBeginOffset = aItem.getOriginalTextMatchBegin();
		int matchEndOffset = aItem.getOriginalTextMatchEnd();
		
		if (aItem.isOriginalMatchSet()) {
			matchBeginOffset = aItem.getOriginalTextMatchBegin();
			matchEndOffset = aItem.getOriginalTextMatchEnd();
			anyMatchSet = true;
		}
		else if (aItem.isTokenMatchSet()) {
			matchBeginOffset = tokens.get(aItem.getTokenMatchBegin()).getBegin();
			matchEndOffset = tokens.get(aItem.getTokenMatchEnd()).getEnd();
			anyMatchSet = true;
		}

		Collections.reverse(tokens);
		// default: output pos with tokens
		if (outputPos) {
			for (Token t : tokens) {
				if (t.getPos() != null && t.getPos().getPosValue() != null) {
					String postfix = "/" + t.getPos().getPosValue();
					windowText.insert(t.getEnd() - windowBegin, postfix);
					unitEndDisplacement += postfix.length();
					if (anyMatchSet) {
						if ((t.getEnd() <= matchEndOffset) && (t.getBegin() >= matchBeginOffset)) {
							matchEndDisplacement += postfix.length();
						}
						if (t.getEnd() <= matchBeginOffset) {
							matchBeginDisplacement += postfix.length();
						}
					}
				}
			}
		}

		ItemContext ctx = new ItemContext(windowText.toString(), windowBegin, windowEnd, unitBegin,
				unitEnd + unitEndDisplacement);

		if (anyMatchSet) {
			ctx.setMatch(matchBeginOffset - windowBegin + matchBeginDisplacement, matchEndOffset
					- windowBegin + matchBeginDisplacement + matchEndDisplacement);
		}

		ctx.setTextLength(text.length());

		timer.stop();
		log.debug("Extracting the context took " + timer.getTime() + "ms");

		return ctx;
	}
}
