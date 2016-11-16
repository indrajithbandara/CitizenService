package io.github.deepbluecitizenservice.citizenservice.database;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@IgnoreExtraProperties
public class ProblemModel{
    public String url, creatorKey, description, locationAddress, solutionUrl,
            creatorName, creatorURL;
    public double locationX, locationY;
    public long sla, timeCreated, negTimeCreated;
    public int status, category;

    static final String  SOLVED_PROBLEM = "solved";
    static final String OPEN_PROBLEM = "open";

    public static final int CATEGORY_TRAFFIC = 0;
    public static final int CATEGORY_GARBAGE = 1;
    public static final int CATEGORY_POTHOLES = 2;

    public static final int STATUS_UNSOLVED = 0;
    public static final int STATUS_SOLVED = 1;

    ProblemModel(){

    }

    ProblemModel(String url, int status, double locationX, double locationY, String locationAddress,
                 String creatorKey, long SLA, long timeCreated, String description, int category,
                 String creatorName, String creatorURL){

        this.url             = url;
        this.status          = status;
        this.locationX       = locationX;
        this.locationY       = locationY;
        this.creatorKey      = creatorKey;
        this.sla             = SLA;
        this.timeCreated     = timeCreated;
        this.negTimeCreated  = 0-timeCreated;
        this.description     = description;
        this.category        = category;
        this.locationAddress = locationAddress;
        this.creatorName     = creatorName;
        this.creatorURL      = creatorURL;
    }

    @Exclude
    public String getPeriod(){
        long time = timeCreated*1000;
        long after;

        after = sla * 24 * 60 * 60* 1000;

        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yy", Locale.US);
        String fromDate = formatter.format(new Date(time));

        String toDate = formatter.format(new Date(time + after));
        return fromDate + " - " + toDate;
    }

    @Exclude
    public static String getCategory(int category){

        switch(category){
            case ProblemModel.CATEGORY_POTHOLES:
                return "Potholes";
            case ProblemModel.CATEGORY_GARBAGE:
                return "Garbage";
            case ProblemModel.CATEGORY_TRAFFIC:
                return "Traffic";
        }

        return "none";
    }

    @Exclude
    public String getCategory(){
        return getCategory(category);
    }
}