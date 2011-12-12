package engine.easy.analyzer;

/**
 * This is a EasySearchAnalyzer class which works on the same lucene proposed steps of analyzing the text.
 * The steps are involved to tokenize the text, remove stop words, stem English words, and transform the text into lowercase pattern.
 * 
 * Author: Adnan Urooj
 * 
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.LowerCaseTokenizer;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;

import engine.easy.util.AppConstants;


public class EasySearchAnalyzer extends Analyzer {

	private final boolean enableStopPositionIncrements;
	
	public EasySearchAnalyzer() {
		// According to API, this below will preserve positions of the incoming tokens (ie, accumulate and set position increments of the removed tokens).
		 enableStopPositionIncrements = StopFilter.getEnablePositionIncrementsVersionDefault(Version.LUCENE_30);
	}
	
	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {

		return new PorterStemFilter(
				new StopFilter(enableStopPositionIncrements, 
						new LowerCaseFilter(
								new StandardFilter(new StandardTokenizer(Version.LUCENE_30, reader))),
										EasySearchStopAnalyzer.ENGLISH_STOP_WORDS_SET));
	}
	
	public static void testAnalyzer(String fileName, Analyzer analyzer, boolean compareWithLuceneAnaylzers) throws IOException {
		
		try {
			File file = new File(fileName);
			if(!file.exists()) {
				System.out.println("The specified data bank directory does not exist: " + file);
				System.exit(-1);
			}
			
			int ch;
			StringBuffer text = new StringBuffer("");
			FileInputStream fin = new FileInputStream(file);
		    while ((ch = fin.read()) != -1)
		    	text.append((char) ch);
		    fin.close();
		        
			if (compareWithLuceneAnaylzers) {
				
				System.out.println("\n###### StandardAnalyzer !! ######");
				printResult(text.toString(), new StandardAnalyzer(Version.LUCENE_30));
				
//				System.out.println("\n###### SimpleAnalyzer !! ######");
//				printResult(text.toString(), new StandardAnalyzer());
//				
//				System.out.println("\n###### StopAnalyzer !! ######");
//				printResult(text.toString(), new StandardAnalyzer());
//				
//				System.out.println("\n###### KeywordAnalyzer !! ######");
//				printResult(text.toString(), new StandardAnalyzer());
//				
//				System.out.println("\n###### WhitespaceAnalyzer !! ######");
//				printResult(text.toString(), new StandardAnalyzer());
//				
//				System.out.println("\n###### StopAnalyzer !! ######");
//				printResult(text.toString(), new StandardAnalyzer());
			}
			
			System.out.println("\n###### EasySearchAnalyzer !! ######");
			printResult(text.toString(), new EasySearchAnalyzer());
		} 
		catch (Exception e) {
			System.out.println("Exception : " + e.toString());
		}
	}
	
	private static void printResult(String text, Analyzer analyzer) throws IOException {
		
		int tokenCount = 0;
		TokenStream tokenStream = analyzer.tokenStream("FIELDNAME", new StringReader(text)); // this method will used for token streams
		TermAttribute termAtt = tokenStream.getAttribute(TermAttribute.class); 
		while(tokenStream.incrementToken()) {
			tokenCount++;
			String tokenText = new String(termAtt.termBuffer(), 0, termAtt.termLength()); 
			System.out.println(" >> Token " + tokenCount + ": "+tokenText);
		}
	}
	
	public static void main(String args[]) {
		
		try {
			testAnalyzer(AppConstants.TXT_FILE, new EasySearchAnalyzer(), Boolean.TRUE);
		} catch (Exception e) {
			System.out.println("Exception : " + e.toString());
		}
	}
}
