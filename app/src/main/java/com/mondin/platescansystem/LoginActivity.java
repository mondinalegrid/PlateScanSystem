package com.mondin.platescansystem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    TextView link_register,forgotpass,title;
    private FirebaseAuth mAuth;
    Button login;
    EditText email,password;
    ProgressBar loading;
    ProgressDialog progressDialog;
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            login=findViewById(R.id.btnLogin);
            email=findViewById(R.id.loginEmail);
            password=findViewById(R.id.passLogin);
            forgotpass=findViewById(R.id.forgotPass);
            link_register=findViewById(R.id.linkRegister);
            title=findViewById(R.id.loginTitle);
            email.setVisibility(View.VISIBLE);
            password.setVisibility(View.VISIBLE);
            login.setVisibility(View.VISIBLE);
            forgotpass.setVisibility(View.VISIBLE);
            link_register.setVisibility(View.VISIBLE);
            title.setVisibility(View.VISIBLE);
        }
    };
    Button testbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //R.layout.activity_login dati walang 1
        setContentView(R.layout.activity_login1);
        testbtn=findViewById(R.id.testScanBtn);
        testbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,testScanner.class));
            }
        });
        mAuth = FirebaseAuth.getInstance();
        link_register=findViewById(R.id.linkRegister);
        loading=findViewById(R.id.progressBarLogin);
        link_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                finish();
            }
        });
        email=findViewById(R.id.loginEmail);
        password=findViewById(R.id.passLogin);
        forgotpass=findViewById(R.id.forgotPass);
        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,ResetPassword.class));
            }
        });
        login=findViewById(R.id.btnLogin);
        handler.postDelayed(runnable, 2000);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(LoginActivity.this,
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Authenticating...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setCancelable(false);
                progressDialog.show();

                //login.setVisibility(View.GONE);
                //loading.setVisibility(View.VISIBLE);
                if(validate()){
                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                        //we are connected to a network
                        String emailGet=email.getText().toString();
                        String passwordGet=password.getText().toString();
                        mAuth.signInWithEmailAndPassword(emailGet,passwordGet)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            loggedIn(user);
                                            //login.setVisibility(View.VISIBLE);
                                            //loading.setVisibility(View.GONE);
                                            progressDialog.dismiss();
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            showToast("Email or Password is Incorrect!");
                                            //login.setVisibility(View.VISIBLE);
                                            //loading.setVisibility(View.GONE);
                                            progressDialog.dismiss();
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
                                String emailGet=email.getText().toString();
                                String passwordGet=password.getText().toString();
                                mAuth.signInWithEmailAndPassword(emailGet,passwordGet)
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    // Sign in success, update UI with the signed-in user's information
                                                    FirebaseUser user = mAuth.getCurrentUser();
                                                    loggedIn(user);
                                                    //login.setVisibility(View.VISIBLE);
                                                    //loading.setVisibility(View.GONE);
                                                    progressDialog.dismiss();
                                                } else {
                                                    // If sign in fails, display a message to the user.
                                                    showToast("Email or Password is Incorrect!");
                                                    //login.setVisibility(View.VISIBLE);
                                                    //loading.setVisibility(View.GONE);
                                                    progressDialog.dismiss();
                                                }
                                            }
                                        });
                            } else {
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(LoginActivity.this);
                                builder1.setMessage("Please make sure you're connected to the internet and try again!");
                                builder1.setCancelable(false);
                                builder1.setTitle("Connection Error");
                                builder1.setPositiveButton(
                                        "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                                //login.setVisibility(View.VISIBLE);
                                                //loading.setVisibility(View.GONE);
                                                progressDialog.dismiss();
                                            }
                                        });
                                AlertDialog alert11 = builder1.create();
                                alert11.show();
                                //showToast("Please make sure you're connected to the internet and try again!");
                            }
                        } else {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(LoginActivity.this);
                            builder1.setMessage("Please make sure you're connected to the internet and try again!");
                            builder1.setCancelable(false);
                            builder1.setTitle("Connection Error");
                            builder1.setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            //login.setVisibility(View.VISIBLE);
                                            //loading.setVisibility(View.GONE);
                                            progressDialog.dismiss();
                                        }
                                    });
                            AlertDialog alert11 = builder1.create();
                            alert11.show();
                            //showToast("Please make sure you're connected to the internet and try again!");
                        }
                    }
                }
            }
        });
    }

    private boolean validate(){
        Boolean result = false;
        String c2 = email.getText().toString().trim();
        String c3 = password.getText().toString().trim();

        if(c2.isEmpty() || c3.isEmpty()){
            showToast("Please fill up all fields");
            //login.setVisibility(View.VISIBLE);
            //loading.setVisibility(View.GONE);
            progressDialog.dismiss();
        }else{
                if(!Patterns.EMAIL_ADDRESS.matcher(c2).matches()){
                    email.setError("Please Enter a valid email address");
                    email.requestFocus();
                    //login.setVisibility(View.VISIBLE);
                    //loading.setVisibility(View.GONE);
                    progressDialog.dismiss();
                }else{
                    result = true;
                }
        }
        return result;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            FirebaseUser currentUser = mAuth.getCurrentUser();
            loggedIn(currentUser);
        } else {
            ConnectivityManager connectivityManager2 = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            Network network;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                network = connectivityManager2.getActiveNetwork();
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                if (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    //we are connected to network
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    loggedIn(currentUser);
                } else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(LoginActivity.this);
                    builder1.setMessage("Please make sure you're connected to the internet");
                    builder1.setCancelable(false);
                    builder1.setTitle("Connection Error");
                    builder1.setPositiveButton(
                            "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                    //showToast("Please make sure you're connected to the internet!");
                }
            } else {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(LoginActivity.this);
                builder1.setMessage("Please make sure you're connected to the internet");
                builder1.setCancelable(false);
                builder1.setTitle("Connection Error");
                builder1.setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert11 = builder1.create();
                alert11.show();
                //showToast("Please make sure you're connected to the internet!");
            }
        }
    }
    private void loggedIn(FirebaseUser user) {
        if(user != null){
            startActivity(new Intent(LoginActivity.this,MainActivity.class));
            finish();
        }else{
            //do nothing for now
        }
    }
    private void showToast(final String text) {
        Toast.makeText(LoginActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        email=null;
        password=null;
        mAuth=null;
        login=null;
        link_register=null;
        loading=null;
        title=null;
    }
}
