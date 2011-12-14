package engine.easy.search;

public class Result {

	Integer id; // the lucene's internal id
	String docId;
	double score; // BM25 score
	String higlightedText;	//document text;
	
	public Result(Integer id, String docId, double score, String text) {
		super();
		this.id = id;
		this.docId = docId;
		this.score = score;
		this.higlightedText = text;
	}

	@Override
	public String toString() {
		return "Result [id=" + id + ", docId=" + docId + ", score=" + score
				+ ", higlightedText=" + higlightedText + "]";
	}


}
