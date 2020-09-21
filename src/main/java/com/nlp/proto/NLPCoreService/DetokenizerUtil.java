package com.nlp.proto.NLPCoreService;

import opennlp.tools.tokenize.DetokenizationDictionary;
import opennlp.tools.tokenize.DetokenizationDictionary.Operation;
import opennlp.tools.tokenize.DictionaryDetokenizer;

public class DetokenizerUtil {
	private static final String tokens[] = { ".", "!", "(", ")", "\"", "-","'",",","�" };
	private static final Operation operations[] = { Operation.MOVE_LEFT, Operation.MOVE_LEFT, Operation.MOVE_RIGHT, Operation.MOVE_LEFT, Operation.RIGHT_LEFT_MATCHING, Operation.MOVE_BOTH,Operation.MOVE_BOTH, Operation.MOVE_LEFT,Operation.MOVE_BOTH };
    
	public static String deTokenize(String[] inputtokens) {
	    return new DictionaryDetokenizer(new DetokenizationDictionary(tokens, operations)).detokenize(inputtokens, null);
	}
	
}