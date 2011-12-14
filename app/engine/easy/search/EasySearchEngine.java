package engine.easy.search;

/**
 * This is a EasySearchEngine class which provides a core search engine.
 * 
 * Author: Adnan Urooj
 * 
 */

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;

import engine.easy.analyzer.EasySearchAnalyzer;
import engine.easy.indexer.reader.EasySearchIndexReader;
import engine.easy.ranking_model.BM25;
import engine.easy.util.AppConstants;

public class EasySearchEngine {

	private SpellChecker spellChecker;

	public EasySearchEngine() {
		this.spellChecker = getSpecSpellChecker();
	}

	private SpellChecker getSpecSpellChecker() {
		SpellChecker spellchecker = null;

		try {
			File dir = new File(AppConstants.DICTIONARY_INDEX_PATH);
			Directory directory = FSDirectory.open(dir);
			spellchecker = new SpellChecker(new RAMDirectory());
			spellchecker.indexDictionary(new PlainTextDictionary(new File(AppConstants.DICTIONARY_PATH)));

		} catch (Exception e) {
			System.out.println("Exception: getSpecSpellChecker" + e.toString());
		}

		return spellchecker;
	}

	public String[] getSuggestions(String keyword) throws IOException {

		try {
			if (this.spellChecker != null) {
				return spellChecker.suggestSimilar(keyword, AppConstants.SPELL_SUGGESTIONS);
			}
		} catch (Exception e) {
			System.out.println("Exception: getSuggestions" + e.toString());
		}

		return null;
	}

	/**
	 * Get the luecene query object for given string query
	 * 
	 * @param query the given string query
	 * @return Query the lucene query object
	 * @throws Exception if one is thrown.
	 */
	private Query getQuery(String query) {
		Query q = null;

		try {
			QueryParser qparser = new QueryParser(Version.LUCENE_30, AppConstants.CONTENT_FIELD, new EasySearchAnalyzer());

			// now you can use the query parser to parse the query.
			q = qparser.parse(query);
		} catch (Exception e) {
			System.out.println("Exception: " + e.toString());
		}

		return q;
	}

	/**
	 * Perform the search for given query
	 * 
	 * @param query the given string query
	 * @return the list of highest ranked results.
	 * @throws Exception if one is thrown.
	 */
	public Result[] performSearch(String query) {

		Query q = getQuery(query);
		return performSearch(q);
	}

	/**
	 * Perform the search for given query
	 * 
	 * @param Query the given query
	 * @return the list of highest ranked results.
	 * @throws Exception if one is thrown.
	 */
	public Result[] performSearch(Query query) {

		Result[] results = null;
		try {
			Directory indexDir = FSDirectory.open(new File(AppConstants.INDEX_DIR_PATH));

			IndexReader indexReader = IndexReader.open(indexDir);
			EasySearchIndexReader esiReader = new EasySearchIndexReader(indexReader);

			System.out.println("\n >> QUERY: " + query.toString());

			// Get the results!!
			results = getResults(query, indexReader, esiReader);
			//Display the results!
			displayResults(results, indexReader);

		} catch (Exception e) {
			System.out.println("Exception: performSearch " + e.toString());
		}

		return results;
	}

