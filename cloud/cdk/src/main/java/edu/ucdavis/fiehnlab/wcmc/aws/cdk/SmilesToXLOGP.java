package edu.ucdavis.fiehnlab.wcmc.aws.cdk;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.descriptors.molecular.XLogPDescriptor;
import org.openscience.cdk.qsar.result.DoubleResult;
import org.openscience.cdk.smiles.SmilesParser;

public class SmilesToXLOGP implements RequestHandler<String, Double> {
    SmilesParser parser = new SmilesParser(DefaultChemObjectBuilder.getInstance());
    XLogPDescriptor descriptor = new XLogPDescriptor();


    @Override
    public Double handleRequest(String smile, Context context) {

        try {
            IAtomContainer container = parser.parseSmiles(smile);
            DescriptorValue value = descriptor.calculate(container);
            return ((DoubleResult) value.getValue()).doubleValue();

        } catch (InvalidSmilesException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
