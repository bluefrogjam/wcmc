package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.deconvolution.gcms;

import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.math.BasicMathematics;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.math.LUMatrix;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.math.MatrixOperations;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.Peak;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.PeakQuality;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.gcms.MS1DeconvolutionPattern;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.gcms.MS1DeconvolutionResult;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.gcms.ModelChromatogramVector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by diego on 8/12/2016.
 */
class MS1DeconvolutionUtilities {
	
	static MS1DeconvolutionResult getMS1DeconvolutionResult(ModelChromatogramVector ModelChromatogramVector, List<List<Peak>> ms1Chromatograms) {
		switch (ModelChromatogramVector.ms1DeconvolutionPattern) {
			case C:
				return MS1DeconvolutionPatternSingle(ModelChromatogramVector, ms1Chromatograms);
			case BC:
				return MS1DeconvolutionPatternDouble(ModelChromatogramVector.ms1DeconvolutionPattern, ModelChromatogramVector, ms1Chromatograms);
			case CD:
				return MS1DeconvolutionPatternDouble(ModelChromatogramVector.ms1DeconvolutionPattern, ModelChromatogramVector, ms1Chromatograms);
			case ABC:
				return MS1DeconvolutionPatternTriple(ModelChromatogramVector.ms1DeconvolutionPattern, ModelChromatogramVector, ms1Chromatograms);
			case BCD:
				return MS1DeconvolutionPatternTriple(ModelChromatogramVector.ms1DeconvolutionPattern, ModelChromatogramVector, ms1Chromatograms);
			case CDE:
				return MS1DeconvolutionPatternTriple(ModelChromatogramVector.ms1DeconvolutionPattern, ModelChromatogramVector, ms1Chromatograms);
			case ABCD:
				return MS1DeconvolutionPatternQuadruple(ModelChromatogramVector.ms1DeconvolutionPattern, ModelChromatogramVector, ms1Chromatograms);
			case BCDE:
				return MS1DeconvolutionPatternQuadruple(ModelChromatogramVector.ms1DeconvolutionPattern, ModelChromatogramVector, ms1Chromatograms);
			case ABCDE:
				return MS1DeconvolutionPatternQuintuple(ModelChromatogramVector, ms1Chromatograms);
            default:
                return null;
		}
	}


    /**
     *
     * @param ModelChromatogramVector
     * @param ms1Chromatograms
     * @return
     */
	private static MS1DeconvolutionResult MS1DeconvolutionPatternSingle(ModelChromatogramVector ModelChromatogramVector, List<List<Peak>> ms1Chromatograms) {
		double[] constArray = new double[ModelChromatogramVector.chromatogramScanList.size()];
		double[] linearArray = new double[ModelChromatogramVector.chromatogramScanList.size()];
		double[] tModelArray = new double[ModelChromatogramVector.chromatogramScanList.size()];
		double[] expIntArray = new double[ModelChromatogramVector.chromatogramScanList.size()];

		//initialize
		for (int i = 0; i < ModelChromatogramVector.chromatogramScanList.size(); i++) {
			constArray[i] = 1;
			linearArray[i] = i;
			tModelArray[i] = ModelChromatogramVector.targetIntensityArray.get(i);
		}

		double t_t = BasicMathematics.sumOfSquare(tModelArray);
		double la_la = BasicMathematics.sumOfSquare(linearArray);
		double ca_ca = BasicMathematics.sumOfSquare(constArray);

		double t_la = BasicMathematics.innerProduct(tModelArray, linearArray);
		double t_ca = BasicMathematics.innerProduct(tModelArray, constArray);
		double la_ca = BasicMathematics.innerProduct(linearArray, constArray);

		double[][] matrix = new double[][]{
				{t_t, t_la, t_ca},
				{t_la, la_la, la_ca},
				{t_ca, la_ca, ca_ca}
		};

		LUMatrix luMatrix = MatrixOperations.matrixDecompose(matrix);

		if (luMatrix == null) {
			System.out.println("LU Matrix null (pattern single)");
			return null;
		}

		double detA = MatrixOperations.determinantA(luMatrix);

		if (detA == 0) {
			System.out.println("Det A zero (pattern single)");
			return null;
		}

		double[][] invMatrix = MatrixOperations.matrixInverse(luMatrix);

		List<List<Peak>> deconvolutedPeaklistList = new ArrayList<>();
		List<List<Peak>> originalChromatograms = new ArrayList<>();

		for (List<Peak> chrom : ms1Chromatograms) {
			for (int i = 0; i < chrom.size(); i++)
			    expIntArray[i] = chrom.get(i).intensity;

			double z_t = BasicMathematics.innerProduct(tModelArray, expIntArray);
			double z_la = BasicMathematics.innerProduct(linearArray, expIntArray);
			double z_ca = BasicMathematics.innerProduct(constArray, expIntArray);

			Double coefficient = invMatrix[0][0] * z_t + invMatrix[0][1] * z_la + invMatrix[0][2] * z_ca;

			if (coefficient <= 0) {
				if (!tryGetAdhocCoefficient(expIntArray, ModelChromatogramVector.targetScanTopInModelChromatogramVector)) {
					continue;
				}
			}

			List<Peak> dPeaklist = getDeconvolutedPeaklist(coefficient, ModelChromatogramVector, chrom.get(ModelChromatogramVector.targetScanTopInModelChromatogramVector).mz, expIntArray);

			if (dPeaklist != null) {
				originalChromatograms.add(chrom);
				deconvolutedPeaklistList.add(dPeaklist);
			}
		}

		return MS1DeconvolutionResultProperty(ModelChromatogramVector, deconvolutedPeaklistList, originalChromatograms);
	}

