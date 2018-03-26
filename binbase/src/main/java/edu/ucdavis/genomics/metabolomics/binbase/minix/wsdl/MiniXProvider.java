package edu.ucdavis.genomics.metabolomics.binbase.minix.wsdl;

import edu.ucdavis.genomics.metabolomics.binbase.bci.Configurator;
import edu.ucdavis.genomics.metabolomics.binbase.bci.setupX.SetupXProvider;
import edu.ucdavis.genomics.metabolomics.binbase.minix.jmx.MiniXConfigurationJMXMBean;
import edu.ucdavis.genomics.metabolomics.exception.BinBaseException;
import minix.CommunicationsServiceStub;
import minix.CommunicationsServiceStub.*;
import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axis2.AxisFault;
import org.slf4j.Logger;

import javax.activation.DataHandler;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

/**
 * @author wohlgemuth
 */
public class MiniXProvider implements SetupXProvider {

    CommunicationsServiceStub stub;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public MiniXProvider() {
        try {

            ObjectName name = new ObjectName(
                    "binbase.miniX:service=MiniXConfigurationJMX");

            logger.warn("create a server");
            MBeanServer server = MBeanServerFactory.findMBeanServer(null).get(0);

            logger.warn("connect to jmx bean");
            MiniXConfigurationJMXMBean bean = MBeanServerInvocationHandler
                    .newProxyInstance(server, name,
                            MiniXConfigurationJMXMBean.class, false);

            logger.warn("create stub");
            this.intialize(bean.getUrl());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public MiniXProvider(String url) {
        this.intialize(url);
    }

    public void intialize(String url) {

        logger.debug("initialize miniX communications for url: " + url);
        try {
            stub = new CommunicationsServiceStub(url);
            stub._getServiceClient()
                    .getOptions()
                    .setProperty(
                            org.apache.axis2.transport.http.HTTPConstants.SO_TIMEOUT,
                            6000000);
            stub._getServiceClient()
                    .getOptions()
                    .setProperty(
                            org.apache.axis2.transport.http.HTTPConstants.CONNECTION_TIMEOUT,
                            6000000);

        } catch (AxisFault e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String getOrgan(String sample) throws BinBaseException {
        logger.debug("query for organ for sample: " + sample);

        String value = "";
        try {
            GetOrganForSample name = new GetOrganForSample();
            name.setSample(sample);

            GetOrganForSampleE namE = new GetOrganForSampleE();
            namE.setGetOrganForSample(name);

            GetOrganForSampleResponseE resp = this.stub.getOrganForSample(namE);

            value = resp.getGetOrganForSampleResponse().get_return();

        } catch (Exception e) {
            logger.warn("error for sample: " + sample + ", message was: " + e.getMessage(), e);
            throw new BinBaseException(e);
        }

        logger.debug("received: " + value);
        return String.valueOf(value);
    }

    public String getLabel(String sample) throws BinBaseException {
        logger.debug("query for label for sample: " + sample);
        String value = "";
        try {
            GetLabelForSample name = new GetLabelForSample();
            name.setSample(sample);

            GetLabelForSampleE namE = new GetLabelForSampleE();
            namE.setGetLabelForSample(name);

            GetLabelForSampleResponseE resp = this.stub.getLabelForSample(namE);

            value = resp.getGetLabelForSampleResponse().get_return();

        } catch (Exception e) {
            logger.warn("error for sample: " + sample + ", message was: " + e.getMessage(), e);
            throw new BinBaseException(e);
        }

        logger.debug("received: " + value);
        return String.valueOf(value);
    }

    public String getComment(String sample) throws BinBaseException {
        logger.debug("query for comment for sample: " + sample);

        String value = "";
        try {
            GetCommentForSample name = new GetCommentForSample();
            name.setSample(sample);

            GetCommentForSampleE namE = new GetCommentForSampleE();
            namE.setGetCommentForSample(name);

            GetCommentForSampleResponseE resp = this.stub
                    .getCommentForSample(namE);

            value = resp.getGetCommentForSampleResponse().get_return();

        } catch (Exception e) {
            logger.warn("error for sample: " + sample + ", message was: " + e.getMessage(), e);
            throw new BinBaseException(e);
        }

        logger.debug("received: " + value);
        return String.valueOf(value);
    }

    public String getSpecies(String sample) throws BinBaseException {
        logger.debug("query for species for sample: " + sample);

        String value = "";
        try {
            GetSpeciesForSample name = new GetSpeciesForSample();
            name.setSample(sample);

            GetSpeciesForSampleE namE = new GetSpeciesForSampleE();
            namE.setGetSpeciesForSample(name);

            GetSpeciesForSampleResponseE resp = this.stub
                    .getSpeciesForSample(namE);

            value = resp.getGetSpeciesForSampleResponse().get_return();

        } catch (Exception e) {
            logger.warn("error for sample: " + sample + ", message was: " + e.getMessage(), e);
            throw new BinBaseException(e);
        }

        logger.debug("received: " + value);
        return String.valueOf(value);
    }

    public String getTreatment(String sample) throws BinBaseException {
        logger.debug("query for treatment for sample: " + sample);

        String value = "";
        try {
            GetTreatmentForSample name = new GetTreatmentForSample();
            name.setSample(sample);

            GetTreatmentForSampleE namE = new GetTreatmentForSampleE();
            namE.setGetTreatmentForSample(name);

            GetTreatmentForSampleResponseE resp = this.stub
                    .getTreatmentForSample(namE);

            value = resp.getGetTreatmentForSampleResponse().get_return();

        } catch (Exception e) {
            logger.warn("error for sample: " + sample + ", message was: " + e.getMessage(), e);
            throw new BinBaseException(e);
        }

        logger.debug("received: " + value);
        return String.valueOf(value);
    }

    public String getSetupXId(String sampleName) throws BinBaseException {

        logger.debug("query id for sample: " + sampleName);
        long value = 0;
        try {
            GetSampleIdForName name = new GetSampleIdForName();
            name.setFileName(sampleName);
            GetSampleIdForNameE namE = new GetSampleIdForNameE();
            namE.setGetSampleIdForName(name);

            GetSampleIdForNameResponseE resp = this.stub
                    .getSampleIdForName(namE);

            value = resp.getGetSampleIdForNameResponse().get_return();

        } catch (Exception e) {
            logger.warn("error for sample: " + sampleName + ", message was: " + e.getMessage(), e);
            throw new BinBaseException(e);
        }

        logger.debug("received: " + value);
        return String.valueOf(value);
    }

    public void upload(String experimentId, String content)
            throws BinBaseException {

        logger.info("uploading result for: " + experimentId + " content is: "
                + content);

        try {

            byte[] data = Configurator.getExportService().getResult(content);
            upload(experimentId, data);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if (e instanceof BinBaseException) {
                throw (BinBaseException) e;
            }
            throw new BinBaseException(e);
        }

        logger.debug("done...");
    }

    public void upload(String experimentId, byte[] data) throws BinBaseException {
        logger.info("received data size is: " + data.length + " bytes");

        UploadResult upload = new UploadResult();

        upload.setStudyId(Long.parseLong(experimentId));
        upload.setContent(new DataHandler(new ByteArrayDataSource(data)));

        UploadResultE uploadE = new UploadResultE();
        uploadE.setUploadResult(upload);

        // required for the upload to work
        stub._getServiceClient()
                .getOptions()
                .setProperty(
                        org.apache.axis2.transport.http.HTTPConstants.SO_TIMEOUT,
                        6000000);
        stub._getServiceClient()
                .getOptions()
                .setProperty(
                        org.apache.axis2.transport.http.HTTPConstants.CONNECTION_TIMEOUT,
                        6000000);

        try {
            UploadResultResponse res = this.stub.uploadResult(uploadE)
                    .getUploadResultResponse();

            logger.debug("result code is: " + res.get_return());

            if (res.get_return() == false) {
                throw new BinBaseException(
                        "sorry the waas an error during the upload...");
            }
        } catch (Exception e) {
            throw new BinBaseException(e);
        }
    }

    @Override
    public boolean canCreateBins(String setupxId) throws BinBaseException {

        logger.debug("checking if we can create a new bin for the given id: " + setupxId);
        long value = 0;
        try {
            CanGenerateNewBin generateNewBin = new CanGenerateNewBin();
            generateNewBin.setSampleId(Long.parseLong(setupxId));

            CanGenerateNewBinE generateNewBinE = new CanGenerateNewBinE();
            generateNewBinE.setCanGenerateNewBin(generateNewBin);


            CanGenerateNewBinResponseE responseE = this.stub.canGenerateNewBin(generateNewBinE);

            return responseE.getCanGenerateNewBinResponse().get_return();

        } catch (Exception e) {
            throw new BinBaseException(e);
        }
    }

}
