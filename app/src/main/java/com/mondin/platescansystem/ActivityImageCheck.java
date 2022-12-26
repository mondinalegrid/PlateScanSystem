package com.mondin.platescansystem;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

public class ActivityImageCheck extends AppCompatActivity {
    ImageView profImage;
Button confirm,cancel,retry;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_check);
        profImage=findViewById(R.id.profImage);
        Uri filepath=Uri.fromFile(new File(getExternalFilesDir(null), "pic.jpg"));
        String path= filepath.getPath();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        Bitmap bitmap = BitmapFactory.decodeFile(path,options);
        profImage.setImageBitmap(bitmap);

        confirm=findViewById(R.id.confirmCheck);
        cancel=findViewById(R.id.cancelCheck);
        retry=findViewById(R.id.retryCheck);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (ActivityImageCheck.this, MainActivity.class);
                intent.putExtra("RESULT", "SETIMAGE");
                //intent.putExtra("SETIMAGE", "SETIMAGE");
                startActivity(intent);
                finish();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityImageCheck.this,MainActivity.class));
                finish();
            }
        });
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (ActivityImageCheck.this, CameraActivity.class);
                intent.putExtra("PROFILE", "PROFILE");
                startActivity(intent);
                finish();
            }
        });
    }
}
