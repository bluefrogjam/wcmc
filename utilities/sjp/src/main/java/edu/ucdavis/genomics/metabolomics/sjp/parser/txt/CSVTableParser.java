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
package edu.ucdavis.genomics.metabolomics.sjp.parser.txt;

import edu.ucdavis.genomics.metabolomics.sjp.Parser;
import edu.ucdavis.genomics.metabolomics.sjp.exception.ParserException;

import java.util.Properties;


/**
 * Ein einfacher Parser welcher CSV Tabellen parst. <b>dataSet = row <b>
 * attribute = cell <b><b>es muss der seperator angegeben werder. dieses
 * geschieht mir der Kostante SEPERATOR in den parserproperties. default m?ssig
 * wird ein tab verwendet
 *
 * @author gert wohlgemuth
 */
public class CSVTableParser extends Parser {
    /**
     * DOCUMENT ME!
     */
    private static final String SEPERATOR = "SEPERATOR";

    /**
     * DOCUMENT ME!
     */
    private String seperator = "\t";

    /**
     * DOCUMENT ME!
     */
    private String[] headers = null;

    /**
     * DOCUMENT ME!
     */
    private boolean header = false;

    /**
     * @see edu.ucdavis.genomics.metabolomics.sjp.Parser#setProperties(Properties)
     */
    public void setProperties(Properties properties) {
        if (properties.getProperty(SEPERATOR) != null) {
            this.seperator = properties.getProperty(SEPERATOR);
        }
    }

    /**
     * @throws ParserException
     * @see edu.ucdavis.genomics.metabolomics.sjp.Parser#finish()
     */
    protected void finish() throws ParserException {
        super.finish();
        header = false;
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.sjp.Parser#parseLine(String)
     */
    protected void parseLine(String line) throws ParserException {
        String[] l = line.split(seperator);

        if (header == false) {
            header = true;
            headers = l;
        } else {
            for (int i = 0; i < l.length; i++) {
                this.getHandler().startElement(headers[i], l[i]);
                this.getHandler().endElement(headers[i]);
            }
        }
    }
}
