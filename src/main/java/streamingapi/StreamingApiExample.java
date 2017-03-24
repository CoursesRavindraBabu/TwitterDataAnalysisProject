
package streamingapi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import support.OAuthTokenSecret;
import utils.OAuthUtils;

public class StreamingApiExample {
    OAuthTokenSecret OAuthToken;
    final int RECORDS_TO_PROCESS = 10;
    HashSet<String> Keywords;
    HashSet<String> Geoboxes;
    HashSet<String> Userids;
    final String CONFIG_FILE_PATH = "/home/satheesh/projects/TDAnalysis/src/main/java/streamingapi/searchKeywords.txt";
    final String DEF_OUTPATH = "/home/satheesh/projects/TDAnalysis/src/main/java/streamingapi/";
    /**
     * Loads the Twitter access token and secret for a user
     */
    public void LoadTwitterToken() {
        OAuthToken = new OAuthTokenSecret(OAuthUtils.ACCESS_TOKEN, OAuthUtils.ACCESS_TOKEN_SECRET);
    }

    public void createStreamingConnection(String baseUrl, String outFilePath) {
        HttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, new Integer(90000));
        OAuthConsumer consumer = new CommonsHttpOAuthConsumer(OAuthUtils.CONSUMER_KEY,OAuthUtils.CONSUMER_SECRET);
        consumer.setTokenWithSecret(OAuthToken.getAccessToken(),OAuthToken.getAccessSecret());
        HttpPost httppost = new HttpPost(baseUrl);
        try {
            httppost.setEntity(new UrlEncodedFormEntity(createRequestBody(), "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        try {
             //Step 3: Sign the request
                consumer.sign(httppost);
        } catch (Exception ex) {
                ex.printStackTrace();
        }
        HttpResponse response;
        InputStream is = null;
        try {
             //Step 4: Connect to the API
                response = httpClient.execute(httppost);
                if (response.getStatusLine().getStatusCode()!= HttpStatus.SC_OK)
                {
                    throw new IOException("Got status " +response.getStatusLine().getStatusCode());
                }
                else {
                    System.out.println(OAuthToken.getAccessToken()+ ": Processing from " + baseUrl);
                    HttpEntity entity = response.getEntity();
                    try {
                        is = entity.getContent();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } catch (IllegalStateException ex) {
                        ex.printStackTrace();
                    }
                    //Step 5: Process the incoming Tweet Stream
                    this.processTwitterStream(is, outFilePath);
                }
         } catch (IOException ex) {
            ex.printStackTrace();
        }finally {
            // Abort the method, otherwise releaseConnection() will
            // attempt to finish reading the never-ending response.
            // These methods do not throw exceptions.
            if(is!=null)
            {
                try {
                    is.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void processTwitterStream(InputStream is, String outFilePath) {
        BufferedWriter bwrite = null;
        try {
            JSONTokener jsonTokener = new JSONTokener(new InputStreamReader(is, "UTF-8"));
            ArrayList<JSONObject> rawtweets = new ArrayList<JSONObject>();
            int nooftweetsuploaded = 0;
            while (true) {
                try {
                    JSONObject temp = new JSONObject(jsonTokener);
                    System.out.println("Tweet" + temp);
                    rawtweets.add(temp);
                    System.out.println(temp);
                    if (rawtweets.size() >= RECORDS_TO_PROCESS)
                    {
                        Calendar cal = Calendar.getInstance();
                        String filename = outFilePath + "tweets_" + cal.getTimeInMillis() + ".json";
                        bwrite = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
                        nooftweetsuploaded += RECORDS_TO_PROCESS;
                        //Write the collected tweets to a file
                        for (JSONObject jobj : rawtweets) {
                            bwrite.write(jobj.toString());
                            bwrite.newLine();
                        }
                        System.out.println("Written "+nooftweetsuploaded+" records so far");
                        bwrite.close();
                        rawtweets.clear();
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        StreamingApiExample sae = new StreamingApiExample();
        sae.LoadTwitterToken();
        //load parameters from a TSV file
        String filename = sae.CONFIG_FILE_PATH;
        String outfilepath = sae.DEF_OUTPATH;
        sae.readParameters(filename);
        sae.createStreamingConnection("https://stream.twitter.com/1.1/statuses/filter.json", outfilepath);
    }

    public void readParameters(String filename) throws IOException {

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
            String temp = "";
            int count = 1;
            if (Keywords == null) {
                Keywords = new HashSet<String>();
            }
            while ((temp = br.readLine()) != null) {
                String[] keywords = temp.split("\t");
                for (String word : keywords) {
                    if (!Keywords.contains(word)) {
                        Keywords.add(word);
                    }
                }
            }
        } finally {

        }
    }

     private List<NameValuePair> createRequestBody() {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        if (Keywords != null&&Keywords.size()>0) {
            params.add(createNameValuePair("track", Keywords));
            System.out.println("keywords = "+Keywords);
        }
        return params;
    }


    private NameValuePair createNameValuePair(String name, Collection<String> items) {
        StringBuilder sb = new StringBuilder();
        boolean needComma = false;
        for (String item : items) {
            if (needComma) {
                sb.append(',');
            }
            needComma = true;
            sb.append(item);
        }
        return new BasicNameValuePair(name, sb.toString());
    }
}
