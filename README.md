# PDTB-RST-Alignment

The alignment table includes 6758 rows, equivalent to the number of PDTB relations that have been aligned with an RST relation from the RST-DT corpus. Columns of this table provide some basic information about the source relation extracted from PDTB, the target relation extracted from RST-DT, and the quality of the alignment between the two. These three gropups of columns will be introduced in this document. The mapping between relation instances from PDTB to RST-DT is one-to-many. This means that each PDTB relation is uniquely mapped to one RST relation, however, a given RST relation might have been selected as the best fit to more than one PDTB relation, thus might appear in more than one row of the table as the matched relation. Suspicious alignments are flagged in the paper using the last group of columns.  Please refer to the paper for detailed information on the mapping procedure as well as discussions on suspicious alignments.

## PDTB columns
The following columns provide some attributes of the PDTB relation in each row of the table:

* **WSJID:** 
ID of the WSJ article, i.e., the text file within the Penn Treebank where the PDTB relation was annotated.


* **OrderedID:**
ID of the PDTB discourse relation with respect to the charachter offset of the earliest occuring argument.

* **Relation:**
Type of relation (Explicit/Implicit/EntRel/AltLex) according to PDTB high-level classification.

* **Conn1:**
Connective from the original text for Explicit relations, the alternative lexicalization for AltLex relations, or the connective inserted by PDTB annotators for Implicit relation. EntRel relations have EMPTY in this field.

* **Conn1Sense1:**
Relation sense 1 with regard to Conn1 according to PDTB annotation.

* **Conn1Sense2:**
Relation sense 2 if more than one relation was recognized for Conn1 according to PDTB annotation.

* **Conn2:**
For some implicit relations, PDTB annotators decided to provide two possible discourse connectives. For other types of relations, this field will be EMPTY.

* **Conn2Sense1:**
Relation sense 1 with regard to Conn2 according to PDTB annotation.

* **Conn2Sense2:**
Relation sense 2 if more than one relation was recognized for Conn2 according to PDTB annotation.

* **PDTB_Arg1Begin:**
The charachter offset of the begining of the first argument of the relation.

* **PDTB_Arg1End:**
The charachter offset of the end of the first argument of the relation.

* **PDTB_Arg1Text:**
The text content of the first argument.

* **PDTB_Arg2Begin:**
The charachter offset of the begining of the second argument of the relation.

* **PDTB_Arg2End:**
The charachter offset of the end of the second argument of the relation.

* **PDTB_Arg2Text:**
The text content of the second argument.

* **PDTB_Arg1SpansNo:**
The number of text spans (according to PDTB segmentations) included in the first argument.

* **PDTB_Arg1Spans:**
The text spans (according to PDTB segmentations) included in the first argument.

* **PDTB_Arg2SpansNo:**
The number of text spans (according to PDTB segmentations) included in the second argument.

* **PDTB_Arg2Spans:**
The text spans (according to PDTB segmentations) included in the second argument.

* **PDTB_ArgOverlap:**
Indicator for overlapping arguments within the PDTB relation: whether the two arguments covered any common charachters in the text.

## RST columns
The following columns provide some attributes of the RST relation matched with the PDTB relation in each row of the table:


* **RST_Arg1IsRelation:**
Indicator for complex argument: whether the RST span aligned with the first argument of the PDTB relation is an RST relation itself (TRUE) or just an elementary discourse unit (FALSE) according to RST definitions.

