package com.aut.android.highlysecuretexter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.aut.android.highlysecuretexter.Controller.HttpHelper;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button requestEmailButton;
    private HttpHelper httpHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // Set buttons
        requestEmailButton = (Button) findViewById(R.id.email_request_button);
        requestEmailButton.setOnClickListener(this);

        // Set up HTTP Helper
        httpHelper = new HttpHelper(this);


    }

    @Override
    public void onClick(View view) {
        // If Requesting email
        if (view == requestEmailButton)
        {
            httpHelper.post();

        }
    }
}
