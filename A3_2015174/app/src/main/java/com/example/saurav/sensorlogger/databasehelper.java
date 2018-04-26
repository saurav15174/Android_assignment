package com.example.saurav.sensorlogger; /**
 * Created by saurav on 22/4/18.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.apache.commons.logging.Log;


/**
 * Created by saurav on 31/3/18.
 */

public class databasehelper extends SQLiteOpenHelper {
    private static  final int DATABASE_VERSION=1;
    private static final String DATABASE_NAME="sensordata.db";
    private static final String TABLE_USER="Sensor";
    private static final String ID= "user_id";
    private static final String X_ACCELERATION="x_acceleration";
    private static final String Y_ACCELERATION="y_acceleration";
    private static final String Z_ACCELERATION="z_acceleration";
    private static final String X_ROTATION="x_rotation";
    private static final String Y_ROTATION="y_rotation";
    private static final String Z_ROTATION="z_rotation";
    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";
    private static final String CELL_ID ="cell_id";
    private static final String WIFI ="access_point";
    private static final String SOUND_LEVEL="sound_level";
    private String CREATE_USER_TABLE="CREATE TABLE " + TABLE_USER + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, X_ACCELERATION TEXT,Y_ACCELERATION TEXT, Z_ACCELERATION TEXT,X_ROTATION TEXT,Y_ROTATION TEXT,Z_ROTATION TEXT,LONGITUDE TEXT,LATITUDE TEXT,CELL_ID TEXT,WIFI TEXT,SOUND_LEVEL TEXT)";
    private String DROP_USER_TABLE = "DROP TABLE IF EXISTS" + TABLE_USER;
    public databasehelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_USER_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(DROP_USER_TABLE);
        onCreate(sqLiteDatabase);
    }
    public void accelerometer(String x,String y,String z ){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(X_ACCELERATION,x);
        values.put(Y_ACCELERATION,y);
        values.put(Z_ACCELERATION,z);
        db.insert(TABLE_USER,null,values);
        db.close();
    }
    public void gyroscope(String gx,String gy,String gz){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(X_ROTATION,gx);
        values.put(Y_ROTATION,gy);
        values.put(Z_ROTATION,gz);
        db.insert(TABLE_USER,null,values);
        db.close();
    }
    public void coordinates(String Long, String lat){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LONGITUDE,Long);
        values.put(LATITUDE,lat);
        db.insert(TABLE_USER,null,values);
        db.close();
    }
    public void addcellid(String cellid){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CELL_ID,cellid);
        db.insert(TABLE_USER,null,values);
        db.close();
    }
    public void addwifi(String wifi){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(WIFI,wifi);
        db.insert(TABLE_USER,null,values);
        db.close();
    }
    public void addsound(String sound){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SOUND_LEVEL,sound);
        db.insert(TABLE_USER,null,values);
        db.close();
    }
    public Cursor getalldata(){
        SQLiteDatabase db =  this.getWritableDatabase();
        Cursor res = db.rawQuery(String.format("select * from %s", TABLE_USER),null);
        return  res;
    }
    public Cursor getdata(String name, String email)
    {
        SQLiteDatabase db =  this.getWritableDatabase();
        Cursor res = db.rawQuery(String.format("select * from %s WHERE USER_NAME = %s", TABLE_USER,name),null);
        return  res;
    }
}
