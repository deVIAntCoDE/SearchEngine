package engine.easy.indexer.writer;

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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;

import engine.easy.util.ArrayUtil;

public class EasySearchIndexWriter implements Closeable {

    private IndexWriter indexWriter;
    private static final String EXTRA_DATA_FIELD_NAME_SUFFIX = "_etdat";
    private static final int READ_CHAR_BUF_SIZE = 0x40000;
    private static char readCharBuf[] = new char[0x40000];

	public EasySearchIndexWriter(IndexWriter ixWriter) {
		  this.indexWriter = ixWriter;
	}

	/**
	 * Add the document in the index file.
	 * 
     * @throws IOException if the file would have any IO operation.
	 */
	public void addDocument(Document doc) throws IOException {
		addDocument(doc, indexWriter.getAnalyzer());
	}

	/**
	 * Add the document in the index file.
	 * 
     * @throws IOException if the file would have any IO operation.
	 */
	public void addDocument(Document doc, Analyzer analyzer) throws IOException {
		ArrayList extraDataFields = new ArrayList();
		List fdlist = doc.getFields();
		for (int i = 0; i < fdlist.size(); i++) {
			Object obj = fdlist.get(i);
			if (obj instanceof Field) {
				Field field = (Field) obj;
				if (field.isIndexed()) {
					int extraData[] = extraData(field, analyzer);
					byte data[] = ArrayUtil.toBytes(extraData);
					if (data != null) {
						Field dataField = new Field(
								extraDataFieldName(field.name()), data,
								org.apache.lucene.document.Field.Store.YES);
						doc.add(dataField);
						extraDataFields.add(dataField);
					}
				}
			}
		}

		indexWriter.addDocument(doc, analyzer);
		for (int i = 0; i < extraDataFields.size(); i++)
			doc.removeField(((Field) extraDataFields.get(i)).name());

		extraDataFields.clear();
	}

	public void close() throws IOException {
		close(true);
	}

	public void close(boolean flush) throws IOException {
//		if (flush)
//			indexWriter.close();
		indexWriter = null;
	}
	
	/**
	 * Get the extra data field name for a given field.
	 * 
	 * @return it returns the the extra data field name for a given field.
     * @throws IOException if the file would have any IO operation.
	 */
    public static String extraDataFieldName(String fdname)
    {
        return (new StringBuilder(String.valueOf(fdname))).append(EXTRA_DATA_FIELD_NAME_SUFFIX).toString();
    }

	/**
	 * Read the extra data field information
	 * 
	 * @return it returns the no: of token streams for the extra data field information.
     * @throws IOException if the file would have any IO operation.
	 */
    private int[] extraData(Field field, Analyzer analyzer)
        throws IOException
    {
        if(!field.isIndexed())
            return null;
        if(!field.isTokenized())
            return (new int[] {
                1, 1
            });
        String strv = field.stringValue();
        int v[];
        if(strv == null)
        {
            Reader readerv = field.readerValue();
            if(readerv == null)
            {
                TokenStream tsv = field.tokenStreamValue();
                if(tsv == null)
                {
                    throw new IllegalArgumentException((new StringBuilder("Cannot obtain field value. field_name: ")).append(field.name()).append(".").toString());
                } else
                {
                    v = countTokenStream(tsv);
                    return v;
                }
            }
            strv = readAll(readerv);
            if(strv == null)
                throw new IllegalArgumentException((new StringBuilder("Cannot obtain field value. field_name: ")).append(field.name()).append(".").toString());
            
            field.setValue(strv);
        }
        BufferedReader reader = new BufferedReader(new StringReader(strv));
        TokenStream ts = analyzer.tokenStream(field.name(), reader);
        v = countTokenStream(ts);
        ts.close();
        reader.close();
        return v;
    }

	/**
	 * Read all the information of field
	 * 
	 * @return it returns the information of given field in text.
     * @throws IOException if the file would have any IO operation.
	 */
    private String readAll(Reader reader)
        throws IOException
    {
        StringBuilder sb = new StringBuilder();
        for(int read = reader.read(readCharBuf); read != -1; read = reader.read(readCharBuf))
            sb.append(readCharBuf, 0, read);

        return sb.toString();
    }

	/**
	 * Count the token stream tokens.
	 * 
	 * @return it returns the no:of stream tokens.
     * @throws IOException if the file would have any IO operation.
	 */
    private static int[] countTokenStream(TokenStream tokenStream)
        throws IOException
    {
        int v[] = new int[2];
        HashSet countTokenStreamBuffer = new HashSet();
		TermAttribute termAtt = tokenStream.getAttribute(TermAttribute.class); 
        
		while (tokenStream.incrementToken()) {
            v[0]++;
            countTokenStreamBuffer.add(new String(termAtt.termBuffer(), 0, termAtt.termLength()));
		}

		v[1] = countTokenStreamBuffer.size();
        tokenStream.reset();
        countTokenStreamBuffer.clear();
        return v;
    }
}
