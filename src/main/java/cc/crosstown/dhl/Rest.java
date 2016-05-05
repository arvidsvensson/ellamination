package cc.crosstown.dhl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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
import cc.crosstown.dhl.model.Doc;
import cc.crosstown.dhl.model.Piece;
import cc.crosstown.dhl.model.Route;
import cc.crosstown.dhl.model.Route.Drop;
import cc.crosstown.dhl.model.Row;
import cc.crosstown.dhl.model.Scan;
import cc.crosstown.dhl.model.Scan.Result;
import cc.crosstown.dhl.pdf.Document;
import cc.crosstown.dhl.pdf.PdfParser;
import cc.crosstown.dhl.pdf.Decorator2;
import cc.crosstown.ellamination.DocRepository;
import cc.crosstown.ellamination.PieceRepository;
import cc.crosstown.ellamination.RouteRepository;

import com.google.common.collect.Lists;

import org.apache.commons.io.IOUtils;

@RestController
@RequestMapping("api")
public class Rest {
	private final Logger logger = Logger.getLogger("rest");
	
	private DocRepository docs;
	private PieceRepository pieces;
	private RouteRepository routes;
	private Decorator2 decorator2;

	@Autowired
	public Rest(DocRepository docs, PieceRepository pieces, RouteRepository routes, Decorator2 decorator2) {
		this.docs = docs;
		this.pieces = pieces;
		this.routes = routes;
		this.decorator2 = decorator2;
	}

	@RequestMapping("drop")
	public String drop() throws Exception {
		docs.deleteAll();
		pieces.deleteAll();
		routes.deleteAll();

		return "Dropped all";
	}
	
	@RequestMapping("search/{term}")
	public List<Piece> searchPiece(@PathVariable("term") String term) throws Exception {
		Pattern pattern = Pattern.compile(".*" + Piece.normalize(term) + "$");
		return pieces.findByWaybillOrPieceNormalizedRegex(pattern.pattern());
	}

	@RequestMapping("route/add/{name}")
	public Route routeStart(@PathVariable("name") String name) throws Exception {
		Date date = new Date();
		Route route = routes.save(new Route(name, date));
		logger.info("route start: " + route);
		return route;
	}

	@RequestMapping("route/delete/{rid}")
	public boolean routeDelete(@PathVariable("rid") String rid) throws Exception {
		Route route = routeFind(rid);
		if (route == null) {
			return false;
		}
		
		for (Drop drop : route.getDrops()) {
			if ("ct-empty".equals(drop.getPiece().getDoc())) {
				pieces.delete(drop.getPiece().getId());
			}
		}
		routes.delete(rid);
		return true;
	}	

	@RequestMapping("route/piece/add/{rid}/{scan}")
	public Piece routeAddPiece(@PathVariable("rid") String rid, @PathVariable("scan") String scan) throws Exception {
		Route route = routeFind(rid);
		if (route == null) {
			return null;
		}
		
		Piece piece = findPiece(Piece.normalize(scan));
		if (piece == null) {
			return null;
		}
		
		if (route.contains(piece.getId())) {
			logger.info("route: piece already added: " + piece);
			return null;
		}
		
		logger.info("route add " + piece.getId());
		route.add(piece);
		
		routes.save(route);
		
		return piece;
	}

	@RequestMapping("route/piece/add/scan/{rid}/{first}")
	public Scan routeAddScan(
			@PathVariable("rid") String rid,
			@PathVariable("first") String first) throws Exception {
		return routeAddScan(rid, first, null);
	}
		
	@RequestMapping("route/piece/add/scan/{rid}/{first}/{second}")
	public Scan routeAddScan(
			@PathVariable("rid") String rid,
			@PathVariable("first") String first, 
			@PathVariable("second") String second) throws Exception {
		
		Route route = routeFind(rid);
		if (route == null) {
			return null;
		}

		second = getOptionalParam(second);
		
		String waybillScan;
		String pieceScan;
		if (first.matches("\\d{10}")) {
			waybillScan = Piece.normalize(first);
			pieceScan = Piece.normalize(second);
		} else {
			pieceScan = Piece.normalize(first);
			waybillScan = Piece.normalize(second);
		}
		
		if (second != null
				&& first.matches("\\d{10}")
				&& second.matches("\\d{10}")) {
			return new Scan(first, second, null, Result.NEED_SECOND);
		}

		if (second != null
				&& !first.matches("\\d{10}")
				&& !second.matches("\\d{10}")) {
			return new Scan(first, second, null, Result.NEED_SECOND);
		}

		Lookup lookup = scanPiece(waybillScan, pieceScan);
		
		if (lookup == null) {
			return null;
		}
				
		switch (lookup.result) {
			case NEED_SECOND:
				return new Scan(first, second, null, Result.NEED_SECOND);
				
			case ADD:
				Row row = new Row.Builder()
					.withWaybill(waybillScan)
					.withPiece(Piece.denormalize(pieceScan))
					.withRow(0)
					.build();
				row.setDoc("ct-empty");
				Piece addPiece = Piece.create(row).get(0);
				
				route.add(pieces.save(addPiece));
				routes.save(route);
				return new Scan(first, second, addPiece, Result.OK);
				
			case OK:
				Piece piece = lookup.piece;
				if (route.contains(piece.getId())) {
					logger.info("route: piece already added: " + piece);
					return null;
				}
				logger.info("route add " + piece.getId());
				route.add(piece);
				routes.save(route);
				return new Scan(first, second, piece, Result.OK);
				
			default:
				logger.info("Strange result: " + lookup.result);
				return null;
		}
	}

