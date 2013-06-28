import java.net.*;
import java.util.*;
import java.io.*;
import org.json.simple.*;
import org.json.simple.parser.*;

/**
 * Copyright (c) 2013 Citrix Systems, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

/**
 * Methods in this class make use of ShareFile API. Please see api.sharefile.com for more information.
 *
 * Requirements:
 * 
 * JSON.simple jar in your CLASSPATH. see http://code.google.com/p/json-simple/
 * 
 * tested with Java 1.7 and json-simple-1.1.1.jar
 *
 * Optional parameters can be passed to functions as a HashMap as follows:
 * 
 * ex:
 * 
 * HashMap<String, Object> optionalParameters = new HashMap<String, Object>();
 * optionalParameters.put("company", "ACompany");
 * optionalParameters.put("password", "apassword");
 * optionalParameters.put("addshared", true);
 * sample.usersCreate("firstname", "lastname", "an@email.address", false, optionalParameters);
 * 
 * See api.sharefile.com for optional parameter names for each operation.
 */
public class ShareFileSample 
{
	String subdomain;
	String tld;
	String authId;
	
	/**
	 * Calls getAuthID to retrieve an authid that will be used for subsequent calls to API. 
     *
     * If you normally login to ShareFile at an address like https://mycompany.sharefile.com,
     * then your subdomain is mycompany and your tld is sharefile.com
     *
     * sample.authenticate("mycompany", "sharefile.com", "my@user.name", "mypassword");
     *
	 * @param subdomain
	 * @param tld
	 * @param username
	 * @param password
	 * @return boolean true if authentication was successful, false otherwise
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ParseException
	 */
	public boolean authenticate(String subdomain, String tld, String username, String password)
	throws MalformedURLException, IOException, ParseException
	{	
		this.subdomain = subdomain; 
		this.tld = tld;
		
		String requestUrl = String.format("https://%s.%s/rest/getAuthID.aspx?fmt=json&username=%s&password=%s",
				subdomain, tld, URLEncoder.encode(username, "UTF-8"), URLEncoder.encode(password, "UTF-8"));
		
		System.out.println(requestUrl);

		JSONObject jsonObj = this.invokeShareFileOperation(requestUrl);
		boolean error = (boolean) jsonObj.get("error");
		if (!error) {
			String authId = (String) jsonObj.get("value");
			System.out.println("authid="+authId);
			this.authId = authId;
			return(true);
		}
		else {
			long errorCode = (long) jsonObj.get("errorCode");
			String errorMessage = (String) jsonObj.get("errorMessage");
			System.out.println(errorCode + " : " + errorMessage);
			return(false);
		}        
	}
	
	/**
	 * Prints out a folder list for the specified path or root if none is provided.
     *
     * Currently prints out id, filename, creationdate, type.
     *
	 * @param path folder to list
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ParseException
	 */
	public void folderList(String path)
	throws MalformedURLException, IOException, ParseException
	{
		if (path.isEmpty()) {
			path = "/";
		}

		HashMap<String, Object> requiredParameters = new HashMap<String, Object>();
		requiredParameters.put("path", path);

		String url = this.buildUrl("folder", "list", requiredParameters);
		System.out.println(url);

		JSONObject jsonObj = this.invokeShareFileOperation(url);
		boolean error = (boolean) jsonObj.get("error");
		if (!error) {
			JSONArray items = (JSONArray) jsonObj.get("value");
			if (items.isEmpty()) {
				System.out.println("No 	Results");
			}
			Iterator<?> iterItems = items.iterator();
			while (iterItems.hasNext()) {
				JSONObject item = (JSONObject) iterItems.next();
				System.out.println(item.get("id")+" "+item.get("filename")+" "+item.get("creationdate")+" "+item.get("type"));
			}
		}
		else {
			long errorCode = (long) jsonObj.get("errorCode");
			String errorMessage = (String) jsonObj.get("errorMessage");
			System.out.println(errorCode + " : " + errorMessage);
		}
	}
	
