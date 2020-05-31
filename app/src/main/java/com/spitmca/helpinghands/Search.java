package com.spitmca.helpinghands;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class Search extends AppCompatActivity {

// Database related Start

    FirebaseFirestore fstore;
    FirebaseAuth fAuth;

    String[] timings;
    ArrayList<String> list;

    String TOWARDS_GHATKOPAR="TowardsGhatkopar";
    String TOWARDS_VERSOVA="TowardsVersova";

    String AIRPORT_ROAD="Airport Road";
    String ANDHERI="Andheri";
    String ASALPHA="Asalpha";
    String AZAD_NAGAR="Azad Nagar";
    String CHAKALA="Chakala";
    String D_N_NAGAR="D N Nagar";
    String JAGRUTI_NAGAR="Jagruti Nagar";
    String MAROL_NAKA="Marol Naka";
    String SAKINAKA="Sakinaka";
    String WEH="WEH";
    String VERSOVA="Versova";
    String GHATKOPAR="Ghatkopar";

    private static final String FILE_EXTENSION=".hhs";
    private static final String PREFS_NAME = "HelpingHandsPrefs";
    private double database_version;

// Database Related End



    String source;
    String destination;
    int sourcePosition;
    int destinationPosition;
    String direction;

    ImageView profileButton;
    ProgressDialog progressDialog;

    TextView guardianName,guardianNumber1,guardianNumber2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

// Database Related Start
        list = new ArrayList<String>();
        fstore=FirebaseFirestore.getInstance();
        checkDatabaseUpdate();
// Database Related End

        progressDialog=new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Updating Database");

        fAuth=FirebaseAuth.getInstance();

        guardianName=(TextView)findViewById(R.id.search_et_guardianname);
        guardianNumber1=(TextView)findViewById(R.id.search_et_guardiannumber1);
        guardianNumber2=(TextView)findViewById(R.id.search_et_guardiannumber2);

        DocumentReference d= fstore.collection("users").document(fAuth.getUid());
        d.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                guardianName.setText(documentSnapshot.getString("guardian_name"));
                guardianNumber1.setText(documentSnapshot.getString("guardian_phone1"));
                guardianNumber2.setText(documentSnapshot.getString("guardian_phone2"));
            }
        });

        profileButton=(ImageView)findViewById(R.id.search_profileButton);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i =new Intent(Search.this,ProfileActivity.class);
                startActivity(i);
            }
        });

        ArrayList<String> stationList = new ArrayList<>();
        stationList.add(VERSOVA);
        stationList.add(D_N_NAGAR);
        stationList.add(AZAD_NAGAR);
        stationList.add(ANDHERI);
        stationList.add("Western Express Highway");
        stationList.add("Chakala(J.B.Nagar)");
        stationList.add(AIRPORT_ROAD);
        stationList.add(MAROL_NAKA);
        stationList.add(SAKINAKA);
        stationList.add(ASALPHA);
        stationList.add(JAGRUTI_NAGAR);
        stationList.add(GHATKOPAR);

        final Spinner spinner =findViewById(R.id.fromStation);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, stationList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                source = parent.getItemAtPosition(position).toString();
                if(source.equals("Western Express Highway"))
                {
                    source=WEH;
                }
                else if(source.equals("Chakala(J.B.Nagar)"))
                {
                    source=CHAKALA;
                }
                sourcePosition = spinner.getSelectedItemPosition()+1;
//                Toast.makeText(parent.getContext(), "FROM: " + source, Toast.LENGTH_LONG).show();
//                Toast.makeText(parent.getContext(), "From ID: "+sourcePosition, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView <?> parent) {
            }
        });


        final Spinner spinner2 =findViewById(R.id.toStation);
        ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, stationList);
        arrayAdapter2.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
        spinner2.setAdapter(arrayAdapter2);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                destination = parent.getItemAtPosition(position).toString();
                destinationPosition = spinner2.getSelectedItemPosition()+1;
                if(destination.equals("Western Express Highway"))
                {
                    destination=WEH;
                }
                else if(destination.equals("Chakala(J.B.Nagar)"))
                {
                    destination=CHAKALA;
                }
