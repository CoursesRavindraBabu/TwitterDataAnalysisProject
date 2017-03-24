package restapi;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
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
public class Profile {

    public OAuthConsumer getConsumer() {
        OAuthConsumer consumer = new DefaultOAuthConsumer(OAuthUtils.CONSUMER_KEY, OAuthUtils.CONSUMER_SECRET);
        consumer.setTokenWithSecret(OAuthUtils.ACCESS_TOKEN, OAuthUtils.ACCESS_TOKEN_SECRET);
        return consumer;
    }

    public JSONObject getProfile(String username) throws IOException {
        BufferedReader bRead = null;
        JSONObject profile = null;
        try {
            System.out.println("Processing profile of " + username);
            URL url = new URL("https://api.twitter.com/1.1/users/show.json?screen_name=" + username);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setReadTimeout(5000);
            OAuthConsumer consumer = getConsumer();
            consumer.sign(huc);
            huc.connect();
            System.out.println(huc.getResponseCode());
            StringBuilder content = new StringBuilder();
            bRead = new BufferedReader(new InputStreamReader((InputStream) huc.getContent()));
            String temp = "";
            while ((temp = bRead.readLine()) != null) {
                    content.append(temp);
            }
            huc.disconnect();
            try {
                profile = new JSONObject(content.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return profile;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return profile;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(new Profile().getProfile("satheeshdannur1"));
    }
}
