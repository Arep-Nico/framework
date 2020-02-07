package edu.escuelaing.arep.pojos;

import edu.escuelaing.arep.framework.annotatations.web;

/**
 * webHello
 */
public class webHello {

    @web
    public static String Hello() {
        return "<h1>Hello world!</h1>";
    }
}