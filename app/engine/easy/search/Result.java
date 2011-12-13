package engine.easy.search;

public class Result {

	Integer id; // the lucene's internal id
	double score; // BM25 score
	String text;	//document text;
	
	public Result(Integer id, double score, String text) {
		super();
		this.id = id;
		this.score = score;
		this.text = text;
	}
        
        public String toString(){
            return "Id"+this.id+"";
            
        }
}
