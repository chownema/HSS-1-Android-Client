package com.aut.android.highlysecuretexter.Controller;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

/**
 * Created by MI on 23/09/16.
 */

public class HttpHelper {


    Activity currentActivity;
    private static Context context;


    public HttpHelper(Context c)
    {
        this.context = c;
    }

    public String post()
    {
        AsyncHttpClient client = new AsyncHttpClient();
        String URL = "http://156.62.62.37:8080/PKAServer/webresources/pka/request/012556332";
        String URL2 = "http://172.28.41.238:8080/PKAServer/webresources/pka/request/123456"; //MAC
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

        return "";
    }

}
