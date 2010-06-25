/*
 * Copyright (C) 2009 Hrafn Loftsson
 *
 * This file is part of the IceNLP toolkit.
 * IceNLP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * IceNLP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with IceNLP. If not,  see <http://www.gnu.org/licenses/>.
 *
 * Contact information:
 * Hrafn Loftsson, School of Computer Science, Reykjavik University.
 * hrafn@ru.is
 */
 
/* This transducer marks potential subjects 					*/
/* A potential subject is:							*/
/* a nominative NP which precedes/follows a finite verb phrase 			*/
/* a nominative NP which precedes a relative conjunction			*/
/* a dative NP which precedes/follows a verb which demands a dative subject 	*/
/* an accusative NP which precedes/follows a verb which demands an accusative subject */

package is.iclt.icenlp.core.iceparser;
import java.io.*;
import is.iclt.icenlp.core.utils.IceParserUtils;
%%

%public
%class Func_SUBJ
%standalone
%line
%extends IceParserTransducer
%unicode

%{
	String Func0Open=" {*SUBJ ";
	String Func0Close=" *SUBJ} ";
	String Func1Open=" {*SUBJ> ";
	String Func1Close=" *SUBJ>} ";
	String Func2Open=" {*SUBJ< ";
	String Func2Close=" *SUBJ<} ";
	boolean agreement = false;  // -a parameter attribute
  
	int theIndex=0;

	//java.io.Writer out = new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8"));
	java.io.Writer out = new BufferedWriter(new OutputStreamWriter(System.out));

	public void set_doAgreementCheck(boolean option)
	{
		agreement = option;
	}  
	public void parse(java.io.Writer _out) throws java.io.IOException
	{
	  	out = _out;
	  	while (!zzAtEOF) 
	  	    yylex();
	}

	public String AgreementCheck(String s1, String s2, String s3, String s4, int order)
	{
		//order of the input is not always the same and the output shouldnt be either.
		String trueOut = createOutputString(s1,s2,s3,s4, order, true);
		String falseOut = createOutputString(s1,s2,s3,s4, order, false);
		
		if(!agreement)
		{
			return trueOut;
		}

		String tokenAndWordLess = RemoveTokens(falseOut);
		//System.err.println("Tokenless: " + tokenAndWordLess);

		boolean allAgree = checkAgreement(tokenAndWordLess);
/*
		System.err.print("s1: " + s1 + "\n" + 
							"s2: " + s2 + "\n" +
							"s3: " + s3 + "\n" + 
							"s4: " + s4 + "\n" +
							"trueOut: " + trueOut + "\n" +
							"falseOut: " + falseOut + "\n");*/
		if(allAgree)
		{
			return trueOut;
		}
		
		return falseOut;
	}
	public String createOutputString(String s1, String s2, String s3, String s4, int order, boolean tag)
	{
		if(tag)
			return s1+s2+s3+s4;

		switch(order)
		{
			case 1:
				return s2+s4;			
			case 2:
				return  s1+s3;
		}
		
		return "[ERR "+s1+s2+s3+s4+" ERR]";
	}
	public static String RemoveTokens(String str)
	{
		str = IceParserUtils.RemoveFromSymbolToWhitespace("[", str);
		str = IceParserUtils.RemoveFromSymbolToWhitespace("{", str);
		str = new StringBuffer(str).reverse().toString();
		str = IceParserUtils.RemoveFromSymbolToWhitespace("]", str);
		str = IceParserUtils.RemoveFromSymbolToWhitespace("}", str);
		str = new StringBuffer(str).reverse().toString();
		str = IceParserUtils.RemoveSpacesAndWords(str);
			
		return str;
	}
	public boolean checkAgreement(String str)
	{
		boolean allTheSame = true;
		String [] tags = null;
		tags = str.split(" ");

		for(int i=0; i<tags.length; i++)
		{
			for(int x=i+1; x<tags.length; x++)
			{
				int mod1, mod2;
			
				mod1 = GetModifier(tags[i].substring(0,1));
				mod2 = GetModifier(tags[x].substring(0,1));
///////////
				if( mod1 == -1 || mod2 == -1) continue;
				if(tags[i].length() < 5 || tags[x].length() < 5) continue;

				String pers1, pers2, nr1, nr2;
			
				pers1 = tags[i].substring(2 + mod1,3 + mod1);
				nr1 = tags[i].substring(3 + mod1,4 + mod1);

				pers2 = tags[x].substring(2 + mod2,3 + mod2);
				nr2 = tags[x].substring(3 + mod2,4 + mod2);
			
				pers1 = IfGenderReturnPers(pers1);
				pers2 = IfGenderReturnPers(pers2);

				//System.err.println("pers1: " + pers1 + "\n" + "pers2: " + pers2 + "\n" + "nr1: " + nr1 + "\n" + "nr2: " + nr2);
				if( !pers1.equals(pers2) || !nr1.equals(nr2))
				{
					allTheSame = false;
				}
			}
		}

		return allTheSame;
	}
	public static int GetModifier(String letter)
	{
		if( letter.equals("f") || letter.equals("n") )
			return 0;
		if( letter.equals("s") )
			return 1;

		return -1;
	}
	public String IfGenderReturnPers(String pers)
	{
		if(pers.equals("h") || pers.equals("k") || pers.equals("v"))
			return "3";
		
		return pers;
	}
	  
%}

