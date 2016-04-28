package cc.crosstown.ups.xlsx;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import cc.crosstown.common.Parser;
import cc.crosstown.common.Settings;
import cc.crosstown.ups.model.UPSRow;
import cc.crosstown.ups.model.UPSRow.Builder;

public class UPSParser implements Parser<UPSDocument> {
	enum Column {
		WAYBILL,
		NAME,
		ADDRESS;
		
		static Column of(Settings settings, int columnIndex) {
			if (columnIndex == settings.getWaybillColumn() - 1) {
				return WAYBILL;
			}
			if (columnIndex == settings.getNameColumn() - 1) {
				return NAME;
			}
			if (columnIndex == settings.getAddressColumn() - 1) {
				return ADDRESS;
			}
			return null;
		}
		
		static void with(Builder builder, Settings settings, Cell cell) {
			Column column = of(settings, cell.getColumnIndex());
			if (column == null) {
				return;
			}
			String cellValue = cellValue(cell);
			switch(column) {	
				case WAYBILL: builder.withWaybill(cellValue); break;
				case NAME: builder.withName(cellValue); break;
				case ADDRESS: builder.withAddress(cellValue); break;
				default: break;
			}
		}
		
		static String cellValue(Cell cell) {
			switch(cell.getCellType()) {
				case Cell.CELL_TYPE_BLANK: return "";
				case Cell.CELL_TYPE_BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
				case Cell.CELL_TYPE_ERROR: return "<" + String.valueOf(cell.getErrorCellValue()) + ">";
				case Cell.CELL_TYPE_FORMULA: return cell.getCellFormula();
				case Cell.CELL_TYPE_NUMERIC: return String.valueOf(cell.getNumericCellValue());
				case Cell.CELL_TYPE_STRING: return cell.getStringCellValue();
				default: return "";
			}
		}
	}

	private Settings settings;

	public UPSParser(Settings settings) {
		this.settings = settings;
	}

	@Override
	public UPSDocument parse(InputStream input) throws IOException {
		List<UPSRow> rows = new ArrayList<UPSRow>();
				
		XSSFWorkbook workbook = new XSSFWorkbook(input);
		XSSFSheet sheet = workbook.getSheetAt(0);
		Iterator<Row> rowIterator = sheet.iterator();
		int rowCount = 0;
        while (rowIterator.hasNext()) {
        	Builder builder = new UPSRow.Builder();
            //For each row, iterate through all the columns
            Row row = rowIterator.next();
            if (rowCount++ < settings.getStartRow() - 1) {
            	continue;
            }
            
			Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                Column.with(builder, settings, cell);
            }
            
            UPSRow built = builder.build();
            if (built != null) {
            	rows.add(built);
            }
        }

		return new UPSDocument(getDocName(rows), rows);
	}
	
	private String getDocName(List<UPSRow> rows) {
		if (rows.size() == 0) {
			return "EMTPY?!";
		}
		
		return rows.get(0).getWaybill() + " -- " + rows.get(rows.size() - 1).getWaybill();
	}
}
