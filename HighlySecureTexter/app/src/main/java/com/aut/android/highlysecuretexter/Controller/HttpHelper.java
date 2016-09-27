package com.aut.android.highlysecuretexter.Controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.aut.android.highlysecuretexter.MainActivity;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.lang.ref.WeakReference;

import cz.msebera.android.httpclient.Header;

/**
 * Created by MI on 23/09/16.
 */

public class HttpHelper {

    // Get Context of Activity Class is used
    private static Context context;
    String response;

    public HttpHelper(Context c)
    {
        this.context = c;

    }

    public void post(String urlResource) {
        AsyncHttpClient client = new AsyncHttpClient();
        String URL = "http://156.62.62.37:8080/PKAServer/webresources/pka/request/012556332";
        String ip = "192.168.0.6"; // home
        String ip2 = "172.28.41.238";
        String URL2 = "http://"+ip2+":8080/PKAServerLatest2/webresources/pka/" + urlResource; //MAC

        client.post(URL2, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
                Toast.makeText(context, "ATTEMPTING", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Toast.makeText(context, "SUCCESS", Toast.LENGTH_SHORT).show();
                // Get the Key and Successful response from the server
                Toast.makeText(context, "Response :: " + new String(responseBody), Toast.LENGTH_SHORT).show();
                setResponse(new String(responseBody));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(context, "FAIL", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });


    }



    public void setResponse(String v)
    {
        response = v;
    }

    public String getResponse()
    {
        String pResponse = "";
        // If response exists then copy return proxy
        // value then reset response value
        if(response != "" || response != null)
        {
            pResponse = response;
            response = "";
        }

        return pResponse;
    }

}
