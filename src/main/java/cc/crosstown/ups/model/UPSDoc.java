package cc.crosstown.ups.model;

import java.util.Date;

import org.springframework.data.annotation.Id;

public class UPSDoc {
	@Id
	private String id;
	
	private String name;
	
	private Date date;
	
	private byte[] bytes;
	
	@SuppressWarnings("unused")
	private UPSDoc() {
		// json
	}
	
	public UPSDoc(String name, Date date, byte[] bytes) {
		this.name = name;
		this.date = date;
		this.bytes = bytes;
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
	
	public byte[] getBytes() {
		return bytes;
	}
}
