package engine.easy.search;

/**
 * This is a LuceneSearchIndex class which provides a standard search engine.
 * 
 * Author: Adnan Urooj
 * 
 */

import java.io.File;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class LuceneSearchIndex {
	
	public static void main(String[] args){
		try{
			
			// check parameters
			if(args==null || args.length!=2 || args[0]==null || args[1]==null){
				System.out.println("Invalid parameters!");
				System.out.println("To run the program in command line mode, parameters should be:");
				System.out.println("\targs[0]: path of the index directory.");
				System.out.println("\targs[1]: search query (if your query include white space, you need to use \" at both sides of the query when inputting parameters).");
				System.out.println("Also, make sure that args[0] is the index directory created in the tutorial for building index.");
				System.exit(-1);
			}
			
			// check the source directory
			File pathSrc = new File(args[0]);
			if(!pathSrc.exists()){
				System.out.println("The specified index path does not exist: "+pathSrc);
			}
			
			Directory ixdir = FSDirectory.open(new File(args[0]));
			IndexSearcher ixSearcher = new IndexSearcher(ixdir);
			QueryParser qparser = new QueryParser(Version.LUCENE_30, "CONTENT", new StandardAnalyzer(Version.LUCENE_30));
			
			// now you can use the query parser to parse the query.
			Query q = qparser.parse(args[1]);
			
			// now search for documents using the qury, results will be stored in a Hits object
			ScoreDoc[] hits = ixSearcher.search(q, null, 10).scoreDocs;
			
			// you can consider hits as an array of results and iterate the results list.
			for(int pos=0;pos<hits.length;pos++){
				int id = hits[pos].doc;
				double score = hits[pos].score; 
				Document hitDoc = ixSearcher.doc(hits[pos].doc);
				Field fd = hitDoc.getField("DOCID"); 
				String fdv = fd.stringValue(); 
				System.out.println("Result No."+pos+": Lucene id = "+id+", DOCID = "+fdv+", score = "+score);
			}
			
			// remember to close the index searcher
			ixSearcher.close();

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
