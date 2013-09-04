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
package de.tudarmstadt.ukp.csniper.webapp.evaluation;

import static java.util.Collections.singleton;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitive;
import static org.apache.uima.fit.factory.TypeSystemDescriptionFactory.createTypeSystemDescription;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCreationUtils;
import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.DataWriter;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.jar.DirectoryDataWriterFactory;
import org.cleartk.classifier.jar.Train;

import com.google.common.io.Files;

import de.tudarmstadt.ukp.csniper.ml.DummySentenceSplitter;
import de.tudarmstadt.ukp.csniper.ml.GoldFromMetadataAnnotator;
import de.tudarmstadt.ukp.csniper.ml.TKSVMlightFeatureExtractor;
import de.tudarmstadt.ukp.csniper.ml.tksvm.DefaultTKSVMlightDataWriterFactory;
import de.tudarmstadt.ukp.csniper.ml.tksvm.TKSVMlightDataWriter;
import de.tudarmstadt.ukp.csniper.ml.tksvm.TKSVMlightSequenceClassifier;
import de.tudarmstadt.ukp.csniper.ml.tksvm.TKSVMlightSequenceClassifierBuilder;
import de.tudarmstadt.ukp.csniper.ml.tksvm.TreeFeatureVector;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.CachedParse;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationItem;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationResult;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.Mark;
import de.tudarmstadt.ukp.csniper.webapp.project.model.AnnotationType;
import de.tudarmstadt.ukp.csniper.webapp.search.tgrep.PennTreeUtils;
import de.tudarmstadt.ukp.csniper.webapp.statistics.SortableAggregatedEvaluationResultDataProvider.ResultFilter;
import de.tudarmstadt.ukp.csniper.webapp.statistics.model.AggregatedEvaluationResult;
import de.tudarmstadt.ukp.csniper.webapp.support.task.Task;
import de.tudarmstadt.ukp.csniper.webapp.support.uima.AnalysisEngineFactory;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;

public class MlPipeline
{
	private static Log LOG = LogFactory.getLog(MlPipeline.class);

	// private static final String LANGUAGE = "en";
	private static final Double THRESHOLD = 0.0;

	private String language;

	private AnalysisEngine gold;
	private AnalysisEngine sent;
	private AnalysisEngine tok;
	private AnalysisEngine parser;

	private EvaluationRepository repository;

	private Task task;

	public MlPipeline(String aLanguage)
		throws ResourceInitializationException
	{
		language = aLanguage;
		gold = createPrimitive(GoldFromMetadataAnnotator.class);
		sent = createPrimitive(DummySentenceSplitter.class);
		tok = AnalysisEngineFactory.createAnalysisEngine(
				AnalysisEngineFactory.SEGMENTER, "language", aLanguage, "createSentences",
				false);
		parser = AnalysisEngineFactory.createAnalysisEngine(
				AnalysisEngineFactory.PARSER, "language", aLanguage);
	}

	public void setRepostitory(EvaluationRepository aRepostitory)
	{
		repository = aRepostitory;
	}

	public void setTask(Task aTask)
	{
		task = aTask;
	}

	public String parse(EvaluationResult result, CAS cas)
		throws UIMAException
	{
		// get parse from db, or parse now
		String pennTree = "";
		CachedParse cp = repository.getCachedParse(result.getItem());
		if (cp != null && !cp.getPennTree().isEmpty()) {
			if ("ERROR".equals(cp.getPennTree())) {
				System.out.println("Unable to parse: [" + result.getItem().getCoveredText()
						+ "] (cached)");
				return "";
			}
			// write existing parse to cas for extraction
			pennTree = cp.getPennTree();
			addPennTree(cas, cp.getPennTree());
		}
		else {
			parser.process(cas);
			try {
				pennTree = StringUtils.normalizeSpace(JCasUtil.selectSingle(cas.getJCas(),
						PennTree.class).getPennTree());
				repository.writeCachedParse(new CachedParse(result.getItem(), pennTree));
			}
			catch (IllegalArgumentException e) {
				System.out.println("Unable to parse: [" + result.getItem().getCoveredText() + "]");
				repository.writeCachedParse(new CachedParse(result.getItem(), "ERROR"));
			}
		}

		return pennTree;
	}

