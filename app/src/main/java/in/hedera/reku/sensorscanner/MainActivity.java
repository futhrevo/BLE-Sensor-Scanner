package in.hedera.reku.sensorscanner;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.List;

import in.hedera.reku.sensorscanner.data.LuxBeacon;

public class MainActivity extends AppCompatActivity implements BeaconConsumer, Handler.Callback{

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST_ALL_PERMISSIONS = 101;
    public static final int START_SCAN = -1000;
    public static final int STOP_SCAN = -1010;
    public static final int INIT_SCAN = -1030;
    public static final int CLEAR_LIST = -1040;
    private FloatingActionButton fab;
    private Snackbar snackbar;
    public boolean isBTEnabled;
    private boolean isScanning = false;
    private BeaconManager beaconManager;
    private Region region;
    private Collection<Beacon> beaconList;

    private final Handler mHandler = new Handler(this);
    private static final Object sLock = new Object();
    private boolean mIsReady = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        snackbar = Snackbar.make(findViewById(R.id.coordinatorLayout), "snackbar", Snackbar.LENGTH_LONG);
        mHandler.sendEmptyMessage(INIT_SCAN);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isBTEnabled){
                    if(isScanning){
                        Toast.makeText(getApplicationContext(), R.string.already_scanning, Toast.LENGTH_LONG).show();
                    }else{
                        if(!getBTStatus()){
                            showBluetoothEnableDialog();
                            return;
                        }
                       mHandler.sendEmptyMessage(START_SCAN);
                        int scan_time = readScanValue();
                        if(scan_time > 0){
                            mHandler.sendEmptyMessageDelayed(STOP_SCAN, scan_time);
                        }

                    }
                }else{
                    snackbar.setText(R.string.enable_bt);
                    snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction(R.string.enable, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            enableBT();
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                }
            }
        });
        if (needPermissions(this)) {
            requestPermissions();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getBTStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSIONS_REQUEST_ALL_PERMISSIONS:
                boolean hasAllPermissions = true;
                for (int i = 0; i < grantResults.length; ++i) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        hasAllPermissions = false;
                        Log.e(TAG, "Unable to get permission " + permissions[i]);
                    }
                }
                if (hasAllPermissions) {
                    Log.d(TAG, "All permissions granted");

                } else {
                    Toast.makeText(this,
                            "Unable to get all required permissions", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "Permission has been denied by user");
//                    finish();
                    return;
                }

                break;
            default:
                Log.e(TAG, "Unexpected request code");

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            toggleBottomSheet();
            return true;
        }

        if(id == R.id.menu_clear_list){
            clearRecycler();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch(msg.what){
            case START_SCAN:
                startRanging();
                break;
            case STOP_SCAN:
                stopRanging();
                break;
            case INIT_SCAN:
                initBeaconManager();
                break;
            case CLEAR_LIST:
                mHandler.sendEmptyMessage(CLEAR_LIST);
                break;
        }

        return true;
    }


    @Override
    public void onBeaconServiceConnect() {
        mIsReady = true;
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                beaconList =  collection;
                if (collection.size() > 0) {
                    updateRecycler();
                    Beacon beacon = collection.iterator().next();
                    LuxBeacon luxBeacon = new LuxBeacon(beacon);
                    List<Long> data = beacon.getDataFields();
                    Log.i(TAG, "The first beacon I see is about " +
                            beacon.getDistance() + " meters away." + "rssi is " + luxBeacon.getBeacon().getRssi()
                            + " mac " + luxBeacon.getBeacon().getBluetoothAddress()
                            + " name " + luxBeacon.getBeacon().getBluetoothName()
                            +" lux " + luxBeacon.getLux());


                    Log.i(TAG, android.text.TextUtils.join(", ", data));
                }
            }
        });

        synchronized (sLock) {
            sLock.notify();
        }
    }

    static public boolean needPermissions(Context context) {
        return  ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                ;
    }

    private void requestPermissions() {
        Log.d(TAG, "requestPermissions: ");
        String[] permissions = new String[] {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
        };
        ActivityCompat.requestPermissions(this ,permissions, PERMISSIONS_REQUEST_ALL_PERMISSIONS);
    }

    public boolean getBTStatus(){
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            isBTEnabled = false;
            return false;
        }else{
            isBTEnabled = true;
            return true;
        }
    }

    public void askBTSettings(){
        if(!getBTStatus()){
            showBluetoothEnableDialog();
        }
    }

    public void enableBT(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.enable();
        isBTEnabled = true;
    }

    public void showBluetoothEnableDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Enable Bluetooth");
        // Setting Dialog Message
        alertDialog.setMessage("Bluetooth is required for all crucial functions. Do you want to turn ON bluetooth?");
        alertDialog.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                enableBT();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isBTEnabled = false;
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    // Currently identifying beacons starting with 0d00
    // modify accordingly with link below
    // https://altbeacon.github.io/android-beacon-library/javadoc/org/altbeacon/beacon/BeaconParser.html
    private void initBeaconManager(){
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.removeAllRangeNotifiers();
        beaconManager.removeAllMonitorNotifiers();
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-1=0d00,i:2-3,d:4-5,d:6-7,p:25-25"));
        beaconManager.bind(this);
        region = new Region("First iBeacon", null, null, null);
    }

    private void startRanging(){
        while (!mIsReady) {
            synchronized (sLock) {
                try {
                    sLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            beaconManager.startRangingBeaconsInRegion(region);
            isScanning = true;
            fab.setVisibility(View.INVISIBLE);
            snackbar.setText(R.string.scan);
            snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.stop, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHandler.sendEmptyMessage(STOP_SCAN);
                    snackbar.dismiss();
                }
            });
            snackbar.show();
        }  catch (RemoteException e) {
            isScanning = false;
        }
    }

    private void stopRanging(){
        fab.setVisibility(View.VISIBLE);
        if(snackbar.isShown()){
            snackbar.dismiss();
        }
        try {
            beaconManager.stopRangingBeaconsInRegion(region);
            isScanning = false;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public Collection<Beacon> getCollection(){
        return beaconList;
    }

    private void updateRecycler(){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivityFragment mainActivityFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
                    if(mainActivityFragment != null){
                        mainActivityFragment.updateRecycler();
                    }
                }
            });


    }

    private void clearRecycler(){
        MainActivityFragment mainActivityFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        if(mainActivityFragment != null){
            mainActivityFragment.clearRecycler();
        }
    }

    public void toggleBottomSheet() {
        SettingsDialogFragment settingsDialogFragment = new SettingsDialogFragment();
        settingsDialogFragment.show(getSupportFragmentManager(), settingsDialogFragment.getTag());
    }

    private int readScanValue(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int scantime = prefs.getInt("SCAN_TIME", 0);
        if(scantime == 0){
            return 10*1000;
        } else if(scantime == 100){
            return -1;
        }else{
            return scantime * 10 * 1000;
        }
    }
}
