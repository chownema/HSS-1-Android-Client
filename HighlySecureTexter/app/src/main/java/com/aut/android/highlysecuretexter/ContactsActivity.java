package com.aut.android.highlysecuretexter;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
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
import com.aut.android.highlysecuretexter.Controller.Contact;
import com.aut.android.highlysecuretexter.Controller.Network;
import com.aut.android.highlysecuretexter.Controller.Utility;

import java.util.ArrayList;

public class ContactsActivity extends Activity implements View.OnClickListener {

    private Client client;
    private static String[] contacts = null;
    private Button refreshButton;
    private ListView contactsListView;
    private ArrayAdapter<String> adapter;
    ArrayList<String> contactsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        // Get client data from previous activity intent
        client = (Client) getIntent().getSerializableExtra("client");

        // Init List view
        contactsListView = (ListView) findViewById(R.id.contacts_list_view);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactsList);
        contactsListView.setAdapter(adapter);

        // Update contacts from Server
        //updateContacts();

        // Init Buttons
        refreshButton = (Button) findViewById(R.id.refresh_contacts_button);
        refreshButton.setOnClickListener(this);

//         Init on click list
        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent myIntent = new Intent(ContactsActivity.this, MessagingActivity.class);
                myIntent.putExtra("number", contactsList.get(position)); // Number of contact
                myIntent.putExtra("client", client); // Client object
                ContactsActivity.this.startActivity(myIntent);
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == refreshButton) {
            Toast.makeText(getApplicationContext(), "Requesting Contacts", Toast.LENGTH_SHORT).show();
            updateContacts();
        }
    }

    private void updateContacts() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try
                {
                    contacts = null;
                    // Get contact data from rest endpoint
                    String[] contactData = Network.updateContacts(client);

                    if(contactData != null) {
                        contacts = contactData;
                    }
                } catch (Exception e) {Log.e(e.toString(), "shit fucked up");}
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {

                if(contacts != null) {
                    // Clear list
                    adapter.clear();
                    // Insert items
                    for(int i = 0; i < contacts.length; i++) {
                        // Add contact to list
                        adapter.insert(contacts[i], i);
                        // Add contact to contact list
                        if(!client.getContacts().containsKey(contacts[i]));
                            client.getContacts().put(contacts[i], new Contact(contacts[i]));
                    }
                    // Notify data changed
                    adapter.notifyDataSetChanged();
                    // Notify client of update
                    Toast.makeText(getApplicationContext(), "Contacts Refreshed", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getApplicationContext(), "Unable to refresh contacts", Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }
}
