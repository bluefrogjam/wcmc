/*
 * Created on Aug 18, 2003
 *
 */
package edu.ucdavis.genomics.metabolomics.util.xls;

import edu.ucdavis.genomics.metabolomics.util.transform.crosstable.object.*;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author wohlgemuth
 * @version Aug 18, 2003 <br>
 * BinBaseDatabase
 * @description erstellt eine formatierte excel tabelle, mit farben und
 * nullwertmarkierung
 */
public class ColoredSplitToSheets implements Splitter {

    private transient Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * count of columns per sheet
     */
    private int maxColumnCount;

    /**
     * style for the class
     */
    private CellStyle classStyle;

    /**
     * DOCUMENT ME!
     */
    private CellStyle errorStyle;

    /**
     * style for header entrys
     */
    private CellStyle headerStyle;

    /**
     * DOCUMENT ME!
     */
    private CellStyle metaStyle;

    /**
     * style for the samples
     */
    private CellStyle sampleStyle;

    /**
     * style for zero numbers
     */
    private CellStyle zeroStyle;

    /**
     * the workbook
     */
    private Workbook workbook;

    /**
     * sheets
     */
    private Sheet[] sheets = null;

    /**
     * DOCUMENT ME!
     */
    private boolean firstrun = true;

    /**
     * DOCUMENT ME!
     */
    private boolean header;

    /**
     * DOCUMENT ME!
     */
    private boolean last;

    /**
     * DOCUMENT ME!
     */
    private int size = 0;

    /**
     * the row counter
     */
    private short rowCounter;

    private CellStyle probStyle;

    private CellStyle nullStyle;

    private CellStyle binStyle;

    private CellStyle refrenceStyle;

    private CellStyle contentStyle;

    private CellStyle combinedStyle;

    public ColoredSplitToSheets() {
        this(256);
    }

    /**
     * Creates a new ColoredSplitToSheets object.
     */
    public ColoredSplitToSheets(int columnCount) {

        this.workbook = new XSSFWorkbook();

        this.maxColumnCount = columnCount;
        // sample style
        {
            sampleStyle = workbook.createCellStyle();

            Font font = workbook.createFont();
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            font.setItalic(true);
            sampleStyle.setFont(font);

            sampleStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            sampleStyle.setFillForegroundColor(HSSFColor.YELLOW.index);
            sampleStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        }
        // header style
        {
            headerStyle = workbook.createCellStyle();

            Font font = workbook.createFont();
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            headerStyle.setFont(font);
            headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            headerStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            headerStyle.setFillForegroundColor(HSSFColor.YELLOW.index);
        }

        // bin style
        {
            binStyle = workbook.createCellStyle();
            binStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            binStyle.setFillForegroundColor(HSSFColor.YELLOW.index);
            binStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        }

        // refrence style
        {
            refrenceStyle = workbook.createCellStyle();
            refrenceStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            refrenceStyle.setFillForegroundColor(HSSFColor.YELLOW.index);
            refrenceStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        }

        // content style
        {
            contentStyle = workbook.createCellStyle();
            contentStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            contentStyle.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
            contentStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        }

        // combined style
        {
            combinedStyle = workbook.createCellStyle();
            combinedStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            combinedStyle.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
            combinedStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        }
        // zero style
        {
            zeroStyle = workbook.createCellStyle();
            zeroStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            zeroStyle.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
            zeroStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        }
        // null style
        {
            nullStyle = workbook.createCellStyle();
            nullStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            nullStyle.setFillForegroundColor(HSSFColor.ORANGE.index);
            nullStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        }

        // problematic style
        {
            probStyle = workbook.createCellStyle();
            probStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            probStyle.setFillForegroundColor(HSSFColor.LIGHT_ORANGE.index);
            probStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        }

        // error style
        {
            errorStyle = workbook.createCellStyle();
            errorStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            errorStyle.setFillForegroundColor(HSSFColor.DARK_RED.index);
            errorStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        }
        // class style
        {
            classStyle = workbook.createCellStyle();
            classStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            classStyle.setFillForegroundColor(HSSFColor.YELLOW.index);
            classStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        }
        // meta style
        {
            metaStyle = workbook.createCellStyle();
            metaStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            metaStyle.setFillForegroundColor(HSSFColor.YELLOW.index);
            metaStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        }
    }

    /**
     * @version Aug 21, 2003
     * @author wohlgemuth <br>
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.xls.Splitter#getBook()
     */
    public Workbook getBook() {
        return this.workbook;
    }

    /**
     * @uml.property name="header"
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.xls.Splitter#setHeader(boolean)
     */
    public void setHeader(boolean value) {
        header = value;
    }

    /**
     * @uml.property name="header"
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.xls.Splitter#isHeader()
     */
    public boolean isHeader() {
        return header;
    }

