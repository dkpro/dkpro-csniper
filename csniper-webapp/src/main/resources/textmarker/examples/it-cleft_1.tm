WORDLIST CleftStoppers = 'CleftStoppers.txt';
DECLARE CleftStopper, CleftConstituent, CleftClause;

Document{-> MARKFAST(CleftStopper, CleftStoppers)};

"It" de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma{FEATURE("value","be")} de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.NP de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.SBAR{-> MARK(CleftConstituent,3,4)};
"It" de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma{FEATURE("value","be")} de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.NP de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.SBAR{-> MARK(CleftClause)};