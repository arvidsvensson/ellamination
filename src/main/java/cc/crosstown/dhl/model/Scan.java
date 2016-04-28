package cc.crosstown.dhl.model;

public class Scan {
	public enum Result {
		OK, // found
		ADD, // add to CT-list
		NEED_SECOND; // need more info
	}
	
	private String first;
	private String second;
	private Piece piece;
	private Result result;
	
	@SuppressWarnings("unused")
	private Scan() {
		// json
	}

	public Scan(String first, String second, Piece piece, Result result) {
		this.first = first;
		this.second = second;
		this.piece = piece;
		this.result = result;
	}

	public String getFirst() {
		return first;
	}

	public String getSecond() {
		return second;
	}

	public Result getResult() {
		return result;
	}
	
	public Piece getPiece() {
		return piece;
	}
}
