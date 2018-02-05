package edu.ucdavis.fiehnlab.ms.carrot.core.msdial;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.deconvolution.SpectralDeconvolution;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.isotope.IsotopeEstimator;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.peakpicking.DataDependentPeakSpotting;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.MS2DecResult;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.PeakAreaBean;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.TypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MSDialProcessing {

    private static Logger logger = LoggerFactory.getLogger(MSDialProcessing.class);

    public Sample process(Sample sample, MSDialProcessingProperties properties) {
        List<Feature> spectra = TypeConverter.getJavaSpectrumList(sample);

        // Peak picking
        List<PeakAreaBean> detectedPeaks = new DataDependentPeakSpotting().getPeaks(spectra, properties);

        logger.debug("Peaks before isotope estimation: " + detectedPeaks.size());
        // Isotope detection
        new IsotopeEstimator().setIsotopeInformation(detectedPeaks, properties);
        logger.debug("Peaks after  isotope estimation: " + detectedPeaks.size());

        // Calculate peak properties for deconvolution
        // DataSummary bean does not appear to be used, so skipping that translation
        List<MS2DecResult> deconvolutionResults = new SpectralDeconvolution().getMS2Deconvolution(spectra, detectedPeaks, properties);



        logger.warn("Returning tha input sample");
        // TODO add deconvolution and return a ProcessedSample
        return sample;
    }

}
