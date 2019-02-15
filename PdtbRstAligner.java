package sb.coli.consumer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import sb.coli.types.pdtb.PdtbArg;
import sb.coli.types.pdtb.PdtbConnective;
import sb.coli.types.pdtb.PdtbRelation;
import sb.coli.types.pdtb.PdtbSpan;
import sb.coli.types.rst.RstAnnotation;
import sb.coli.types.rst.RstRelation;
import sb.coli.util.UimaUtils;


public class PdtbRstAligner extends JCasAnnotator_ImplBase {
	public static final String PARAM_TARGET_LOCATION = "targetLocation";
	private String targetLocation;
	private int allPDTBRelations;
	private int allRSTRelations;

	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		targetLocation = (String) context.getConfigParameterValue(PARAM_TARGET_LOCATION);
		allPDTBRelations = 0;
		allRSTRelations = 0;
		
		StringBuffer output = new StringBuffer ("");
		output.append("WSJID\t"
				+ "OrderedID\t"
				+ "Relation\t"
				+ "Conn1Sense1\t" + "Conn1Sense2\t" + "Conn1\t"
				+ "Conn2Sense1\t" + "Conn2Sense2\t" + "Conn2\t"
				+ "PDTB_Arg1Begin\t"+ "PDTB_Arg1End\t" + "PDTB_Arg1Text\t"
				+ "PDTB_Arg2Begin\t"+ "PDTB_Arg2End\t" + "PDTB_Arg2Text\t"
				+ "PDTB_Arg1SpansNo\t" + "PDTB_Arg1Spans\t" 
				+ "PDTB_Arg2SpansNo\t" + "PDTB_Arg2Spans\t"
				+ "PDTB_ArgOverlap\t"
				
				+ "RST_Arg1IsRelation\t" + "RST_Arg1Span\t" + "RST_Arg1Diff\t" 
				+ "RST_Arg1Begin\t" + "RST_Arg1End\t" + "RST_Arg1Text\t" 

				+ "RST_Arg2IsRelation\t" + "RST_Arg2Span\t" + "RST_Arg2Diff\t" 
				+ "RST_Arg2Begin\t" + "RST_Arg2End\t" + "RST_Arg2Text\t"
				
				+ "RST_RelationSpan\t" + "ExtraSpans\t" 
				+ "Sats\t" + "Nucs\t"
				+ "arg1Sats\t" + "arg1Nucs\t"
				+ "arg2Sats\t" + "arg2Nucs\t"
				+ "RST_RelationName\t" 
				+ "revise\t" + "strictRevise\t" 
				+ "multiNucleiEncounters\t" + "multiNucleiRoot\t"
				+ "attrEncounters\t" + "attrRoot\t"
				+ "problemTraversing\t" 
				+ "sameunitTreatment\t"
				+ "pdtbRelAttribution\n"
				);
		String workingDir = System.getProperty("user.dir");
		System.out.println("Current working directory : " + workingDir);
		String outFileAddress = workingDir + "/" + targetLocation  +  "/alignmentData/allAlignments.csv";
		System.out.println("File to be written:" + outFileAddress);
		File targetFile = new File(outFileAddress);
		File parent = targetFile.getParentFile();
		if (!parent.exists() && !parent.mkdirs()) {
			throw new IllegalStateException("Cannot create dir: " + parent);
		}
		BufferedWriter bwr;
		try {
			bwr = new BufferedWriter(new FileWriter(targetFile));
			bwr.write(output.toString());
		    bwr.flush();
		    bwr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		Collection<PdtbRelation> PdtbRelations = (Collection<PdtbRelation>) JCasUtil.select(aJCas, PdtbRelation.class);
		Collection<RstRelation> RstRelations = (Collection<RstRelation>) JCasUtil.select(aJCas, RstRelation.class);
		Collection<RstAnnotation> RstAnnotations = (Collection<RstAnnotation>) JCasUtil.select(aJCas, RstAnnotation.class);
		DocumentMetaData docMetaData = (DocumentMetaData) aJCas.getDocumentAnnotationFs();
		String fileName = docMetaData.getDocumentTitle();
		if(RstRelations.isEmpty() || PdtbRelations.isEmpty()){
			//System.err.println("Warning:: No PDTB or RST relations found for this document!");
			return;
		}
		allPDTBRelations = allPDTBRelations + PdtbRelations.size();
		allRSTRelations = allRSTRelations + RstRelations.size();
		int counter = 0;
		ArrayList<Alignment> alignments = new ArrayList<Alignment>();
		for (PdtbRelation relation : PdtbRelations) {
			//System.out.println("Working on the " + ++counter + "th PDTB relation " + relation.getOrderedId() + ": "+ relation.getRelation() );
			Alignment alignment =  aligningRstRelation(RstAnnotations, RstRelations, relation);
			alignments.add(alignment);
		}
		writeOneToTXT(fileName, alignments);
		addToEXCEL(fileName, alignments);
		return;

	}

	
	
	
	private void writeOneToTXT(String fileName, ArrayList<Alignment> alignments) {
		StringBuffer output = new StringBuffer ("");
		for( Alignment alignment : alignments){
			PdtbRelation relation = alignment.getRelation();
			String connective = "";
			String sense = "";
			String connective2 = "";
			String sense2 = "";
			if(!(relation.getRelation().equals("EntRel") || relation.getRelation().equals("NoRel")) ){
				PdtbConnective conn = ((PdtbConnective) ((FSList) relation.getConnectives()).getNthElement(0));
				connective = conn.getText();
				sense = (conn.getSemClass2()!="") ? conn.getSemClass1() + " & " + conn.getSemClass2() : conn.getSemClass1();
				// For implicit relations with two annotated connectives
				try{
					conn = ((PdtbConnective) ((FSList) relation.getConnectives()).getNthElement(1));
					connective2 =  conn.getText();
					sense2 = (conn.getSemClass2()!="") ? conn.getSemClass1() + " & " + conn.getSemClass2() : conn.getSemClass1();
				}catch(Exception e){
					//System.err.println("Cannot retrieve second connective annotation!");
				}
			}
			String outputRecord;
			if(alignment.getRelationEquivalent() == null)
				outputRecord = "PDTB relation: " + relation.getOrderedId() +" :: "+ relation.getRelation() +
						" :: " + sense + " -- " + connective + "/"  + sense2 + " -- " + connective2 +  "\n\t" +
						relation.getArg1().getBegin() + " -- " + relation.getArg1().getEnd() + "\n\t" +
						relation.getArg1().getCoveredText() + "\n\t" +
						relation.getArg2().getBegin() + " -- " + relation.getArg2().getEnd() + "\n\t" +
						relation.getArg2().getCoveredText() + "\n" +
						"has no equivalent.";
			else
				outputRecord = "PDTB relation: " + relation.getOrderedId() +" :: "+ relation.getRelation() + 
						" :: " + sense + " -- " + connective + "/"  + sense2 + " -- " + connective2 +  "\n\t" +
						relation.getArg1().getBegin() + " -- " + relation.getArg1().getEnd() + "\n\t" +
						relation.getArg1().getCoveredText() + "\n\t" +
						relation.getArg2().getBegin() + " -- " + relation.getArg2().getEnd() + "\n\t" +
						relation.getArg2().getCoveredText() + "\n\t  " +
						alignment.getPdtbArgsOverlap() + " PDTB arg overlap\n" +
						
					
					"RST span for arg1: "  + ( alignment.getArg1IsRelation() ?  ((RstRelation) alignment.getArg1Equivalent()).getSpan() : ((RstAnnotation) alignment.getArg1Equivalent()).getSpanId()  ) +
					" (with " + alignment.getArg1Diff() + " character difference)\n\t" +
					alignment.getArg1Equivalent().getBegin()  + " -- " + alignment.getArg1Equivalent().getEnd() + "\n\t" +
					alignment.getArg1Equivalent().getCoveredText() + "\n  " +
					"RST span for arg2: "  + ( alignment.getArg2IsRelation() ?  ((RstRelation) alignment.getArg2Equivalent()).getSpan() : ((RstAnnotation) alignment.getArg2Equivalent()).getSpanId()  ) +
					" (with " + alignment.getArg2Diff() + " character difference)\n\t" +
					 alignment.getArg2Equivalent().getBegin()  + " -- " + alignment.getArg2Equivalent().getEnd() + "\n\t" +
					 alignment.getArg2Equivalent().getCoveredText() + "\n  " +
					 "RST relation: "  + alignment.getRelationEquivalent().getSpan() +
					" (with " + alignment.getExtraSpans() + " extra span(s))\n\t" +
					alignment.getIntervSats() +  " Satellite edge(s)\n\t" +
					alignment.getIntervNucs() +  " Nucleus edge(s)\n\t" +
					alignment.getRelationEquivalent().getName() ;
						
			outputRecord =   outputRecord + "\n________________________\n";
			//System.out.println(outputRecord);
			output.append(outputRecord);
			
		}
			
		String workingDir = System.getProperty("user.dir");
		//System.out.println("Current working directory : " + workingDir);
		String outFileAddress = workingDir + "/" + targetLocation  +  "/alignmentData/"+ fileName +  ".txt";
		//System.out.println("File to be written:" + outFileAddress);
		File targetFile = new File(outFileAddress);
		File parent = targetFile.getParentFile();
		if (!parent.exists() && !parent.mkdirs()) {
			throw new IllegalStateException("Cannot create dir: " + parent);
		}
		BufferedWriter bwr;
		try {
			bwr = new BufferedWriter(new FileWriter(targetFile));
			bwr.write(output.toString());
		    bwr.flush();
		    bwr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}
		 
	private void addToEXCEL(String fileName, ArrayList<Alignment> alignments) {
		System.out.println("All PDTB relations in the overlapping parts: " + allPDTBRelations);
		System.out.println("All RST relations in the overlapping parts: " +  allRSTRelations);
		StringBuffer output = new StringBuffer ("");
		String NA = "EMPTY";
		for( Alignment alignment : alignments){
			PdtbRelation relation = alignment.getRelation();
			String connective = NA;
			String sense = NA + "\t" + NA;
			String connective2 = NA;
			String sense2 = NA + "\t" + NA;
			String pdtbAttribution = NA;
			if(!(relation.getRelation().equals("EntRel") || relation.getRelation().equals("NoRel")) ){
				PdtbConnective conn = ((PdtbConnective) ((FSList) relation.getConnectives()).getNthElement(0));
				connective = conn.getText();
				sense = (conn.getSemClass2()!="") ? conn.getSemClass1() + "\t" + conn.getSemClass2() : conn.getSemClass1() + "\t EMPTY";
				if (relation.getAttribution() != null)
					pdtbAttribution = relation.getAttribution().getAttrType() + "::" + relation.getAttribution().getSource() + "::" + relation.getAttribution().getPolarity();
				// For implicit relations with two annotated connectives
				try{
					conn = ((PdtbConnective) ((FSList) relation.getConnectives()).getNthElement(1));
					connective2 =  conn.getText();
					sense2 = (conn.getSemClass2()!="") ? conn.getSemClass1() + "\t" + conn.getSemClass2() : conn.getSemClass1() + "\t EMPTY";
				}catch(Exception e){
					//System.err.println("Cannot retrieve second connective annotation!");
				}
			}
			// Multi-span args
			int arg1SpansNo = 0;
			String arg1Spans = "";
		
			while(true){
			//for(;arg1SpansNo < 3;){
				try{
					PdtbSpan span = ((PdtbSpan) ( (FSList) ((PdtbArg) relation.getArg1()).getArgSpans() ).getNthElement(arg1SpansNo));
					arg1Spans = "[" +   span.getCoveredText() + "]" + arg1Spans ;
					//System.out.println(arg1SpansNo);
					arg1SpansNo = arg1SpansNo + 1;
				}catch(Exception e){
					break;
				}
			}
		
			int arg2SpansNo = 0;
			String arg2Spans = "";
			
			while(true){
			//for(;arg2SpansNo < 3;){
				try{
					PdtbSpan span = ((PdtbSpan) ( (FSList) ((PdtbArg) relation.getArg2()).getArgSpans() ).getNthElement(arg2SpansNo));
					arg2Spans = "[" +   span.getCoveredText() + "]"+ arg2Spans ;
					//System.out.println(arg2SpansNo);
					arg2SpansNo = arg2SpansNo + 1;
				}catch(Exception e){
					break;
				}
			}
			
			String outputRecord =   relation.getWsjSectionFileNo() + "\t" + relation.getOrderedId() + "\t" + relation.getRelation() + "\t"
					 + sense + "\t" + connective + "\t"  + sense2 + "\t" + connective2 +  "\t" +
					relation.getArg1().getBegin() + "\t" + relation.getArg1().getEnd() + "\t" +
					relation.getArg1().getCoveredText().replaceAll("(\\r|\\n|\\r\\n|\\t|\")+", " ") + "\t" +
					relation.getArg2().getBegin() + "\t" + relation.getArg2().getEnd() + "\t" +
					relation.getArg2().getCoveredText().replaceAll("(\\r|\\n|\\r\\n|\\t|\")+", " ") + "\t" +
					arg1SpansNo + "\t" + arg1Spans.replaceAll("(\\r|\\n|\\r\\n|\\t|\")+", " ") + "\t" +
					arg2SpansNo + "\t" + arg2Spans.replaceAll("(\\r|\\n|\\r\\n|\\t|\")+", " ") + "\t" +
					alignment.getPdtbArgsOverlap() + "\t"  ;
			
			if(alignment.getRelationEquivalent() == null)
				outputRecord += StringUtils.repeat(NA + "\t", 17) + NA;
			else{
				// a few changes to the RST relation name to fix a few bad annotations in the corpus:
				String name = alignment.getRelationEquivalent().getName();
				name = name.toLowerCase().equals("comparison") ? "Comparison" : name;
				name = name.toLowerCase().equals("temporal-same-time") ? "temporal-same-time" : name;
				
				outputRecord += alignment.getArg1IsRelation() + "\t" + 
					( alignment.getArg1IsRelation() ?  ((RstRelation) alignment.getArg1Equivalent()).getSpan() : ((RstAnnotation) alignment.getArg1Equivalent()).getSpanId()  ) + "\t" +
					alignment.getArg1Diff() + "\t" +
					alignment.getArg1Equivalent().getBegin()  + "\t" + alignment.getArg1Equivalent().getEnd() + "\t" +
					alignment.getArg1Equivalent().getCoveredText().replaceAll("(\\r|\\n|\\r\\n|\\t|\")+", " ") + "\t" +
					
					alignment.getArg2IsRelation() + "\t" + 
					( alignment.getArg2IsRelation() ?  ((RstRelation) alignment.getArg2Equivalent()).getSpan() : ((RstAnnotation) alignment.getArg2Equivalent()).getSpanId()  ) + "\t" +
					alignment.getArg2Diff() + "\t" +
					alignment.getArg2Equivalent().getBegin()  + "\t" + alignment.getArg2Equivalent().getEnd() + "\t" +
					alignment.getArg2Equivalent().getCoveredText().replaceAll("(\\r|\\n|\\r\\n|\\t|\")+", " ") + "\t" +
					
					alignment.getRelationEquivalent().getSpan() + "\t" +
					alignment.getExtraSpans() + "\t" +
					alignment.getIntervSats() +  "\t" +
					alignment.getIntervNucs()  + "\t" + 
					alignment.getArg1Sats() +  "\t" +
					alignment.getArg1Nucs()  + "\t" + 
					alignment.getArg2Sats() +  "\t" +
					alignment.getArg2Nucs()  + "\t" + 
					name  + "\t" +
					(alignment.getIntervNucs() < 0 || alignment.getIntervSats() < 0 ) + "\t" +  //This indicates the alignment needs revision
					(alignment.getIntervNucs() < 0 || alignment.getIntervSats() < 0  || alignment.getIntervSats() > 0 ) + "\t" + //Stricter revision includes cases that don't comply with RST nuclearity annotation principle
					(alignment.getMultiNucleiEncounters() /2) + "\t" +
					alignment.isMultiNucleiRoot() + "\t" +
					(alignment.getAttrEncounters() /2) + "\t" +
					alignment.isAttrRoot() + "\t" +
					alignment.isTravProblem() + "\t" +
					alignment.isSameunitTreatment() + "\t" + 
					pdtbAttribution ;
		
			}
			outputRecord +=   "\n";
			//System.out.println(outputRecord);
			output.append(outputRecord);
		}

		String workingDir = System.getProperty("user.dir");
		//System.out.println("Current working directory : " + workingDir);
		String outFileAddress = workingDir + "/" + targetLocation  +  "/alignmentData/allAlignments.csv";
		//System.out.println("File to be written:" + outFileAddress);
		File targetFile = new File(outFileAddress);
		File parent = targetFile.getParentFile();
		if (!parent.exists() && !parent.mkdirs()) {
			throw new IllegalStateException("Cannot create dir: " + parent);
		}
		BufferedWriter bwr;
		try {
			bwr = new BufferedWriter(new FileWriter(targetFile, true));//append
			bwr.write(output.toString());
		    bwr.flush();
		    bwr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}
	
	private Alignment aligningRstRelation(Collection<RstAnnotation> rstAnnotations, Collection<RstRelation> rstRelations, PdtbRelation relation) {
		int MAXCHARCOUNT = 1000000; //maximum count of charachters in a document
		Alignment alignment = new Alignment();
		alignment.setRelation(relation);
		alignment.setRelationEquivalent(null);
		
		PdtbArg arg1 = relation.getArg1();
		PdtbArg arg2 = relation.getArg2();
		alignment.setPdtbArgsOverlap(overlap(arg1.getBegin(), arg1.getEnd(), arg2.getBegin(), arg2.getEnd()) > 0);
		// Find the arg1 and arg2 equivalents among RstAnnotations
		Annotation bestMatch = null;
		int maxOverlap = 0; 
		int minMargine = MAXCHARCOUNT;
		for( RstAnnotation a : rstAnnotations ){
			int overlap = overlap(arg1.getBegin(),arg1.getEnd(), a.getBegin(), a.getEnd());
			if ( (a.getSpanId() == 0) || overlap == 0)
				continue;
			int margine = margine(arg1.getBegin(),arg1.getEnd(), a.getBegin(), a.getEnd()) ;
			//System.out.println("overlap: "+ overlap + "   margine: " + margine +   "  TEXT: " + a.getCoveredText());
			if( overlap > maxOverlap || ( overlap == maxOverlap && margine < minMargine ) ){
				maxOverlap = overlap;
				minMargine = margine;
				bestMatch = a;
			}
		}
		if(bestMatch == null){
			return alignment;
		}
		Boolean arg1IsRelation = false;
		for( RstRelation a : rstRelations ){
			int overlap = overlap(arg1.getBegin(),arg1.getEnd(), a.getBegin(), a.getEnd());
			int margine = margine(arg1.getBegin(),arg1.getEnd(), a.getBegin(), a.getEnd()) ;
			if( overlap > maxOverlap || ( overlap == maxOverlap && margine < minMargine ) ){
				maxOverlap = overlap;
				minMargine = margine;
				bestMatch = a;
				arg1IsRelation = true;
				//System.out.println("overlap: "+ overlap + "   margine: " + margine +   "  TEXT: " + a.getCoveredText());
			}
		}
		alignment.setArg1Equivalent(bestMatch);
		alignment.setArg1Diff( margine(arg1.getBegin(),arg1.getEnd(), bestMatch.getBegin(), bestMatch.getEnd()) );
		alignment.setArg1IsRelation(arg1IsRelation);
		
		bestMatch = null;
		maxOverlap = 0; 
		minMargine = MAXCHARCOUNT;
		for( RstAnnotation a : rstAnnotations ){
			int overlap = overlap(arg2.getBegin(),arg2.getEnd(), a.getBegin(), a.getEnd());
			if ( a.getSpanId() == 0 || overlap == 0)
				continue;
			int margine = margine(arg2.getBegin(),arg2.getEnd(), a.getBegin(), a.getEnd()) ;
			if( overlap > maxOverlap || ( overlap == maxOverlap && margine < minMargine ) ){
				maxOverlap = overlap;
				minMargine = margine;
				bestMatch = a;
				//System.out.println("overlap: "+ overlap + "   margine: " + margine +   "  TEXT: " + a.getCoveredText());
			}
		}
		if(bestMatch == null)
			return alignment;
		Boolean arg2IsRelation = false;
		for( RstRelation a : rstRelations ){
			int overlap = overlap(arg2.getBegin(),arg2.getEnd(), a.getBegin(), a.getEnd());
			int margine = margine(arg2.getBegin(),arg2.getEnd(), a.getBegin(), a.getEnd()) ;
			if( overlap > maxOverlap || ( overlap == maxOverlap && margine < minMargine ) ){
				maxOverlap = overlap;
				minMargine = margine;
				bestMatch = a;
				arg2IsRelation = true;
				//System.out.println("overlap: "+ overlap + "   margine: " + margine +   "TEXT: " + a.getCoveredText());
			}
		}
		alignment.setArg2Equivalent(bestMatch);
		alignment.setArg2Diff( margine(arg2.getBegin(),arg2.getEnd(), bestMatch.getBegin(), bestMatch.getEnd()) );
		alignment.setArg2IsRelation(arg2IsRelation);
		
		// Find the smallest RST relation covering arg1Equivalent and arg2Equivalent
		int arg1SpanBegin;
		int arg1SpanEnd;
		if( alignment.getArg1IsRelation() ){
			String[] spans = ((RstRelation) alignment.getArg1Equivalent()).getSpan().split(" ");
			arg1SpanBegin = Integer.parseInt(spans[0]);
			arg1SpanEnd = Integer.parseInt(spans[1]);
		}
		else{
			arg1SpanBegin = ((RstAnnotation) alignment.getArg1Equivalent()).getSpanId();
			arg1SpanEnd = ((RstAnnotation) alignment.getArg1Equivalent()).getSpanId();
		}
		int arg2SpanBegin;
		int arg2SpanEnd;
		if( alignment.getArg2IsRelation() ){
			String[] spans = ((RstRelation) alignment.getArg2Equivalent()).getSpan().split(" ");
			arg2SpanBegin = Integer.parseInt(spans[0]);
			arg2SpanEnd = Integer.parseInt(spans[1]);
		}
		else{
			arg2SpanBegin = ((RstAnnotation) alignment.getArg2Equivalent()).getSpanId();
			arg2SpanEnd = ((RstAnnotation) alignment.getArg2Equivalent()).getSpanId();
		}
		int arg1Len = arg1SpanEnd - arg1SpanBegin + 1;
		int arg2Len = arg2SpanEnd - arg2SpanBegin + 1;
		int minExtraSpans = MAXCHARCOUNT;
		RstRelation bestRelation = null;
		for( RstRelation r : rstRelations ){
			//if (r.getName().equals("Same-Unit")){
			//	System.out.println("Same-Unit skipped");
			//	continue;
			//}
			String[] spans = r.getSpan().split(" ");
			int begin = Integer.parseInt(spans[0]);
			int end = Integer.parseInt(spans[1]);
			if(!( arg1SpanBegin >= begin  && arg1SpanEnd <= end &&
					arg2SpanBegin >= begin  && arg2SpanEnd <= end ) )
				continue;
			if ( ((end - begin + 1) - (arg1Len + arg2Len)) > minExtraSpans )
				continue;
			// reaching here means the relation covers both args and is the minimal such one so far
			bestRelation = r;
			minExtraSpans = (end - begin + 1) - (arg1Len + arg2Len);
		}
		if(bestRelation == null)
			return alignment;
		if(bestRelation.getName().equals("Same-Unit")){
			// same-unit relations special treatment: the relation below same-unit will be considered
			// if there are more than one children that are relations (and not an EDU), then bestRelation will be kept intact
			RstRelation r = null; 
			if(bestRelation.getNuclei() != null ){
				for(Annotation nucl : UimaUtils.getList(bestRelation.getNuclei())) {
					if ( r == null && ((RstAnnotation) nucl).getSpanId() == 0 )// there is an explorable relation
						r = (RstRelation) nucl;
					else if( r != null && ((RstAnnotation) nucl).getSpanId() == 0 )// more than one complex child: reset to same-unit itsel
						r = bestRelation;
				}
			}
			if(bestRelation.getSatellite() != null ){
				Annotation sat = bestRelation.getSatellite();
				if ( r == null && ((RstAnnotation) sat).getSpanId() == 0 )// there is an explorable relation
					r = (RstRelation) sat;
				else if( r != null && ((RstAnnotation) sat).getSpanId() == 0 )// more than one complex child: reset to same-unit itsel
					r = bestRelation;
			}
			if( r != null &&  r != bestRelation ){//it had exactly one child that is not a single EDU
				String[] spans = r.getSpan().split(" ");
				int begin = Integer.parseInt(spans[0]);
				int end = Integer.parseInt(spans[1]);
				minExtraSpans = (end - begin + 1) - (arg1Len + arg2Len);
				bestRelation = r;
				alignment.setSameunitTreatment(true);
			}
			
		}
		alignment.setRelationEquivalent(bestRelation);
		alignment.setExtraSpans(minExtraSpans);
		alignment.setEdgeCounts();
		return alignment;
	}
	
	private int overlap(int myBegin, int myEnd, int rBegin, int rEnd){
		if( myBegin > rEnd || myEnd < rBegin )
			return 0;
		int begin = ( myBegin < rBegin ) ? rBegin: myBegin;
		int end = ( myEnd < rEnd ) ? myEnd: rEnd;
		return end - begin;
	}
	private int margine(int myBegin, int myEnd, int rBegin, int rEnd){
		if( myBegin > rEnd || myEnd < rBegin )
			return 0;
		int begin = ( myBegin < rBegin ) ? myBegin: rBegin;
		int end = ( myEnd < rEnd ) ? rEnd: myEnd;
		return (end - begin) - overlap(myBegin, myEnd, rBegin, rEnd);
	}
	
	private class Alignment{
		PdtbRelation relation;
		RstRelation relationEquivalent;
		
		private boolean pdtbArgsOverlap;
		private Annotation arg1Equivalent;
		private Annotation arg2Equivalent;
		private int arg1Diff;
		private int arg2Diff;
		private boolean arg1IsRelation;
		private boolean arg2IsRelation;
		private int extraSpans;
		private int intervNucs;
		private int intervSats;
		private int arg1Sats;
		private int arg2Sats;
		private int arg1Nucs;
		private int arg2Nucs;
		private int multiNucleiEncounters;
		private boolean multiNucleiRoot;
		private int attrEncounters;
		private boolean attrRoot;
		private boolean travProblem;
		private boolean sameunitTreatment;

		
		public int getAttrEncounters() {
			return attrEncounters;
		}
		public boolean isAttrRoot() {
			return attrRoot;
		}
		Alignment(){
			this.sameunitTreatment = false;
		}
		public void setEdgeCounts() {
			this.multiNucleiEncounters = 0;
			this.multiNucleiRoot = false;
			this.attrEncounters = 0;
			this.attrRoot = false;
			this.travProblem = false;
			
			//System.out.println("Looking for SAT in arg1");
			int arg1Sats = depthOf((RstAnnotation) arg1Equivalent, relationEquivalent, "SAT", false);
			//System.out.println("Looking for SAT in arg2");
			int arg2Sats = depthOf((RstAnnotation) arg2Equivalent, relationEquivalent, "SAT", false);
			//System.out.println("Looking for NUC in arg1");
			int arg1Nucs = depthOf((RstAnnotation) arg1Equivalent, relationEquivalent, "NUC", false);
			//System.out.println("Looking for NUC in arg2: " + arg2Equivalent.getBegin() + " -- " + arg2Equivalent.getEnd() );
			int arg2Nucs = depthOf((RstAnnotation) arg2Equivalent, relationEquivalent, "NUC", false);
			
			
			if(extraSpans < 0 || 
					(((RstAnnotation) arg1Equivalent).getBegin() == ((RstAnnotation) arg2Equivalent).getBegin() &&
							((RstAnnotation) arg1Equivalent).getEnd() == ((RstAnnotation) arg2Equivalent).getEnd()) ){
				intervNucs = -1;
				intervSats = -1;
				return;
			}	
			if(arg1Sats < 0 ||  arg2Sats < 0 ){
				//the deptOf function returned -1 
				System.out.println("deptOf function returned -1");
				this.travProblem = true;
				intervSats = -1;
			}
			else{
				intervSats = arg1Sats + arg2Sats;
				this.arg1Sats = arg1Sats;
				this.arg2Sats = arg2Sats;
			}
			if (arg1Nucs < 0 || arg2Nucs < 0 ){
				//the deptOf function returned -1 
				System.out.println("deptOf function returned -1");
				this.travProblem = true;
				intervNucs = -1;
			}
			else{
				intervNucs = arg1Nucs + arg2Nucs;
				this.arg1Nucs = arg1Nucs;
				this.arg2Nucs = arg2Nucs;
			}
			return;
		}
		public boolean isTravProblem() {
			return travProblem;
		}
		public boolean isMultiNucleiRoot() {
			return multiNucleiRoot;
		}
		private int depthOf(Annotation a, RstRelation r, String countable, boolean flag) {
			// flag is designed to avoid counting the direct children of the root relation when it's not supposed to be counted
			
			if(!flag && (((RstRelation) r).getName().toLowerCase().contains("attribution"))) // Set a flag for the root relation
				this.attrRoot = true ; 
			if(flag && (((RstRelation) r).getName().toLowerCase().contains("attribution"))) 
				this.attrEncounters++ ; 
			
			if( r.getSatellite() != null &&  a.getBegin() >= r.getSatellite().getBegin() && 
					a.getEnd() <= r.getSatellite().getEnd() ){
				int counted = countable.equals("SAT") && flag ? 1 : 0;
				if( a.getBegin() == r.getSatellite().getBegin() && 
						a.getEnd() == r.getSatellite().getEnd())
					return counted;
				if ( r.getSatellite().getSpanId() == 0 ){ // there is an explorable relation
					int d = depthOf(a, (RstRelation) r.getSatellite(), countable, true) ;
					return d == -1 ? d : counted + d;
				}
			}
			else {
				if( r.getNuclei() != null) {
					int nucleiCount = UimaUtils.getList(r.getNuclei()).size();
					if(!flag && nucleiCount > 1) // Set a flag for the root relation
						this.multiNucleiRoot = true ; 
					if(flag && nucleiCount > 1) // Set a flag for the root relation
						this.multiNucleiEncounters++ ; 
					for(Annotation nucl : UimaUtils.getList(r.getNuclei())) {
						nucleiCount++;
						RstAnnotation nucleus = (RstAnnotation) nucl;
						if (a.getBegin() >= nucleus.getBegin() && a.getEnd() <= nucleus.getEnd() ){
							int counted = countable.equals("NUC") && flag ? 1 : 0;
							
							if( a.getBegin() == nucleus.getBegin() && a.getEnd() == nucleus.getEnd())
								return counted;
							if ( nucleus.getSpanId() == 0 ){ // there is an explorable relation
								 int d = depthOf(a, (RstRelation) nucleus, countable, true) ;
								 return d == -1 ? d : counted + d;
							}
						}
					}
				}
			}
//			System.err.println(" ");
//			System.out.println(" ");
//			System.err.println("Looking for " + a.getBegin() + " -- " + a.getEnd()
//					+ " inside " + r.getSpan() + " : " + r.getBegin() + " -- " + r.getEnd());
//			System.err.println("WARNING: Span neither matching nor including the argument!");
			return -1;
		}
		public Annotation getArg1Equivalent() {
			return arg1Equivalent;
		}
		public Annotation getArg2Equivalent() {
			return arg2Equivalent;
		}
		public void setArg1Equivalent(Annotation arg1Equivalent) {
			this.arg1Equivalent = arg1Equivalent;
		}
		public void setArg2Equivalent(Annotation arg2Equivalent) {
			this.arg2Equivalent = arg2Equivalent;
		}
		
		public boolean getArg1IsRelation() {
			return arg1IsRelation;
		}
		public void setArg1IsRelation(boolean arg1IsRelation) {
			this.arg1IsRelation = arg1IsRelation;
		}
		public boolean getArg2IsRelation() {
			return arg2IsRelation;
		}
		public void setArg2IsRelation(boolean arg2IsRelation) {
			this.arg2IsRelation = arg2IsRelation;
		}
		
		public PdtbRelation getRelation() {
			return relation;
		}
		public void setRelation(PdtbRelation relation) {
			this.relation = relation;
		}
		public RstRelation getRelationEquivalent() {
			return relationEquivalent;
		}
		public void setRelationEquivalent(RstRelation relationEquivalent) {
			this.relationEquivalent = relationEquivalent;
		}
	
		public int getArg1Diff() {
			return arg1Diff;
		}
		public void setArg1Diff(int arg1Diff) {
			this.arg1Diff = arg1Diff;
		}
		public int getArg2Diff() {
			return arg2Diff;
		}
		public void setArg2Diff(int arg2Diff) {
			this.arg2Diff = arg2Diff;
		}

		public int getExtraSpans() {
			return extraSpans;
		}
		public void setExtraSpans(int extraSpans) {
			this.extraSpans = extraSpans;
		}
		public int getArg1Sats() {
			return arg1Sats;
		}
		public void setArg1Sats(int arg1Sats) {
			this.arg1Sats = arg1Sats;
		}
		public int getArg2Sats() {
			return arg2Sats;
		}
		public void setArg2Sats(int arg2Sats) {
			this.arg2Sats = arg2Sats;
		}
		public int getArg1Nucs() {
			return arg1Nucs;
		}
		public int getArg2Nucs() {
			return arg2Nucs;
		}
		public int getIntervNucs() {
			return intervNucs;
		}
		public int getIntervSats() {
			return intervSats;
		}
		public boolean getPdtbArgsOverlap() {
			return pdtbArgsOverlap;
		}
		public void setPdtbArgsOverlap(boolean pdtbArgsOverlap) {
			this.pdtbArgsOverlap = pdtbArgsOverlap;
		}
		public int getMultiNucleiEncounters() {
			return multiNucleiEncounters;
		}
		public boolean isSameunitTreatment() {
			return sameunitTreatment;
		}
		public void setSameunitTreatment(boolean sameunitTreatment) {
			this.sameunitTreatment = sameunitTreatment;
		}
	}
}