	private String getOptionalParam(String value) {
		return value == null || "".equals(value) ? null : value;
	}

	private Piece findPiece(String code) throws Exception {
		return pieces.findByWaybillOrPieceNormalized(Piece.normalize(code));		
	}
	private static class Lookup {
		Piece piece;
		Result result;
		
		private Lookup(Piece piece, Result result) {
			this.piece = piece;
			this.result = result;
		}
	}
	
	private Lookup scanPiece(String waybillScan, String pieceScan) throws Exception {
		if (waybillScan != null && pieceScan != null) {
			Piece piece = pieces.findByWaybillAndPieceNormalized(waybillScan, pieceScan);
			if (piece != null) {
				return new Lookup(piece, Result.OK);
			}
			return new Lookup(null, Result.ADD);
		}
		
		if (waybillScan != null) {
			List<Piece> pieces = this.pieces.findByWaybill(waybillScan);
			if (pieces.size() == 1) {
				return new Lookup(pieces.get(0), Result.OK);
			}
			return new Lookup(null, Result.NEED_SECOND);
		}
		
		if (pieceScan != null) {
			Piece piece = pieces.findByPieceNormalized(pieceScan);
			if (piece != null) {
				return new Lookup(piece, Result.OK);
			}
			
			return new Lookup(null, Result.NEED_SECOND);			
		}	
		
		// WTF ?!
		return null;
	}	

	@RequestMapping("route/piece/delete/{rid}/{id}")
	public boolean routeDeletePiece(@PathVariable("rid") String rid, @PathVariable("id") String id) throws Exception {
		Route route = routeFind(rid);
		if (route == null) {
			return false;
		}
		Piece piece = route.delete(id);
		if (piece == null) {
			return false;
		}
		
		if ("ct-empty".equals(piece.getDoc())) {
			pieces.delete(piece.getId());
		}

		logger.info("route delete " + piece.getId());
		routes.save(route);
		return true;
	}
	@RequestMapping("route/{rid}")
	public Route routeFind(@PathVariable("rid") String rid) throws Exception {	
		Route route = routes.findOne(rid);
		logger.info("route find: " + route);
		return route;
	}

	@RequestMapping("route/combine/{rid}")
	public boolean routeCombine(@PathVariable("rid") String rid) throws Exception {
		Route route = routes.findOne(rid);
		if (route == null) {
			return false;
		}
		
		if (!route.combineLast()) {
			return false;
		}
		logger.info("route combine: " + route);
		routes.save(route);
		return true;
	}

	@RequestMapping("routes")
	public List<Route> routes() {
		List<Route> routes = this.routes.findAll();
		logger.info("routes: " + routes);
		return routes;
	}

	@RequestMapping("docs")
	public List<Doc> getFiles() {
		return docs.findAll();
	}

	@RequestMapping("pieces")
	public List<Piece> pieces() {
		return pieces.findAll();
	}

	@RequestMapping("piece/{pid}")
	public Piece piece(@PathVariable("pid") String pid) {
		return pieces.findOne(pid);
	}

	@RequestMapping(value="upload", method=RequestMethod.POST)
	public ResponseEntity<String> uploadFile(
		@RequestParam("file") MultipartFile multipartFile) throws IOException, ParseException {
 
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int size = IOUtils.copy(multipartFile.getInputStream(), baos);
		
		Parser<Document> parser = new PdfParser();
		Document doc = parser.parse(new ByteArrayInputStream(baos.toByteArray()));
		
		if (null != docs.findByName(doc.getName())) {
			return new ResponseEntity<>("Already uploaded: " + doc.getName(), HttpStatus.OK);
		}
		
		Date date = dateFromDocName(doc.getName());
		
		String id = docs.save(new Doc(doc.getName(), date, baos.toByteArray())).getId();

		for (Row row : doc.getRows()) {
			row.setDoc(id);
			pieces.save(Piece.create(row));
		}
		
		return new ResponseEntity<>("Uploaded DHL'" + doc.getName() + "', " + size + " bytes", HttpStatus.OK);
	}

	private Date dateFromDocName(String name) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		return dateFormat.parse(name);
	}

	@RequestMapping("route/print/{rid}")
	public List<String> printRoute(@PathVariable("rid") String rid) throws Exception {
		Route route = routes.findOne(rid);
		if (route == null) {
			return Collections.emptyList();
		}
				
		List<String> urls = Lists.newArrayList(decorator2.generate(route));
		route.setUrls(urls);
		routes.save(route);
		return urls;
	}
}
