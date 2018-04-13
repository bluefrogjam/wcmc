/*
 * Created: 14.02.2004
 * Project: SJP-API
 *
 * Copyright (C) 2003 wohlgemuth
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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
 * diese klasse parst die kegg enzyme files (ftp.genome.ad.jp/pub/kegg/ligand). F?r jedes gefundende attribute wird das handler ElmentFound event ausgel?st.
 */
public class EnzymeParser extends Parser {
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
    public static final String REACTION = "REACTION";

    /**
     * DOCUMENT ME!
     */
    public static final String PATHWAY = "PATHWAY";

    /**
     * DOCUMENT ME!
     */
    public static final String DBLINKS = "DBLINKS";

    /**
     * DOCUMENT ME!
     */
    public static final String CLASS = "CLASS";

    /**
     * DOCUMENT ME!
     */
    public static final String SYSNAME = "SYSNAME";

    /**
     * DOCUMENT ME!
     */
    public static final String SUBSTRATE = "SUBSTRATE";

    /**
     * DOCUMENT ME!
     */
    public static final String PRODUCT = "PRODUCT";

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
    public static final String REFERENCE = "REFERENCE";

    /**
     * DOCUMENT ME!
     */
    public static final String ORTHOLOG = "ORTHOLOG";

    /**
     * DOCUMENT ME!
     */
    public static final String GENES = "GENES";

    /**
     * DOCUMENT ME!
     */
    public static final String DISEASE = "DISEASE";

    /**
     * DOCUMENT ME!
     */
    public static final String MOTIF = "MOTIF";

    /**
     * DOCUMENT ME!
     */
    public static final String STRUCTURES = "STRUCTURES";

    /**
     * DOCUMENT ME!
     */
    private String lastline = "";

    /**
     * DOCUMENT ME!
     */
    private String name = "";

    /**
     * DOCUMENT ME!
     */
    private StringBuffer buffer = new StringBuffer();

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.parser.Parser#parse(edu.ucdavis.genomics.metabolomics.binbase.parser.ParserHandler)
     */
    public void parse(ParserHandler handler) throws ParserException {
        try {
            this.parse(new URL("ftp://ftp.genome.ad.jp/pub/kegg/ligand/enzyme"),
                handler);
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
        if (line.startsWith(ENTRY)) {
            clearBuffer();
            name = ENTRY;
            line = (line.replaceAll(name, "").trim() + " ");
            this.getHandler().startDataSet();
        } else if (line.startsWith(NAME)) {
            clearBuffer();
            name = NAME;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.startsWith(REACTION)) {
            clearBuffer();
            name = REACTION;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.startsWith(PATHWAY)) {
            clearBuffer();
            name = PATHWAY;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.startsWith(DBLINKS)) {
            clearBuffer();
            name = DBLINKS;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.startsWith(CLASS)) {
            clearBuffer();
            name = CLASS;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.startsWith(SYSNAME)) {
            clearBuffer();
            name = SYSNAME;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.startsWith(SUBSTRATE)) {
            clearBuffer();
            name = SUBSTRATE;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.startsWith(PRODUCT)) {
            clearBuffer();
            name = PRODUCT;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.startsWith(COMMENT)) {
            clearBuffer();
            name = COMMENT;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.startsWith(REFERENCE)) {
            clearBuffer();
            name = REFERENCE;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.startsWith(ORTHOLOG)) {
            clearBuffer();
            name = ORTHOLOG;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.startsWith(GENES)) {
            clearBuffer();
            name = GENES;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.startsWith(DISEASE)) {
            clearBuffer();
            name = DISEASE;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.startsWith(MOTIF)) {
            clearBuffer();
            name = MOTIF;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.startsWith(STRUCTURES)) {
            clearBuffer();
            name = STRUCTURES;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.startsWith(END_OF_DATASET)) {
            clearBuffer();
            this.getHandler().endDataSet();
            name = END_OF_DATASET;
        }

        if (name.equals(ENTRY)) {
            this.getHandler().startElement(name, line.trim());
            this.getHandler().endElement(name);
        } else if (name.equals(GENES)) {
            if (line.trim().matches("^[A-Z][A-Z][A-Z]: .*")) {
                if (lastline.length() == 0) {
                    buffer.append(line.trim() + " ");
                } else if (lastline.matches("^[A-Z][A-Z][A-Z]: .*")) {
                    clearBuffer();
                    buffer.append(line.trim() + " ");
                }

                lastline = line.trim();
            } else {
                buffer.append(line.trim() + " ");
            }
        } else if (name.equals(CLASS)) {
            buffer.append(line.trim() + " ");
        } else if (name.equals(COMMENT)) {
            buffer.append(line.trim() + " ");
        } else if (name.equals(REFERENCE)) {
            if (line.trim().matches("^[1-9].*")) {
                clearBuffer();
            } else {
                buffer.append(line.trim() + " ");
            }
        } else if (name.equals(NAME)) {
            this.getHandler().startElement(name, line.trim());
            this.getHandler().endElement(name);
        } else if (name.equals(SYSNAME)) {
            this.getHandler().startElement(name, line.trim());
            this.getHandler().endElement(name);
        } else if (name.equals(REACTION)) {
            this.getHandler().startElement(name, line.trim());
            this.getHandler().endElement(name);
        } else if (name.equals(SUBSTRATE)) {
            this.getHandler().startElement(name, line.trim());
            this.getHandler().endElement(name);
        } else if (name.equals(PRODUCT)) {
            this.getHandler().startElement(name, line.trim());
            this.getHandler().endElement(name);
        } else if (name.equals(ORTHOLOG)) {
            this.getHandler().startElement(name, line.trim());
            this.getHandler().endElement(name);
        } else if (name.equals(DISEASE)) {
            this.getHandler().startElement(name, line.trim());
            this.getHandler().endElement(name);
        } else if (name.equals(PATHWAY)) {
            this.getHandler().startElement(name,
                line.replaceAll("PATH:", "").trim());
            this.getHandler().endElement(name);
        } else if (name.equals(STRUCTURES)) {
            if (line.trim().matches("^[A-Z][A-Z][A-Z]: .*")) {
                if (lastline.length() == 0) {
                    buffer.append(line.trim() + " ");
                } else if (lastline.matches("^[A-Z][A-Z][A-Z]: .*")) {
                    clearBuffer();
                    buffer.append(line.trim() + " ");
                }

                lastline = line.trim();
            } else {
                buffer.append(line.trim() + " ");
            }
        } else if (name.equals(MOTIF)) {
            if (line.trim().matches("^[A-Z][A-Z]: .*")) {
                if (lastline.length() == 0) {
                    buffer.append(line.trim() + " ");
                } else if (lastline.matches("^[A-Z][A-Z]: .*")) {
                    clearBuffer();
                    buffer.append(line.trim() + " ");
                }

                lastline = line.trim();
            } else {
                buffer.append(line.trim() + " ");
            }
        } else if (name.equals(DBLINKS)) {
            this.getHandler().startElement(name, line.trim());
            this.getHandler().endElement(name);
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
