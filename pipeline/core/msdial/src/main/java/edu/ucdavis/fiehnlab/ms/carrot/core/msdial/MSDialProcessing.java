package edu.ucdavis.fiehnlab.ms.carrot.core.msdial;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample;

public interface MSDialProcessing {
    Sample process(Sample sample, MSDialProcessingProperties properties);
}
