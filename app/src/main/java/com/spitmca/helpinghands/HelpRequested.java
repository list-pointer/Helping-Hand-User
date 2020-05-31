package com.spitmca.helpinghands;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class HelpRequested extends AppCompatActivity{

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;


    TextView assistantName,assistantPhone;
    ImageView assistantPhoto;

    ImageView profileButton;

    double latitude,longitude;
    String stationName,uid;
    String request_date,request_time,dateTimeSuffix;

    Handler handler = new Handler();
    Runnable refresh;

    String expectedEndTime;

    Button btn_endservice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_requested);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        uid=fAuth.getCurrentUser().getUid();

        assistantName=(TextView)findViewById(R.id.helprequested_et_username);
        assistantPhone=(TextView)findViewById(R.id.helprequested_et_usernumber);

        assistantPhoto=(ImageView)findViewById(R.id.helprequested_profile_image);

        btn_endservice=(Button)findViewById(R.id.helprequested_endservice);

        profileButton=(ImageView)findViewById(R.id.helprequested_profileButton);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i =new Intent(HelpRequested.this,ProfileActivity.class);
                startActivity(i);
            }
        });

        Intent helpRequest=getIntent();
        latitude=helpRequest.getDoubleExtra("latitude",0.0);
        longitude=helpRequest.getDoubleExtra("longitude",0.0);
        stationName=helpRequest.getStringExtra("stationName");

        dateTimeSuffix = getDate()+getTime();
        request_date=getDate();
        request_time=getTime();

        DocumentReference userDoc=fStore.collection("users").document(uid);
        Map<String,Object> user =new HashMap<>();
        user.put("request_station",stationName);
        user.put("request_latitude",Double.toString(latitude));
        user.put("request_longitude",Double.toString(longitude));
        userDoc.set(user, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("test", "User Location Updated");
            }
        });

        DocumentReference requestDoc = fStore.collection("Admins")
                .document("StationAdmins")
                .collection(stationName)
                .document("Requests")
                .collection("ActiveRequests")
                .document(uid+dateTimeSuffix);

        Map<String,Object> request =new HashMap<>();
        request.put("user_id",uid);
        request.put("staff_id",null);
        request.put("request_date",request_date);
        request.put("request_time",request_time);
        request.put("start_time",null);
        request.put("end_time",null);
        requestDoc.set(request).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(HelpRequested.this, "Request sent", Toast.LENGTH_SHORT).show();
            }
        });


        refresh = new Runnable() {
            public void run() {

                findAssistant();

                handler.postDelayed(refresh, 5000);
            }
        };
        handler.post(refresh);

    }

    public String getDate() {

        String request_date;

        Calendar c = Calendar.getInstance();
        request_date=c.get(Calendar.YEAR)+"-"
                + (c.get(Calendar.MONTH)+1)+"-"
                + c.get(Calendar.DAY_OF_MONTH);

        return request_date;
    }

    public String getTime() {

        String request_time="";

        Calendar c = Calendar.getInstance();
        int h = c.get(Calendar.HOUR_OF_DAY);
        int m = c.get(Calendar.MINUTE);
        if(h<10)
        {
            request_time=request_time+"0"+h;
        }
        else
        {
            request_time=request_time+h;
        }

        request_time+=":";

        if(m<10)
        {
            request_time=request_time+"0"+m;
        }
        else
        {
            request_time=request_time+m;
        }

        return request_time;
    }

    public void findAssistant() {

        final DocumentReference requestStateDoc=fStore.collection("users").document(uid);
        requestStateDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String requestStatus=documentSnapshot.getString("request_state");
                if(requestStatus.equals("accepted"))
                {
                    final DocumentReference assistantDoc=fStore.collection("Admins")
                            .document("StationAdmins")
                            .collection(stationName)
                            .document("Assistants")
                            .collection("StationAssistants")
                            .document(documentSnapshot.getString("assistant_id"));

                    assistantDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                            assistantName.setText(documentSnapshot.getString("assistant_name"));
                            assistantPhone.setText(documentSnapshot.getString("assistant_phone"));

                            String downloadUrl=documentSnapshot.getString("assistant_profileImage");
                            Picasso.get().load(downloadUrl).into(assistantPhoto, new Callback() {
                                @Override
                                public void onSuccess() {

//                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });
                            handler.removeCallbacks(refresh);
                            Toast.makeText(HelpRequested.this, "Assistant Found!", Toast.LENGTH_SHORT).show();
                            btn_endservice.setVisibility(View.VISIBLE);

                            final DocumentReference requestDoc=fStore.collection("Admins")
                                    .document("StationAdmins")
                                    .collection(stationName)
                                    .document("Requests")
                                    .collection("ActiveRequests")
                                    .document(uid+dateTimeSuffix);
                            requestDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    expectedEndTime=documentSnapshot.getString("end_time");

                                    refresh = new Runnable() {
                                        public void run() {

                                            checkEndRequest();

                                            handler.postDelayed(refresh, 15000);
                                        }
                                    };
                                    handler.post(refresh);
                                }
                            });
                        }
                    });
                }
                else if(requestStatus.equals("rejected"))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(HelpRequested.this);
                    builder.setTitle("Invalid Location");
                    builder.setMessage("Please request help on the correct station");
                    builder.setCancelable(false);

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            final DocumentReference requestStateDoc=fStore.collection("users").document(uid);
                            Map<String,Object> newRequestStatus = new HashMap<>();
                            newRequestStatus.put("request_state","idle");
                            requestStateDoc.set(newRequestStatus,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Intent goBack = new Intent(HelpRequested.this,RequestingHelp.class);
                                    startActivity(goBack);
                                }
                            });
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                }
                else if(requestStatus.equals("unavailable"))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(HelpRequested.this);
                    builder.setTitle("No Assistant Available");
                    builder.setMessage("Please request help after some time");
                    builder.setCancelable(false);

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            final DocumentReference requestStateDoc=fStore.collection("users").document(uid);
                            Map<String,Object> newRequestStatus = new HashMap<>();
                            newRequestStatus.put("request_state","idle");
                            requestStateDoc.set(newRequestStatus,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Intent goBack = new Intent(HelpRequested.this,RequestingHelp.class);
                                    startActivity(goBack);
                                }
                            });
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(HelpRequested.this);
        builder.setTitle("Request Active");
        builder.setMessage("Do you wish to cancel the request?");
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                cancelRequest();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void cancelRequest() {

        final DocumentReference requestDoc=fStore.collection("Admins")
                .document("StationAdmins")
                .collection(stationName)
                .document("Requests")
                .collection("ActiveRequests")
                .document(uid+dateTimeSuffix);

        requestDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {


                DocumentReference userDoc = fStore.collection("users").document(documentSnapshot.getString("user_id"));
                Map<String,Object> userData = new HashMap<>();
                userData.put("assistant_id",null);
                userData.put("request_state","idle");
                userDoc.set(userData,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Test", "User link cleared");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HelpRequested.this, "User link could not be cleared", Toast.LENGTH_SHORT).show();
                    }
                });

                if(documentSnapshot.getString("staff_id")!=null)
                {
                    DocumentReference assistantDoc=fStore.collection("Admins")
                            .document("StationAdmins")
                            .collection(stationName)
                            .document("Assistants")
                            .collection("StationAssistants")
                            .document(documentSnapshot.getString("staff_id"));

                    Map<String,Object> assistantData = new HashMap<>();
                    assistantData.put("current_clientID",null);
                    assistantData.put("isAvailable","true");
                    assistantDoc.set(assistantData,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Test", "Assistant link cleared");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(HelpRequested.this, "Assistant link could not be cleared", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                DocumentReference deletedRequestsDoc=fStore.collection("Admins")
                        .document("StationAdmins")
                        .collection(stationName)
                        .document("Requests")
                        .collection("CancelledRequests")
                        .document(uid+dateTimeSuffix);

                String end_time=getTime();

                Map<String,Object> delRequest = new HashMap<>();
                delRequest.put("user_id",documentSnapshot.get("user_id"));
                delRequest.put("request_date",documentSnapshot.get("request_date"));
                delRequest.put("request_time",documentSnapshot.get("request_time"));
                delRequest.put("staff_id",documentSnapshot.get("staff_id"));
                delRequest.put("start_time",documentSnapshot.get("start_time"));
                delRequest.put("end_time",end_time);

                deletedRequestsDoc.set(delRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Test", "Request moved to Cancelled Requests");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HelpRequested.this, "Failed to move request to cancelled", Toast.LENGTH_SHORT).show();
                    }
                });

                requestDoc.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Test", "Request Deleted");

                        handler.removeCallbacks(refresh);
                        finish();