	public void createTrainingData(File aModelDir, List<EvaluationResult> aTrainingList)
		throws UIMAException, IOException
	{
		AnalysisEngine extract = createPrimitive(TKSVMlightFeatureExtractor.class,
				DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY, aModelDir.getAbsolutePath(),
				TKSVMlightFeatureExtractor.PARAM_DATA_WRITER_FACTORY_CLASS_NAME,
				DefaultTKSVMlightDataWriterFactory.class.getName());

		ProgressMeter progress = new ProgressMeter(aTrainingList.size());
		// extract features
		CAS cas = CasCreationUtils.createCas(createTypeSystemDescription(), null, null);
		for (EvaluationResult result : aTrainingList) {
			// add gold annotation
			DocumentMetaData.create(cas).setDocumentTitle(result.getResult());
			// set doc text
			cas.setDocumentText(result.getItem().getCoveredText());
			// set language
			cas.setDocumentLanguage(language);

			// convert gold annotations
			gold.process(cas);
			// preprocessing
			sent.process(cas);
			tok.process(cas);
			// get parse from db, or parse now
			parse(result, cas);
			// extract features
			extract.process(cas);
			cas.reset();
			progress.next();
			LOG.info(progress);
			if (task != null) {
				task.increment();
				task.checkCanceled();
			}
		}
		extract.collectionProcessComplete();
	}

	public void classify(File aModelDir, List<EvaluationResult> aToPredictList)
		throws IOException, UIMAException
	{
		TKSVMlightSequenceClassifierBuilder builder = new TKSVMlightSequenceClassifierBuilder();
		TKSVMlightSequenceClassifier classifier = builder
				.loadClassifierFromTrainingDirectory(aModelDir);
		File cFile = File.createTempFile("tkclassify", ".txt");

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(cFile));

