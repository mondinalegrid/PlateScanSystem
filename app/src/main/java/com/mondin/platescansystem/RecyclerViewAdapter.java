package com.mondin.platescansystem;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<String> mplateNos;
    private ArrayList<String> mplateNoLinks;
    private ArrayList<String> mDateTimes;
    private ArrayList<String> docIDs;
    private Context mContext;

    public RecyclerViewAdapter(Context context, ArrayList<String> plateNo, ArrayList<String> plateNoLink, ArrayList<String> dateTime, ArrayList<String> docID ) {
        mplateNos = plateNo;
        mplateNoLinks = plateNoLink;
        mDateTimes = dateTime;
        docIDs = docID;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //Log.d(TAG, "onBindViewHolder: called.");

        //Glide.with(mContext)
          //      .asBitmap()
            //    .load(mImages.get(position))
              //  .into(holder.image);

        Uri filepath=Uri.parse(mplateNoLinks.get(position));
        String path= filepath.getPath();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        Bitmap bitmap = BitmapFactory.decodeFile(path,options);
        holder.mplateNoLink.setImageBitmap(bitmap);

        holder.mPlateNo.setText(mplateNos.get(position));
        holder.docID.setText(docIDs.get(position));
        holder.mDateTime.setText(mDateTimes.get(position));

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d(TAG, "onClick: clicked on: " + mImageNames.get(position));

                //Toast.makeText(mContext, mplateNos.get(position), Toast.LENGTH_SHORT).show();

                //Intent intent = new Intent(mContext, GalleryActivity.class);
                //intent.putExtra("image_url", mImages.get(position));
                //intent.putExtra("image_name", mImageNames.get(position));
                //mContext.startActivity(intent);


                Bundle bundle = new Bundle();
                bundle.putString("IMAGEFILEPATH",mplateNoLinks.get(position));
                bundle.putString("PLATENUMBER",mplateNos.get(position));
                bundle.putString("BACK","History");
                FragmentResults fragmentResults = new FragmentResults();
                fragmentResults.setArguments(bundle);
                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragmentResults).commit();
                //.addToBackStack( "resultslayout" )
                /*
                Intent intent = new Intent (view.getContext(), ActivityResults.class);
                intent.putExtra("IMAGEFILEPATH", mplateNoLinks.get(position));
                intent.putExtra("PLATENUMBER",mplateNos.get(position));
                view.getContext().startActivity(intent);
                */
            }
        });
        holder.deleteHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder1 = new AlertDialog.Builder (v.getContext());
                builder1.setMessage("Delete "+mplateNos.get(position));
                builder1.setCancelable(false);
                builder1.setTitle("Are you sure?");
                builder1.setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, int id) {
                                FirebaseFirestore firebaseFirestore;
                                firebaseFirestore = FirebaseFirestore.getInstance();
                                firebaseFirestore.collection("User Informations")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .collection("History").document(docIDs.get(position))
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mplateNos.remove(position);
                                                mplateNoLinks.remove(position);
                                                mDateTimes.remove(position);
                                                docIDs.remove(position);
                                                notifyItemRemoved(position);
                                                notifyItemRangeChanged(position,mplateNos.size());
                                                Toast.makeText(mContext, "Successfully deleted!", Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(mContext, "Error deleting document "+e, Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
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
        });
    }

    @Override
    public int getItemCount() {
        return mplateNos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView mplateNoLink,deleteHistory;
        TextView mPlateNo,mDateTime,docID;
        RelativeLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            mplateNoLink = itemView.findViewById(R.id.image);
            mPlateNo = itemView.findViewById(R.id.plateno);
            mDateTime = itemView.findViewById(R.id.dateTimeStamp);
            parentLayout = itemView.findViewById(R.id.parent_layout);
            deleteHistory = itemView.findViewById(R.id.deleteHistory);
            docID = itemView.findViewById(R.id.docID);
        }
    }
    
}