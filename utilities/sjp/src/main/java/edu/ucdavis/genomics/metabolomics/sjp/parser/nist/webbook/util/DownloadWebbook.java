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
package edu.ucdavis.genomics.metabolomics.sjp.parser.nist.webbook.util;

import edu.ucdavis.genomics.metabolomics.sjp.Parser;
import edu.ucdavis.genomics.metabolomics.sjp.handler.FileDownloadHandler;
import edu.ucdavis.genomics.metabolomics.sjp.parser.nist.webbook.JCAMPDownloader;
import edu.ucdavis.genomics.metabolomics.sjp.parser.nist.webbook.WebbookSpeciesFileParser;

import java.io.File;
import java.util.Properties;


/**
 * l?dt das nist webbook in form von jcamp dateien herunter, welche ein massenspektren enthalten
 *
 * @author Gert Wohlgemuth
 */
public class DownloadWebbook {
    /**
     * das programm l?dt die species datei herunter, parst sie und l?dt die substanzen welche ein massenspektrum aufweisen herunter. diese werde im tempor?ren verzeichnis des system gespeichert
     * <p>
     * <b>die dateien enden dann auf *.jdx
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        JCAMPDownloader handler = new JCAMPDownloader();
        Properties p = new Properties();
        p.setProperty(FileDownloadHandler.DIR,
            File.createTempFile("temp", "temp").getParent() + "/nist");
        p.setProperty(FileDownloadHandler.URL,
            "http://webbook.nist.gov/cgi/cbook.cgi?Name=(ID)&Units=SI&cMS=on");
        p.setProperty(FileDownloadHandler.ATTRIBUTE, "compound");
        p.setProperty(FileDownloadHandler.EXTENSION, "jdx");
        handler.setProperties(p);

        Parser parser = Parser.create(WebbookSpeciesFileParser.class);
        parser.parse(handler);
    }
}
