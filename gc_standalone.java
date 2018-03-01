package com.gcintegration;

import com.gocardless.GoCardlessClient;
import com.gocardless.resources.Customer;

import java.util.List;
import java.util.Arrays;

public class GCIntegration {
    public static void main(String[] args) {
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
        } catch (Exception e) {
            System.out.println("Unable to create GC client");
            System.out.println e.getMessage();
        }

        System.out.println("List of clients, empty array is good, it shows that we are authenticated");

        try {
            List<Customer> customers = client.customers().list().execute().getItems();
            System.out.println(Arrays.toString(customers.toArray()));
        } catch (Exception e) {
            System.out.println("Unable to list customers");
            System.out.println e.getMessage();
        }

        System.out.println("Before redirect flow implementation");

        try {
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
            System.out.println("Unable to create redirect flow");
            System.out.println e.getMessage();
        }
    }
}