	/**
	 * Computes the results on ranking function and other scoring factors.
	 * 
	 * @param terms the query terms
	 * @param ixReader the index reader
	 * @param esiReader the custom easy index reader
	 * @param numberOfResults the number of results return back
	 * @return the Results.
	 * @throws Exception if one is thrown.
	 */
	public Result[] getResults(Query query, IndexReader ixReader, 
			EasySearchIndexReader esiReader) {

		Map<Integer, Result> results = null;

		try {
			Set<Term> terms = new HashSet<Term>();
			query.extractTerms(terms);

			results = new HashMap<Integer, Result>();
			Iterator<Term> itr = terms.iterator();

			while (itr.hasNext()) {
				Term term = itr.next();

				TermDocs docs = ixReader.termDocs(term);
				int docFreq = ixReader.docFreq(term); // get the document frequency of the term from lucene's index reader
				int docNum = esiReader.recordCount(AppConstants.CONTENT_FIELD); // get the total record of the field from lucene extra index (you may think it is also possible to use ixreader.maxDoc() here, but the ixreader.maxDoc() only returns the number of documents, while some documents may not have the search field (although every document has the search field in this example))

				while (docs.next()) {
					int id = docs.doc(); // get the internal lucene's id of the document
					int termFreq = docs.freq(); // get the frequency of the term in this document
					int docLen = esiReader.docLength(id, AppConstants.CONTENT_FIELD); // get the length of the document from lucene extra index.
					double avgDocLen = esiReader.avgFieldLength(AppConstants.CONTENT_FIELD); // get the average length of the search field from lucene extra index.
					Document document = ixReader.document(id);	//get the particular document.

					// Compute the scoring with BM25 ranking and also include other scoring factors such as (relevance feedback based on terms) 
					BM25 bm25 = new BM25();
					//System.out.println(bm25.getInfo());

					// Also add the document boost in the ranking score.
					double termWeight = bm25.score(termFreq, docNum, docLen, avgDocLen, 1d, docFreq) + document.getBoost();

					if(results.containsKey(id)){
						results.get(id).score = results.get(id).score + termWeight;
					} else{
						Result result = new Result(new Integer(id), termWeight, "text");
						results.put(id, result);
					}
				}
			}

			return sortArray(results, AppConstants.TOP_RESULTS);

		} catch (Exception e) {
			System.out.println("Exception: getResults " + e.toString());
		}

		return null;
	}

	/**
	 * Sort the results on highest ranking.
	 * 
	 * @param results the results map with details.
	 * @return the sorted results array.
	 * @throws Exception if one is thrown.
	 */
	private Result[] sortArray(Map<Integer, Result> results, int numberOfResults) {
		Result[] resultArray = new Result[results.values().size()];
		int pos = 0;
		for(Result result:results.values()){
			resultArray[pos] = result;
			pos++;
		}

		//Sort the results
		Arrays.sort(resultArray, new Comparator<Result>(){
			public int compare(Result r1, Result r2) {
				if(r1.score < r2.score){
					return 1;
				}else if(r1.score > r2.score){
					return -1;
				}else{
					return 0;
				}
			}
		});

		//Top results
		Result[] topArray = null;
		if (resultArray.length > numberOfResults) {
			topArray = new Result[numberOfResults];

			for (int position=0; position < numberOfResults; position++) {
				topArray[position] = resultArray[position];
			}
		}
		else {
			topArray = resultArray;
		}

		return topArray;
	}

	/**
	 * Display the results in highest ranking order
	 * 
	 * @param the array with results detail
	 * @param ixReader the index reader
	 * @throws Exception if one is thrown.
	 */
	public void displayResults(Result[] results, IndexReader ixReader) {

		try {
			// Now output ranked results;
			for(int pos=0; pos<results.length; pos++){
				Result result = results[pos];
				int id = result.id;
				double score = result.score;
				Document doc = ixReader.document(result.id); // Also, you can get the document from indexreader
				String docid = doc.getField("DOCID").stringValue();
				System.out.println("Result No."+(pos+1)+": Lucene id = "+id+", DOCID = "+docid+", score = "+score);
			}
		} catch (Exception e) {
			System.out.println("Exception - displayResults: " + e.toString());
		}
	}

	/**
	 * Perform the pesudo relevance feedback
	 * 
	 * @param the array with results detail
	 * @param ixReader the index reader
	 * @throws Exception if one is thrown.
	 */
        
	public Result[] performPesudoRelevanceFeedback(String q) {

		Result[] results = null;

		try {
			//First perform the raw query, get the results and then perform again on highest terms.
			results = performSearch(q);

			//perform the search again with new formulated query!
			Query newQuery = RelevanceFeedBackUtil.performPesduoRelevance(results);

			//Get the pesudo relevance results
			results = performSearch(newQuery);

		} catch (Exception e) {
			System.out.println("Exception - performUserRelevanceFeedback: " + e.toString());
		}

		return results;
	}

