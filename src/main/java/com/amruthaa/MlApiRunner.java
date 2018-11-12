package com.amruthaa;

import com.amruthaa.models.UserInput;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class MlApiRunner {

    public static String sentimentalAnalysisApi(String urlString, UserInput data) throws Exception {
        byte[] encoded = Files.readAllBytes(Paths.get(data.getDataUrl()));
        StringBuilder url = new StringBuilder(urlString);
        URL obj = new URL(url.toString());
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Server:API");
        con.setRequestProperty("Content-Type","text/plain");
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(new String(encoded, Charset.defaultCharset()));
        wr.flush();
        wr.close();
        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url.toString());
        System.out.println("Response Code : " + responseCode);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response;
        response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    public static String imageClassificationApi(String urlString, UserInput data) throws Exception {
        StringBuilder url = new StringBuilder(urlString);
        URL obj = new URL(url.toString());
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Server:API");
        con.setRequestProperty("Content-Type","application/json");
        con.setDoOutput(true);
        Gson gson = new Gson();
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(gson.toJson(data));
        wr.flush();
        wr.close();
        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url.toString());
        System.out.println("Response Code : " + responseCode);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response;
        response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }
}