* **RST_Arg1Span:**
The RST span aligned with the first argument of the PDTB relation. It can be a single number (if one EDU is covered) or a range (if multiple consecutive EDUs are covered. Span numbering is according to RST segmentation.

* **RST_Arg1Begin:**
The charachter offset of the begining of the RST span matched with the first argument of the PDTB relation.

* **RST_Arg1End:**
The charachter offset of the end of the RST span matched with the first argument of the PDTB relation.

* **RST_Arg1Text:**
The text of the RST span matched with the first argument of the PDTB relation.

* **RST_Arg1Diff:**
Charachter difference between the text span of the first argument of the PDTB relation and the matched RST span.

* **RST_Arg2IsRelation:**
Indicator for complex argument: whether the RST span aligned with the second argument of the PDTB relation is an RST relation itself (TRUE) or just an elementary discourse unit (FALSE) according to RST definitions.

* **RST_Arg2Span:**
The RST span aligned with the second argument of the PDTB relation. It can be a single number (if one EDU is covered) or a range (if multiple consecutive EDUs are covered. Span numbering is according to RST segmentation.

* **RST_Arg2Begin:**
The charachter offset of the begining of the RST span matched with the second argument of the PDTB relation.

* **RST_Arg2End:**
The charachter offset of the end of the RST span matched with the second argument of the PDTB relation.

* **RST_Arg2Text:**
The text of the RST span matched with the second argument of the PDTB relation.

* **RST_Arg2Diff:**
Charachter difference between the text span of the second argument of the PDTB relation and the matched RST span.

* **RST_RelationSpan:**
The entire text span covered by the matched RST relation. This is a range in the form of (m,n) where n > m. Span numbering is according to RST segmentation.

* **RST_RelationName:**
The name (type/sense) of the matched RST relation. 


## Alignment qualification columns
The following columns provide some information about the quality of the alignment obtained for each PDTB relation. These include integer and boolian features. Integer features provide some information about the structure of the RST relational tree aligned with the PDTB relation and boolean features are indicators of possible alignment mistakes, i.e., suspicious cases that were flagged during the automatic alignment procedure for furthur manual investigation. All of these features are extensively and visually explained in the paper, thus we only provide a brief definition of them in this document.

* **arg1Sats:**
Number of intevening Satelite edges between the RST span considered as the match of the first argument of the PDTB relation and the root of the matched RST relation's tree. 

* **arg1Nucs:**
Number of intevening Nucleus edges between the RST span considered as the match of the first argument of the PDTB relation and the root of the matched RST relation's tree. 	

* **arg2Sats:**
Number of intevening Satelite edges between the RST span considered as the match of the second argument of the PDTB relation and the root of the matched RST relation's tree. 

* **arg2Nucs:**
Number of intevening Nucleus edges between the RST span considered as the match of the second argument of the PDTB relation and the root of the matched RST relation's tree. 	

* **Sats:**
Number of intevening Satelite edges between the two RST spans considered as PDTB relational argument matches. This number is obtained by traversing the matched RST relation's tree and is equal to the sum of arg1Sats and arg2Sats. 

* **Nucs:**
Number of intevening Satelite edges between the two RST spans considered as PDTB relational argument matches. This number is obtained by traversing the matched RST relation's tree and is equal to the sum of arg1Nucs and arg2Nucs. 

* **problemTraversing:**
If any of the above Sat and Nuc flags is a negative number it means that traversing the path between the two relational arguments had a problem. This happens due to a variety of reasons that are extensively explained in the paper.

* **extraSpans:**
The alignment procedure to find the RST relation that covers both RST spans (matched with the first and second PDTB relational arguments) is based centrally on the number of extra spans between the matched arguments. This numbers tells us how much extra text *had to* be included within the minimal tree. In a lot of alignments this number is zero, which means the RST relation looks very similar to the PDTB relation no intervening text spans between the two relational arguments). Alignments that have large number of extraSpans or a negative number in this column need to be examined manually. 

* **revise:**
If any of the above Sat and Nuc flags or extraSpans is a negative number then the alignment is flagged to be revised manually. This is indicative of various suspicious alignments that are extensively explained in the paper. Some of these alignments pass the manual checking test, and some do not.

* **strictRevise:**
This flag is specifically designated to additionally mark cases of alignment in which the *strict nuclearity principle* is likely violated; that is when there exists an intervening satelite edge in the path between the two RST spans matched with the two PDTB relational arguments. Please refer to the paper for definition of the neuclearity principle and how it should be handeled in a theoretical study.

* **multiNucleiEncounters:**
This flag is incremented during traversing the three (from one argument matching RST span to the other). It is the count of multi-nuclei nodes in the path between the arguments within the RST relation three.

* **multiNucleiRoot:**
This flag is set if the root of the RST relation tree is a multi-nuclei relation.

* **attrEncounters:**
This flag is incremented during traversing the three (from one argument matching RST span to the other). It is the count of attribution nodes in the path between the arguments within the RST relation three.

* **attrRoot:**
This flag is set if the root of the RST relation tree is an attribution relation.

* **sameunitTreatment:**
This flag is set if sameunit treatment is applied during the alignment procedure. Same-unit relations are specific to RST framework and are usually taken as one single text span in PDTB segmentation. In order to find the best matching RST relation that covers the two relational arguments we had to curate the algorithm to skip Same-unit relations. This procedure is discussed in the paper.





