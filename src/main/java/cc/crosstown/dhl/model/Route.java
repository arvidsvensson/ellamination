package cc.crosstown.dhl.model;

import java.util.*;

import org.springframework.data.annotation.Id;

public class Route {
	
	@Id
	private String id;
	
	private String name;
	
	private Date date;
	
	private ArrayList<Piece> pieces = new ArrayList<>();

	private List<String> urls;

	@SuppressWarnings("unused")
	private Route() {
		// json
	}
	
	public Route(String name, Date date) {
		this.name = name;
		this.date = date;
	}
		
	public Piece add(Piece piece) {
		pieces.add(piece);
		return piece;
	}

	public boolean contains(String pieceId) {
		return null != getPiece(pieceId);
	}
	
	public Piece getPiece(String pieceId) {
		for (Piece piece : pieces) {
			if (piece.getId().equals(pieceId)) {
				return piece;
			}
		}
		
		return null;
	}
	
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public Date getDate() {
		return date;
	}
	
	public List<Piece> getPieces() {
		return pieces;
	}

	public Piece delete(String pieceId) {
		Iterator<Piece> iterator = pieces.iterator();
		while (iterator.hasNext()) {
			Piece next = iterator.next();
			if (next.getId().equals(pieceId)) {
				iterator.remove();
				return next;
			}
		}
		return null;
	}

	public void setUrls(List<String> urls) {
		this.urls = urls;
	}
	
	public List<String> getUrls() {
		return urls;
	}
}
