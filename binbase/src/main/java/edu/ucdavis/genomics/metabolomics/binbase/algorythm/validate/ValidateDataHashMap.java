/*
 * Created on 02.05.2003
 *
 * To change the template for this generated file go to Window>Preferences>Java>Code Generation>Code and Comments
 */
package edu.ucdavis.genomics.metabolomics.binbase.algorythm.validate;

import edu.ucdavis.genomics.metabolomics.exception.InvalidException;
import edu.ucdavis.genomics.metabolomics.util.BasicObject;

import java.util.Map;


/**
 * @author wohlgemuth
 *
 * To change the template for this generated type comment go to Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ValidateDataHashMap extends BasicObject {
    /**
     * ?berpr?ft ob eine hashmap valid ist
     * @param hash
     */
    public static  final void validate(Map hash) throws InvalidException {
        if (hash == null) {
            throw new InvalidException(" map not initialized!");
        } else {
            if (((String) hash.get("Area")) == null) {
                throw new InvalidException(" need attribut \"Area\"!");
            }

            if (((String) hash.get("S/N")) == null) {
                throw new InvalidException(" need attribut \"S/N\"!");
            }

            if (((String) hash.get("Purity")) == null) {
                throw new InvalidException(" need attribut \"Purity\"!");
            }

            if (((String) hash.get("Retention Index")) == null) {
                throw new InvalidException(
                    " need attribut \"Retention Index\"!");
            }

            if (((String) hash.get("UniqueMass")) == null) {
                throw new InvalidException(" need attribut \"UniqueMass\"!");
            }

            if (((String) hash.get("Quant S/N")) == null) {
                throw new InvalidException(" need attribut \"Quant S/N\"!");
            }

            /*
             * if ( ( (String) hash.get("Full Width at Half Height")) == null) { // validate = false; logger.error(" need attribut \"Full Width at Half
             * Height\"!"); }
             */
            if (((String) hash.get("Quant Masses")) == null) {
                throw new InvalidException(" need attribut \"Quant Masses\"!");
            }

            if (((String) hash.get("Height")).length() == 0) {
                throw new InvalidException(" need attribut \"Height\"!");
            }

            if (((String) hash.get("Spectra")).length() == 0) {
                throw new InvalidException(" need attribut \"Spectra\"!");
            }
        }
    }

    /**
     * ?berpr?ft ob eine hashmap valid ist
     * @param hash
     */
    public static  final void validateRow(Map hash, Map entrys)
        throws InvalidException {
        if (hash == null) {
            throw new InvalidException(" map not initialized!");
        } else {
            if (((String) hash.get("Area")) == null) {
                throw new InvalidException(" need attribut \"Area\"!");
            }

            if (((String) hash.get("S/N")) == null) {
                throw new InvalidException(" need attribut \"S/N\"!");
            }

            if (((String) hash.get("Purity")) == null) {
                throw new InvalidException(" need attribut \"Purity\"!");
            }

            if (((String) hash.get("Retention Index")) == null) {
                throw new InvalidException(
                    " need attribut \"Retention Index\"!");
            }

            if (((String) hash.get("UniqueMass")) == null) {
                throw new InvalidException(" need attribut \"UniqueMass\"!");
            }

            if (((String) hash.get("Quant S/N")) == null) {
                throw new InvalidException(" need attribut \"Quant S/N\"!");
            }

            /*
             * if ( ( (String) hash.get("Full Width at Half Height")) == null) { // validate = false; logger.error(" need attribut \"Full Width at Half
             * Height\"!"); }
             */
            if (((String) hash.get("Quant Masses")) == null) {
                throw new InvalidException(" need attribut \"Quant Masses\"!");
            }

            if (((String) hash.get("Height")).length() == 0) {
                throw new InvalidException(" need attribut \"Height\"!");
            }

            if (((String) hash.get("Spectra")).length() == 0) {
                throw new InvalidException(" need attribut \"Spectra\"!");
            }
        }
    }
}
