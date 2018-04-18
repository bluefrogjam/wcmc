package edu.ucdavis.fiehnlab.ms.carrot.core.msdial;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.UnknownMode;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.AccuracyType;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.MSDataType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by sajjan on 10/13/2016.
 */
@ConfigurationProperties
@Component
public class MSDialGCMSProcessingProperties extends MSDialProcessingProperties {

    public MSDialGCMSProcessingProperties() {
        this.massRangeBegin = 0;
        this.massRangeEnd = 1000;

        this.retentionTimeBegin = 0;
        this.retentionTimeEnd = 100;

        this.smoothingLevel = 3;
        this.massSliceWidth = 0.1;
        this.averagePeakWidth = 20;
        this.minimumAmplitude = 1000;
        this.minimumDataPoints = 10;
        this.amplitudeCutoff = 10;

        this.ionMode = new UnknownMode();

        this.accuracyType = AccuracyType.NOMINAL;
        this.massAccuracy = 0.025;
        this.dataType = MSDataType.CENTROID;
    }

    public String toString() {
        return (
            "massRangeBegin: " + massRangeBegin +
                "\nmassRangeEnd: " + massRangeEnd +
                "\nretentionTimeBegin: " + retentionTimeBegin +
                "\nretentionTimeEnd: " + retentionTimeEnd +
                "\nsmoothingMethod: " + smoothingMethod +
                "\nsmoothingLevel: " + smoothingLevel +
                "\naveragePeakWidth: " + averagePeakWidth +
                "\nminimumAmplitude: " + minimumAmplitude +
                "\nmassSliceWith: " + massSliceWidth +
                "\nmassAccuracy: " + massAccuracy +
                "\ndataType: " + dataType +
                "\nionMode: " + ionMode +
                "\namplitudeCutoff: " + amplitudeCutoff +
                "\nminimumDataPoints: " + minimumDataPoints
        );
    }
}
