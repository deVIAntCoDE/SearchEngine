package engine.easy.indexer;

/**
 * This is a BasicIndexBuilder class which build a collection with 100 compressed files in zip.
 * This builder build the indexes by using the lucene.
 * 
 * Author: Adnan Urooj
 * 
 */

import java.io.File;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import engine.easy.util.Appcontants;

public class BasicIndexBuilder {
	
	public static void createIndexes() {
		try {
			
			File dataBankPath = new File(Appcontants.DATA_BANK_DIR_PATH);
			if(!dataBankPath.exists()){
				System.out.println("The specified data bank directory does not exist: " + dataBankPath);
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
			Directory indexDir = FSDirectory.getDirectory(dataBankPath);
			
			// Step2 - in this case the index directory may be locked by lucene, and you may need the following codes to unlock the directory.
			if(IndexReader.isLocked(indexDir)){
				IndexReader.unlock(indexDir);
			}
			
			// Step3 - Now create an index writer on this directory using StandardAnalyzer() which
			// will give you a standard lucene text analyzer,that tokenize text unit. 	
			IndexWriter indexWriter = new IndexWriter(indexDir, new StandardAnalyzer(), true);

			//Step4 - Now Iterate over the collecion of files and create the index for each file.
			ZipFile zipSrc = new ZipFile(dataBankPath);
			Enumeration<? extends ZipEntry> entries = zipSrc.entries();
			int entryCount = 0;

			while(entries.hasMoreElements()){
				
				ZipEntry entry = entries.nextElement();
				
				entryCount++;
				String docid = entry.getName(); 
				System.out.println(" >> Indexing "+ entryCount + "/" + zipSrc.size() + " "+ docid);
				
				// read the content of each entry
				InputStream inStream = zipSrc.getInputStream(entry);
				BufferedReader bfReader = new BufferedReader(new InputStreamReader(inStream, Appcontants.UTF_8)); 
				
				// Create a document for each index document.
				Document doc = new Document();
				
				Field fdDocid = new Field("DOCID", docid, Field.Store.COMPRESS, Field.Index.NO); // This field for document id, which will be later used for identification. But this document id will not indexed so it will not be searched.
				Field fdContent = new Field("CONTENT", bfReader); // This field is specifically for the content, which will be not stored but indexed in order to search inside the document.
				
				doc.add(fdDocid); // Now adding this field to the document
				doc.add(fdContent); // Now adding this field to the document
				
				// add the document.
				indexWriter.addDocument(doc);
				
				// Closed the buffer and inputstream.
				bfReader.close();
				inStream.close();
			}
			
			zipSrc.close(); // close the zip file

			indexWriter.optimize(); // Optimze the index structure, which will enhance the efficiency of index but will cost on time.
			
			indexWriter.close(); // close the indexwriter
			indexWriter.close(); // close the index directory, so that the file lock will be released

		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args){
		BasicIndexBuilder.createIndexes();
	}
}
