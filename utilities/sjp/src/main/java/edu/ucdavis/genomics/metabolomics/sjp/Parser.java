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
package edu.ucdavis.genomics.metabolomics.sjp;

import edu.ucdavis.genomics.metabolomics.sjp.exception.ParserException;
import edu.ucdavis.genomics.metabolomics.sjp.handler.ConsoleHandler;

import java.io.*;
import java.net.URL;
import java.util.Map;


/**
 * @author Gert Wohlgemuth
 * <p>
 * diese klasse stellt die grundlage aller parser da. Dabei gibt es diverse
 * inputmethoden wir urls oder files... <b>um mit der klasse zu arbeiten muss
 * eine konkreteklasse gebildet werden, welche die daten dann parst. Die
 * Ausgabe der Ergebnisse kann dann mittels eines handlers erfolgen. <b>Des
 * weiteren wird ein Parser immer mit der create(...) Methode erstellt und es
 * gibt keinen ?ffentlichen Konstruktor
 */
public abstract class Parser {

    /**
     * der handler f?r die datenausgabe
     *
     * @uml.property name="handler"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private ParserHandler handler;

    /**
     * properties falls der parser welche ben?tigt
     */
    private Map<?, ?> properties;

    /**
     * der default konstruktor
     */
    protected Parser() {
    }

    /**
     * gibt den verwendeten handler zur?ck
     *
     * @return
     * @uml.property name="handler"
     */
    public ParserHandler getHandler() {
        return this.handler;
    }

    /**
     * erstellt einen neuen parser
     *
     * @param parserclass
     * @return @throws
     * InstantiationException
     * @throws IllegalAccessException
     */
    public static Parser create(Class<?> parserclass)
        throws InstantiationException, IllegalAccessException {
        return (Parser) parserclass.newInstance();
    }

    /**
     * @param handler The handler to set.
     * @uml.property name="handler"
     */
    public void setHandler(ParserHandler handler) {
        this.handler = handler;
    }

    /**
     * @param properties The properties to set.
     * @uml.property name="properties"
     */
    public void setProperties(Map<?, ?> properties) {
        this.properties = properties;
    }

    /**
     * @return Returns the properties.
     * @uml.property name="properties"
     */
    public Map<?, ?> getProperties() {
        return properties;
    }

    /**
     * erstellt einen neuen parser
     *
     * @param parserclass
     * @return @throws
     * InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    public Parser create(String parserclass)
        throws InstantiationException, IllegalAccessException,
        ClassNotFoundException {
        return (Parser) Class.forName(parserclass).newInstance();
    }

    /**
     * der parser weiss selber wo er sein daten findet und ben?tigt keine
     * weiteren eingabe parameter es muss also nur der handler angegeben
     * werden. es ist zu beachten das nicht jeder parser das unterst?zt, daher
     * kann eine exception geworfen werden und dem parser m?ssen die daten
     * manuel mitgeteilt werden
     *
     * @throws ParserException
     */
    public void parse(ParserHandler handler) throws ParserException {
        throw new ParserException("sorry this is for this parser not available");
    }

    /**
     * der parser weiss selber wo er sein daten findet und ben?tigt keine
     * weiteren eingabe parameter desweiteren stellt die implementierung ihren
     * eigenen parser zur verf?gung. es ist zu beachten das nicht jeder parser
     * das unterst?zt, daher kann eine exception geworfen werden und dem parser
     * m?ssen die daten manuel mitgeteilt werden. Intern wird der
     * ConsoleHandler als default klasse benutzt
     *
     * @throws ParserException
     */
    public void parse() throws ParserException {
        this.parse(new ConsoleHandler());
    }

    /**
     * parst ein dokument und ?bergibt die ergebnisse an den handler
     *
     * @param reader
     * @param handler
     */
    public void parse(Reader reader, ParserHandler handler)
        throws ParserException {
        this.handler = handler;
        handler.startDocument();

        try {
            String line;
            BufferedReader buffer = new BufferedReader(reader);

            while ((line = buffer.readLine()) != null) {
                parseLine(line);
            }
        } catch (Exception e) {
            throw new ParserException(e);
        }

        this.finish();
        handler.endDocument();
    }

    /**
     * parse einen inputstream
     *
     * @param stream
     * @param handler
     * @throws ParseException
     */
    public void parse(InputStream stream, ParserHandler handler)
        throws ParserException {
        parse(new InputStreamReader(stream), handler);
    }

    /**
     * parse eine url
     *
     * @param url     die url die geparst werden soll
     * @param handler
     * @throws ParseException
     * @throws IOException
     */
    public void parse(URL url, ParserHandler handler)
        throws ParserException, IOException {
        parse(new InputStreamReader(url.openStream()), handler);
    }

    /**
     * parse eine datei
     *
     * @param file
     * @param handler
     * @throws FileNotFoundException
     * @throws ParseException
     * @throws IOException
     */
    public void parse(File file, ParserHandler handler)
        throws FileNotFoundException, ParserException, IOException {
        if (file.isFile()) {
            parse(new FileReader(file), handler);
        } else if (file.exists() == false) {
            throw new FileNotFoundException("file not exists! " + file);
        } else {
            File[] f = file.listFiles();

            for (int i = 0; i < f.length; i++) {
                parse(f[i], handler);
            }
        }

        this.finish();
    }

    /**
     * parse eine datei
     *
     * @param file
     * @param handler
     * @throws FileNotFoundException
     * @throws ParseException
     * @throws IOException
     */
    public void parse(String file, ParserHandler handler)
        throws FileNotFoundException, ParserException, IOException {
        parse(new FileReader(file), handler);
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        return handler.toString();
    }

    /**
     * parst die aktuelle line als solches
     *
     * @param line
     * @throws ParserException
     */
    protected abstract void parseLine(String line) throws ParserException;

    /**
     * wird bei beendigung des parsers aufgerufen
     *
     * @throws ParserException
     */
    protected void finish() throws ParserException {
    }
}
