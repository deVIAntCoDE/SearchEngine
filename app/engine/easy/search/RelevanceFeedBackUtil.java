package engine.easy.search;

/**
 * This is a RelevanceFeedBackUtil class which provides a relevance feedback.
 * 
 * Author: Adnan Urooj
 * 
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import engine.easy.indexer.EasySearchIndexBuilder;
import engine.easy.indexer.reader.EasySearchIndexReader;
import engine.easy.util.AppConstants;

public class RelevanceFeedBackUtil {

	/**
	 * This method will perform the thumbs up action. And generate the new query
	 * based on top specific highest terms. It also increase the relevant
	 * document boost so that their ranking is higher in search results for the
	 * similar terms.
	 */
	public static Query performThumbsUp(List<Integer> luceneDocIds) throws IOException {

		Query q = null;
		
		try {
			final Map<String, Integer> frequencyMap = new HashMap<String, Integer>();
			Map<Integer, Document> documentMap = new HashMap<Integer, Document>();
			List<String> termsList = new ArrayList<String>();

			Directory indexDir = FSDirectory.open(new File(AppConstants.INDEX_DIR_PATH));
			IndexReader indexReader = IndexReader.open(indexDir);
			EasySearchIndexReader esiReader = new EasySearchIndexReader(indexReader);
			
			for (Integer docId : luceneDocIds) {
				
				TermFreqVector tfv = indexReader.getTermFreqVector(docId, "CONTENT");
				Document doc = indexReader.document(docId);
				float boost = doc.getBoost() + AppConstants.THUMBS_UP;
				doc.setBoost(boost);
				
				System.out.print("DOC : "+ docId + " Field : " + tfv.getField() + "\n");
				
				for (int i=0; i < tfv.getTermFrequencies().length; i++) {
					termsList.add(tfv.getTerms()[i]);
					System.out.println("TERM : "+ tfv.getTerms()[i] + " FREQ : " + tfv.getTermFrequencies()[i]);
					frequencyMap.put(tfv.getTerms()[i], tfv.getTermFrequencies()[i]);
				}
				
				//put the document with doc id.
				documentMap.put(docId, doc);
			}
			
			//close the index reader;
			indexReader.close();
			
			//Boost the terms visibility in documents, so these documents more frequently for specific search terms.
			q = computeTopTermQuery(termsList, frequencyMap, AppConstants.TOP_DOCUMENTS);
			q.setBoost(2.0F);
			
			//Update the documents with their boost.
			//EasySearchIndexBuilder.updateDocuments(documentMap);

		} catch (Exception e) {
			System.out.println("Exception: performThumbsUp" + e.toString());
		}

		return q;
	}

	/**
	 * This method will perform the thumbs down action. And generate the new
	 * query based on top specific highest terms. It also decrease the relevant
	 * document boost so that their ranking is lower in search results for the
	 * similar terms.
	 */
	public static Query performThumbsDown(List<Integer> luceneDocIds) {

		Query q = null;
		
		try {
			final Map<String, Integer> frequencyMap = new HashMap<String, Integer>();
			Map<Integer, Document> documentMap = new HashMap<Integer, Document>();
			List<String> termsList = new ArrayList<String>();

			Directory indexDir = FSDirectory.open(new File(AppConstants.INDEX_DIR_PATH));
			IndexReader indexReader = IndexReader.open(indexDir);
			EasySearchIndexReader esiReader = new EasySearchIndexReader(indexReader);
			
			for (Integer docId : luceneDocIds) {
				
				TermFreqVector tfv = indexReader.getTermFreqVector(docId, "CONTENT");
				Document doc = indexReader.document(docId);
				float boost = doc.getBoost() + AppConstants.THUMBS_UP;
				doc.setBoost(boost);
				
				System.out.print("DOC : "+ docId + " Field : " + tfv.getField() + "\n");
				
				for (int i=0; i < tfv.getTermFrequencies().length; i++) {
					termsList.add(tfv.getTerms()[i]);
					System.out.println("TERM : "+ tfv.getTerms()[i] + " FREQ : " + tfv.getTermFrequencies()[i]);
					frequencyMap.put(tfv.getTerms()[i], tfv.getTermFrequencies()[i]);
				}
				
				//put the document with doc id.
				documentMap.put(docId, doc);
			}
			
			//close the index reader;
			indexReader.close();
			
			//Boost the terms visibility in documents, so these documents more frequently for specific search terms.
			q = computeTopTermQuery(termsList, frequencyMap, AppConstants.TOP_DOCUMENTS);
			q.setBoost(-2.0F);
			
			//Update the documents with their boost.
			//EasySearchIndexBuilder.updateDocuments(documentMap);

		} catch (Exception e) {
			System.out.println("Exception: performThumbsUp" + e.toString());
		}

		return q;
	}

	/**
	 * This method will perform the thumbs down action. And generate the new
	 * query based on top specific highest terms. It also decrease the relevant
	 * document boost so that their ranking is lower in search results for the
	 * similar terms.
	 */
	public static Query performPesduoRelevance(Result[] results) {

		Query q = null;
		
		try {
			final Map<String, Integer> frequencyMap = new HashMap<String, Integer>();
			Map<Integer, Document> documentMap = new HashMap<Integer, Document>();
			List<String> termsList = new ArrayList<String>();

			Directory indexDir = FSDirectory.open(new File(AppConstants.INDEX_DIR_PATH));
			IndexReader indexReader = IndexReader.open(indexDir);
			EasySearchIndexReader esiReader = new EasySearchIndexReader(indexReader);
			float boost = 0F;
			
			for (Result result : results) {
				
				TermFreqVector tfv = indexReader.getTermFreqVector(result.id, "CONTENT");
				Document doc = indexReader.document(result.id);
				boost += doc.getBoost() + AppConstants.THUMBS_UP;
				
				System.out.print("DOC : "+ result.id + " Field : " + tfv.getField() + "\n");
				
				for (int i=0; i < tfv.getTermFrequencies().length; i++) {
					termsList.add(tfv.getTerms()[i]);
					//System.out.println("TERM : "+ tfv.getTerms()[i] + " FREQ : " + tfv.getTermFrequencies()[i]);
					frequencyMap.put(tfv.getTerms()[i], tfv.getTermFrequencies()[i]);
				}
			}
			
			//close the index reader;
			indexReader.close();
			
			//Boost the terms visibility in documents, so these documents more frequently for specific search terms.
			q = computeTopTermQuery(termsList, frequencyMap, AppConstants.TOP_DOCUMENTS);
			q.setBoost(boost);
			System.out.print("Query boost : "+ boost);
			
		} catch (Exception e) {
			System.out.println("Exception: performThumbsUp" + e.toString());
		}

		return q;
	}
	
	
	/**
	 * Computes a term frequency map for the overall index at the specified location.
	 * Builds a Boolean OR query out of the "most frequent" terms in the index
	 * and returns it. "Most Frequent" is defined as the terms whose frequencies
	 * are greater than or equal to the topTermCutoff * the frequency of the top
	 * term, where the topTermCutoff is number between 0 and 1.
	 * 
	 * @param ramdir the directory where the index is created.
	 * @return a Boolean OR query.
	 * @throws Exception if one is thrown.
	 */
	private static Query computeTopTermQueryFromDataCollection(Directory ramdir, int numOf) throws Exception {
		
		final Map<String, Integer> frequencyMap = new HashMap<String, Integer>();
		List<String> termlist = new ArrayList<String>();
		IndexReader reader = IndexReader.open(ramdir);
		
		TermEnum terms = reader.terms();
		while (terms.next()) {
			Term term = terms.term();
			String termText = term.text();
			int frequency = reader.docFreq(term);
			frequencyMap.put(termText, frequency);
			termlist.add(termText);
		}
		reader.close();
		
		return computeTopTermQuery(termlist, frequencyMap, AppConstants.TOP_DOCUMENTS);
	}
	
	/**
	 * Computes a term frequency map for the specific terms at the specified location.
	 * Builds a Boolean OR query out of the "most frequent" terms in the index
	 * and returns it. "Most Frequent" is defined as the terms whose frequencies
	 * are greater than or equal to the topTermCutoff * the frequency of the top
	 * term, where the topTermCutoff is number between 0 and 1.
	 * 
	 * @param ramdir the directory where the index is created.
	 * @return a Boolean OR query.
	 * @throws Exception if one is thrown.
	 */
	private static Query computeTopTermQuery(List<String> termlist, Map<String, Integer> frequencyMap,
			int numOf) throws Exception {

		// sort the term map by frequency descending
		Collections.sort(termlist, new ReverseComparator(new ByValueComparator<String, Integer>(frequencyMap)));

		// retrieve the top terms based on topTermCutoff
		List<String> topTerms = new ArrayList<String>();
		float topFreq = -1.0F;
		for (String term : termlist) {
			if (topFreq < 0.0F) {
				// first term, capture the value
				topFreq = (float) frequencyMap.get(term);
				topTerms.add(term);
			} else {
				// not the first term, compute the ratio and discard if below
				// topTermCutoff score
				float ratio = (float) ((float) frequencyMap.get(term) / topFreq);
				if (ratio >= AppConstants.TOP_TERM_CUT_OFF_FREQ) {
					topTerms.add(term);
				} else {
					break;
				}
			}
		}

		//Top results
		List<String> topArray = null;
		if (topTerms.size() > numOf) {
			topArray = new ArrayList<String>(numOf);

			for (int position=0; position < numOf; position++) {
				topArray.add(topTerms.get(position));
			}
		}
		else {
			topArray = topTerms;
		}

		StringBuilder termBuf = new StringBuilder();
		BooleanQuery q = new BooleanQuery();
		for (String topTerm : topArray) {
			termBuf.append(topTerm).append("(").append(frequencyMap.get(topTerm)).append(");");
			q.add(new TermQuery(new Term("CONTENT", topTerm)), Occur.SHOULD);
		}
		System.out.println(">>> top terms: " + termBuf.toString());
		System.out.println(">>> query: " + q.toString());
		return q;
	}

	private static class ByValueComparator<K, V extends Comparable<? super V>> implements Comparator<K> {

		private Map<K, V> map = new HashMap<K, V>();

		public ByValueComparator(Map<K, V> map) {
			this.map = map;
		}

		public int compare(K k1, K k2) {
			return map.get(k1).compareTo(map.get(k2));
		}
	}
        
        public static Query performUpAndDown(String ids) throws IOException {
            float boosta=0.0F;
            float Boosta=0.0F;
            String[] Ids=ids.split(",");
            

		Query q = null;
		
		try {
			final Map<String, Integer> frequencyMap = new HashMap<String, Integer>();
			Map<Integer, Document> documentMap = new HashMap<Integer, Document>();
			List<String> termsList = new ArrayList<String>();

			Directory indexDir = FSDirectory.open(new File(AppConstants.INDEX_DIR_PATH));
			IndexReader indexReader = IndexReader.open(indexDir);
			EasySearchIndexReader esiReader = new EasySearchIndexReader(indexReader);
			Integer docId=0;
			for (String Id : Ids) {
				if(Id.startsWith("-")){
                                    boosta=-1.0F;
                                    Boosta=-2.0F;
                                    System.out.println("-----------------------------------------negative booster------"+Id);
                                    docId=Integer.parseInt(Id.substring(1));
                                }else{
                                    boosta=1.0F;
                                     Boosta=2.0F;
                                     System.out.println("-----------------------------------------positive booster------"+Id);
                                     docId=Integer.parseInt(Id);
                                }
                                
				TermFreqVector tfv = indexReader.getTermFreqVector(docId, "CONTENT");
				Document doc = indexReader.document(docId);
				float boost = doc.getBoost() + boosta;
				doc.setBoost(boost);
				
				System.out.print("DOC : "+ docId + " Field : " + tfv.getField() + "-----------------------------------userrelevance docs\n");
				
				for (int i=0; i < tfv.getTermFrequencies().length; i++) {
					termsList.add(tfv.getTerms()[i]);
					System.out.println("TERM : "+ tfv.getTerms()[i] + " FREQ : " + tfv.getTermFrequencies()[i]);
					frequencyMap.put(tfv.getTerms()[i], tfv.getTermFrequencies()[i]);
				}
				
				//put the document with doc id.
				documentMap.put(docId, doc);
			}
			
			//close the index reader;
			indexReader.close();
			
			//Boost the terms visibility in documents, so these documents more frequently for specific search terms.
			q = computeTopTermQuery(termsList, frequencyMap, AppConstants.TOP_DOCUMENTS);
                        
                        
			q.setBoost(Boosta);
			
			//Update the documents with their boost.
			//EasySearchIndexBuilder.updateDocuments(documentMap);

		} catch (Exception e) {
			System.out.println("Exception: performThumbsUp" + e.toString());
		}

		return q;
	}
}
