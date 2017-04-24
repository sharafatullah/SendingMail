package com.sendingmail.customeViews;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

/**
 * Created by Sunil on 1/25/2017.
 */

public class PreferenceServices {

    public static PreferenceServices mInstance;
    private static Context mContext;
    private static final String TAG="SendingMail";
    private static final String DEFAULT = "NoData";

    //these String for Storing the LoginDetails
    public static final String Username="Username";
    public static final String Password="Password";


    public PreferenceServices(Context context) {

        mContext=context;
    }

    //making a singleton class
    public static synchronized PreferenceServices getInstance(Context context) {
        if(mInstance == null)
        {
            mContext=context;
            mInstance= new PreferenceServices(context);
            return mInstance;
        }
        return mInstance;
    }
    //getting a statis preference object
    public SharedPreferences getpref()
    {
        SharedPreferences preferences=mContext.getSharedPreferences(TAG,Context.MODE_PRIVATE);
        return preferences;
    }

    public void Settingdata(String Username1,String Password1){

        SharedPreferences.Editor editor=getpref().edit();
        editor.putString(Username,Username1);
        editor.putString(Password,Password1);
        editor.commit();
    }

    public HashMap<String,String> gettingdata(){
        HashMap<String,String> map=new HashMap<>();
        map.put(Username,getpref().getString(Username,DEFAULT));
        map.put(Password,getpref().getString(Password,DEFAULT));
        return map;
    }


}
