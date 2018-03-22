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
package edu.ucdavis.genomics.metabolomics.sjp;

import edu.ucdavis.genomics.metabolomics.sjp.exception.ParserException;

import java.util.Properties;


/**
 * definiert die events welche von einem Parser ausgel?st werden k?nnen. Es kann aber auch sein das ein PArser
 * nicht alle Events kennt. Zum Beispiel kennen viele Parser das Event startAttribute/endAttribute
 * nicht.
 * <b>
 * <b>
 * Dieses Interface muss dann von der Klasse implementiert werden welche als Handler dienen sollte
 * @author gert wohlgemuth
 */
public interface ParserHandler {
    /**
     * setzt ben?tigte properties
     * @param p
     * @throws ParserException
     */
    void setProperties(Properties p) throws ParserException;

    /**
     * das parsen eines elementes wurde abgeschlossen
     * @param element
     * @param name
     * @throws ParserException
     */
    void endAttribute(String element, String name) throws ParserException;

    /**
     * beendet ein datenset
     * @throws ParserException
     */
    void endDataSet() throws ParserException;

    /**
     * das parsen wurde abgeschlossen
     * @throws ParserException
     */
    void endDocument() throws ParserException;

    /**
     * das parsen eines elementes wurde abgeschlossen
     * @param name
     * @throws ParserException
     */
    void endElement(String name) throws ParserException;

    /**
     * das parsen eines attributes wurde abgeschlossen
     * @param element
     * @param name
     * @param value
     * @throws ParserException
     */
    void startAttribute(String element, String name, String value)
        throws ParserException;

    /**
     * startet ein neues datenset
     * @throws ParserException
     */
    void startDataSet() throws ParserException;

    /**
     * es wird angefangen das dokument zu parsen
     * @throws ParserException
     */
    void startDocument() throws ParserException;

    /**
     * ein element wurde gefunden
     * @param name
     * @param value
     * @throws ParserException
     */
    void startElement(String name, String value) throws ParserException;
}
