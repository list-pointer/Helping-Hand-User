package com.spitmca.helpinghands;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class MetroShow extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    Station source,destination;
    Time start_time;

    private static final String FILE_EXTENSION=".hhs";
    String sourceName,destinationName,direction;

    ImageView profileButton;
    TextView startTime;
    RecyclerView trainList;

    TrainAdapter trainAdapter;

    int currIndex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metro_show);

        currIndex=0;

        start_time=new Time();

        Intent i = getIntent();
        sourceName=i.getStringExtra("sourceName");
        destinationName=i.getStringExtra("destinationName");
        direction=i.getStringExtra("direction");
        getStations(sourceName,destinationName,direction);

        profileButton=(ImageView)findViewById(R.id.metroshow_profileButton);
        startTime=(TextView)findViewById(R.id.metroshow_tv_startTime);
        trainList=(RecyclerView)findViewById(R.id.metroshow_rv_trainlist);

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i =new Intent(MetroShow.this,ProfileActivity.class);
                startActivity(i);
            }
        });

        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timepicker = new TimePickerFragment(startTime);
                timepicker.show(getSupportFragmentManager(),"Start Time");
            }
        });


        int h=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int m=Calendar.getInstance().get(Calendar.MINUTE);
        String pod;
        if(h>12)
        {
            h=h%12;
            pod="PM";
        }
        else
        {
            pod="AM";
        }
        start_time.hour=h;
        start_time.min=m;
        start_time.partOfDay=pod;


        int h1,h2;
        if(source.trains.get(source.trains.size()-1).partOfDay.equals("PM"))
        {
            h1=(((source.trains.get(source.trains.size()-1).hour + 12)%24)*60)+source.trains.get(source.trains.size()-1).min;
        }
        else
        {
            h1=(source.trains.get(source.trains.size()-1).hour*60)+source.trains.get(source.trains.size()-1).min;
        }

        if(start_time.partOfDay.equals("PM"))
        {
            h2=(((start_time.hour + 12)%24)*60)+start_time.min;
        }
        else
        {
            h2=(start_time.hour*60)+start_time.min;
        }

        if(h2<=h1)
        {
            showTrains(start_time);
        }
        else if(source.trains.get(source.trains.size()-1).hour==12)
        {
            showTrains(start_time);
        }
        else
        {
            Toast.makeText(this, h1+"  "+h2, Toast.LENGTH_SHORT).show();

            AlertDialog.Builder alert = new AlertDialog.Builder(MetroShow.this);
            alert.setMessage("No trains available right now");
            alert.setPositiveButton("OK", null);
            alert.show();
        }

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        if(hourOfDay>12)
        {
            start_time.partOfDay="PM";
            start_time.hour=hourOfDay%12;
        }
        else
        {
            start_time.hour=hourOfDay;
            start_time.partOfDay="AM";
        }
        start_time.min=minute;
        startTime.setText(getTime(start_time.hour,start_time.min,start_time.partOfDay));
    }

    public void requestHelp(View view){
        Button closeButton;
        final AlertDialog.Builder builder;
        closeButton = (Button) findViewById(R.id.metroshow_helpButton);
        builder = new AlertDialog.Builder(this);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.setMessage("Are you sure you need assistance ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
//                                Toast.makeText(getApplicationContext(),"you choose yes action for alertbox",Toast.LENGTH_SHORT).show();
                                Intent launchRequestHelp= new Intent(MetroShow.this,RequestingHelp.class);
                                startActivity(launchRequestHelp);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
//                                Toast.makeText(getApplicationContext(),"you choose no action for alertbox",Toast.LENGTH_SHORT).show();
                            }
                        });
                AlertDialog alert = builder.create();
                //Setting the title manually
                alert.setTitle("Confirm");
                alert.show();
            }
        });
    }

    public void getStations(final String sourceName,final String destinationName,final String direction) {

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
    }

    public void showAllTrains(View view) {
        Intent i = new Intent(this,ShowAllTrains.class);
        i.putExtra("sourceName",sourceName);
        i.putExtra("destinationName",destinationName);
        i.putExtra("direction",direction);
        startActivity(i);
    }

    public void showTrains(Time start_time) {

        trainList.setHasFixedSize(true);
        trainList.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<Time> board_timings,reach_timings;
        board_timings=new ArrayList<>();
        reach_timings=new ArrayList<>();

        int h1,h2;
        if(source.trains.get(currIndex).partOfDay.equals("PM"))
        {
            h1=(((source.trains.get(currIndex).hour + 12)%24)*60)+source.trains.get(currIndex).min;
        }
        else
        {
            h1=(source.trains.get(currIndex).hour*60)+source.trains.get(currIndex).min;
        }

        if(start_time.partOfDay.equals("PM"))
        {
            h2=(((start_time.hour + 12)%24)*60)+start_time.min;
        }
        else
        {
            h2=(start_time.hour*60)+start_time.min;
        }

        while (h1<h2)
        {
            currIndex++;

            if(source.trains.get(currIndex).partOfDay.equals("PM"))
            {
                h1=(((source.trains.get(currIndex).hour + 12)%24)*60)+source.trains.get(currIndex).min;
            }
            else
            {
                h1=(source.trains.get(currIndex).hour*60)+source.trains.get(currIndex).min;
            }
        }

        int bound;
        if(currIndex+3>=source.trains.size()-1)
        {
            bound=source.trains.size();
        }
        else
        {
            bound=currIndex+3;
        }

        for (int i=currIndex;i<bound;i++)
        {
            board_timings.add(source.trains.get(i));
            reach_timings.add(destination.trains.get(i));
        }
        trainAdapter=new TrainAdapter(this,board_timings,reach_timings);
        trainList.setAdapter(trainAdapter);

    }

    public void showPreviousTrains(View view) {

        currIndex=currIndex-3;
        if(currIndex<0)
        {
            Toast.makeText(this, "These are the first trains of the day", Toast.LENGTH_SHORT).show();
            currIndex=0;
        }

        trainList.setHasFixedSize(true);
        trainList.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<Time> board_timings,reach_timings;
        board_timings=new ArrayList<>();
        reach_timings=new ArrayList<>();

        for (int i=currIndex;i<(currIndex+3);i++)
        {
            if(currIndex<0)
                break;
            board_timings.add(source.trains.get(i));
            reach_timings.add(destination.trains.get(i));
        }
        trainAdapter=new TrainAdapter(this,board_timings,reach_timings);
        trainList.setAdapter(trainAdapter);

    }

    public void showNextTrains(View view) {

        int bound;

        if(currIndex+3>=source.trains.size())
        {
            Toast.makeText(this, "These were last trains for the day", Toast.LENGTH_SHORT).show();
            currIndex=(source.trains.size()-1);
            return;
        }
        else
        {
            currIndex+=3;
            if(currIndex+3>=source.trains.size())
            {
                bound=source.trains.size();
            }
            else
            {
                bound=currIndex+3;
            }
        }

        trainList.setHasFixedSize(true);
        trainList.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<Time> board_timings,reach_timings;
        board_timings=new ArrayList<>();
        reach_timings=new ArrayList<>();

        for (int i=currIndex;i<bound;i++)
        {
            board_timings.add(source.trains.get(i));
            reach_timings.add(destination.trains.get(i));
        }
        trainAdapter=new TrainAdapter(this,board_timings,reach_timings);
        trainList.setAdapter(trainAdapter);

    }

    public void customSearch(View view) {

        int h1,h2;
        if(source.trains.get(source.trains.size()-1).partOfDay.equals("PM"))
        {
            h1=(((source.trains.get(source.trains.size()-1).hour + 12)%24)*60)+source.trains.get(source.trains.size()-1).min;
        }
        else
        {
            h1=(source.trains.get(source.trains.size()-1).hour*60)+source.trains.get(source.trains.size()-1).min;
        }

        if(start_time.partOfDay.equals("PM"))
        {
            h2=(((start_time.hour + 12)%24)*60)+start_time.min;
        }
        else
        {
            h2=(start_time.hour*60)+start_time.min;
        }

        if(h2<=h1)
        {
            currIndex=0;
            showTrains(start_time);
        }
        else if(source.trains.get(source.trains.size()-1).hour==12)
        {
            currIndex=0;
            showTrains(start_time);
        }
        else
        {
            Toast.makeText(this, "No trains available after this time", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public String getTime(int hour,int min, String partofDay) {

        String time="";
        if(hour<10)
        {
            time=time+"0"+hour;
        }
        else{
            time=time+hour;
        }
        time=time+":";
        if(min<10)
        {
            time=time+"0"+min;
        }
        else{
            time=time+min;
        }
        time=time+" "+partofDay;

        return time;
    }

}
