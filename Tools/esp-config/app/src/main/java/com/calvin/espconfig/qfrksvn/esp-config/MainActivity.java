package com.calvin.espconfig;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

// Location
import android.location.LocationManager;
//import android.location.LocationListener;
import android.location.Criteria;
import android.location.Location;
import android.location.Geocoder;
import android.location.Address;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private static final int MY_PERMISSIONS_REQUEST = 1;
    private static final String TAG = "esp-config";
    private static final String ESP_SSID = "calvin-esp";
    private static final String ESP_PASSWORD = "calvin-esp";
    String postData;
    private ProgressBar spinner;
    private Dialog dialog;
    private TextView textScanning;
    private final int MY_PERMISSION_ACCESS_COARSE_LOCATION=1;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private int USE_GPS = 0;
    Address currentAddress = null;
    double currentPossition[] = {0.0, 0.0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    this.MY_PERMISSION_ACCESS_COARSE_LOCATION);
        }


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.configure);
        dialog.setTitle("Constrained runtime found");
        dialog.getWindow().setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.FILL_PARENT);

        Button dialogButton = (Button) dialog.findViewById(R.id.buttonConfigure);

        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String network, networkSSID, networkPassword, networkURIs;
                String name, nameOrganization, nameOrganizationalUnit, namePurpose, nameGroup, nameName;
                String address, addressCountry, addressState, addressLocality, addressStreet, addressStreetNumber, addressBuilding, addressFloor, addressRoom;
                String owner, ownerOrganization, ownerOrganizationUnit, ownerRole, ownerPerson;

                networkSSID = ((EditText)dialog.findViewById(R.id.textSSID)).getText().toString();
                networkPassword = ((EditText)dialog.findViewById(R.id.textPassword)).getText().toString();
                networkURIs = ((EditText)dialog.findViewById(R.id.textURIs)).getText().toString();
                network = String.format("\"%s\": \"%s\", \"%s\": \"%s\", \"%s\": \"%s\"",
                        "ssid", networkSSID, "password", networkPassword, "proxy_uris", networkURIs);

                nameOrganization = ((EditText)dialog.findViewById(R.id.textOrganization)).getText().toString();
                nameOrganizationalUnit = ((EditText)dialog.findViewById(R.id.textOrganizationalUnit)).getText().toString();
                namePurpose = ((EditText)dialog.findViewById(R.id.textPurpose)).getText().toString();
                nameGroup = ((EditText)dialog.findViewById(R.id.textGroup)).getText().toString();
                nameName = ((EditText)dialog.findViewById(R.id.textName)).getText().toString();
                name = String.format("\"%s\": \"%s\", \"%s\": \"%s\", \"%s\": \"%s\", \"%s\": \"%s\", \"%s\": \"%s\"",
                        "organization", nameOrganization, "organizationalUnit", nameOrganizationalUnit, "purpose", namePurpose, "group", nameGroup, "name", nameName);

                addressCountry = ((EditText)dialog.findViewById(R.id.textCountry)).getText().toString();
                addressState = ((EditText)dialog.findViewById(R.id.textState)).getText().toString();
                addressLocality = ((EditText)dialog.findViewById(R.id.textLocality)).getText().toString();
                addressStreet = ((EditText)dialog.findViewById(R.id.textStreet)).getText().toString();
                addressStreetNumber = ((EditText)dialog.findViewById(R.id.textStreetNumber)).getText().toString();
                addressBuilding = ((EditText)dialog.findViewById(R.id.textBuilding)).getText().toString();
                addressFloor = ((EditText)dialog.findViewById(R.id.textFloor)).getText().toString();
                addressRoom = ((EditText)dialog.findViewById(R.id.textRoom)).getText().toString();
                address = String.format("\"%s\": \"%s\", \"%s\": \"%s\", \"%s\": \"%s\", \"%s\": \"%s\", \"%s\": \"%s\", \"%s\": \"%s\", \"%s\": \"%s\", \"%s\": \"%s\"",
                        "country", addressCountry, "stateOrProvince", addressState, "locality", addressLocality, "street", addressStreet, "streetNumber", addressStreetNumber, "building", addressBuilding, "floor", addressFloor, "room", addressRoom);

                ownerOrganization = ((EditText)dialog.findViewById(R.id.textOrganization)).getText().toString();
                ownerOrganizationUnit = ((EditText)dialog.findViewById(R.id.textOrganizationalUnit)).getText().toString();
                ownerRole = ((EditText)dialog.findViewById(R.id.textRole)).getText().toString();
                ownerPerson = ((EditText)dialog.findViewById(R.id.textPersonOrGroup)).getText().toString();
                owner = String.format("\"%s\": \"%s\", \"%s\": \"%s\", \"%s\": \"%s\", \"%s\": \"%s\"",
                        "organization", ownerOrganization, "organizationalUnit", ownerOrganizationUnit, "role", ownerRole, "personOrGroup", ownerPerson);

                postData = String.format("{%s, \"attributes\": {\"indexed_public\": {\"node_name\": {%s}, \"address\": {%s}, \"owner\": {%s}}}}",
                        network, name, address, owner);

                sendPost();
                dialog.dismiss();
            }
        });

        spinner = (ProgressBar)findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);

        textScanning = (TextView)findViewById(R.id.textViewScanning);
        textScanning.setVisibility(View.GONE);

        Button btnScan = (Button)findViewById(R.id.scan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });
    }

    private void doScan() {
        // TODO: Fix this
        WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        WifiScanReceiver wifiReciever = new WifiScanReceiver();

        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        if (wifi.isWifiEnabled() == false)
            wifi.setWifiEnabled(true);

        wifi.startScan();
        spinner.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.VISIBLE);
        textScanning.setText(("Scanning"));
        textScanning.setVisibility(View.VISIBLE);
    }

    private void startScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST);
            else
                doScan();
        } else
            doScan();
    }

    // For play services and location

    @Override
    protected void onStart() {
        super.onStart();
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    this.MY_PERMISSION_ACCESS_COARSE_LOCATION);
        }
        // Connect the client if are allowed to use it
         Log.i(TAG, "Activty started!");
         mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            if (ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.removeLocationUpdates(
                        mGoogleApiClient,
                        this);
            }
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000); // Update location every second

        Log.i(TAG, "Google API connected");

        // Check permissions and use GPS if we are allowed
        if (ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Request location updates");
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            this.onLocationChanged(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiClient connection has failed");
    }

    @Override
    public void onLocationChanged(Location location) {

        if (location == null) {
            Log.w(TAG, "We have no location yet");
        }
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        currentPossition[0] = longitude;
        currentPossition[0] = latitude;

        Log.i(TAG, "latitude-- " + latitude);
        Log.i(TAG, "longitude-- " + longitude);

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && addresses.size() > 0) {
                currentAddress = addresses.get(0);
                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();

                Log.w(TAG, "Adress:  " + address);
                Log.w(TAG, "Road:    " + addresses.get(0).getThoroughfare() + " " + addresses.get(0).getSubThoroughfare());
                Log.w(TAG, "Postal:  " + postalCode);
                Log.w(TAG, "City:    " + city);
                Log.w(TAG, "State:   " + state);
                Log.w(TAG, "Country: " + country);
                Log.w(TAG, "Ccode:   " + addresses.get(0).getCountryCode());
                Log.w(TAG, "Name:    " + addresses.get(0).getFeatureName());

            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                doScan();
            else
                showMessage("No permission");
        }
    }

    class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            String action = intent.getAction();
            int i = 0;
            ArrayList<String> wifis = new ArrayList<String>();
            WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);

            unregisterReceiver(this);

            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                List<ScanResult> wifiScanList = wifi.getScanResults();
                for(int x = 0; x < wifiScanList.size(); x++) {
                    if (wifiScanList.get(x).SSID.equals((ESP_SSID))) {
                        connectWifi();
                        return;
                    }
                }
            }

            showMessage("No AP found");
            spinner.setVisibility(View.GONE);
        }
    }

    public class WifiConnectReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.isConnected()) {
                    WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
                    WifiInfo connInfo = wifi.getConnectionInfo();
                    if (ESP_SSID.equals(connInfo.getSSID()) || ("\"" + ESP_SSID + "\"").equals(connInfo.getSSID())) {
                        showConfig();
                        unregisterReceiver(this);
                    } else {
                        showMessage("Connected, but incorrect AP " + connInfo.getSSID());
                    }
                }
            }
        }
    }

    private void connectWifi() {
        textScanning.setText("Connecting");

        WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);

        BroadcastReceiver broadcastReceiver = new WifiConnectReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = String.format("\"%s\"", ESP_SSID);
        conf.preSharedKey = String.format("\"%s\"", ESP_PASSWORD);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN); // For WPA2
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);

        int netId = wifi.addNetwork(conf);
        wifi.disconnect();
        wifi.enableNetwork(netId, true);
        wifi.reconnect();
    }

    private void showConfig() {
        spinner.setVisibility(View.GONE);
        textScanning.setVisibility(View.GONE);

        if (currentAddress != null) {
            Log.w(TAG, "Populating address");
            ((EditText)dialog.findViewById(R.id.textCountry)).setText(currentAddress.getCountryCode());
            ((EditText)dialog.findViewById(R.id.textState)).setText(currentAddress.getAdminArea());
            ((EditText)dialog.findViewById(R.id.textLocality)).setText(currentAddress.getLocality());
            //((EditText)dialog.findViewById(R.id.textLocality)).setText(currentPossition[0] + ", " + currentPossition[1]);
            ((EditText)dialog.findViewById(R.id.textStreet)).setText(currentAddress.getThoroughfare());
            ((EditText)dialog.findViewById(R.id.textStreetNumber)).setText(currentAddress.getSubThoroughfare());
            if (currentAddress.getThoroughfare().equals("Sölvegatan") && currentAddress.getSubThoroughfare().equals("53")) {
                ((EditText) dialog.findViewById(R.id.textBuilding)).setText("Bricks Lund");
                ((EditText) dialog.findViewById(R.id.textFloor)).setText("1");
                ((EditText) dialog.findViewById(R.id.textRoom)).setText("1872 Ericsson Garage");
                ((EditText) dialog.findViewById(R.id.textOrganization)).setText("Ericsson");
            }
        }
        dialog.show();
    }

    class ClientThread implements Runnable {
        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName("172.16.0.1");
                Socket socket = new Socket(serverAddr, 80);
                OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out.write(postData);
                out.flush();
                String data;
                while ((data = in.readLine()) != null) {
                    if (data.contains("200 OK")) {
                        showMessage("Runtime configured");
                    } else if (data.contains(("500 Internal Server Error"))) {
                        showMessage("Internal Server Error");
                    }
                }
                socket.close();
            } catch (UnknownHostException e){
                e.printStackTrace();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendPost() {
        new Thread(new ClientThread()).start();
    }

    private void showMessage(final String data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
