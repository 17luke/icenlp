package is.iclt.icenlp.core.utils;


import is.iclt.icenlp.core.icetagger.IceTaggerOutput;
import is.iclt.icenlp.core.utils.Lexicon;
import is.iclt.icenlp.core.lemmald.Lemmald;
import is.iclt.icenlp.core.lemmald.LemmaResult;

/**
 * Created by IntelliJ IDEA.
 * User: gudmundur
 * Date: 2/10/12
 * Time: 3:38 PM
 * To change this template use File | Settings | File Templates.
 */
// GöL - error identification
public class ErrorDetector {

	static boolean SqlEnabled = false;
	static String SqlUser;
	static String SqlPass;
	static String SqlUrl;

	public static String setSQL(boolean enableSQL, String url, String user, String pass) {
		SqlEnabled = enableSQL;
		SqlUser = user;
		SqlPass = pass;
		SqlUrl = url;
		return "lol";
	}





	// for Phrase_NP
	static String encO =  IceParserUtils.encodeOpen;
	static String encC =  IceParserUtils.encodeClose;


	// for Phrase_NP
	private static int GetModifierNLG(String letter)
	{
		if(letter.equals("n") || letter.equals("l") || letter.equals("g") )
			return 0;
		if(letter.equals("f") || letter.equals("t"))
			return 1;

		return -1;
	}

	// for Phrase_NP
	// returns an error string like : ?g+ca
	private static String CheckGenNumCase(String str)
	{
//		System.out.println("CheckGenNumCase  str="+str);

        String errors = "";
		boolean allTheSame = true;
		String [] tags = str.split(" ");

		String tagI = tags[0].substring(encO.length(), tags[0].length()-encC.length());

		int mod1;
		mod1 = GetModifierNLG(tagI.substring(0,1));

		int forLoopStop = tags.length;
		int mod2;
		String gen1,num1,case1, gen2,num2,case2;

		for(int i=0; i<forLoopStop; i++)
		{
			String tagX = tags[i].substring(encO.length(), tags[i].length()-encC.length());

			mod2 = GetModifierNLG(tagX.substring(0,1));

			//System.out.println("Mod1 : "+mod1+"\nMod2 : "+mod2);

			if(mod1 == -1 || mod2 == -1) continue;

			//ef t þá verður 2 sæti að vera f

			if(tagI.length() < 4+mod1 || tagX.length() < 4+mod2)
			{
				continue;
			}

			gen1 = tagI.substring(1+mod1, 2+mod1);
			num1 = tagI.substring(2+mod1, 3+mod1);
			case1 = tagI.substring(3+mod1, 4+mod1);

			gen2 = tagX.substring(1+mod2, 2+mod2);
			num2 = tagX.substring(2+mod2, 3+mod2);
			case2 = tagX.substring(3+mod2, 4+mod2);

//			System.out.println("gnc1 : "+gen1+" - "+num1+" - "+case1);
//			System.out.println("gnc2 : "+gen2+" - "+num2+" - "+case2);

			if( !gen1.equals(gen2) || !num1.equals(num2) || !case1.equals(case2))
			{
				errors = "?";
				// if there has been something written to error we need to add "+" between error tags
				if (!gen1.equals(gen2))
				{
					errors += "Ng";
				};
            	if (!num1.equals(num2)) {
					errors += "Nn";
				};
               	if (!case1.equals(case2)) {
					errors += "Nca";
				};
			}
		}

		return errors;
	}


	// for Phrase_NP
	public static String ErrorCheck(String originalStr, boolean agreement, boolean markGrammarError, String PhraseType)
	{
		if(!agreement && !markGrammarError)
		{
			return " [" + PhraseType + " " + originalStr + " " + PhraseType + "] ";//do nothing to originalStr
		}
		else if (markGrammarError) // check for errors
		{
			String tokenlessStr = RemoveTokens(originalStr);
			String errors = CheckGenNumCase(tokenlessStr);//create the error string which is going to be something like ge+n+ca

			return " [" + PhraseType + errors + " " + originalStr + " " + PhraseType + errors + "] ";
		}
		return originalStr;
	}









	// for Phrase_NP and Func_SUBJ
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