    /**
     *
     * @param pattern
     * @param ModelChromatogramVector
     * @param ms1Chromatograms
     * @return
     */
	private static MS1DeconvolutionResult MS1DeconvolutionPatternDouble(MS1DeconvolutionPattern pattern, ModelChromatogramVector ModelChromatogramVector, List<List<Peak>> ms1Chromatograms) {
		double[] constArray = new double[ModelChromatogramVector.chromatogramScanList.size()];
		double[] linearArray = new double[ModelChromatogramVector.chromatogramScanList.size()];
		double[] tModelArray = new double[ModelChromatogramVector.chromatogramScanList.size()];
		double[] aModelArray = new double[ModelChromatogramVector.chromatogramScanList.size()];
		double[] expIntArray = new double[ModelChromatogramVector.chromatogramScanList.size()];

		//initialize
		for (int i = 0; i < ModelChromatogramVector.chromatogramScanList.size(); i++) {
			constArray[i] = 1;
			linearArray[i] = i;
			tModelArray[i] = ModelChromatogramVector.targetIntensityArray.get(i);

			if (pattern == MS1DeconvolutionPattern.BC)
				aModelArray[i] = ModelChromatogramVector.oneLeftIntensityArray.get(i);
			else if (pattern == MS1DeconvolutionPattern.CD)
				aModelArray[i] = ModelChromatogramVector.oneRightIntensityArray.get(i);
		}

		double t_t = BasicMathematics.sumOfSquare(tModelArray);
		double a_a = BasicMathematics.sumOfSquare(aModelArray);
		double la_la = BasicMathematics.sumOfSquare(linearArray);
		double ca_ca = BasicMathematics.sumOfSquare(constArray);

		double t_a = BasicMathematics.innerProduct(tModelArray, aModelArray);
		double t_la = BasicMathematics.innerProduct(tModelArray, linearArray);
		double t_ca = BasicMathematics.innerProduct(tModelArray, constArray);

		double a_la = BasicMathematics.innerProduct(aModelArray, linearArray);
		double a_ca = BasicMathematics.innerProduct(aModelArray, constArray);

		double la_ca = BasicMathematics.innerProduct(linearArray, constArray);

		double[][] matrix = new double[][]{
				{t_t, t_a, t_la, t_ca},
				{t_a, a_a, a_la, a_ca},
				{t_la, a_la, la_la, la_ca},
				{t_ca, a_ca, la_ca, ca_ca}
		};

		LUMatrix luMatrix = MatrixOperations.matrixDecompose(matrix);

		if (luMatrix == null) {
			System.out.println("LU Matrix null (pattern double)");
			return MS1DeconvolutionPatternSingle(ModelChromatogramVector, ms1Chromatograms);
		}

		double detA = MatrixOperations.determinantA(luMatrix);

		if (detA == 0) {
			System.out.println("Det A zero (pattern double)");
			return MS1DeconvolutionPatternSingle(ModelChromatogramVector, ms1Chromatograms);
		}

		double[][] invMatrix = MatrixOperations.matrixInverse(luMatrix);

		List<List<Peak>> deconvolutedPeaklistList = new ArrayList<>();
		List<List<Peak>> originalChromatograms = new ArrayList<>();

		for (List<Peak> chrom : ms1Chromatograms) {
			for (int i = 0; i < chrom.size(); i++)
			    expIntArray[i] = chrom.get(i).intensity;

			double z_t = BasicMathematics.innerProduct(tModelArray, expIntArray);
			double z_a = BasicMathematics.innerProduct(aModelArray, expIntArray);
			double z_la = BasicMathematics.innerProduct(linearArray, expIntArray);
			double z_ca = BasicMathematics.innerProduct(constArray, expIntArray);

			Double coefficient = invMatrix[0][0] * z_t + invMatrix[0][1] * z_a + invMatrix[0][2] * z_la + invMatrix[0][3] * z_ca;

			if (coefficient <= 0)
				if (!tryGetAdhocCoefficient(expIntArray, ModelChromatogramVector.targetScanTopInModelChromatogramVector))
					continue;

			List<Peak> dPeaklist = getDeconvolutedPeaklist(coefficient, ModelChromatogramVector, chrom.get(ModelChromatogramVector.targetScanTopInModelChromatogramVector).mz, expIntArray);

			if (dPeaklist != null) {
				originalChromatograms.add(chrom);
				deconvolutedPeaklistList.add(dPeaklist);
			}
		}
		return MS1DeconvolutionResultProperty(ModelChromatogramVector, deconvolutedPeaklistList, originalChromatograms);
	}

