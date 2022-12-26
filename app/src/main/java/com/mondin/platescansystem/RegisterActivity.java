package com.mondin.platescansystem;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText name,email,password,cpassword;
    private Button btn_regis;
    TextView link_login;
    private ProgressBar loading;
    private FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        setContentView(R.layout.activity_register);
        name=findViewById(R.id.nameReg);
        loading=findViewById(R.id.loadingReg);
        email=findViewById(R.id.emailReg);
        password=findViewById(R.id.passReg);
        cpassword=findViewById(R.id.cpassReg);
        btn_regis=findViewById(R.id.btnReg);
        link_login=findViewById(R.id.linkLogin);
        btn_regis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_regis.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                if(validate()){
                    final String nameReg = name.getText().toString().trim();
                    String emailReg = email.getText().toString().trim();
                    String passReg = password.getText().toString().trim();
                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                        //we are connected to a network
                        firebaseAuth.createUserWithEmailAndPassword(emailReg,passReg)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()){

                                            DocumentReference docRef = firebaseFirestore
                                                    .collection("User Informations")
                                                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                            Map<String, String> data = new HashMap<>();
                                            data.put("Name", nameReg);
                                            docRef.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    firebaseFirestore.collection("Count")
                                                            .document("UserCount")
                                                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            DocumentSnapshot document = task.getResult();
                                                            if (document.exists()) {
                                                                int update = document.getLong("count").intValue() + 1;
                                                                firebaseFirestore.collection("Count")
                                                                        .document("UserCount")
                                                                        .update("count",update).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                   showToast("Error has occurred : "+e);
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            showToast("Error has occurred : "+e);
                                                        }
                                                    });

                                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                            .setDisplayName(nameReg)
                                                            .build();
                                                    firebaseAuth.getCurrentUser().updateProfile(profileUpdates);

                                                    firebaseAuth.getCurrentUser().sendEmailVerification()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    btn_regis.setVisibility(View.VISIBLE);
                                                                    loading.setVisibility(View.GONE);
                                                                    showToast("Registration Successful\nVerification Email Sent to " + firebaseAuth.getCurrentUser().getEmail());
                                                                    firebaseAuth.signOut();
                                                                    startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                                                                    finish();
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    btn_regis.setVisibility(View.VISIBLE);
                                                                    loading.setVisibility(View.GONE);
                                                                    showToast(e.getMessage());
                                                                }
                                                            });
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    btn_regis.setVisibility(View.VISIBLE);
                                                    loading.setVisibility(View.GONE);
                                                    showToast(e.getMessage());
                                                }
                                            });
                                        /*
                                        FirebaseDatabase.getInstance().getReference("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .setValue(nameReg).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    final FirebaseUser user = firebaseAuth.getCurrentUser();
                                                    user.sendEmailVerification()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    btn_regis.setVisibility(View.VISIBLE);
                                                                    loading.setVisibility(View.GONE);
                                                                    firebaseAuth.signOut();
                                                                    showToast("Registration Successful\nVerification Email Sent to " + user.getEmail());
                                                                    startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                                                                }
                                                            });
                                                }else{
                                                    btn_regis.setVisibility(View.VISIBLE);
                                                    loading.setVisibility(View.GONE);
                                                    Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                        */
                                        }else{
                                            btn_regis.setVisibility(View.VISIBLE);
                                            loading.setVisibility(View.GONE);
                                            showToast(task.getException().getMessage());
                                        }
                                    }
                                });
                    } else {
                        ConnectivityManager connectivityManager2 = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        Network network;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            network = connectivityManager2.getActiveNetwork();
                            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                            if (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                                //we are connected to network
                                firebaseAuth.createUserWithEmailAndPassword(emailReg,passReg)
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if(task.isSuccessful()){

                                                    DocumentReference docRef = firebaseFirestore
                                                            .collection("User Informations")
                                                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                    Map<String, String> data = new HashMap<>();
                                                    data.put("Name", nameReg);
                                                    docRef.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            firebaseAuth.getCurrentUser().sendEmailVerification()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            btn_regis.setVisibility(View.VISIBLE);
                                                                            loading.setVisibility(View.GONE);
                                                                            showToast("Registration Successful\nVerification Email Sent to " + firebaseAuth.getCurrentUser().getEmail());
                                                                            firebaseAuth.signOut();
                                                                            startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                                                                            finish();
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            btn_regis.setVisibility(View.VISIBLE);
                                                                            loading.setVisibility(View.GONE);
                                                                            showToast(e.getMessage());
                                                                        }
                                                                    });
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            btn_regis.setVisibility(View.VISIBLE);
                                                            loading.setVisibility(View.GONE);
                                                            showToast(e.getMessage());
                                                        }
                                                    });
                                                }else{
                                                    btn_regis.setVisibility(View.VISIBLE);
                                                    loading.setVisibility(View.GONE);
                                                    showToast(task.getException().getMessage());
                                                }
                                            }
                                        });
                            } else {
                                showToast("Please make sure you're connected to the internet and try again!");
                            }
                        } else {
                            showToast("Please make sure you're connected to the internet and try again!");
                        }
                    }
                }
            }
        });
        link_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                finish();
            }
        });
    }
    private boolean validate(){
        Boolean result = false;
        String c1 = name.getText().toString().trim();
        String c2 = email.getText().toString().trim();
        String c3 = password.getText().toString().trim();
        String c4 = cpassword.getText().toString().trim();

        if(c1.isEmpty() || c2.isEmpty() || c3.isEmpty() || c4.isEmpty()){
            showToast("Please fill up all fields");
            btn_regis.setVisibility(View.VISIBLE);
            loading.setVisibility(View.GONE);
        }else{
            if(!c3.equals(c4)){
                showToast("Passwords do not match, Please try again!");
                btn_regis.setVisibility(View.VISIBLE);
                loading.setVisibility(View.GONE);
            }else{
                if(!Patterns.EMAIL_ADDRESS.matcher(c2).matches()){
                    email.setError("Please Enter a valid email address");
                    email.requestFocus();
                    btn_regis.setVisibility(View.VISIBLE);
                    loading.setVisibility(View.GONE);
                }else{
                    result = true;
                }

            }
        }
        return result;
    }
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            loggedIn(currentUser);
        } else {
            ConnectivityManager connectivityManager2 = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            Network network;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                network = connectivityManager2.getActiveNetwork();
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                if (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    //we are connected to network
                    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                    loggedIn(currentUser);
                } else {
                    showToast("Please make sure you're connected to the internet!");
                }
            } else {
                showToast("Please make sure you're connected to the internet!");
            }
        }
    }
    private void loggedIn(FirebaseUser user) {
        if(user != null){
            startActivity(new Intent(RegisterActivity.this,MainActivity.class));
            finish();
        }else{
            //do nothing for now
        }
    }
    private void showToast(final String text) {
        Toast.makeText(RegisterActivity.this, text, Toast.LENGTH_SHORT).show();
    }
}
