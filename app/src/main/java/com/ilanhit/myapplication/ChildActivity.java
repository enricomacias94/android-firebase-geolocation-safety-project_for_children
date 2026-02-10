package com.ilanhit.myapplication;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Location;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ilanhit.myapplication.models.Child;
import com.ilanhit.myapplication.models.ChildPerson;
import com.ilanhit.myapplication.models.Parent;
import com.ilanhit.myapplication.models.ParentPerson;
import com.ilanhit.myapplication.models.Person;
import com.ilanhit.myapplication.models.Responsible;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChildActivity extends AppCompatActivity implements LocationListener {

    private EditText etContactPhone;
    private TextView tvContactResult;
    private TextView tvLocation;
    private Button btnAddRequest;
    private ParentPerson contact;
    private ChildPerson childPerson;
    private LocationManager locationManager;
    private String locationProvider;
    private double lat;
    private double lon;
// ok mettre les coms ici
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_child);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        childPerson = (ChildPerson)getIntent().getSerializableExtra("person");

        etContactPhone = findViewById(R.id.etContactPhone);
        tvContactResult = findViewById(R.id.tvContactResult);
        tvLocation = findViewById(R.id.tvLocation);
        Button btnSendAlert = findViewById(R.id.btnSendAlert);
        Button btnSearchContact = findViewById(R.id.btnSearchContact);
        Button btnShowLocation = findViewById(R.id.btnShowLocation);
        btnAddRequest = findViewById(R.id.btnAddRequest);
        btnAddRequest.setClickable(false);

        btnSendAlert.setOnClickListener( (view) -> sendAlert());
        btnSearchContact.setOnClickListener( (view) -> searchContact());
        btnAddRequest.setOnClickListener( (view) -> sendRequest());
        btnShowLocation.setOnClickListener(view -> showLocation());

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        locationProvider = locationManager.getBestProvider(criteria, false);
        if(locationProvider == null){
            if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }else{
            Location location = locationManager.getLastKnownLocation(locationProvider);
            if(location != null){
                onLocationChanged(location);
            }else{
                Toast.makeText(this, "Location not available", Toast.LENGTH_LONG).show();
            }
        }
    }
  //location update
    @Override
    protected void onResume(){
        super.onResume();
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }else{
            Criteria criteria = new Criteria();
            locationProvider = locationManager.getBestProvider(criteria, false);
            if(locationProvider != null){
                locationManager.requestLocationUpdates(locationProvider, 1000, 1, this);
                Location location  = locationManager.getLastKnownLocation(locationProvider);
                if(location != null)
                    onLocationChanged(location);
            }
        }
    }
// stop receiving location
    @Override
    protected void onPause(){
        super.onPause();
        locationManager.removeUpdates(this);
    }

    private void showLocation(){
        Criteria criteria = new Criteria();
        if(locationProvider == null){
            locationProvider = locationManager.getBestProvider(criteria, false);
        }
        if(locationProvider != null){
            if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            Location location  = locationManager.getLastKnownLocation(locationProvider);
            onLocationChanged(location);
        }

    }
    //loop the contacts and send the alert
    private void sendAlert(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        for(Responsible resp :childPerson.getContacts().values()){
            Parent parent = (Parent)resp;
            if(parent.isEnabled()){
                Child child = new Child(childPerson.getId());
                child.setLat(lat);
                child.setLon(lon);
                child.setHasAlert(true);
                database.getReference("users").child(resp.getId()).child("contacts").child(childPerson.getId())
                        .setValue(child).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(ChildActivity.this, "alert was sent", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }
//cherche par telephone//
    private void searchContact(){
        String phone = etContactPhone.getText().toString();
        if (phone.equals("")) {
            Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!phone.matches("\\d{10}")) { // adjust pattern based on your local format
            Toast.makeText(this, "Phone number must be 10 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users").orderByChild("phone").equalTo(phone).get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(task.isSuccessful()){
                            boolean found = false;
                            for(DataSnapshot data :  task.getResult().getChildren()){
                                found = true;
                                contact = data.getValue(ParentPerson.class);
                                if(contact == null){
                                    Toast.makeText(ChildActivity.this, "Contact was not found", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                Log.d("searchContact","contact " +contact.getDetails());
                                tvContactResult.setText(contact.getDetails());
                            }
                            if(found){
                                btnAddRequest.setClickable(true);
                                btnAddRequest.setEnabled(true);
                            }else{
                                Toast.makeText(ChildActivity.this, "Contact was not found", Toast.LENGTH_LONG).show();
                                btnAddRequest.setClickable(false);
                            }

                        }else{
                            Toast.makeText(ChildActivity.this, "Contact was not found", Toast.LENGTH_LONG).show();
                            btnAddRequest.setClickable(false);
                        }
                    }
                });
    }
    //add parent to child contact using ID from firebase
    private void sendRequest(){
        if(contact == null){
            Toast.makeText(ChildActivity.this, "Please search for the contact again", Toast.LENGTH_LONG).show();
            return;
        }

        String childId = FirebaseAuth.getInstance().getUid();
        Child child = new Child(childId);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users")
                .child(contact.getId()).
                child("contacts").child(child.getId())
                .setValue(child)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            updateChild(childId, contact.getId());
                        }else{
                            Log.d("sendRequest", task.getException().getMessage());
                            Toast.makeText(ChildActivity.this, "There was an unknown error, please try again", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }
//ajoute le parent sur firebase a lenfant //
    private void updateChild(String childId, String parentId){
        Parent parent = new Parent(parentId);
        Map<String, Parent> contactMap = childPerson.getContacts();
        contactMap.put(parent.getId(), parent);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users")
                .child(childId).
                child("contacts")
                .setValue(contactMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(ChildActivity.this, "Parent added successfully", Toast.LENGTH_LONG).show();
                        }else{
                            Log.d("sendRequest", task.getException().getMessage());
                            Toast.makeText(ChildActivity.this, "There was an unknown error, please try again", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
//update location
    @Override
    public void onLocationChanged(@NonNull Location location) {

        try{
            lat = location.getLatitude();
            lon = location.getLongitude();
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addressList = geocoder.getFromLocation(lat, lon, 1);
            if(addressList != null){
                Log.d("onLocationChanged", addressList.toString());
            }else{
                Log.d("onLocationChanged", "address list is null");
            }
            if(addressList != null && !addressList.isEmpty()){

                Address address = addressList.get(0);
                tvLocation.setText("lat " + lat + " " + address.getLocality());
            }

        }catch(IOException ex){
            Log.d("onLocationChanged", ex.toString());
            Log.d("onLocationChanged", ex.getMessage());
            Toast.makeText(ChildActivity.this, "Error retrieving location name", Toast.LENGTH_LONG).show();
        }catch(NullPointerException e) {
            Toast.makeText(ChildActivity.this, "Error retrieving location name", Toast.LENGTH_LONG).show();
        }
    }
}