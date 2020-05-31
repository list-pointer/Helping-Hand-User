package com.spitmca.helpinghands;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrationGuardian extends AppCompatActivity {

    FirebaseAuth fAuth;
    FirebaseFirestore fstore;

    String user_name,user_address,user_gender,user_disabilities,user_phone;

    EditText guardianName,guardianAddress,guardianPhone1,guardianPhone2;
    String guardian_name,guardian_address,guardian_phone1,guardian_phone2;

    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_guardian);

        fAuth=FirebaseAuth.getInstance();
        fstore=FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        user_name=intent.getStringExtra("user_name");
        user_phone=intent.getStringExtra("user_phone");
        user_address=intent.getStringExtra("user_address");
        user_gender=intent.getStringExtra("user_gender");
        user_disabilities=intent.getStringExtra("user_disabilities");

        guardianName=(EditText)findViewById(R.id.et_guardianname);
        guardianAddress=(EditText)findViewById(R.id.et_guardianaddress);
        guardianPhone1=(EditText)findViewById(R.id.et_guardiannumber1);
        guardianPhone2=(EditText)findViewById(R.id.et_guardiannumber2);

        progressDialog=new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Please wait");

        Button register = (Button)findViewById(R.id.registration_finish);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog.show();

                guardian_name=guardianName.getText().toString().trim();
                guardian_address=guardianAddress.getText().toString().trim();
                guardian_phone1=guardianPhone1.getText().toString().trim();
                guardian_phone2=guardianPhone2.getText().toString().trim();

                if(guardian_name.isEmpty())
                    guardian_name="None";
                if(guardian_address.isEmpty())
                    guardian_address="None";
                if(guardian_phone1.isEmpty())
                    guardian_phone1="None";
                if(guardian_phone2.isEmpty())
                    guardian_phone2="None";


//                Toast.makeText(getApplicationContext(),"Name : "+user_name+
//                                " Address : "+ user_address+
//                                " Gender : "+user_gender+
//                                " Phone : "+user_phone+
//                                " Disabilities : "+user_disabilities+
//                                " GName : "+guardian_name+
//                                " GAddress : "+guardian_address+
//                                " gphone1 : "+guardian_phone1+
//                                " gphone2 : "+guardian_phone2
//                        ,Toast.LENGTH_LONG).show();
                String uid= fAuth.getCurrentUser().getUid();
                DocumentReference documentReference = fstore.collection("users").document(uid);
                Map<String,Object> user =new HashMap<>();
                user.put("user_name",user_name);
                user.put("user_phone",user_phone);
                user.put("user_address",user_address);
                user.put("user_gender",user_gender);
                user.put("user_disabilities",user_disabilities);
                user.put("guardian_name",guardian_name);
                user.put("guardian_phone1",guardian_phone1);
                user.put("guardian_phone2",guardian_phone2);
                user.put("guardian_address",guardian_address);
                user.put("user_profileImage",null);
                user.put("request_station",null);
                user.put("request_latitude",null);
                user.put("request_longitude",null);
                user.put("request_state","idle");
                user.put("assistant_id",null);
                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        progressDialog.dismiss();

                        Intent intent1 = new Intent(RegistrationGuardian.this,Search.class);
                        startActivity(intent1);
                    }
                });

            }
        });

    }
}
