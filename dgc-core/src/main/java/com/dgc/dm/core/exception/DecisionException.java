/*
  @author david
 */

package com.dgc.dm.core.exception;

public class DecisionException extends RuntimeException {

    public DecisionException ( ) {
        super("Decision Exception");
    }

    public DecisionException (final String errorMessage) {
        super(errorMessage);
    }

}
