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
package edu.ucdavis.genomics.metabolomics.sjp.parser.nist.webbook;

import edu.ucdavis.genomics.metabolomics.sjp.handler.FileDownloadHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;


/**
 * @author Gert Wohlgemuth
 * <p>
 * l?dt JCAMP files herunter indem er die ermmitelten urls parst
 * und speichert sie im angeebenen verzeichnes. Dieses gilt allerdings nur f?r die nistms
 * webbook seiten!
 */
public class JCAMPDownloader extends FileDownloadHandler {
    /**
     * @see edu.ucdavis.genomics.metabolomics.binbase.parser.handler.FileDownloader#handleUrl(URL)
     */
    protected void handleUrl(URL url) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(
            url.openStream()));

        String s;
        boolean moreResults = false;

        while ((s = in.readLine()) != null) {
            if (s.indexOf("<h1>Search Results</h1>") > -1) {
                System.err.println("found severals result");
                moreResults = true;
            } else if (s.indexOf("Download <a href=") > -1) {
                if (s.indexOf("in JCAMP-DX format.") > -1) {
                    System.err.println("download masspec");
                    super.handleUrl(new URL((url.getProtocol() + "://" +
                        url.getHost() +
                        s.substring(s.indexOf("\"") + 1, s.lastIndexOf("\""))).replaceAll(
                        "amp;", "")));
                }

                moreResults = false;
            }

            if (moreResults == true) {
                if (s.indexOf("<li><a href=\"/cgi/cbook.cgi?ID") > -1) {
                    System.err.println("\tdownloading new url " +
                        new URL((url.getProtocol() + "://" + url.getHost() +
                            s.substring(s.indexOf("\"") + 1, s.lastIndexOf("\""))).replaceAll(
                            "amp;", "")));
                    this.handleUrl(new URL((url.getProtocol() + "://" +
                        url.getHost() +
                        s.substring(s.indexOf("\"") + 1, s.lastIndexOf("\""))).replaceAll(
                        "amp;", "")));
                }
            }
        }
    }
}
