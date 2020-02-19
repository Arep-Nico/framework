package edu.escuelaing.arep;

import java.io.IOException;

import edu.escuelaing.arep.framework.HttpServer;
import edu.escuelaing.arep.framework.LoadClasses;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        HttpServer server = new HttpServer(LoadClasses.getPathClass());
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
