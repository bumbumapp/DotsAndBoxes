package com.dotsandboxes;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferenc {


    private SharedPreferences sharedPreferences;

    public Preferenc(Context context){
        sharedPreferences = context.getSharedPreferences("name",Context.MODE_PRIVATE);
    }

    public void putString(String value){

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("mode", value);
        editor.apply();
    }

    public String getString(){

        return sharedPreferences.getString("mode", "easy");

    }






}