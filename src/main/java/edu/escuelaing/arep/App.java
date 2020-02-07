package edu.escuelaing.arep;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import edu.escuelaing.arep.framework.annotatations.web;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        String className = "edu.escuelaing.arep.pojos.webHello";
        try {
            Class c = Class.forName(className);
            for (Method m : c.getMethods()) {
                if (m.isAnnotationPresent(web.class)) {
                    System.out.println(m.getName());
                    System.out.println(m.invoke(null));
                }
            }
        } catch (Exception e) {
            Logger.getLogger(e.toString());
        }
    }
}