    /**
     * f?gt eine neue linie in das workbook ein
     *
     * @param line
     * @version Aug 18, 2003
     * @author wohlgemuth <br>
     */
    public void addLine(List<FormatObject<?>> line) {
        if (line == null) {
            return;
        }

        boolean lastLine = line.isEmpty();

        /*
         * berechnen der ben?tigten ausmasse f?r das excel sheet
         */
        if (firstrun == true) {
            size = line.size();

            double count = (double) size / (double) (maxColumnCount - 1);
            count = Math.ceil(count) + 1;
            sheets = new Sheet[(int) count];

            for (int i = 0; i < sheets.length; i++) {
                sheets[i] = workbook.createSheet();
            }
            logger.info("count of sheets needed: " + (sheets.length + 1));
        }

        /*
         * f?llen und formatieren des sheets nach dem gew?nschten vorgaben
         */
        Iterator<FormatObject<?>> it = line.iterator();

        for (short i = 0; i < sheets.length; i++) {
            Row row = sheets[i].createRow(rowCounter);
            short x = 0;

            boolean next = it.hasNext();

            while (next == true) {
                if (x < maxColumnCount) {
                    try {
                        Object nextObject = it.next();
                        FormatObject<?> o = null;

                        if (nextObject instanceof FormatObject) {
                            o = (FormatObject<?>) nextObject;
                        } else {
                            if (nextObject == null) {
                                nextObject = "";
                            }
                            o = new MetaObject<Object>(nextObject);
                        }

                        // it.remove();

                        // wenn diese zeile nichtbesonders formatiert werden
                        // soll
                        // header
                        if (last == true) {
                            firstrun = last;
                        }

                        formatObject(row, x, o);

                        x++;
                    } catch (NoSuchElementException e) {
                        next = false;
                    }
                } else {
                    next = false;
                }
            }
        }

        rowCounter++;
        firstrun = false;
        this.last = lastLine;
    }

    /**
     * formats the actual object
     *
     * @param row
     * @param x
     * @param o
     */
    private void formatObject(Row row, short x, FormatObject o) {
        if (o instanceof SampleObject) {
            Cell cell = row.createCell(x);
            cell.setCellStyle(sampleStyle);
            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
            cell.setCellValue(o.getValue().toString());
        } else if (o instanceof BinObject) {
            Cell cell = row.createCell(x);
            cell.setCellStyle(binStyle);
            cell.setCellType(HSSFCell.CELL_TYPE_STRING);

            cell.setCellValue(o.getValue().toString());

        } else if (o instanceof ClassObject) {
            Cell cell = row.createCell(x);
            cell.setCellStyle(classStyle);
            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
            cell.setCellValue(o.getValue().toString());
        } else if (o instanceof NullObject) {
            Cell cell = row.createCell(x);
            cell.setCellStyle(nullStyle);
            cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            try {
                cell.setCellValue(Double.parseDouble(o.getValue().toString()));
            } catch (NumberFormatException e) {
                cell.setCellValue((o.getValue().toString()));
            }

        } else if (o instanceof ProblematicObject) {
            Cell cell = row.createCell(x);
            cell.setCellStyle(probStyle);
            cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            try {
                cell.setCellValue(Double.parseDouble(o.getValue().toString()));
            } catch (NumberFormatException e) {
                cell.setCellValue((o.getValue().toString()));
            }

        } else if (o instanceof CombinedObject) {
            Cell cell = row.createCell(x);
            cell.setCellStyle(combinedStyle);
            cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            try {
                cell.setCellValue(Double.parseDouble(o.getValue().toString()));
            } catch (NumberFormatException e) {
                cell.setCellValue((o.getValue().toString()));
            }

        } else if (o instanceof ZeroObject) {
            Cell cell = row.createCell(x);
            cell.setCellStyle(zeroStyle);
            cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            try {
                cell.setCellValue(Double.parseDouble(o.getValue().toString()));
            } catch (NumberFormatException e) {
                cell.setCellValue((o.getValue().toString()));
            }

        } else if (o instanceof HeaderFormat) {
            Cell cell = row.createCell(x);
            cell.setCellStyle(headerStyle);
            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
            cell.setCellValue(o.getValue().toString());
        } else if (o instanceof RefrenceObject) {
            Cell cell = row.createCell(x);
            cell.setCellStyle(refrenceStyle);
            cell.setCellType(HSSFCell.CELL_TYPE_STRING);

            RefrenceObject<?> ref = (RefrenceObject<?>) o;

            if (ref.getHyperlink() != null) {
                if (ref.getHyperlink().length() > 0) {
                    cell.setCellFormula("HYPERLINK(\"" + ref.getHyperlink()
                        + "\";\"" + ref.getValue() + "\")");
                } else {
                    cell.setCellValue(o.getValue().toString());
                }
            } else {
                cell.setCellValue(o.getValue().toString());
            }
        } else if (o instanceof MetaObject) {
            Cell cell = row.createCell(x);
            cell.setCellStyle(metaStyle);
            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
            cell.setCellValue(o.getValue().toString());
        } else if (o instanceof ContentObject) {
            Cell cell = row.createCell(x);
            cell.setCellStyle(contentStyle);
            cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            try {
                cell.setCellValue(Double.parseDouble(o.getValue().toString()));
            } catch (NumberFormatException e) {
                cell.setCellValue((o.getValue().toString()));
            }
        } else if (o instanceof ErrorObject) {
            Cell cell = row.createCell(x);
            cell.setCellStyle(errorStyle);
            cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            cell.setCellValue(o.getValue().toString());
        } else if (o == null) {
            Cell cell = row.createCell(x);
            cell.setCellStyle(nullStyle);
            cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            cell.setCellValue(0);
        } else {
            Cell cell = row.createCell(x);
            cell.setCellStyle(sampleStyle);
            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
            cell.setCellValue(o.getValue().toString());
        }
    }

    @Override
    protected void finalize() throws Throwable {
        sheets = null;
        super.finalize();
    }

    /**
     * Speichert die Datei als excel workbook und beendet die m?glichkeit des
     * anf?gen von Linie
     *
     * @param file
     * @throws FileNotFoundException
     * @throws IOException
     * @version Aug 18, 2003
     * @author wohlgemuth <br>
     */
    public void saveBook(File file) throws FileNotFoundException, IOException {
        this.saveBook(new FileOutputStream(file));
    }

    /**
     * @version Aug 30, 2003
     * @author wohlgemuth <br>
     * @see edu.ucdavis.genomics.metabolomics.binbase.utils.xls.Splitter#saveBook(OutputStream)
     */
    public void saveBook(OutputStream stream) throws IOException {
        workbook.write(stream);
    }
}
