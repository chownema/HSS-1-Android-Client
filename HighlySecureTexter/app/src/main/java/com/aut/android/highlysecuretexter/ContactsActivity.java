package com.aut.android.highlysecuretexter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.aut.android.highlysecuretexter.Controller.Client;
import com.aut.android.highlysecuretexter.Controller.Network;
import com.aut.android.highlysecuretexter.Controller.Utility;

import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity implements View.OnClickListener {

    private Client client;
    private Button refreshButton;
    private ListView contactsListView;
    private ArrayAdapter<String> adapter;
    ArrayList<String> contactsList = new ArrayList<String>() {{
        add("0211245734"); //mine
        add("0212547306"); //sonic
        add("021256332"); //adam
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        // Get client data from previous activity intent
        client = (Client) getIntent().getSerializableExtra("client");

        // TODO: Get active clients

        // Init List view
        contactsListView = (ListView) findViewById(R.id.contacts_list_view);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactsList);
        contactsListView.setAdapter(adapter);

        // Init Buttons
        refreshButton = (Button) findViewById(R.id.refresh_contacts_button);
        refreshButton.setOnClickListener(this);

        // Init on click list
//        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View view,
//                                    int position, long id) {
//
//                Toast.makeText(getApplicationContext(), contactsList.get(position) + "CLICKED", Toast.LENGTH_SHORT).show();
//                Intent myIntent = new Intent(ContactsActivity.this, MessagingActivity.class);
//                myIntent.putExtra("number", contactsList.get(position)); //Optional parameters
//                ContactsActivity.this.startActivity(myIntent);
//            }
//        });
    }

    @Override
    public void onClick(View view) {
        if (view == refreshButton)
        {
            // Get contact data from rest endpoint
            String[] contacts = Network.updateContacts(client);
            // Clear list
            adapter.clear();
            // Insert items
            for(int i = 0; i < contacts.length; i++)
                adapter.insert(contacts[i], i);
            // Notify data changed
            adapter.notifyDataSetChanged();
            // Notify client of update
            Toast.makeText(getApplicationContext(), "Contacts Refreshed", Toast.LENGTH_SHORT).show();
        }
    }
}
