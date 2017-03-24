/* TweetTracker. Copyright (c) Arizona Board of Regents on behalf of Arizona State University
 * @author shamanth
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import support.OAuthTokenSecret;
import utils.OAuthUtils;

public class OAuthExample
{        
    public OAuthTokenSecret GetUserAccessKeySecret()
    {
        try {
            OAuthConsumer consumer = new CommonsHttpOAuthConsumer(OAuthUtils.CONSUMER_KEY,OAuthUtils.CONSUMER_SECRET);
            OAuthProvider provider = new DefaultOAuthProvider(OAuthUtils.REQUEST_TOKEN_URL, OAuthUtils.ACCESS_TOKEN_URL, OAuthUtils.AUTHORIZE_URL);
            String authUrl = provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);
            System.out.println("Now visit:\n" + authUrl + "\n and grant this app authorization");
            System.out.println("Enter the PIN code and hit ENTER when you're done:");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String pin = br.readLine();
            System.out.println("Fetching access token from Twitter");
            provider.retrieveAccessToken(consumer,pin);
            String accesstoken = consumer.getToken();
            String accesssecret  = consumer.getTokenSecret();
            OAuthTokenSecret tokensecret = new OAuthTokenSecret(accesstoken,accesssecret);
            return tokensecret;
        } catch (OAuthNotAuthorizedException ex) {
                ex.printStackTrace();
        } catch (OAuthMessageSignerException ex) {
                ex.printStackTrace();
        } catch (OAuthExpectationFailedException ex) {
                ex.printStackTrace();
        } catch (OAuthCommunicationException ex) {
                ex.printStackTrace();
        } catch(IOException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args)
    {
        OAuthExample aue = new OAuthExample();
        OAuthTokenSecret tokensecret = aue.GetUserAccessKeySecret();
        System.out.println(tokensecret.toString());
    }
}
