package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.deconvolution;

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.math.BasicMathematics;
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.math.MatrixOperationHelper;
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by diego on 8/12/2016.
 */
public class MS1Dec {
	public static MS1DecResult getMs1DecResult(ModelChromVector modelChromVector, List<List<MsDialPeak>> ms1Chromatograms) {

		switch (modelChromVector.ms1DecPattern) {
			case C:
				return ms1DecPatternSingle(modelChromVector, ms1Chromatograms);
			case BC:
				return ms1DecPatternDouble(modelChromVector.ms1DecPattern, modelChromVector, ms1Chromatograms);
			case CD:
				return ms1DecPatternDouble(modelChromVector.ms1DecPattern, modelChromVector, ms1Chromatograms);
			case ABC:
				return ms1DecPatternTriple(modelChromVector.ms1DecPattern, modelChromVector, ms1Chromatograms);
			case BCD:
				return ms1DecPatternTriple(modelChromVector.ms1DecPattern, modelChromVector, ms1Chromatograms);
			case CDE:
				return ms1DecPatternTriple(modelChromVector.ms1DecPattern, modelChromVector, ms1Chromatograms);
			case ABCD:
				return ms1DecPatternQuadruple(modelChromVector.ms1DecPattern, modelChromVector, ms1Chromatograms);
			case BCDE:
				return ms1DecPatternQuadruple(modelChromVector.ms1DecPattern, modelChromVector, ms1Chromatograms);
			case ABCDE:
				return ms1DecPatternQuintuple(modelChromVector, ms1Chromatograms);
		}
		return null;
	}

