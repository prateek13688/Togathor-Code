/*
 * The MIT License (MIT)
 * 
 * Copyright � 2013 Clover Studio Ltd. All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.uf.togathor.db.couchdb;

import com.uf.togathor.Togathor;
import com.uf.togathor.utils.constants.Const;
import com.uf.togathor.utils.appservices.Logger;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.nio.charset.Charset;
import java.util.List;

/**
 * ConnectionHandler
 * 
 * Handles basic HTTP connections with server.
 */

public class ConnectionHandler {

	private static String TAG = "ConnectionHandler";

	public ConnectionHandler() {
	}
	
	public static JSONObject getJsonObject(String url, String userId) throws IOException, JSONException, TogathorException, IllegalStateException, TogathorForbiddenException {

		JSONObject retVal = null;

		InputStream is = httpGetRequest(url, userId);
		String result = getString(is);

		is.close();

		retVal = jObjectFromString(result);
			
		Logger.debug("Response: ", retVal.toString());
		return retVal;
	}
	
	/**
	 * Http GET
	 * 
	 * @param url
	 * @return
	 * @throws JSONException 
	 * @throws TogathorException
	 * @throws IOException 
	 * @throws IllegalStateException 
	 * @throws ClientProtocolException 
	 * @throws TogathorForbiddenException
	 */
	public static String getString(String url, String userId) throws IllegalStateException, IOException, TogathorException, JSONException, TogathorForbiddenException {

		String result = null;

		InputStream is = httpGetRequest(url, userId);
		result = getString(is);
		is.close();

		Logger.debug("Response: ", result);
		return result;
	}
	
