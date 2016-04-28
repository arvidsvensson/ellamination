package cc.crosstown.dhl.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;

public class Piece {
	@Id
	private String id;
	
	private String piece;
	private String pieceNormalized;
	
	private String doc;
	private float top;
	private float bottom;
	private int page;
	private int row;
	private String receiver;
	private String address;
	private String waybill;
	private boolean charge;
	private String documentName;
	
	private Piece() {
		// json
	}
	
	private Piece(String piece, Row row) {
		this.piece = piece;
		pieceNormalized = normalize(piece);
		doc = row.getDoc();
		top = row.getTop();
		bottom = row.getBottom();
		page = row.getPage();
		this.row = row.getRow();
		receiver = row.getReceiver();
		address = row.getAddress();
		waybill = row.getWaybill();
		charge = row.isCharge();
		documentName = row.getDocumentName();
	}

	public static String normalize(String string) {
		if (string == null) {
			return null;
		}
		
		return string.replaceAll("\\s|\\W", "");
	}

	public static String denormalize(String string) {
		if (string == null) {
			return null;
		}
		
		if (string.matches("\\d{10}")) {
			return string;
		}

		StringBuilder sb = new StringBuilder();
		if (string.startsWith("J")) {
			string = string.substring(1);
			sb.append("(J)");
		} else if (string.startsWith("00")) {
			string = string.substring(2);
			sb.append("(00)");
			
		}

		for (int i = 0; i < string.length(); i++) {
			if (i % 4 == 0) {
				sb.append(" ");
			}
			sb.append(string.charAt(i));
		}
		
		string = sb.toString();
		

		return string;
	}	
	
	public static List<Piece> create(Row row) {
		List<Piece> result = new ArrayList<Piece>();
		
		List<String> pieces = row.getPieces();
		for (String name : pieces) {
			result.add(new Piece(name, row));
		}
		
		if (pieces.isEmpty() && row.getWaybill() != null) {
			result.add(new Piece(null, row));			
		}
		
		return result;
	}
	
	public String getId() {
		return id;
	}
	
	public String getPiece() {
		return piece;
	}
	
	public String getPieceNormalized() {
		return pieceNormalized;
	}

	public String getDoc() {
		return doc;
	}

	public float getTop() {
		return top;
	}

	public float getBottom() {
		return bottom;
	}

	public int getPage() {
		return page;
	}

	public int getRow() {
		return row;
	}
	
	public String getReceiver() {
		return receiver;
	}
	
	public String getAddress() {
		return address;
	}

	public String getWaybill() {
		return waybill;
	}

	public boolean isCharge() {
		return charge;
	}

	public String getDocumentName() {
		return documentName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((piece == null) ? 0 : piece.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Piece other = (Piece) obj;
		if (piece == null) {
			if (other.piece != null)
				return false;
		} else if (!piece.equals(other.piece))
			return false;
		return true;
	}
}