    /**
     * 
     * @param pattern
     * @param ModelChromatogramVector
     * @param ms1Chromatograms
     * @return
     */
	private static MS1DeconvolutionResult MS1DeconvolutionPatternTriple(MS1DeconvolutionPattern pattern, ModelChromatogramVector ModelChromatogramVector, List<List<Peak>> ms1Chromatograms) {
		double[] constArray = new double[ModelChromatogramVector.chromatogramScanList.size()];
		double[] linearArray = new double[ModelChromatogramVector.chromatogramScanList.size()];
		double[] tModelArray = new double[ModelChromatogramVector.chromatogramScanList.size()];
		double[] aModelArray = new double[ModelChromatogramVector.chromatogramScanList.size()];
		double[] bModelArray = new double[ModelChromatogramVector.chromatogramScanList.size()];
		double[] expIntArray = new double[ModelChromatogramVector.chromatogramScanList.size()];

		//initialize
		for (int i = 0; i < ModelChromatogramVector.chromatogramScanList.size(); i++) {
			constArray[i] = 1;
			linearArray[i] = i;
			tModelArray[i] = ModelChromatogramVector.targetIntensityArray.get(i);

			if (pattern == MS1DeconvolutionPattern.ABC) {
				aModelArray[i] = ModelChromatogramVector.twoLeftIntensityArray.get(i);
				bModelArray[i] = ModelChromatogramVector.oneLeftIntensityArray.get(i);
			} else if (pattern == MS1DeconvolutionPattern.BCD) {
				aModelArray[i] = ModelChromatogramVector.oneLeftIntensityArray.get(i);
				bModelArray[i] = ModelChromatogramVector.oneRightIntensityArray.get(i);
			} else if (pattern == MS1DeconvolutionPattern.CDE) {
				aModelArray[i] = ModelChromatogramVector.oneRightIntensityArray.get(i);
				bModelArray[i] = ModelChromatogramVector.twoRightInetnsityArray.get(i);
			}
		}

		double t_t = BasicMathematics.sumOfSquare(tModelArray);
		double a_a = BasicMathematics.sumOfSquare(aModelArray);
		double b_b = BasicMathematics.sumOfSquare(bModelArray);
		double la_la = BasicMathematics.sumOfSquare(linearArray);
		double ca_ca = BasicMathematics.sumOfSquare(constArray);

		double t_a = BasicMathematics.innerProduct(tModelArray, aModelArray);
		double t_b = BasicMathematics.innerProduct(tModelArray, bModelArray);
		double t_la = BasicMathematics.innerProduct(tModelArray, linearArray);
		double t_ca = BasicMathematics.innerProduct(tModelArray, constArray);

		double a_b = BasicMathematics.innerProduct(aModelArray, bModelArray);
		double a_la = BasicMathematics.innerProduct(aModelArray, linearArray);
		double a_ca = BasicMathematics.innerProduct(aModelArray, constArray);

		double b_la = BasicMathematics.innerProduct(bModelArray, linearArray);
		double b_ca = BasicMathematics.innerProduct(bModelArray, constArray);

		double la_ca = BasicMathematics.innerProduct(linearArray, constArray);

		double[][] matrix = new double[][]{
				{t_t, t_a, t_b, t_la, t_ca},
				{t_a, a_a, a_b, a_la, a_ca},
				{t_b, a_b, b_b, b_la, b_ca},
				{t_la, a_la, b_la, la_la, la_ca},
				{t_ca, a_ca, b_ca, la_ca, ca_ca}
		};

		LUMatrix luMatrix = MatrixOperations.matrixDecompose(matrix);

		if (luMatrix == null) {
			System.out.println("LU Matrix null (pattern triple)");
			if (pattern == MS1DeconvolutionPattern.ABC)
				return MS1DeconvolutionPatternDouble(MS1DeconvolutionPattern.BC, ModelChromatogramVector, ms1Chromatograms);
			else
				return MS1DeconvolutionPatternDouble(MS1DeconvolutionPattern.CD, ModelChromatogramVector, ms1Chromatograms);
		}

		double detA = MatrixOperations.determinantA(luMatrix);

		if (detA == 0) {
			System.out.println("Det A zero (pattern triple)");
			if (pattern == MS1DeconvolutionPattern.ABC)
				return MS1DeconvolutionPatternDouble(MS1DeconvolutionPattern.BC, ModelChromatogramVector, ms1Chromatograms);
			else
				return MS1DeconvolutionPatternDouble(MS1DeconvolutionPattern.CD, ModelChromatogramVector, ms1Chromatograms);
		}

		double[][] invMatrix = MatrixOperations.matrixInverse(luMatrix);

		List<List<Peak>> deconvolutedPeaklistList = new ArrayList<>();
		List<List<Peak>> originalChromatograms = new ArrayList<>();

		for (List<Peak> chrom : ms1Chromatograms) {
			for (int i = 0; i < chrom.size(); i++)
			    expIntArray[i] = chrom.get(i).intensity;

			double z_t = BasicMathematics.innerProduct(tModelArray, expIntArray);
			double z_a = BasicMathematics.innerProduct(aModelArray, expIntArray);
			double z_b = BasicMathematics.innerProduct(bModelArray, expIntArray);
			double z_la = BasicMathematics.innerProduct(linearArray, expIntArray);
			double z_ca = BasicMathematics.innerProduct(constArray, expIntArray);

			double coefficient = invMatrix[0][0] * z_t + invMatrix[0][1] * z_a + invMatrix[0][2] * z_b +
                    invMatrix[0][3] * z_la + invMatrix[0][4] * z_ca;

			if (coefficient <= 0)
				if (!tryGetAdhocCoefficient(expIntArray, ModelChromatogramVector.targetScanTopInModelChromatogramVector))
					continue;

			List<Peak> dPeaklist = getDeconvolutedPeaklist(coefficient, ModelChromatogramVector, chrom.get(ModelChromatogramVector.targetScanTopInModelChromatogramVector).mz, expIntArray);

			if (dPeaklist != null) {
				originalChromatograms.add(chrom);
				deconvolutedPeaklistList.add(dPeaklist);
			}
		}
		return MS1DeconvolutionResultProperty(ModelChromatogramVector, deconvolutedPeaklistList, originalChromatograms);
	}

