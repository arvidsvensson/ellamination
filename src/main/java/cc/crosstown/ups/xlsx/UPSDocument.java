package cc.crosstown.ups.xlsx;

import java.util.List;

import cc.crosstown.ups.model.UPSRow;

public class UPSDocument {
	private final String name;
	private final List<UPSRow> rows;
	
	public UPSDocument(String name, List<UPSRow> rows) {
		this.name = name;
		this.rows = rows;
	}

	public String getName() {
		return name;
	}
	
	public List<UPSRow> getRows() {
		return rows;
	}

	@Override
	public String toString() {
		return "Document [name=" + name + ", rows=" + rows + "]";
	}
}