//                        Intent i = new Intent(HelpRequested.this,RequestingHelp.class);
//                        startActivity(i);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HelpRequested.this, "Unable to delete request", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(HelpRequested.this, "Cannot find Request", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void endService(View view) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(HelpRequested.this);
        builder.setTitle("End Service");
        builder.setMessage("Do you wish to end the service?");
        builder.setCancelable(true);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

                final DocumentReference requestDoc=fStore.collection("Admins")
                        .document("StationAdmins")
                        .collection(stationName)
                        .document("Requests")
                        .collection("ActiveRequests")
                        .document(uid+dateTimeSuffix);

                requestDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {


                        DocumentReference userDoc = fStore.collection("users").document(documentSnapshot.getString("user_id"));

                        Map<String,Object> userData = new HashMap<>();
                        userData.put("assistant_id",null);
                        userData.put("request_state","idle");
                        userDoc.set(userData,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("Test", "User link cleared");
                            }
                        });

                        final DocumentReference assistantDoc=fStore.collection("Admins")
                                .document("StationAdmins")
                                .collection(stationName)
                                .document("Assistants")
                                .collection("StationAssistants")
                                .document(documentSnapshot.getString("staff_id"));

                        assistantDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                long counter=documentSnapshot.getLong("counter")+1;

                                Map<String,Object> assistantData = new HashMap<>();
                                assistantData.put("current_clientID",null);
                                assistantData.put("isAvailable","true");
                                assistantData.put("counter",counter);
                                assistantDoc.set(assistantData,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("Test", "Assistant link cleared");
                                    }
                                });

                            }
                        });

                        DocumentReference deletedRequestsDoc=fStore.collection("Admins")
                                .document("StationAdmins")
                                .collection(stationName)
                                .document("Requests")
                                .collection("CompletedRequests")
                                .document(uid+dateTimeSuffix);

                        String end_time=getTime();

                        Map<String,Object> delRequest = new HashMap<>();
                        delRequest.put("user_id",documentSnapshot.get("user_id"));
                        delRequest.put("request_date",documentSnapshot.get("request_date"));
                        delRequest.put("request_time",documentSnapshot.get("request_time"));
                        delRequest.put("staff_id",documentSnapshot.get("staff_id"));
                        delRequest.put("start_time",documentSnapshot.get("start_time"));
                        delRequest.put("end_time",end_time);

                        deletedRequestsDoc.set(delRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("Test", "Request moved to Completed Requests");
                            }
                        });

                        requestDoc.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("Test", "Request Deleted");

                                handler.removeCallbacks(refresh);