	// for Func_SUBJ
	public static String checkAgreement(String str)
	{
		String error = "";
//		System.out.println("gDB>> checkAgreement="+str);
		boolean allTheSame = true;
		String [] tags = str.split(" ");

		// in this for-loop [x = i+1] and we stop when x reaches the last tag
		int x = 0;
		int forLoopStop =  tags.length-1 ;
		for(int i=0; x<forLoopStop; i++)
		{
			x++;
//			System.out.print("gDB>> ["+(i)+"]"+tags[(i)]);
//			System.out.print("="+tags[x]+"["+(x)+"]\n");
			int mod1, mod2;

			String tagI = tags[i].substring(encO.length(), tags[i].length()-encC.length());
			String tagX = tags[x].substring(encO.length(), tags[x].length()-encC.length());

			mod1 = getModifierNFS(tagI.substring(0,1));
			mod2 = getModifierNFS(tagX.substring(0,1));

			if (tagI.equals("ssg") || tagI.equals("sng") || tagX.equals("ssg") || tagX.equals("sng") )
			{
				if (!error.equals(""))
				{
					error += "+";
				}
				error += "Ss";
				continue;
			}

			// if either word doesn't include a gender or a person
			if ( mod1 == -1 || mod2 == -1)
			{
				continue;
			}

			if (tagI.length() < 3+mod1 || tagX.length() < 3+mod2)
			{
				continue;
			}
			String pers1, pers2, nr1, nr2;

			pers1 = tagI.substring(1 + mod1,2 + mod1);
			nr1 = tagI.substring(2 + mod1,3 + mod1);

			pers2 = tagX.substring(1 + mod2,2 + mod2);
			nr2 = tagX.substring(2 + mod2,3 + mod2);

			pers1 = IfGenderReturnPers(pers1);
			pers2 = IfGenderReturnPers(pers2);
//	System.err.println("pers1: " + pers1 + "\n" + "pers2: " + pers2 + "\n" + "nr1: " + nr1 + "\n" + "nr2: " + nr2);
			// if there is an error we add a letter [p], but if we already have an error we first add a + [p+n]
			if( !pers1.equals(pers2)&&!error.endsWith("Sp") )
			{
				if (!error.equals(""))
				{
					error += "+";
				}
				error += "Sp";
			}
			// if we have already found problem with number we do not report it again
			if ( !nr1.equals(nr2)/*&&!error.endsWith("Sn")*/)
			{
				if (!error.equals(""))
				{
					error += "+";
				}
				error += "Sn";
			}
		}
		return error;
	}

	// for Func_SUBJ
	public static int getModifierNFS(String letter)
	{
		if( letter.equals("n")  )   // gender's position is right after n [nhfn]
		{
			return 0;
		}
		else if( letter.equals("f")  )
		{
			return 1;
		}
		else if( letter.equals("s")  )   // persona is the second one after s [sfg3eþ]
		{
			return 2;
		}

		return -1;
	}

	// for Func_SUBJ
	private static String IfGenderReturnPers(String pers)
	{
		if(pers.equals("h") || pers.equals("k") || pers.equals("v"))
			return "3";

		return pers;
	}


	public static String AgreementCheck(String s1, String s2, String s3, String s4, int order, boolean agreement, boolean markGrammarError)
	{
//		System.out.println("gDB>> s1=("+s1+") s2=("+s2+") s3=("+s3+") s4=("+s4+")");
		//order of the input is not always the same and the output shouldnt be either.
		String trueOut = createOutputString(s1,s2,s3,s4, order, true, markGrammarError, "");
		if(!agreement)
		{
			return trueOut;
		}

		String tokenAndWordLess = RemoveTokens(trueOut);

		String error = checkAgreement(tokenAndWordLess);

		if(error.equals(""))
		{
			return trueOut;
		}

//String falseOut = createOutputString(s1,s2,s3,s4, order, false, markGrammarError, error);
//		System.out.println("gDB>>falseOut="+falseOut);
//		return falseOut;
		return createOutputString(s1,s2,s3,s4, order, false, markGrammarError, error);
	}

	public static String createOutputString(String s1, String s2, String s3, String s4, int order, boolean tag, boolean markGrammarError, String error)
	{
		if(tag)
			return s1+s2+s3+s4;
		if(markGrammarError)
		{
			switch(order)
			{
				case 1:
					return getErrTag(s1, error)+s2+getErrTag(s3, error)+s4;
				case 2:
					return  s1+getErrTag(s2, error)+s3+getErrTag(s4, error);
			}
		}
		else
		{
			switch(order)
			{
				case 1:
					return s2+s4;
				case 2:
					return  s1+s3;
			}
		}

		return "[ERR "+s1+s2+s3+s4+" ERR]";
	}

