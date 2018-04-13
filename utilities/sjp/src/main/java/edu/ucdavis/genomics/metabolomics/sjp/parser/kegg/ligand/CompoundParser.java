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
 * genrisch ?ber leerzeichen am anfang elemente erkennen und dann spezielle
 * vererben
 *
 * @author Gert Wohlgemuth
 * <p>
 * diese klasse parst die kegg compound files
 * (ftp.genome.ad.jp/pub/kegg/ligand). F?r jedes gefundende attribute wird das
 * handler ElmentFound event ausgel?st. <b>momentant werden die folgenden
 * elemente unterst?zt ENTRY = 1:1 NAME = 1:n FORMULA = 1:1 REACTION = 1:n
 * ENZYME = 1:n DBLINKS = 1:1
 */
public class CompoundParser extends Parser implements CompoundKeys {
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
     *
     * @param args DOCUMENT ME!
     * @throws Exception DOCUMENT ME!
     */
    public static void main(String[] args) throws Exception {
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.parser.Parser#parse(edu.ucdavis.genomics.metabolomics.binbase.parser.ParserHandler)
     */
    public void parse(ParserHandler handler) throws ParserException {
        try {
            this.parse(new URL(
                "ftp://ftp.genome.ad.jp/pub/kegg/ligand/compound"), handler);
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
            name = ENTRY;
            line = (line.replaceAll(name, "").trim() + " ");
            this.getHandler().startDataSet();
        } else if (line.indexOf(NAME) > -1) {
            name = NAME;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.indexOf(FORMULA) > -1) {
            name = FORMULA;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.indexOf(MASS) > -1) {
            name = MASS;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.indexOf(REACTION) > -1) {
            name = REACTION;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.indexOf(PATHWAY) > -1) {
            name = PATHWAY;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.indexOf(ENZYME) > -1) {
            name = ENZYME;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.indexOf(DBLINKS) > -1) {
            name = DBLINKS;
            line = (line.replaceAll(name, "").trim() + " ");
        } else if (line.indexOf(END_OF_DATASET) > -1) {
            this.getHandler().endDataSet();
            name = END_OF_DATASET;
        } else {
            if (line.startsWith(" ") == false) {
                name = "-1";
            }
        }

        if (name.equals(ENTRY)) {
            this.getHandler().startElement(name,
                line.replaceAll("Compound", "").trim());
            this.getHandler().endElement(name);
        } else if (name.equals(NAME)) {
            this.getHandler().startElement(name, line.trim());
            this.getHandler().endElement(name);
        } else if (name.equals(FORMULA)) {
            this.getHandler().startElement(name, line.trim());
            this.getHandler().endElement(name);
        } else if (name.equals(MASS)) {
            this.getHandler().startElement(name, line.trim());
            this.getHandler().endElement(name);
        } else if (name.equals(REACTION)) {
            String[] l = line.split(" ");

            for (int i = 0; i < l.length; i++) {
                if (l[i].length() > 0) {
                    this.getHandler().startElement(name, l[i].trim());
                    this.getHandler().endElement(name);
                }
            }
        } else if (name.equals(PATHWAY)) {
            this.getHandler().startElement(name,
                line.replaceAll("PATH:", "").trim());
            this.getHandler().endElement(name);
        } else if (name.equals(ENZYME)) {
            String[] l = line.split(" ");

            for (int i = 0; i < l.length; i++) {
                if (l[i].length() > 0) {
                    this.getHandler().startElement(name, l[i].trim());
                    this.getHandler().endElement(name);
                }
            }
        } else if (name.equals(DBLINKS)) {
            if (line.indexOf(CAS) > -1) {
                line = line.replaceAll(CAS + ":", "").trim();
                this.getHandler().startElement(CAS.replaceAll(":", ""), line);
                this.getHandler().endElement(name);
            } else {
                this.getHandler().startElement(name, line);
                this.getHandler().endElement(name);
            }
        } else {
        }
    }
}
