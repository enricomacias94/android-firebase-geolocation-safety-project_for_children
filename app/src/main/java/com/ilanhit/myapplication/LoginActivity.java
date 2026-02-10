package com.ilanhit.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.ilanhit.myapplication.models.ChildPerson;
import com.ilanhit.myapplication.models.ParentPerson;
import com.ilanhit.myapplication.models.Person;
import com.ilanhit.myapplication.models.PersonType;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText etLoginEmail;
    private EditText etLoginPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        Button btnDoLogin = findViewById(R.id.btnDoLogin);
        btnDoLogin.setOnClickListener(this);
    }

    //signe avec firebase
    @Override
    public void onClick(View view){
        String email= etLoginEmail.getText().toString();
        String password =etLoginPassword.getText().toString();
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

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            // save person in firebase realtime
                            String userUid = auth.getUid();  // uid of the user in authentication system
                            getPerson(userUid);
                        }else{
                            Log.d("signInWithEmailAndPassword", task.getException().getMessage());
                            Toast.makeText(LoginActivity.this, "failed creating person: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
//get person from firebase
    private void getPerson(String uid){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users").child(uid).get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(task.isSuccessful()){

                            DataSnapshot snapshot = task.getResult();
                            if(snapshot == null){
                                Toast.makeText(LoginActivity.this, "error loading data, please try again", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if(snapshot.child("personType").getValue().equals("Parent")){
                                ParentPerson person = task.getResult().getValue(ParentPerson.class);
                                Intent intent = new Intent(LoginActivity.this, ParentActivity.class);
                                intent.putExtra("person", person);
                                startActivity(intent);
                                finish();
                            }else{
                                ChildPerson person = task.getResult().getValue(ChildPerson.class);
                                Intent intent = new Intent(LoginActivity.this, ChildActivity.class);
                                intent.putExtra("person", person);
                                startActivity(intent);
                                finish();
                            }

                        }else{
                            Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}