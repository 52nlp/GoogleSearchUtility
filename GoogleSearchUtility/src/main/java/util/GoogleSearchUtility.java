package util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class GoogleSearchUtility {

	private static final String googleAddress = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";
	private static final String characterSet = "UTF-8";
	

	/**
	 * This method does a google search, based on the query string, and returns flat, unformatted results.
	 * 
	 * @param queryString (String) the Google search string
	 * 
	 * @return (List<String>) the unformatted results from Google.
	 * 
	 * @throws IOException
	 */
	public static List<String> googleQueryReturnStringList(String queryString) throws IOException
	{
		List<String> searchResults = new ArrayList<String>();
		URL fullSearchURL = new URL(googleAddress.concat(URLEncoder.encode(queryString, characterSet)));
		BufferedReader reader = new BufferedReader(new InputStreamReader(fullSearchURL.openStream()));
		String resultLine;
		while((resultLine = reader.readLine())!=null) {
			searchResults.add(resultLine);
		}
		return searchResults;
	}
	
	/**
	 * This method does a google search, based on the query string, and returns GSON formatted object encapsulating 
	 * the results.
	 * 
	 * @param queryString (String) the Google search string
	 * 
	 * @return (GoogleResults) search results from Google formatted with GSON.
	 * 
	 * @throws IOException
	 */
	public static GoogleResults googleQueryReturnGSON(String queryString) throws IOException
	{
		GoogleResults googleResults = null;
		URL fullSearchURL = new URL(googleAddress.concat(URLEncoder.encode(queryString, characterSet)));
		BufferedReader reader = new BufferedReader(new InputStreamReader(fullSearchURL.openStream()));
		googleResults = new Gson().fromJson(reader, GoogleResults.class);
		return googleResults;	
	}
	

}