	private static MS1DeconvolutionResult MS1DeconvolutionPatternQuadruple(MS1DeconvolutionPattern pattern, ModelChromatogramVector ModelChromatogramVector, List<List<Peak>> ms1Chromatograms) {
		double[] constArray = new double[ModelChromatogramVector.chromatogramScanList.size()];
		double[] linearArray = new double[ModelChromatogramVector.chromatogramScanList.size()];
		double[] tModelArray = new double[ModelChromatogramVector.chromatogramScanList.size()];
		double[] aModelArray = new double[ModelChromatogramVector.chromatogramScanList.size()];
		double[] bModelArray = new double[ModelChromatogramVector.chromatogramScanList.size()];
		double[] cModelArray = new double[ModelChromatogramVector.chromatogramScanList.size()];
		double[] expIntArray = new double[ModelChromatogramVector.chromatogramScanList.size()];

		//initialize
		for (int i = 0; i < ModelChromatogramVector.chromatogramScanList.size(); i++) {
			constArray[i] = 1;
			linearArray[i] = i;
			tModelArray[i] = ModelChromatogramVector.targetIntensityArray.get(i);

			if (pattern == MS1DeconvolutionPattern.ABCD) {
				aModelArray[i] = ModelChromatogramVector.twoLeftIntensityArray.get(i);
				bModelArray[i] = ModelChromatogramVector.oneLeftIntensityArray.get(i);
				cModelArray[i] = ModelChromatogramVector.oneRightIntensityArray.get(i);
			} else if (pattern == MS1DeconvolutionPattern.BCDE) {
				aModelArray[i] = ModelChromatogramVector.oneLeftIntensityArray.get(i);
				bModelArray[i] = ModelChromatogramVector.oneRightIntensityArray.get(i);
				cModelArray[i] = ModelChromatogramVector.twoRightInetnsityArray.get(i);
			}
		}

		double t_t = BasicMathematics.sumOfSquare(tModelArray);
		double a_a = BasicMathematics.sumOfSquare(aModelArray);
		double b_b = BasicMathematics.sumOfSquare(bModelArray);
		double c_c = BasicMathematics.sumOfSquare(cModelArray);
		double la_la = BasicMathematics.sumOfSquare(linearArray);
		double ca_ca = BasicMathematics.sumOfSquare(constArray);

		double t_a = BasicMathematics.innerProduct(tModelArray, aModelArray);
		double t_b = BasicMathematics.innerProduct(tModelArray, bModelArray);
		double t_c = BasicMathematics.innerProduct(tModelArray, cModelArray);
		double t_la = BasicMathematics.innerProduct(tModelArray, linearArray);
		double t_ca = BasicMathematics.innerProduct(tModelArray, constArray);

		double a_b = BasicMathematics.innerProduct(aModelArray, bModelArray);
		double a_c = BasicMathematics.innerProduct(aModelArray, cModelArray);
		double a_la = BasicMathematics.innerProduct(aModelArray, linearArray);
		double a_ca = BasicMathematics.innerProduct(aModelArray, constArray);

		double b_c = BasicMathematics.innerProduct(bModelArray, cModelArray);
		double b_la = BasicMathematics.innerProduct(bModelArray, linearArray);
		double b_ca = BasicMathematics.innerProduct(bModelArray, constArray);

		double c_la = BasicMathematics.innerProduct(cModelArray, linearArray);
		double c_ca = BasicMathematics.innerProduct(cModelArray, constArray);

		double la_ca = BasicMathematics.innerProduct(linearArray, constArray);

		double[][] matrix = new double[][]{
				{t_t, t_a, t_b, t_c, t_la, t_ca},
				{t_a, a_a, a_b, a_c, a_la, a_ca},
				{t_b, a_b, b_b, b_c, b_la, b_ca},
				{t_c, a_c, b_c, c_c, c_la, c_ca},
				{t_la, a_la, b_la, c_la, la_la, la_ca},
				{t_ca, a_ca, b_ca, c_ca, la_ca, ca_ca}
		};

		LUMatrix luMatrix = MatrixOperations.matrixDecompose(matrix);

		if (luMatrix == null) {
			System.out.println("LU Matrix null (pattern quadruple)");
			return MS1DeconvolutionPatternTriple(MS1DeconvolutionPattern.BCD, ModelChromatogramVector, ms1Chromatograms);
		}

		double detA = MatrixOperations.determinantA(luMatrix);

		if (detA == 0) {
			System.out.println("Det A zero (pattern quadruple)");
			return MS1DeconvolutionPatternTriple(MS1DeconvolutionPattern.BCD, ModelChromatogramVector, ms1Chromatograms);
		}

		double[][] invMatrix = MatrixOperations.matrixInverse(luMatrix);

		List<List<Peak>> deconvolutedPeaklistList = new ArrayList<>();
		List<List<Peak>> originalChromatograms = new ArrayList<>();

		for (List<Peak> chrom : ms1Chromatograms) {
			for (int i = 0; i < chrom.size(); i++)
			    expIntArray[i] = chrom.get(i).intensity;

			double z_t = BasicMathematics.innerProduct(tModelArray, expIntArray);
			double z_a = BasicMathematics.innerProduct(aModelArray, expIntArray);
			double z_b = BasicMathematics.innerProduct(bModelArray, expIntArray);
			double z_c = BasicMathematics.innerProduct(cModelArray, expIntArray);
			double z_la = BasicMathematics.innerProduct(linearArray, expIntArray);
			double z_ca = BasicMathematics.innerProduct(constArray, expIntArray);

			double coefficient = invMatrix[0][0] * z_t + invMatrix[0][1] * z_a + invMatrix[0][2] * z_b +
                    invMatrix[0][3] * z_c + invMatrix[0][4] * z_la + invMatrix[0][5] * z_ca;

			if (coefficient <= 0)
				if (!tryGetAdhocCoefficient(expIntArray, ModelChromatogramVector.targetScanTopInModelChromatogramVector))
					continue;

			List<Peak> dPeaklist = getDeconvolutedPeaklist(coefficient, ModelChromatogramVector, chrom.get(ModelChromatogramVector.targetScanTopInModelChromatogramVector).mz, expIntArray);

			if (dPeaklist != null) {
				originalChromatograms.add(chrom);
				deconvolutedPeaklistList.add(dPeaklist);
			}
		}
		return MS1DeconvolutionResultProperty(ModelChromatogramVector, deconvolutedPeaklistList, originalChromatograms);
	}

