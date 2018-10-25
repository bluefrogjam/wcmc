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
package edu.ucdavis.genomics.metabolomics.sjp.parser.ini;

import edu.ucdavis.genomics.metabolomics.sjp.Parser;
import edu.ucdavis.genomics.metabolomics.sjp.ParserHandler;
import edu.ucdavis.genomics.metabolomics.sjp.exception.ParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;


/**
 * parst die bekannten ini dateien, commentare werden immer ignoriert
 *
 * @author gert wohlgemuth
 */
public class INIParser extends Parser {
    /**
     * DOCUMENT ME!
     */
    private static String COMMENT = "(^;.*)|(^/*)";

    /**
     * DOCUMENT ME!
     */
    private static String NAME = "^\\[.*\\]";

    /**
     * DOCUMENT ME!
     */
    private static String SEPERATOR = ".*=.*";

    /**
     * DOCUMENT ME!
     */
    private String lastLine = null;

    /**
     * DOCUMENT ME!
     */
    private boolean startdataset = false;

    /**
     * parst ein dokument und ?bergibt die ergebnisse an den handler
     *
     * @param reader
     * @param handler
     */
    public void parse(Reader reader, ParserHandler handler)
        throws ParserException {
        this.setHandler(handler);
        handler.startDocument();

        try {
            String line;
            BufferedReader buffer = new BufferedReader(reader);

            while ((line = buffer.readLine()) != null) {
                parseLine(line);
            }

            this.getHandler().endDataSet();
        } catch (IOException e) {
            throw new ParserException(e);
        }

        handler.endDocument();
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.sjp.Parser#parseLine(String)
     */
    protected void parseLine(String line) throws ParserException {
        if (line.matches(COMMENT)) {
        } else if (line.matches(NAME)) {
            if (lastLine == null) {
            } else if (startdataset == false) {
            } else {
                this.getHandler().endDataSet();
            }

            startdataset = true;
            this.getHandler().startDataSet();
            this.getHandler().startElement("title",
                line.trim().replaceAll("\\[", "").replaceAll("\\]", ""));
            this.getHandler().endElement("title");
        } else if (line.matches(SEPERATOR)) {
            String[] s = line.trim().split("=");
            this.getHandler().startElement(s[0].trim(), s[1].trim());
            this.getHandler().endElement("title");
        }

        lastLine = line;
    }
}
