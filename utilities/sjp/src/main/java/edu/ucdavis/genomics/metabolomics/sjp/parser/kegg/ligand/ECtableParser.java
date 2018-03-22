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
 * @author Gert Wohlgemuth parst die ec-table datei (ECtable) parst die
 *         ectable, welche von kegg auf ftp.genome.ad.jp/pub/kegg/ligand
 *         bereitgestellt wird die id des enzymes wird als name verwendet die
 *         beschreibung als value. Es kann zu 1:n beziehunkommen.
 */
public class ECtableParser extends Parser {
    /**
     * DOCUMENT ME!
     */
    private boolean start = false;

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.parser.Parser#parse(edu.ucdavis.genomics.metabolomics.binbase.parser.ParserHandler)
     */
    public void parse(ParserHandler handler) throws ParserException {
        try {
            this.parse(new URL("ftp://ftp.genome.ad.jp/pub/kegg/ligand/ECtable"),
                handler);
        } catch (MalformedURLException e) {
            throw new ParserException(e);
        } catch (IOException e) {
            throw new ParserException(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param line DOCUMENT ME!
     *
     * @throws ParserException DOCUMENT ME!
     */
    protected void parseLine(String line) throws ParserException {
        if (line.matches("[1-9]*")) {
            this.getHandler().startDataSet();
            start = true;
        } else if (line.length() == 0) {
            start = false;
            this.getHandler().endDataSet();
        }

        if (start == true) {
            if ((line.indexOf("Obsolete  Transferred to") == -1) &&
                    (line.indexOf("Obsolete  Deleted entry") == -1)) {
                if (line.length() != 0) {
                    line = line.trim();

                    String[] l = line.split("  ");

                    if (l.length == 2) {
                        if (l[1].indexOf(";") > -1) {
                            String[] s = l[1].split(";");

                            for (int i = 0; i < s.length; i++) {
                                this.getHandler().startElement(l[0], s[i].trim());
                                this.getHandler().endElement(l[0]);
                            }
                        } else {
                            this.getHandler().startElement(l[0], l[1].trim());
                            this.getHandler().endElement(l[0]);
                        }
                    } else if (l.length == 1) {
                        String[] s = l[0].split(" ");

                        if (s[1].indexOf(";") > -1) {
                            String[] x = l[1].split(";");

                            for (int i = 0; i < x.length; i++) {
                                this.getHandler().startElement(l[0], x[i].trim());
                                this.getHandler().endElement(l[0]);
                            }
                        } else {
                            this.getHandler().startElement(s[0], s[1].trim());
                            this.getHandler().endElement(s[0]);
                        }
                    } else if (l.length == 3) {
                        this.getHandler().startElement(l[0], l[1] +
                            l[2].trim());
                        this.getHandler().endElement(l[0]);
                    }
                }
            }
        }
    }
}
