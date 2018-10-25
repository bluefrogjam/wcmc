package edu.ucdavis.genomics.metabolomics.sjp.transform;

/**
 * transforms from rt to ri, but has quite a big error
 *
 * @author wohlgemuth
 */
public class RTX5RTtoRTX5RITransformator implements Transformer<Double> {


    /**
     * calibration is based on rtx5 column
     */
    public Double transform(Double toTransform) {
        return (0.1772 * toTransform + 295.11) * 1000;
    }

}
