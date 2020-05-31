package com.spitmca.helpinghands;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ShowAllTrains extends AppCompatActivity {

    String sourceName,destinationName,direction;
    private static final String FILE_EXTENSION=".hhs";
    Station source,destination;

    RecyclerView trainList;
    TrainAdapter trainAdapter;

    ImageView profileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_trains);

        profileButton=(ImageView)findViewById(R.id.showalltrains_profileButton);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i =new Intent(ShowAllTrains.this,ProfileActivity.class);
                startActivity(i);
            }
        });

        Intent i = getIntent();
        sourceName=i.getStringExtra("sourceName");
        destinationName=i.getStringExtra("destinationName");
        direction=i.getStringExtra("direction");
        getStations(sourceName,destinationName,direction);

        trainList=(RecyclerView)findViewById(R.id.showalltrains_rv_trainlist);
        trainList.setHasFixedSize(true);
        trainList.setLayoutManager(new LinearLayoutManager(this));

        trainAdapter=new TrainAdapter(this,source.trains,destination.trains);
        trainList.setAdapter(trainAdapter);
    }

    public void getStations(final String sourceName,final String destinationName,final String direction)
    {
        String fileName=sourceName+direction+FILE_EXTENSION;
        File file=new File(getFilesDir(),fileName);

        if(file.exists())
        {
            FileInputStream fis;
            ObjectInputStream ois;

            try
            {
                fis=openFileInput(fileName);
                ois=new ObjectInputStream(fis);

                source=(Station) ois.readObject();

                ois.close();
                fis.close();
            }
            catch (IOException | ClassNotFoundException e)
            {
                Toast.makeText(getApplicationContext(), "Unable to fetch Files", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Log.d("Test", "Source file doesnt exist");
        }

        fileName=destinationName+direction+FILE_EXTENSION;
        file=new File(getFilesDir(),fileName);

        if(file.exists())
        {
            FileInputStream fis;
            ObjectInputStream ois;

            try
            {
                fis=openFileInput(fileName);
                ois=new ObjectInputStream(fis);

                destination=(Station) ois.readObject();

                ois.close();
                fis.close();
            }
            catch (IOException | ClassNotFoundException e)
            {
                Toast.makeText(getApplicationContext(), "Unable to fetch Files", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Log.d("Test", "Destination file doesnt exist");
        }
    }
}
