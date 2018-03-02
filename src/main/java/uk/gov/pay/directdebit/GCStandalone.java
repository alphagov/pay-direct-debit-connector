package uk.gov.pay.directdebit;

import com.gocardless.GoCardlessClient;
import com.gocardless.resources.Customer;
import com.gocardless.resources.RedirectFlow;

import java.util.List;
import java.util.Arrays;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
//import org.json.simple.JSONObject;

public class GCStandalone {
/*
    private static void httpsRequest() {
        HttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead

        JSONObject customer = new JSONObject();
        customer.put("email", "user@example.com");
        customer.put("given_name", "Frank");
        customer.put("family_name", "Osborne");

        JSONObject customers = new JSONObject();
        customers.put("customers", customer);

	    try {
            HttpPost post = new HttpPost("https://api-sandbox.gocardless.com/customers");
	        StringEntity params =new StringEntity(customers.toString());
            post.addHeader("content-type", "application/json");
            post.addHeader("accept", "application/json");
            post.addHeader("GoCardless-Version", "2015-07-06");
            post.addHeader("authorization", System.getenv("GDS_DIRECTDEBIT_CONNECTOR_GOCARDLESS_ACCESS_TOKEN"));
            post.setEntity(params);

	        HttpResponse response = httpClient.execute(post);

            System.out.println("Successfully posted json");
            System.out.println(response.toString());
	    } catch (Exception ex) {
	        System.out.println("Failed to post request");
	        System.out.println(ex.getMessage());
	    }
    }
*/
    private static void gcRequest() {
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

            System.out.println("List of clients:");
            List<Customer> customers = client.customers().list().execute().getItems();
            customers.forEach (customer-> {
                System.out.println((com.gocardless.resources.Customer)customer);
            });

            System.out.println(Arrays.toString(customers.toArray()));

            return;
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
            System.out.println(redirectFlow.getRedirectUrl());*/
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            //httpsRequest();
            gcRequest();
        } catch (Exception e) {
            System.out.println("Failed ....");
            System.out.println(e.getMessage());
        }
    }
}