		//inserts a questio mark to identify as a possible error
	public static String getErrTag(String str, String error)
	{
		StringBuffer stb = new StringBuffer(str);

		if( str.substring(1,2).equals("{") )
			return stb.insert(str.length()-1, "?"+error).toString();

		return stb.insert(str.length()-2, "?"+error).toString();

	}



	public static char findGender(String[] input)
	{
		// we prefer to find gender in nouns
		// go through all the inputs and return the gender of the noun if there is any noun found
		for (String a : input)
		{
			// when reaching the rest (null values) then stop the loop
			if (a == null)
			{
				break;
			}

			if (a.charAt(0) == 'n')
			{
				return a.charAt(1);
			}
		}

		// checks the first letter in the tag and pulls out the appropriate letter to indicate the gender
		for (String a : input)
		{
			// when reaching the rest (null values) then stop the loop
			if (a == null)
			{
				break;
			}

			if ((a.charAt(0) == 'l') || (a.charAt(0) == 'g'))  // nkeng , lkensf , ghen
			{
				return a.charAt(1);
			}
			else if ((a.charAt(0) == 'f')||((a.charAt(0) == 't')&&(a.length() == 5))) //feken , tfvfn (watch out for short tags)
			{
				return a.charAt(2);
			}
			else if ((a.charAt(0) == 's')&&(a.length()>3)) // sþgven
			{
				return a.charAt(3);
			}
		}
		return 'x'; // unspecified gender
	}


	public static boolean isGenderMismatch(char c1, char c2)
	{
//		System.out.println("gDB >> isGenderMismatch c1=("+c1+") c2="+c2);

		// return false if the gender matches
		if (c1 == c2)
		{
			return false;
		}
		// return false if the gender is a (1. or 2.) person
		else if ((c1 == '1')||(c1 == '2')||(c2 == '1')||(c2 == '2'))
		{
			return false;
		}

		return true;
	}


	public static char findNumber(String[] input)
	{
		// we prefer to find number in nouns
		// go through all the inputs and return the number of the noun if there is any noun found
		for (String a : input)
		{
			// when reaching the rest (null values) then stop the loop
			if (a == null)
			{
				break;
			}

			if (a.charAt(0) == 'n')
			{
				return a.charAt(2);
			}
		}

		// checks the first letter in the tag and pulls out the appropriate letter to indicate the gender
		for (String a : input)
		{
			// when reaching the rest (null values) then stop the loop
			if (a == null)
			{
				break;
			}
			if ((a.charAt(0) == 'l') || (a.charAt(0) == 'g'))  // nkeng , lkensf , ghen
			{
				return a.charAt(2);
			}
			else if ((a.charAt(0) == 'f')||((a.charAt(0) == 't')&&(a.length() == 5))) //feken , tfvfn (watch out for short tags)
			{
				return a.charAt(3);
			}
			else if ((a.charAt(0) == 's')&&(a.length()>3)) // sþgven  sfg3eþ
			{
				return a.charAt(4);
			}
		}
		return 'x'; // unspecified gender
	}

