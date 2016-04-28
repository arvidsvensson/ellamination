package cc.crosstown.common;

import org.springframework.data.annotation.Id;

public class Settings {
	@Id
	private String id;
	
	private int startRow;
	private int waybillColumn;
	private int nameColumn;
	private int addressColumn;
		
	@SuppressWarnings("unused")
	private Settings() {
		// json
	}
	
	public Settings(int startRow, int waybillColumn, int nameColumn, int addressColumn) {
		this.startRow = startRow;
		this.waybillColumn = waybillColumn;
		this.nameColumn = nameColumn;
		this.addressColumn = addressColumn;
	}
	
	public int getStartRow() {
		return startRow;
	}
	
	public String getId() {
		return id;
	}
		
	public int getWaybillColumn() {
		return waybillColumn;
	}
	
	public int getNameColumn() {
		return nameColumn;
	}
	
	public int getAddressColumn() {
		return addressColumn;
	}

	@Override
	public String toString() {
		return "Settings [startRow= " + startRow + ", waybillColumn=" + waybillColumn + ", nameColumn="
				+ nameColumn + ", addressColumn=" + addressColumn + "]";
	}
}
