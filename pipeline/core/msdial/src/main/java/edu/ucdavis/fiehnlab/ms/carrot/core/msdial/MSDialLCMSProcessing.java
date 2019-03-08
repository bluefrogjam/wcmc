package edu.ucdavis.fiehnlab.ms.carrot.core.msdial;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Profile("carrot.lcms")
public class MSDialLCMSProcessing implements MSDialProcessing {

    private Optional<SampleSerializer> serializer;

    private Logger logger = LoggerFactory.getLogger(MSDialLCMSProcessing.class);

    @Autowired
    public MSDialLCMSProcessing(Optional<SampleSerializer> serializer) {
        this.serializer = serializer;
    }

    public MSDialLCMSProcessedSample process(Sample sample, MSDialProcessingProperties properties) {
        List<? extends Feature> spectra = TypeConverter.getJavaSpectrumList(sample);

        // Peak picking
        logger.info("\tpeak picking...");
        List<PeakAreaBean> detectedPeaks = new DataDependentPeakSpotting().getPeaks(spectra, properties);

        // Isotope detection
        logger.info("\tdetecting isotopes...");
        new IsotopeEstimator().setIsotopeInformation(detectedPeaks, properties);

        // Calculate peak properties for deconvolution
        // DataSummary bean does not appear to be used, so skipping that translation
        logger.info("\tdeconvoluting ms2 spectra...");
        List<MS2DeconvolutionResult> deconvolutionResults = new SpectralDeconvolution().getMS2Deconvolution(spectra, detectedPeaks, properties);
        logger.info("Found " + deconvolutionResults.size() + " total deconvoluted features");
        logger.info("Found " + deconvolutionResults.stream().filter(d -> d.peak.ms2LevelDataPointNumber < 0).count() + " MS1 spectra");
        logger.info("Found " + deconvolutionResults.stream().filter(d -> d.peak.ms2LevelDataPointNumber > 0).count() + " MS2 spectra");

        MSDialLCMSProcessedSample processed = new MSDialLCMSProcessedSample(deconvolutionResults, properties.ionMode, sample.fileName());

        serializer.ifPresent(sampleSerializer -> sampleSerializer.saveFile(processed));

        return processed;
    }
}
