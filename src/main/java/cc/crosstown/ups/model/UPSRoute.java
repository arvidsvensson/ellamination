package cc.crosstown.ups.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;

public class UPSRoute {
	public static class Drop {
		private String scan;
		private String line1;
		private String line2;
	
		private Drop() {
			// json
		}

		private Drop(String scan, String line1, String line2) {
			this.scan = scan;
			this.line1 = line1;
			this.line2 = line2;
		}

		public String getScan() {
			return scan;
		}

		public String getLine1() {
			return line1;
		}

		public String getLine2() {
			return line2;
		}
	}
	
	@Id
	private String id;
	
	private String name;
	
	private Date date;
	
	private ArrayList<Drop> drops = new ArrayList<Drop>();
	
	private String url;

	@SuppressWarnings("unused")
	private UPSRoute() {
		// json
	}
	
	public UPSRoute(String name, Date date) {
		this.name = name;
		this.date = date;
	}
		
	public Drop add(String scan, String line1, String line2) {
		Drop drop = new Drop(scan, line1, line2);
		drops.add(drop);
		return drop;
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

	public Drop delete(String scan) {
		for (Drop drop : drops) {
			if (drop.scan.equals(scan)) {
				drops.remove(drop);
				return drop;
			}
		}
		
		return null;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}	
}