//                Toast.makeText(parent.getContext(), "TO: " + destination, Toast.LENGTH_LONG).show();
//                Toast.makeText(parent.getContext(), "To ID: "+destinationPosition, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView <?> parent) {
            }
        });

    }

    public void Search_Btn(View view) {
        if(sourcePosition==destinationPosition) {
            AlertDialog.Builder alert = new AlertDialog.Builder(Search.this);
            alert.setMessage("Boarding and departing station cannot be same");
            alert.setPositiveButton("OK", null);
            alert.show();
        }else{
            if(sourcePosition<destinationPosition)
                direction=TOWARDS_GHATKOPAR;
            else
                direction=TOWARDS_VERSOVA;
//            Toast.makeText(getApplicationContext(),"Boarding Station: "+source,Toast.LENGTH_LONG).show();
//            Toast.makeText(getApplicationContext(),"Destination: "+destination,Toast.LENGTH_LONG).show();
//            Toast.makeText(this, "Direction: "+direction, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Search.this,MetroShow.class);
            intent.putExtra("sourceName",source);
            intent.putExtra("destinationName",destination);
            intent.putExtra("direction",direction);
            startActivity(intent);
        }
    }

    public void requestHelp(View view){
        Button closeButton;
        final AlertDialog.Builder builder;
        closeButton = findViewById(R.id.search_helpButton);
        builder = new AlertDialog.Builder(this);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.setMessage("Are you sure you need assistance ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
//                                Toast.makeText(getApplicationContext(),"you choose yes action for alertbox",Toast.LENGTH_SHORT).show();
                                Intent launchRequestHelp= new Intent(Search.this,RequestingHelp.class);
                                startActivity(launchRequestHelp);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
//                                Toast.makeText(getApplicationContext(),"you choose no action for alertbox",
//                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                AlertDialog alert = builder.create();
                //Setting the title manually
                alert.setTitle("Confirm");
                alert.show();
            }
        });
    }


// Database Related Methods Start

    public void saveObject(final String name,final String direction)
    {
        DocumentReference doc=fstore.collection("Trains").document(direction)
                .collection(name).document("Timings");


        doc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {

                    list=(ArrayList<String>)documentSnapshot.get("ArrivalTime");


                    Station station=new Station(getApplicationContext(),name,direction,list);


//                     Saving the file offline

                    String fileName=station.stationName+station.direction+FILE_EXTENSION;
                    FileOutputStream fos;
                    ObjectOutputStream oos;

                    try
                    {
                        fos=openFileOutput(fileName,MODE_PRIVATE);
                        oos=new ObjectOutputStream(fos);
                        oos.writeObject(station);
                        oos.close();
                        fos.close();
//                        Toast.makeText(MainActivity.this, "File created for "+name + " "+direction, Toast.LENGTH_SHORT).show();
                    }
                    catch (IOException e)
                    {
                        Toast.makeText(getApplicationContext(),e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void updateDatabase()
    {
        saveObject(AIRPORT_ROAD,TOWARDS_GHATKOPAR);
        saveObject(AIRPORT_ROAD,TOWARDS_VERSOVA);

        saveObject(ANDHERI,TOWARDS_GHATKOPAR);
        saveObject(ANDHERI,TOWARDS_VERSOVA);

        saveObject(ASALPHA,TOWARDS_GHATKOPAR);
        saveObject(ASALPHA,TOWARDS_VERSOVA);

        saveObject(AZAD_NAGAR,TOWARDS_GHATKOPAR);
        saveObject(AZAD_NAGAR,TOWARDS_VERSOVA);

        saveObject(CHAKALA,TOWARDS_GHATKOPAR);
        saveObject(CHAKALA,TOWARDS_VERSOVA);

        saveObject(D_N_NAGAR,TOWARDS_GHATKOPAR);
        saveObject(D_N_NAGAR,TOWARDS_VERSOVA);

        saveObject(JAGRUTI_NAGAR,TOWARDS_GHATKOPAR);
        saveObject(JAGRUTI_NAGAR,TOWARDS_VERSOVA);

        saveObject(SAKINAKA,TOWARDS_GHATKOPAR);
        saveObject(SAKINAKA,TOWARDS_VERSOVA);

        saveObject(VERSOVA,TOWARDS_GHATKOPAR);
        saveObject(VERSOVA,TOWARDS_VERSOVA);

        saveObject(GHATKOPAR,TOWARDS_GHATKOPAR);
        saveObject(GHATKOPAR,TOWARDS_VERSOVA);

        saveObject(MAROL_NAKA,TOWARDS_GHATKOPAR);
        saveObject(MAROL_NAKA,TOWARDS_VERSOVA);

        saveObject(WEH,TOWARDS_GHATKOPAR);
        saveObject(WEH,TOWARDS_VERSOVA);

        progressDialog.dismiss();
    }

    public void checkDatabaseUpdate()
    {

        DocumentReference doc= fstore.collection("Trains").document("Version");
        doc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                progressDialog.show();
                Double version=0.0;
                if(documentSnapshot.exists()) {
                    version = documentSnapshot.getDouble("database_version");
                }


                SharedPreferences sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor ed=sharedPrefs.edit();

                if(!sharedPrefs.contains("database_version")){
                    ed.putString("database_version",Double.toString(version));
                    ed.commit();
                    updateDatabase();
                }
                else
                {
                    database_version=Double.parseDouble(sharedPrefs.getString("database_version","0.0"));
                    if(database_version<version)
                    {
                        updateDatabase();
                    }
                    else
                    {
                        progressDialog.dismiss();
                    }
                }
            }
        });
    }

// Database Related Methods End


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
