package edu.columbia.cs.psl.mountaindew.example;

import java.io.Reader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LengthFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

public class DirichletAnalyzer extends Analyzer{
	private final Pattern alphabet = Pattern.compile("[a-z]+");
	  
	  @SuppressWarnings("deprecation")
	  @Override
	  public TokenStream tokenStream(String fieldName, Reader reader) {
	    /*TokenStream result = new StandardTokenizer(
	        Version.LUCENE_CURRENT, reader);*/
	    TokenStream result = new StandardTokenizer(Version.LUCENE_36, reader);
	    //result = new LowerCaseFilter(result);
	    result = new LowerCaseFilter(Version.LUCENE_36, result);
	    //result = new LengthFilter(result, 3, 50);
	    result = new LengthFilter(true, result, 3, 50);
	    //result = new StopFilter(true, result, StandardAnalyzer.STOP_WORDS_SET);
	    result = new StopFilter(Version.LUCENE_36, result, StandardAnalyzer.STOP_WORDS_SET);
	    //result = new PorterStemFilter(result);
	    CharTermAttribute charAtt = (CharTermAttribute)result.addAttribute(CharTermAttribute.class);
	    
	    StringBuilder buf = new StringBuilder();
	    try {
	    	while (result.incrementToken()) {
	    		if (charAtt.length() < 3)
	    			continue;
	    		
	    		String word = new String(charAtt.buffer(), 0, charAtt.length());
	    		
	    		Matcher m = alphabet.matcher(word);
	    		
	    		if (m.matches()) {
	    			buf.append(word).append(" ");
	    			//System.out.println("Match word: " + word);
	    		}
	    	}
	    } catch (Exception ex) {
	    	ex.printStackTrace();
	    }
	    //System.out.println("All words: " + buf.toString());
	    return new WhitespaceTokenizer(Version.LUCENE_36, new StringReader(buf.toString()));
	  }

}
