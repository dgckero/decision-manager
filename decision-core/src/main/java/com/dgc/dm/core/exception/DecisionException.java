/*
  @author david
 */

package com.dgc.dm.core.exception;

public class DecisionException extends RuntimeException {

    private static final long serialVersionUID = 647399188819769830L;

    public DecisionException ( ) {
        super("Decision Exception");
    }

    public DecisionException (String errorMessage) {
        super(errorMessage);
    }

}
