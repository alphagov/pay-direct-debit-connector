package uk.gov.pay.directdebit.oauth;

import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import okhttp3.OkHttpClient;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;

public class OauthInitatorCommand extends Command {
    private OkHttpClient client = new OkHttpClient();

    //'https://connect-sandbox.gocardless.com/oauth/authorize?client_id=foo&redirect_uri=https%3A%2F%2Fbbda790a.ngrok.io%2Fdone&access_type=offline&response_type=code&initial_view=signup&scope=read_write'
    private static final String authoriseRedirectUrl =
            "https://connect-sandbox.gocardless.com/oauth/authorize";
    private static final String accessTokenUrl =
            "https://connect-sandbox.gocardless.com/oauth/access_token";
    private static final String redirectUrl =  "https://selfservice.test.pymnt.uk/oauth/complete";
    // Ngrok url here. See: https://ngrok.com/
    private static final String clientID = System.getenv("GOCARDLESS_CLIENT_ID");
    private static final String clientSecret = System.getenv("GOCARDLESS_CLIENT_SECRET");

    public OauthInitatorCommand() {
        super("oauth", "Initiate oauth flow with gocardless");
    }


    @Override
    public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
        //String encodedURL = URLEncoder.encode(redirectUrl, "UTF-8" );
        String oauthUrl = UriBuilder
                .fromPath(authoriseRedirectUrl)
                .queryParam("client_id", clientID)
                .queryParam("redirect_uri", redirectUrl)
                .queryParam("scope", "read_write")
                .queryParam("response_type", "code")
                .queryParam("initial_view", "signup")
                .queryParam("access_type", "offline")
                .toString();
        
        System.out.println("Redirect to: " + oauthUrl);
    }


    @Override
    public void configure(Subparser subparser) {
        
    }


//    public String loggedIn(String oauthCode) throws IOException {
//        String accessToken = getAccessToken(oauthCode);
//        System.out.println("Save the access token " +
//                accessToken + "to your database.");
//        return "Greetings!";
//    }
}
