package edu.cmu.lti.oaqa.bio.resource_wrapper;

/**
 * Static Database Information Object (for database logins)
 * @author Collin McCormack (cmccorma)
 * @version 0.1
 */
public class DBInfo {
	
	public String URL;
	public String userName;
	public String password;
	public String dbClass;
	
	public DBInfo(String specificDB) {
		this.URL = "jdbc:mysql://peace.isri.cs.cmu.edu:12321/"+specificDB;
		this.userName = "root";
		this.password = "s3p1apa55w0rd";
		this.dbClass = "com.mysql.jdbc.Driver";
	}
	
	public DBInfo(String url, String username, String pass) {
		this.URL = url;
		this.userName = username;
		this.password = pass;
		this.dbClass = "com.mysql.jdbc.Driver";
	}
	
	public DBInfo(String url, String username, String pass, String dbClass) {
		this.URL = url;
		this.userName = username;
		this.password = pass;
		this.dbClass = dbClass;
	}
	
	///// Resources Info /////
	//private String dbClass = "com.mysql.jdbc.Driver";
	//private String dbUrl = "jdbc:mysql://localhost:3306/bioqa";
	//private String dbUser = "root";
	//private String dbPassword = "bioqa";
}
