---
layout: page-fullwidth
title: "Conversion Guide"
permalink: "/documentation/conversion/"
---

<div class="row">
<div class="medium-4 medium-push-8 columns" markdown="1">
<div class="panel radius" markdown="1">
**Content**
{: #toc }
*  TOC
{:toc}
</div>
</div><!-- /.medium-4.columns -->

<div class="medium-8 medium-pull-4 columns" markdown="1">

CSniper requires the corpora/data to be used with it to be in a special binary format for fast search. See below for examples on how to convert your data.

## Corpus format and structure

The main query engine for CSniper is CQP, the Corpus Query Processor from the IMS Corpus Workbench [http://cwb.sourceforge.net](http://cwb.sourceforge.net). Thus, corpora which shall be used by CSniper have to be converted to the CQP format. For relatively fast context delivery, serialized CASes are used.

The corpora directory structure being used by CSniper is as follows:

	corpora/
		registry/
			corpus1
			corpus2
			...
		CORPUS1/
			corpus.properties
				format1/
					corpus-files for engine 1
					...
				format2/
					corpus-files for engine 2
					...
		CORPUS2/
			...

At the moment, a *format* can be, e.g., cqp (mandatory), bin (used for context display) or tgrep (to search on parse trees rather than token/lemmas/POS tags). Each corpus directory has to contain a corpus.properties file, an example of which can be seen further below.

## Example conversion

Let's say we have a corpus in the NEGRA export format (a sample can be found here: [http://www.coli.uni-saarland.de/projects/sfb378/negra-corpus/corpus-sample.export](http://www.coli.uni-saarland.de/projects/sfb378/negra-corpus/corpus-sample.export)).

To convert it, we'll write a short conversion program:

	#!/usr/bin/env groovy
	@Grab(
		group='de.tudarmstadt.ukp.dkpro.core', 
		module='de.tudarmstadt.ukp.dkpro.core.io.bincas-asl', 
		version='1.5.0')
	@Grab(
		group='de.tudarmstadt.ukp.dkpro.core', 
		module='de.tudarmstadt.ukp.dkpro.core.io.negra-asl', 
		version='1.5.0')
	@Grab(
		group='de.tudarmstadt.ukp.dkpro.core', 
		module='de.tudarmstadt.ukp.dkpro.core.io.imscwb-asl', 
		version='1.5.0')

	import org.uimafit.pipeline.SimplePipeline;
	import de.tudarmstadt.ukp.dkpro.core.io.bincas.SerializedCasWriter;
	import de.tudarmstadt.ukp.dkpro.core.io.imscwb.ImsCwbWriter;
	import de.tudarmstadt.ukp.dkpro.core.io.negra.NegraExportReader;
	import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionMethod;
	import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
	import static org.uimafit.factory.CollectionReaderFactory.createDescription;

	// Collection ID
	def id = args[0]

	// Source file (e.g. tuebadz-5.0.anaphora.export.bz2)
	def source = args[1]

	// Target folder
	def target = args[2];

	SimplePipeline.runPipeline(
		createDescription(NegraExportReader.class,
		    NegraExportReader.PARAM_SOURCE_LOCATION, source,
		    NegraExportReader.PARAM_COLLECTION_ID, id,
		    NegraExportReader.PARAM_LANGUAGE, "de",
		    NegraExportReader.PARAM_ENCODING, "ISO-8859-15",
		    NegraExportReader.PARAM_READ_PENN_TREE, true),

		createPrimitiveDescription(SerializedCasWriter.class,
		    SerializedCasWriter.PARAM_PATH, target + "/bin",
		    SerializedCasWriter.PARAM_USE_DOCUMENT_ID, true,
		    SerializedCasWriter.PARAM_COMPRESSION, CompressionMethod.XZ),

		createPrimitiveDescription(ImsCwbWriter.class,
		    ImsCwbWriter.PARAM_TARGET_ENCODING, "UTF-8",
		    ImsCwbWriter.PARAM_TARGET_LOCATION, target + "/cqp",
		    ImsCwbWriter.PARAM_WRITE_TEXT_TAG, true,
		    ImsCwbWriter.PARAM_WRITE_DOCUMENT_TAG, true,
		    ImsCwbWriter.PARAM_WRITE_OFFSETS, true,
		    ImsCwbWriter.PARAM_WRITE_LEMMA, true,
		    ImsCwbWriter.PARAM_WRITE_DOC_ID, false));

Save it as `convert-sample.groovy` and run it, using: `groovy convert-sample sample corpus-sample.export SAMPLE`.

We're presented with a directory `SAMPLE`, with two subdirectories `bin` and `cqp`. Copy `SAMPLE` to your corpora directory. Now we have to do some post-processing:
First create a `corpus.properties` below `SAMPLE`, containing these lines:

	name=NEGRA sample
	description=Just an example corpus.
	language=de

Then move the registry directory from `/srv/csniper/corpora/SAMPLE/cqp/registry` to `/srv/csniper/corpora/registry`.
You have to edit the `/srv/csniper/corpora/registry/sample` file, changing the following lines:

	# path to binary data files
	HOME target/SAMPLE/cqp/data

to

	# path to binary data files
	HOME /srv/csniper/corpora/SAMPLE/cqp

At last, move all files from `/srv/csniper/corpora/SAMPLE/cqp/data` to `/srv/csniper/corpora/SAMPLE/cqp`, and remove the now empty data directory.

Note: If a corpus is not found in CSniper, make sure the corpus name is in all uppercase letters in your directory structure and in the `HOME ...` line in the corresponding registry file.
Also make sure your application server has file access to the corpora directory and all its subdirectories.

## Tested corpora

We have tested CSniper with the following corpora

|| *Corpus* || *Reader* || *Features* || *Comment* ||
|| [British National Corpus](http://www.natcorp.ox.ac.uk) || BncReader || POS, CPOS ||  ||
|| [deWaC](http://wacky.sslmit.unibo.it/doku.php?id=corpora) || ImsCwbReader || POS, lemma || tested with the first deWaC data file (~ 1/20th) ||
|| [TüBa D/Z](http://www.sfs.uni-tuebingen.de/en/ascl/resources/corpora/tueba-dz.html) ||  NegraExportReader || POS, parse trees || tested with version 5, contains no lemmas ||
|| [TIGER Corpus](http://www.ims.uni-stuttgart.de/forschung/ressourcen/korpora/tiger.html) || NegraExportReader || POS, parse trees, (lemma?) || tested with version 2 and 2.1 ||
|| [TextGrid Digitale Bibliothek](http://www.textgrid.de/ueber-textgrid/digitale-bibliothek/) || TEIReader || || ||

Many of the parse trees from TüBa D/Z and Tiger can currently not be imported, because the corpora use POS tags such as "$(" which are not compatible with the bracketed parse tree representation used by TGrep. 

## Conversion scripts

we supply here a couple of conversion scripts written in groovy. There is nothing required besides a [groovy installation](http://groovy.codehaus.org) - the script will fetch all needed dependencies on its own. After installing groovy simply paste the script into a file, make the file executable, and run it. The scripts all take three arguments:

- **corpus ID**: an ID you choose, no spaces, slashes or backslashes (e.g. "tueba5")
- **source**: the source file
- **target**: a folder into which the output is generated

If you name your file e.g. convert-tueba5.groovy, run it as
 
	groovy convert-tueba5 tueba5 tuebadz-5.0.anaphora.export.bz2 tueba5

### Conversion script - TüBa D/Z 5

	#!/usr/bin/env groovy
	@Grab(
		group='de.tudarmstadt.ukp.dkpro.core', 
		module='de.tudarmstadt.ukp.dkpro.core.io.bincas-asl', 
		version='1.5.0')
	@Grab(
		group='de.tudarmstadt.ukp.dkpro.core', 
		module='de.tudarmstadt.ukp.dkpro.core.io.negra-asl', 
		version='1.5.0')
	@Grab(
		group='de.tudarmstadt.ukp.dkpro.core', 
		module='de.tudarmstadt.ukp.dkpro.core.io.tgrep-gpl',
		version='1.5.0')
	@Grab(
		group='de.tudarmstadt.ukp.dkpro.core', 
		module='de.tudarmstadt.ukp.dkpro.core.io.imscwb-asl', 
		version='1.5.0')

	import org.uimafit.pipeline.SimplePipeline;
	import de.tudarmstadt.ukp.dkpro.core.io.bincas.SerializedCasWriter;
	import de.tudarmstadt.ukp.dkpro.core.io.imscwb.ImsCwbWriter;
	import de.tudarmstadt.ukp.dkpro.core.io.negra.NegraExportReader;
	import de.tudarmstadt.ukp.dkpro.core.io.tgrep.TGrepWriter;
	import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionMethod;
	import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
	import static org.uimafit.factory.CollectionReaderFactory.createDescription;

	// Collection ID
	def id = args[0]

	// Source file (e.g. tuebadz-5.0.anaphora.export.bz2)
	def source = args[1]

	// Target folder
	def target = args[2];

	SimplePipeline.runPipeline(
		createDescription(NegraExportReader.class,
		    NegraExportReader.PARAM_SOURCE_LOCATION, source,
		    NegraExportReader.PARAM_COLLECTION_ID, id,
		    NegraExportReader.PARAM_LANGUAGE, "de",
		    NegraExportReader.PARAM_ENCODING, "ISO-8859-15",
		    NegraExportReader.PARAM_READ_PENN_TREE, true),

		createPrimitiveDescription(SerializedCasWriter.class,
		    SerializedCasWriter.PARAM_PATH, target + "/bin",
		    SerializedCasWriter.PARAM_USE_DOCUMENT_ID, true,
		    SerializedCasWriter.PARAM_COMPRESSION, CompressionMethod.XZ),
		
		createPrimitiveDescription(TGrepWriter.class,
		    TGrepWriter.PARAM_TARGET_LOCATION, target + "/tgrep",
		    TGrepWriter.PARAM_COMPRESSION, CompressionMethod.GZIP,
		    TGrepWriter.PARAM_DROP_MALFORMED_TREES, true,
		    TGrepWriter.PARAM_WRITE_COMMENTS, true,
		    TGrepWriter.PARAM_WRITE_T2C, true),
		
		createPrimitiveDescription(ImsCwbWriter.class,
		    ImsCwbWriter.PARAM_TARGET_ENCODING, "UTF-8",
		    ImsCwbWriter.PARAM_TARGET_LOCATION, target + "/cqp",
		    ImsCwbWriter.PARAM_WRITE_TEXT_TAG, true,
		    ImsCwbWriter.PARAM_WRITE_DOCUMENT_TAG, true,
		    ImsCwbWriter.PARAM_WRITE_OFFSETS, true,
		    ImsCwbWriter.PARAM_WRITE_LEMMA, true,
		    ImsCwbWriter.PARAM_WRITE_DOC_ID, false));

### Conversion script - TIGER

	#!/usr/bin/env groovy
	@Grab(
		group='de.tudarmstadt.ukp.dkpro.core', 
		module='de.tudarmstadt.ukp.dkpro.core.io.bincas-asl', 
		version='1.5.0')
	@Grab(
		group='de.tudarmstadt.ukp.dkpro.core', 
		module='de.tudarmstadt.ukp.dkpro.core.io.negra-asl', 
		version='1.5.0')
	@Grab(
		group='de.tudarmstadt.ukp.dkpro.core', 
		module='de.tudarmstadt.ukp.dkpro.core.io.tgrep-gpl',
		version='1.5.0')
	@Grab(
		group='de.tudarmstadt.ukp.dkpro.core', 
		module='de.tudarmstadt.ukp.dkpro.core.io.imscwb-asl', 
		version='1.5.0')

	import org.apache.uima.collection.CollectionReaderDescription;
	import org.uimafit.pipeline.SimplePipeline;
	import de.tudarmstadt.ukp.dkpro.core.io.bincas.SerializedCasWriter;
	import de.tudarmstadt.ukp.dkpro.core.io.imscwb.ImsCwbWriter;
	import de.tudarmstadt.ukp.dkpro.core.io.negra.NegraExportReader;
	import de.tudarmstadt.ukp.dkpro.core.io.tgrep.TGrepWriter;
	import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionMethod;
	import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
	import static org.uimafit.factory.CollectionReaderFactory.createDescription;

	// Collection ID
	def id = args[0]

	// Source file (e.g. tigercorpus2.1/corpus/tiger_release_aug07.export)
	def source = args[1]

	// Target folder
	def target = args[2];

	SimplePipeline.runPipeline(
		createDescription(NegraExportReader.class,
		    NegraExportReader.PARAM_SOURCE_LOCATION, source,
		    NegraExportReader.PARAM_COLLECTION_ID, id,
		    NegraExportReader.PARAM_LANGUAGE, "de",
		    NegraExportReader.PARAM_ENCODING, "ISO-8859-15",
		    NegraExportReader.PARAM_GENERATE_NEW_IDS, true,
		    NegraExportReader.PARAM_READ_PENN_TREE, true,
		    NegraExportReader.PARAM_DOCUMENT_UNIT, NegraExportReader.DocumentUnit.ORIGIN_NAME),

		createPrimitiveDescription(SerializedCasWriter.class,
		    SerializedCasWriter.PARAM_PATH, target + "/bin",
		    SerializedCasWriter.PARAM_USE_DOCUMENT_ID, true,
		    SerializedCasWriter.PARAM_COMPRESSION, CompressionMethod.XZ),
		
		createPrimitiveDescription(TGrepWriter.class,
		    TGrepWriter.PARAM_TARGET_LOCATION, target + "/tgrep",
		    TGrepWriter.PARAM_COMPRESSION, CompressionMethod.GZIP,
		    TGrepWriter.PARAM_DROP_MALFORMED_TREES, true,
		    TGrepWriter.PARAM_WRITE_COMMENTS, true,
		    TGrepWriter.PARAM_WRITE_T2C, true),
		
		createPrimitiveDescription(ImsCwbWriter.class,
		    ImsCwbWriter.PARAM_TARGET_ENCODING, "UTF-8",
		    ImsCwbWriter.PARAM_TARGET_LOCATION, target + "/cqp",
		    ImsCwbWriter.PARAM_WRITE_TEXT_TAG, true,
		    ImsCwbWriter.PARAM_WRITE_DOCUMENT_TAG, true,
		    ImsCwbWriter.PARAM_WRITE_OFFSETS, true,
		    ImsCwbWriter.PARAM_WRITE_LEMMA, true,
		    ImsCwbWriter.PARAM_WRITE_DOC_ID, false));

### Conversion script - Digitale Bibliothek

	#!/usr/bin/env groovy
	@Grab(
		group='de.tudarmstadt.ukp.dkpro.core', 
		module='de.tudarmstadt.ukp.dkpro.core.io.tei-asl', 
		version='1.5.0')
	@Grab(
		group='de.tudarmstadt.ukp.dkpro.core', 
		module='de.tudarmstadt.ukp.dkpro.core.io.bincas-asl', 
		version='1.5.0')
	@Grab(
		group='de.tudarmstadt.ukp.dkpro.core', 
		module='de.tudarmstadt.ukp.dkpro.core.io.imscwb-asl', 
		version='1.5.0')

	import org.uimafit.pipeline.SimplePipeline;
	import de.tudarmstadt.ukp.dkpro.core.io.bincas.SerializedCasWriter;
	import de.tudarmstadt.ukp.dkpro.core.io.imscwb.ImsCwbWriter;
	import de.tudarmstadt.ukp.dkpro.core.io.tei.TEIReader;
	import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionMethod;
	import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
	import static org.uimafit.factory.CollectionReaderFactory.createDescription;

	// Collection ID
	def id = args[0]

	// Source file (e.g. digitale-bibliothek/literatur-nur-texte-1.zip)
	def source = args[1]

	// Target folder
	def target = args[2];

	SimplePipeline.runPipeline(
		createDescription(TEIReader.class, 
		    TEIReader.PARAM_PATH, "jar:file:/" + source + "!",
		    TEIReader.PARAM_PATTERNS, [ { "[+]**/*.xml" } ],
		    TEIReader.PARAM_USE_FILENAME_ID, true,
		    TEIReader.PARAM_LANGUAGE, "de"),

		createPrimitiveDescription(SerializedCasWriter.class,
		    SerializedCasWriter.PARAM_PATH, target + "/bin",
		    SerializedCasWriter.PARAM_USE_DOCUMENT_ID, true,
		    SerializedCasWriter.PARAM_COMPRESSION, CompressionMethod.XZ),
		
		createPrimitiveDescription(ImsCwbWriter.class,
		    ImsCwbWriter.PARAM_TARGET_ENCODING, "UTF-8",
		    ImsCwbWriter.PARAM_TARGET_LOCATION, target + "/cqp",
		    ImsCwbWriter.PARAM_WRITE_TEXT_TAG, true,
		    ImsCwbWriter.PARAM_WRITE_DOCUMENT_TAG, true,
		    ImsCwbWriter.PARAM_WRITE_OFFSETS, true,
		    ImsCwbWriter.PARAM_WRITE_LEMMA, true,
		    ImsCwbWriter.PARAM_WRITE_DOC_ID, false));

</div><!-- /.medium-8.columns -->
</div><!-- /.row -->
