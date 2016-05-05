package cc.crosstown.ups;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cc.crosstown.common.Parser;
import cc.crosstown.common.Settings;
import cc.crosstown.ellamination.SettingsRepository;
import cc.crosstown.ellamination.UPSDocRepository;
import cc.crosstown.ellamination.UPSPieceRepository;
import cc.crosstown.ellamination.UPSRouteRepository;
import cc.crosstown.ups.model.UPSDoc;
import cc.crosstown.ups.model.UPSPiece;
import cc.crosstown.ups.model.UPSRoute;
import cc.crosstown.ups.model.UPSRoute.Drop;
import cc.crosstown.ups.model.UPSRow;
import cc.crosstown.ups.pdf.UPSDecorator;
import cc.crosstown.ups.xlsx.UPSDocument;
import cc.crosstown.ups.xlsx.UPSParser;

@RestController
@RequestMapping("api/ups")
public class UPSRest {
	private final Logger logger = Logger.getLogger("ups-rest");
	
	private SettingsRepository settings;
	private UPSDocRepository docs;
	private UPSRouteRepository routes;
	private UPSPieceRepository pieces;
	private UPSDecorator decorator;

	@Autowired
	public UPSRest(SettingsRepository settings, UPSDocRepository docs, UPSRouteRepository routes, UPSPieceRepository pieces, UPSDecorator decorator) {
		this.settings = settings;
		this.docs = docs;
		this.routes = routes;
		this.pieces = pieces;
		this.decorator = decorator;
	}

	@RequestMapping("drop")
	public String drop() throws Exception {
		docs.deleteAll();
		routes.deleteAll();
		pieces.deleteAll();

		return "Dropped all";
	}
	
	@RequestMapping("drop/settings")
	public String dropSettings() throws Exception {
		settings.deleteAll();

		return "Dropped settings";
	}
	
	@RequestMapping("settings")
	public Settings getSettings() throws Exception {
		Settings result;
		if (settings.count() == 0) {
			result = settings.save(new Settings(1, 1,  5,  6)); // defaults...			
		}
		result = settings.findAll().get(0);
		logger.info("get setting " + result);
		return result;
	}
	
	@RequestMapping(value = "settings/{startRow}/{waybill}/{name}/{address}")
	public Settings setSettings(
			@PathVariable("startRow") int startRow, 
			@PathVariable("waybill") int waybill, 
			@PathVariable("name") int name, 
			@PathVariable("address") int address) throws Exception {
		if (settings.count() > 0) {
			settings.deleteAll();
		}
		Settings result = settings.save(new Settings(startRow, waybill, name, address));
		logger.info("set setting " + result);
		return result;
	}

	@RequestMapping("route/add/{name}")
	public UPSRoute routeStart(@PathVariable("name") String name) throws Exception {
		Date date = new Date();
		UPSRoute route = routes.save(new UPSRoute(name, date));
		logger.info("route start: " + route);
		return route;
	}

	@RequestMapping("route/delete/{rid}")
	public boolean routeDelete(@PathVariable("rid") String rid) throws Exception {
		UPSRoute route = routeFind(rid);
		if (route == null) {
			return false;
		}
				
		routes.delete(rid);
		return true;
	}	
	
	private boolean has(String... strings) {
		for (String string : strings) {
			if (string == null || string.isEmpty() || "undefined".equals(string)) {
				return false;
			}
		}
		return true;
	}

	@RequestMapping("route/piece/add/scan/{rid}/{scan}/{line1}/{line2}")
	public Drop routeAddPiece(@PathVariable("rid") String rid, @PathVariable("scan") String scan, @PathVariable("line1") String line1, @PathVariable("line2") String line2) throws Exception {
		UPSRoute route = routeFind(rid);
		if (route == null) {
			return null;
		}
		
		if (!has(scan)) {
			return null;
		}		
		
		if (has(line1, line2)) {
			Drop drop = route.add(scan, line1, line2);			
			if (drop == null) {
				return null;
			}
			routes.save(route);			
			return drop;
		}
		
		for(UPSPiece piece : pieces.findByWaybill(scan)) {
			Drop drop = route.add(scan, trim(piece.getName()), trim(piece.getAddress()));
			if (drop == null) {
				return null;
			}
			routes.save(route);
			return drop;
		}
		
		return null;
	}
	
	private String trim(String string) {
		return string == null ? "" : string.trim();
	}
//	@RequestMapping("route/piece/add/scan/{rid}/{scan}/{line1}/{line2}")
//	public Drop routeAddPiece(@PathVariable("rid") String rid, @PathVariable("scan") String scan, @PathVariable("line1") String line1, @PathVariable("line2") String line2) throws Exception {
//		UPSRoute route = routeFind(rid);
//		if (route == null) {
//			return null;
//		}
//		
//		Drop drop = route.add(scan, line1, line2);
//		if (drop == null) {
//			return null;
//		}
//		
//		
//		routes.save(route);
//		
//		return drop;
//	}
	
	@RequestMapping("route/piece/delete/{rid}/{scan}")
	public boolean routeDeletePiece(@PathVariable("rid") String rid, @PathVariable("scan") String scan) throws Exception {
		UPSRoute route = routeFind(rid);
		if (route == null) {
			return false;
		}
		Drop piece = route.delete(scan);
		if (piece == null) {
			return false;
		}
		
		routes.save(route);
		return true;
	}
	
	@RequestMapping("route/{rid}")
	public UPSRoute routeFind(@PathVariable("rid") String rid) throws Exception {	
		UPSRoute route = routes.findOne(rid);
		logger.info("route find: " + route);
		return route;
	}

	@RequestMapping("routes")
	public List<UPSRoute> routes() {
		List<UPSRoute> routes = this.routes.findAll();
		logger.info("routes: " + routes);
		return routes;
	}

	@RequestMapping("route/print/{rid}")
	public String printRoute(@PathVariable("rid") String rid) throws Exception {
		UPSRoute route = routes.findOne(rid);
		if (route == null) {
			return null;
		}
				
		String url = decorator.generate(route);
		route.setUrl(url);
		routes.save(route);
		return url;
	}	
	
	@RequestMapping("pieces")
	public List<UPSPiece> pieces() {
		return pieces.findAll();
	}
	@RequestMapping("docs")
	public List<UPSDoc> getFiles() {
		return docs.findAll();
	}

	
	@RequestMapping(value="upload", method=RequestMethod.POST)
	public ResponseEntity<String> uploadFile(
		@RequestParam("file") MultipartFile multipartFile) throws Exception {
 
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int size = IOUtils.copy(multipartFile.getInputStream(), baos);
		
		Parser<UPSDocument> parser = new UPSParser(getSettings());
		UPSDocument doc = parser.parse(new ByteArrayInputStream(baos.toByteArray()));
		
		if (null != docs.findByName(doc.getName())) {
			return new ResponseEntity<>("Already uploaded: " + doc.getName(), HttpStatus.OK);
		}
		
		Date date = new Date();
		
		String id = docs.save(new UPSDoc(doc.getName(), date, baos.toByteArray())).getId();

		for (UPSRow row : doc.getRows()) {
			row.setDoc(id);
			pieces.save(UPSPiece.create(row));
		}
		
		return new ResponseEntity<>("Uploaded UPS'" + doc.getName() + "', " + size + " bytes", HttpStatus.OK);
	}
}
