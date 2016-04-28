package cc.crosstown.dhl.pdf;

import cc.crosstown.dhl.model.Piece;
import cc.crosstown.dhl.model.Route;
import cc.crosstown.dhl.model.Route.Drop;
import cc.crosstown.dhl.model.Row;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.Barcode39;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Decorator2 {

private static final String STATIC_GENERATED = "./static/generated";
	
	public Decorator2() {
		File generated = new File(STATIC_GENERATED);
		if (!generated.exists()) {
			generated.mkdirs();
		}
	}
	
    public static void main(String[] args) throws IOException, DocumentException {                
        Route route = new Route("Östermalm", new Date());
        add(route, 1, 1, "(J)JD014600002374483310", "6079333621", "Happy Socks AB", "Kungsgatan 12-14");
        add(route, 1, 2, "(J)JD014600002374483311", "6079333621", "Angry Socks AB", "Kungsgatan 12-15");
        add(route, 1, 3, "(J)JD014600002374483312", "6079333621", "Stinky Socks AB", "Kungsgatan 12-16");
        add(route, 1, 4, "(J)JD014600002374483313", "6079333621", "Odd Socks AB", "Kungsgatan 12-17");
        add(route, 2, 1, "(J)JD014600002374483314", "6079333621", "Old Socks AB", "Kungsgatan 12-18");
        add(route, 2, 2, "(J)JD014600002374483315", "6079333621", "New Socks AB", "Kungsgatan 12-19");
        add(route, 3, 1, "(J)JD014600002374483316", "6079333621", "Blue Socks AB", "Kungsgatan 12-20");
        add(route, 3, 2, "(J)JD014600002374483317", "6079333621", "Even Bluer Socks AB", "Kungsgatan 12-21");
        add(route, 4, 1, "(J)JD014600002374483318", "6079333621", "Dirty Socks AB", "Kungsgatan 12-22");
        add(route, 4, 2, "(J)JD014600002374483319", "6079333621", "Clean Socks AB", "Kungsgatan 12-23");
        add(route, 4, 3, "(J)JD014600002374483320", "6079333621", "Cheesy Socks AB", "Kungsgatan 12-23");
        add(route, 12, 27, "(J)JD014600002374483321", "6079333621", "Missing Socks AB", "Kungsgatan 12-23");

        new Decorator2().generate(route);
    }
    
    private static void add(Route route, int page, int row, String piece, String waybill, String receiver, String address) {
    	Row r = new Row.Builder()
	    	.withPage(page)
	    	.withRow(row)
	    	.addTo(receiver)
	    	.addTo(address)
	    	.withPiece(piece)
	    	.withDocumentName("abc")
	    	.withWaybill(waybill)
	    	.build();
    	r.setDoc("abc123");
		List<Piece> pieces = Piece.create(r);
	    
	    for (Piece p : pieces) {
			route.add(p);
		}
    }
    
    public String generate(Route route) throws IOException, DocumentException {
		String url = "DHL2-" + route.getId() + "_" + route.getDrops().size() + ".pdf";

        Document document = new Document(PageSize.A4.rotate());
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("./static/generated/" + url));
        document.open();
        PdfContentByte directContent = writer.getDirectContent();
        
        List<Piece> pieces = pieces(route);
        List<List<Piece>> pages = Lists.partition(pieces, 10);
        int pageNumber = 1;
        AtomicInteger line = new AtomicInteger(1);
        for (List<Piece> page : pages) {
			document.newPage();
			createPage(document, route.getName() + " " + pieces.size(), directContent, page, pageNumber++, pages.size(), line);
		}
        
        document.close();
        
        return url;
    }
    
    private List<Piece> pieces(Route route) {
    	return Lists.transform(route.getDrops(), new Function<Drop, Piece>() {
			@Override
			public Piece apply(Drop drop) {
				return drop.getPiece();
			}
		});
    }
    
    private void createPage(Document document, String name, PdfContentByte directContent, List<Piece> pieces, int pageNumber, int pagesTotal, AtomicInteger line) throws MalformedURLException, DocumentException, IOException {
		   createHeader(document);        
		   createBody(document, directContent, pieces, line);
		   createFooter(document, directContent, name, pageNumber, pagesTotal);    	
    }

	private void createHeader(Document document) throws DocumentException, MalformedURLException, IOException {
		Image logo = Image.getInstance(getClass().getResource("/dhl-logga.png"));
		PdfPTable table = new PdfPTable(9);
		table.setWidthPercentage(100);
		table.setWidths(new int[] {2, 2, 2, 1, 2, 2, 2, 4, 7});
		table.setSpacingAfter(10);
		
		Font font = new Font();
		font.setSize(7);
		
		PdfPCell cell = new PdfPCell();
		cell.setBorderWidth(0.5f);
		cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
		cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		cell.setBorder(PdfPCell.LEFT | PdfPCell.RIGHT | PdfPCell.TOP | PdfPCell.BOTTOM);

		cell.setPhrase(new Phrase("Delivery Date", font));
		table.addCell(cell);

		cell.setPhrase(new Phrase("24.04.2016", font));
		table.addCell(cell);
		cell.setPhrase(new Phrase("Service Center:", font));
		table.addCell(cell);
		cell.setPhrase(new Phrase("STO-", font));
		table.addCell(cell);
		cell.setPhrase(new Phrase("Courier Route ID:", font));
		table.addCell(cell);
		cell.setPhrase(new Phrase("A29A A", font));
		table.addCell(cell);
		cell.setPhrase(new Phrase("Courier ID:", font));
		table.addCell(cell);
		cell.setPhrase(new Phrase("", font));
		table.addCell(cell);
		
		cell.setBorder(PdfPCell.NO_BORDER);
		cell.setVerticalAlignment(PdfPCell.ALIGN_TOP);
		cell.setRowspan(3);
		cell.setPaddingLeft(10);
		cell.setPaddingTop(0);
		cell.setImage(logo);

		table.addCell(cell);

		cell.setRowspan(1);
		cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);

		for (int i = 0; i < 8 * 2; i++) {
			cell.setPhrase(new Phrase("", font));
			table.addCell(cell);
		}
				
		document.add(table);
	}
        	
	private Image barcode(PdfContentByte cb, Piece piece) {
		Barcode39 code39ext = new Barcode39();
		code39ext.setCode(piece.getPiece()); // TODO: normalized??
		code39ext.setStartStopText(false);
		code39ext.setTextAlignment(Element.ALIGN_BOTTOM);
		code39ext.setBarHeight(20);
		code39ext.setExtended(true);
		code39ext.setGuardBars(true);
		code39ext.setAltText(piece.getPiece() + "     " + piece.getWaybill() + "     " + piece.getPage() + " / " + piece.getRow());
		code39ext.setSize(12.5f);
		code39ext.setBaseline(13);
		Image createImageWithBarcode = code39ext.createImageWithBarcode(cb, null, null);
		
		return createImageWithBarcode;

	}
	
	private void createBody(Document document, PdfContentByte directContent, List<Piece> pieces, AtomicInteger line) throws DocumentException {
		PdfPTable table = new PdfPTable(6);
		table.setWidthPercentage(100);
		table.setWidths(new int[] {5, 7, 2, 4, 1, 5});
		
		Font small = new Font();
		small.setSize(6);
		
		PdfPCell cell = new PdfPCell();
		cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
		cell.setVerticalAlignment(PdfPCell.ALIGN_BOTTOM);
		cell.setBorder(PdfPCell.NO_BORDER);
		cell.setPhrase(new Phrase("Receiver", small)); table.addCell(cell);
		cell.setPhrase(new Phrase("                 Piece                                                          Waybill             Page / Row", small)); table.addCell(cell);
		cell.setPhrase(new Phrase("Time", small)); table.addCell(cell);
		cell.setPhrase(new Phrase("Print Name", small)); table.addCell(cell);
		cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		cell.setPaddingLeft(5);
		cell.setPhrase(new Phrase("N:o", small)); table.addCell(cell);
		cell.setPaddingLeft(0);
		cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
		cell.setPhrase(new Phrase("Signature *", small)); table.addCell(cell);
		
		for (Piece piece : pieces) {
			createDrop(table, directContent, piece, line.getAndIncrement());
		}
		
		document.add(table);
		
		table = new PdfPTable(1);
		table.setWidthPercentage(100);
		
		cell = new PdfPCell();
		cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		cell.setVerticalAlignment(PdfPCell.ALIGN_BOTTOM);
		cell.setBorder(PdfPCell.NO_BORDER);

		cell.setPhrase(new Phrase(" * Consignee confirms shipment(s) received in good order and condition", small));
		table.addCell(cell);	

		document.add(table);
	}

	private void createDrop(PdfPTable table, PdfContentByte directContent, Piece piece, int line) {
		Font large = new Font();
		large.setSize(16);

		Font medium = new Font();
		medium.setSize(12);

		Font medium2 = new Font();
		medium2.setSize(10);

		Font small = new Font();
		small.setSize(8);

		Font xsmall = new Font();
		xsmall.setSize(4);
		xsmall.setStyle(Font.ITALIC);

		PdfPCell cell = new PdfPCell();
		
		// NAME
		cell = new PdfPCell();
		cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		cell.setBorder(PdfPCell.LEFT | PdfPCell.RIGHT | PdfPCell.TOP);
		cell.setPhrase(new Phrase(piece.getReceiver(), medium));
		table.addCell(cell);
		
		// BARCODE
		cell = new PdfPCell();
		cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
		cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
//		cell.setPadding(5);
		cell.setRowspan(2);
		cell.setBorder(PdfPCell.LEFT | PdfPCell.RIGHT | PdfPCell.TOP | PdfPCell.BOTTOM);
		cell.setImage(barcode(directContent, piece));
		table.addCell(cell);	
		
		// TIME
		cell = new PdfPCell();
		cell.setBorder(PdfPCell.LEFT | PdfPCell.RIGHT | PdfPCell.TOP | PdfPCell.BOTTOM);
		cell.setPhrase(new Phrase(" ", small));
		table.addCell(cell);
		
		// PRINT NAME
		cell = new PdfPCell();
		cell.setBorder(PdfPCell.LEFT | PdfPCell.RIGHT | PdfPCell.TOP | PdfPCell.BOTTOM);
		cell.setPhrase(new Phrase(" ", small));
		table.addCell(cell);

		// NUM
		cell = new PdfPCell();
		cell.setPaddingLeft(5);
		cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		cell.setRowspan(2);
		cell.setBorder(PdfPCell.LEFT | PdfPCell.TOP | PdfPCell.BOTTOM);
		cell.setPhrase(new Phrase(String.valueOf(line), large));
		table.addCell(cell);

		// SIGN
		cell = new PdfPCell();
		cell.setRowspan(2);
		cell.setBorder(PdfPCell.RIGHT | PdfPCell.TOP | PdfPCell.BOTTOM);
		cell.setPhrase(new Phrase("", small));
		table.addCell(cell);

		// ADDRESS
		cell = new PdfPCell();
		cell.setPaddingBottom(10);
		cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		cell.setBorder(PdfPCell.LEFT | PdfPCell.RIGHT | PdfPCell.BOTTOM);
		cell.setPhrase(new Phrase(piece.getAddress(), medium2));
		table.addCell(cell);
		
		// INSTRUCTIONS
		cell = new PdfPCell();
		cell.setColspan(2);
		cell.setBorder(PdfPCell.LEFT | PdfPCell.RIGHT | PdfPCell.TOP | PdfPCell.BOTTOM);
		cell.setVerticalAlignment(PdfPCell.ALIGN_TOP);
		cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		cell.setPhrase(new Phrase("Instructions / Information", xsmall));
		table.addCell(cell);
	}
	
	private void createFooter(Document document, PdfContentByte cb, String name, int pageNumber, int pagesTotal) throws DocumentException {
		PdfPTable table = new PdfPTable(2);
		table.setWidthPercentage(100);
		table.setWidths(new int[] {10, 1});
		
		Font small = new Font();
		small.setSize(10);
		
		Font large = new Font();
		large.setSize(18);

		PdfPCell cell = new PdfPCell();
		cell.setPaddingLeft(150);
		cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		cell.setBorder(PdfPCell.NO_BORDER);

		cell.setPhrase(new Phrase(name, large));
		table.addCell(cell);
		
		cell = new PdfPCell();
		cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
		cell.setBorder(PdfPCell.NO_BORDER);

		cell.setPhrase(new Phrase("page " + pageNumber + "/" + pagesTotal, small));
		table.addCell(cell);
		
		table.setTotalWidth(document.right(document.rightMargin())
			    - document.left(document.leftMargin()));
	
		table.writeSelectedRows(0, -1,
			    document.left(document.leftMargin()),
			    table.getTotalHeight() + document.bottomMargin(), 
			    cb);
	}
	
}