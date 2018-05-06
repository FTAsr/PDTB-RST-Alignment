# PDTB-RST-Alignment

The alignment table includes 6758 rows, equivalent to the number of PDTB relations that have been aligned with an RST relation from the RST-DT corpus.
Columns of this table provide some basic information about the source relation extracted from PDTB, the target relation extracted from RST-DT, and finally the alignment between the two such as the charachter differences between the aligned spans.
The mapping between relation instances from PDTB to RST-DT is one-to-many. This means that each PDTB relation is uniquely mapped to one RST relation, however, a given RST relation might have been selected as the best fit to more than one PDTB relation. For more information on the aligning procedure, please refer to the paper.

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

