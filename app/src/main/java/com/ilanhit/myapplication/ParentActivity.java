package com.ilanhit.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ilanhit.myapplication.adapters.ContactsAdapter;
import com.ilanhit.myapplication.models.Child;
import com.ilanhit.myapplication.models.ChildPerson;
import com.ilanhit.myapplication.models.ParentPerson;
import com.ilanhit.myapplication.models.Person;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ParentActivity extends AppCompatActivity implements ValueEventListener {

    private ParentPerson parent;
    private RecyclerView rvChildren;
    private List<ChildPerson> childList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_parent);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        parent = (ParentPerson)getIntent().getSerializableExtra("person");
        rvChildren = findViewById(R.id.rvChildren);
        rvChildren.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        loadChildren();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("child_alert_id", "child alert channel", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("alert for child safety");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if(manager != null){
                manager.createNotificationChannel(channel);
            }else{
                Toast.makeText(this, "Could not use notifications", Toast.LENGTH_SHORT).show();
            }
        }
        listenToAlerts();
    }

    //add child to recycle view
    private void loadChildren(){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference users = db.getReference("users");
        childList = new ArrayList<ChildPerson>();
        rvChildren.setAdapter(new ContactsAdapter<ChildPerson>(childList, this::showChildLocation));
        for(String childKey : parent.getContacts().keySet()){
            users.child(childKey).get()
                    .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful()){
                        ChildPerson child = task.getResult().getValue(ChildPerson.class);
                        childList.add(child);
                        rvChildren.getAdapter().notifyDataSetChanged();
                    }
                }
            });
        }
    }

    private void showChildLocation(Person person){
        Child child = (Child)parent.getContacts().get(person.getId());
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("latitude", child.getLat());
        intent.putExtra("longitude", child.getLon());
        startActivity(intent);
    }
    //look for changed in status
    private void listenToAlerts(){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference users = db.getReference("users");
        for(String childKey : parent.getContacts().keySet()){
            users.child(parent.getId()).child("contacts").child(childKey).addValueEventListener(this);
        }
    }

    private void showNotification(String message){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "child_alert_id" )
                .setSmallIcon(R.drawable.ic_crisis_alert)
                .setContentTitle("Child alert")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        NotificationManagerCompat notifManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
           ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.POST_NOTIFICATIONS}, 10);
            return;
        }else{
            notifManager.notify(110, builder.build());
        }

    }
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        Child child = snapshot.getValue(Child.class);
        //TODO read child lat long and name
        if(child.isHasAlert()){
            Optional<ChildPerson> opt = childList.stream().filter(c -> c.getId().equals(child.getId())).findFirst();
            if(opt.isPresent()){
                ChildPerson childPerson = opt.get();
                //child.getLat() + child.getLon()
                String notification = childPerson.fullname() + " is in " + getFullAddress(child.getLat(), child.getLon());
                showNotification(notification);
            }

        }

    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {
        Log.d("onCancelled", error.toString());
    }
    private String getFullAddress(double lat, double lon) {

        Geocoder geocoder;
        List<Address> addressList;
        geocoder = new Geocoder(this , Locale.getDefault());
        try {
            addressList = geocoder.getFromLocation(lat , lon , 1);
            String address = addressList.get(0).getAddressLine(0);
            String city = addressList.get(0).getLocality();
            String state = addressList.get(0).getAdminArea();
            String country = addressList.get(0).getCountryName();
            String postalCode = addressList.get(0).getPostalCode();
            String knowName = addressList.get(0).getFeatureName();
            return address+" "+city+" "+state+" "+country+" "+postalCode+" "+knowName;

        } catch (IOException e) {
            Log.d("getFullAddress", e.getMessage());
            return "address not available";
        }catch(IndexOutOfBoundsException e){
            return "address not available";
        }
    }
}