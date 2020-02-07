package edu.escuelaing.arep.framework;

import java.net.*;
import java.io.*;
import java.util.Date;

public class HttpServer {

   public static final String USERPATH = System.getProperty("user.dir");
   public static final String SEPARATOR = System.getProperty("file.separator");

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
         if (!fileName.equals("/"))
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
            out.println("HTTP/1.1 200 OK");
            out.println("Access-Control-Allow-Origin: *");
            out.println("Server: Java HTTP Server");
            out.println("Date: " + new Date());
            out.println("Content-type: " + contentType);
            out.println("\r\n");
            out.println();
            out.flush();
            String st;
            while ((st = br.readLine()) != null)
               out.println(st);
            br.close();
         }
      } catch (IOException e) {
         out.println("HTTP/1.1 404 Not Found");
         out.println("Content-type: " + "text/html");
         out.println("\r\n");
         String outputLine = "<!DOCTYPE html>" + 
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

   private static void serveImage(File file, OutputStream outputStream, String ext) throws IOException {
         FileInputStream fis = new FileInputStream(file);
         byte[] data = new byte[(int) file.length()];
         fis.read(data);
         fis.close();

         // Cabeceras con la info de la imágen
         DataOutputStream binaryOut = new DataOutputStream(outputStream);
         binaryOut.writeBytes("HTTP/1.0 200 OK\r\n");
         binaryOut.writeBytes("Content-Type: image/"+ ext +"\r\n");
         binaryOut.writeBytes("Content-Length: " + data.length);
         binaryOut.writeBytes("\r\n\r\n");
         binaryOut.write(data);

         binaryOut.close();
   }

   private static int getPort() {
      if (System.getenv("PORT") != null) {
         return Integer.parseInt(System.getenv("PORT"));
      }
      return 5000; // returns default port if heroku-port isn't set (i.e. on localhost)
   }
}