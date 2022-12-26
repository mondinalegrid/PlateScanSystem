package com.mondin.platescansystem;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class VerifyActivity extends AppCompatActivity {
    Button verifyBtn;
    TextView notVerifyBtn;
    ImageView backVerify;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scroll_verify);

        firebaseAuth = FirebaseAuth.getInstance();
        //firebaseAuth.signOut();

        notVerifyBtn = findViewById(R.id.notVerifyBtn);
        backVerify = findViewById(R.id.backVerify);
        verifyBtn = findViewById(R.id.verifyBtn);
        backVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                startActivity(new Intent(VerifyActivity.this,LoginActivity.class));
                finish();
            }
        });
        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth = FirebaseAuth.getInstance();
                firebaseAuth.getCurrentUser().reload();
                if(firebaseAuth.getCurrentUser().isEmailVerified()){
                    showToast("Please login again to continue!");
                    firebaseAuth.signOut();
                    startActivity(new Intent(VerifyActivity.this,LoginActivity.class));
                    finish();
                }else{
                    showToast("Email is not verified please also check the email in your spam box");
                }
            }
        });
        notVerifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notVerifyBtn.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notVerifyBtn.setEnabled(true);
                    }
                }, 120000);

                firebaseAuth.getCurrentUser().sendEmailVerification()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                showToast("Verification Email sent!\nWait 2 minutes before using again");
                            }
                        });
            }
        });
    }

    @Override
    public void onBackPressed() {
        firebaseAuth.signOut();
        startActivity(new Intent(VerifyActivity.this,LoginActivity.class));
        finish();
    }
    private void showToast(final String text) {
        Toast.makeText(VerifyActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        firebaseAuth.signOut();
        firebaseAuth=null;
    }
}
