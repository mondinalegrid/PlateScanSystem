package com.mondin.platescansystem;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageProcessActivity extends AppCompatActivity {
    ImageView plateImage;
    TextView textView;
    ProgressBar progressBar;
    FloatingActionButton back;
    FirebaseFirestore firebaseFirestore;
    //String plateNumberValidate;
    //List<String> allMatches = new ArrayList<>();
    //List<String> allMatches = new ArrayList<String>();
    List<String> allMatches = new ArrayList();
    //int count = 1;
    Matcher matcher1;
    Uri filepath, filepath1;
    Bitmap bitmap,bitmap1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_process);
        plateImage=findViewById(R.id.plateImage);
        textView=findViewById(R.id.statusView);
        progressBar=findViewById(R.id.progressBarImageProcess);
        firebaseFirestore = FirebaseFirestore.getInstance();
        back=findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ImageProcessActivity.this,CameraActivity.class));
            }
        });
        filepath=null;
        filepath1=null;
        bitmap=null;
        bitmap1=null;

        filepath1=Uri.fromFile(new File(getExternalFilesDir(null), "pic.jpg"));
        String path1= filepath1.getPath();
        BitmapFactory.Options options1 = new BitmapFactory.Options();
        options1.inSampleSize = 2;
        bitmap1 = BitmapFactory.decodeFile(path1,options1);
        plateImage.setImageBitmap(bitmap1);

        filepath=Uri.fromFile(new File(getExternalFilesDir(null), "pic.jpg"));
        String path= filepath.getPath();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 3;
        bitmap = BitmapFactory.decodeFile(path,options);
        detectImage(bitmap);
    }
    private void detectImage(Bitmap bitmap){
        progressBar.setVisibility(View.VISIBLE);
        textView.setVisibility(View.GONE);
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        detector.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                processTxt(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast("No Text Detected");
                //textView.setText(Result);
                back.show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
    private void processTxt(FirebaseVisionText text){
        List<FirebaseVisionText.TextBlock> blocks = text.getTextBlocks();
        if (blocks.size() == 0) {
            String Result = "No Text Detected";
            showToast(Result);
            textView.setText(Result);
            back.show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        textView.setText("");
        for (FirebaseVisionText.TextBlock block : text.getTextBlocks()) {
            //String txt = block.getText();
            //textView.setTextSize(24);
            //showToast(block.getText());
            textView.append(" "+block.getText());

        }
        //final List<String> allMatches = new ArrayList();
        Pattern pattern = Pattern.compile(
                "(?i)\\b([A-Z]{3} ?[0-9]{3,4}|[A-Z]{2} ?[0-9]{4}|[0-9]{4} ?[A-Z]{2})\\b");
        Matcher matcher = pattern.matcher(textView.getText().toString().replace(".","")
        .replace("TC","").replace("2017","").replace("O","0").replace("5","6"));
        if(matcher.find()){
            matcher1 = pattern.matcher(textView.getText().toString().replace(".","")
                    .replace("TC","").replace("2017","").replace("O","0").replace("5","6"));
            while(matcher1.find()){
                allMatches.add(matcher1.group());
                //plateNumberValidate = matcher1.group(1);
            }
            firebaseFirestore.collection("Plate Number").document(allMatches.get(0).toUpperCase().replace(" ","").trim())
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if(documentSnapshot != null && documentSnapshot.exists()){
                            showToast("Plate Number Detected! "+allMatches.get(0));
                            FileOutputStream output=null;
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            SimpleDateFormat sdft = new SimpleDateFormat("hh-mm");
                            String time = sdft.format(Calendar.getInstance().getTime());
                            String date = sdf.format(Calendar.getInstance().getTime());
                            BitmapDrawable draw = (BitmapDrawable) plateImage.getDrawable();
                            Bitmap bitmap = draw.getBitmap();
                            final String fileName = date+" "+time+" "+" "+allMatches.get(0).toUpperCase()+".jpg";
                            try {
                                File outFile = new File(getExternalFilesDir(null), fileName);
                                output = new FileOutputStream(outFile);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
                                output.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                                showToast("Error Occurred on creating new image");
                            } finally {
                                if (null != output) {
                                    try {
                                        bitmap.recycle();
                                        output.close();
                                    } catch (IOException e) {
                                        showToast("Error Occurred on creating new image");
                                        e.printStackTrace();
                                    }
                                }
                            }

                            DocumentReference docRef = firebaseFirestore
                                    .collection("User Informations")
                                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .collection("History").document();
                            Map<String, String> data = new HashMap<>();
                            data.put("Plate Number", allMatches.get(0).toUpperCase());
                            data.put("Photo", fileName);
                            data.put("Date", date);
                            data.put("Time", time);
                            final SimpleDateFormat timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                            data.put("TimeStamp",timestamp.format(Calendar.getInstance().getTime()));
                            docRef.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Uri filepath=Uri.fromFile(new File(getExternalFilesDir(null), fileName));
                                    Intent intent = new Intent (ImageProcessActivity.this, MainActivity.class);
                                    intent.putExtra("RESULT", "openResults");
                                    intent.putExtra("IMAGEFILEPATH",filepath.toString());
                                    intent.putExtra("PLATENUMBER",allMatches.get(0).toUpperCase().replace(" ","").trim());
                                    startActivity(intent);
                                    finish();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressBar.setVisibility(View.GONE);
                                    showToast(e.getMessage());
                                }
                            });
                        }else{
                            if(allMatches.size()<=2 && allMatches.size()>=2){
                                firebaseFirestore.collection("Plate Number").document(allMatches.get(1).toUpperCase().replace(" ","").trim())
                                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            DocumentSnapshot documentSnapshot = task.getResult();
                                            if(documentSnapshot != null && documentSnapshot.exists()){
                                                showToast("Plate Number Detected! "+allMatches.get(1));
                                                FileOutputStream output=null;
                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                                SimpleDateFormat sdft = new SimpleDateFormat("hh-mm");
                                                String time = sdft.format(Calendar.getInstance().getTime());
                                                String date = sdf.format(Calendar.getInstance().getTime());
                                                BitmapDrawable draw = (BitmapDrawable) plateImage.getDrawable();
                                                Bitmap bitmap = draw.getBitmap();
                                                final String fileName = date+" "+time+" "+" "+allMatches.get(1).toUpperCase()+".jpg";
                                                try {
                                                    File outFile = new File(getExternalFilesDir(null), fileName);
                                                    output = new FileOutputStream(outFile);
                                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
                                                    output.flush();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                    showToast("Error Occurred on creating new image");
                                                } finally {
                                                    if (null != output) {
                                                        try {
                                                            bitmap.recycle();
                                                            output.close();
                                                        } catch (IOException e) {
                                                            showToast("Error Occurred on creating new image");
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }

                                                DocumentReference docRef = firebaseFirestore
                                                        .collection("User Informations")
                                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                        .collection("History").document();
                                                Map<String, String> data = new HashMap<>();
                                                data.put("Plate Number", allMatches.get(1).toUpperCase());
                                                data.put("Photo", fileName);
                                                data.put("Date", date);
                                                data.put("Time", time);
                                                final SimpleDateFormat timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                                                data.put("TimeStamp",timestamp.format(Calendar.getInstance().getTime()));
                                                docRef.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Uri filepath=Uri.fromFile(new File(getExternalFilesDir(null), fileName));
                                                        Intent intent = new Intent (ImageProcessActivity.this, MainActivity.class);
                                                        intent.putExtra("RESULT", "openResults");
                                                        intent.putExtra("IMAGEFILEPATH",filepath.toString());
                                                        intent.putExtra("PLATENUMBER",allMatches.get(1).toUpperCase().replace(" ","").trim());
                                                        startActivity(intent);
                                                        finish();
                                                        progressBar.setVisibility(View.GONE);
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        progressBar.setVisibility(View.GONE);
                                                        showToast(e.getMessage());
                                                    }
                                                });
                                            }else{
                                                textView.setVisibility(View.VISIBLE);
                                                textView.setText(allMatches.get(1).toUpperCase().replace(" ","").trim());
                                                showToast("No Match! "+allMatches.get(1).toUpperCase().replace(" ","").trim());
                                                back.show();
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        }else{
                                            showToast("ERROR : "+task.getException().getMessage());
                                            textView.setVisibility(View.VISIBLE);
                                            back.show();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });
                            }else{
                                textView.setVisibility(View.VISIBLE);
                                textView.setText(allMatches.get(0).toUpperCase().replace(" ","").trim());
                                showToast("No Match! "+allMatches.get(0).toUpperCase().replace(" ","").trim());
                                back.show();
                                progressBar.setVisibility(View.GONE);
                            }

                        }
                    }else{
                        showToast("ERROR : "+task.getException().getMessage());
                        textView.setVisibility(View.VISIBLE);
                        back.show();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        }else{
            textView.setVisibility(View.VISIBLE);
            showToast("No Match! "+textView.getText().toString().replace(".",""));
            back.show();
            progressBar.setVisibility(View.GONE);
        }
        /*
        final String plateNumberValidate=textView.getText().toString().replace(".","")
                .replace("-","")
                .replace("PHILIPPINES 2000","")
                .replace("MC","")
                .replace("NCR","")
                .replace("REGION 6","")
                .replace("PILIPINAS","")
                .replace("FOR REGISTRATION","")
                .replace("REGISTERED","")
                .replace("TOYOTA","")
                .replace("MATATAG NA REPUBLIKA","")
                .replace("MATATAG","")
                .replace("NA","")
                .replace("REPUBLIKA","").trim();
        if(plateNumberExist(plateNumberValidate)){
            showToast("Plate Number Detected!");
            textView.setText(plateNumberValidate);
            //back.show();
            //progressBar.setVisibility(View.GONE);

            FileOutputStream output=null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdft = new SimpleDateFormat("hh-mm");
            //sdft.setTimeZone(TimeZone.getTimeZone("UTC"));
            String time = sdft.format(Calendar.getInstance().getTime());
            String date = sdf.format(Calendar.getInstance().getTime());
            BitmapDrawable draw = (BitmapDrawable) plateImage.getDrawable();
            Bitmap bitmap = draw.getBitmap();
            final String fileName = date+" "+time+" "+" "+plateNumberValidate.toUpperCase()+".jpg";
            try {
                //File outFile = new File(getExternalFilesDir(null), plateNumberValidate.toUpperCase()+" "+ dateTime +".jpg");
                //File sdCard = Environment.getExternalStorageDirectory();
                //File dir = new File(sdCard.getAbsolutePath() + "/camtest");
                //dir.mkdirs();
                //String fileName = String.format("%d.jpg", System.currentTimeMillis());
                //File outFile = new File(dir, fileName);
                File outFile = new File(getExternalFilesDir(null), fileName);
                output = new FileOutputStream(outFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
                output.flush();
                //showToast("Saved Image");
            } catch (IOException e) {
                e.printStackTrace();
                showToast("Error Occurred on creating new image1");
            } finally {
                if (null != output) {
                    try {
                        bitmap.recycle();
                        output.close();
                    } catch (IOException e) {
                        showToast("Error Occurred on creating new image2");
                        e.printStackTrace();
                    }
                }
            }

            DocumentReference docRef = firebaseFirestore
                    .collection("User Informations")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection("History").document();
            Map<String, String> data = new HashMap<>();
            data.put("Plate Number", plateNumberValidate.toUpperCase());
            data.put("Photo", fileName);
            data.put("Date", date);
            data.put("Time", time);
            final SimpleDateFormat timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
            data.put("TimeStamp",timestamp.format(Calendar.getInstance().getTime()));
            docRef.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    //Uri filepath=Uri.fromFile(new File(getExternalFilesDir(null), "pic.jpg"));
                    Uri filepath=Uri.fromFile(new File(getExternalFilesDir(null), fileName));
                    Intent intent = new Intent (ImageProcessActivity.this, MainActivity.class);
                    intent.putExtra("RESULT", "openResults");
                    intent.putExtra("IMAGEFILEPATH",filepath.toString());
                    intent.putExtra("PLATENUMBER",plateNumberValidate.toUpperCase());
                    startActivity(intent);
                    progressBar.setVisibility(View.GONE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                    showToast(e.getMessage());
                }
            });
        }else{
            showToast("No Match! ");
            //textView.setText(plateNumberValidate);
            back.show();
            progressBar.setVisibility(View.GONE);
        }
        */
    }
    public static boolean plateNumberExist(String target) {
        return Pattern.compile("^([A-Za-z]{3} ?[0-9]{3})?([A-Za-z]{3} ?[0-9]{4})?([A-Za-z]{2} ?[0-9]{4})?([0-9]{4} ?[A-Za-z]{2})?$").matcher(target).matches();
    }

    private void showToast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        textView.setText(null);
        plateImage.setImageURI(null);
        progressBar.setVisibility(View.GONE);
        filepath=null;
        filepath1=null;
        bitmap=null;
        bitmap1=null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ImageProcessActivity.this,CameraActivity.class));
        finish();
    }

}
