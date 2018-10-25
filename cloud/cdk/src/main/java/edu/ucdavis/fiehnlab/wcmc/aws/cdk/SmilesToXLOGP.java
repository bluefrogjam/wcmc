package edu.ucdavis.fiehnlab.wcmc.aws.cdk;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.descriptors.molecular.XLogPDescriptor;
import org.openscience.cdk.qsar.result.DoubleResult;
import org.openscience.cdk.smiles.SmilesParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SmilesToXLOGP extends SmilesToHandler {
    private SmilesParser parser = new SmilesParser(DefaultChemObjectBuilder.getInstance());
    private XLogPDescriptor descriptor = new XLogPDescriptor();

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected Map<String, Object> handleSmile(String smile) throws CDKException {
        IAtomContainer container = parser.parseSmiles(smile);
        DescriptorValue value = descriptor.calculate(container);
        Map<String, Object> result = new HashMap<>();

        result.put("xLogP", ((DoubleResult) value.getValue()).doubleValue());

        return result;
    }
}
