package edu.ucdavis.fiehnlab.utilities.minix;

import edu.ucdavis.fiehnlab.utilities.minix.types.BinBaseResult;
import edu.ucdavis.fiehnlab.utilities.minix.types.SampleInformationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

import org.jdom2.Element;
import org.jdom2.input.DOMBuilder;

/**
 * simple implementation of a minix study reader
 */
public class SXStudyFileReader {

    private Logger logger = LoggerFactory.getLogger(getClass().getName());

    public List<SampleInformationResult> loadData(InputStream inputStream) {

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputStream);

            DOMBuilder builder = new DOMBuilder();
            Element root = builder.build(doc).getRootElement();

            List<SampleInformationResult> results = new Vector<SampleInformationResult>();
            for (Element clazz : root.getChild("classes").getChildren("class")) {

                for (Element sample : clazz.getChild("samples").getChildren()) {

                    for (String fileName : sample.getAttributeValue("fileName").split(",")) {

                        SampleInformationResult result = new SampleInformationResult();
                        Collection<BinBaseResult> annotations = new ArrayList<BinBaseResult>();

                        result.setSample(fileName);
                        result.setLabel(sample.getAttributeValue("label"));
                        result.setComment(sample.getAttributeValue("comment"));
                        result.setClassName(clazz.getAttributeValue("id"));
                        result.setExperimentId(Integer.parseInt(root.getAttributeValue("id")));
                        result.setDescription(root.getAttributeValue("title"));
                        result.setOrgan(clazz.getAttributeValue("organ"));
                        result.setSpecies(clazz.getAttributeValue("species"));


                        if (sample.getChild("annotations") != null) {
                            if (sample.getChild("annotations").getChild("annotation") != null) {
                                for (Element annotationElement : sample.getChild("annotations").getChildren("annotation")) {
                                    BinBaseResult annotation = new BinBaseResult();

                                    String similarity = annotationElement.getAttributeValue("similarity");
                                    String purity = annotationElement.getAttributeValue("purity");
                                    String retentionTime = annotationElement.getAttributeValue("retentionTime");
                                    String retentionIndex = annotationElement.getAttributeValue("retentionIndex");
                                    String uniqueMass = annotationElement.getAttributeValue("uniqueMass");
                                    String correctionFailed = annotationElement.getAttributeValue("correctionFailed");
                                    String lecoVersion = annotationElement.getAttributeValue("lecoVersion");
                                    String binID = annotationElement.getAttributeValue("binid");
                                    String bin = annotationElement.getAttributeValue("bin");
                                    String group = annotationElement.getAttributeValue("group");
                                    String binRetentionIndex = annotationElement.getAttributeValue("binRetentionIndex");
                                    String binUniqueMass = annotationElement.getAttributeValue("binUniqueMass");
                                    String quantMass = annotationElement.getAttributeValue("quantmass");
                                    String spectra = annotationElement.getAttributeValue("spectra");

                                    if (similarity != null && !similarity.isEmpty()) {
                                        annotation.setSimilarity(Double.parseDouble(similarity));
                                    }
                                    if (purity != null && !purity.isEmpty()) {
                                        annotation.setPurity(Double.parseDouble(purity));
                                    }
                                    if (retentionTime != null && !retentionTime.isEmpty()) {
                                        annotation.setRetentionTime(Double.parseDouble(retentionTime));
                                    }
                                    if (retentionIndex != null && !retentionIndex.isEmpty()) {
                                        annotation.setRetentionindex(Double.parseDouble(retentionIndex));
                                    }
                                    if (uniqueMass != null && !uniqueMass.isEmpty()) {
                                        annotation.setUniqueMass(Double.parseDouble(uniqueMass));
                                    }
                                    if (correctionFailed != null && !correctionFailed.isEmpty()) {
                                        annotation.setCorrectionFailed(Boolean.parseBoolean(correctionFailed));
                                    }
                                    if (lecoVersion != null && !lecoVersion.isEmpty()) {
                                        annotation.setLeco(lecoVersion);
                                    }
                                    if (binID != null && !binID.isEmpty()) {
                                        annotation.setBinid(Integer.parseInt(binID));
                                    }
                                    if (bin != null && !bin.isEmpty()) {
                                        annotation.setBin(bin);
                                    }
                                    if (group != null && !group.isEmpty()) {
                                        annotation.setGroup(group);
                                    }
                                    if (binRetentionIndex != null && !binRetentionIndex.isEmpty()) {
                                        annotation.setBinRetentionIndex(Double.parseDouble(binRetentionIndex));
                                    }
                                    if (binUniqueMass != null && !binUniqueMass.isEmpty()) {
                                        annotation.setBinUniqueMass(Double.parseDouble(binUniqueMass));
                                    }
                                    if (quantMass != null && !quantMass.isEmpty()) {
                                        annotation.setQuantMass(Double.parseDouble(quantMass));
                                    }
                                    if (spectra != null && !spectra.isEmpty()) {
                                        annotation.setSpectra(spectra);
                                    }

                                    annotations.add(annotation);
                                }
                            }
                        }

                        result.setAnnotations(annotations);

                        if (clazz.getChild("treatments") != null) {
                            if (clazz.getChild("treatments").getChild("treatment") != null) {
                                result.setTreatmentName(clazz.getChild("treatments").getChild("treatment").getAttributeValue("name"));
                                result.setTreatmentValue(clazz.getChild("treatments").getChild("treatment").getAttributeValue("value"));
                            }
                        }
                        results.add(result);
                    }
                }
            }


            if (results.size() > 0) {
                logger.debug("contains sample count: " + results.size());
                return results;
            } else {
                throw new Exception("this file had 0 samples and can't be processssed!");
            }
        } catch (Exception e) {
            logger.warn("error: " + e.getMessage());
            return Collections.emptyList();
        }

    }
        /**
         * parses the minix xml files and if successful returns a list of all the found message objects or an empty list
         *
         * @param file
         * @return
         */
    public List<SampleInformationResult> loadData(File file) throws FileNotFoundException {
        return loadData(new FileInputStream(file));
    }
}
