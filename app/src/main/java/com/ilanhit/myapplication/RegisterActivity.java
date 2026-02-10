package com.ilanhit.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.ilanhit.myapplication.models.ChildPerson;
import com.ilanhit.myapplication.models.ParentPerson;
import com.ilanhit.myapplication.models.Person;
import com.ilanhit.myapplication.models.PersonType;

import java.util.Arrays;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText etFirstName;
    private EditText etLastName;
    private EditText etPhone;
    private EditText etEmail;
    private EditText etPassword;
    private Spinner spnPersonType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        spnPersonType = findViewById(R.id.spnPersonType);

        ArrayAdapter<PersonType> personTypeAdapter = new ArrayAdapter<PersonType>(this, android.R.layout.simple_spinner_item, Arrays.asList(PersonType.values()));
        spnPersonType.setAdapter(personTypeAdapter);
        Button btnDoRegister = findViewById(R.id.btnDoRegister);
        btnDoRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View view){

        String firstName = etFirstName.getText().toString();
        String lastName = etLastName.getText().toString();
        String phone =etPhone.getText().toString();
        String email= etEmail.getText().toString();
        String password =etPassword.getText().toString();

        if(firstName.equals("")){
            Toast.makeText(this, "Please enter your first name", Toast.LENGTH_SHORT).show();
            return;
        }
        if(firstName.length()< 2){
            Toast.makeText(this, "First name must contain at least two letters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (lastName.equals("")) {
            Toast.makeText(this, "Please enter your last name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (lastName.length() < 2) {
            Toast.makeText(this, "Last name must contain at least two letters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone.equals("")) {
            Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!phone.matches("\\d{10}")) { // adjust pattern based on your local format
            Toast.makeText(this, "Phone number must be 10 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        if (email.equals("")) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.equals("") || password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        PersonType personType = (PersonType)spnPersonType.getSelectedItem();


        // register in firebase auth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            // save person in firebase realtime
                            // create person class and create person object
                            Person person = null;
                            if(personType == PersonType.Parent){
                                person = new ParentPerson(firstName, lastName, phone, email);
                            }else{
                                person = new ChildPerson(firstName, lastName, phone, email);
                            }
                            String userUid = auth.getUid();  // uid of the user in authentication system
                            createPerson(person, userUid);
                        }else{
                            Log.d("signInWithEmailAndPassword", task.getException().getMessage());
                            Toast.makeText(RegisterActivity.this, "failed creating person: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void createPerson(Person person, String uid){
        person.setId(uid);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users").child(uid).setValue(person)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(RegisterActivity.this, "Person created: " + person.getFirstName(), Toast.LENGTH_SHORT).show();
                            if(person.getPersonType().equals(PersonType.Parent)){
                                Intent intent = new Intent(RegisterActivity.this, ParentActivity.class);
                                startActivity(intent);
                                finish();
                            }else{
                                Intent intent = new Intent(RegisterActivity.this, ChildActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }else{
                            Log.d("createPerson", task.getException().getMessage());
                            Toast.makeText(RegisterActivity.this, "failed creating person: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}