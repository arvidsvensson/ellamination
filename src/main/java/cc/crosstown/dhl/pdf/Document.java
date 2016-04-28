package cc.crosstown.dhl.pdf;

import java.util.List;

import cc.crosstown.dhl.model.Row;

public class Document {
	private final String name;
	private final List<Row> rows;
	
	public Document(String name, List<Row> rows) {
		this.name = name;
		this.rows = rows;
	}

	public String getName() {
		return name;
	}
	
	public List<Row> getRows() {
		return rows;
	}

	@Override
	public String toString() {
		return "Document [name=" + name + ", rows=" + rows + "]";
	}
}
