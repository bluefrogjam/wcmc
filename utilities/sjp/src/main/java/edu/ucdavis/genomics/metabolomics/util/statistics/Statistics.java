/*
 * Created on 28.07.2004
 */
package edu.ucdavis.genomics.metabolomics.util.statistics;

import edu.ucdavis.genomics.metabolomics.util.statistics.deskriptiv.DeskriptiveMethod;
import edu.ucdavis.genomics.metabolomics.util.statistics.replacement.ZeroReplaceable;

import java.io.File;
import java.util.Collection;
import java.util.Vector;


/**
 * @author wohlgemuth erstellt aus dem datenset automatisch statistiken,
 */
public interface Statistics {
    /**
     * contains all instances of descriptive methods
     */
    Collection<DeskriptiveMethod> DESCRIPTIVE_METHODS = new Vector<DeskriptiveMethod>();

    /**
     * setzt die methode durch die null werte einer klasse ersetzt werden sollen
     */
    public void setZeroReplacementMethod(ZeroReplaceable method);

    /**
     * setzt die statistische method welcher zur auswertung benutzt werden soll
     *
     * @param o
     */
    public void addMethod(Object o);

    /**
     * f?hrt auf den datenset die statistiken durch
     *
     * @param data    das vom transform handler convertierte datenset
     * @param classes die verf?gbaren klassen
     * @return gibt ein datenset zur?ck was nur noch geschrieben werden muss
     */
    public Collection doStatistics(Collection data, Collection classes);

    /**
     * sollen graphiken erstellt werden und wenn ja wo sollen sie gespeichert
     * werden
     *
     * @param value
     * @param dir
     */
    public void generateGraphics(boolean value, File dir);
}
