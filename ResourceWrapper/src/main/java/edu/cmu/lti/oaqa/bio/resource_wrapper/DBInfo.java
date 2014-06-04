package edu.cmu.lti.oaqa.bio.resource_wrapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Static Database Information Object (for database logins)
 * 
 * @author Collin McCormack (cmccorma)
 * @version 0.1
 */
public final class DBInfo {

  public static String dbClass;

  public static String URL;

  public static String userName;

  public static String password;

  static {
    InputStream in = DBInfo.class.getResourceAsStream("/default.properties");
    Properties prop = new Properties();
    try {
      prop.load(in);
      in.close();
      dbClass = prop.getProperty("dbClass");
      URL = prop.getProperty("URL");
      userName = prop.getProperty("userName");
      password = prop.getProperty("password");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
