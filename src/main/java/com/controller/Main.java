package com.controller;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static spark.Spark.*;


/**
 * Created by lior on 8/23/2017.
 */
public class Main {

    String val = "initial value";
    String localUrl = "http://localhost:8080";
    String instance1Url = "http://ec2-35-158-118-58.eu-central-1.compute.amazonaws.com:8080";
    String instance2Url = "http://ec2-54-93-82-181.eu-central-1.compute.amazonaws.com:8080";
    String instance3Url = "http://ec2-52-59-194-154.eu-central-1.compute.amazonaws.com:8080";

    private List<String> instancesUrls = Arrays.asList(instance1Url, instance2Url, instance3Url);



    public static void main(String[] args) {
        new Main().run();
    }

    public void run(){
        //staticFiles.location("/");
        port(8080);
        before((request, response) -> response.type("application/json"));
        get("/", "application/json", (req, res) -> {
            return val;
        });
        post("/", "application/json", (req, res) -> {
            if(!val.equals(req.body())) {
                val = req.body();
                instancesUrls.forEach(instance -> postRequestToInstance(instance, val));
            }
            return "Working";
        });
    }

    private String postRequestToInstance(String instanceUrl, String urlParameter){
        return sendRequestToInstance("POST", instanceUrl, urlParameter);
    }

    private String getRequestToInstance(String instanceUrl){
        return sendRequestToInstance("GET", instanceUrl, null);
    }

    private String sendRequestToInstance(String requestType, String instanceUrl, String urlParameters) {
        HttpURLConnection connection = null;

        try {
            //Create connection
            URL url = new URL(instanceUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(requestType);
            connection.setRequestProperty("Content-Type",
                    "application/json");

            if(urlParameters != null) {
                connection.setRequestProperty("Content-Length",
                        Integer.toString(urlParameters.getBytes().length));
            }
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            if(urlParameters != null) {
                wr.writeBytes(urlParameters);
            }

            wr.close();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
