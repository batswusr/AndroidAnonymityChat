package com.batsw.anonimitychat.mainScreen.addContact;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.batsw.anonimitychat.R;
import com.batsw.anonimitychat.appManagement.AppController;
import com.batsw.anonimitychat.mainScreen.entities.ContactEntity;
import com.batsw.anonimitychat.persistence.entities.DBContactEntity;
import com.batsw.anonimitychat.tor.bundle.TorConstants;

/**
 * Created by tudor on 4/15/2017.
 */

public class ContactAddActivity extends AppCompatActivity {

    private static final String LOG = ContactAddActivity.class.getSimpleName();

    private EditText mContactName, mContactAddress, mContactEmail;
    //    private EditText mContactNickname;
    private TextView mBackIcon, mEmailIcon;
    private TextView mChangeContactAvatar;
    private Button mAdd;

    private TextInputLayout mNameTextInputLayout, mAddressTextInputLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(LOG, "onCreate -> ENTER");
        setContentView(R.layout.contact_tab_add_contact);

        AppController.getInstanceParameterized(null).setChatControllerCurrentActivityContext(this);

        //Loading fontAwesome
        Typeface fontAwesome = Typeface.createFromAsset(getAssets(), "font_awesome/fontawesome.ttf");

        mContactName = (EditText) findViewById(R.id.name_contact_add);
//        mContactNickname = (EditText) findViewById(R.id.nickname_contact_add);

        mContactAddress = (EditText) findViewById(R.id.address_contact_add);
//        mContactEmail = (EditText) findViewById(R.id.email_contact_add);

//        mEmailIcon = (TextView) findViewById(R.id.contact_email_icon_add);
//        mEmailIcon.setTypeface(fontAwesome);

        mNameTextInputLayout = (TextInputLayout) findViewById(R.id.name_contact_add_textInputLayout);
        mAddressTextInputLayout = (TextInputLayout) findViewById(R.id.address_add_textInputLayout);


        mAdd = (Button) findViewById(R.id.contact_add_button);
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(LOG, "mAdd.onClick -> ENTER");

                final String enteredName = mContactName.getText().toString();
                if (enteredName.length() > 0) {
                    final String enteredAddress = mContactAddress.getText().toString();

                    if (enteredAddress.length() > 0 && enteredAddress.length() == 16) {
                        String myAddressComplete = enteredAddress + TorConstants.TOR_ADDRESS_SUFFIX;

                        DBContactEntity contact = new DBContactEntity();
                        contact.setAddress(myAddressComplete);
                        contact.setName(enteredName);
                        contact.setNickName(mContactName.getText().toString());
//                    contact.setNickName(mContactNickname.getText().toString());
//                    contact.setEmail(mContactEmail.getText().toString());

                        if (validateNewContact(contact)) {

                            boolean insertSuccessfull = AppController.getInstanceParameterized(null).addNewContact(contact);

                            if (insertSuccessfull) {
                                ContactEntity newContactEntity = new ContactEntity(
                                        (contact.getNickName() == null || contact.getNickName().isEmpty()) ? contact.getName() : contact.getNickName(),
                                        contact.getSessionId());
                                AppController.getInstanceParameterized(null).addNewContactToTab(newContactEntity);
                                finish();
                            }
                        } else {
                            mAddressTextInputLayout.setErrorEnabled(true);
                            mAddressTextInputLayout.setError("Address must have 16 characters!");
                        }
                    } else {
                        mAddressTextInputLayout.setErrorEnabled(true);
                        mAddressTextInputLayout.setError("Address must have 16 characters!");
                    }
                } else {
                    mNameTextInputLayout.setErrorEnabled(true);
                    mNameTextInputLayout.setError("Name cannot be empty!");
                }

                Log.i(LOG, "mAdd.onClick -> LEAVE");
            }
        });

        mBackIcon = (TextView) findViewById(R.id.contact_back_icon_add);
        mBackIcon.setTypeface(fontAwesome);
        mBackIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(LOG, "mBackIcon.setOnClickListener -> ENTER");
                finish();
                Log.i(LOG, "mBackIcon.setOnClickListener -> LEAVE");
            }
        });

        //TODO: to add a listener that allows loading pictures for avatars
//        mChangeContactAvatar = (TextView) findViewById(R.id.change_avatar_contact_add);
//        mChangeContactAvatar.setTypeface(fontAwesome);

        Log.i(LOG, "onCreate -> LEAVE");
    }

    private boolean validateNewContact(DBContactEntity contactEntity) {
        Log.i(LOG, "validateNewContact -> ENTER contactEntity=" + contactEntity);
        boolean retVal = false;

        if (!(contactEntity.getAddress().length() == 22)) {
            retVal = false;
        } else if (contactEntity.getName() == null) {
            retVal = false;
        } else if (contactEntity.getNickName() == null) {
            contactEntity.setNickName(contactEntity.getName());
        } else {
            retVal = true;
        }

        Log.i(LOG, "validateNewContact -> LEAVE retVal=" + retVal);
        return retVal;
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i(LOG, "onPause -> ENTER");

        Log.i(LOG, "Do nothing !");

        Log.i(LOG, "onPause -> LEAVE");
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.i(LOG, "onStart -> ENTER");

        Log.i(LOG, "Do nothing !");

        Log.i(LOG, "onStart -> LEAVE");
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.i(LOG, "onStop -> ENTER");

        Log.i(LOG, "Do nothing !");

        Log.i(LOG, "onStop -> LEAVE");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.i(LOG, "onDestroy -> ENTER");

        Log.i(LOG, "Do nothing !");

        Log.i(LOG, "onDestroy -> LEAVE");
    }

    /**
     * Creating Intent for the activity of this class
     *
     * @param context
     * @return Intent
     */
    public static Intent makeIntent(Context context) {
        Log.i(LOG, "makeIntent -> ENTER context=" + context);

        Intent retVal = new Intent(context, ContactAddActivity.class);

        Log.i(LOG, "makeIntent -> LEAVE retVal=" + retVal);
        return retVal;
    }
}
