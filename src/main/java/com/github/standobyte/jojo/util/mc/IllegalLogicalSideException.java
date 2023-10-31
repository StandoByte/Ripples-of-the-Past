package com.github.standobyte.jojo.util.mc;

public class IllegalLogicalSideException extends RuntimeException {
    
    private static final long serialVersionUID = -5751591837292502539L;

    public IllegalLogicalSideException() {
    }
    
    public IllegalLogicalSideException(String s) {
        super(s);
    }
    
    public IllegalLogicalSideException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public IllegalLogicalSideException(Throwable cause) {
        super(cause);
    }
    
}
