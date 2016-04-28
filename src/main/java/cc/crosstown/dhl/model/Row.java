package cc.crosstown.dhl.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;

public class Row {
	@Id
	private String id;

	private String doc;
	
	private float top;
	private float bottom;
	private int page;
	private int row;
	private String receiver;
	private String address;
	private String waybill;
	private List<String> pieces;
	private boolean charge;
	private String documentName;
	
	@SuppressWarnings("unused")
	private Row() {
		// json
	}
	
	public Row(Builder builder) {
		top = builder.top;
		bottom = builder.bottom;
		page = builder.page;
		row = builder.row;
		receiver = builder.receiver;
		address = builder.address;
		waybill = builder.waybill;
		pieces = builder.pieces;
		charge = builder.charge;
		documentName = builder.documentName;
	}
	
	public String getId() {
		return id;
	}
	
	public String getDoc() {
		return doc;
	}
	
	public void setDoc(String doc) {
		this.doc = doc;
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

	public List<String> getPieces() {
		return pieces;
	}

	public boolean isCharge() {
		return charge;
	}
	
	public String getDocumentName() {
		return documentName;
	}

	@Override
	public String toString() {
		return "Row [id=" + id + ", page=" + page + ", row=" + row
				+ ", receiver=" + receiver + ", address=" + address
				+ ", waybill=" + waybill + ", pieces=" + pieces + ", charge="
				+ charge + ", doc=" + documentName + ", top=" + top
				+ ", bottom=" + bottom + "]";
	}

	public static class Builder {
		private float top;
		private float bottom;
		private int page;
		private int row;
		private String receiver;
		private String address;
		private String waybill;
		private List<String> pieces = new ArrayList<String>();
		private boolean charge;
		private String documentName;
		
		public Builder withTop(float top) {
			this.top = top;
			return this;
		}

		public Builder withBottom(float bottom) {
			this.bottom = bottom;
			return this;
		}

		public Builder withPage(int page) {
			this.page = page;
			return this;
		}

		public Builder withRow(int row) {
			this.row = row;
			return this;
		}

		public Builder addTo(String text) {
			if (receiver == null) {
				receiver = text;
			} else if (address == null) {
				address = text;
			}
			
			return this;
		}

		public Builder withWaybill(String waybill) {
			this.waybill = waybill;
			return this;
		}

		public Builder withPiece(String piece) {
			pieces.add(piece);
			return this;
		}

		public Builder withCharge(boolean charge) {
			this.charge = charge;
			return this;
		}
		
		public Builder withDocumentName(String documentName) {
			this.documentName = documentName;
			return this;
		}
		
		public Row build() {
			return new Row(this);
		}
	}
}
