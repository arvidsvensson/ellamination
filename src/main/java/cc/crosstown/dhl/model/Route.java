package cc.crosstown.dhl.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Id;

import com.google.common.collect.Iterables;

public class Route {
	public static class Drop {
		private int drop;
		private Piece piece;
	
		private Drop() {
			// json
		}
		
		private Drop(int drop, Piece piece) {
			this.drop = drop;
			this.piece = piece;
		}
		
		public int getDrop() {
			return drop;
		}
		
		public Piece getPiece() {
			return piece;
		}
	}
	
	@Id
	private String id;
	
	private String name;
	
	private Date date;
	
	private ArrayList<Drop> drops = new ArrayList<>();

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
		int drop = last();
		drop = isOnSameRow(piece) ? drop : drop + 1;
		drops.add(new Drop(drop, piece));
		return piece;
	}
	
	private int last() {
		if (drops.isEmpty()) {
			return 0;
		}
		
		return Iterables.getLast(drops).drop;
	}
	
	private boolean isOnSameRow(Piece piece) {
		if (drops.isEmpty()) {
			return false;
		}
				
		Drop last = Iterables.getLast(drops);
		if (last.piece.getRow() == 0 || piece.getRow() == 0) {
			return false;
		}
			
		return last.piece.getDoc().equals(piece.getDoc())
				&& last.piece.getPage() == piece.getPage()
				&& last.piece.getRow() == piece.getRow();
	}
	
	public boolean combineLast() {
		if (drops.size() < 2) {
			return false;
		}
		
		Drop secondLast = drops.get(drops.size() - 2);
		Drop last = drops.get(drops.size() - 1);
		
		if (secondLast.drop == last.drop) {
			return false;
		}
		
		last.drop = secondLast.drop;
		return true;
	}

	public boolean contains(String pieceId) {
		return null != getPiece(pieceId);
	}
	
	public Piece getPiece(String pieceId) {
		for (Drop drop : drops) {
			if (drop.piece.getId().equals(pieceId)) {
				return drop.piece;
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
	
	public List<Drop> getDrops() {
		return drops;
	}

	public Piece delete(String pieceId) {
		int at = -1;
		for (int i = 0; i < drops.size(); i++) {
			Drop drop = drops.get(i);
			if (drop.piece.getId().equals(pieceId)) {
				at = i;
				break;
			}
		}
		
		if (at == -1) {
			return null;
		}
		
		boolean alone = isAlone(at);
		Drop removed = drops.remove(at);
		if (!alone) {
			return removed.piece;
		}
		for (int i = at; i < drops.size(); i++) {
			drops.get(i).drop--;
		}
		
		return removed.piece;
	}

	private boolean isAlone(int at) {
		Drop me = drops.get(at);
		
		for (int i = 0; i < drops.size(); i++) {
			if (i != at && drops.get(i).drop == me.drop) {
				return false;
			}
		}
		
		return true;
	}

	public Set<String> getDocIds() {
		Set<String> ids = new HashSet<>();
		for (Drop drop : drops) {
			String doc = drop.piece.getDoc();
			if (!"ct-empty".equals(doc)) {
				ids.add(doc);				
			}
		}
		
		return ids;
	}

	public void setUrls(List<String> urls) {
		this.urls = urls;
	}
	
	public List<String> getUrls() {
		return urls;
	}
}
