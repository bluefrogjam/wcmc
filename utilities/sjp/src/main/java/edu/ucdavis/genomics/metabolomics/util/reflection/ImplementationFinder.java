/*
 * Created on 08.08.2004
 */
package edu.ucdavis.genomics.metabolomics.util.reflection;

import java.io.File;

import java.net.URL;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 * ermittelt die class hirachie
 * @author wohlgemuth
 */
public class ImplementationFinder {
    /**
     * gibt alle klassen aus dem verzeichnis zur?ck
     * @param pkg
     * @return
     */
    public static List getClassesOfPackage(String pkg) {
        return find(pkg, "java.lang.Object");
    }

    /**
     * erstellt eine liste mit allen klassen und interfaces wovon diese klasse
     * erbt
     *
     * @param name
     * @param cache
     * @return
     */
    public static List getSuper(Class name, List cache) {
        Class ms = name.getSuperclass();

        Object[] o = name.getInterfaces();

        for (int i = 0; i < o.length; i++) {
            if (cache.contains(o[i]) == false) {
                cache.add(o[i]);

                List temp = getSuper((Class) o[i], cache);

                Iterator it = temp.iterator();

                while (it.hasNext()) {
                    Class c = (Class) it.next();

                    if (cache.contains(c) == false) {
                        cache.add(c);
                    }
                }
            }
        }

        if (ms != null) {
            List temp = getSuper(ms, cache);
            Iterator it = temp.iterator();

            while (it.hasNext()) {
                Class c = (Class) it.next();

                if (cache.contains(c) == false) {
                    cache.add(c);
                }
            }

            if (cache.contains(ms) == false) {
                cache.add(ms);
            }
        }

        return cache;
    }

    /**
     * sucht in den angegebenen packages und sub packages nach implementierungen oder ableitungen der ?bergebenen klasses
     * @param pckgname
     * @param className
     * @return
     */
    public static List find(String pckgname, Class className) {
        return find(pckgname, className.getName(), new Vector());
    }

    /**
     * sucht in den angegebenen packages und sub packages nach implementierungen oder ableitungen der ?bergebenen klasses
     * @param pckgname
     * @param className
     * @return
     */
    public static List find(String pckgname, String className) {
        return find(pckgname, className, new Vector());
    }

    /**
     * ermittelt ob der erste parameter eine instance des zweiten ist
     * @param classToTest
     * @param classOfInteresst
     * @return
     */
    public static boolean instanceOf(Class classToTest, Class classOfInteresst) {
        List content = getSuper(classToTest, new Vector());
        Iterator it = content.iterator();

        if(classToTest.getName().equals(classOfInteresst.getName())){
        	return true;
        }
        while (it.hasNext()) {
            Class c = (Class) it.next();

            if (c.getName().equals(classOfInteresst.getName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        System.err.println(ImplementationFinder.instanceOf(Double.class,String.class));
    }

    /**
     * DOCUMENT ME!
     *
     * @param pckgname DOCUMENT ME!
     * @param className DOCUMENT ME!
     * @param cache DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static List find(String pckgname, String className, List cache) {
        String name = new String(pckgname);

        if (!name.startsWith("/")) {
            name = "/" + name;
        }

        name = name.replace('.', '/');

        // Get a File object for the package
        URL url = Object.class.getResource(name);

        File directory = new File(url.getFile());

        return find(directory, pckgname, className, cache);
    }

    /**
     * DOCUMENT ME!
     *
     * @param directory DOCUMENT ME!
     * @param pckgname DOCUMENT ME!
     * @param interfaceName DOCUMENT ME!
     * @param cache DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static List find(File directory, String pckgname,
        String interfaceName, List cache) {
        pckgname = pckgname.replaceAll("/", ".");

        if (directory.exists()) {
            File[] files = directory.listFiles();

            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    if (files[i].getName().endsWith(".class")) {
                        try {
                            String classname = files[i].getName().substring(0,
                                    files[i].getName().length() - 6);

                            Class o = Class.forName(pckgname + "." + classname);
                            List content = getSuper(o, new Vector());
                            Iterator it = content.iterator();

                            while (it.hasNext()) {
                                Class c = (Class) it.next();

                                if (c.getName().equals(interfaceName)) {
                                    cache.add(o);

                                    break;
                                }
                            }
                        } catch (Throwable cnfex) {
                            cnfex.printStackTrace();
                        }
                    }
                } else {
                    find(pckgname + "." + files[i].getName(), interfaceName,
                        cache);
                }
            }
        } else {
            throw new RuntimeException("directory does not exists " +
                directory);
        }

        return cache;
    }
}
