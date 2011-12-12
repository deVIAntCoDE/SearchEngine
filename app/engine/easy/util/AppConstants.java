package engine.easy.util;

public interface AppConstants {

	// Main Easy Search Engine Settings 
	public static final String INDEX_DIR_PATH = "/Users/Deminem/Desktop/MSC_workspace/EasySearchEngine/data_collection/indexed_files/";
	public static final String DATA_BANK_DIR_PATH = "/Users/Deminem/Desktop/MSC_workspace/EasySearchEngine/data_collection/databank";

	// Spell checker service
	public static final String DICTIONARY_PATH = "/Users/Deminem/Desktop/MSC_workspace/EasySearchEngine/english_dictionary/fulldictionary00.txt";
	public static final String DICTIONARY_INDEX_PATH = "/Users/Deminem/Desktop/MSC_workspace/EasySearchEngine/english_dictionary/index/";

	//Content Field
	public static final String CONTENT_FIELD = "CONTENT";

	//Spell checker suggestion
	public static final int SPELL_SUGGESTIONS = 3;
	
	//Thumbs Up/Down
	public static final float THUMBS_UP = 1.0F;
	public static final float THUMBS_DOWN = -1.0F;
	
	//Number of top results
	public static final int TOP_RESULTS = 15;
	
	//Number of terms suggestion from top documents
	public static final int TOP_DOCUMENTS = 10;
	
	//Top term cut off frequency
	public static final double TOP_TERM_CUT_OFF_FREQ = 0.5F;
	
	
	public static final int USER_RELEVANCE_FEEDBACK = 0;
	public static final int PESUDO_RELEVANCE_FEEDBACK = 1;
	
	public static final String UTF_8 = "UTF-8";
	
	// BM25 constant parameters
	public static final double k1 = 1.2;
	public static final double b = 0.75;
	
	// Test Easy Search Engine Settings
	public static final String TXT_FILE = "/Users/Deminem/Desktop/MSC_workspace/EasySearchEngine/data_collection/test/Resume_txt_version.txt";

}
