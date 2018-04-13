package edu.ucdavis.genomics.metabolomics.exception;

/**
 * an exception during the notification process
 *
 * @author wohlgemuth
 */
public class NotificationException extends BinBaseException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public NotificationException() {
        super();
    }

    public NotificationException(final String arg0, final Throwable arg1) {
        super(arg0, arg1);
    }

    public NotificationException(final String arg0) {
        super(arg0);
    }

    public NotificationException(final Throwable arg0) {
        super(arg0);
    }

}
