package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils;

/**
 * Created by diego on 7/22/2016.
 */
public enum SmoothingMethod {
	LINEAR_WEIGHTED_MOVING_AVERAGE,
	SIMPLE_MOVING_AVERAGE,
	SAVITZKY_GOLAY_FILTER,
	BINOMIAL_FILTER,
	LOWESS_FILTER,
	LOESS_FILTER
}
