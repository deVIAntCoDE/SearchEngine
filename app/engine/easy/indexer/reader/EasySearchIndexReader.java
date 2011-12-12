package engine.easy.indexer.reader;

/**
 * <p>
 * This is a EasySearchIndexWriter class which extends the properties of lucene IndexWriter.
 * EasySearchIndexWriter is basically written for implementing the ranking language models BM25.
 * </p>
 * 
 * <p>
 * It will collect the extra information of the fields and append the information to the index with particular unique
 * extra filed which will be named as _extdat. All the indexes created through EasySearchIndexWriter is compaitable
 * with Lucene's index  format, and can be read and search by Lucene's standard index reader. However the extra field information
 * will not be read or search without EasySearchIndexWriter.
 * </p>
 * 
 * Author: Adnan Urooj
 * 
 */

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.Directory;

import engine.easy.indexer.writer.EasySearchIndexWriter;
import engine.easy.util.ArrayUtil;

public class EasySearchIndexReader implements Closeable {

	private IndexReader ixReader;
    private static HashMap fieldLengths = new HashMap();
    private static HashMap uniqueTermCounts = new HashMap();
    private static HashMap fieldRecordCounts = new HashMap();
    
	public EasySearchIndexReader(IndexReader ixReader) {
		 this.ixReader = ixReader;
	}

	public int uniqTermCount(String fdname) throws IOException {
		if (uniqueTermCounts.containsKey(fdname))
			return ((Integer) uniqueTermCounts.get(fdname)).intValue();

		TermEnum tmEnum = ixReader.terms();
		int tmNum = 0;
		while (tmEnum.next()) {
			String field = tmEnum.term().field();
			if (field.equals(fdname))
				tmNum++;
		}
		tmEnum.close();
		uniqueTermCounts.put(fdname, Integer.valueOf(tmNum));
		return tmNum;
	}

	/**
	 * Get the data field name for a given field.
	 * 
	 * @return it returns the the extra data field name for a given field.
     * @throws IOException if the file would have any IO operation.
	 */
    public static String getFieldName(String fdname)
    {
        return getFieldName(fdname);
    }
    
	public long fieldLength(String fdname) throws IOException {
		if (fieldLengths.containsKey(fdname))
			return ((Long) fieldLengths.get(fdname)).longValue();
		int recCount = 0;
		long fdLength = 0L;
		int maxDocid = ixReader.maxDoc();
		for (int docid = 0; docid < maxDocid; docid++) {
			int info[] = loadExtraData(docid, fdname);
			if (info != null && info.length != 0) {
				fdLength += info[0];
				recCount++;
			}
		}

		fieldRecordCounts.put(fdname, Integer.valueOf(recCount));
		fieldLengths.put(fdname, Long.valueOf(fdLength));
		return fdLength;
	}

	public double avgFieldLength(String fdname) throws IOException {
		long tmLen = fieldLength(fdname);
		int recNum = recordCount(fdname);
		if (recNum == 0)
			return 0.0D;
		else
			return ((double) tmLen * 1.0D) / (double) recNum;
	}

	public int recordCount(String fdname) throws IOException {
		if (fieldRecordCounts.containsKey(fdname))
			return ((Integer) fieldRecordCounts.get(fdname)).intValue();
		int recCount = 0;
		long fdLength = 0L;
		int maxDocid = ixReader.maxDoc();
		for (int docid = 0; docid < maxDocid; docid++) {
			int info[] = loadExtraData(docid, fdname);
			if (info != null && info.length != 0) {
				fdLength += info[0];
				recCount++;
			}
		}

		fieldRecordCounts.put(fdname, Integer.valueOf(recCount));
		fieldLengths.put(fdname, Long.valueOf(fdLength));
		return recCount;
	}

	public int docLength(int docid, String fdname) throws IOException {
		int info[] = loadExtraData(docid, fdname);
		if (info == null || info.length == 0)
			return 0;
		else
			return info[0];
	}

	public int docUniqueTermCount(int docid, String fdname) throws IOException {
		int info[] = loadExtraData(docid, fdname);
		if (info == null || info.length == 0)
			return 0;
		else
			return info[1];
	}

	public long frequency(Term term) throws IOException {
		TermDocs docs = ixReader.termDocs(term);
		if (docs == null)
			return 0L;
		long freq;
		for (freq = 0L; docs.next(); freq += docs.freq());
		return freq;
	}

	private synchronized int[] loadExtraData(int docid, String fdname)
			throws IOException {
		String extraDataFieldName = EasySearchIndexWriter.extraDataFieldName(fdname);
		Document doc = ixReader.document(docid, new engine.easy.indexer.FieldSelectorByName(
				new String[] { extraDataFieldName }));
		if (doc == null)
			return null;
		Field fd = doc.getField(extraDataFieldName);
		if (fd == null)
			return null;
		byte v[] = fd.getBinaryValue();
		if (v == null)
			return null;
		else
			return ArrayUtil.toInts(v);
	}

    public void close() throws IOException
    {
        ixReader = null;
    }
}
