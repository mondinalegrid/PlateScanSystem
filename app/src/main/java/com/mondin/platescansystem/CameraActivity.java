package com.mondin.platescansystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class CameraActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        /*
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, Camera2BasicFragment.newInstance())
                    .commit();
        }
        */
        if(getIntent().getStringExtra("PROFILE") != null){
            getIntent().removeExtra("PROFILE");
            Bundle bundle = new Bundle();
            bundle.putString("PROFILE","PROFILE");
            Camera2BasicFragment camera2BasicFragment = new Camera2BasicFragment();
            camera2BasicFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.container, camera2BasicFragment).commit();
            //Toast.makeText(this, "Started for take picture", Toast.LENGTH_SHORT).show();
        }else if(getIntent().getStringExtra("TESTSCAN") != null){
            getIntent().removeExtra("TESTSCAN");
            Bundle bundle = new Bundle();
            bundle.putString("TESTSCAN","TESTSCAN");
            Camera2BasicFragment camera2BasicFragment = new Camera2BasicFragment();
            camera2BasicFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.container, camera2BasicFragment).commit();
        }else{
            if (null == savedInstanceState) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, Camera2BasicFragment.newInstance())
                        .commit();
               // Toast.makeText(this, "Started for take plate number", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onBackPressed() {
            startActivity(new Intent(this,MainActivity.class));
            finish();
    }
}
