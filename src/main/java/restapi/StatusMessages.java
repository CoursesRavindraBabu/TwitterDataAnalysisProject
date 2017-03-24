package restapi;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.OAuthUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by satheesh on 21/3/17.
 */
public class StatusMessages {

    public OAuthConsumer getConsumer() {
        OAuthConsumer consumer = new DefaultOAuthConsumer(OAuthUtils.CONSUMER_KEY, OAuthUtils.CONSUMER_SECRET);
        consumer.setTokenWithSecret(OAuthUtils.ACCESS_TOKEN, OAuthUtils.ACCESS_TOKEN_SECRET);
        return consumer;
    }
    /**
     * Retrieved the status messages of a user
     * @param username the name of the user whose status messages need to be retrieved
     * @return a list of status messages
     */
    public JSONArray getStatuses(String username) {
        BufferedReader bRead = null;
        //Get the maximum number of tweets possible in a single page 200
        int tweetcount = 200;
        //Include include_rts because it is counted towards the limit anyway.
        boolean include_rts = true;
        JSONArray statuses = new JSONArray();
        try {
            System.out.println("Processing status messages of "+username);
            long maxid = 0;
            while(true) {
                URL url = null;
                if(maxid==0) {
                    url = new URL("https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=" + username+"&include_rts="+include_rts+"&count="+tweetcount);
                }
                else {
                    //use max_id to get the tweets in the next page. Use max_id-1 to avoid getting redundant tweets.
                    url = new URL("https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=" + username+"&include_rts="+include_rts+"&count="+tweetcount+"&max_id="+(maxid-1));
                }
                HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                huc.setReadTimeout(5000);
                OAuthConsumer consumer = getConsumer();
                consumer.sign(huc);
                huc.connect();
                bRead = new BufferedReader(new InputStreamReader((InputStream) huc.getInputStream()));
                StringBuilder content = new StringBuilder();
                String temp = "";
                while((temp = bRead.readLine())!=null) {
                    content.append(temp);
                }
                try {
                    JSONArray statusarr = new JSONArray(content.toString());
                    if(statusarr.length()==0) {
                        break;
                    }
                    for(int i=0;i<statusarr.length();i++) {
                        JSONObject jobj = statusarr.getJSONObject(i);
                        statuses.put(jobj);
                        if(!jobj.isNull("id")) {
                            maxid = jobj.getLong("id");
                        }
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
            System.out.println(statuses.length());
        } catch (Exception e) {

        }
        return statuses;
    }

    /*
    Returns a collection of the most recent Tweets posted by the user indicated by the
     screen_name or user_id parameters.
     */
    public static void main(String[] args) throws IOException {
        System.out.println(new StatusMessages().getStatuses("satheeshdannur1"));
    }

}
