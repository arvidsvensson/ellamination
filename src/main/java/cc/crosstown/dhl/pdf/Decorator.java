package cc.crosstown.dhl.pdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

import cc.crosstown.dhl.model.Doc;
import cc.crosstown.dhl.model.Piece;
import cc.crosstown.dhl.model.Route;
import cc.crosstown.dhl.model.Route.Drop;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

@Component
public class Decorator {
	private final Logger logger = Logger.getLogger("decorator");
	private static final String STATIC_GENERATED = "./static/generated";
	private final static int WAYBILL_INDEX = 0;
	private final static int PIECE_INDEX = 1;
	private final static float[][] CT_POSITIONS = new float[][] {
			{ 475.66f, 423.58f, 371.47f, 319.39f, 267.29f, 215.21f, 163.1f, 111.02f },
			{ 449.62f, 397.54f, 345.43f, 293.35f, 241.25f, 189.17f, 137.06f, 84.984f } 
		};
	
	public Decorator() {
		File generated = new File(STATIC_GENERATED);
		if (!generated.exists()) {
			generated.mkdirs();
		}
	}
	
	public List<String> generate(Route route, Iterable<Doc> docs) throws Exception {
		List<String> urls = new ArrayList<String>();

		for (final Doc doc : docs) {
			List<Drop> drops = Lists.newArrayList(Iterables.filter(route.getDrops(), new Predicate<Drop>() {
				@Override
				public boolean apply(Drop drop) {
					return doc.getId().equals(drop.getPiece().getDoc());
				}
			}));
			
			String url = decorate(route, doc, drops);
			if (url != null) {
				urls.add(url);
			}
		}
		
		List<Drop> ctDrops = Lists.newArrayList(Iterables.filter(route.getDrops(), new Predicate<Drop>() {
			@Override
			public boolean apply(Drop drop) {
				return "ct-empty".equals(drop.getPiece().getDoc());
			}
		}));
		
		logger.info("empty drops; " + ctDrops);
	
		urls.addAll(decorateCt(route, ctDrops));
		
		return urls;
	}	
	
	private List<String> decorateCt(Route route, List<Drop> ctDrops) throws FileNotFoundException, IOException, DocumentException {
		List<String> urls = new ArrayList<String>();
		
		PdfStamper stamper = null;
		PdfContentByte content = null;
		int lastPage = 0;
		for (int i = 0; i < ctDrops.size(); i++) {
			Drop drop = ctDrops.get(i);

			int page = (i / 8) + 1;
			if (page != lastPage) {
				if (lastPage != 0) {
					stamper.close();
				}
				lastPage = page;
				
				String url = "ct_empty_" + page + "_" + route.getId() + ".pdf";
				urls.add(url);
				
				FileOutputStream out = new FileOutputStream("./static/generated/" + url);
				PdfReader reader = new PdfReader(getClass().getResourceAsStream("/ct_empty.pdf"));
				stamper = new PdfStamper(reader, out);
				content = stamper.getOverContent(1);
				addRouteInfo(route, content);
			}
			
			addCt(drop, i%8 , content, route);
		}
		
		if (stamper != null) {
			stamper.close();
		}
		
		return urls;
	}
	
