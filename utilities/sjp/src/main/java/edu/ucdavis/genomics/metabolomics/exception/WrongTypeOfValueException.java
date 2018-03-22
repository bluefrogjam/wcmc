/*
 * Created on Aug 20, 2003
 *
 */
package edu.ucdavis.genomics.metabolomics.exception;

import java.io.PrintStream;
import java.io.PrintWriter;


/**
 * @author wohlgemuth
 * @version Aug 20, 2003
 * <br>
 * BinBaseDatabase
 * @description
 */
public class WrongTypeOfValueException extends RuntimeException {
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 2L;

    /**
     * @author wohlgemuth
     * @version Aug 20, 2003
     * <br>
     * @param string
     */
    public WrongTypeOfValueException(String string) {
        super(string);
    }

    /**
     * @version Aug 20, 2003
     * @author wohlgemuth
     * <br>
     * @see Throwable#getCause()
     */
    public Throwable getCause() {
        return super.getCause();
    }

    /**
     * @version Aug 20, 2003
     * @author wohlgemuth
     * <br>
     * @see Throwable#getLocalizedMessage()
     */
    public String getLocalizedMessage() {
        return super.getLocalizedMessage();
    }

    /**
     * @version Aug 20, 2003
     * @author wohlgemuth
     * <br>
     * @see Throwable#getMessage()
     */
    public String getMessage() {
        return super.getMessage();
    }

    /**
     * @version Aug 20, 2003
     * @author wohlgemuth
     * <br>
     * @see Throwable#setStackTrace(StackTraceElement[])
     */
    public void setStackTrace(StackTraceElement[] stackTrace) {
        super.setStackTrace(stackTrace);
    }

    /**
     * @version Aug 20, 2003
     * @author wohlgemuth
     * <br>
     * @see Throwable#getStackTrace()
     */
    public StackTraceElement[] getStackTrace() {
        return super.getStackTrace();
    }

    /**
     * @version Aug 20, 2003
     * @author wohlgemuth
     * <br>
     * @see Object#equals(Object)
     */
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /**
     * @version Aug 20, 2003
     * @author wohlgemuth
     * <br>
     * @see Throwable#fillInStackTrace()
     */
    public synchronized Throwable fillInStackTrace() {
        return super.fillInStackTrace();
    }

    /**
     * @version Aug 20, 2003
     * @author wohlgemuth
     * <br>
     * @see Object#hashCode()
     */
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * @version Aug 20, 2003
     * @author wohlgemuth
     * <br>
     * @see Throwable#initCause(Throwable)
     */
    public synchronized Throwable initCause(Throwable cause) {
        return super.initCause(cause);
    }

    /**
     * @version Aug 20, 2003
     * @author wohlgemuth
     * <br>
     * @see Throwable#printStackTrace()
     */
    public void printStackTrace() {
        super.printStackTrace();
    }

    /**
     * @version Aug 20, 2003
     * @author wohlgemuth
     * <br>
     * @see Throwable#printStackTrace(PrintStream)
     */
    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
    }

    /**
     * @version Aug 20, 2003
     * @author wohlgemuth
     * <br>
     * @see Throwable#printStackTrace(PrintWriter)
     */
    public void printStackTrace(PrintWriter s) {
        super.printStackTrace(s);
    }

    /**
     * @version Aug 20, 2003
     * @author wohlgemuth
     * <br>
     * @see Object#toString()
     */
    public String toString() {
        return super.toString();
    }

    /**
     * @version Aug 20, 2003
     * @author wohlgemuth
     * <br>
     * @see Object#clone()
     */
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * @version Aug 20, 2003
     * @author wohlgemuth
     * <br>
     * @see Object#finalize()
     */
    protected void finalize() throws Throwable {
        super.finalize();
    }
}
