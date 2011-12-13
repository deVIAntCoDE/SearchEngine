package engine.easy.indexer;

/**
 * This is a EasySearchIndexBuilder class which build a collection with of files type zip, text, pdf, xml and html.
 * This builder build the indexes by using the custom @EasySearchAnalyzer.
 * 
 * Author: Adnan Urooj
 * 
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import engine.easy.analyzer.EasySearchAnalyzer;
import engine.easy.indexer.writer.EasySearchIndexWriter;
import engine.easy.util.AppConstants;
import engine.easy.util.FileType;
import engine.easy.util.Util;

public class EasySearchIndexBuilder  {
	
	/**
	 * Create the index for given collection of documents in the data bank.
	 * 
     * @throws IOException if the file would have any IO operation.
	 */
	public static void createIndexes(String dataBankDirPath, String indexDirPath) throws IOException {
		try {
			
			File dataBank = new File(dataBankDirPath);
                       
			if(!dataBank.exists()){
				System.out.println("The specified data bank directory does not exist: " + dataBank);
			}
			
			/*
			 * Steps to create the lucene basic index builder using - StandardAnalyzer
			 * 
			 * 1- First step is to create a directory in lucene.
			 * 2- Confirm that previously index writer is not closed properly, then unlock the directory.
			 * 3- Now create an index writer on this directory.
			 * 4- After that create an zip iterator for the collection of files in data bank and create
			 * 	  the index for each file.
			 */

			// Step1 - Create the index directory for given path.
			Directory indexDir = FSDirectory.open(new File(indexDirPath));
			
			// Step2 - in this case the index directory may be locked by lucene, and you may need the following codes to unlock the directory.
			if(IndexWriter.isLocked(indexDir)){
				IndexWriter.unlock(indexDir);
			}
			
			// Step3 - Now create an index writer on this directory using EasySearchAnalyzer() which
			// will give you a custom text analyzer, that tokenize the text units.
			IndexWriter indexWriter = new IndexWriter(indexDir, new EasySearchAnalyzer(), Boolean.TRUE, MaxFieldLength.UNLIMITED);
			EasySearchIndexWriter esiWrtier = new EasySearchIndexWriter(indexWriter);
			
			//Step4 - Now Iterate over the collecion of files and create the index for each file.
			if (dataBank.isDirectory()) {
				for (File file : dataBank.listFiles()) {
					if (Util.getFileExtension(file).equalsIgnoreCase(FileType.ZIP)) {
						indexZipDocuments(esiWrtier, file);
					} 
					else {
						indexTextDocuments(esiWrtier, file);
					}
				}
			}

			esiWrtier.close(); 
			indexWriter.optimize(); // Optimze the index structure, which will enhance the efficiency of index but will cost on time.
			System.out.println(" >> Finished .... ");
			
			indexWriter.close(); // close the indexwriter
			indexDir.close(); // close the index directory, so that the file lock will be released

		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private static String getText(Reader reader) throws IOException {
		
		StringBuffer sb = null;
		
		try {
			if (reader != null) {
				sb = new StringBuffer("");
				BufferedReader br = new BufferedReader(reader); 
				String s; 
				while((s = br.readLine()) != null) { 
					sb.append(s);
				} 
			}
		} catch (Exception e) {
			System.out.println("Exception : getText" + e.toString());
		}
		
		System.out.println(sb.toString());
		return sb.toString();
	}

	private static void indexTextDocuments(EasySearchIndexWriter iw, File file) throws IOException {
		
		try {
			FileReader fr = new FileReader(file);
			String docid = file.getName(); 
			System.out.println(" >> Indexing  "+ docid);
			
			// Create a document for each index document.
			Document doc = new Document();
			
			Field fdDocid = new Field("DOCID", docid, Field.Store.YES, Field.Index.NO); // This field for document id, which will be later used for identification. But this document id will not indexed so it will not be searched.
			Field fdContent = new Field(AppConstants.CONTENT_FIELD, getText(fr), Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES);

			doc.add(fdDocid); // Now adding this field to the document
			doc.add(fdContent); // Now adding this field to the document
			
			// add the document.
			iw.addDocument(doc);
			
			// Closed the buffer and inputstream.
			fr.close();
		} catch (Exception e) {
			System.out.println("Exception : indexTextDocuments" + e.toString());
		}
	}
	
	private static void indexZipDocuments(EasySearchIndexWriter iw, File file) throws IOException {
		
		try {
			ZipFile zipSrc = new ZipFile(file);
			Enumeration<? extends ZipEntry> entries = zipSrc.entries();
			int entryCount = 0;

			while(entries.hasMoreElements()){
				
				ZipEntry entry = entries.nextElement();
				
				entryCount++;
				String docid = entry.getName(); 
				System.out.println(" >> Indexing "+ entryCount + "/" + zipSrc.size() + " "+ docid);
				
				// read the content of each entry
				InputStream inStream = zipSrc.getInputStream(entry);
				BufferedReader bfReader = new BufferedReader(new InputStreamReader(inStream, AppConstants.UTF_8)); 
				
				// Create a document for each index document.
				Document doc = new Document();
				
				Field fdDocid = new Field("DOCID", docid, Field.Store.YES, Field.Index.NO); // This field for document id, which will be later used for identification. But this document id will not indexed so it will not be searched.
				Field fdContent = new Field(AppConstants.CONTENT_FIELD, getText(bfReader), Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES); // This field is specifically for the content, which will be not stored but indexed in order to search inside the document.
				
				doc.add(fdDocid); // Now adding this field to the document
				doc.add(fdContent); // Now adding this field to the document
				
				// add the document.
				iw.addDocument(doc);
				
				// Closed the buffer and inputstream.
				bfReader.close();
				inStream.close();
			}
			
			zipSrc.close(); // close the zip file
		} catch (Exception e) {
			System.out.println("Exception : " + e.toString());
		}
	}
	
	public static void updateDocuments(Map<Integer, Document> docsMap) {
		
		try {
			if (!docsMap.isEmpty()) {
				Directory indexDir = FSDirectory.open(new File(AppConstants.INDEX_DIR_PATH));
				IndexWriter indexWriter = new IndexWriter(indexDir, new EasySearchAnalyzer(), Boolean.TRUE, MaxFieldLength.UNLIMITED);
				EasySearchIndexWriter esiWrtier = new EasySearchIndexWriter(indexWriter);
				
				if(IndexWriter.isLocked(indexDir)){
					IndexWriter.unlock(indexDir);
				}
				
				for (Integer docId : docsMap.keySet()) {
					Document doc = docsMap.get(docId);
					indexWriter.updateDocument(new Term("DOCID", docId.toString()), doc);
				}

				indexWriter.optimize();
				indexWriter.commit();
				indexWriter.close();
			}
		} catch (Exception e) {
			System.out.println("Exception : " + e.toString());
		}
	}
	
	public static void main(String[] args) {
		
		try {
			EasySearchIndexBuilder biBuilder = new EasySearchIndexBuilder();
			biBuilder.createIndexes(AppConstants.DATA_BANK_DIR_PATH, AppConstants.INDEX_DIR_PATH);
		} catch (Exception e) {
			System.out.println("Exception : " + e.toString());
		}
	}
}
