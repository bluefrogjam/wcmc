/*
 * Created: 14.02.2004 Project: SJP-API
 *
 * Copyright (C) 2003 wohlgemuth
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
package edu.ucdavis.genomics.metabolomics.sjp.parser.msp;

import edu.ucdavis.genomics.metabolomics.sjp.Parser;
import edu.ucdavis.genomics.metabolomics.sjp.exception.ParserException;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.2 $
 */
public class MSPParser extends Parser implements SpectraParser {

    /**
     * daran erkenne man ein massspec
     */
    public static final String KEY_MASS_SPEC = "Num Peaks";

    /**
     * daran erkenne man einen ri marker im name tag
     */
    public static final String KEY_RI_TAG = "RI";

    /**
     * das ende eines datensets
     */
    public static final String KEY_END_OF_DATA_SET = "";

    /**
     * identifier f?r ein attribut
     */
    public static final String KEY_ATTRIBUTE = ":";
    StringBuffer massSpec = new StringBuffer();
    boolean dataSetStart = false;
    boolean specStart = false;

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.parser.Parser#parse(java.io.Reader,
     * edu.ucdavis.genomics.metabolomics.binbase.parser.ParserHandler)
     */
    public void parseLine(String line) throws ParserException {
        if (line.indexOf(KEY_ATTRIBUTE) > -1) {
            if (dataSetStart == false) {
                dataSetStart = true;
                this.getHandler().startDataSet();
            }

            if (line.startsWith(KEY_MASS_SPEC)) {
                specStart = true;
                massSpec = new StringBuffer();
            }

            String[] attr = line.split(":");

            if (attr[1].trim().indexOf("##") > -1) {
                attr[1] = attr[1].trim();
                attr[1] = attr[1].substring(attr[1].lastIndexOf("##") + 2,
                    attr[1].length());

                String[] at = attr[1].split("=");

                String name = attr[0].trim().toLowerCase();
                String value = at[1];

                this.getHandler().startElement(name, value);
                this.getHandler().endElement(name);
            } else if (attr[1].trim().indexOf(KEY_RI_TAG) > -1) {
                String[] value = attr[1].trim().split(KEY_RI_TAG);
                if (value.length == 2) {
                    this.getHandler().startElement(
                        attr[0].trim().toLowerCase(), value[0]);
                    this.getHandler().endElement(attr[0].trim().toLowerCase());

                    try {
                        this.getHandler().startElement("RI", value[1].trim());
                        this.getHandler().endElement("RI");
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                String name = attr[0].trim().toLowerCase();
                String value = attr[1].trim().toLowerCase();

                this.getHandler().startElement(name, value);
                this.getHandler().endElement(name);
            }
        }

        if (specStart == true) {
            if (line.startsWith(KEY_MASS_SPEC) == false) {
                massSpec.append(line);
            }
        }

        if (line.equals(KEY_END_OF_DATA_SET)) {
            parseMassSpec();
        }
        // line
        if (line.matches(".*\\(.*\\).*\\(.*\\).*")) {
            if (specStart == false) {
                massSpec.append(line);
                specStart = true;
            }
        }
        // line
        if (line.matches(".*\\;.*")) {
            if (specStart == false) {
                massSpec.append(line);
                specStart = true;
            }
        }

    }

    private void parseMassSpec() throws ParserException {
        String spec = massSpec.toString();

        massSpec = new StringBuffer();

        String[] array = null;

        // seperated by ;
        if (spec.indexOf(';') > 0) {
            array = spec.split(";");
        }
        // wrapped by ()
        else if (spec.indexOf(')') > 0) {
            spec = spec.replace('(', ' ');
            spec = spec.replace(')', ';');
            array = spec.split(";");
        }

        for (int i = 0; i < array.length; i++) {
            String[] secondary = array[i].split(" ");

            if (secondary.length > 0) {
                for (int x = 0; x < (secondary.length - 1); x++) {
                    if (secondary[x].length() > 0) {
                        massSpec.append(secondary[x].trim());

                        massSpec.append(":");
                    }
                }

                massSpec.append((int) Double
                    .parseDouble((secondary[secondary.length - 1])));
                massSpec.append(" ");
            }
        }

        this.getHandler().startElement(SPECTRA, massSpec.toString().trim());
        this.getHandler().endElement(SPECTRA);
        this.getHandler().endDataSet();
        dataSetStart = false;
        specStart = false;
    }

    @Override
    protected void finish() throws ParserException {
        if (specStart) {
            parseMassSpec();
        }
        super.finish();
    }

}