	private void addCt(Drop drop, int row, PdfContentByte content, Route route) {
		Piece piece = drop.getPiece();

		Font font = new Font();
		font.setSize(16);
		
		content.beginText();
		ColumnText.showTextAligned(content, Element.ALIGN_LEFT, new Phrase(
				String.valueOf(drop.getDrop()), font), 2, CT_POSITIONS[WAYBILL_INDEX][row] - 15, 0);
		content.endText();

		
		content.beginText();		
		ColumnText.showTextAligned(content, Element.ALIGN_LEFT, new Phrase(
				piece.getWaybill(), font), 172 + 5, CT_POSITIONS[WAYBILL_INDEX][row] - 15, 0);
		content.endText();

		content.beginText();
		ColumnText.showTextAligned(content, Element.ALIGN_LEFT, new Phrase(
				piece.getPiece(), font), 172 + 5, CT_POSITIONS[PIECE_INDEX][row] - 15, 0);		
		content.endText();
		
		int sames = 0;
		for (Drop allDrops : route.getDrops()) {
			if (drop.getDrop() == allDrops.getDrop()) {
				sames++;
			}
		}
		
		if (sames > 1) {
			content.beginText();
			ColumnText.showTextAligned(content, Element.ALIGN_LEFT, new Phrase(
					sames + "st", font), 172 + 150, CT_POSITIONS[0][row] - 15, 0);		
			content.endText();			
		}
	}


	private String decorate(Route route, Doc doc, List<Drop> drops) throws Exception {
		String url = doc.getId() + "_" + route.getId() + "_" + route.getDrops().size() + ".pdf";
		FileOutputStream out = new FileOutputStream("./static/generated/" + url);

		NavigableSet<Integer> pages = getPages(drops);
		PdfReader reader = new PdfReader(doc.getBytes());
		reader.selectPages(Lists.newArrayList(pages));
		
		PdfStamper stamper = new PdfStamper(reader, out);
		
		int dropCount = 0;
		int actualPage = 1;
		for (Integer page : pages) {
			PdfContentByte content = stamper.getOverContent(actualPage++);
			addRouteInfo(route, content);
			for (Drop drop : drops) {
				Piece piece = drop.getPiece();
				if (piece.getPage() == page.intValue()) {
					addDrop(content, drop, route);
					dropCount++;
				}
			}
		}
		
		stamper.close();
		return dropCount > 0 ? url : null;
	}

	private void addRouteInfo(Route route, PdfContentByte content) {
		int drops = Iterables.getLast(route.getDrops()).getDrop();
		int pieces = route.getDrops().size();
		
		content.beginText();
		ColumnText.showTextAligned(content, Element.ALIGN_LEFT, new Phrase(
				route.getName() + " (" + drops + "/" + pieces + ")"), 500, 570, 0);

		content.endText();
	}

	private void addDrop(PdfContentByte content, Drop drop, Route route) {
		content.beginText();
		
		Piece piece = drop.getPiece();

		Phrase phrase = new Phrase(String.valueOf(drop.getDrop()));
		ColumnText.showTextAligned(content, Element.ALIGN_LEFT, phrase, 5, piece.getBottom() + 5, 0);
		ColumnText.showTextAligned(content, Element.ALIGN_LEFT, phrase, 800, piece.getBottom() + 5, 0);

		int notePos = 235;
		notePos += addSames(content, notePos, drop, route);
		notePos += addCharge(content, notePos, drop);

		content.endText();
	}

	private int addCharge(PdfContentByte content, int notePos, Drop drop) {
		if (drop.getPiece().isCharge()) {
			ColumnText.showTextAligned(content, Element.ALIGN_LEFT, new Phrase("Tull"), notePos, drop.getPiece().getBottom() + 5, 0);
			return 50;
		}
		return 0;
	}

	private int addSames(PdfContentByte content, int notePos, Drop drop, Route route) {
		int sames = 0;
		for (Drop other : route.getDrops()) {
			if (other.getDrop() == drop.getDrop()) {
				sames++;
			}
		}
		if (sames > 1) {
			ColumnText.showTextAligned(content, Element.ALIGN_LEFT, new Phrase(sames + "st"), notePos, drop.getPiece().getBottom() + 5, 0);
			return 20;
		}
		return 0;
	}

	private NavigableSet<Integer> getPages(List<Drop> drops) {
		NavigableSet<Integer> pages = new TreeSet<Integer>();
		for (Drop drop : drops) {
			pages.add(Integer.valueOf(drop.getPiece().getPage()));
		}
		return pages;
	}
}
