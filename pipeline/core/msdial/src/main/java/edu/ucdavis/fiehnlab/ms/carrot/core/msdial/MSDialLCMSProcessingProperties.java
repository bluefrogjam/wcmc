package edu.ucdavis.fiehnlab.ms.carrot.core.msdial;

import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.AccuracyType;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.MSDataType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Created by diego on 10/13/2016.
 */
@ConfigurationProperties
@Component
@Profile("carrot.lcms")
public class MSDialLCMSProcessingProperties extends MSDialProcessingProperties {

    public MSDialLCMSProcessingProperties() {
        this.massRangeBegin = 100;
        this.massRangeEnd = 1700;

        this.retentionTimeBegin = 0.5;
        this.retentionTimeEnd = 12.5;

        this.smoothingLevel = 1;
        this.massSliceWidth = 0.1;
        this.averagePeakWidth = 5;
        this.minimumAmplitude = 2500;
        this.minimumDataPoints = 5;
        this.amplitudeCutoff = 0;

        this.dataType = MSDataType.PROFILE;
        this.accuracyType = AccuracyType.ACCURATE;

        this.centroidMS1Tolerance = 0.01;
        this.centroidMS2Tolerance = 0.1;

        this.maxChargeNumber = 2;
        this.maxTraceNumber = 8;
        this.keptIsotopeRange = 0.5;

        this.peakDetectionBasedCentroid = true;
        this.removeAfterPrecursor = true;
    }
}
