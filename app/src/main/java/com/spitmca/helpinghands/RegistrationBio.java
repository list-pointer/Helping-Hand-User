package com.spitmca.helpinghands;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

public class RegistrationBio extends AppCompatActivity {

    EditText userName,userAddress;
    RadioGroup rgUserGender;
    RadioButton rbMale,rbFemale;
    CheckBox cbLimp,cbBlind,cbMute,cbPartiallyBlind,cbDeaf;
    String user_name,user_address,user_gender,user_disabilities,user_phone;

    ArrayList<String> d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_bio);

        Intent intent = getIntent();
        user_phone=intent.getStringExtra("user_phone");

        userName=(EditText)findViewById(R.id.et_name);
        userAddress=(EditText)findViewById(R.id.et_address);

        cbLimp=(CheckBox)findViewById(R.id.cb_limp);
        cbBlind=(CheckBox)findViewById(R.id.cb_blind);
        cbMute=(CheckBox)findViewById(R.id.cb_mute);
        cbPartiallyBlind=(CheckBox)findViewById(R.id.cb_partiallyblind);
        cbDeaf=(CheckBox)findViewById(R.id.cb_deaf);

        rgUserGender=(RadioGroup)findViewById(R.id.rg_gender);
        rbMale=(RadioButton)findViewById(R.id.rb_male);
        rbFemale=(RadioButton)findViewById(R.id.rb_female);

        d=new ArrayList<String>();
        user_disabilities="";
        cbLimp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cbLimp.isChecked())
                {
                    d.add("Limp");
                    //user_disabilities=user_disabilities+" Limp ";
                }
                else
                    d.remove("Limp");
            }
        });

        cbBlind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cbBlind.isChecked())
                {
                    d.add("Blind");
                    //user_disabilities=user_disabilities+" Blind ";
                }
                else
                    d.remove("Blind");
            }
        });

        cbMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cbMute.isChecked())
                {
                    d.add("Mute");
                    //user_disabilities=user_disabilities+" Mute ";
                }
                else
                    d.remove("Mute");
            }
        });

        cbPartiallyBlind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cbPartiallyBlind.isChecked())
                {
                    d.add("Partially Blind");
                    //user_disabilities=user_disabilities+" Partially Blind ";
                }
                else
                    d.remove("PartiallyBlind");
            }
        });

        cbDeaf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cbDeaf.isChecked())
                {
                    d.add("Deaf");
                    //user_disabilities=user_disabilities+" Deaf ";
                }
                else
                    d.remove("Deaf");
            }
        });

        Button button = (Button) findViewById(R.id.registration_next);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(d.size()!=0)
                {
                    user_disabilities="";
                    for(int i=0;i<d.size();i++)
                    {
                        user_disabilities=user_disabilities+" "+d.get(i)+" ";
                    }
                }
                else
                {
                    user_disabilities="None";
                }
                //Toast.makeText(getApplicationContext(),user_disabilities,Toast.LENGTH_SHORT).show();

                user_name=userName.getText().toString().trim();
                if (user_name.isEmpty()) {
                    userName.setError("Enter the Name");
                    userName.requestFocus();
                    return;
                }

                user_address=userAddress.getText().toString().trim();
                if (user_address.isEmpty()) {
                    userAddress.setError("Enter the Address");
                    userAddress.requestFocus();
                    return;
                }

                int r = -1;
                r = rgUserGender.getCheckedRadioButtonId();
                if(r==-1) {
                    Toast.makeText(getApplicationContext(), "Please select gender", Toast.LENGTH_SHORT).show();
                    return;
                }
                RadioButton g = (RadioButton)findViewById(r);
                if(g==rbMale)
                    user_gender="Male";
                else if(g==rbFemale)
                    user_gender="Female";


                Intent intent = new Intent(RegistrationBio.this, RegistrationGuardian.class);

                intent.putExtra("user_name",user_name);
                intent.putExtra("user_address",user_address);
                intent.putExtra("user_gender",user_gender);
                intent.putExtra("user_phone",user_phone);
                intent.putExtra("user_disabilities",user_disabilities);

                startActivity(intent);
            }
        });

    }
}
