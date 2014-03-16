package com.goironbox.client;

import java.io.PrintStream;
import java.util.Date;

class Logger  {
    
    private static Logger instance = null;
    private final boolean verbose;
    
    private Logger(boolean verbose) {
        this.verbose = verbose;
    }

    public static Logger getInstance() {
        if (null == instance) {
            instance = new Logger(true);
        }
        return instance;        
    }

    public static Logger getInstance(boolean verbose) {
        if (null == instance) {
            instance = new Logger(verbose);
        }
        return instance;        
    }

    private void log(PrintStream ps, String msg) {
        ps.println(String.format("%s: %s", new Date(), msg));
    }
    
    public void info(String msg) {
        if (verbose) {
            log(System.out, msg);
        }
    }

    public void info(String format, Object ... args) {
        if (verbose) {
            log(System.out, String.format(format, args));
        }
    }

    public void progress(String format, Object ... args) {
        if (verbose) {
            System.out.print(String.format(format, args));
        }
    }

    public void progressDone() {
        if (verbose) {
            System.out.println("");
        }
    }

    public void error(String msg) {
        log(System.err, msg);
    }

    public void error(String format, Object ... args) {
        log(System.err, String.format(format, args));
    }

    public void exception(String msg, Exception e) {
        log(System.err, msg);
        log(System.err, String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage()));
        e.printStackTrace(System.err);
    }

}