	// returns true if there is a mismatch between the first tag that has a number and the incoming number
	public static boolean isNumberMismatch(char n1, char n2)
	{
//		System.out.println("gDB >> isNumberMismatch n1=("+n1+") n2=("+n2+")");
		if (n1 == n2)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	// returns the person if there is any person found, or x if none is found
	public static char findPerson(String[] input)
	{

		// we prefer to find number in nouns
		// go through all the inputs and return the number of the noun if there is any noun found
		for (String a : input)
		{
			// when reaching the rest (null values) then stop the loop
			if (a == null)
			{
				break;
			}

			if ((a.charAt(0) == 'n')&&((a.charAt(1) == 'k')||(a.charAt(1) == 'v')|(a.charAt(1) == 'h')))
			{
				return '3';
			}
		}

		// checks the first letter in the tag and pulls out the appropriate letter to indicate the gender
		for (String a : input)
		{
			// when reaching the rest (null values) then stop the loop
			if (a == null)
			{
				break;
			}

			if ((a.charAt(0) == 'n')||(a.charAt(0) == 'l')||(a.charAt(0) == 'g'))  // nkeng , lkensf , ghen
			{
				if ((a.charAt(1) == 'k')||(a.charAt(1) == 'v')|(a.charAt(1) == 'h'))
				{
					return '3';
				}
			}
			else if ((a.charAt(0) == 't')&&(a.length() == 5)) // tfvfn (watch out for short tags)
			{

				if ((a.charAt(2) == 'k')||(a.charAt(2) == 'v')|(a.charAt(2) == 'h'))
				{
					return '3';
				}
			}
			else if (a.charAt(0) == 'f') //feken, fp1en
			{
				if ((a.charAt(2) == 'k')||(a.charAt(1) == 'v')|(a.charAt(1) == 'h'))
				{
					return '3';
				}
				else
				{
					return a.charAt(2);
				}
			}
			else if ((a.charAt(0) == 's')&&(a.length()>3)) // sþgven  sfg3eþ sfg1en
			{
				return a.charAt(3);
			}
		}
		return 'x'; // unspecified gender
	}

		// returns true if there is a mismatch between the first tag that has a person and the incoming person
	public static boolean isPersonMismatch(String[] input, char person)
	{
		for (String a : input)
		{
			// when reaching the rest (null values) then stop the loop
			if (a == null)
			{
				break;
			}

			if ((a.charAt(0) == 'n') || (a.charAt(0) == 'l') || (a.charAt(0) == 'g'))  // nkeng , lkensf , ghen
			{
				if (person != '3')// n l and g only have third person so if we land here and person is not 3rd person, we return an error
				{
					return true;
				}
			}
			else if ((a.charAt(0) == 'f')||((a.charAt(0) == 't')&&(a.length() == 5))) //feken , tfvfn (watch out for short tags)
			{
				// if we see f with k v or h and persona is not 3, we return true (an error), if it is 3 we return false
				if ((a.charAt(2) == 'k')||(a.charAt(2) == 'v')||(a.charAt(2) == 'h'))
				{
					if (person != '3')
					{
						return true;
					}
					else
					{
						return false;
					}
				}
				else // if we land here it means the a.charAt returns 1,2 or 3 and we can match it OR if it returns k v or h with persona = 3
				{
					if (a.charAt(2) != person)
					{
						return true;
					}
				}
			}
			else if ((a.charAt(0) == 's')&&(a.length()>3)) // sþgven sfg1en but watch out for ssg
			{
				// if we see f with k v or h and persona is not 3, we return an error
				if (((a.charAt(3) == 'k')||(a.charAt(2) == 'v')||(a.charAt(2) == 'h'))&&(person != '3'))
				{
					if (person != '3')
					{
						return true;
					}
					else
					{
						return false;
					}
				}
				else // if we land here it means the a.charAt returns 1,2 or 3 and we can match it
				{
					if (a.charAt(3) != person)
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	// returns the findCase if there is any findCase found, or x if none is found
	public static char findCase(String[] input)
	{
		// checks the first letter in the tag and pulls out the appropriate letter to indicate the gender
		for (String a : input)
		{
			// when reaching the rest (null values) then stop the loop
			if (a == null)
			{
				break;
			}

			if ((a.charAt(0) == 'n')||(a.charAt(0) == 'l')||(a.charAt(0) == 'g'))  // nkeng , lkensf , ghen
			{
				return a.charAt(3);
			}
			else if ((a.charAt(0) == 't')&&(a.length() == 5)||(a.charAt(0) == 'f')) // tfvfn (watch out for short tags)//feken, fp1en
			{
					return a.charAt(4);
			}
			else if ((a.charAt(0) == 's')&&(a.length()>3)) // sþgven  sfg3eþ sfg1en
			{
				return a.charAt(5);
			}
		}
		return 'x'; // unspecified case
	}


	// gets in a string like : ^lkensf$ ^nkeng$ ^sfg3en$ ^aa$ ^lkensf$
	// we can also handle any other strings now and just filter out strings that do not start with a ^
	// then it is turned into a string array where each item is [lkensf][nkeng][sfg3en][aa][lkensf]
	public static String[] toTagList(String input)
	{
		String[] output;
		int x = 0;

		String [] tags = input.split(" ");
		int forLoopStop = tags.length;

		output = new String[tags.length];

		// each string is on the form ^nkeng$ which we want to keep
		// we take the ^ and $ off and only keep nouns, verbs...
		for (int i = 0; i < forLoopStop; i++)
		{
			if ((tags[i].length() > 0)&&(tags[i].charAt(0) == '^'))  // make sure we only add tags (strings starting with ^) to output
			{
				output[x] = tags[i].substring(1,tags[i].length()-1);
				x++;
			}
		}

		return output;
	}

	// finds and crops out the phrase requested.
	// phraseType can be the beginning of a phrase
	private static String findPhrase(String s1, String phraseType)
	{
		if (phraseType.equals(""))
		{
			return s1;
		}
		if (s1.indexOf(phraseType) != -1)
		{
			int maxLength = s1.length(); // to make sure we don't check outside of the incoming string
			int startPos = s1.indexOf(phraseType)+phraseType.length(); // the start of the phrase we are looking for
			int endPos = s1.lastIndexOf(phraseType); // the end location of the phrase we are looking for

			while (true)
			{
				if ((s1.charAt(startPos) == ' ')&&(startPos <= maxLength))
				{
					break;
				}
				startPos++;
			}

			return s1.substring(startPos,endPos);
		}
		else
		{
			return "";
		}
	}

	private static String removePhrase(String s1, String phraseType)
	{
		if (phraseType.equals(""))
		{
			return s1;
		}

		String out = s1.substring(0,s1.indexOf(phraseType)-1); // -1 to take out the initial [

		out = out + s1.substring(s1.lastIndexOf(phraseType)+phraseType.length()+1); // +phraseType.length() to take out the remainder of the phraseType and the +1 to get rid of the ]
		if (out.length() > 0)
		{
			return out;
		}
		else
		{
			return s1;
		}
	}

	// for Func_COMP
	public static String CompAgreementCheck(String s1, String s2, String open, String close, int order)
	{

		if ((order == 1) && (s2.contains("[NP")))
		{
//			System.out.println("s2=("+s2+") found [NP");
			return s1+open+s2+close;
		}
		else if ((order != 1) && (s1.contains("NP]")))
		{
//			System.out.println("s1=("+s1+") found [NP");
			return s2+open+s1+close;
		}

		String error = "";

//	    System.out.println("gDB>> s1:("+s1+")");
//	    System.out.println("gDB>> s2:["+s2+"]");

		String phrase = "VPb";
		if (order == 99) // if the incoming sentences do not have VPb
		{
			phrase = "";
			order = 1;
		}

		// get s1 without the VPb
		String s1tmp = removePhrase(s1,phrase);

		// s1:({*SUBJ> [NPn Börnin ^nhfng$  NP] *SUBJ>}  [VPb  voru ^sfg3fþ$   VPb])
		// pull out the VPb tag and compare it to the subject
		String[] VPbTags = toTagList(RemoveTokens(findPhrase(s1,phrase)));
		 /* detect mismatch between VPb and the subject and object */


		String[] s1Tags = toTagList(RemoveTokens(s1tmp));
		String[] s2Tags = toTagList(RemoveTokens(s2));

		System.out.println("gDB >> s1Gender="+findGender(s1Tags));
		System.out.println("gDB >> s2Gender="+findGender(s2Tags));

		if (isGenderMismatch(findGender(s2Tags),findGender(s1Tags)))
		{
			error += "Cg";
		}
		if (isNumberMismatch(findNumber(s2Tags),findNumber(s1Tags)))
		{
			error += "Cn";
		}

		if (error.length() > 0)
		{
			error = "?" + error;
		}

    // order 1 = s1 {comp s2 comp}
		open = open.substring(0,open.length()-1)+error+" ";
		close = close.substring(0,close.indexOf('}'))+error+"} ";

		if (order == 1)
		{
			return s1+open+s2+close;
		}
	// order 2 = {comp s1 comp} s2
		else
		{
			return open+s1+close+s2;
		}
	}

	// looks at s1 and s2 and turns an error if their numbers doesn't match
	// if order is 1, then the s2 will get open and close around it, otherwise it will be s1
	// made for Func_SUBJ
	public static String agreementSubjectVerbCheckNumberAndPerson(String s1, String s2, String open, String close, int order)
	{
//		System.out.println("gDB>> agreementSubjectVerbCheckNumberAndPerson s1=("+s1+") s2=("+s2+")");

		String[] s1Tags = toTagList(RemoveTokens(s1));
		String[] s2Tags = toTagList(RemoveTokens(s2));
		String error = "";

		String VPTags[];
		VPTags = new String[1];
		String VPTemp = "";

		 /* detect mismatch between VPb and the subject and object */

		VPTemp = findPhrase(s1,"VP");
		// if we find VP tag in s1 we check if the gender matches s2
		// if we don't find it in s1 we check s2 and check for gender match there
		if (!VPTemp.equals(""))
		{
			                    toTagList(VPTemp);
			                    findNumber(toTagList(VPTemp));
			                    findNumber(s1Tags);
			                    isNumberMismatch(findNumber(s1Tags),findNumber(toTagList(VPTemp)));

			if (isNumberMismatch(findNumber(s1Tags),findNumber(toTagList(VPTemp))))
			{
//				System.out.println("gDB>> VPTemp=("+VPTemp+") s1Tags=("+s1Tags+")");
				error += "Vn";
			}
			if (isPersonMismatch(s1Tags,findPerson(toTagList(VPTemp))))
			{
				error += "Vp";
			}
			s1 = errorMarkPhrase(s1, "VP", error);
		}
		else
		{
			VPTemp = findPhrase(s2,"VP");
	/*		if (isNumberMismatch(s1Tags,findNumber(toTagList(VPTemp))))
			{

			}
	*/		if (isNumberMismatch(findNumber(s1Tags),findNumber(s2Tags)))
			{
//				System.out.println("gDB>> VPTemp=("+VPTemp+") s1Tags=("+s1Tags+")");
				error += "Vn";
			}
			if (isPersonMismatch(s1Tags,findPerson(s2Tags)))
			{
				error += "Vp";
			}

			s2 = errorMarkPhrase(s2, "VP", error);


		}



	//	open = open.substring(0,open.length()-1)+error+" ";
	//	close = close.substring(0,close.indexOf('}'))+error+"} ";
//		System.out.println("gDB>> s2 before=("+s2+")");
		// if s2 has the VP we insert the error there, otherwise check if s1 has VP and put it in there
/*		if (s2.contains("[VP"))
		{
			s2 = s2.substring(0,s2.indexOf("[VP")+3)+error+s2.substring(s2.indexOf("[VP")+3);
		}
		else if (s1.contains("[VP"))
		{
			s1 = s1.substring(0,s1.indexOf("[VP")+3)+error+s2.substring(s1.indexOf("[VP")+3);
		}
*/
//		System.out.println("gDB>> s2 after=("+s2+")");
		// insert the error into the VP
//		s2 = s2.substring()

		if (order == 1)
		{
			return s1+open+s2+close;
		}
		else
		{
			return open+s1+close+s2;
		}
	}

	// adds error to the phrase in s1 and returns the results
	private static String errorMarkPhrase (String s1, String phrase, String error)
	{
		if (s1.indexOf("["+phrase) == -1)
		{
			return s1;
		}
		if (error.length() < 1)
		{
			return s1;
		}

		// mark the first NP

		String out = "";
		int phraseStartExtend = 0;
		int phraseEndExtend = 0;
		int startOfPhrase = s1.indexOf(phrase)+phrase.length();
		int endOfPhrase = s1.lastIndexOf(phrase)+phrase.length();
		int endOfS1 = s1.length();

		// increase the length of startOfPhrase if the error/phrase is not exact and some letters are missing
		// counts the number of letters are after the start of phrase [NPn until it finds whitespace
		while(true)
		{
			if ((s1.charAt(startOfPhrase+phraseStartExtend) != ' ')&&(startOfPhrase < endOfPhrase))
			{
				phraseStartExtend++;
			}
			else
			{
				break;
			}
		}
		startOfPhrase += phraseStartExtend;

		// increase the length of startOfPhrase if the error/phrase is not exact and some letters are missing
		// counts the number of letters are after the start of phrase NPn] until it finds the close bracket ] sign
		while(true)
		{
			if ((s1.charAt(endOfPhrase+phraseEndExtend) != ']')&&(startOfPhrase+phraseEndExtend < endOfS1))
			{
				phraseEndExtend++;
			}
			else
			{
				break;
			}
		}
		endOfPhrase += phraseEndExtend;

//		out = s1.substring(0,startOfPhrase); // start the string with everything in front of the phrase
		out = s1.substring(0,startOfPhrase); // start the string with everything in front of the phrase
		out += "?"+error;	// insert the error after the first NP
		out += s1.substring(startOfPhrase,endOfPhrase); //everything inside NP
		out += "?"+error;	// insert the error after the second NP
		out += s1.substring(endOfPhrase,endOfS1); // append the rest

		return out;
	}

	private static String stripOffWhitespaces (String in)
	{
		int start = 0;
		int end = 0;

		// find where the word begins, from left
		int max = in.length();
		for (int i = 0;i < max;i++)
		{
			if (in.charAt(i) != ' ')
			{
				start = i;
				break;
			}
		}

		// find where the word begins, from right
		for (int i = max-1; 1 < i;i--)
		{
			if (in.charAt(i) != ' ')
			{
				break;
			}
				end = i;
		}

		return in.substring(start,end);
	}

	public static String agreementCheckSubjVerbObj(String s1, String s2, String open, String close, int order)
	{

		if (SqlEnabled) {
			String s1in = s1.substring(s1.indexOf("[VP")+3,s1.indexOf("VP]")); // gets the verb and tag from s1
			String word = s1in.substring(0,s1in.indexOf('^')); // gets the verb s1
			word = stripOffWhitespaces(word);

			String tag = s1in.substring(s1in.indexOf('^')+1,s1in.indexOf('$')); // gets the tag from s1

	//		String s2in = toTagList(s2)[0]; // get tag of the noun followed by the verb

			char s2case = findCase(toTagList(s2)); // get the tag of the nounphrase in s2



			String lemma = lemmado(word,tag);
//			System.out.print("lemmado lemma=("+lemma+")");

			String possibleTags[] = SqlLookup.sqlWordLookup(lemma,SqlUrl,SqlUser,SqlPass);


			// if the case of the object is the same as any possible tag for the verb, then do nothing, if there is a mismatch the nounphrase will be error marked
			if (possibleTags[1].equals(Character.toString(s2case))||
				possibleTags[2].equals(Character.toString(s2case))||
				possibleTags[3].equals(Character.toString(s2case))||
			// if we cannot find the word in the sqlWordLoopup then we do nothing
				(possibleTags[1].equals("") && possibleTags[2].equals("") && possibleTags[3].equals("") && possibleTags[4].equals("")))
			{
	//			System.out.print("MATCH! possibleTags.length="+possibleTags.length);
				// do nothing
			}
			else
			{
				System.out.println("MISMATCH! possibleTag:");
				System.out.println(s2case+"-"+possibleTags[1]);
				System.out.println(s2case+"-"+possibleTags[2]);
				System.out.println(s2case+"-"+possibleTags[3]);
				System.out.println(s2case+"-"+possibleTags[4]);

				//mark the nounphrase with an error
				s2 = errorMarkPhrase(s2,"NP","Aca");
			}

//		System.out.println("gDB>>MATCH possibleTag:"+possibleTags+" s2in:"+s2case);
		// bera saman s2in og possibleTags[]

		}
		return s1+open+s2+close;
	}

	private static String lemmado(String wordform, String tag)
	{
//		System.out.println("gDB>> lemmaldo Wordform=("+wordform+") tag=("+tag+")");

		Lemmald myLemmald=null;
		myLemmald = Lemmald.getInstance();

            if (myLemmald != null) {
                 LemmaResult lemmaResult = myLemmald.lemmatize(wordform,tag);// String wordform, String tag
 		 		return lemmaResult.getLemma();
            }

		 return "";
	}

	private static String lammatizeWord(String wordform, String tag)
	{
		//return IceTaggerOutput.lemmatizeWord(wordform,tag);
		return "";
	}


	// when we try to
	public static String objVerbSubj (String s1, String s2, String open, String close)
	{
		String out = "s1=("+s1+") s2=("+s2+")";

		String noun[] = toTagList(s1);
		String verb[] = toTagList(findPhrase(s2,"VP"));

		if (isNumberMismatch(findNumber(noun),findNumber(verb)))
		{
			s2 = errorMarkPhrase(s2,"VP","Vn");
		}

		out = open+s1+close+s2;

		return out;
	}
}