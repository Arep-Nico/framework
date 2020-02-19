package edu.escuelaing.arep.framework;

import java.net.*;
import java.io.*;
import java.util.Date;

public class HttpServer {

   public static final String USERPATH = System.getProperty("user.dir");
   public static final String SEPARATOR = System.getProperty("file.separator");

   /**
    * inicia el servidor web desarrollado con la api de net y io de Java
    * @param args
    * @throws IOException
    */
    public static void main(String[] args) throws IOException {
      while (true) {
         ServerSocket serverSocket = null;
         try {
            serverSocket = new ServerSocket(getPort());
         } catch (IOException e) {
            System.err.println("Could not listen on port:" + getPort());
            System.exit(1);
         }

         Socket clientSocket = null;
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
            serverDB(out);
         else if (!fileName.equals("/"))
            HttpServer.returnFile(fileName, out, outbs, ops);
         out.flush();
         out.close();
         ops.close();
         outbs.close();
         in.close();
         clientSocket.close();
         serverSocket.close();
      }
   }

   /**
    * Clasifica el contenido de la peticion del cliente 
    * @param fileName
    * @param out
    * @param outbs
    * @param os
    */
   private static void returnFile(String fileName, PrintWriter out, BufferedOutputStream outbs, OutputStream os) {

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
            HttpServer.serveImage(file, os, contentType.substring(contentType.indexOf("/")+1));
         } else {
            String outString = 
               "HTTP/1.1 200 Ok\r\n" + 
               "Content-type: "+ contentType +"\r\n" +
               "Server: Java HTTP Server\r\n" +
               "Date: " + new Date() + "\r\n" +
               "\r\n";
            String st;
            while ((st = br.readLine()) != null)
               outString += st;
            // System.out.println(outString);
            out.println(outString);
            br.close();
         }
      } catch (IOException e) {
         String outputLine =
            "HTTP/1.1 404 Not Found\r\n" +
            "Content-type: "+ contentType +"\r\n" +
            "Server: Java HTTP Server\r\n" +
            "Date: " + new Date() + "\r\n" +
            "\r\n" +
            "<!DOCTYPE html>" + 
            "<html>" + 
            "<head>" + 
            "<meta charset=\"UTF-8\">" + 
            "<title>File Not Found</title>\n" + 
            "</head>" + 
            "<body>" + 
            "<center><h1>File Not Found</h1></center>" + 
            "</body>" + 
            "</html>";
          out.println(outputLine);
      }
   }

   /**
    * Transforma la imagen solicitada para mandarla por un socket
    * @param file
    * @param outputStream
    * @param ext
    * @throws IOException
    */
   private static void serveImage(File file, OutputStream outputStream, String ext) throws IOException {
         FileInputStream fis = new FileInputStream(file);
         byte[] data = new byte[(int) file.length()];
         fis.read(data);
         fis.close();

         // Cabeceras con la info de la imágen
         DataOutputStream binaryOut = new DataOutputStream(outputStream);
         String outString = "HTTP/1.1 200 Ok\r\n" + 
         "Content-type: image/"+ ext +"\r\n" +
         "Server: Java HTTP Server\r\n" +
         "Date: " + new Date() + "\r\n" +
         "Content-Length: " + data.length + "\r\n" +
         "\r\n";
         binaryOut.writeBytes(outString);
         binaryOut.write(data);

         binaryOut.close();
   }

   /**
    * 
    * @param out
    */
   private static void serverDB(PrintWriter out) {
      String res = DataBase.getData();
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
            "<center>" +
            "<h1>Data DataBase</h1></br>" +
            res + 
            "</center>" + 
            "</body>" + 
            "</html>";
         out.println(outString);
   }

   /**
    * retorna un puerto disponible 
    * @return
    */
   private static int getPort() {
      if (System.getenv("PORT") != null) {
         return Integer.parseInt(System.getenv("PORT"));
      }
      return 5000; // returns default port if heroku-port isn't set (i.e. on localhost)
   }
}