%eof{
	try {
	  out.flush();	
	} 
	catch (IOException e) {
            e.printStackTrace();
        }
%eof}

%include regularDef.txt
%include funcDef.txt
%include verbLexicon.txt

VPBe = {OpenVPb}~{CloseVPb}
VP = {OpenVP}" "~{CloseVP}
//VP = {OpenVP}(" "|s)~({CloseVP}|{CloseVPs})
VPPast = {OpenVPp}~{CloseVPp}
VPorVPBe = {VP}|{VPBe} 
VPDat = {OpenVP}{WhiteSpace}+{VerbDat}~{CloseVP}
VPAcc = {OpenVP}{WhiteSpace}+{VerbAcc}~{CloseVP} 
RelCP = {OpenSCP}{WhiteSpace}+sem{WhiteSpace}+{ConjTag}~{CloseSCP}
AdvP = {OpenAdvP}~{CloseAdvP}
PP = {OpenPP}~{ClosePP}

PPorQual = ({PP}{WhiteSpace}+)+ | {FuncQualifier}{WhiteSpace}+ (({PP}{WhiteSpace}+)+)?
/* Qual = {FuncQualifier}{WhiteSpace}+ */

SubjectVerb = 	({FuncQualifier}{WhiteSpace}+)?({NomSubject}|{NPNum}){WhiteSpace}+{PPorQual}?{VPorVPBe} 	| 
		{DatSubject}{WhiteSpace}+{PPorQual}?{VPDat} 	| 
		{AccSubject}{WhiteSpace}+{PPorQual}?{VPAcc}
		
/* SubjectAPVerb = {APNom}{WhiteSpace}+{VPorVPBe} */
		
SubjectVerbMissing = {NomSubject}{WhiteSpace}+({PP}{WhiteSpace}+)?({OpenAP}~{CloseAP} | {OpenAPs}~{CloseAPs} | {VPPast}) 
		
/* The subject can follow the verb */
VerbSubject = 	
		// {VPorVPBe}{WhiteSpace}+({FuncQualifier}{WhiteSpace}+)?{NomSubject}({WhiteSpace}+{FuncQualifier})? | /* kom hann þá */
		{VPorVPBe}{WhiteSpace}+{PPorQual}?{NomSubject}({WhiteSpace}+{FuncQualifier})? | /* kom hann þá */
		{VPDat}{WhiteSpace}+{DatSubject} | 	/* þótti mér hann erfiður */
		{VPAcc}{WhiteSpace}+{AccSubject}  	/* langaði mig að .. */
		
VerbAdvPSubject = {VPorVPBe}{WhiteSpace}+{AdvP}{WhiteSpace}({FuncQualifier}{WhiteSpace}+)?{NomSubject}({WhiteSpace}+{FuncQualifier})?  /* hafði þá samingagerðin .. */
		
