package edu.cmu.lti.oaqa.bio.umls_wrapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LogUtil 
{
	// Database configuration information
	// This information is used across all classes in the package to connect to the database.

    // TODO:  Move this out into a separate resource
    
    static String jdbcDriver;
    static String jdbcURL;
    static String userName;
    static String password;
    
    static {
      InputStream in = LogUtil.class.getResourceAsStream("/default.properties");
      Properties prop = new Properties();
      try {
        prop.load(in);
        in.close();
        jdbcDriver = prop.getProperty("jdbcDriver");
        jdbcURL = prop.getProperty("jdbcURL");
        userName = prop.getProperty("userName");
        password = prop.getProperty("password");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
	
    private static boolean traceEnabled = true;
	private static int verbosity = 2;

	
    // TODO:  Currently outputs messages to the console, can change this later to write to
    // an event log.
	
    /// <summary>
    /// Outputs a message to a log based on verbosity settings and message importance.
    /// </summary>
    /// <param name="messagePriority">Priority of the message 1=High, 2=Med, 3=Low</param>
    /// <param name="message">Content of the message to log.</param>
	public static void traceLog(int messagePriority, String message)
	{
		if (LogUtil.traceEnabled && (messagePriority <= LogUtil.verbosity))
		{
			System.out.println(message);
		}
	}
	
    /// <summary>
    /// Outputs a message to a log based on verbosity settings and message importance.
    /// </summary>
    /// <param name="messagePriority">Priority of the message 1=High, 2=Med, 3=Low</param>
    /// <param name="message">Content of the message to log.</param>
	/// <param name="e">Raised exception.</param>
	public static void traceLog(int messagePriority, String message, Exception ex)
	{
		if (LogUtil.traceEnabled && (messagePriority <= LogUtil.verbosity))
		{
			System.out.println(message);
			ex.printStackTrace();
		}
	}

}
