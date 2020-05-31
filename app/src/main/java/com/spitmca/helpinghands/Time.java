package com.spitmca.helpinghands;

import java.io.Serializable;

public class Time implements Serializable {

    public int hour,min;
    String partOfDay;

    Time()
    {
        hour=min=0;
        partOfDay="";
    }

    Time(int h, int m , String pod)
    {
        hour=h;
        min=m;
        partOfDay=pod;
    }
}
