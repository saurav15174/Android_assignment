package com.example.saurav.sensorlogger;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.content.BroadcastReceiver;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.CellIdentityGsm;
import android.telephony.CellInfo;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class SensoLogger extends AppCompatActivity implements SensorEventListener {

    private Button start;
    private Button stop;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Sensor Gyroscope;
    private TextView xvalue;
    private TextView yvalue;
    private TextView zvalue;
    private TextView gx;
    private TextView gy;
    private Spinner sip;
    private TextView point;
    private Button export;
    private TextView gz;
    TelephonyManager tm;
    private WifiManager wifiManager;
    private TextView longit;
    private TextView latit;
    private List<ScanResult> wifilist;
    private TextView cellinfo;
    private LinearLayout ly;
    private TextView Sound;
    ArrayList<String> accesspoints;
    private TextView accesspoint;
    private static final int MY_PERMISSION_REQUEST = 1;
    MediaRecorder mRecorder;
    Thread runner;
    private databasehelper DB;
    int read;
    int mediastarter;
    private static double mema = 0.0;
    static final private double ema_filter = 0.6;
    final Runnable updater = new Runnable(){

        public void run(){
            updateTv();
        };
    };
    final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_senso_logger);
        read=0;
        mediastarter =0;
        export = findViewById(R.id.trans);
        DB = new databasehelper(SensoLogger.this);
        ly = findViewById(R.id.wifi);
        Sound = findViewById(R.id.level);
        longit = findViewById(R.id.longitude);
        latit = findViewById(R.id.latitude);
        accesspoints = new ArrayList<>();
        xvalue = findViewById(R.id.x);
        sip = findViewById(R.id.spinner);
        yvalue = findViewById(R.id.y);
        zvalue = findViewById(R.id.z);
        cellinfo = findViewById(R.id.info);
        gx = findViewById(R.id.gtx);
        gy = findViewById(R.id.gty);
        gz = findViewById(R.id.gtz);
        point = findViewById(R.id.access);
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.RECORD_AUDIO},MY_PERMISSION_REQUEST );
        }

        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                read=0;
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, new MyLocationListener());
                sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                Gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                accesspoints = new ArrayList<>();
                    start.setEnabled(false);
                    sensorManager.registerListener(SensoLogger.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                    sensorManager.registerListener(SensoLogger.this, Gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
                    PackageManager packageManager = getPackageManager();
                    if (!packageManager.hasSystemFeature(packageManager.FEATURE_SENSOR_GYROSCOPE)) {
                        gx.setText("Gyroscope sensor not found");
                        gy.setText("Gyroscope sensor not found");
                        gz.setText("Gyroscope senor not found");
                    }
                    if (mediastarter==0){
                        startRecorder();
                    }
                    soundMeasure();
                    showCurrentLocation();
                    tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    GsmCellLocation cellLocation = (GsmCellLocation) tm.getCellLocation();
                    cellinfo.setText(Integer.toString(cellLocation.getCid()));
                    DB.addcellid(Integer.toString(cellLocation.getCid()));
                    detect();

            }


        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sensorManager.unregisterListener(SensoLogger.this);
                mediastarter=1;
                read=1;
                start.setEnabled(true);
            }
        });

    export.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            exportdB();
        }
    });
    }
    private void soundMeasure(){
        if (runner == null)
        {
            runner = new Thread(){
                public void run()
                {
                    while (runner != null)
                    {
                            try
                            {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) { };
                            mHandler.post(updater);
                    }
                }
            };
            runner.start();
        }
    }
    private void showCurrentLocation() {
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            longit.setText(Float.toString((float)location.getLongitude()));
            latit.setText(Float.toString((float)location.getLatitude()));
            DB.coordinates(Float.toString((float)location.getLongitude()),Float.toString((float)location.getLatitude()));
        }

    }
    private class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location location) {
            longit.setText(Float.toString((float)location.getLongitude()));
            latit.setText(Float.toString((float)location.getLatitude()));
            DB.coordinates(Float.toString((float)location.getLongitude()),Float.toString((float)location.getLatitude()));
        }
        public void onStatusChanged(String s, int i, Bundle b) {
        }
        public void onProviderDisabled(String s) {

        }
        public void onProviderEnabled(String s) {

        }
    }

    public void detect(){
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        checkwifi();
        wifiManager.startScan();
        accesspoints.clear();
        wifilist = wifiManager.getScanResults();
        for (int i=0;i<wifilist.size();i++)
        {
            accesspoints.add(wifilist.get(i).SSID);
            //DB.addwifi(wifilist.get(i).SSID.toString());
        }
        ArrayAdapter<String> data = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,accesspoints);
        sip.setAdapter(data);
    }

    private void checkwifi() {
        if (!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            xvalue.setText(Float.toString( sensorEvent.values[0]));
            yvalue.setText(Float.toString( sensorEvent.values[1]));
            zvalue.setText(Float.toString(sensorEvent.values[2]));
            DB.accelerometer(Float.toString( sensorEvent.values[0]),Float.toString( sensorEvent.values[1]),Float.toString(sensorEvent.values[2]));
        }
        if (sensorEvent.sensor.getType()==Sensor.TYPE_GYROSCOPE){
                gx.setText(Float.toString(sensorEvent.values[0]));
                gy.setText(Float.toString(sensorEvent.values[1]));
                gz.setText(Float.toString(sensorEvent.values[2]));
                DB.gyroscope(Float.toString(sensorEvent.values[0]),Float.toString(sensorEvent.values[1]),Float.toString(sensorEvent.values[2]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    public void startRecorder(){
            mRecorder = new MediaRecorder();
            if (mRecorder!=null)
            {
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mRecorder.setOutputFile("/dev/null");
                try
                {
                    mRecorder.prepare();
                }catch (java.io.IOException ioe) {

                }catch (java.lang.SecurityException e) {

                }
                try
                {
                    mRecorder.start();
                }catch (java.lang.SecurityException e) {

                }
            }

        }

    private void exportdB() {

        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists())
        {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, "csvsensor.csv");
        try
        {
            file.createNewFile();
            CSVWriter csvwrite = new CSVWriter(new FileWriter(file));
            SQLiteDatabase db = DB.getReadableDatabase();
            Cursor curCSV = DB.getalldata();
            curCSV.moveToNext();
            csvwrite.writeNext(curCSV.getColumnNames());
            while(curCSV.moveToNext())
            {
                String arrStr[] ={curCSV.getString(0),curCSV.getString(1), curCSV.getString(2),curCSV.getString(3),curCSV.getString(4), curCSV.getString(5),curCSV.getString(6),curCSV.getString(7), curCSV.getString(8),curCSV.getString(9),curCSV.getString(10),curCSV.getString(11)};
                csvwrite.writeNext(arrStr);
            }
            csvwrite.close();
            curCSV.close();
        }
        catch(Exception sqlEx)
        {
            sqlEx.printStackTrace();

        }
    }

    public double getAmplitude() {
        if (mRecorder != null)
            return  (mRecorder.getMaxAmplitude());
        else
            return 0;

    }
    public void updateTv(){
        if (read==0){
            Sound.setText(Double.toString((getAmplitudeEMA())) + " dB");
            DB.addsound(Double.toString((getAmplitudeEMA())) + " dB");

        }
        else
        {
            Sound.setText("");
        }

    }
    public double soundDb(double ampl){
        return  20 * Math.log10(getAmplitudeEMA() / ampl);
    }
    public double getAmplitudeEMA() {
        double amp =  getAmplitude();
        mema = ema_filter * amp + (1.0 - ema_filter) * mema;
        return mema;
    }

}