	public static JSONArray getJsonArray(String url, String userId,
			String token) throws IOException, JSONException, TogathorException, IllegalStateException, TogathorForbiddenException {

		JSONArray retVal = null;

		InputStream is = httpGetRequest(url, userId);
		String result = getString(is);

		is.close();

		retVal = jArrayFromString(result);	

		Logger.debug("Response: ", retVal.toString());
		return retVal;
	}

	
    /**
     * Http POST
     * 
     * @param create
     * @return
     * @throws IOException 
     * @throws ClientProtocolException 
     * @throws JSONException 
     * @throws TogathorException
     * @throws IllegalStateException 
     * @throws TogathorForbiddenException
     */
    public static JSONObject postJsonObject(String apiName,JSONObject create, String userId,
            String token) throws IOException, JSONException, IllegalStateException, TogathorException, TogathorForbiddenException {

        JSONObject retVal = null;

        InputStream is = httpPostRequest(CouchDB.getUrl() + apiName, create, userId);
        String result = getString(is);

        is.close();

        retVal = new JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1));


        Logger.debug("Response: ", retVal.toString());
        return retVal;
    }
    
    /**
     * Http POST
     * 
     * @param create
     * @return
     * @throws IOException 
     * @throws ClientProtocolException 
     * @throws JSONException 
     * @throws TogathorException
     * @throws IllegalStateException 
     * @throws TogathorForbiddenException
     */
    public static String postJsonObjectForString(String apiName,JSONObject create, String userId,
            String token) throws IOException, JSONException, IllegalStateException, TogathorException, TogathorForbiddenException {

        InputStream is = httpPostRequest(CouchDB.getUrl() + apiName, create, userId);
        String result = getString(is);

        is.close();

        Logger.debug("Response: ", result);
        return result;
    }

    /**
	 * Http Auth POST
	 * 
	 * @param create
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 * @throws TogathorException
	 * @throws IllegalStateException 
	 * @throws TogathorForbiddenException
	 */
	public static JSONObject postAuth(JSONObject jPost) throws IOException,
			JSONException, IllegalStateException, TogathorException, TogathorForbiddenException {

		JSONObject retVal = null;

		InputStream is = httpPostRequest(CouchDB.getAuthUrl(), jPost, "");
		String result = getString(is);

		is.close();

		retVal = jObjectFromString(result);

		Logger.debug("Response: ", retVal.toString());
		return retVal;
	}

	/**
	 * Get a File object from an URL
	 * 
	 * @param url
	 * @param path
	 * @return
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 * @throws TogathorException
	 * @throws JSONException 
	 * @throws IllegalStateException 
	 * @throws TogathorForbiddenException
	 */
	public static void getFile(String url, File file, String userId,
			String token) throws IOException, TogathorException, IllegalStateException, JSONException, TogathorForbiddenException {

		File mFile = file;

		//URL mUrl = new URL(url); // you can write here any link

		InputStream is = httpGetRequest(url, userId);
		BufferedInputStream bis = new BufferedInputStream(is);

		ByteArrayBuffer baf = new ByteArrayBuffer(20000);
		int current = 0;
		while ((current = bis.read()) != -1) {
			baf.append((byte) current);
		}

		/* Convert the Bytes read to a String. */
		FileOutputStream fos = new FileOutputStream(mFile);
		fos.write(baf.toByteArray());
		fos.flush();
		fos.close();
		is.close();

	}

	/**
	 * Convert a string response to JSON object
	 * 
	 * @param result
	 * @return
	 * @throws JSONException
	 */
	private static JSONObject jObjectFromString(String result)
			throws JSONException {
		Logger.debug("response to json", result);
		return new JSONObject(result);
	}
	
	/**
	 * Convert a string response to JSON array
	 * 
	 * @param result
	 * @return
	 * @throws JSONException
	 */
	private static JSONArray jArrayFromString(String result)
			throws JSONException {
		Logger.debug("response to json", result);
		return new JSONArray(result);
	}

	/**
	 * Get a String from InputStream
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	private static String getString(InputStream is) throws IOException {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is,
				"UTF-8"), 8);
		StringBuilder builder = new StringBuilder();
		String line = null;

		while ((line = reader.readLine()) != null) {
			builder.append(line + "\n");
		}

		is.close();

		return builder.toString();
	}

	/**
	 * Forming a GET request
	 * 
	 * @param url
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws JSONException 
	 * @throws IllegalStateException 
	 * @throws TogathorForbiddenException
	 */
	public static InputStream httpGetRequest(String url, String userId) throws IOException, TogathorException, IllegalStateException, JSONException, TogathorForbiddenException {

		HttpGet httpget = new HttpGet(url);

		httpget.getParams().setBooleanParameter(
				CoreProtocolPNames.USE_EXPECT_CONTINUE, false);

		httpget.setHeader("Content-Type", "application/json");
		httpget.setHeader("Encoding", "utf-8");
	    httpget.setHeader("database", Const.DATABASE);

		if(userId != null && userId.length() > 0)
		    httpget.setHeader("user_id", userId);
		else{
		    String userIdSaved = Togathor.getPreferences().getUserId();
		    if(userIdSaved != null)
		        httpget.setHeader("user_id", userIdSaved);
		}
		
		String token = Togathor.getPreferences().getUserToken();
		
        if(token != null && token.length() > 0)
            httpget.setHeader("token", token);
		
		Logger.error("httpGetRequest", Togathor.getPreferences().getUserToken());

		print (httpget);
		
		HttpResponse response = HttpSingleton.getInstance().execute(httpget);
		HttpEntity entity = response.getEntity();
		
		Logger.debug("STATUS", "" + response.getStatusLine().getStatusCode());
		if (response.getStatusLine().getStatusCode() > 400)
		{
			HttpSingleton.sInstance=null;
			if (response.getStatusLine().getStatusCode() == 500) throw new TogathorException(getError(entity.getContent()));
			if (response.getStatusLine().getStatusCode() == 403) throw new TogathorForbiddenException();
			throw new IOException(response.getStatusLine().getReasonPhrase());
		}

		return entity.getContent();
	}

	/**
	 * Forming a POST request
	 * 
	 * @param url
	 * @param create
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws TogathorException
	 * @throws IllegalStateException 
	 * @throws JSONException 
	 * @throws TogathorForbiddenException
	 */
	private static InputStream httpPostRequest(String url, Object create,
			String userId) throws
            IOException, IllegalStateException, TogathorException, JSONException, TogathorForbiddenException {

		HttpPost httppost = new HttpPost(url);

		httppost.getParams().setBooleanParameter(
				CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
		
		HttpConnectionParams.setConnectionTimeout(httppost.getParams(), 5000);
		HttpConnectionParams.setSoTimeout(httppost.getParams(), 5000);

		Logger.debug("TIMEOUTS", 
				HttpConnectionParams.getConnectionTimeout(httppost.getParams()) + " " +
						HttpConnectionParams.getSoTimeout(httppost.getParams()));

		httppost.setHeader("Content-Type", "application/json");
        httppost.setHeader("Encoding", "utf-8");
        httppost.setHeader("database", Const.DATABASE);

        if(userId != null && userId.length() > 0)
            httppost.setHeader("user_id", userId);
        else{
            String userIdSaved = Togathor.getPreferences().getUserId();
            if(userIdSaved != null)
                httppost.setHeader("user_id", userIdSaved);
        }
        
        String token = Togathor.getPreferences().getUserToken();
        if(token != null && token.length() > 0)
            httppost.setHeader("token", token);

        

		StringEntity stringEntity = new StringEntity(create.toString(),
				HTTP.UTF_8);

		httppost.setEntity(stringEntity);

		print (httppost);
		
		HttpResponse response = HttpSingleton.getInstance().execute(httppost);
		HttpEntity entity = response.getEntity();
		
		Logger.debug("STATUS", "" + response.getStatusLine().getStatusCode());
		if (response.getStatusLine().getStatusCode() > 400)
		{
			HttpSingleton.sInstance=null;
			if (response.getStatusLine().getStatusCode() == 500) throw new TogathorException(getError(entity.getContent()));
			if (response.getStatusLine().getStatusCode() == 403) throw new TogathorForbiddenException();
			throw new IOException(response.getStatusLine().getReasonPhrase());
		}
		
		return entity.getContent();
	}

	public static String getIdFromFileUploader(String url,
			List<NameValuePair> params) throws IOException, UnsupportedOperationException {
		// Making HTTP request
		
		// defaultHttpClient
		HttpParams httpParams = new BasicHttpParams();
		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(httpParams, "UTF-8");
		HttpClient httpClient = new DefaultHttpClient(httpParams);
		HttpPost httpPost = new HttpPost(url);

		httpPost.setHeader("database", Const.DATABASE);
	     
		Charset charSet = Charset.forName("UTF-8"); // Setting up the
													// encoding

		MultipartEntity entity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);
		for (int index = 0; index < params.size(); index++) {
			if (params.get(index).getName()
					.equalsIgnoreCase(Const.FILE)) {
				// If the key equals to "file", we use FileBody to
				// transfer the data
				entity.addPart(params.get(index).getName(),
						new FileBody(new File(params.get(index)
								.getValue())));
			} else {
				// Normal string data
				entity.addPart(params.get(index).getName(),
						new StringBody(params.get(index).getValue(),
								charSet));
			}
		}

		httpPost.setEntity(entity);

		print (httpPost);
		
		HttpResponse httpResponse = httpClient.execute(httpPost);
		HttpEntity httpEntity = httpResponse.getEntity();
		InputStream is = httpEntity.getContent();

//		if (httpResponse.getStatusLine().getStatusCode() > 400)
//		{
//			if (httpResponse.getStatusLine().getStatusCode() == 500) throw new SpikaException(ConnectionHandler.getError(entity.getContent()));
//			throw new IOException(httpResponse.getStatusLine().getReasonPhrase());
//		}
		
		// BufferedReader reader = new BufferedReader(new
		// InputStreamReader(is, "iso-8859-1"), 8);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				is, Charset.forName("UTF-8")), 8);
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		is.close();
		String json = sb.toString();

		Logger.debug("RESPONSE", json);
		
		return json;
	}
	
	/**
	 * Forming a PUT request
	 * 
	 * @param url
	 * @param create
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
//	private static InputStream httpPutRequest(String url, JSONObject create,
//			String userId) throws ClientProtocolException,
//			IOException {
//
//		HttpPut httpput = new HttpPut(url);
//
//		httpput.getParams().setBooleanParameter(
//				CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
//
//		httpput.setHeader("Content-Type", "application/json");
//		httpput.setHeader("Encoding", "utf-8");
//	    httpput.setHeader("database", Const.DATABASE);
//
//        if(userId != null && userId.length() > 0)
//            httpput.setHeader("user_id", userId);
//        else{
//            String userIdSaved = SpikaApp.getPreferences().getUserId();
//            if(userIdSaved != null)
//                httpput.setHeader("user_id", userIdSaved);
//        }
//        
//        String token = SpikaApp.getPreferences().getUserToken();
//        if(token != null && token.length() > 0)
//            httpput.setHeader("token", token);
//        
//
//		StringEntity stringEntity = new StringEntity(create.toString(),
//				HTTP.UTF_8);
//
//		httpput.setEntity(stringEntity);
//
//		print (httpput);
//		
//		HttpResponse response = HttpSingleton.getDetailInstance().execute(httpput);
//		HttpEntity entity = response.getEntity();
//
//		return entity.getContent();
//	}

	/**
	 * Form a DELETE reqest
	 * 
	 * @param url
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
//	private static InputStream httpDeleteRequest(String url, String userId) throws ClientProtocolException, IOException {
//
//		HttpDelete httpdelete = new HttpDelete(url);
//
//		httpdelete.getParams().setBooleanParameter(
//				CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
//
//		httpdelete.setHeader("Content-Type", "application/json");
//		httpdelete.setHeader("Encoding", "utf-8");
//	    httpdelete.setHeader("database", Const.DATABASE);
//
//        if(userId != null && userId.length() > 0)
//            httpdelete.setHeader("user_id", userId);
//        else{
//            String userIdSaved = SpikaApp.getPreferences().getUserId();
//            if(userIdSaved != null)
//                httpdelete.setHeader("user_id", userIdSaved);
//        }
//        
//        String token = SpikaApp.getPreferences().getUserToken();
//        if(token != null && token.length() > 0)
//            httpdelete.setHeader("token", token);
//        
//        print (httpdelete);
//        
//		HttpResponse response = HttpSingleton.getDetailInstance().execute(httpdelete);
//		HttpEntity entity = response.getEntity();
//		
//		return entity.getContent();
//	}

	/**
	 * HttpClient mini singleton
	 */
	public static class HttpSingleton {
		private static HttpClient sInstance = null;

		protected HttpSingleton() {

		}

		public static HttpClient getInstance() {
			if (sInstance == null) {

				HttpParams params = new BasicHttpParams();
				params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
						HttpVersion.HTTP_1_1);

				SchemeRegistry schemeRegistry = new SchemeRegistry();
				schemeRegistry.register(new Scheme("http", PlainSocketFactory
						.getSocketFactory(), 80));
				final SSLSocketFactory sslSocketFactory = SSLSocketFactory
						.getSocketFactory();
				schemeRegistry.register(new Scheme("https", sslSocketFactory,
						443));
				ClientConnectionManager cm = new ThreadSafeClientConnManager(
						params, schemeRegistry);

				sInstance = new DefaultHttpClient(cm, params);
			}

			return sInstance;
		}
	}

	public static void print (HttpEntity entity) throws IOException
	{
		ByteArrayOutputStream outstream = new ByteArrayOutputStream();
		entity.writeTo(outstream);
		String content = outstream.toString();
		Logger.debug("content", content);
	}
	
	public static void print (HttpEntityEnclosingRequestBase httpMethod) throws IOException
	{
		Logger.debug (httpMethod.getMethod(), httpMethod.getRequestLine().toString());
		
		print(httpMethod.getEntity());
		
		Header[] headers = httpMethod.getAllHeaders();
		for (Header header : headers) {
			Logger.debug("headers", header.toString());
		}
	}
	
	public static void print (HttpRequestBase httpMethod)
	{
		Logger.debug (httpMethod.getMethod(), httpMethod.getRequestLine().toString());
				
		Header[] headers = httpMethod.getAllHeaders();
		for (Header header : headers) {
			Logger.debug("headers", header.toString());
		}
	}
	
	public static String getError (InputStream inputStream) throws IOException, JSONException {
		
		String error = "Unknown Spika Error: ";
		
		String jsonString = getString(inputStream);
		JSONObject jsonObject = jObjectFromString(jsonString);
		if (jsonObject.has("message"))
		{
			error = jsonObject.getString("message");
		}
		else
		{
			error += jsonObject.toString();
		}
		return error;
	}
	
	public static String getError (JSONObject jsonObject) {
		
		String error = "Unknown Spika Error: ";
		
		if (jsonObject.has("message"))
		{
			try {
				error = jsonObject.getString("message");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else {
			error += jsonObject.toString();
		}
		return error;
	}
}
