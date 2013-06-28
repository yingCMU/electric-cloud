import java.io.IOException;
import java.net.MalformedURLException;

import org.json.simple.parser.ParseException;

/*
 * @author Ying Li yli@electric-cloud.com
 * reference HTTPS API to http://api.sharefile.com/https.aspx
 * 
 * API methods are URLs called over the HTTPS protocol, specifically of the form:
 * https://subdomain.sharefile.com/rest/method.aspx?[parameter collection]
 * All API calls should be sent as a GET HTTP request
 *  Automate Sharefile accounts
 Responses are returned in one of two formats based on the caller's preference.

    JSON can be returned by appending &fmt=json to the query string
    Alternatively, JSONP can be returned by appending &fmt=jsonp along with an optional callback: &jsonp_callback=callbackname
    XML can be returned by appending &fmt=xml to the query string


Authentication URL example:
https://subdomain.sharefile.com/rest/getAuthID.aspx?op=login&fmt=json&username=email@address&password=password
Response: The authentication URL will return among its results an authid that MUST be used for all subsequent API calls in the current session. (ex. 0oye2k45kbna1234abcd1gyk) The duration of an authid is eighteen (18) hours. API callers must renew authentication within that period to continue using the API. 




Create user in specific organization
--> user should have access to their organizations licenses and uploads directory (full access)
--> User should belong in a distribution group that allows product downloads(view and read)

 */

/*
 * HashMap<String, Object> optionalParameters = new HashMap<String, Object>();
 * optionalParameters.put("company", "ACompany");
 * optionalParameters.put("password", "apassword");
 * optionalParameters.put("addshared", true);
 * sample.usersCreate("firstname", "lastname", "an@email.address", false, optionalParameters);
 */
public class AutoSF {
	static ShareFileSample sf;
	/**
	 * String folderPath = "/test";//"/Clients/Jimbo";
	    String folderName = "test1";
	    would create folder /test/test1
	 * @param args
	 */
	public static void main(String[] args) {
		String folderPath = "/test";//"/Clients/Jimbo";
	    String folderName = "test1";
	    String testOrg = "testOrg";
		String authid = null;
		String mycompany = "electric-cloud";
		String tld = "sharefile.com";
		String username = "yli@electric-cloud.com";
		String password = "0814Cmu$";
	    String orgName = "orgName"; 
	    String clientsPath = "/clients";
	    String contacts = "yli@electric-cloud.com";
		sf= login( mycompany, tld, username, password);
		//createFolder(folderPath, folderName);
		//createOrg(clientsPath, testOrg);
		//add2Group(contacts);
		//groupList();
		
}
	/*
	 * Create Organization procedure
	--> Org name
	--> create licenses and uploads subdirectories
	-->  permissions for EC employees
	--> Distribution groups
	 */
	public static void createOrg(String clientsPath, String orgName){
		//create org's folder in clients
		createFolder(clientsPath,orgName );
		String orgPath = clientsPath + "/" + orgName;
		//create licenses folder in org folder
		createFolder(orgPath,"licenses" );
		//create uploads folder in org folder
		createFolder(orgPath,"uploads" );
	/*
	 * file permission for EC employees are granted automatically I didn't do anything!???
	 */
	}
	
	private static void groupList(){
		try {
			sf.groupList();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	 * contacts can be email address or id
	 */
	private static void add2Group(String contacts){
		try {
			sf.addContact2Group(contacts, null);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void createFolder(String path, String name){
		
		try {
			sf.folderCreate(path,name);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/*
	 * --> permissions for EC employees
	 */
	private static void permissions(){
		
	}
	/*
	 * Distribution groups
	 */
	private static void add2group(){
		
	}
	private static ShareFileSample login(String mycompany, String tld, String username, String password){
		ShareFileSample sample = new ShareFileSample();
	      try {
			sample.authenticate(mycompany, tld, username, password);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      return sample;
		}
	 	
	}
