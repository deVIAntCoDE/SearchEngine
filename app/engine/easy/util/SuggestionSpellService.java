package engine.easy.util;

import java.io.File;

import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

public class SuggestionSpellService {

	public static String[] suggestionTerms (String query) {
	
		
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		
		File dir = new File(AppConstants.DICTIONARY_INDEX_PATH);
		
		Directory directory = FSDirectory.open(dir);
		
		SpellChecker spellChecker = new SpellChecker(new RAMDirectory());
		
		spellChecker.indexDictionary(new PlainTextDictionary(new File(AppConstants.DICTIONARY_PATH)));
		
		String wordForSuggestions = "hee";
		
		int suggestionsNumber = 3;

		String[] suggestions = spellChecker.suggestSimilar(wordForSuggestions, suggestionsNumber);

		if (suggestions!=null && suggestions.length>0) {
			for (String word : suggestions) {
				System.out.println("Did you mean:" + word);
			}
		}
		else {
			System.out.println("No suggestions found for word:"+wordForSuggestions);
		}
	}
}
