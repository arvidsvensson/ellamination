package cc.crosstown.ups.model;


import org.springframework.data.annotation.Id;

public class UPSRow {
	@Id
	private String id;

	private String doc;
	
	private String waybill;
	private String name;
	private String address;
	private String documentName;
	
	@SuppressWarnings("unused")
	private UPSRow() {
		// json
	}
	
	public UPSRow(Builder builder) {
		waybill = builder.waybill;
		name = builder.name;
		address = builder.address;
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
	

	public String getWaybill() {
		return waybill;
	}	
	
	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public String getDocumentName() {
		return documentName;
	}

	@Override
	public String toString() {
		return "Row [id=" + id 
				+ ", waybill=" + waybill 
				+ ", doc=" + documentName 
				+ ", name=" + name
				+ ", address=" + address + "]";
	}

	public static class Builder {
		private String waybill;
		private String name;
		private String address;
		private String documentName;
		
		public Builder withName(String name) {
			this.name = name;
			return this;
		}
		
		public Builder withAddress(String address) {
			this.address = address;
			return this;
		}
		
		public Builder withWaybill(String waybill) {
			this.waybill = waybill;
			return this;
		}

		public Builder withDocumentName(String documentName) {
			this.documentName = documentName;
			return this;
		}
		
		public UPSRow build() {
			if (waybill == null || waybill.trim().isEmpty()) {
				return null;
			}
			
			return new UPSRow(this);
		}
	}
}
