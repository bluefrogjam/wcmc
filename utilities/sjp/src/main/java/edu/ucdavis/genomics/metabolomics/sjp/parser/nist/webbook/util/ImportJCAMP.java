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
import edu.ucdavis.genomics.metabolomics.sjp.ParserHandler;
import edu.ucdavis.genomics.metabolomics.sjp.parser.jcamp.JCAMPParser;

import java.io.File;


/**
 * parst die daten eines verzeichnis mittels eines jcamparsers
 *
 * @author Gert Wohlgemuth
 */
public class ImportJCAMP {
    /**
     * parst die datei oder die im verzeichnis enthaltenen dateien.
     * es muss arg[0] angegeben sein (datei/dir). des weiteren muss der handler als
     * classname angegeben sein
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        ParserHandler handler = (ParserHandler) Class.forName(args[1])
            .newInstance();
        Parser parser = Parser.create(JCAMPParser.class);
        parser.parse(new File(args[0]), handler);
    }
}
