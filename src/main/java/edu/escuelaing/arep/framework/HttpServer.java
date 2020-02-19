package edu.escuelaing.arep.framework;

import java.net.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HttpServer {

   public static final String USERPATH = System.getProperty("user.dir");
   public static final String SEPARATOR = System.getProperty("file.separator");
   private Map<String, Method> mappingUrl = new HashMap<String, Method>();

   public HttpServer(Map<String, Method> url) {
      this.mappingUrl = url;
   }

   public void start() throws IOException {
      ServerSocket serverSocket = null;
      try {
         serverSocket = new ServerSocket(getPort());
      } catch (IOException e) {
         System.err.println("Could not listen on port:" + getPort());
         System.exit(1);
      }

      Socket clientSocket = null;
      while (true) {
         try {
            System.out.println("Listo para recibir, puerto: " + serverSocket.getLocalPort());
            clientSocket = serverSocket.accept();
            System.out.println("Nueva Coneccion");
         } catch (IOException e) {
            System.err.println("Accept failed.");
            System.exit(1);
         }

         OutputStream ops = clientSocket.getOutputStream();
         PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
         BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
         BufferedOutputStream outbs = new BufferedOutputStream(clientSocket.getOutputStream());

         String inputLine, fileName = "/";
         while ((inputLine = in.readLine()) != null) {
            // System.out.println("Recibí: " + inputLine);
            if (inputLine.startsWith("GET"))
               fileName = inputLine.substring(inputLine.indexOf("/") + 1, inputLine.indexOf("HTTP"));
            if (!in.ready()) {
               break;
            }
         }
         if (fileName.equals(" "))
            fileName = "index.html ";
         if (fileName.equals("getDB "))
            HanderDB(out);
         else if (fileName.startsWith("api"))
            HanderApi(out, fileName.substring(fileName.indexOf("/"), fileName.length()-1));
         else if (!fileName.equals("/"))
            HandlerFiles(fileName, out, outbs, ops);
         out.flush();
         out.close();
         ops.close();
         outbs.close();
         in.close();
      }
   }

   /**
    * Clasifica el contenido de la peticion del cliente
    * 
    * @param fileName
    * @param out
    * @param outbs
    * @param os
    */
   private void HandlerFiles(String fileName, PrintWriter out, BufferedOutputStream outbs, OutputStream os) {

      String path = HttpServer.USERPATH + HttpServer.SEPARATOR + "src" + HttpServer.SEPARATOR + "main"
            + HttpServer.SEPARATOR + "java" + HttpServer.SEPARATOR + "resources" + HttpServer.SEPARATOR
            + fileName.substring(0, fileName.length() - 1);

      System.out.println("Request: " + fileName);
      String contentType = "";

      if (fileName.endsWith(".html ") || fileName.endsWith(".htm "))
         contentType = "text/html";
      else if (fileName.endsWith(".css "))
         contentType = "text/css";
      else if (fileName.endsWith(".ico "))
         contentType = "image/x-icon";
      else if (fileName.endsWith(".png "))
         contentType = "image/png";
      else if (fileName.endsWith(".jpeg ") || fileName.endsWith(".jpg "))
         contentType = "image/jpeg";
      else if (fileName.endsWith(".js "))
         contentType = "application/javascript";
      else if (fileName.endsWith(".json "))
         contentType = "application/json";
      else
         contentType = "text/plain";

      try {
         File file = new File(path);
         BufferedReader br = new BufferedReader(new FileReader(file));

         if (contentType.contains("image/")) {
            HandlerImage(file, os, contentType.substring(contentType.indexOf("/") + 1));
         } else {
            String outString = "HTTP/1.1 200 Ok\r\n" + "Content-type: " + contentType + "\r\n"
                  + "Server: Java HTTP Server\r\n" + "Date: " + new Date() + "\r\n" + "\r\n";
            String st;
            while ((st = br.readLine()) != null)
               outString += st;
            // System.out.println(outString);
            out.println(outString);
            br.close();
         }
      } catch (IOException e) {
         String outputLine = "HTTP/1.1 404 Not Found\r\n" + "Content-type: " + contentType + "\r\n"
               + "Server: Java HTTP Server\r\n" + "Date: " + new Date() + "\r\n" + "\r\n" + "<!DOCTYPE html>" + "<html>"
               + "<head>" + "<meta charset=\"UTF-8\">" + "<title>File Not Found</title>\n" + "</head>" + "<body>"
               + "<center><h1>File Not Found</h1></center>" + "</body>" + "</html>";
         out.println(outputLine);
      }
   }

   /**
    * Transforma la imagen solicitada para mandarla por un socket
    * 
    * @param file
    * @param outputStream
    * @param ext
    * @throws IOException
    */
   private void HandlerImage(File file, OutputStream outputStream, String ext) throws IOException {
      FileInputStream fis = new FileInputStream(file);
      byte[] data = new byte[(int) file.length()];
      fis.read(data);
      fis.close();

      // Cabeceras con la info de la imágen
      DataOutputStream binaryOut = new DataOutputStream(outputStream);
      String outString = "HTTP/1.1 200 Ok\r\n" + "Content-type: image/" + ext + "\r\n" + "Server: Java HTTP Server\r\n"
            + "Date: " + new Date() + "\r\n" + "Content-Length: " + data.length + "\r\n" + "\r\n";
      binaryOut.writeBytes(outString);
      binaryOut.write(data);

      binaryOut.close();
   }

   /**
    * 
    * @param out
    */
   private void HanderDB(PrintWriter out) {
      String res = DataBase.getData();
      String outString = "HTTP/1.1 200 Ok\r\n" + "Content-type: " + "text/html" + "\r\n"
            + "Server: Java HTTP Server\r\n" + "Date: " + new Date() + "\r\n" + "\r\n" + "<!DOCTYPE html>" + "<html>"
            + "<head>" + "<meta charset=\"UTF-8\">" + "<title>DataBase</title>\n" + "</head>" + "<body>" + "<center>"
            + "<h1>Data DataBase</h1></br>" + res + "</center>" + "</body>" + "</html>";
      out.println(outString);
   }

   /**
    * optiene el valor del metodo especificado y lo estructura en un html
    * @param out
    * @param path
    */
   private void HanderApi(PrintWriter out, String path) {
      Method m = mappingUrl.get(path);
      String res = "";
      if (!m.equals(null)) {
         try {
            res = (String) m.invoke(null);
         } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            res = "<center><h1>Can't find a handler</h1></center>";
         }
      }

      String outString = 
         "HTTP/1.1 200 Ok\r\n" + 
         "Content-type: " + "text/html" + "\r\n" + 
         "Server: Java HTTP Server\r\n" + 
         "Date: " + new Date() + "\r\n" + 
         "\r\n" +
         "<!DOCTYPE html>" + 
            "<html>" + 
            "<head>" + 
            "<meta charset=\"UTF-8\">" + 
            "<title>DataBase</title>\n" + 
            "</head>" + 
            "<body>" + 
            res +
            "</body>" + 
            "</html>";
      out.println(outString);
   }

   /**
    * retorna un puerto disponible 
    * @return
    */
   private int getPort() {
      if (System.getenv("PORT") != null) {
         return Integer.parseInt(System.getenv("PORT"));
      }
      return 5000; // returns default port if heroku-port isn't set (i.e. on localhost)
   }

}