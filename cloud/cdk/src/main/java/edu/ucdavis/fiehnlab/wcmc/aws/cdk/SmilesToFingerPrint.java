package edu.ucdavis.fiehnlab.wcmc.aws.cdk;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.fingerprint.PubchemFingerprinter;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.DefaultChemObjectReader;
import org.openscience.cdk.smiles.SmilesParser;

/**
 * converts a received smile code to a fingerprint
 */
public class SmilesToFingerPrint implements RequestHandler<String, byte[]> {
    SmilesParser parser = new SmilesParser(DefaultChemObjectBuilder.getInstance());
    PubchemFingerprinter printer = new PubchemFingerprinter(DefaultChemObjectBuilder.getInstance());

    @Override
    public byte[] handleRequest(String smile, Context context) {

        try {
            IAtomContainer container = parser.parseSmiles(smile);
            return printer.getFingerprintAsBytes();

        } catch (InvalidSmilesException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
