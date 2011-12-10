package engine.easy.indexer;

/**
 * This is a BasicIndexBuilder class which build a collection with 100 compressed files in zip.
 * This builder build the indexes by using the lucene.
 * 
 * Author: Adnan Urooj
 * 
 */

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import engine.easy.indexer.writer.EasySearchIndexWriter;
import engine.easy.util.Appcontants;
import engine.easy.util.FileType;
import engine.easy.util.Util;

public class BasicIndexBuilder implements IndexBuilder {
	
	public void createIndexes(String dataBankDirPath, String indexDirPath) throws IOException {
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
			
			// Step2 - Now create an index writer on this directory using StandardAnalyzer() which
			// will give you a standard lucene text analyzer,that tokenize text unit. 	
			IndexWriter indexWriter = new IndexWriter(indexDir, new StandardAnalyzer(Version.LUCENE_30), Boolean.TRUE, MaxFieldLength.LIMITED);
			
			//Step4 - Now Iterate over the collecion of files and create the index for each file.
			if (dataBank.isDirectory()) {
				for (File file : dataBank.listFiles()) {
					if (Util.getFileExtension(file).equalsIgnoreCase(FileType.ZIP)) {
						indexZipDocuments(indexWriter, file);
					} 
					else {
						indexTextDocuments(indexWriter, file);
					}
				}
			}

			indexWriter.optimize(); // Optimze the index structure, which will enhance the efficiency of index but will cost on time.
			
			indexWriter.close(); // close the indexwriter
			indexDir.close(); // close the index directory, so that the file lock will be released

		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void indexTextDocuments(IndexWriter iw, File file) throws IOException {
		
		try {
			FileReader fr = new FileReader(file);
			String docid = file.getName(); 
			System.out.println(" >> Indexing  "+ docid);
			
			// Create a document for each index document.
			Document doc = new Document();
			
			Field fdDocid = new Field("DOCID", docid, Field.Store.YES, Field.Index.NO); // This field for document id, which will be later used for identification. But this document id will not indexed so it will not be searched.
			Field fdContent = new Field("CONTENT", fr); // This field is specifically for the content, which will be not stored but indexed in order to search inside the document.
			
			doc.add(fdDocid); // Now adding this field to the document
			doc.add(fdContent); // Now adding this field to the document
			
			// add the document.
			iw.addDocument(doc);
			
			// Closed the buffer and inputstream.
			fr.close();
		} catch (Exception e) {
			System.out.println("Exception : " + e.toString());
		}
	}
	
	private void indexZipDocuments(IndexWriter iw, File file) throws IOException {
		
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
				BufferedReader bfReader = new BufferedReader(new InputStreamReader(inStream, Appcontants.UTF_8)); 
				
				// Create a document for each index document.
				Document doc = new Document();
				
				Field fdDocid = new Field("DOCID", docid, Field.Store.YES, Field.Index.NO); // This field for document id, which will be later used for identification. But this document id will not indexed so it will not be searched.
				Field fdContent = new Field("CONTENT", bfReader); // This field is specifically for the content, which will be not stored but indexed in order to search inside the document.
				
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
	
	public static void main(String[] args) {
		
		try {
			BasicIndexBuilder biBuilder = new BasicIndexBuilder();
			biBuilder.createIndexes(Appcontants.DATA_BANK_DIR_PATH, Appcontants.INDEX_DIR_PATH);
		} catch (Exception e) {
			System.out.println("Exception : " + e.toString());
		}
	}
}