	/**
	 * Uploads a file to ShareFile.
	 * 
	 * @param localPath full path to local file like "c:\\path\\to\\file.txt"
	 * @param optionalParameters HashMap of optional parameter names/values
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ParseException
	 */
	public void fileUpload(String localPath, HashMap<String, Object> optionalParameters)
	throws MalformedURLException, IOException, ParseException
	{
		HashMap<String, Object> requiredParameters = new HashMap<String, Object>();
		requiredParameters.put("filename", new File(localPath).getName());

		String url = this.buildUrl("file", "upload", requiredParameters, optionalParameters);
		System.out.println(url);
		
		JSONObject jsonObj = this.invokeShareFileOperation(url);
		boolean error = (boolean) jsonObj.get("error");
		if (!error) {
			String uploadUrl = (String)jsonObj.get("value");
			System.out.println("uploadUrl = "+uploadUrl);
			this.multipartUploadFile(localPath, uploadUrl);
		}
		else {
			long errorCode = (long) jsonObj.get("errorCode");
			String errorMessage = (String) jsonObj.get("errorMessage");
			System.out.println(errorCode + " : " + errorMessage);
		}
		
	}

	/**
	 * Downloads a file from ShareFile.
	 * 
	 * @param fileId id of the file to download
	 * @param localPath complete path to download file to including filename
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ParseException
	 */
	public void fileDownload(String fileId, String localPath)
	throws MalformedURLException, IOException, ParseException
	{
		HashMap<String, Object> requiredParameters = new HashMap<String, Object>();
		requiredParameters.put("id", fileId);

		String url = this.buildUrl("file", "download", requiredParameters);
		System.out.println(url);

		JSONObject jsonObj = this.invokeShareFileOperation(url);
		boolean error = (boolean) jsonObj.get("error");
		if (!error) {
			String downloadUrl = (String)jsonObj.get("value");
			System.out.println("downloadUrl = "+downloadUrl);
			
			BufferedInputStream source = null;
			FileOutputStream target = null;
			try {
				source = new BufferedInputStream(new URL(downloadUrl).openStream());
				target = new FileOutputStream(localPath);

    			byte chunk[] = new byte[8192];
    			int len;
    			while ((len = source.read(chunk, 0, 8192)) != -1)
    			{
    				target.write(chunk, 0, len);
    			}
    			System.out.println("Download complete.");
			}
			catch(IOException ioe){
				ioe.printStackTrace();
			}
			finally {
				source.close();
				target.close();
			}
		}
		else {
			long errorCode = (long) jsonObj.get("errorCode");
			String errorMessage = (String) jsonObj.get("errorMessage");
			System.out.println(errorCode + " : " + errorMessage);
		}

	}
	
	/**
	 * Sends a Send a File email.
	 * 
	 * @param path path to file in ShareFile to send
	 * @param to email address to send to
	 * @param subject email subject 
	 * @param optionalParameters HashMap of optional parameter names/values
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ParseException
	 */
	public void fileSend(String path, String to, String subject, HashMap<String, Object> optionalParameters)
	throws MalformedURLException, IOException, ParseException
	{
		HashMap<String, Object> requiredParameters = new HashMap<String, Object>();
		requiredParameters.put("path", path);
		requiredParameters.put("to", to);
		requiredParameters.put("subject", subject);
		
		String url = this.buildUrl("file", "send", requiredParameters, optionalParameters);
		System.out.println(url);

		JSONObject jsonObj = this.invokeShareFileOperation(url);
		boolean error = (boolean) jsonObj.get("error");
		if (!error) {
			System.out.println(jsonObj.get("value"));
		}
		else {
			long errorCode = (long) jsonObj.get("errorCode");
			String errorMessage = (String) jsonObj.get("errorMessage");
			System.out.println(errorCode + " : " + errorMessage);
		}
	}
	
