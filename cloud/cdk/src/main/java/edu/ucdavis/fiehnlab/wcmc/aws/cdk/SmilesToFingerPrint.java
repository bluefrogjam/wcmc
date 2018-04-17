package edu.ucdavis.fiehnlab.wcmc.aws.cdk;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.openscience.cdk.DefaultChemObjectBuilder;
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
public class SmilesToFingerPrint extends ApiGatewayHandler {
    SmilesParser parser = new SmilesParser(DefaultChemObjectBuilder.getInstance());

    protected Map<String, Object> evaluateRequest(Map<String, Object> request) {
        try {
            PubchemFingerprinter printer = new PubchemFingerprinter(DefaultChemObjectBuilder.getInstance());

            String smile = request.get("body").toString();

            IAtomContainer container = parser.parseSmiles(smile);
            AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
            IBitFingerprint fingerprint = printer.getBitFingerprint(container);



            Map<String, Object> result = new HashMap<>();
            result.put("result", fingerprint.getSetbits());
            System.out.println(fingerprint.size());


            return result;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
