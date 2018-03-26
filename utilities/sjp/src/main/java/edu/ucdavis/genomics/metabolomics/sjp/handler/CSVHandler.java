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
package edu.ucdavis.genomics.metabolomics.sjp.handler;

import edu.ucdavis.genomics.metabolomics.sjp.ParserHandler;

import java.util.Properties;


/**
 * erstellt eine csv tabelle ohne header!
 *
 * @author gert wohlgemuth
 */
public class CSVHandler implements ParserHandler {
    StringBuffer buffer = new StringBuffer();

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.parser.ParserHandler#setProperties(Properties)
     */
    public void setProperties(Properties p) {
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.sjp.ParserHandler#endAttribute(String, String)
     */
    public void endAttribute(String element, String name) {
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.parser.ParserHandler#endDataSet()
     */
    public void endDataSet() {
        buffer.append("\n");
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.parser.ParserHandler#endDocument()
     */
    public void endDocument() {
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.sjp.ParserHandler#endElement(String)
     */
    public void endElement(String name) {
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.sjp.ParserHandler#startAttribute(String, String, String)
     */
    public void startAttribute(String element, String name, String value) {
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.parser.ParserHandler#startDataSet()
     */
    public void startDataSet() {
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.parser.ParserHandler#startDocument()
     */
    public void startDocument() {
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.parser.ParserHandler#startElement(String,
     *      String)
     */
    public void startElement(String name, String value) {
        buffer.append(value.replaceAll("\t", " ").trim() + "\t");
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        return buffer.toString();
    }
}
