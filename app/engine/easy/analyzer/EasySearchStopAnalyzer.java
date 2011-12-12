package engine.easy.analyzer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.CharArraySet;

/**
 * This is a EasySearchStopAnalyzer contains all the stop words which will be used for analyzer.
 * 
 * Author: Adnan Urooj
 * 
 */
public class EasySearchStopAnalyzer {

	public static final Set ENGLISH_STOP_WORDS_SET;
	
	static {
		final List<String> stopWords = Arrays.asList(
				"a","about","above","across","actually","add","added","after","again","against","ago","all","almost","along","already","also","although","always","am","among","an","and","another","any","anyone","are","around","as","asked","at","b","back","bad","be","became","because","become","been","before","began","behind","being","best","better","between","big","biggest","both","brought","but","by","c","called","came","can","cannot","cent","come","complete","continued",
				"could","d","day","decided","declared","despite","did","do","does","down","during","e","each","early","eight","enough","entire","ep","etc","even","ever","every","everything","f","face","faced","fact","failed","far","fell","few","finally","find","first","five","for","found","four","from","g","gave","get","give","given","go","going","good","got","h","had","has","have","having","he","held","her","here","him","himself","his","hour","hours","how","however","i","idea","if","in","including",
				"instead","into","is","it","its","itself","j","k","keep","know","known","knows","l","lack","last","later","least","led","less","let","like","little","long","longer","look","lot","m","made","make","making","man","many","matter","may","me","means","men","might","miles","million","moment","month","months","more","morning","most","much","must","my","n","named","near","nearly","necessary","need","needed","needs","never","night","no","nor","not","note","nothing","now","o","of","off","often",
				"on","once","one","only","or","other","others","our","out","outside","over","own","p","page","part","past","per","perhaps","place","point","proved","put","q","qm","question","r","really","recent","recently","reported","round","s","said","same","say","says","sec","second","section","see","seemed","seems","sense","set","sets","seven","she","short","should","showed","since","single","six","small","so","some","soon","start","started","still","such","t","take","taken","takes","ten","text",
				"than","that","the","their","them","themselves","then","there","these","they","thing","things","third","this","those","though","thought","thousands","three","through","thus","time","tiny","to","today","together","told","too","took","toward","two","u","under","until","up","upon","us","use","used","v","very","w","warning","was","way","we","week","weeks","well","went","were","what","when","where","whether","which","while","who","whom","whose","why","will","with","without","word","words",
				"would","x","y","year","years","yet","you","your","z");
		
		final CharArraySet stopSet = new CharArraySet(stopWords.size(), false);
		stopSet.addAll(stopWords);  
		ENGLISH_STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet); 
	}
}
