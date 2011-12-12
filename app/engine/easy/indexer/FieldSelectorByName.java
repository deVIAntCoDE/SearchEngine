package engine.easy.indexer;

/**
 * This is a FieldSelectorByName class is an extension of FieldSelector that uses only field name for selection of fields.
 * It is used for selecting fields when reading documents from the index.which build a collection with of files type zip, text, pdf, xml and html.
 * 
 * Author: Adnan Urooj
 * 
 */

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.Directory;

import engine.easy.indexer.FieldSelectorByName;

public class FieldSelectorByName implements FieldSelector {

	private static final long serialVersionUID = 0x711a19e87e9f25a6L;
	private HashSet validFieldNames;
	
	private FieldSelectorByName() {
		validFieldNames = new HashSet();
	}

	public FieldSelectorByName(String validFieldNames[]) {
		this();
		String as[];
		int j = (as = validFieldNames).length;
		for (int i = 0; i < j; i++) {
			String fdname = as[i];
			this.validFieldNames.add(fdname);
		}
	}

	public FieldSelectorByName(Collection validFieldNames) {
		this();
		String fdname;
		for (Iterator iterator = validFieldNames.iterator(); iterator.hasNext(); this.validFieldNames
				.add(fdname))
			fdname = (String) iterator.next();
	}

	public boolean isValid(String fdname) {
		return validFieldNames.contains(fdname);
	}

	public void addAValidName(String fdname) {
		validFieldNames.add(fdname);
	}

	public FieldSelectorResult accept(String fieldName) {
		if (validFieldNames.contains(fieldName))
			return FieldSelectorResult.LOAD;
		else
			return FieldSelectorResult.NO_LOAD;
	}
}