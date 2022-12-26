package com.mondin.platescansystem;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ActivityResults extends AppCompatActivity {
    EditText plateNoView,makeView,modelView,yearView,dateView,ltoView,ltoAlarmView,colorView,bodyTypeView;
    TextView editPlate,editMake,editModel,editYear,editDate,editLto,editLtoAlarm,editColor,editBodyType;
    ImageView plateImage;
    FirebaseFirestore firebaseFirestore;
    ImageView backFab;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        plateImage = findViewById(R.id.plateNoImage);
        plateNoView = findViewById(R.id.plateNumberView);
        makeView = findViewById(R.id.makeView);
        modelView = findViewById(R.id.modelView);
        yearView = findViewById(R.id.yearView);
        dateView = findViewById(R.id.dateView);
        ltoView = findViewById(R.id.ltoView);
        ltoAlarmView = findViewById(R.id.ltoAlarmNew);
        colorView = findViewById(R.id.colorView);
        bodyTypeView = findViewById(R.id.bodyTypeView);
        progressBar = findViewById(R.id.progBarResult);
        firebaseFirestore = FirebaseFirestore.getInstance();
        backFab = findViewById(R.id.backFab);
        editPlate = findViewById(R.id.editPlate);
        editMake = findViewById(R.id.editMake);
        editModel = findViewById(R.id.editModel);
        editYear = findViewById(R.id.editYear);
        editDate = findViewById(R.id.ltoRegEdit);
        editLto = findViewById(R.id.ltoEdit);
        editLtoAlarm = findViewById(R.id.ltoAlarmEditNew);
        editColor = findViewById(R.id.colorEdit);
        editBodyType = findViewById(R.id.bodyTypeEdit);
        backFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getIntent().getStringExtra("BACK") != null){
                    Fragment fragment;
                    fragment = new HistoryList();
                    if (fragment != null) {
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.content_frame, fragment);
                        ft.commit();
                    }
                }else{
                    startActivity(new Intent(ActivityResults.this,MainActivity.class));
                }
            }
        });
        progressBar.setVisibility(View.VISIBLE);
        plateImage.setVisibility(View.GONE);
        editPlate.setVisibility(View.GONE);
        plateNoView.setVisibility(View.GONE);
        editMake.setVisibility(View.GONE);
        makeView.setVisibility(View.GONE);
        editModel.setVisibility(View.GONE);
        modelView.setVisibility(View.GONE);
        editYear.setVisibility(View.GONE);
        yearView.setVisibility(View.GONE);
        editDate.setVisibility(View.GONE);
        dateView.setVisibility(View.GONE);
        editLto.setVisibility(View.GONE);
        ltoView.setVisibility(View.GONE);
        backFab.setVisibility(View.GONE);
        ltoAlarmView.setVisibility(View.GONE);
        colorView.setVisibility(View.GONE);
        bodyTypeView.setVisibility(View.GONE);
        editLtoAlarm.setVisibility(View.GONE);
        editColor.setVisibility(View.GONE);
        editBodyType.setVisibility(View.GONE);

        if(getIntent().getExtras() != null){
            final String plateNo = getIntent().getStringExtra("PLATENUMBER");
            final String imagefilepath = getIntent().getStringExtra("IMAGEFILEPATH");
            final Uri filepath = Uri.parse(imagefilepath);

            firebaseFirestore.collection("Plate Number").document(plateNo.replace(" ",""))
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if(documentSnapshot != null && documentSnapshot.exists()){

                            progressBar.setVisibility(View.GONE);
                            backFab.setVisibility(View.VISIBLE);
                            plateImage.setVisibility(View.VISIBLE);
                            editPlate.setVisibility(View.VISIBLE);
                            plateNoView.setVisibility(View.VISIBLE);
                            editMake.setVisibility(View.VISIBLE);
                            makeView.setVisibility(View.VISIBLE);
                            editModel.setVisibility(View.VISIBLE);
                            modelView.setVisibility(View.VISIBLE);
                            editYear.setVisibility(View.VISIBLE);
                            yearView.setVisibility(View.VISIBLE);
                            editDate.setVisibility(View.VISIBLE);
                            dateView.setVisibility(View.VISIBLE);
                            editLto.setVisibility(View.VISIBLE);
                            ltoView.setVisibility(View.VISIBLE);
                            ltoAlarmView.setVisibility(View.VISIBLE);
                            colorView.setVisibility(View.VISIBLE);
                            bodyTypeView.setVisibility(View.VISIBLE);
                            editLtoAlarm.setVisibility(View.VISIBLE);
                            editColor.setVisibility(View.VISIBLE);
                            editBodyType.setVisibility(View.VISIBLE);


                            plateNoView.setText(plateNo);
                            String path= filepath.getPath();
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = 2;
                            Bitmap bitmap = BitmapFactory.decodeFile(path,options);
                            plateImage.setImageBitmap(bitmap);
                            makeView.setText(documentSnapshot.getString("Make"));
                            bodyTypeView.setText(documentSnapshot.getString("Body Type"));
                            modelView.setText(documentSnapshot.getString("Series"));
                            colorView.setText(documentSnapshot.getString("Color"));
                            yearView.setText(documentSnapshot.getString("Year Model"));
                            dateView.setText(documentSnapshot.getString("Date"));
                            ltoAlarmView.setText(documentSnapshot.getString("LTO Alarm"));
                            ltoView.setText(documentSnapshot.getString("LTO Apprehension"));

                        }else{
                            showToast("No Matching Plate Number Found");
                            finish();
                            //Toast.makeText(getActivity(), "Document Snapshot is Null", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        showToast("ERROR : "+task.getException().getMessage());
                        //Toast.makeText(getActivity(),"ERROR : "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        //Log.d("FIRE_LOG", "ERROR : "+task.getException().getMessage());
                    }
                }
            });


        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ActivityResults.this,MainActivity.class));
    }

    private void showToast(final String text) {
        Toast.makeText(ActivityResults.this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        firebaseFirestore=null;
        plateImage=null;
        backFab=null;
        progressBar=null;
        plateNoView=null;
        makeView=null;
        modelView=null;
        yearView=null;
        dateView=null;
        ltoView=null;
        editPlate=null;
        editMake=null;
        editModel=null;
        editYear=null;
        editDate=null;
        editLto=null;
        ltoAlarmView=null;
        colorView=null;
        bodyTypeView=null;
        editLtoAlarm=null;
        editColor=null;
        editBodyType=null;
    }
}
