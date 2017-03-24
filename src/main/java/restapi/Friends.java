package restapi;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
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

/**
 * Created by satheesh on 21/3/17.
 */
public class Friends {

    public OAuthConsumer getConsumer() {
        OAuthConsumer consumer = new DefaultOAuthConsumer(OAuthUtils.CONSUMER_KEY, OAuthUtils.CONSUMER_SECRET);
        consumer.setTokenWithSecret(OAuthUtils.ACCESS_TOKEN, OAuthUtils.ACCESS_TOKEN_SECRET);
        return consumer;
    }

    public JSONArray getFriends(String username) throws IOException {
        BufferedReader bRead = null;
        JSONArray friends = new JSONArray();
        try {
            System.out.println("Processing friends of "+username);
            long cursor = -1;
            while(true) {
                if(cursor==0) {
                    break;
                }
                // Step 1: Create the APi request using the supplied username
                URL url = new URL("https://api.twitter.com/1.1/friends/list.json?screen_name="+username+"&cursor="+cursor);
                HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                huc.setReadTimeout(5000);
                OAuthConsumer consumer = getConsumer();
                //Step 2: Sign the request using the OAuth Secret
                consumer.sign(huc);
                huc.connect();
                // Step 4: Retrieve the friends list from Twitter
                bRead = new BufferedReader(new InputStreamReader((InputStream) huc.getContent()));
                StringBuilder content = new StringBuilder();
                String temp = "";
                while((temp = bRead.readLine())!=null) {
                    content.append(temp);
                }
                try {
                    JSONObject jobj = new JSONObject(content.toString());
                    // Step 5: Retrieve the token for the next request
                    cursor = jobj.getLong("next_cursor");
                    JSONArray userlist = jobj.getJSONArray("users");
                    if(userlist.length()==0)
                        break;
                    System.out.println("Number of users   " + userlist.length());
                    for(int i=0;i<userlist.length();i++) {
                        friends.put(userlist.get(i));
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                huc.disconnect();
            }
        } catch (Exception e) {

        }
        return friends;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(new Friends().getFriends("satheeshdannur1"));
    }
}