	/**
	 * @param modelChromVector
	 * @param ms1Chromatograms
	 * @return
	 */
	private static MS1DecResult ms1DecPatternSingle(ModelChromVector modelChromVector, List<List<MsDialPeak>> ms1Chromatograms) {
		double[] constArray = new double[modelChromVector.chromScanList.size()];
		double[] linearArray = new double[modelChromVector.chromScanList.size()];
		double[] tModelArray = new double[modelChromVector.chromScanList.size()];
		double[] expIntArray = new double[modelChromVector.chromScanList.size()];

		//initialize
		for (int i = 0; i < modelChromVector.chromScanList.size(); i++) {
			constArray[i] = 1;
			linearArray[i] = i;
			tModelArray[i] = modelChromVector.targetIntensityArray.get(i);
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

		LUMatrix luMatrix = MatrixOperationHelper.matrixDecompose(matrix);
		if (luMatrix == null) {
			System.out.println("LU Matrix null (pattern single)");
			return null;
		}

		double detA = MatrixOperationHelper.determinantA(luMatrix);
		if (detA == 0) {
			System.out.println("Det A zero (pattern single)");
			return null;
		}
		double[][] invMatrix = MatrixOperationHelper.matrixInverse(luMatrix);

		List<List<MsDialPeak>> deconvolutedPeaklistList = new ArrayList<>();
		List<List<MsDialPeak>> originalChromatograms = new ArrayList<>();

		for (List<MsDialPeak> chrom : ms1Chromatograms) {
			for (int i = 0; i < chrom.size(); i++) expIntArray[i] = chrom.get(i).intensity;
			double z_t = BasicMathematics.innerProduct(tModelArray, expIntArray);
			double z_la = BasicMathematics.innerProduct(linearArray, expIntArray);
			double z_ca = BasicMathematics.innerProduct(constArray, expIntArray);

			Double coefficient = invMatrix[0][0] * z_t + invMatrix[0][1] * z_la + invMatrix[0][2] * z_ca;
			if (coefficient <= 0) {
				if (!tryGetAdhocCoefficient(expIntArray, tModelArray, modelChromVector.targetScanTopInModelChromVector, coefficient)) {
					continue;
				}
			}

			List<MsDialPeak> dPeaklist = getDeconvolutedPeaklist(coefficient, modelChromVector, chrom.get(modelChromVector.targetScanTopInModelChromVector).mass, expIntArray);

			if (dPeaklist != null) {
				originalChromatograms.add(chrom);
				deconvolutedPeaklistList.add(dPeaklist);
			}
		}

		return ms1DecResultProperty(modelChromVector, deconvolutedPeaklistList, originalChromatograms);
	}

	private static MS1DecResult ms1DecPatternDouble(Ms1DecPattern pattern, ModelChromVector modelChromVector, List<List<MsDialPeak>> ms1Chromatograms) {
		double[] constArray = new double[modelChromVector.chromScanList.size()];
		double[] linearArray = new double[modelChromVector.chromScanList.size()];
		double[] tModelArray = new double[modelChromVector.chromScanList.size()];
		double[] aModelArray = new double[modelChromVector.chromScanList.size()];
		double[] expIntArray = new double[modelChromVector.chromScanList.size()];

		//initialize
		for (int i = 0; i < modelChromVector.chromScanList.size(); i++) {
			constArray[i] = 1;
			linearArray[i] = i;
			tModelArray[i] = modelChromVector.targetIntensityArray.get(i);
			if (pattern == Ms1DecPattern.BC)
				aModelArray[i] = modelChromVector.oneLeftIntensityArray.get(i);
			else if (pattern == Ms1DecPattern.CD)
				aModelArray[i] = modelChromVector.oneRightIntensityArray.get(i);
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

		LUMatrix luMatrix = MatrixOperationHelper.matrixDecompose(matrix);
		if (luMatrix == null) {
			System.out.println("LU Matrix null (pattern double)");
			return ms1DecPatternSingle(modelChromVector, ms1Chromatograms);
		}

		double detA = MatrixOperationHelper.determinantA(luMatrix);
		if (detA == 0) {
			System.out.println("Det A zero (pattern double)");
			return ms1DecPatternSingle(modelChromVector, ms1Chromatograms);
		}

		double[][] invMatrix = MatrixOperationHelper.matrixInverse(luMatrix);

		List<List<MsDialPeak>> deconvolutedPeaklistList = new ArrayList<>();
		List<List<MsDialPeak>> originalChromatograms = new ArrayList<>();
		for (List<MsDialPeak> chrom : ms1Chromatograms) {
			for (int i = 0; i < chrom.size(); i++) expIntArray[i] = chrom.get(i).intensity();
			double z_t = BasicMathematics.innerProduct(tModelArray, expIntArray);
			double z_a = BasicMathematics.innerProduct(aModelArray, expIntArray);
			double z_la = BasicMathematics.innerProduct(linearArray, expIntArray);
			double z_ca = BasicMathematics.innerProduct(constArray, expIntArray);

			Double coefficient = invMatrix[0][0] * z_t + invMatrix[0][1] * z_a + invMatrix[0][2] * z_la + invMatrix[0][3] * z_ca;
			if (coefficient <= 0)
				if (!tryGetAdhocCoefficient(expIntArray, tModelArray, modelChromVector.targetScanTopInModelChromVector, coefficient))
					continue;
			List<MsDialPeak> dPeaklist = getDeconvolutedPeaklist(coefficient, modelChromVector, chrom.get(modelChromVector.targetScanTopInModelChromVector).mass, expIntArray);

			if (dPeaklist != null) {
				originalChromatograms.add(chrom);
				deconvolutedPeaklistList.add(dPeaklist);
			}
		}
		return ms1DecResultProperty(modelChromVector, deconvolutedPeaklistList, originalChromatograms);
	}

	private static MS1DecResult ms1DecPatternTriple(Ms1DecPattern pattern, ModelChromVector modelChromVector, List<List<MsDialPeak>> ms1Chromatograms) {
		double[] constArray = new double[modelChromVector.chromScanList.size()];
		double[] linearArray = new double[modelChromVector.chromScanList.size()];
		double[] tModelArray = new double[modelChromVector.chromScanList.size()];
		double[] aModelArray = new double[modelChromVector.chromScanList.size()];
		double[] bModelArray = new double[modelChromVector.chromScanList.size()];
		double[] expIntArray = new double[modelChromVector.chromScanList.size()];

		//initialize
		for (int i = 0; i < modelChromVector.chromScanList.size(); i++) {
			constArray[i] = 1;
			linearArray[i] = i;
			tModelArray[i] = modelChromVector.targetIntensityArray.get(i);
			if (pattern == Ms1DecPattern.ABC) {
				aModelArray[i] = modelChromVector.twoLeftIntensityArray.get(i);
				bModelArray[i] = modelChromVector.oneLeftIntensityArray.get(i);
			} else if (pattern == Ms1DecPattern.BCD) {
				aModelArray[i] = modelChromVector.oneLeftIntensityArray.get(i);
				bModelArray[i] = modelChromVector.oneRightIntensityArray.get(i);
			} else if (pattern == Ms1DecPattern.CDE) {
				aModelArray[i] = modelChromVector.oneRightIntensityArray.get(i);
				bModelArray[i] = modelChromVector.twoRightInetnsityArray.get(i);
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

		LUMatrix luMatrix = MatrixOperationHelper.matrixDecompose(matrix);
		if (luMatrix == null) {
			System.out.println("LU Matrix null (pattern triple)");
			if (pattern == Ms1DecPattern.ABC)
				return ms1DecPatternDouble(Ms1DecPattern.BC, modelChromVector, ms1Chromatograms);
			else
				return ms1DecPatternDouble(Ms1DecPattern.CD, modelChromVector, ms1Chromatograms);
		}

		double detA = MatrixOperationHelper.determinantA(luMatrix);
		if (detA == 0) {
			System.out.println("Det A zero (pattern triple)");
			if (pattern == Ms1DecPattern.ABC)
				return ms1DecPatternDouble(Ms1DecPattern.BC, modelChromVector, ms1Chromatograms);
			else
				return ms1DecPatternDouble(Ms1DecPattern.CD, modelChromVector, ms1Chromatograms);
		}
		double[][] invMatrix = MatrixOperationHelper.matrixInverse(luMatrix);

		List<List<MsDialPeak>> deconvolutedPeaklistList = new ArrayList<>();
		List<List<MsDialPeak>> originalChromatograms = new ArrayList<>();
		for (List<MsDialPeak> chrom : ms1Chromatograms) {
			for (int i = 0; i < chrom.size(); i++) expIntArray[i] = chrom.get(i).intensity;
			double z_t = BasicMathematics.innerProduct(tModelArray, expIntArray);
			double z_a = BasicMathematics.innerProduct(aModelArray, expIntArray);
			double z_b = BasicMathematics.innerProduct(bModelArray, expIntArray);
			double z_la = BasicMathematics.innerProduct(linearArray, expIntArray);
			double z_ca = BasicMathematics.innerProduct(constArray, expIntArray);

			double coefficient = invMatrix[0][0] * z_t + invMatrix[0][1] * z_a + invMatrix[0][2] * z_b + invMatrix[0][3] * z_la + invMatrix[0][4] * z_ca;
			if (coefficient <= 0)
				if (!tryGetAdhocCoefficient(expIntArray, tModelArray, modelChromVector.targetScanTopInModelChromVector, coefficient))
					continue;
			List<MsDialPeak> dPeaklist = getDeconvolutedPeaklist(coefficient, modelChromVector, chrom.get(modelChromVector.targetScanTopInModelChromVector).mass, expIntArray);

			if (dPeaklist != null) {
				originalChromatograms.add(chrom);
				deconvolutedPeaklistList.add(dPeaklist);
			}
		}
		return ms1DecResultProperty(modelChromVector, deconvolutedPeaklistList, originalChromatograms);
	}

	private static MS1DecResult ms1DecPatternQuadruple(Ms1DecPattern pattern, ModelChromVector modelChromVector, List<List<MsDialPeak>> ms1Chromatograms) {
		double[] constArray = new double[modelChromVector.chromScanList.size()];
		double[] linearArray = new double[modelChromVector.chromScanList.size()];
		double[] tModelArray = new double[modelChromVector.chromScanList.size()];
		double[] aModelArray = new double[modelChromVector.chromScanList.size()];
		double[] bModelArray = new double[modelChromVector.chromScanList.size()];
		double[] cModelArray = new double[modelChromVector.chromScanList.size()];
		double[] expIntArray = new double[modelChromVector.chromScanList.size()];

		//initialize
		for (int i = 0; i < modelChromVector.chromScanList.size(); i++) {
			constArray[i] = 1;
			linearArray[i] = i;
			tModelArray[i] = modelChromVector.targetIntensityArray.get(i);
			if (pattern == Ms1DecPattern.ABCD) {
				aModelArray[i] = modelChromVector.twoLeftIntensityArray.get(i);
				bModelArray[i] = modelChromVector.oneLeftIntensityArray.get(i);
				cModelArray[i] = modelChromVector.oneRightIntensityArray.get(i);
			} else if (pattern == Ms1DecPattern.BCDE) {
				aModelArray[i] = modelChromVector.oneLeftIntensityArray.get(i);
				bModelArray[i] = modelChromVector.oneRightIntensityArray.get(i);
				cModelArray[i] = modelChromVector.twoRightInetnsityArray.get(i);
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

		LUMatrix luMatrix = MatrixOperationHelper.matrixDecompose(matrix);
		if (luMatrix == null) {
			System.out.println("LU Matrix null (pattern quadruple)");
			return ms1DecPatternTriple(Ms1DecPattern.BCD, modelChromVector, ms1Chromatograms);
		}

		double detA = MatrixOperationHelper.determinantA(luMatrix);
		if (detA == 0) {
			System.out.println("Det A zero (pattern quadruple)");
			return ms1DecPatternTriple(Ms1DecPattern.BCD, modelChromVector, ms1Chromatograms);
		}

		double[][] invMatrix = MatrixOperationHelper.matrixInverse(luMatrix);

		List<List<MsDialPeak>> deconvolutedPeaklistList = new ArrayList<>();
		List<List<MsDialPeak>> originalChromatograms = new ArrayList<>();
		for (List<MsDialPeak> chrom : ms1Chromatograms) {
			for (int i = 0; i < chrom.size(); i++) expIntArray[i] = chrom.get(i).intensity;
			double z_t = BasicMathematics.innerProduct(tModelArray, expIntArray);
			double z_a = BasicMathematics.innerProduct(aModelArray, expIntArray);
			double z_b = BasicMathematics.innerProduct(bModelArray, expIntArray);
			double z_c = BasicMathematics.innerProduct(cModelArray, expIntArray);
			double z_la = BasicMathematics.innerProduct(linearArray, expIntArray);
			double z_ca = BasicMathematics.innerProduct(constArray, expIntArray);

			double coefficient = invMatrix[0][0] * z_t + invMatrix[0][1] * z_a + invMatrix[0][2] * z_b + invMatrix[0][3] *
					z_c + invMatrix[0][4] * z_la + invMatrix[0][5] * z_ca;
			if (coefficient <= 0)
				if (!tryGetAdhocCoefficient(expIntArray, tModelArray, modelChromVector.targetScanTopInModelChromVector, coefficient))
					continue;
			List<MsDialPeak> dPeaklist = getDeconvolutedPeaklist(coefficient, modelChromVector, chrom.get(modelChromVector.targetScanTopInModelChromVector).mass, expIntArray);

			if (dPeaklist != null) {
				originalChromatograms.add(chrom);
				deconvolutedPeaklistList.add(dPeaklist);
			}
		}
		return ms1DecResultProperty(modelChromVector, deconvolutedPeaklistList, originalChromatograms);
	}

	private static MS1DecResult ms1DecPatternQuintuple(ModelChromVector modelChromVector, List<List<MsDialPeak>> ms1Chromatograms) {
		double[] constArray = new double[modelChromVector.chromScanList.size()];
		double[] linearArray = new double[modelChromVector.chromScanList.size()];
		double[] tModelArray = new double[modelChromVector.chromScanList.size()];
		double[] aModelArray = new double[modelChromVector.chromScanList.size()];
		double[] bModelArray = new double[modelChromVector.chromScanList.size()];
		double[] cModelArray = new double[modelChromVector.chromScanList.size()];
		double[] dModelArray = new double[modelChromVector.chromScanList.size()];
		double[] expIntArray = new double[modelChromVector.chromScanList.size()];

		//initialize
		for (int i = 0; i < modelChromVector.chromScanList.size(); i++) {
			constArray[i] = 1;
			linearArray[i] = i;
			tModelArray[i] = modelChromVector.targetIntensityArray.get(i);
			aModelArray[i] = modelChromVector.twoLeftIntensityArray.get(i);
			bModelArray[i] = modelChromVector.oneLeftIntensityArray.get(i);
			cModelArray[i] = modelChromVector.oneRightIntensityArray.get(i);
			dModelArray[i] = modelChromVector.twoRightInetnsityArray.get(i);
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

		LUMatrix luMatrix = MatrixOperationHelper.matrixDecompose(matrix);
		if (luMatrix == null) {
			System.out.println("LU Matrix null (pattern quintuple)");
			return ms1DecPatternTriple(Ms1DecPattern.BCD, modelChromVector, ms1Chromatograms);
		}

		double detA = MatrixOperationHelper.determinantA(luMatrix);
		if (detA == 0) {
			System.out.println("Det A zero (pattern quintuple)");
			return ms1DecPatternTriple(Ms1DecPattern.BCD, modelChromVector, ms1Chromatograms);
		}

		double[][] invMatrix = MatrixOperationHelper.matrixInverse(luMatrix);

		List<List<MsDialPeak>> deconvolutedPeaklistList = new ArrayList<>();
		List<List<MsDialPeak>> originalChromatograms = new ArrayList<>();
		for(List<MsDialPeak> chrom : ms1Chromatograms) {
			for (int i = 0; i < chrom.size(); i++) expIntArray[i] = chrom.get(i).intensity;
			double z_t = BasicMathematics.innerProduct(tModelArray, expIntArray);
			double z_a = BasicMathematics.innerProduct(aModelArray, expIntArray);
			double z_b = BasicMathematics.innerProduct(bModelArray, expIntArray);
			double z_c = BasicMathematics.innerProduct(cModelArray, expIntArray);
			double z_d = BasicMathematics.innerProduct(dModelArray, expIntArray);
			double z_la = BasicMathematics.innerProduct(linearArray, expIntArray);
			double z_ca = BasicMathematics.innerProduct(constArray, expIntArray);

			double coefficient = invMatrix[0][0]*z_t + invMatrix[0][1]*z_a + invMatrix[0][2]*z_b + invMatrix[0][3]*
			z_c + invMatrix[0][4]*z_d + invMatrix[0][5]*z_la + invMatrix[0][6]*z_ca;
			if (coefficient <= 0)
				if (!tryGetAdhocCoefficient(expIntArray, tModelArray, modelChromVector.targetScanTopInModelChromVector, coefficient))
					continue;
			List<MsDialPeak> dPeaklist = getDeconvolutedPeaklist(coefficient, modelChromVector, chrom.get(modelChromVector.targetScanTopInModelChromVector).mass, expIntArray);

			if (dPeaklist != null) {
				originalChromatograms.add(chrom);
				deconvolutedPeaklistList.add(dPeaklist);
			}
		}
		return ms1DecResultProperty(modelChromVector, deconvolutedPeaklistList, originalChromatograms);
	}


	private static boolean tryGetAdhocCoefficient(double[] expIntArray, double[] tModelArray, int peakTop, Double coefficient) {
		coefficient = -1.0;

		if (peakTop < 4) return false;
		if (peakTop > expIntArray.length - 5) return false;

		if (expIntArray[peakTop - 1] > expIntArray[peakTop - 2] && expIntArray[peakTop - 2] > expIntArray[peakTop - 3] && expIntArray[peakTop - 3] > expIntArray[peakTop - 4] &&
				expIntArray[peakTop + 1] > expIntArray[peakTop + 2] && expIntArray[peakTop + 2] > expIntArray[peakTop + 3] && expIntArray[peakTop + 3] > expIntArray[peakTop + 4]) {
			coefficient = expIntArray[peakTop] / tModelArray[peakTop];
			return true;
		} else
			return false;
	}

	private static List<MsDialPeak> getDeconvolutedPeaklist(double coefficient, ModelChromVector modelChromVector, double targetMz, double[] expIntArray) {
		int targetLeft = modelChromVector.targetScanLeftInModelChromVector;
		int targetRight = modelChromVector.targetScanRightInModelChromVector;
		int targetTop = modelChromVector.targetScanTopInModelChromVector;

		if (expIntArray[targetTop] < coefficient * modelChromVector.targetIntensityArray.get(targetTop))
			coefficient = expIntArray[targetTop] / modelChromVector.targetIntensityArray.get(targetTop);

		List<MsDialPeak> dPeaklist = new ArrayList<>();
		for (int i = targetLeft; i <= targetRight; i++) {
			double intensity = coefficient * modelChromVector.targetIntensityArray.get(i);
			if (i == targetTop && intensity <= 0) return null;

			dPeaklist.add(new MsDialPeak(modelChromVector.rawScanList.get(i), modelChromVector.rtArray.get(i), targetMz, intensity));
		}
		return dPeaklist;
	}

	private static MS1DecResult ms1DecResultProperty(ModelChromVector modelChromVector, List<List<MsDialPeak>> deconvolutedPeaklistList, List<List<MsDialPeak>> ms1Chromatograms) {
		if (deconvolutedPeaklistList == null || deconvolutedPeaklistList.size() == 0) return null;

		int peakTopOfDeconvolutedChrom = modelChromVector.targetScanTopInModelChromVector - modelChromVector.targetScanLeftInModelChromVector;
		int peakTopOfOriginalChrom = modelChromVector.targetScanTopInModelChromVector;
		MS1DecResult ms1DecResult = new MS1DecResult();

		ms1DecResult.scanNumber = modelChromVector.rawScanList.get(modelChromVector.targetScanTopInModelChromVector);
		ms1DecResult.retentionTime = modelChromVector.rtArray.get(modelChromVector.targetScanTopInModelChromVector);

		double sumArea = 0;
		double sumHeight = 0;
		double minModelMzDiff = Double.MAX_VALUE;
		int modelMzID = -1;

		for (int i = 0; i < deconvolutedPeaklistList.size(); i++) {
			List<MsDialPeak> chromatogram = deconvolutedPeaklistList.get(i);
			if (Math.abs(chromatogram.get(peakTopOfDeconvolutedChrom).mass - modelChromVector.modelMasses.get(0)) < minModelMzDiff) {
				minModelMzDiff = Math.abs(chromatogram.get(peakTopOfDeconvolutedChrom).mass - modelChromVector.modelMasses.get(0));
				modelMzID = i;
			}

			for (int j = 0; j < chromatogram.size(); j++) {
				if (j == peakTopOfDeconvolutedChrom) {
					ms1DecResult.spectrum.add(chromatogram.get(j));
					sumHeight += chromatogram.get(j).intensity;

					if (chromatogram.get(j).intensity > ms1Chromatograms.get(i).get(peakTopOfOriginalChrom).intensity) {
						ms1DecResult.spectrum.get(ms1DecResult.spectrum.size() - 1).peakQuality = PeakQuality.SATURATED;
					}
				}
				if (j < chromatogram.size() - 1)
					sumArea += (chromatogram.get(j).intensity + chromatogram.get(j + 1).intensity) * (chromatogram.get(j + 1).rtMin - chromatogram.get(j).rtMin) * 0.5;
			}
		}

		ms1DecResult.integratedArea = sumArea * 60;
		ms1DecResult.integratedHeight = sumHeight;

		List<MsDialPeak> quantChromatogram = deconvolutedPeaklistList.get(modelMzID);
		ms1DecResult.basepeakMz = quantChromatogram.get(peakTopOfDeconvolutedChrom).mass;
		ms1DecResult.basepeakHeight = quantChromatogram.get(peakTopOfDeconvolutedChrom).intensity;
		ms1DecResult.baseChromatogram = quantChromatogram;
		sumArea = 0;
		for (int i = 0; i < quantChromatogram.size() - 1; i++) {
			sumArea += (quantChromatogram.get(i).intensity + quantChromatogram.get(i + 1).intensity)
					* (quantChromatogram.get(i + 1).rtMin - quantChromatogram.get(i).rtMin) * 0.5;
		}
		ms1DecResult.basepeakArea = sumArea * 60;

		ms1DecResult.spectrum = ms1DecResult.spectrum.stream().sorted(Comparator.comparing(MsDialPeak::mass)).collect(Collectors.toList());
		ms1DecResult.modelMasses = modelChromVector.modelMasses;

		return ms1DecResult;
	}
}
