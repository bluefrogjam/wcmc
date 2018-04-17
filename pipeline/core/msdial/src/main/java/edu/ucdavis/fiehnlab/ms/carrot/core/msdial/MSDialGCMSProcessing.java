package edu.ucdavis.fiehnlab.ms.carrot.core.msdial;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.deconvolution.SpectralDeconvolution;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.peakpicking.gcms.GCMSPeakSpotting;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.MS2DecResult;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.MSDialProcessedSample;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.PeakAreaBean;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.TypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MSDialGCMSProcessing {
    private Logger logger = LoggerFactory.getLogger(MSDialGCMSProcessing.class);

    public Sample process(Sample sample, MSDialGCMSProcessingProperties properties) {
        List<Feature> spectra = TypeConverter.getJavaSpectrumList(sample);

        // Peak picking
        List<PeakAreaBean> detectedPeaks = new GCMSPeakSpotting().getPeakSpots(spectra, properties);
        logger.debug("Peaks after peak detection: " + detectedPeaks.size());

        // Calculate peak properties for deconvolution
        // DataSummary bean does not appear to be used, so skipping that translation
        List<MS2DecResult> deconvolutionResults = new SpectralDeconvolution().getMS2Deconvolution(spectra, detectedPeaks, properties);
        logger.debug("Peaks after deconvolution: " + deconvolutionResults.size());

        logger.info("Found " + deconvolutionResults.size() + " deconvoluted features");

        return new MSDialProcessedSample(deconvolutionResults, properties.ionMode, sample.fileName());
    }
}
