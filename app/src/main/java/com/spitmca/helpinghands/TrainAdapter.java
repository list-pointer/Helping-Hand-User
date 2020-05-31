package com.spitmca.helpinghands;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TrainAdapter extends RecyclerView.Adapter<TrainAdapter.TrainViewHolder>
{
    private Context ctx;
    private ArrayList<Time> boardingTimes;
    private ArrayList<Time> reachingTimes;


    public TrainAdapter(Context ctx, ArrayList<Time> boardingTimes, ArrayList<Time> reachingTimes) {
        this.ctx = ctx;
        this.boardingTimes = boardingTimes;
        this.reachingTimes = reachingTimes;
    }

    @NonNull
    @Override
    public TrainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(ctx);
        View view = inflater.inflate(R.layout.item_train,parent,false);
        return new TrainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrainViewHolder holder, int position) {

        Time board_Time=boardingTimes.get(position);
        Time reach_Time=reachingTimes.get(position);

        holder.boardTime.setText(getTime(board_Time.hour,board_Time.min,board_Time.partOfDay));
        holder.reachTime.setText(getTime(reach_Time.hour,reach_Time.min,reach_Time.partOfDay));
    }

    @Override
    public int getItemCount() {
        return boardingTimes.size();
    }

    class TrainViewHolder extends RecyclerView.ViewHolder{

        TextView boardTime,reachTime;

        public TrainViewHolder(@NonNull View itemView) {
            super(itemView);

            boardTime=(TextView)itemView.findViewById(R.id.item_train_boardtime);
            reachTime=(TextView)itemView.findViewById(R.id.item_train_reachtime);
        }
    }

    public String getTime(int hour,int min, String partofDay)
    {
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