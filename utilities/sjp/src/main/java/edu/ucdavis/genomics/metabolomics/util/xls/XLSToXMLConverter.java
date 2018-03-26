/*
 * Created on Sep 1, 2005
 */
package edu.ucdavis.genomics.metabolomics.util.xls;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;


/**
 * converts a xls stream in a xml document
 *
 * @author wohlgemuth
 *
 */
public class XLSToXMLConverter {
    /**
     * the standard headers of an excel file
     */
    static final String[] HEADER = new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

    /**
     *
     */
    public XLSToXMLConverter() {
        super();
    }

    /**
     * converts the inputfile to an xml file
     *
     * @throws Exception
     *
     * @see edu.ucdavis.genomics.metabolomics.binbase.meta.converter.AbstractConverter#convert(InputStream,
     *      java.io.OutputStream)
     */
    public Document convert(InputStream in) throws Exception {
        HSSFWorkbook workbook = new HSSFWorkbook(in);

        Document document = new Document();
        Element root = new Element("workbook");
        document.setRootElement(root);

        for (int a = 0; a < (workbook.getNumberOfSheets()); a++) {
            HSSFSheet sheet = workbook.getSheetAt(a);

            int min = sheet.getFirstRowNum();
            int max = sheet.getLastRowNum();

            String[] header = HEADER;
            Element sheetElement = new Element("sheet");
            sheetElement.setAttribute("name", workbook.getSheetName(a));
            sheetElement.setAttribute("index", String.valueOf(a));

            for (int i = min; i <= max; i++) {
                HSSFRow row = sheet.getRow(i);
                createElement(header, sheetElement, row);
            }

            root.addContent(sheetElement);
        }

        workbook = null;
        in.close();

        return document;
    }

    /**
     * @param header
     * @param sheetElement
     * @param row
     */
    private void createElement(String[] header, Element sheetElement,
        HSSFRow row) {
        if (row != null) {
            short firstCell = row.getFirstCellNum();
            short lastCell = row.getLastCellNum();

            Element element = new Element("row");
            element.setAttribute("id", String.valueOf(row.getRowNum()));

            for (short x = firstCell; x <= lastCell; x++) {
                HSSFCell cell = row.getCell(x);

                if (cell == null) {
                } else {
                    Element content = new Element(header[x].replaceAll(" ", "_"));

                    switch (cell.getCellType()) {
                        case HSSFCell.CELL_TYPE_BLANK:
                            break;

                        case HSSFCell.CELL_TYPE_BOOLEAN:
                            content.setText(String.valueOf(
                                    cell.getBooleanCellValue()).trim());
                            element.addContent(content);

                            break;

                        case HSSFCell.CELL_TYPE_ERROR:
                            content.setText(String.valueOf(
                                    cell.getErrorCellValue()).trim());
                            element.addContent(content);

                            break;

                        case HSSFCell.CELL_TYPE_FORMULA:
                            content.setText(String.valueOf(
                                    cell.getCellFormula()).trim());
                            element.addContent(content);

                            break;

                        case HSSFCell.CELL_TYPE_NUMERIC:
                            content.setText(String.valueOf(
                                    cell.getNumericCellValue()).trim());
                            element.addContent(content);

                            break;

                        case HSSFCell.CELL_TYPE_STRING:
                            content.setText(String.valueOf(
                                    cell.getStringCellValue()).trim());
                            element.addContent(content);

                            break;

                        default:}
                }
            }

            sheetElement.addContent(element);
        }
    }
    
    public static void main(String[] args) throws Exception{
		Document d =new XLSToXMLConverter().convert(new FileInputStream(args[0]));
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		outputter.output(d,new FileOutputStream(args[0].replace(".xls",".xml")));
	}
}
