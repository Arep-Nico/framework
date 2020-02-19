package edu.escuelaing.arep.pojos;

import edu.escuelaing.arep.framework.annotatations.server;
import edu.escuelaing.arep.framework.annotatations.web;

/**
 * webHello
 */
@server(path = "/hello")
public class webHello {

    @web(path = "/greeting")
    public static String Hello() {
        return "<h1>Hello world!</h1>";
    }
}