//PPVerbSubject = ({AdvP}{WhiteSpace}+({PP}{WhiteSpace}+)? | {PP}{WhiteSpace}+) {VPBe}{WhiteSpace}+{NomSubject}	/* kannski voru allir; innst í honum var maður, í bílnum var ... */
SubjectRel = {NomSubject}{WhiteSpace}+({FuncQualifier}{WhiteSpace}+)?{RelCP}


%%

{SubjectVerb}	
{ 
	String str = yytext();
	if (str.contains("[PP"))	/* We don't want the preposition phrase to be included */
		StringSearch.splitString(str, "[PP", true, -1);
	//else if (str.contains("{*QUAL")) {	/* Make sure the qualifier is a part of the subject */
	//	StringSearch.splitString(str,"[VP", true, -1);		
	//}
	//else {
	//	/* Find where the NPs/NP ended and insert the SUBJ label */
	//	theIndex = StringSearch.splitString(str,"NPs]", true, 4);
	//	if (theIndex == -1)
	//		StringSearch.splitString(str,"NP]", true, 3);
	//}
	else
		StringSearch.splitString(str,"[VP", true, -1);		
	out.write(AgreementCheck(Func1Open,StringSearch.firstString,Func1Close,StringSearch.nextString,1));
} 
//{SubjectAPVerb}	{ 
//			/* Find where the AP ended and insert the SUBJ label */
//			StringSearch.splitString(yytext(),"AP]", true, 3);
//			out.write(Func1Open+StringSearch.firstString+Func1Close+StringSearch.nextString);
//		} 






{SubjectVerbMissing}	
{ 
	String str = yytext();
	if (str.contains("[PP"))
		StringSearch.splitString(str, "[PP", true, -1);
	else 
	{
		// Find where the NPs/NP ended and insert the SUBJ label 
		theIndex = StringSearch.splitString(str,"NPs]", true, 4);
		if (theIndex == -1)
			StringSearch.splitString(str,"NP]", true, 3);		
	}
	out.write(AgreementCheck(Func0Open,StringSearch.firstString,Func0Close,StringSearch.nextString,1));
} 
		
{VerbSubject}	
{ 
	String str = yytext();
	if (str.contains("PP]"))
		StringSearch.splitString(str, "PP]", true, 3);

	else 
	{
		// Find where the VP ended and insert the SUBJ label 
		if (str.contains("VPb]"))
			StringSearch.splitString(str,"VPb]", true, 4);	
		else if (str.contains("VPs]"))
			StringSearch.splitString(str,"VPs]", true, 4);
		else
			StringSearch.splitString(str,"VP]", true, 3);		
	}
	out.write(AgreementCheck(StringSearch.firstString,Func2Open,StringSearch.nextString,Func2Close,2));
}
		
{VerbAdvPSubject}	
{
	String str = yytext();
	String searchFor=null;
	if (str.contains("VPb]"))
		searchFor = "VPb]";
	else if (str.contains("VPs]"))
		searchFor = "VPs]";
	else
		searchFor = "VP]";
	StringSearch.splitString2(yytext(),searchFor,"AdvP]");
	out.write(AgreementCheck(StringSearch.firstString,Func2Open,StringSearch.nextString,Func2Close,2));
}
			
//{PPVerbSubject}	{ 
//			String str = yytext();
//			if (str.contains("{*QUAL")) {	// Make sure the qualifier is a part of the subject 
//				StringSearch.splitString(yytext(),"{*QUAL", true, -1);		
//			}
//			else {
//				// Find where the NPs/NP started and insert the SUBJ label 
//				theIndex = StringSearch.splitString(yytext(),"[NPs", false, -1);
//				if (theIndex == -1)
//					StringSearch.splitString(yytext(),"[NP", false, -1);		
//			}
//			out.write(StringSearch.firstString+Func2Open+StringSearch.nextString+Func2Close);
//			
//		}

{SubjectRel}	
{ 
	// Find where the relative phrase started and the NP ended and insert the SUBJ label 
	StringSearch.splitString(yytext(),"[SCP", true, -1);		
	out.write(AgreementCheck(Func1Open,StringSearch.firstString,Func1Close,StringSearch.nextString,1));
}
"\n"
{ 
	//System.err.print("Reading line: " + Integer.toString(yyline+1) + "\r"); 
	out.write("\n"); 
}
.
{ 
	out.write(yytext());
}





