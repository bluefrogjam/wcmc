package edu.ucdavis.genomics.metabolomics.exception;

public class ObjectNotFoundException extends BinBaseException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ObjectNotFoundException() {
        super();
    }

    public ObjectNotFoundException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public ObjectNotFoundException(String arg0) {
        super(arg0);
    }

    public ObjectNotFoundException(Throwable arg0) {
        super(arg0);
    }

}