//                                Intent i = new Intent(HelpRequested.this,RequestingHelp.class);
//                                startActivity(i);
                                finish();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HelpRequested.this, "Couldn't fetch request data", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void checkEndRequest() {

        String currTime=getTime();

        if(currTime.equals(expectedEndTime))
        {

            final DocumentReference requestDoc=fStore.collection("Admins")
                    .document("StationAdmins")
                    .collection(stationName)
                    .document("Requests")
                    .collection("ActiveRequests")
                    .document(uid+dateTimeSuffix);

            requestDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    DocumentReference userDoc = fStore.collection("users").document(documentSnapshot.getString("user_id"));

                    Map<String,Object> userData = new HashMap<>();
                    userData.put("assistant_id",null);
                    userData.put("request_state","idle");
                    userDoc.set(userData,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Test", "User link cleared");
                        }
                    });

                    final DocumentReference assistantDoc=fStore.collection("Admins")
                            .document("StationAdmins")
                            .collection(stationName)
                            .document("Assistants")
                            .collection("StationAssistants")
                            .document(documentSnapshot.getString("staff_id"));

                    assistantDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                            long counter=documentSnapshot.getLong("counter")+1;

                            Map<String,Object> assistantData = new HashMap<>();
                            assistantData.put("current_clientID",null);
                            assistantData.put("isAvailable","true");
                            assistantData.put("counter",counter);
                            assistantDoc.set(assistantData,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("Test", "Assistant link cleared");
                                }
                            });

                        }
                    });

                    DocumentReference deletedRequestsDoc=fStore.collection("Admins")
                            .document("StationAdmins")
                            .collection(stationName)
                            .document("Requests")
                            .collection("CompletedRequests")
                            .document(uid+dateTimeSuffix);

                    Map<String,Object> delRequest = new HashMap<>();
                    delRequest.put("user_id",documentSnapshot.get("user_id"));
                    delRequest.put("request_date",documentSnapshot.get("request_date"));
                    delRequest.put("request_time",documentSnapshot.get("request_time"));
                    delRequest.put("staff_id",documentSnapshot.get("staff_id"));
                    delRequest.put("start_time",documentSnapshot.get("start_time"));
                    delRequest.put("end_time",documentSnapshot.get("end_time"));

                    deletedRequestsDoc.set(delRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Test", "Request moved to Completed Requests");
                        }
                    });

                    requestDoc.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Test", "Request Deleted");

                            handler.removeCallbacks(refresh);
                            finish();
//                            Intent i = new Intent(HelpRequested.this,RequestingHelp.class);
//                            startActivity(i);
                        }
                    });
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(refresh);
    }

}