			// predict unclassified
			CAS cas = CasCreationUtils.createCas(createTypeSystemDescription(), null, null);
			ProgressMeter progress = new ProgressMeter(aToPredictList.size());
			for (EvaluationResult result : aToPredictList) {
				cas.setDocumentText(result.getItem().getCoveredText());
				cas.setDocumentLanguage(language);

				// dummy sentence split
				sent.process(cas);

				// tokenize
				tok.process(cas);

				// get parse from db, or parse now
				String pennTree = parse(result, cas);
				
				// write tree to file
				Feature tree = new Feature("TK_tree", StringUtils.normalizeSpace(pennTree));
				TreeFeatureVector tfv = classifier.getFeaturesEncoder().encodeAll(
						Arrays.asList(tree));
				try {
					bw.write("0");
					bw.write(TKSVMlightDataWriter.createString(tfv));
					bw.write(SystemUtils.LINE_SEPARATOR);
				}
				catch (IOException e) {
					throw new AnalysisEngineProcessException(e);
				}
				cas.reset();
				progress.next();
				LOG.info(progress);
				if (task != null) {
					task.increment();
					task.checkCanceled();
				}
			}
		}
		finally {
			IOUtils.closeQuietly(bw);
		}

		// classify all
		List<Double> predictions = classifier.tkSvmLightPredict2(cFile);

		if (predictions.size() != aToPredictList.size()) {
			// TODO throw different exception instead
			throw new IOException("there are [" + predictions.size() + "] predictions, but ["
					+ aToPredictList.size() + "] were expected.");
		}

		for (int i = 0; i < aToPredictList.size(); i++) {
			Mark m = (predictions.get(i) > THRESHOLD) ? Mark.PRED_CORRECT : Mark.PRED_WRONG;
			aToPredictList.get(i).setResult(m.getTitle());
		}
	}

	public void predict(List<EvaluationResult> aTrainingList, List<EvaluationResult> aToPredictList)
		throws UIMAException, IOException
	{
		if (aTrainingList.size() == 0) {
			return;
		}

		if (task != null) {
			task.setTotal(aTrainingList.size() + aToPredictList.size());
		}

		// create temp dir for model files
		File modelDir = Files.createTempDir();
		createTrainingData(modelDir, aTrainingList);

		// train model
		try {
			Train.main(modelDir.getPath(), "-t", "5", "-c", "1.0", "-C", "+");
		}
		catch (Exception e) {
			throw new UIMAException(e);
		}

		// classify
		classify(modelDir, aToPredictList);
	}

	public boolean predict(List<EvaluationResult> aResults, int minItemsAssessed)
		throws UIMAException, IOException
	{
		// split results in assessed and empty
		List<EvaluationResult> assessed = new ArrayList<EvaluationResult>();
		List<EvaluationResult> empty = new ArrayList<EvaluationResult>();

		for (EvaluationResult result : aResults) {
			Mark m = Mark.fromString(result.getResult());
			switch (m) {
			case CORRECT:
			case WRONG:
				assessed.add(result);
				break;
			case NA:
			case PRED_CORRECT:
			case PRED_WRONG:
				empty.add(result);
				break;
			default:
				// CHECK
				break;
			}
		}

		// exit, if not enough items have been assessed
		// TODO differentiate between correct/wrong?
		// i.e. ensure the user to at least have X correct and X wrong items before predicting?
		// a classifier trained only on "correct"s will not issue "wrong"s for anything, etc.
		if (assessed.size() < minItemsAssessed) {
			return false;
		}
		predict(assessed, empty);

		return true;
	}

	public boolean predictAggregated(List<EvaluationResult> aResults, String aCollectionId,
			AnnotationType aType, Set<String> aUsers, double aUserThreshold,
			double aConfidenceThreshold)
		throws UIMAException, IOException
	{
		// get aggregated results
		List<AggregatedEvaluationResult> aggregatedResults = repository.listAggregatedResults(
				singleton(aCollectionId), singleton(aType), aUsers, aUserThreshold,
				aConfidenceThreshold);

		if (aggregatedResults.isEmpty()) {
			return false;
		}

		// create training list
		List<EvaluationResult> trainingList = convertToSimple(aggregatedResults);

		// create toPredict list
		List<EvaluationResult> toPredict = new ArrayList<EvaluationResult>();
		for (EvaluationResult er : aResults) {
			Mark result = Mark.fromString(er.getResult());
			if (result != Mark.CORRECT && result != Mark.WRONG) {
				toPredict.add(er);
			}
		}

		predict(trainingList, toPredict);

		return true;
	}

	private void addPennTree(CAS aCas, String aPennTree)
		throws CASException
	{
		PennTree tree = new PennTree(aCas.getJCas(), 0, aCas.getDocumentText().length());
		tree.setPennTree(aPennTree);
		tree.addToIndexes();
	}

	public static List<EvaluationResult> convertToSimple(List<AggregatedEvaluationResult> aAgg)
	{
		// create training list
		List<EvaluationResult> trainingList = new ArrayList<EvaluationResult>();
		for (AggregatedEvaluationResult aer : aAgg) {
			ResultFilter aggregated = aer.getClassification();
			if (aggregated == ResultFilter.CORRECT || aggregated == ResultFilter.WRONG) {
				trainingList.add(new EvaluationResult(aer.getItem(), "__dummy__", aggregated
						.getLabel()));
			}
		}

		return trainingList;
	}
	
    public static File train(List<EvaluationResult> aTrainingList, EvaluationRepository aRepository)
        throws IOException, CleartkProcessingException
    {
        File modelDir = Files.createTempDir();
        DefaultTKSVMlightDataWriterFactory dataWriterFactory = new DefaultTKSVMlightDataWriterFactory();
        dataWriterFactory.setOutputDirectory(modelDir);
        DataWriter<Boolean> dataWriter = dataWriterFactory.createDataWriter();
        
        for (EvaluationResult result : aTrainingList) {
            CachedParse cp = aRepository.getCachedParse(result.getItem());
            if (cp == null || cp.getPennTree().isEmpty() || "ERROR".equals(cp.getPennTree())) {
                System.out.println("Unable to parse: [" + result.getItem().getCoveredText()
                        + "] (cached)");
                continue;
            }
            
            Instance<Boolean> instance = new Instance<Boolean>();
            instance.add(new Feature("TK_tree", StringUtils.normalizeSpace(cp.getPennTree())));
            instance.setOutcome(Mark.fromString(result.getResult()) == Mark.CORRECT);
            dataWriter.write(instance);
        }

        dataWriter.finish();

        // train model
        try {
            Train.main(modelDir.getPath(), "-t", "5", "-c", "1.0", "-C", "+");
        }
        catch (Exception e) {
            throw new CleartkProcessingException(e);
        }
        
        return modelDir;
	}
    
    /**
     * Mind this method may return less results than parses were passed to it, e.g. because a 
     * cached parse may be empty or "ERROR" in which case no result for it is generated!
     */
    public static List<EvaluationResult> classifyPreParsed(File aModelDir, List<CachedParse> aParses,
            String aType, String aUser)
        throws IOException, UIMAException
    {
        TKSVMlightSequenceClassifierBuilder builder = new TKSVMlightSequenceClassifierBuilder();
        TKSVMlightSequenceClassifier classifier = builder
                .loadClassifierFromTrainingDirectory(aModelDir);
        File cFile = File.createTempFile("tkclassify", ".txt");

        List<EvaluationItem> items = new ArrayList<EvaluationItem>();
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(cFile));

            for (CachedParse parse : aParses) {
                if (parse.getPennTree().isEmpty() || "ERROR".equals(parse.getPennTree())) {
                    continue;
                }
                
                String coveredText;
                try {
                    coveredText = PennTreeUtils.toText(parse.getPennTree());
                }
                catch (EmptyStackException e) {
                    LOG.error("Invalid Penn Tree: ["+parse.getPennTree()+"]", e);
                    continue;
                }
                
                // Prepare evaluation item to return
                EvaluationItem item = new EvaluationItem();
                item.setType(aType);
                item.setBeginOffset(parse.getBeginOffset());
                item.setEndOffset(parse.getEndOffset());
                item.setDocumentId(parse.getDocumentId());
                item.setCollectionId(parse.getCollectionId());
                item.setCoveredText(coveredText);
                items.add(item);
                
                // write tree to file
                Feature tree = new Feature("TK_tree", StringUtils.normalizeSpace(parse.getPennTree()));
                TreeFeatureVector tfv = classifier.getFeaturesEncoder().encodeAll(
                        Arrays.asList(tree));
                
                bw.write("0");
                bw.write(TKSVMlightDataWriter.createString(tfv));
                bw.write(SystemUtils.LINE_SEPARATOR);
            }
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
        finally {
            IOUtils.closeQuietly(bw);
        }

        // classify all
        List<Double> predictions = classifier.tkSvmLightPredict2(cFile);

        if (predictions.size() != items.size()) {
            // TODO throw different exception instead
            throw new IOException("there are [" + predictions.size() + "] predictions, but ["
                    + items.size() + "] were expected.");
        }

        List<EvaluationResult> results = new ArrayList<EvaluationResult>();
        for (EvaluationItem item : items) {
            results.add(new EvaluationResult(item, aUser, ""));
        }
        
        for (int i = 0; i < results.size(); i++) {
            Mark m = (predictions.get(i) > THRESHOLD) ? Mark.PRED_CORRECT : Mark.PRED_WRONG;
            results.get(i).setResult(m.getTitle());
        }
        
        return results;
    }
}
