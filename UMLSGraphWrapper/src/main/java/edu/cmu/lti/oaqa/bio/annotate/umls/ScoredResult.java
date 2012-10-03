package edu.cmu.lti.oaqa.bio.annotate.umls;

public class ScoredResult {
	private String result;
	private double score; //from 0 to 1
	
	ScoredResult(String result, double score){
		this.result=result;
		this.score=score;
	}
	
	public String getResult() {
		return result;
	}
	
	public double getScore() {
		return score;
	}
}
