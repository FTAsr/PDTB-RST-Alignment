# PDTB-RST-Alignment

The alignment table includes 6758 rows, equivalent to the number of PDTB relations that have been aligned with an RST relation from the RST-DT corpus.
Columns of this table provide some basic information about the source relation extracted from PDTB, the target relation extracted from RST-DT, and finally the alignment between the two such as the charachter differences between the aligned spans.
The mapping between relation instances from PDTB to RST-DT is one-to-many. This means that each PDTB relation is uniquely mapped to one RST relation, however, a given RST relation might have been selected as the best fit to more than one PDTB relation. For more information on the aligning procedure, please refer to the paper.

## PDTB columns
The following columns provide some attributes of the PDTB relation in each row of the table:

WSJID
ID of the WSJ article, i.e., the text file within the Penn Treebank where the PDTB relation was annotated.

OrderedID
ID of the PDTB discourse relation with respect to the charachter offset of the earliest occuring argument.

Relation
Type of relation (Explicit/Implicit/EntRel/AltLex) according to PDTB high-level classification.

Conn1Sense1
Relation sense 1 according to PDTB annotation.

Conn1Sense2
Relation sense 2 according to PDTB annotation.

Conn1
Connective from the original text for Explicit relations, the alternative lexicalization for AltLex relations, or the connective inserted by PDTB annotators for Implicit relation. EntRel relations have EMPTY in this field.

Conn2Sense1

Conn2Sense2

Conn2

PDTB_Arg1Begin

PDTB_Arg1End

PDTB_Arg1Text

PDTB_Arg2Begin

PDTB_Arg2End

PDTB_Arg2Text

PDTB_Arg1SpansNo

PDTB_Arg1Spans

PDTB_Arg2SpansNo

PDTB_Arg2Spans

PDTB_ArgOverlap
