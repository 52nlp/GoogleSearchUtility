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
import java.util.logging.Level;

import util.GoogleResults.Result;

import com.google.gson.Gson;
import com.sun.istack.internal.logging.Logger;

public class GoogleSearchUtility {

	private final static Logger logger = Logger.getLogger(GoogleSearchUtility.class);
	private static final String googleAddress = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";
	private static final String characterSet = "UTF-8";
	private static final String outputDirectory = "src/main/resources";
	private static final String pdfSearchFilter = " filetype:pdf";
	

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
		// assuring that we are filtering by the PDF file-type
		if(!queryString.contains(pdfSearchFilter))
		{
			queryString = queryString.concat(pdfSearchFilter); 
		}
		URL fullSearchURL = new URL(googleAddress.concat(URLEncoder.encode(queryString, characterSet)));
		BufferedReader reader = new BufferedReader(new InputStreamReader(fullSearchURL.openStream()));
		googleResults = new Gson().fromJson(reader, GoogleResults.class);
		return googleResults;	
	}
	
	/**
	 * This method creates a output file to write the google search result item to disk.
	 * 
	 * @param pathName The string path to the directory that will contain all of the search results.
	 * 
	 * @param fileName The file name of the search result without the prepending URL.
	 * 
	 * @throws IOException
	 */
	private static void createOutputFile(String pathName, String fileName) throws IOException 
	{
		File outputDirectory = new File(pathName);
		if(!outputDirectory.isDirectory())
		{
			throw new IOException("The output directory: " + pathName + ", does not exist on the local file system."); 
		}
		String fullyQualifiedFileName = Paths.get(pathName, fileName).toString(); 
		File outputFile = new File(fullyQualifiedFileName);
		if(!outputFile.exists())
		{
			outputFile.createNewFile();
		}
		else
		{
			// no check for duplicate file, can address later if a significant performance issue
			outputFile.delete();
			outputFile.createNewFile();
		}
	}
	
	/**
	 * This method connects the URL from the search result and writes to the file to disk (if a PDF).
	 * 
	 * @param urlString The full string URL to the PDF search result.
	 * 
	 * @throws IOException This exception is thrown if the file is not a valid PDF.
	 * 
	 */
	public static void connectToURLAndWriteFileToDisk(String urlString) throws IOException 
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
				throw new IOException("The file type of the search result is not a PDF.");
			}
			else 
			{
				String shortFileName = urlString.substring(urlString.lastIndexOf("/")+1);
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
					logger.log(Level.WARNING, "Cannot open destination file to output: " + shortFileName);
				}				
			}
		} 
		catch (ConnectException ce)
		{
			logger.log(Level.WARNING, "Cannot connect to search result URL: " + urlString);
		}
	}

}
