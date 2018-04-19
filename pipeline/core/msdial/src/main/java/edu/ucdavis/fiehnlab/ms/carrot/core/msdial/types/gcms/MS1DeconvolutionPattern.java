package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.gcms;

/**
 * The current chromatogram deconvolution can consider up to 4 coeluting metabolites.
 * The target deconvoluted chromatogram is always defined as 'C' in this enum.
 * The left edge's scan number and right edge's scan number of target chromatogram are determined.
 * The adjacent model chromatograms, in which the peak right edge (or peak right edge) is inside of
 * the RT range of target peak, are considered as 'coeluting' components.
 *     B is the left model chromatogram to C
 *     A is second left model chromatogram to C
 *     D is the right model chrom to C
 *     E is second right chromatogram to C
 *
 * Created by sajjan on 04/16/2018.
 */
public enum MS1DeconvolutionPattern {
    C, BC, CD, ABC, BCD, CDE, ABCD, BCDE, ABCDE
}
