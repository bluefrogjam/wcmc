/*
 * Created: 14.02.2004 Project: SJP-API
 *
 * Copyright (C) 2003 wohlgemuth
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
package edu.ucdavis.genomics.metabolomics.sjp.parser.jcamp;

import edu.ucdavis.genomics.metabolomics.sjp.Parser;
import edu.ucdavis.genomics.metabolomics.sjp.exception.ParserException;


/**
 * parst das jcamp daten file. Die entstehenden daten m?ssen allerdings noch
 * weiter verarbeitet werden, da das format nicht den typ der enthaltenen daten
 * definiert.
 *
 * @author gert wohlgemuth
 */
public class JCAMPParser extends Parser {
    /**
     * das ende eines datensets
     */
    private static final String KEY_END_OF_DATA_SET = "##END=";

    /**
     * identifier f?r ein attribut
     */
    private static final String KEY_ATTRIBUTE = "##";

    /**
     * identifier f?r ein attribut
     */
    private static final String KEY_ATTRIBUTE_SEPERATOR = "=";

    /**
     * DOCUMENT ME!
     */
    private String attributeName = "";

    /**
     * DOCUMENT ME!
     */
    private StringBuffer attribute = new StringBuffer();

    /**
     * DOCUMENT ME!
     */
    private boolean dataSetStart = false;

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.parser.Parser#parse(java.io.Reader,
     * edu.ucdavis.genomics.metabolomics.binbase.parser.ParserHandler)
     */
    public void parseLine(String line) throws ParserException {
        if (line.indexOf(KEY_ATTRIBUTE) > -1) {
            if (dataSetStart == false) {
                dataSetStart = true;
                this.getHandler().startDataSet();
            } else {
                if (attributeName.length() > 0) {
                    this.getHandler().startElement(attributeName.toLowerCase(),
                        attribute.toString());
                    this.getHandler().endElement(attributeName.toLowerCase());

                    attribute = new StringBuffer();
                }
            }
        }

        if (line.startsWith("##")) {
            if (line.indexOf(KEY_ATTRIBUTE_SEPERATOR) > -1) {
                String[] attr = line.split(KEY_ATTRIBUTE_SEPERATOR);

                if (attr.length == 2) {
                    attribute.append(attr[1].trim());
                    attributeName = attr[0].replaceAll(KEY_ATTRIBUTE, "");
                }
            } else {
                attribute.append(line);
            }
        } else {
            if ((line.matches("[0-9].*")) &&
                (line.indexOf(KEY_END_OF_DATA_SET) == -1)) {
                this.getHandler().startElement("data point", line.trim());
                this.getHandler().endElement("data point");
            }
        }

        if (line.equals(KEY_END_OF_DATA_SET)) {
            this.getHandler().endDataSet();
            dataSetStart = false;
        }
    }
}
