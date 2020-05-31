package com.spitmca.helpinghands;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firestore.v1.StructuredQuery;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Callback;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProfileActivity extends AppCompatActivity {

    FirebaseAuth fAuth;
    FirebaseFirestore fstore;
    FirebaseStorage fstorage;
    StorageReference storageReference;

    String uid;
    EditText userName,userPhone,userAddress,guardianName,guardianAddress,guardianPhone1,guardianPhone2;
    RadioGroup rgUserGender;
    RadioButton rbMale,rbFemale;
    CheckBox cbLimp,cbBlind,cbMute,cbPartiallyBlind,cbDeaf;
    ImageView profileImage;
    //ProgressBar progressBar;
    ProgressDialog progressDialog;

    String user_name,user_address,user_gender,user_disabilities,user_phone,guardian_name,guardian_address,guardian_phone1,guardian_phone2;
    ArrayList<String> d;

    private static final int GalleryPick=1;
    Uri profileImageUri;
    String downloadUrl;

    Button save;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        fAuth = FirebaseAuth.getInstance();
        fstore=FirebaseFirestore.getInstance();
        fstorage=FirebaseStorage.getInstance();
        storageReference=fstorage.getReference().child("UserProfileImages");

        uid=fAuth.getCurrentUser().getUid();

        userName=(EditText)findViewById(R.id.profile_et_name);
        userPhone=(EditText)findViewById(R.id.profile_et_number);
        userAddress=(EditText)findViewById(R.id.profile_et_address);
        guardianName=(EditText)findViewById(R.id.profile_et_guardianname);
        guardianAddress=(EditText)findViewById(R.id.profile_et_guardianaddress);
        guardianPhone1=(EditText)findViewById(R.id.profile_et_guardiannumber1);
        guardianPhone2=(EditText)findViewById(R.id.profile_et_guardiannumber2);

        cbLimp=(CheckBox)findViewById(R.id.profile_cb_limp);
        cbBlind=(CheckBox)findViewById(R.id.profile_cb_blind);
        cbMute=(CheckBox)findViewById(R.id.profile_cb_mute);
        cbPartiallyBlind=(CheckBox)findViewById(R.id.profile_cb_partiallyblind);
        cbDeaf=(CheckBox)findViewById(R.id.profile_cb_deaf);

        rgUserGender=(RadioGroup)findViewById(R.id.profile_rg_gender);

        rbMale=(RadioButton)findViewById(R.id.profile_rb_male);
        rbFemale=(RadioButton)findViewById(R.id.profile_rb_female);

        profileImage=(ImageView)findViewById(R.id.profile_image);

        //progressBar=(ProgressBar)findViewById(R.id.progressbar);
        save=(Button)findViewById(R.id.profile_save);

        d=new ArrayList<String>();
        user_disabilities="";

        progressDialog=new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Please wait");


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog.show();

                if(cbLimp.isChecked())
                    d.add("Limp");
                if(cbBlind.isChecked())
                    d.add("Blind");
                if(cbMute.isChecked())
                    d.add("Mute");
                if(cbPartiallyBlind.isChecked())
                    d.add("PartiallyBlind");
                if(cbDeaf.isChecked())
                    d.add("Deaf");

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
                documentReference.set(user,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("test", "onSuccess: User Created");
                    }
                });


                if(profileImageUri!=null)
                {
                    final StorageReference filePath=storageReference.child(uid+".jpg");
                    filePath.putFile(profileImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {

                                    DocumentReference documentReference=fstore.collection("users").document(uid);
                                    Map<String,Object> user =new HashMap<>();
                                    user.put("user_profileImage",task.getResult().toString());
                                    documentReference.set(user,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                            Toast.makeText(getApplicationContext(),"Changes Saved",Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ProfileActivity.this, "Image upload Failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
                else
                {
                            DocumentReference doc=fstore.collection("users").document(uid);
                            doc.update("user_profileImage",downloadUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(),"Changes Saved",Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        progressDialog.show();

        DocumentReference documentReference = fstore.collection("users").document(uid);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists())
                {
                    user_name=documentSnapshot.getString("user_name");
                    user_phone=documentSnapshot.getString("user_phone");
                    user_address=documentSnapshot.getString("user_address");
                    user_gender=documentSnapshot.getString("user_gender");
                    user_disabilities=documentSnapshot.getString("user_disabilities");
                    guardian_name=documentSnapshot.getString("guardian_name");
                    guardian_address=documentSnapshot.getString("guardian_address");
                    guardian_phone1=documentSnapshot.getString("guardian_phone1");
                    guardian_phone2=documentSnapshot.getString("guardian_phone2");
                    downloadUrl=documentSnapshot.getString("user_profileImage");

                    setValues();
                }
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    protected void setValues()
    {
        userName.setText(user_name);
        userPhone.setText(user_phone);
        userAddress.setText(user_address);
        guardianName.setText(guardian_name);
        if(guardian_phone1.equalsIgnoreCase("None"))
            guardianPhone1.setText("");
        else
            guardianPhone1.setText(guardian_phone1);

        if(guardian_phone2.equalsIgnoreCase("None"))
            guardianPhone2.setText("");
        else
            guardianPhone2.setText(guardian_phone2);

        guardianAddress.setText(guardian_address);

        if(user_gender.equalsIgnoreCase("Male"))
        {
            rbMale.setChecked(true);
        }
        else if(user_gender.equalsIgnoreCase("Female"))
        {
            rbFemale.setChecked(true);
        }

        String[] str1;
        str1=user_disabilities.split(" ");

        String s="";
        Set<String> str2 = new HashSet<String>(Arrays.asList(str1));

        cbLimp.setChecked(false);
        cbBlind.setChecked(false);
        cbMute.setChecked(false);
        cbPartiallyBlind.setChecked(false);
        cbDeaf.setChecked(false);

        if(str2.contains("Limp"))
        {
            cbLimp.setChecked(true);
        }
        if(str2.contains("Blind"))
        {
            cbBlind.setChecked(true);
        }
        if(str2.contains("Mute"))
        {
            cbMute.setChecked(true);
        }
        if(str2.contains("PartiallyBlind"))
        {
            cbPartiallyBlind.setChecked(true);
        }
        if(str2.contains("Deaf"))
        {
            cbDeaf.setChecked(true);
        }

//        Picasso.get().load(downloadUrl).into(profileImage);
        Picasso.get().load(downloadUrl).into(profileImage, new Callback() {
            @Override
            public void onSuccess() {
                progressDialog.dismiss();
            }

            @Override
            public void onError(Exception e) {

            }
        });
        progressDialog.dismiss();
    }

    public void changeProfileImage(View view)
    {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode==RESULT_OK)
            {
                Uri resultUri=result.getUri();
                profileImageUri=resultUri;
                profileImage.setImageURI(null);
                profileImage.setImageURI(resultUri);
            }
        }

    }

}