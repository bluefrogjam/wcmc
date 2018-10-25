package edu.ucdavis.fiehnlab.wcmc.aws.cdk;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.fingerprint.IBitFingerprint;
import org.openscience.cdk.fingerprint.PubchemFingerprinter;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.DefaultChemObjectReader;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.result.DoubleResult;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * converts a received smile code to a fingerprint
 */
public class SmilesToFingerPrint extends SmilesToHandler {
    SmilesParser parser = new SmilesParser(DefaultChemObjectBuilder.getInstance());

    @Override
    protected Map<String, Object> handleSmile(String smile) throws CDKException {
        IAtomContainer container = parser.parseSmiles(smile);
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);

        PubchemFingerprinter printer = new PubchemFingerprinter(DefaultChemObjectBuilder.getInstance());
        IBitFingerprint fingerprint = printer.getBitFingerprint(container);



        Map<String, Object> result = new HashMap<>();
        result.put("fingerPubchemPrint", fingerprint.getSetbits());

        return result;
    }
}
