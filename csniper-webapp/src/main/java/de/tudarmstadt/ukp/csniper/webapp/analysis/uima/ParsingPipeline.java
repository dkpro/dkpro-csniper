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
package de.tudarmstadt.ukp.csniper.webapp.analysis.uima;

import static org.apache.uima.util.CasCreationUtils.mergeTypeSystems;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.TypePrioritiesFactory.createTypePriorities;
import static org.uimafit.factory.TypeSystemDescriptionFactory.createTypeSystemDescription;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.CAS;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypePriorities;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.dom4j.io.SAXContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import de.tudarmstadt.ukp.csniper.webapp.support.uima.AnalysisEngineFactory;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
//import de.tudarmstadt.ukp.csniper.textmarker.TextmarkerDescriptorCreator;
//import de.uniwue.tm.textmarker.engine.TextMarkerEngine;
//import de.tudarmstadt.ukp.dkpro.core.stanford.tsurgeon.TsurgeonTransformer;
//import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
//import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;

public class ParsingPipeline
	implements Serializable
{
	private static final long serialVersionUID = 3411626870840060929L;

	private static final int TRANSFORMATION_TREGEX = 0;

	private static final String TEXTMARKER_FILENAME = "tmScript";
	private static final String TREGEX_FILENAME = "tregexScript.xml";
	private File output_html;
	private File output_dump;
	private String CLASSPATH;
	private String TEXTMARKER_BASE;
	private String TREGEX_BASE;
	private String PACKAGE;

	@SpringBean(name = "customAnalysisEngineFactory")
	private AnalysisEngineFactory aef;

	public ParsingPipeline()
	{
		try {
			PACKAGE = this.getClass().getPackage().getName();
			CLASSPATH = ResourceUtils.getUrlAsFile(
					ResourceUtils.resolveLocation("classpath:/", this, null), false)
					.getAbsolutePath();
			TEXTMARKER_BASE = CLASSPATH + "/textmarker";
			TREGEX_BASE = CLASSPATH + "/tregex";

			output_html = File.createTempFile("output", ".html");
			output_dump = File.createTempFile("output", ".txt");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	// public ParsingPipeline(String parser, String language, String
	// scriptingEngine,
	// String inputText, String script, String[] markedAnnotations, String[]
	// markerColors)
	// {
	// try {
	// PACKAGE = this.getClass().getPackage().getName(); // =
	// "de.tudarmstadt.ukp.experiments.erik.wicket"
	// // CLASSPATH = System.getProperty("loewe.ncc.home");
	// CLASSPATH =
	// ResourceUtils.getUrlAsFile(ResourceUtils.resolveLocation("classpath:/",
	// this, null), false).getAbsolutePath();
	// TEXTMARKER_BASE = CLASSPATH + "/textmarker";
	// TREGEX_BASE = CLASSPATH + "/tregex";
	//
	// output_html = File.createTempFile("output", ".html");
	// output_dump = File.createTempFile("output", ".txt");
	// output_png = File.createTempFile("output", ".png");
	// // TODO resolve markedAnnotations so that short names can be used in
	// // scripts instead of fully-qualified names
	//
	// // shortNames = new HashMap<String,Type>();
	// // Iterator<Type> ti = ts.getTypeIterator();
	// // while(ti.hasNext()) {
	// // Type t = ti.next();
	// // if(!shortNames.containsKey(t.getShortName())) {
	// // shortNames.put(t.getShortName(), t);
	// // // System.out.println("OK: " + t.getName() + " [" +
	// // t.getShortName() + "]");
	// // } else {
	// // System.out.println("WARNING: " + t.getName() + " [" +
	// // t.getShortName() + "] is already in the map as " +
	// // shortNames.get(t.getShortName()).getName());
	// // }
	// // }
	// //
	// //
	// // if(tokenType == null) {
	// // System.out.println("ERROR [token]");
	// // } else {
	// // System.out.println("GOOD [token]: " + tokenType.getName() + " ["
	// // + tokenType.getShortName());
	// // }
	// // if(sentenceType == null) {
	// // System.out.println("ERROR [token]");
	// // } else {
	// // System.out.println("GOOD [token]: " + sentenceType.getName() +
	// // " [" + sentenceType.getShortName());
	// // }
	// // types = new ArrayList<Type>();
	// // for(String shortName : markedAnnotations) {
	// // Type type = shortNames.get(shortName);
	// // if(type != null) {
	// // if(!shortNames.containsKey(shortName))
	// // System.out.println("rofl.");
	// // types.add(type);
	// // } else {
	// // // TODO: some message, proceed with other types
	// // // throw new XYZ
	// // System.out.println("Type [" + shortName +
	// // "] could not be found (use shortname!)");
	// // }
	// // }
	//
	// // JCas jcas = parseInput(parser, language, inputText);
	// // runScript(jcas, scriptingEngine, script, markedAnnotations,
	// markerColors);
	// }
	// catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	public CAS parseInput(String parser, String language, String inputText)
	{
		if (inputText == null) {
			inputText = "";
		}
		try {
			AnalysisEngine seg = AnalysisEngineFactory.createAnalysisEngine(
					AnalysisEngineFactory.SEGMENTER, "createSentences", true);
			AnalysisEngine par = AnalysisEngineFactory.createAnalysisEngine(
					AnalysisEngineFactory.PARSER, "language", language);

			// fill cas and create DocumentMetaData
			CAS cas = CasCreationUtils.createCas(createTypeSystemDescription(), null, null);
			DocumentMetaData.create(cas);
			cas.setDocumentText(inputText);
			cas.setDocumentLanguage(language);

			// tokenize
			seg.process(cas);
			// parse
			par.process(cas);

			return cas;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// public void runScript(CAS cas, String tool, String script, String[] types, String[] colors)
	// {
	// try {
	// // merge old and new (script) typesystem
	// TypeSystemDescription scriptTsd = getTypeSystemDescription(tool, script);
	// TypeSystemDescription oldTsd = TypeSystemUtil.typeSystem2TypeSystemDescription(cas
	// .getTypeSystem());
	// TypeSystemDescription merged = mergeTypeSystems(Arrays.asList(scriptTsd, oldTsd));
	//
	// // create a new cas with the appropriate merged typesystem
	// CAS dest = CasCreationUtils.createCas(merged, null, null);
	// DocumentMetaData.create(dest);
	// // copy the contents of the old cas into the newly created one
	// CasCopier.copyCas(cas, dest, true);
	//
	// AnalysisEngine scr = getScriptingEngine(tool, script);
	//
	// AnalysisEngine cm = createPrimitive(HTMLColorMarkerConsumer.class,
	// HTMLColorMarkerConsumer.PARAM_MARKED_TYPES, types,
	// HTMLColorMarkerConsumer.PARAM_MARKER_COLORS, colors,
	// HTMLColorMarkerConsumer.PARAM_OUTPUT_FILE, output_html.getAbsolutePath());
	//
	// AnalysisEngine pc = createPrimitive(SimpleDumpWriter.class,
	// SimpleDumpWriter.PARAM_OUTPUT_FILE, output_dump.getAbsolutePath());
	//
	// scr.process(dest);
	//
	// cm.process(dest);
	// cm.collectionProcessComplete();
	//
	// pc.process(dest);
	// pc.collectionProcessComplete();
	// }
	// catch (Throwable e) {
	// throw new RuntimeException(e);
	// }
	// }
	//
	// private TypeSystemDescription getTypeSystemDescription(String engine, String script)
	// throws ResourceInitializationException, MalformedURLException
	// {
	// List<TypeSystemDescription> descriptions = new ArrayList<TypeSystemDescription>();
	// descriptions.add(createTypeSystemDescription("BasicTypeSystem"));
	// descriptions.add(createTypeSystemDescription("InternalTypeSystem"));
	// descriptions.add(createTypeSystemDescription());
	//
	// if (engine.equals("textmarker")) {
	// TypeSystemDescription tmTsd = TextmarkerDescriptorCreator.createTypeSystem(PACKAGE,
	// TEXTMARKER_FILENAME, script);
	// descriptions.add(tmTsd);
	// }
	// return mergeTypeSystems(descriptions);
	// }
	//
	// /**
	// * Creates an AnalysisEngineDescription according to the given identifier
	// * string.
	// *
	// * @param tool
	// * @param tmScript
	// * @param tregexScript
	// * @return the appropriate AnalysisEngine
	// * @throws ResourceInitializationException
	// * @throws IOException
	// * @throws SAXException
	// * @throws ParserConfigurationException
	// */
	// private AnalysisEngine getScriptingEngine(String tool, String script)
	// throws ResourceInitializationException, IOException, SAXException,
	// ParserConfigurationException
	// {
	// if (tool.equals("textmarker")) {
	// script = "PACKAGE " + PACKAGE + ";\n" + script;
	// return getTextmarkerEngine(script);
	// }
	// else if (tool.equals("tregex")) {
	// // TODO: make more than one transformation possible
	// List<Transformation> transformations = new ArrayList<Transformation>();
	// Transformation t = new Transformation(script);
	// transformations.add(t);
	// createTRegexScript(transformations);
	// AnalysisEngineDescription scriptingEngine = createPrimitiveDescription(
	// TsurgeonTransformer.class, TsurgeonTransformer.PARAM_CASCADING_TRANSFORMATIONS,
	// true, TsurgeonTransformer.PARAM_TRANSFORMATION_FILE_NAME, TREGEX_BASE
	// + "/transformations/" + TREGEX_FILENAME,
	// TsurgeonTransformer.PARAM_SAVE_SOURCE_SENTENCES, false,
	// TsurgeonTransformer.PARAM_SAVE_UNCHANGED_TREES, true);
	// // TsurgeonTransformer.PARAM_ANNOTATE_APPLIED_TRANSFORMATIONS,
	// // true);
	//
	// // The part starting with a splitter and ending with a merger has to
	// // be in its own aggregate.
	// // The flow controller of the aggregate has to drop the intermediate CASes.
	// FlowControllerDescription fcd = createFlowControllerDescription(
	// FixedFlowController.class,
	// FixedFlowController.PARAM_ACTION_AFTER_CAS_MULTIPLIER, "drop");
	// AnalysisEngineDescription embedded = createAggregateDescription(fcd, scriptingEngine);
	//
	// // The final merged CAS, however, needs to be returned from the aggregate
	// embedded.getAnalysisEngineMetaData().getOperationalProperties()
	// .setOutputsNewCASes(true);
	//
	// return createAggregate(embedded);
	// }
	// else {
	// throw new IllegalArgumentException("tool has to be textmarker or tregex");
	// }
	// }
	//
	// /**
	// * Creates a TRegex transformation xml-file from given transformations.
	// *
	// * @param transformations
	// * a list of transformations from which to build a script
	// * @throws IOException
	// * @throws SAXException
	// */
	// private void createTRegexScript(List<Transformation> transformations)
	// throws IOException, SAXException
	// {
	// SAXContentHandler handler = new SAXContentHandler();
	//
	// handler.startDocument();
	// handler.startElement("", "transformations", "", new AttributesImpl());
	// for (Transformation transformation : transformations) {
	// transformation.toXML(handler);
	// }
	// handler.endElement("", "transformations", "");
	// handler.endDocument();
	//
	// File f = new File(TREGEX_BASE + "/transformations/" + TREGEX_FILENAME);
	// FileUtils.writeStringToFile(f, handler.getDocument().asXML(), "UTF-8");
	// }
	//
	// /**
	// * Represents a TRegex Transformation, including name, tregex and multiple operations.
	// *
	// */
	// private static class Transformation
	// {
	// private final String name;
	// private final String tregex;
	// private final List<String> operations;
	//
	// public Transformation(String inputScript)
	// {
	// String[] lines = StringUtils.split(inputScript, SystemUtils.LINE_SEPARATOR);
	// if (lines.length < 2) {
	// throw new IllegalArgumentException(
	// "The input script has to consist of at least two lines: tregex pattern, operation.");
	// }
	// name = "transformation_1";
	// tregex = lines[TRANSFORMATION_TREGEX] == null ? "" : lines[TRANSFORMATION_TREGEX];
	// operations = new ArrayList<String>();
	// for (int i = 1; i < lines.length; i++) {
	// if (lines[i] == null) {
	// lines[i] = "";
	// }
	// operations.add(lines[i]);
	// }
	// }
	//
	// /**
	// * Attaches this transformation to the given SAXContentHandler.
	// *
	// * @param handler
	// * the handler on which to call the events on
	// * @throws SAXException
	// */
	// public void toXML(SAXContentHandler handler)
	// throws SAXException
	// {
	// handler.startElement("", "transformation", "", new AttributesImpl());
	//
	// handler.startElement("", "name", "", new AttributesImpl());
	// handler.characters(name.toCharArray(), 0, name.toCharArray().length);
	// handler.endElement("", "name", "");
	//
	// handler.startElement("", "tregex", "", new AttributesImpl());
	// handler.startCDATA();
	// handler.characters(tregex.toCharArray(), 0, tregex.toCharArray().length);
	// handler.endCDATA();
	// handler.endElement("", "tregex", "");
	//
	// for (String operation : operations) {
	// handler.startElement("", "operation", "", new AttributesImpl());
	// handler.startCDATA();
	// handler.characters(operation.toCharArray(), 0, operation.toCharArray().length);
	// handler.endCDATA();
	// handler.endElement("", "operation", "");
	// }
	//
	// handler.endElement("", "transformation", "");
	// }
	// }
	//
	// /**
	// * Creates the TextMarker Analysis Engine.
	// *
	// * @return a TextMarker Analysis Engine
	// * @throws ResourceInitializationException
	// * @throws FileNotFoundException
	// * @throws SAXException
	// * @throws IOException
	// * @throws ParserConfigurationException
	// */
	// private AnalysisEngine getTextmarkerEngine(String aScript)
	// throws ResourceInitializationException, FileNotFoundException, SAXException, IOException,
	// ParserConfigurationException
	// {
	// // Extract types declared in script
	// TypeSystemDescription merged = getTypeSystemDescription("textmarker", aScript);
	//
	// // Set up type priorities
	// TypePriorities prios = createTypePriorities(new String[] {
	// "de.uniwue.tm.textmarker.kernel.type.TextMarkerFrame", "uima.tcas.Annotation",
	// "de.uniwue.tm.textmarker.kernel.type.TextMarkerBasic" });
	//
	// // Store script to a temporary location
	// File tmFile = new File(TEXTMARKER_BASE + "/script/" + TEXTMARKER_FILENAME + ".tm");
	// FileUtils.writeStringToFile(tmFile, aScript, "UTF-8");
	//
	// // Finally create the engine
	// return createPrimitive(TextMarkerEngine.class, merged, prios, TextMarkerEngine.MAIN_SCRIPT,
	// TEXTMARKER_FILENAME, TextMarkerEngine.SCRIPT_PATHS, new String[] { TEXTMARKER_BASE
	// + "/script/" }, TextMarkerEngine.RESOURCE_PATHS,
	// new String[] { TEXTMARKER_BASE + "/resources/" },
	// TextMarkerEngine.ADDITIONAL_SCRIPTS, new String[0],
	// TextMarkerEngine.ADDITIONAL_ENGINES, new String[0],
	// TextMarkerEngine.CREATE_STYLE_MAP, false,
	// // TextMarkerEngine.CREATE_DEBUG_INFO, true,
	// // TODO find out what this filter does; it seems to have no
	// // effect on the generated annotations?
	// TextMarkerEngine.DEFAULT_FILTERED_TYPES, new String[] { "de.uniwue.tm.type.SPACE",
	// "de.uniwue.tm.type.NBSP", "de.uniwue.tm.type.BREAK",
	// "de.uniwue.tm.type.MARKUP" });
	// }

	// TODO: do this in-memory, i.e. don't create a html file dump
	public String getHTML()
	{
		String output = "";
		try {
			output = FileUtils.readFileToString(output_html, "UTF-8");
		}
		catch (IOException e) {
			output = "An error occurred while trying to read the output html file.";
		}
		return output;
	}

	// TODO this is only a quick hack
	public String getColorMap(String[] markedTypes, String[] markerColors)
	{
		StringBuffer sb = new StringBuffer();
		int typeCount = Math.min(markerColors.length, markedTypes.length);

		// Map<String, String> colors = new HashMap<String, String>();
		for (int i = 0; i < typeCount; i++) {
			sb.append(markedTypes[i] + " is colored <span style=\"color:#DDDDDD; background-color:"
					+ markerColors[i] + ";\">" + markerColors[i] + "</span><br />");
		}

		return sb.toString();
	}

	public String getDump()
	{
		String output;
		try {
			output = FileUtils.readFileToString(output_dump, "UTF-8");
		}
		catch (IOException e) {
			output = "An error occurred while trying to read the analysis output file.";
		}
		return output;
	}
}
