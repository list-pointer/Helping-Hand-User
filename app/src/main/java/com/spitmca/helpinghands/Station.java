package com.spitmca.helpinghands;

import android.content.Context;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

public class Station implements Serializable {
    private static final String TAG = "test";
    public String stationName;
    public String direction;
    public ArrayList<Time> trains;

    Station(Context ctx, String n, String d, ArrayList<String> timings)
    {
        stationName=n;
        direction=d;
        trains=new ArrayList<Time>();

        for(int i=0;i<timings.size();i++)
        {
            Time t = new Time();
            String data = timings.get(i);

            try {
                t.hour = Integer.parseInt(data.trim().substring(0, 2));
                t.min = Integer.parseInt(data.trim().substring(3, 5));
                t.partOfDay = data.substring(6);
            }
            catch (NumberFormatException e)
            {
                Log.d(TAG, stationName+direction+" "+data);
            }

            trains.add(t);
        }
    }
}
