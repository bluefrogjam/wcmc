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
package edu.ucdavis.genomics.metabolomics.sjp.parser.nist.webbook;

import edu.ucdavis.genomics.metabolomics.sjp.Parser;
import edu.ucdavis.genomics.metabolomics.sjp.ParserHandler;
import edu.ucdavis.genomics.metabolomics.sjp.exception.ParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;


/**
 * @author - gert wohlgemuth <br>parst die species datei welche auf <b>
 * http://webbook.nist.gov/chemistry/download/ <b>verf?gbar ist.
 * <p>
 * aus dieser datei erh?lt man die namen aller in der nist library vorhandenen
 * substanzen
 */
public class WebbookSpeciesFileParser extends Parser {
    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.parser.Parser#parse(edu.ucdavis.genomics.metabolomics.binbase.parser.ParserHandler)
     */
    public void parse(ParserHandler handler) throws ParserException {
        try {
            InputStream stream = new GZIPInputStream(new URL(
                "http://webbook.nist.gov/chemistry/download/species.txt.gz").openStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            byte[] buffer = new byte[256];

            int length;

            while ((length = stream.read(buffer, 0, 256)) != -1) {
                out.write(buffer, 0, length);
            }

            this.parse(new ByteArrayInputStream(out.toByteArray()), handler);
        } catch (MalformedURLException e) {
            throw new ParserException(e);
        } catch (IOException e) {
            throw new ParserException(e);
        }
    }

    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.parser.Parser#parse(java.io.Reader)
     */
    protected void parseLine(String line) throws ParserException {
        this.getHandler().startDataSet();
        this.getHandler().startElement("compound", line.split("\t")[0]);
        this.getHandler().endElement("compound");
        this.getHandler().endDataSet();
    }
}
