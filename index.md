---
#
# Use the widgets beneath and the content will be
# inserted automagically in the webpage. To make
# this work, you have to use › layout: frontpage
#
layout: page
title: ""
---

CSniper (Corpus Sniper) is a tool that implements

1. a web-based multi-user scenario for identifying and annotating linguistic phenomena (e.g. non-canonical grammatical constructions) in large corpora, based on linguistic search queries and 
2. evaluation of annotation quality by measuring inter-rater agreement. 

This annotation-by-query approach efficiently harnesses expert knowledge to identify instances of linguistic phenomena that are hard to identify by means of existing purely automatic annotation tools. In addition, CSniper uses inbuilt machine learning mechanism (using an SVM with tree-kernel) to rank search results, facilitating the annotation process.

![Sentence based Annotation in CSniper](images/search.png)

How to cite
-----------

If you use *CSniper* in scientific work, please cite

> Eckart de Castilho, R., Bartsch, S., and Gurevych, I. (2012). **CSniper - annotation-by-query for non-canonical constructions in large corpora.** In *Proceedings of the ACL 2012 System Demonstrations*, pages 85–90, Jeju Island, Korea. Association for Computational Linguistics.
[(pdf)][1] [(bib)][2]

If you are referring to the automatic annotation capabilities, please cite CSniper as:

> Do Dinh, E. and Eckart de Castilho, R. and Gurevych, I. (2015). **In-tool Learning for Selective Manual Annotation in Large Corpora**. In *Proceedings of the ACL 2015 System Demonstrations*, to be published, Beijing, China. Association for Computational Linguistics.
[(pdf)][3] [(bib)][4]

License
-------

CSniper is licensed under the [Apache Software License (ASL) version 2][5].


About CSniper
-------------

This project is being developed by the Ubiquitous Knowledge Processing Lab (UKP) at the Technische Universität Darmstadt, Germany under the auspices of Prof. Dr. Iryna Gurevych.

[1]: https://www.ukp.tu-darmstadt.de/fileadmin/user_upload/Group_UKP/OIAF4HLT2014DKProCore_cameraready.pdf
[2]: https://www.ukp.tu-darmstadt.de/publications/details/?no_cache=1&tx_bibtex_pi1%5Bpub_id%5D=TUD-CS-2014-0864&type=99&tx_bibtex_pi1%5Bbibtex%5D=yes
[3]: https://www.ukp.tu-darmstadt.de/fileadmin/user_upload/Group_UKP/publikationen/2015/csniper_acl_camera.pdf
[4]: https://www.ukp.tu-darmstadt.de/publications/details/?no_cache=1&L=..%3Ftx_bibtex_pi1%5Bpub_id%5D%3DTUD-CS-2013-0259&tx_bibtex_pi1%5Bpub_id%5D=TUD-CS-2015-0098&type=99&tx_bibtex_pi1%5Bbibtex%5D=yes
[5]: http://www.apache.org/licenses/LICENSE-2.0