	/**
	 * Perform the user relevance feedback
	 * 
	 * @param the array with results detail
	 * @param ixReader the index reader
	 * @throws Exception if one is thrown.
	 */
	public Result[] performUserRelevanceFeedback(List<Integer> docIds, boolean isThumbsUp) {

		Result[] results = null;

		try {
			if (!docIds.isEmpty()) {
				Query q = null;

				if (isThumbsUp)
					q = RelevanceFeedBackUtil.performThumbsUp(docIds);
				else
					q = RelevanceFeedBackUtil.performThumbsDown(docIds);

				//perform the search again with new formulated query!
				results = performSearch(q);
			}
		} catch (Exception e) {
			System.out.println("Exception - performUserRelevanceFeedback: " + e.toString());
		}

		return results;
	}

	protected List<TermFreq> getTopTerms(List<Term> terms, IndexReader ixReader,
			int numTermsToReturn) {

		List<TermFreq> result = null;

		try {
			result = new ArrayList<TermFreq>();
			Iterator<Term> itr = terms.iterator();

			while (itr.hasNext()) {
				Term term = itr.next();
				TermDocs docs = ixReader.termDocs(term);

				while (docs.next()) {
					int docId = docs.doc();
					TermFreqVector[] tfvs = ixReader.getTermFreqVectors(docId);

					for (int i = 0; tfvs != null && i < tfvs.length; i++) {
						TermFreqVector tfv = tfvs[i];

						String[] suggestedTerms = tfv.getTerms();// get the
																	// terms
						int[] freqs = tfv.getTermFrequencies();// get the
																// frequencies

						for (int j = 0; j < suggestedTerms.length; j++) {
							// create a container for the Term and Frequency
							// information
							result.add(new TermFreq(suggestedTerms[j], freqs[j]));
						}

						// Sort by frequency
						Collections.sort(result, new Comparator<TermFreq>() {
							public int compare(TermFreq tf1, TermFreq tf2) {
								if (tf1.freq < tf2.freq) {
									return 1;
								} else if (tf1.freq > tf2.freq) {
									return -1;
								} else {
									return 0;
								}
							}
						});

						if (numTermsToReturn < result.size()) {
							result = result.subList(0, numTermsToReturn);
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Exception: " + e.toString());
		}

		return result;
	}

	private class TermFreq {
		String term; // term
		double freq; // freq

		TermFreq(String sTerm, double freq) {
			this.term = sTerm;
			this.freq = freq;
		}
	}	

        
        
        	public Result[] performUserRelevanceFeedback(Map Ids) {

		Result[] results = null;

		try {
			if (Ids!=null&& !Ids.isEmpty()) {
				Query q = null;

				q=RelevanceFeedBackUtil.performUpAndDown(Ids);
				//perform the search again with new formulated query!
				results = performSearch(q);
			}
		} catch (Exception e) {
			System.out.println("Exception - performUserRelevanceFeedback: " + e.toString());
		}

		return results;
	}

	public static void main (String args[]) {

		try {
			Directory indexDir = FSDirectory.open(new File(AppConstants.INDEX_DIR_PATH));

			IndexReader indexReader = IndexReader.open(indexDir);
			EasySearchIndexReader esiReader = new EasySearchIndexReader(indexReader);

			EasySearchEngine engine = new EasySearchEngine();
			Result[] results = engine.performSearch("KENNEDY ADMINISTRATION PRESSURE ON NGO DINH DIEM TO STOP SUPPRESSING THE BUDDHISTS .");

			List<Integer> docIds = new ArrayList<Integer>();
			docIds.add(results[8].id);
			docIds.add(results[13].id);

			results = engine.performUserRelevanceFeedback(docIds, Boolean.TRUE);

		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
