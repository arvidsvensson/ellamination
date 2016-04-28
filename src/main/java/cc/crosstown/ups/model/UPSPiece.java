package cc.crosstown.ups.model;


import org.springframework.data.annotation.Id;

public class UPSPiece {
	@Id
	private String id;
	
	private String doc;
	private String waybill;
	private String name;
	private String address;
	private String documentName;
	
	private UPSPiece() {
		// json
	}
	
	private UPSPiece(UPSRow row) {
		doc = row.getDoc();
		waybill = row.getWaybill();
		name = row.getName();
		address = row.getAddress();
		documentName = row.getDocumentName();
	}
	
	public static UPSPiece create(UPSRow row) {
		return new UPSPiece(row);
	}
	
	public String getId() {
		return id;
	}
	

	public String getDoc() {
		return doc;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((waybill == null) ? 0 : waybill.hashCode());
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
		UPSPiece other = (UPSPiece) obj;
		if (waybill == null) {
			if (other.waybill != null)
				return false;
		} else if (!waybill.equals(other.waybill))
			return false;
		return true;
	}
}
