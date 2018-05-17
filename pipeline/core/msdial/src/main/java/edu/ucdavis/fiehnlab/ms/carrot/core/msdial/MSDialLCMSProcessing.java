package edu.ucdavis.fiehnlab.ms.carrot.core.msdial;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.deconvolution.lcms.SpectralDeconvolution;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.isotope.IsotopeEstimator;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.peakpicking.lcms.DataDependentPeakSpotting;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.MSDialLCMSProcessedSample;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.PeakAreaBean;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.lcms.MS2DeconvolutionResult;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.SampleSerializer;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.TypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("carrot.lcms")
public class MSDialLCMSProcessing implements MSDialProcessing {

    private final SampleSerializer serializer;

    private Logger logger = LoggerFactory.getLogger(MSDialLCMSProcessing.class);

    @Autowired(required = false)
    public MSDialLCMSProcessing(SampleSerializer serializer) {
        this.serializer = serializer;
    }

    public Sample process(Sample sample, MSDialProcessingProperties properties) {
        List<Feature> spectra = TypeConverter.getJavaSpectrumList(sample);

        // Peak picking
        List<PeakAreaBean> detectedPeaks = new DataDependentPeakSpotting().getPeaks(spectra, properties);
        logger.debug("Peaks after peak detection: " + detectedPeaks.size());

        // Isotope detection
        new IsotopeEstimator().setIsotopeInformation(detectedPeaks, properties);
        logger.debug("Peaks after isotope estimation: " + detectedPeaks.size());

        // Calculate peak properties for deconvolution
        // DataSummary bean does not appear to be used, so skipping that translation
        List<MS2DeconvolutionResult> deconvolutionResults = new SpectralDeconvolution().getMS2Deconvolution(spectra, detectedPeaks, properties);
        logger.debug("Peaks after deconvolution: " + deconvolutionResults.size());

        logger.info("Found " + deconvolutionResults.size() + " deconvoluted features");

        MSDialLCMSProcessedSample processed = new MSDialLCMSProcessedSample(deconvolutionResults, properties.ionMode, sample.fileName());

        if (serializer != null) {
            serializer.saveFile(processed);
        }

        return processed;
    }
}
