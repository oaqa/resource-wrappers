package edu.cmu.lti.oaqa.bio.resource_wrapper;

/**
 * Database Information Object (for database logins)
 * @author Collin McCormack (cmccorma)
 * @version 0.2
 */
public class DBInfo {
	
	public String URL;
	public String userName;
	public String password;
	public String dbClass;
	
	/**
	 * Create a database information object populated with the login information
	 * for peace.
	 * @param specificDB which database to connect to (e.g. resources)
	 */
	public DBInfo(String specificDB) {
		this.URL = "jdbc:mysql://peace.isri.cs.cmu.edu:12321/"+specificDB;
		this.userName = "root";
		this.password = "s3p1apa55w0rd";
		this.dbClass = "com.mysql.jdbc.Driver";
	}
	
	/**
	 * Create a database information object, specifying the relevant login information,
	 * using a MySQL database driver.
	 * @param url String URL of the database
	 * @param username Desired user to login as
	 * @param pass Password for specified user
	 */
	public DBInfo(String url, String username, String pass) {
		this.URL = url;
		this.userName = username;
		this.password = pass;
		this.dbClass = "com.mysql.jdbc.Driver";
	}
	
	/**
	 * Create a database information object, specifying all the login information.
	 * @param url String URL of the database
	 * @param username Desired user to login as
	 * @param pass Password for specified user
	 * @param dbClass String name of database driver/class
	 */
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
