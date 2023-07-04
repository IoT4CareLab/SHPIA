package com.raffaello.nordic.util;

import com.raffaello.nordic.model.Device;
import com.raffaello.nordic.service.DataCollectorService;
import com.raffaello.nordic.view.activity.MainActivity;
import com.raffaello.nordic.view.adapter.BeaconGridAdapter;
import com.raffaello.nordic.view.fragment.SensorDetailEnvFragment;
import com.raffaello.nordic.view.fragment.SensorDetailMotionFragment;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class BeaconScanner{
    //singleton
    private static BeaconScanner instance = null;

    //Class
    private BeaconManager mBeaconManager;
    private BeaconGridAdapter beaconGridAdapter;
    private Region mRegion= new Region("region", null, null, null);
    static private List<Device> unavailableDevices=new ArrayList<>();

    //objects to display and collect data
    private String address="";
    private SensorDetailEnvFragment support;
    private SensorDetailMotionFragment support2;
    private SharedPreferencesHelper sharedPreferencesHelper;
    private DataCollectorService dataCollector;

    //if you want to change beacon protocol, modify this method. For more information search "setBeaconLayout" on stackoverflow
    private BeaconScanner() {
        mBeaconManager = BeaconManager.getInstanceForApplication(MainActivity.getAppContext());
        mBeaconManager.getBeaconParsers().clear();
        // Detect the main identifier (UID) frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"));
        // Detect the telemetry (TLM) frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15"));
        // Detect the URL frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-21v"));
        //Detect iBeacon frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
    }

    public static BeaconScanner getInstance() {//singleton pattern
        if (instance == null)
            instance = new BeaconScanner();
        return instance;
    }

    public void setAdapter(BeaconGridAdapter adapter){
        beaconGridAdapter=adapter;
    }

    public void setFragment(String s,SensorDetailEnvFragment fragment){
        address=s;
        support=fragment;
        sharedPreferencesHelper = SharedPreferencesHelper.getInstance(MainActivity.getAppContext());
    }

    public void setFragment2(String s,SensorDetailMotionFragment fragment){
        address=s;
        support2=fragment;
        sharedPreferencesHelper = SharedPreferencesHelper.getInstance(MainActivity.getAppContext());
    }

    public void setCollector(DataCollectorService d){
        dataCollector=d;
        sharedPreferencesHelper = SharedPreferencesHelper.getInstance(MainActivity.getAppContext());
    }

    public void updateUnavailableDevices(List<Device> devices){
        unavailableDevices.clear();
        unavailableDevices.addAll(devices);
    }

    private boolean checkSensorValidity(Beacon sensor){
        for(Device s : unavailableDevices){
            if(s.address.equals(""+sensor.getBluetoothAddress()))
                return false;
        }
        return true;
    }

    public void scan(boolean bind){
        if(bind){
            if(beaconGridAdapter!=null)//if I am in scanner mode
                beaconGridAdapter.clear();
            mBeaconManager.addRangeNotifier(new RangeNotifier() {
                @Override
                public void didRangeBeaconsInRegion(final Collection<Beacon> beacons, Region region) {
                    if (beacons.size() > 0) {
                        Iterator<Beacon> beaconIterator = beacons.iterator();
                        while (beaconIterator.hasNext()) {
                            Beacon beacon = beaconIterator.next();

                            //used when I am trying to find new devices (scanner mode), picks only tag, not other kinds of sensor
                            if (beaconGridAdapter != null && checkSensorValidity(beacon) && hex(beacon.getLastPacketRawBytes()).substring(62).startsWith("03") && !ServiceUtils.isRunning(DataCollectorService.class, MainActivity.getAppContext()))
                                beaconGridAdapter.addDevice(beacon);

                            //used when I am collecting data (Start data collection)
                            if (dataCollector != null && ServiceUtils.isRunning(DataCollectorService.class, MainActivity.getAppContext()) && checkSensorValidity(beacon) == false) {
                                dataCollector.addBeacon(beacon);
                                if (sharedPreferencesHelper.getTemperatureStatus())
                                    dataCollector.uploadTemp(beacon);

                                if (sharedPreferencesHelper.getHumidityStatus())
                                    dataCollector.uploadHum(beacon);

                                if (sharedPreferencesHelper.getMotionStatus())
                                    dataCollector.uploadAcc(beacon);

                                dataCollector.uploaRssi(beacon);//set rssi in any case
                            }

                            //used when I want to see the graph (click on the sensor name when I am inside an environment)
                            if (("" + beacon.getBluetoothAddress()).equals(address) && ServiceUtils.isRunning(DataCollectorService.class, MainActivity.getAppContext())) {
                                if (support != null) {
                                    if (sharedPreferencesHelper.getTemperatureStatus())
                                        support.setTemp(beacon);

                                    if (sharedPreferencesHelper.getHumidityStatus())
                                        support.setHum(beacon);//set humidity

                                    support.setRssi(beacon);//set rssi in any case
                                }
                                if (support2 != null)
                                    support2.setvectorAcc(beacon);
                            }
                        }
                    }
                }
            });
            mBeaconManager.startRangingBeacons(mRegion);}
        else{
            mBeaconManager.stopRangingBeacons(mRegion);
            mBeaconManager.removeAllRangeNotifiers();
        }
    }

    //converts bytes to hex
    private String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes)
            result.append(String.format("%02x", aByte));

        return result.toString();
    }

    //splits the bytes and creates an array with the data
    private String[] convertitore(String pacchetto) {
        String dati= pacchetto.substring(62);
        //temperature int,humidity percentage,temperature decimal,acc x,acc y,acc z
        String [] valoriPiccolo={dati.substring(18,20),dati.substring(20,22),dati.substring(22,24),dati.substring(24,28),dati.substring(28,32),dati.substring(32,36)}; //values for small tag
        String [] valoriGrande={dati.substring(20,22),dati.substring(22,24),dati.substring(24,26),dati.substring(26,30),dati.substring(30,34),dati.substring(34,38)}; //values for big tags
        //big and small tags have different bytes; in big tags, bytes are shifted because the battery level is represented with 4 characters
        if(dati.endsWith("00"))//small tags always ends with 00
            return valoriPiccolo;
        else
            return valoriGrande;
    }

    //returns temperature (int part + decimal part)
    public float getTemperatura(Beacon beacon){
        String [] valori=convertitore(hex(beacon.getLastPacketRawBytes()));
        float intero=Integer.parseInt(valori[0],16);
        float decimale=Integer.parseInt(valori[2],16);
        return  intero+(decimale/100);
    }

    //returns Humidity in percentage
    public int getUmidita(Beacon beacon){
        String [] valori=convertitore(hex(beacon.getLastPacketRawBytes()));
        return  Integer.parseInt(valori[1],16);
    }

    //returns the array [Accx AccY Accz]
    public float [] getAcc(Beacon beacon){
        String [] valori=convertitore(hex(beacon.getLastPacketRawBytes()));
        float x=((float)Integer.parseInt(valori[3],16))/100000;
        float y=((float)(Integer.parseInt(valori[4],16))/100000); //I don't know if I am converting right
        float z=((float)Integer.parseInt(valori[5],16))/100000;
        return new float[]{x,y,z};
    }
}