	private static MS1DeconvolutionResult MS1DeconvolutionPatternQuintuple(ModelChromatogramVector ModelChromatogramVector, List<List<Peak>> ms1Chromatograms) {
		double[] constArray = new double[ModelChromatogramVector.chromatogramScanList.size()];
		double[] linearArray = new double[ModelChromatogramVector.chromatogramScanList.size()];
		double[] tModelArray = new double[ModelChromatogramVector.chromatogramScanList.size()];
		double[] aModelArray = new double[ModelChromatogramVector.chromatogramScanList.size()];
		double[] bModelArray = new double[ModelChromatogramVector.chromatogramScanList.size()];
		double[] cModelArray = new double[ModelChromatogramVector.chromatogramScanList.size()];
		double[] dModelArray = new double[ModelChromatogramVector.chromatogramScanList.size()];
		double[] expIntArray = new double[ModelChromatogramVector.chromatogramScanList.size()];

		//initialize
		for (int i = 0; i < ModelChromatogramVector.chromatogramScanList.size(); i++) {
			constArray[i] = 1;
			linearArray[i] = i;
			tModelArray[i] = ModelChromatogramVector.targetIntensityArray.get(i);
			aModelArray[i] = ModelChromatogramVector.twoLeftIntensityArray.get(i);
			bModelArray[i] = ModelChromatogramVector.oneLeftIntensityArray.get(i);
			cModelArray[i] = ModelChromatogramVector.oneRightIntensityArray.get(i);
			dModelArray[i] = ModelChromatogramVector.twoRightInetnsityArray.get(i);
		}

		double t_t = BasicMathematics.sumOfSquare(tModelArray);
		double a_a = BasicMathematics.sumOfSquare(aModelArray);
		double b_b = BasicMathematics.sumOfSquare(bModelArray);
		double c_c = BasicMathematics.sumOfSquare(cModelArray);
		double d_d = BasicMathematics.sumOfSquare(dModelArray);
		double la_la = BasicMathematics.sumOfSquare(linearArray);
		double ca_ca = BasicMathematics.sumOfSquare(constArray);

		double t_a = BasicMathematics.innerProduct(tModelArray, aModelArray);
		double t_b = BasicMathematics.innerProduct(tModelArray, bModelArray);
		double t_c = BasicMathematics.innerProduct(tModelArray, cModelArray);
		double t_d = BasicMathematics.innerProduct(tModelArray, dModelArray);
		double t_la = BasicMathematics.innerProduct(tModelArray, linearArray);
		double t_ca = BasicMathematics.innerProduct(tModelArray, constArray);

		double a_b = BasicMathematics.innerProduct(aModelArray, bModelArray);
		double a_c = BasicMathematics.innerProduct(aModelArray, cModelArray);
		double a_d = BasicMathematics.innerProduct(aModelArray, dModelArray);
		double a_la = BasicMathematics.innerProduct(aModelArray, linearArray);
		double a_ca = BasicMathematics.innerProduct(aModelArray, constArray);

		double b_c = BasicMathematics.innerProduct(bModelArray, cModelArray);
		double b_d = BasicMathematics.innerProduct(bModelArray, dModelArray);
		double b_la = BasicMathematics.innerProduct(bModelArray, linearArray);
		double b_ca = BasicMathematics.innerProduct(bModelArray, constArray);

		double c_d = BasicMathematics.innerProduct(cModelArray, dModelArray);
		double c_la = BasicMathematics.innerProduct(cModelArray, linearArray);
		double c_ca = BasicMathematics.innerProduct(cModelArray, constArray);

		double d_la = BasicMathematics.innerProduct(dModelArray, linearArray);
		double d_ca = BasicMathematics.innerProduct(dModelArray, constArray);

		double la_ca = BasicMathematics.innerProduct(linearArray, constArray);

		double[][] matrix = new double[][]{
				{t_t, t_a, t_b, t_c, t_d, t_la, t_ca},
				{t_a, a_a, a_b, a_c, a_d, a_la, a_ca},
				{t_b, a_b, b_b, b_c, b_d, b_la, b_ca},
				{t_c, a_c, b_c, c_c, c_d, c_la, c_ca},
				{t_d, a_d, b_d, c_d, d_d, d_la, d_ca},
				{t_la, a_la, b_la, c_la, d_la, la_la, la_ca},
				{t_ca, a_ca, b_ca, c_ca, d_ca, la_ca, ca_ca}
		};

		LUMatrix luMatrix = MatrixOperations.matrixDecompose(matrix);

		if (luMatrix == null) {
			System.out.println("LU Matrix null (pattern quintuple)");
			return MS1DeconvolutionPatternTriple(MS1DeconvolutionPattern.BCD, ModelChromatogramVector, ms1Chromatograms);
		}

		double detA = MatrixOperations.determinantA(luMatrix);

		if (detA == 0) {
			System.out.println("Det A zero (pattern quintuple)");
			return MS1DeconvolutionPatternTriple(MS1DeconvolutionPattern.BCD, ModelChromatogramVector, ms1Chromatograms);
		}

		double[][] invMatrix = MatrixOperations.matrixInverse(luMatrix);

		List<List<Peak>> deconvolutedPeaklistList = new ArrayList<>();
		List<List<Peak>> originalChromatograms = new ArrayList<>();

		for(List<Peak> chrom : ms1Chromatograms) {
			for (int i = 0; i < chrom.size(); i++)
			    expIntArray[i] = chrom.get(i).intensity;

			double z_t = BasicMathematics.innerProduct(tModelArray, expIntArray);
			double z_a = BasicMathematics.innerProduct(aModelArray, expIntArray);
			double z_b = BasicMathematics.innerProduct(bModelArray, expIntArray);
			double z_c = BasicMathematics.innerProduct(cModelArray, expIntArray);
			double z_d = BasicMathematics.innerProduct(dModelArray, expIntArray);
			double z_la = BasicMathematics.innerProduct(linearArray, expIntArray);
			double z_ca = BasicMathematics.innerProduct(constArray, expIntArray);

			double coefficient = invMatrix[0][0] * z_t + invMatrix[0][1] * z_a + invMatrix[0][2] * z_b + invMatrix[0][3] * z_c +
                    invMatrix[0][4] * z_d + invMatrix[0][5] * z_la + invMatrix[0][6] * z_ca;

			if (coefficient <= 0)
				if (!tryGetAdhocCoefficient(expIntArray, ModelChromatogramVector.targetScanTopInModelChromatogramVector))
					continue;

			List<Peak> dPeaklist = getDeconvolutedPeaklist(coefficient, ModelChromatogramVector, chrom.get(ModelChromatogramVector.targetScanTopInModelChromatogramVector).mz, expIntArray);

			if (dPeaklist != null) {
				originalChromatograms.add(chrom);
				deconvolutedPeaklistList.add(dPeaklist);
			}
		}
		return MS1DeconvolutionResultProperty(ModelChromatogramVector, deconvolutedPeaklistList, originalChromatograms);
	}


