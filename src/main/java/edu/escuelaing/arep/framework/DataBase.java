package edu.escuelaing.arep.framework;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * DataBase
 */
public class DataBase {

  public static String getData() {
    String res = "";
    try {
      Class.forName("org.postgresql.Driver");
      String host = "ec2-35-168-54-239.compute-1.amazonaws.com";
      String db = "d8g67as15jh72k";
      String port = "5432";
      String user = "wwycfalrdvlodn";
      String passwd = "a6d4b6436e88f6fee723d67818cc6c4a83576b2bfe73fe66f7793e0f70ddef4d";
      Connection con = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/" + db, user, passwd);
      Statement stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery("select * from test");
      int i = 0;
      while (rs.next()){
        res += "Line" + i + ": " + rs.getInt(1) + "  " + rs.getString(2) + "</br>";
        i++;
      }
      con.close();
    } catch (Exception e) {
      res = "Can't Connect to database";
      System.out.println(e);
    }
    return res;
  }
}