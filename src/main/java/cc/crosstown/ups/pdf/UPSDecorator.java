package cc.crosstown.ups.pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import cc.crosstown.ups.model.UPSRoute;
import cc.crosstown.ups.model.UPSRoute.Drop;

import com.google.common.collect.Lists;
import com.itextpdf.awt.geom.AffineTransform;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.Barcode39;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

@Component
public class UPSDecorator {
	private static final String STATIC_GENERATED = "./static/generated";
	
	public UPSDecorator() {
		File generated = new File(STATIC_GENERATED);
		if (!generated.exists()) {
			generated.mkdirs();
		}
	}
	
	public String generate(UPSRoute route) throws Exception {
		Document document = new Document(PageSize.A4, 50, 50, 50, 50);
		String url = "UPS-" + route.getId() + "_" + route.getDrops().size() + ".pdf";
		
		FileOutputStream outputStream = new FileOutputStream("./static/generated/" + url);
		PdfWriter writer = PdfWriter.getInstance(document, outputStream);
		document.open();
		PdfContentByte cb = writer.getDirectContent();
		
		int page = 1;
		List<List<Drop>> pages = Lists.partition(route.getDrops(), 11);
		for (List<Drop> drops : pages) {
			document.newPage();
			AffineTransform at = AffineTransform.getScaleInstance(.95, .95);
			at.translate(15, 15);
			cb.transform(at);
			createPage(cb, route, drops, page++, pages.size());
		}
		
		document.close();
		writer.close();
		outputStream.close();
		
		return url;
	}	
	
	private void createPage(PdfContentByte cb,UPSRoute route, List<Drop> drops, int page, int pages) throws DocumentException {		
		float offset = PageSize.A4.getHeight() - 30;

		Font font = new Font();
		font.setSize(10);

		cb.beginText();
		ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(
				"Cyklist:", font), 10, offset, 0);
		cb.endText();
		
		cb.beginText();
		ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(
				"Leverantör: 1CROSS", font), 10, offset - 20, 0);
		cb.endText();

//		cb.beginText();
//		ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(
//				"Datum: " + new SimpleDateFormat("yyyy-MM-dd").format(new Date()), font), 200, offset, 0);
//		cb.endText();
		cb.beginText();
		ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(
				"Datum: " + new SimpleDateFormat("yyyy-MM-dd").format(new Date()), font), 200, offset - 20, 0);
		cb.endText();
		
//		cb.beginText();
//		ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(
//				"Slinga: ", font), 320, offset, 0);
//		cb.endText();
//
//		font.setSize(18);
//		font.setStyle(Font.BOLD);
//		cb.beginText();
//		ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(
//				route.getName() + " (" + route.getPieces().size() + ") " + page + "/" + pages, font), 360, offset, 0);
//		cb.endText();
		
		cb.beginText();
		ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(
				"Slinga: ", font), 200, offset, 0);
		cb.endText();
		
		font.setSize(16);
		cb.beginText();
		ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(page + "/" + pages, font), 550, offset, 0);
		cb.endText();

		font.setSize(22);
		font.setStyle(Font.BOLDITALIC);
		cb.beginText();
		ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(
				route.getName() + " (" + route.getDrops().size() + ")", font), 240, offset, 0);
		cb.endText();

		offset -= 40;
		
		for (Drop drop : drops) {
			offset -= addDrop(drop, cb, offset);
		}
	}

	private String truncate(String s, int max) {
		if (s.length() > max) {
			return s.substring(0, max) + "...";
		}
		return s;
	}
	private int addDrop(Drop drop, PdfContentByte cb, float offset) throws DocumentException {

		LineSeparator lineSeparator = new LineSeparator();
		
		lineSeparator.drawLine(cb, 5, PageSize.A4.getWidth() - 5, offset);
		
		Font font = new Font();
		font.setSize(12);
		
		cb.beginText();
		ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(
				String.valueOf(truncate(drop.getLine1(), 35)), font), 10, offset - 15, 0);
		cb.endText();
		
		cb.beginText();
		ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(
				String.valueOf(truncate(drop.getLine2(), 35)), font), 10, offset - 30 , 0);
		cb.endText();

		Barcode39 code39ext = new Barcode39();
		code39ext.setCode(drop.getScan());
		code39ext.setStartStopText(false);
		code39ext.setTextAlignment(Element.ALIGN_BOTTOM);
		code39ext.setBarHeight(10);
		code39ext.setExtended(true);
		Image createImageWithBarcode = code39ext.createImageWithBarcode(cb, null, null);
		cb.addImage(createImageWithBarcode, 250, 0 ,0 , 25 ,10, offset - 65); 
		
		Font sfont = new Font();
		sfont.setSize(16);
		lineSeparator.setLineWidth(0.1f);
		
		cb.beginText();
		ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, new Phrase(
				"Namnteckning:", sfont), 380, offset - 25 , 0);
		cb.endText();
		lineSeparator.drawLine(cb, 400, PageSize.A4.getWidth() - 10, offset - 25);

		sfont.setSize(12);

		cb.beginText();
		ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, new Phrase(
				"Förtydligande:", sfont), 380, offset - 45 , 0);
		cb.endText();
		lineSeparator.drawLine(cb, 400, PageSize.A4.getWidth() - 10, offset - 45);

		cb.beginText();
		ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, new Phrase(
				"Tid:", sfont), 380, offset - 65 , 0);
		cb.endText();
		lineSeparator.drawLine(cb, 400, PageSize.A4.getWidth() - 100, offset - 65);

		return 70;
	}
}