	private static boolean tryGetAdhocCoefficient(double[] expIntArray, int peakTop) {
        return (peakTop >= 4 && peakTop <= expIntArray.length - 5) &&
               expIntArray[peakTop - 1] > expIntArray[peakTop - 2] && expIntArray[peakTop - 2] > expIntArray[peakTop - 3] &&
               expIntArray[peakTop - 3] > expIntArray[peakTop - 4] && expIntArray[peakTop + 1] > expIntArray[peakTop + 2] &&
               expIntArray[peakTop + 2] > expIntArray[peakTop + 3] && expIntArray[peakTop + 3] > expIntArray[peakTop + 4];
    }

	private static List<Peak> getDeconvolutedPeaklist(double coefficient, ModelChromatogramVector ModelChromatogramVector, double targetMz, double[] expIntArray) {
		int targetLeft = ModelChromatogramVector.targetScanLeftInModelChromatogramVector;
		int targetRight = ModelChromatogramVector.targetScanRightInModelChromatogramVector;
		int targetTop = ModelChromatogramVector.targetScanTopInModelChromatogramVector;

		if (expIntArray[targetTop] < coefficient * ModelChromatogramVector.targetIntensityArray.get(targetTop))
			coefficient = expIntArray[targetTop] / ModelChromatogramVector.targetIntensityArray.get(targetTop);

		List<Peak> dPeaklist = new ArrayList<>();

		for (int i = targetLeft; i <= targetRight; i++) {
			double intensity = coefficient * ModelChromatogramVector.targetIntensityArray.get(i);

			if (i == targetTop && intensity <= 0)
			    return null;

			dPeaklist.add(new Peak(ModelChromatogramVector.rawScanList.get(i), ModelChromatogramVector.retentionTimeList.get(i), targetMz, intensity));
		}

		return dPeaklist;
	}

