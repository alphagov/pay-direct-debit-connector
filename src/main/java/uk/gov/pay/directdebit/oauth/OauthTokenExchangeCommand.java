package uk.gov.pay.directdebit.oauth;

import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;

public class OauthTokenExchangeCommand extends Command {
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

    public OauthTokenExchangeCommand() {
        super("oauth-exchange", "Initiate oauth flow with gocardless");
    }


    @Override
    public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
        String oauthCode = namespace.getString("code");
        
        RequestBody requestBody = new FormBody.Builder()
                .add("client_id", clientID)
                .add("client_secret", clientSecret)
                .add("redirect_uri", redirectUrl)
                .add("scope", "read_write")
                .add("grant_type", "authorization_code")
                .add("code", oauthCode)
                .add("initial_view", "signup")
                .add("access_type", "offline")
                .build();

        System.out.println(requestBody);
        Request request = new Request.Builder()
                .url("https://connect-sandbox.gocardless.com/oauth/access_token")
                .post(requestBody)
                .build();
        Response response = client.newCall(request).execute();
        System.out.println(response.body().string());
    }

    @Override
    public void configure(Subparser subparser) {
        subparser.addArgument("code")
                .nargs("?")
                .help("oauth code from initial authorisation");

    }


//    public String loggedIn(String oauthCode) throws IOException {
//        String accessToken = getAccessToken(oauthCode);
//        System.out.println("Save the access token " +
//                accessToken + "to your database.");
//        return "Greetings!";
//    }
}
