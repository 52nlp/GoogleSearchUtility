package util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import util.GoogleResults.Result;

import com.google.gson.Gson;

public class GoogleSearchUtility {

	private static final String googleAddress = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";
	private static final String characterSet = "UTF-8";
	private static final String outputDirectory = "src/main/resources";
	

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
	
	private static void createOutputFile(String pathName, String fileName) throws IOException 
	{
		// TODO, CLG: do a sanity check on the path name
		String fullyQualifiedFileName = Paths.get(pathName, fileName).toString(); 
		File outputFile = new File(fullyQualifiedFileName);
		if(!outputFile.exists())
		{
			outputFile.createNewFile();
		}
		else
		{
			// TODO, CLG: put a check here if the files are identical and don't need an update
			outputFile.delete();
			outputFile.createNewFile();
		}
	}
	
	public static void connectToURL(String urlString) throws IOException 
	{
		// common swap space for temporary storage
		byte[] byteArray = new byte[1024];
		int byteArrayLength;
		URL url = new URL(urlString);
		try 
		{
			URLConnection connection = url.openConnection();
			if(!connection.getContentType().equalsIgnoreCase("application/pdf"))
			{
				// TODO, CLG: implement a logger error
				System.out.println("NOT a PDF.");
			}
			else 
			{
				String shortFileName = urlString.substring(urlString.lastIndexOf("/")+1);
				System.out.println("IS a PDF: " + shortFileName + ".");
				createOutputFile(outputDirectory, shortFileName);
				try
				{
					String outputFileName = Paths.get(outputDirectory, shortFileName).toString();
					FileOutputStream fileOutputStream = new FileOutputStream(outputFileName);
					InputStream fileInputStream =  url.openStream();
					while((byteArrayLength = fileInputStream.read(byteArray))!=-1)
					{
						fileOutputStream.write(byteArray, 0, byteArrayLength);
					}
					fileOutputStream.flush();
					fileOutputStream.close();
					fileInputStream.close();
				} 
				catch (Exception e)
				{
					// TODO, CLG: implement a logger error message for not opening output file
					System.out.println("ERROR: cannot open destination file to output.");					
				}
				

			}
		} 
		catch (ConnectException ce)
		{
			System.out.println("TODO, CLG: implement a logger.");
		}
	}
	
	// TODO, CLG: testing hack -- remove and replace with wrapper method(s) or unit test(s) 
	public static void main(String[] args)
	{
		try {
			GoogleResults results = GoogleSearchUtility.googleQueryReturnGSON("Real Analysis filetype:pdf");
			List<Result> resultList = results.getResponseData().getResults();
			for (Result iResult : resultList)
			{
				System.out.println(iResult.getUrl());
				connectToURL(iResult.getUrl());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
