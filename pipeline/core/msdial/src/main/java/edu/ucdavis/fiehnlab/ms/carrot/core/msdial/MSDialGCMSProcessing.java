package edu.ucdavis.fiehnlab.ms.carrot.core.msdial;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.deconvolution.gcms.GCMSDeconvolution;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.isotope.IsotopeEstimator;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.peakpicking.gcms.GCMSPeakSpotting;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.MSDialGCMSProcessedSample;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.PeakAreaBean;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.gcms.MS1DeconvolutionResult;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.TypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("carrot.gcms")
public class MSDialGCMSProcessing implements MSDialProcessing {

    private Logger logger = LoggerFactory.getLogger(MSDialGCMSProcessing.class);

    public Sample process(Sample sample, MSDialProcessingProperties properties) {
        List<Feature> spectra = TypeConverter.getJavaSpectrumList(sample);

        // Peak picking
        List<PeakAreaBean> detectedPeaks = new GCMSPeakSpotting().getPeakSpots(spectra, properties);
        logger.debug("Peaks after peak detection: " + detectedPeaks.size());

        // Isotope detection
        new IsotopeEstimator().setIsotopeInformation(detectedPeaks, properties);
        logger.debug("Peaks after isotope estimation: " + detectedPeaks.size());

        // Calculate peak properties for deconvolution
        // DataSummary bean does not appear to be used, so skipping that translation
        List<MS1DeconvolutionResult> deconvolutionResults = new GCMSDeconvolution().gcmsMS1Deconvolution(spectra, detectedPeaks, properties);
        logger.debug("Peaks after deconvolution: " + deconvolutionResults.size());

        logger.info("Found " + deconvolutionResults.size() + " deconvoluted features");

        return new MSDialGCMSProcessedSample(deconvolutionResults, properties.ionMode, sample.fileName());
    }
}
