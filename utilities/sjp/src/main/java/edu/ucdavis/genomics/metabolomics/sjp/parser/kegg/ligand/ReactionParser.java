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
package edu.ucdavis.genomics.metabolomics.sjp.parser.kegg.ligand;

import edu.ucdavis.genomics.metabolomics.sjp.Parser;
import edu.ucdavis.genomics.metabolomics.sjp.ParserHandler;
import edu.ucdavis.genomics.metabolomics.sjp.exception.ParserException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * @author Gert Wohlgemuth
 * <p>
 * diese klasse parst die kegg reaction files
 * (ftp.genome.ad.jp/pub/kegg/ligand). F?r jedes gefundende attribute wird das
 * handler ElementFound event ausgel?st.
 */
public class ReactionParser extends Parser {
    /**
     * DOCUMENT ME!
     */
    public static final String ENTRY = "ENTRY";

    /**
     * DOCUMENT ME!
     */
    public static final String NAME = "NAME";

    /**
     * DOCUMENT ME!
     */
    public static final String DEFINITION = "DEFINITION";

    /**
     * DOCUMENT ME!
     */
    public static final String EQUATION = "EQUATION";

    /**
     * DOCUMENT ME!
     */
    public static final String PATHWAY = "PATHWAY";

    /**
     * DOCUMENT ME!
     */
    public static final String ENZYME = "ENZYME";

    /**
     * DOCUMENT ME!
     */
    public static final String COMMENT = "COMMENT";

    /**
     * DOCUMENT ME!
     */
    private static final String END_OF_DATASET = "///";

    /**
     * DOCUMENT ME!
     */
    private String name;

    /**
     * DOCUMENT ME!
     */
    private StringBuffer buffer = new StringBuffer();

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.parser.Parser#parse(edu.ucdavis.genomics.metabolomics.binbase.parser.ParserHandler)
     */
    public void parse(ParserHandler handler) throws ParserException {
        try {
            this.parse(new URL(
                "ftp://ftp.genome.ad.jp/pub/kegg/ligand/reaction"), handler);
        } catch (MalformedURLException e) {
            throw new ParserException(e);
        } catch (IOException e) {
            throw new ParserException(e);
        }
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.parser.Parser#parse(java.io.BufferedReader)
     */
    protected void parseLine(String line) throws ParserException {
        if (line.indexOf(ENTRY) > -1) {
            clearBuffer();
            name = ENTRY;
            line = (line.replaceAll(name, "").trim() + " ");
            this.getHandler().startDataSet();
        } else if (line.indexOf(NAME) > -1) {
            clearBuffer();
            name = NAME;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.indexOf(PATHWAY) > -1) {
            clearBuffer();
            name = PATHWAY;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.indexOf(ENZYME) > -1) {
            clearBuffer();
            name = ENZYME;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.indexOf(DEFINITION) > -1) {
            clearBuffer();
            name = DEFINITION;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.indexOf(EQUATION) > -1) {
            clearBuffer();
            name = EQUATION;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.indexOf(COMMENT) > -1) {
            clearBuffer();
            name = COMMENT;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.indexOf(END_OF_DATASET) > -1) {
            clearBuffer();
            this.getHandler().endDataSet();
            name = END_OF_DATASET;
            System.exit(-1);
        }

        if (name.equals(ENTRY)) {
            this.getHandler().startElement(name, line.trim());
            this.getHandler().endElement(name);
        }

        if (name.equals(COMMENT)) {
            buffer.append(line.trim() + " ");
        } else if (name.equals(NAME)) {
            buffer.append(line.trim().replaceAll("\\$", "") + " ");
        } else if (name.equals(PATHWAY)) {
            this.getHandler().startElement(name,
                line.replaceAll("PATH:", "").trim());
            this.getHandler().endElement(name);
        } else if (name.equals(ENZYME)) {
            this.getHandler().startElement(name, line.trim());
            this.getHandler().endElement(name);
        } else if (name.equals(DEFINITION)) {
            buffer.append(line.trim() + " ");
        } else if (name.equals(EQUATION)) {
            buffer.append(line.trim() + " ");
        }
    }

    /**
     *
     */
    private void clearBuffer() throws ParserException {
        if (buffer.length() > 0) {
            this.getHandler().startElement(name, buffer.toString().trim());
            this.getHandler().endElement(name);
            this.buffer = new StringBuffer();
        }
    }
}