	/**
	 * Creates a client or employee user in ShareFile.
	 * 
	 * @param firstName
	 * @param lastName
	 * @param email
	 * @param isEmployee true to create an employee, false to create a client
	 * @param optionalParameters HashMap of optional parameter names/values
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ParseException
	 */
	public void usersCreate(String firstName, String lastName, String email, Boolean isEmployee, HashMap<String, Object> optionalParameters)
	throws MalformedURLException, IOException, ParseException
	{
		HashMap<String, Object> requiredParameters = new HashMap<String, Object>();
		requiredParameters.put("firstname", firstName);
		requiredParameters.put("lastname", lastName);
		requiredParameters.put("email", email);
		requiredParameters.put("isemployee", isEmployee);
		
		String url = this.buildUrl("users", "create", requiredParameters, optionalParameters);
		System.out.println(url);
		
		JSONObject jsonObj = this.invokeShareFileOperation(url);
		boolean error = (boolean) jsonObj.get("error");
		if (!error) {
			System.out.println(jsonObj.get("value"));
		}
		else {
			long errorCode = (long) jsonObj.get("errorCode");
			String errorMessage = (String) jsonObj.get("errorMessage");
			System.out.println(errorCode + " : " + errorMessage);
		}		
	}	

	/**
	 * Creates a distribution group in ShareFile.
	 * 
	 * Ex: to create a group and add users to it at the same time
	 * 
	 * optionalParameters.put("isshared", true);
	 * optionalParameters.put("contacts", "an@email.address,another@email.address");
	 * sample.groupCreate("MyGroupName", optionalParameters);
	 * 
	 * @param name
	 * @param optionalParameters HashMap of optional parameter names/values
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ParseException
	 */
	public void groupCreate(String name, HashMap<String, Object> optionalParameters)
	throws MalformedURLException, IOException, ParseException
	{
		HashMap<String, Object> requiredParameters = new HashMap<String, Object>();
		requiredParameters.put("name", name);
		
		String url = this.buildUrl("group", "create", requiredParameters, optionalParameters);
		System.out.println(url);

		JSONObject jsonObj = this.invokeShareFileOperation(url);
		boolean error = (boolean) jsonObj.get("error");
		if (!error) {
			System.out.println(jsonObj.get("value"));
		}
		else {
			long errorCode = (long) jsonObj.get("errorCode");
			String errorMessage = (String) jsonObj.get("errorMessage");
			System.out.println(errorCode + " : " + errorMessage);
		}			
	}

	/**
	 * Searches for items in ShareFile.
	 * 
	 * @param query
	 * @param optionalParameters HashMap of optional parameter names/values
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ParseException
	 */
	public void search(String query, HashMap<String, Object> optionalParameters)
	throws MalformedURLException, IOException, ParseException
	{
		HashMap<String, Object> requiredParameters = new HashMap<String, Object>();
		requiredParameters.put("query", query);
		
		String url = this.buildUrl("search", "search", requiredParameters, optionalParameters);
		System.out.println(url);

		JSONObject jsonObj = this.invokeShareFileOperation(url);
		boolean error = (boolean) jsonObj.get("error");
		if (!error) {
			JSONArray items = (JSONArray) jsonObj.get("value");
			if (items.isEmpty()) {
				System.out.println("No Results");
			}
			Iterator<?> iterItems = items.iterator();
			String path = "";
			while (iterItems.hasNext()) {
				JSONObject item = (JSONObject) iterItems.next();
                path = "/";
                if(item.get("parentid").equals("box")) {
                    path = "/File Box";
                }
                else {
                    path = (String)item.get("parentsemanticpath");
                }
				System.out.println(path+"/"+item.get("filename")+" "+item.get("creationdate")+" "+item.get("type"));
			}
		}
		else {
			long errorCode = (long) jsonObj.get("errorCode");
			String errorMessage = (String) jsonObj.get("errorMessage");
			System.out.println(errorCode + " : " + errorMessage);
		}			
	}

	
	/***************************** Helper Operations *****************************/
	private JSONObject invokeShareFileOperation(String requestUrl)
	throws MalformedURLException, IOException, ParseException
	{
		URL url = new URL(requestUrl);
		URLConnection connection = url.openConnection();  
		connection.connect();
		InputStream is = connection.getInputStream();
	
		int read = -1;
		byte[] inbuffer = new byte[4096];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		 
		while((read = is.read(inbuffer)) != -1){
			baos.write(inbuffer, 0, read);
		}
		
		byte [] b = baos.toByteArray();
	    is.close();
	    
        JSONParser parser=new JSONParser();
        return ((JSONObject) parser.parse(new String(b)));
	}

