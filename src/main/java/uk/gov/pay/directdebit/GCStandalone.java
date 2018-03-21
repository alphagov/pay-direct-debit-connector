package uk.gov.pay.directdebit;

import com.gocardless.GoCardlessClient;
import com.gocardless.resources.Customer;
import com.squareup.okhttp.OkHttpClient;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.net.Proxy;
import uk.gov.pay.directdebit.payments.clients.GoCardlessClientWrapper;

public class GCStandalone {

    private static void httpsRequest(boolean useProxy) {
        JSONObject customer = new JSONObject();
        customer.put("email", "belinda@example.com");
        customer.put("given_name", "Belinda");
        customer.put("family_name", "Testing");

        JSONObject customers = new JSONObject();
        customers.put("customers", customer);

	    try {
            HttpPost post = new HttpPost("https://api-sandbox.gocardless.com/customers");
	        StringEntity params =new StringEntity(customers.toString());
            post.addHeader("content-type", "application/json");
            post.addHeader("accept", "application/json");
            post.addHeader("GoCardless-Version", "2015-07-06");
            post.addHeader("Authorization", "Bearer " + System.getenv("GDS_DIRECTDEBIT_CONNECTOR_GOCARDLESS_ACCESS_TOKEN"));
            post.setEntity(params);

            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

            if (useProxy) {
                String PROXY_HOST = System.getenv("HTTPS_PROXY_HOST");
                int PROXY_PORT = Integer.parseInt(System.getenv("HTTP_PROXY_PORT"));
                httpClientBuilder.setProxy(new HttpHost(PROXY_HOST, PROXY_PORT));
            }

            HttpClient httpClient = httpClientBuilder.build();
            HttpResponse response = httpClient.execute(post);

            System.out.println("Successfully posted json");
            System.out.println(response.toString());
	    } catch (Exception ex) {
	        System.out.println("Failed to post request");
	        System.out.println(ex.getMessage());
	    }
    }

/*
    private static void gcRequest_old() {
        System.out.println("Creating gocardless sandbox client");

        try {
            GoCardlessClient client = GoCardlessClient.create(
                    // We recommend storing your access token in an
                    // environment variable for security, but you could
                    // include it as a string directly in your code
                    System.getenv("GDS_DIRECTDEBIT_CONNECTOR_GOCARDLESS_ACCESS_TOKEN"),
                    // Change me to LIVE when you're ready to go live
                    GoCardlessClient.Environment.SANDBOX
            );
            Object httpClient = FieldUtils.readField(client, "httpClient", true);
            OkHttpClient rawClient = (OkHttpClient) FieldUtils.readField(httpClient, "rawClient", true);
            String PROXY_HOST = System.getenv("HTTPS_PROXY_HOST");
            int PROXY_PORT = Integer.parseInt(System.getenv("HTTP_PROXY_PORT"));
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, PROXY_PORT));
            rawClient.setProxy(proxy);
            System.out.println("Creating customer:");
            Customer execute = client.customers().create().withGivenName("a").withFamilyName("b").execute();
            System.out.println("Created customer " + execute.getId());

            for (Customer customer : client.customers().all().execute()) {
                System.out.println(customer.getId() + " " +  customer.getGivenName() + " " + customer.getFamilyName());
            }

            /*System.out.println("Before redirect flow implementation");

            RedirectFlow redirectFlow = client.redirectFlows().create()
                    .withDescription("Cider Barrels") // This will be shown on the payment pages.
                    .withSessionToken("dummy_session_token") // Not the access token
                    .withSuccessRedirectUrl("https://developer.gocardless.com/example-redirect-uri/")
                    // Optionally, prefill customer details on the payment page
                    .withPrefilledCustomerGivenName("Tim")
                    .withPrefilledCustomerFamilyName("Rogers")
                    .withPrefilledCustomerEmail("tim@gocardless.com")
                    .withPrefilledCustomerAddressLine1("338-346 Goswell Road")
                    .withPrefilledCustomerCity("London")
                    .withPrefilledCustomerPostalCode("EC1V 7LQ")
                    .execute();

            // Hold on to this ID - you'll need it when you
            // "confirm" the redirect flow later
            System.out.println(redirectFlow.getId());
            System.out.println(redirectFlow.getRedirectUrl());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
*/
    private static void gcRequest() {
        System.out.println("Creating gocardless sandbox client with new client");

        try {
            GoCardlessClient.Builder builder = GoCardlessClient.newBuilder(System.getenv("GDS_DIRECTDEBIT_CONNECTOR_GOCARDLESS_ACCESS_TOKEN"))
                    .withEnvironment(GoCardlessClient.Environment.SANDBOX);
            builder.withProxy(new Proxy(Proxy.Type.HTTP,
                    new InetSocketAddress(System.getenv("HTTPS_PROXY_HOST"), Integer.parseInt(System.getenv("HTTP_PROXY_PORT")))));

            GoCardlessClientWrapper clientWrapper = new GoCardlessClientWrapper(builder.build());
            clientWrapper.listCustomers();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
//            httpsRequest(args.length == 1);
            gcRequest();
        } catch (Exception e) {
            System.out.println("Failed ....");
            System.out.println(e.getMessage());
        }
    }
}
