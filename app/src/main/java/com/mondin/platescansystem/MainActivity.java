package com.mondin.platescansystem;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.io.File;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    FirebaseAuth firebaseAuth;
    TextView nameNav,emailNav;
    FirebaseFirestore firebaseFirestore;
    ActionBarDrawerToggle toggle;
    ImageView navPic;
    Integer SELECT_FILE=0;
    //REQUEST_CAMERA=1,
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    //StorageReference storageReference = firebaseStorage.getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        //checkVerifiedEmail();

        if(checkInternet()){
            checkVerifiedEmail();
            displaySelectedScreen(R.id.nav_history);
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,CameraActivity.class));
                finish();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        navPic = headerView.findViewById(R.id.picNav);
        navPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });

        //displaySelectedScreen(R.id.nav_gallery);
if(getIntent().getStringExtra("RESULT") != null){
    switch (getIntent().getStringExtra("RESULT")){
        case "openResults":
            /*
            Bundle bundle = new Bundle();
            bundle.putString("IMAGEFILEPATH",getIntent().getStringExtra("IMAGEFILEPATH"));
            bundle.putString("PLATENUMBER",getIntent().getStringExtra("PLATENUMBER"));
            FragmentResults fragmentResults = new FragmentResults();
            fragmentResults.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragmentResults).commit();
            getIntent().removeExtra("RESULT");
            getIntent().removeExtra("IMAGEFILEPATH");
            getIntent().removeExtra("PLATENUMBER");
            */
            Intent intent = new Intent (MainActivity.this, ActivityResults.class);
            intent.putExtra("IMAGEFILEPATH", getIntent().getStringExtra("IMAGEFILEPATH"));
            intent.putExtra("PLATENUMBER",getIntent().getStringExtra("PLATENUMBER"));
            startActivity(intent);
            getIntent().removeExtra("RESULT");
            getIntent().removeExtra("IMAGEFILEPATH");
            getIntent().removeExtra("PLATENUMBER");
            finish();
            break;
    }
}
    }

    @Override
    public void onResume(){
        super.onResume();
        if(getIntent().getStringExtra("RESULT") != null){
            switch (getIntent().getStringExtra("RESULT")){
                case "SETIMAGE":
                    getIntent().removeExtra("RESULT");
                    Uri filepath=Uri.fromFile(new File(getExternalFilesDir(null), "pic.jpg"));
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(filepath)
                            .build();
                    firebaseAuth.getCurrentUser().updateProfile(profileUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            firebaseAuth.getCurrentUser().reload();
                            Picasso.get()
                                    .load(firebaseAuth.getCurrentUser().getPhotoUrl())
                                    .resize(navPic.getWidth(), navPic.getHeight())
                                    .centerCrop()
                                    .into(navPic);
                            //showToast(firebaseAuth.getCurrentUser().getPhotoUrl().toString());
                            showToast("Updated Profile Picture");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showToast("Error occurred setting up profile image "+e.getMessage());
                        }
                    });
                    //getIntent().removeExtra("SETIMAGE");
                    break;
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if(checkInternet()){
                if(firebaseAuth.getCurrentUser().getPhotoUrl()!=null){

                    Picasso.get()
                            .load(firebaseAuth.getCurrentUser().getPhotoUrl())
                            .resize(navPic.getWidth(), navPic.getHeight())
                            .centerCrop()
                            .into(navPic);

                }
            }
        }

    }

    private void SelectImage(){
        final CharSequence[] items={"Take a picture","Select from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Profile Image");

        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Take a picture")) {

                    //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //startActivityForResult(intent, REQUEST_CAMERA);
                    Intent intent = new Intent (MainActivity.this, CameraActivity.class);
                    intent.putExtra("PROFILE", "PROFILE");
                    startActivity(intent);
                    finish();
                } else if (items[i].equals("Select from Gallery")) {

                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent, SELECT_FILE);

                } else if (items[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();

    }

    @Override
    public  void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode,data);

        if(resultCode== Activity.RESULT_OK){

            /*
            if(requestCode==REQUEST_CAMERA){

                Bundle bundle = data.getExtras();
                final Bitmap bmp = (Bitmap) bundle.get("data");
                //navPic.setImageBitmap(bmp);

            }else */
            if(requestCode==SELECT_FILE){

                Uri selectedImageUri = data.getData();
                //ivImage.setImageURI(selectedImageUri);
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setPhotoUri(selectedImageUri)
                        .build();
                firebaseAuth.getCurrentUser().updateProfile(profileUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        navPic.setImageBitmap(null);
                        Picasso.get()
                                .load(firebaseAuth.getCurrentUser().getPhotoUrl())
                                .resize(navPic.getWidth(), navPic.getHeight())
                                .centerCrop()
                                .into(navPic);
                        showToast("Updated Profile Picture");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast("Error occurred setting up profile image "+e.getMessage());
                    }
                });
            }

        }
    }


    private void checkVerifiedEmail(){
        if(firebaseAuth.getCurrentUser().isEmailVerified()){
            //showToast("Email is Verified ");
        }else{
            startActivity(new Intent(MainActivity.this,VerifyActivity.class));
            finish();
            //showToast("Email is not Verified");
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (!(fragment instanceof IOnBackPressed) || !((IOnBackPressed) fragment).onBackPressed()) {
            //getSupportFragmentManager().popBackStack();
            //if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            //    getSupportFragmentManager().popBackStack();
            //}
            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setNavigationIcon(null);
            toggle = new ActionBarDrawerToggle(
                    MainActivity.this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
            displaySelectedScreen(R.id.nav_history);
        }else if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            moveTaskToBack(true);
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_signout){
            firebaseAuth.signOut();
            showToast("Signed Out");
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            finish();
        }/*else if (id == android.R.id.home){
            showToast("worked back");
        }*/

        return super.onOptionsItemSelected(item);
    }

    /*
    @Override
    public boolean onSupportNavigateUp() {
        //This method is called when the up button is pressed. Just the pop back stack.
        //getSupportFragmentManager().popBackStack();
        showToast("worked back 2");
        return true;
    }
    */

    public void changeToolbar(){

        final Toolbar toolbar = findViewById(R.id.toolbar);

        //final Drawable backarrow = getResources().getDrawable(R.drawable.ic_back);
        //backarrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        //toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
       // toolbar.setNavigationIcon(backarrow);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back_white));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbar.setNavigationIcon(null);
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                toggle = new ActionBarDrawerToggle(
                        MainActivity.this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
                drawer.addDrawerListener(toggle);
                toggle.syncState();
                //toggle.setDrawerIndicatorEnabled(true);
                displaySelectedScreen(R.id.nav_history);
            }
        });
       // toggle.setDrawerIndicatorEnabled(false);
//getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//getSupportActionBar().setHomeButtonEnabled(true);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
       /*
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            checkVerifiedEmail();
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        */
        displaySelectedScreen(item.getItemId());
        return true;
    }

    private void displaySelectedScreen(int itemId) {

        //creating fragment object
        Fragment fragment = null;

        //initializing the fragment object which is selected
        switch (itemId) {
            case R.id.nav_history:
                //checkVerifiedEmail();
                if(checkInternet()){
                    fragment = new HistoryList();
                }
                break;
            case R.id.nav_resetpass:
                if(checkInternet()){
                    AlertDialog.Builder builder1 = new AlertDialog.Builder (MainActivity.this);
                    builder1.setMessage("Reset password");
                    builder1.setCancelable(false);
                    builder1.setTitle("Are you sure?");
                    builder1.setPositiveButton(
                            "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, int id) {
                                    firebaseAuth.sendPasswordResetEmail(firebaseAuth.getCurrentUser().getEmail())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        showToast("We have sent you instructions to reset your password!");
                                                        dialog.dismiss();
                                                    } else {
                                                        showToast("Failed to send reset email!");
                                                        dialog.dismiss();
                                                    }
                                                }
                                            });
                                }
                            });
                    builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
                break;
            case R.id.nav_signout:
                firebaseAuth.signOut();
                showToast("Signed Out");
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                finish();
                break;
                /*
            case R.id.nav_gallery:
                startActivity(new Intent(MainActivity.this,CropActivity.class));
                break;
                */
                /*
            case R.id.nav_gallery:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                builder1.setMessage("Please make sure you're connected to the internet and try again!");
                builder1.setCancelable(false);

                builder1.setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });


                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
                break;
                */
                /*
            case R.id.nav_slideshow:
                fragment = new HistoryList();
                break;
                */
        }

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            //.addToBackStack( "historylayout" )
            ft.replace(R.id.content_frame, fragment);
            //ft.commitAllowingStateLoss();
            ft.commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private void showToast(final String text) {
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
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
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                    builder1.setMessage("Please make sure you're connected to the internet and try again!");
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
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                builder1.setMessage("Please make sure you're connected to the internet and try again!");
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
    private boolean checkInternet(){
        Boolean result = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            result = true;
        } else {
            ConnectivityManager connectivityManager2 = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            Network network;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                network = connectivityManager2.getActiveNetwork();
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                if (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    //we are connected to network
                    result = true;
                } else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                    builder1.setMessage("Please make sure you're connected to the internet and try again!");
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
                }
            } else {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                builder1.setMessage("Please make sure you're connected to the internet and try again!");
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
            }
        }
        return result;
    }

    private void loggedIn(FirebaseUser user) {
        if(user != null){
            DocumentReference docRef = firebaseFirestore.collection("User Informations")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            nameNav = findViewById(R.id.nameNav);
                            emailNav = findViewById(R.id.emailNav);
                            firebaseAuth = FirebaseAuth.getInstance();
                            emailNav.setText(firebaseAuth.getCurrentUser().getEmail());
                            nameNav.setText(document.getString("Name"));
                            /*
                            firebaseFirestore.collection("User Informations")
                                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .collection("History")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                displaySelectedScreen(R.id.nav_history);
                                            } else {
                                                //Log.w(TAG, "Error getting documents.", task.getException());
                                                //no history
                                                setContentView(R.layout.activity_main);
                                            }
                                        }
                                    });
                                    */
                        } else {
                            //Log.d(TAG, "No such document");
                            loggedIn(firebaseAuth.getCurrentUser());
                        }
                    } else {
                        //Log.d(TAG, "get failed with ", task.getException());
                        showToast("Get failed with "+task.getException().getMessage());
                    }
                }
            });

        }else{
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        firebaseAuth = null;
        firebaseFirestore=null;
    }
}
