package cc.crosstown.dhl.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import cc.crosstown.common.Parser;
import cc.crosstown.dhl.model.Row;
import cc.crosstown.dhl.model.Row.Builder;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.itextpdf.awt.geom.Rectangle2D;
import com.itextpdf.awt.geom.Rectangle2D.Float;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.ContentByteUtils;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfContentStreamProcessor;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

public class PdfParser implements Parser<Document> {
	public enum Mode {
		HEADER,
		TABLE,
		FOOTER
	}
	
	@Override
	public Document parse(InputStream inputStream) throws IOException {
		PdfReader reader = new PdfReader(inputStream);
		
		final Listener listener = new Listener();
		
		PdfContentStreamProcessor processor = new PdfContentStreamProcessor(listener);
		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			listener.setPage(i);
			PdfDictionary dictionary = reader.getPageN(i).getAsDict(PdfName.RESOURCES);
			processor.processContent(ContentByteUtils.getContentBytesForPage(reader, i), dictionary);
		}
		
		List<Row> rows = Lists.transform(listener.builders, new Function<Builder, Row>() {
			@Override
			public Row apply(Builder builder) {
				builder.withDocumentName(listener.name);
				return builder.build();
			}
		});
		
		return new Document(listener.name, rows);
	}
	
	private static class Listener implements RenderListener {
		private String name;
		private List<Builder> builders = new ArrayList<>();
		protected int page;
		
		private AtomicReference<Rectangle2D.Float> receiver = new AtomicReference<>();
		private AtomicReference<Rectangle2D.Float> id = new AtomicReference<>();
		private AtomicReference<Rectangle2D.Float> puDate = new AtomicReference<>();		
		private AtomicReference<Rectangle2D.Float> charge = new AtomicReference<>();
		private Mode mode = Mode.HEADER;
		
		private Builder builder;
		
		@Override
		public void renderText(TextRenderInfo renderInfo) {
			switch (mode) {
			case HEADER:
				column("Receiver", receiver, renderInfo)
					.column("Waybill / Pcs", id, renderInfo)
					.column("PU Date", puDate, renderInfo)
					.column("Charge", charge, renderInfo);
				
				if (charge.get() != null && isBelow(renderInfo, charge)) {
					mode = Mode.TABLE;
				} else {
					return;					
				}
				//$FALL-THROUGH$
			case TABLE:
				if (renderInfo.getText().startsWith("Shipments:")) {
					builders.add(builder);
					mode = Mode.FOOTER;
					return;
				}
				if (isRow(renderInfo)) {
					if (builder != null) {
						builders.add(builder);
					}
					builder = new Builder();
					builder.withTop(renderInfo.getAscentLine().getBoundingRectange().y);
					builder.withPage(page);
					builder.withRow(Integer.parseInt(renderInfo.getText()));
					return;
				}
				if (isReceiver(renderInfo)) {
					builder.addTo(renderInfo.getText());
					return;
				}
				if (isWaybill(renderInfo)) {
					builder.withWaybill(renderInfo.getText());
					return;
				}
				if (isPiece(renderInfo)) {
					builder.withPiece(renderInfo.getText());
					return;
				}
				if (sameColumn(renderInfo, puDate)) {
					builder.withBottom(renderInfo.getDescentLine().getBoundingRectange().y);
					return;
				}
				if (isCharge(renderInfo)) {
					builder.withCharge(true);
					return;
				}
				
				return;
			case FOOTER:
				if (isRouteName(renderInfo)) {
					name = renderInfo.getText();
				}
				
				return;
				
			default:
				// ignore
				return;
			}
		}
		
		private boolean isRouteName(TextRenderInfo renderInfo) {
			return page == 1 
					&& Pattern.matches("\\d{2}/\\d{2}/\\d{4} .*", renderInfo.getText());
		}

		public void setPage(int page) {
			this.page = page;
			mode = Mode.HEADER;
			builder = null;
		}

		private boolean isBelow(TextRenderInfo renderInfo,
				AtomicReference<Float> column) {
			return renderInfo.getAscentLine().getBoundingRectange().y < column.get().y;
		}

		private boolean sameColumn(TextRenderInfo renderInfo, AtomicReference<Float> column) {
			Float b1 = renderInfo.getBaseline().getBoundingRectange();
			Float b2 = column.get();
			
			if (b1.x <= b2.x) {
				return b1.x + b1.width >= b2.x;
			}
			
			return b2.x + b2.width >= b1.x;
		}

		private boolean isCharge(TextRenderInfo renderInfo) {
			return sameColumn(renderInfo, charge);
		}

		private boolean isPiece(TextRenderInfo renderInfo) {
			return sameColumn(renderInfo, id)
					&& Pattern.matches("\\(.{1,2}\\).*", renderInfo.getText());
		}

		private boolean isWaybill(TextRenderInfo renderInfo) {
			return sameColumn(renderInfo, id)
					&& Pattern.matches("\\d{10}", renderInfo.getText());
		}

		private boolean isRow(TextRenderInfo renderInfo) {
			return Pattern.matches("\\d+", renderInfo.getText())
					&& isLeftOfColumn(renderInfo, receiver);
		}
		
		private boolean isReceiver(TextRenderInfo renderInfo) {
			return sameColumn(renderInfo, receiver);
		}

		private boolean isLeftOfColumn(TextRenderInfo renderInfo,
				AtomicReference<Float> column) {
			Float bounds = renderInfo.getBaseline().getBoundingRectange();
			return bounds.x < column.get().x;
		}

		private Listener column(String name, AtomicReference<Float> column,
				TextRenderInfo renderInfo) {
			if (column.get() != null) {
				return this;
			}
			
			if (name.equals(renderInfo.getText())) {
				column.set(renderInfo.getDescentLine().getBoundingRectange());
			}
			
			return this;
		}

		@Override
		public final void beginTextBlock() {
			// ignore
		}
		
		@Override
		public final void endTextBlock() {
			// ignore
		}
		
		@Override
		public final void renderImage(ImageRenderInfo renderInfo) {
			// ignore
		}
	}	
}
