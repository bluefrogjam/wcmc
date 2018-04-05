package edu.ucdavis.genomics.metabolomics.exception;

public class CommunicationExcpetion extends BinBaseException {

    /**
     *
     */
    private static final long serialVersionUID = 2L;

    public CommunicationExcpetion() {
        super();
    }

    public CommunicationExcpetion(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public CommunicationExcpetion(String arg0) {
        super(arg0);
    }

    public CommunicationExcpetion(Throwable arg0) {
        super(arg0);
    }

}
