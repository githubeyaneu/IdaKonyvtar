package eu.eyan.idakonyvtar.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Charsets;

public class HttpHelper
{

    public static String postUrl(final String request, final String urlParameters) throws IOException
    {
        URL obj = new URL(request);
        HttpURLConnection httpURLConnection = (HttpURLConnection) obj.openConnection();
    
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:26.0) Gecko/20100101 Firefox/26.0");
        httpURLConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
    
        httpURLConnection.setDoOutput(true);
        DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
        dataOutputStream.writeBytes(urlParameters);
        dataOutputStream.flush();
        dataOutputStream.close();
    
        System.out.println("\nSending 'POST' request to URL : " + request);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + httpURLConnection.getResponseCode());
        System.out.println("Connection Encoding : " + httpURLConnection.getContentEncoding());
        
    
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), Charsets.UTF_8));
    
        StringBuffer ret = new StringBuffer();
        String inputLine;
        while ((inputLine = bufferedReader.readLine()) != null)
        {
            ret.append(inputLine + "\r\n");
        }
        bufferedReader.close();
    
        FileUtils.writeStringToFile(new File("C:\\tmp\\test.html"), ret.toString());
        FileUtils.writeStringToFile(new File("C:\\tmp\\test_" + HttpHelper.ct++ + ".html"), ret.toString());
    
        return ret.toString();
    }

    public static long ct = System.currentTimeMillis();

}