	private String buildUrl(String endpoint, String op, HashMap<String, Object> requiredParameters)
	{
		return this.buildUrl(endpoint, op, requiredParameters, new HashMap<String, Object>());
	}
	
	private String buildUrl(String endpoint, String op, HashMap<String, Object> requiredParameters, HashMap<String, Object> optionalParameters)
	{
		requiredParameters.put("authid", this.authId);
		requiredParameters.put("op", op);
		requiredParameters.put("fmt", "json");
		ArrayList<String> parameters = new ArrayList<String>();
		StringBuilder urlParameters = new StringBuilder();
		
		try {
			for (Map.Entry<String, Object> entry : requiredParameters.entrySet()) {
				parameters.add(String.format("%s=%s", entry.getKey(), URLEncoder.encode(entry.getValue().toString(), "UTF-8")));
			}
			for (Map.Entry<String, Object> entry : optionalParameters.entrySet()) {
				parameters.add(String.format("%s=%s", entry.getKey(), URLEncoder.encode(entry.getValue().toString(), "UTF-8")));
			}
		
			String separator = "";

			for(String param : parameters) {
				urlParameters.append(separator);
				urlParameters.append(param);
				separator = "&";
			}
		}
		catch (UnsupportedEncodingException ue) {
			ue.printStackTrace();
		}
		String url = String.format("https://%s.%s/rest/%s.aspx?%s", this.subdomain, this.tld, endpoint, urlParameters);
		return(url);
	}

	private void multipartUploadFile(String localPath, String uploadUrl)
	throws MalformedURLException, IOException
	{
		URL url = new URL(uploadUrl);
		URLConnection connection = url.openConnection();

		String boundary = "--"+UUID.randomUUID().toString();

		connection.setDoOutput(true);
	    connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		
		File file = new File(localPath);
		String filename = file.getName();
		
		InputStream source = new FileInputStream(file);
		OutputStream target = connection.getOutputStream();
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("--"+boundary+"\r\n");
		buffer.append("Content-Disposition: form-data; name=File1; filename=\""+filename+"\"\r\n");		
	    String contentType = URLConnection.guessContentTypeFromName(filename);
	    if (contentType == null) { contentType = "application/octet-stream"; }
		buffer.append("Content-Type: "+contentType+"\r\n\r\n");

		target.write(buffer.toString().getBytes());
		
		// read from file, and write to outputstream
	    byte[] buf = new byte[1024*1024];
	    int len;
	    while((len = source.read(buf, 0, buf.length)) >= 0) {
	    	target.write(buf, 0, len);
	    }
	    target.flush();
	    
		target.write(("\r\n--"+boundary+"--\r\n").getBytes());
		
		// get Response
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		StringBuffer response = new StringBuffer();
	    String line = null;
	    while ((line = reader.readLine()) != null) {
	        response.append(line).append("\n");
	    }
	    reader.close();
	    System.out.println(response.toString());
	    
		target.close();
		source.close();
	}	


	public static void main(String[] args) throws Exception
	{
		ShareFileSample sample = new ShareFileSample();
		//HashMap<String, Object> optionalParameters = new HashMap<String, Object>();
		
		boolean loginStatus = sample.authenticate("mysubdomain", "sharefile.com", "my@email.address", "mypassword");
		if (loginStatus)
		{
			sample.folderList("/MyFolder");
		}
	}

}