	private static MS1DeconvolutionResult MS1DeconvolutionResultProperty(ModelChromatogramVector ModelChromatogramVector, List<List<Peak>> deconvolutedPeaklistList, List<List<Peak>> ms1Chromatograms) {
		if (deconvolutedPeaklistList == null || deconvolutedPeaklistList.size() == 0) return null;

		int peakTopOfDeconvolutedChrom = ModelChromatogramVector.targetScanTopInModelChromatogramVector - ModelChromatogramVector.targetScanLeftInModelChromatogramVector;
		int peakTopOfOriginalChrom = ModelChromatogramVector.targetScanTopInModelChromatogramVector;

		MS1DeconvolutionResult ms1DeconvolutionResult = new MS1DeconvolutionResult();
		ms1DeconvolutionResult.scanNumber = ModelChromatogramVector.rawScanList.get(ModelChromatogramVector.targetScanTopInModelChromatogramVector);
		ms1DeconvolutionResult.retentionTime = ModelChromatogramVector.retentionTimeList.get(ModelChromatogramVector.targetScanTopInModelChromatogramVector);

		double sumArea = 0;
		double sumHeight = 0;
		double minModelMzDiff = Double.MAX_VALUE;
		int modelMzID = -1;

		for (int i = 0; i < deconvolutedPeaklistList.size(); i++) {
			List<Peak> chromatogram = deconvolutedPeaklistList.get(i);

			if (Math.abs(chromatogram.get(peakTopOfDeconvolutedChrom).mz - ModelChromatogramVector.modelMasses.get(0)) < minModelMzDiff) {
				minModelMzDiff = Math.abs(chromatogram.get(peakTopOfDeconvolutedChrom).mz - ModelChromatogramVector.modelMasses.get(0));
				modelMzID = i;
			}

			for (int j = 0; j < chromatogram.size(); j++) {
				if (j == peakTopOfDeconvolutedChrom) {
					ms1DeconvolutionResult.spectrum.add(chromatogram.get(j));
					sumHeight += chromatogram.get(j).intensity;

					if (chromatogram.get(j).intensity > ms1Chromatograms.get(i).get(peakTopOfOriginalChrom).intensity) {
						ms1DeconvolutionResult.spectrum.get(ms1DeconvolutionResult.spectrum.size() - 1).peakQuality = PeakQuality.SATURATED;
					}
				}

				if (j < chromatogram.size() - 1) {
                    sumArea += (chromatogram.get(j).intensity + chromatogram.get(j + 1).intensity) * (chromatogram.get(j + 1).retentionTime - chromatogram.get(j).retentionTime) * 0.5;
                }
			}
		}

		ms1DeconvolutionResult.integratedArea = sumArea * 60;
		ms1DeconvolutionResult.integratedHeight = sumHeight;

		List<Peak> quantChromatogram = deconvolutedPeaklistList.get(modelMzID);

		ms1DeconvolutionResult.basepeakMz = quantChromatogram.get(peakTopOfDeconvolutedChrom).mz;
		ms1DeconvolutionResult.basepeakHeight = quantChromatogram.get(peakTopOfDeconvolutedChrom).intensity;
		ms1DeconvolutionResult.baseChromatogram = quantChromatogram;

		sumArea = 0;

		for (int i = 0; i < quantChromatogram.size() - 1; i++) {
			sumArea += (quantChromatogram.get(i).intensity + quantChromatogram.get(i + 1).intensity)
					* (quantChromatogram.get(i + 1).retentionTime - quantChromatogram.get(i).retentionTime) * 0.5;
		}

		ms1DeconvolutionResult.basepeakArea = sumArea * 60;
		ms1DeconvolutionResult.spectrum = ms1DeconvolutionResult.spectrum.stream().sorted(Comparator.comparing(Peak::mz)).collect(Collectors.toList());
		ms1DeconvolutionResult.modelMasses = ModelChromatogramVector.modelMasses;

		return ms1DeconvolutionResult;
	}
}
