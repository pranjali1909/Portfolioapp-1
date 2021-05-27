package com.example.portfolioapp.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.portfolioapp.MainActivity;
import com.example.portfolioapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class PostDetailFragment extends Fragment {

    String pid = null;
    String uid = null;
    String currentuid = null;
    String uname,uimage;
    String UI="noImage",UN="noName";



    FirebaseAuth fauth;
    FirebaseFirestore fstore;

    ImageView userpic,projectpic;
    TextView username,posttime,projectname,projectdetails,paid_unpaid,projecttype,domain;

    Button Applybtn,likebtn,commentbtn,sharebtn;

    Context mcontext;

    int tot_likes;


    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        mcontext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_post_detail, container, false);
        getActivity().setTitle("Post Details");

        userpic = v.findViewById(R.id.detail_userpic);
        projectpic = v.findViewById(R.id.detail_projectpic);
        username = v.findViewById(R.id.detail_uname);
        posttime = v.findViewById(R.id.detail_time);
        projectname = v.findViewById(R.id.detail_project_name);
        projectdetails = v.findViewById(R.id.detail_description);
        paid_unpaid = v.findViewById(R.id.detail_paid_unpaid);
        projecttype = v.findViewById(R.id.detail_project_type);
        domain = v.findViewById(R.id.detail_domain);
        Applybtn = v.findViewById(R.id.Apply_btn);
        likebtn = v.findViewById(R.id.detail_likebtn);
        commentbtn = v.findViewById(R.id.detail_commentbtn);
        sharebtn = v.findViewById(R.id.detail_sharebtn);
        fauth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        currentuid = fauth.getCurrentUser().getUid();


        Bundle bundle = getArguments();
        if(bundle!=null)
        {
            pid = bundle.getString("pid");
            uid = bundle.getString("puid");
            uname = bundle.getString("uname");
            uimage = bundle.getString("uimage");
            tot_likes = bundle.getInt("tot_likes");
        }

        if(uid.equals(currentuid))
        {
            Applybtn.setText("Who Applied");
        }

        if(uid.equals(currentuid))
        {
            Applybtn.setText("Who Applied");
        }

        if(Applybtn.getText().toString().equals("Apply")) {
            Applybtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    WhoApplied();
                }
            });
        }
        else{
            Applybtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Bundle bundle = new Bundle();
                    bundle.putString("pid",pid);

                    AllBidsFragment f = new AllBidsFragment();
                    f.setArguments(bundle);
                    FragmentManager fragmentManager = ((MainActivity)mcontext).getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment,f);
                    fragmentTransaction.addToBackStack(null).commit();
                }
            });
        }


        loaddata();

        likebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fstore.collection("Posts").whereEqualTo("pid",pid)
                        .whereArrayContains("Likes",currentuid).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.isEmpty())
                        {

                            fstore.collection("Posts").document(pid).update("pLike", FieldValue.increment(1));
                            fstore.collection("Posts").document(pid).update("Likes",FieldValue.arrayUnion(currentuid));
                            likebtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_liked,0,0,0);


                            tot_likes++;

                            if(tot_likes==1)
                            {
                                likebtn.setText(Integer.toString(tot_likes)+" Like");
                            }
                            else
                            {
                                likebtn.setText(Integer.toString(tot_likes)+" Likes");
                            }

                            if(!uid.equals(currentuid))
                                addtonotification(uid,pid,"Liked Your Post");

                        }
                        else
                        {
                            fstore.collection("Posts").document(pid).update("pLike", FieldValue.increment(-1));
                            fstore.collection("Posts").document(pid).update("Likes",FieldValue.arrayRemove(currentuid));
                            likebtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like,0,0,0);


                            tot_likes--;

                            if(tot_likes==0 || tot_likes==1)
                            {
                                likebtn.setText(Integer.toString(tot_likes)+" Like");
                            }
                            else
                            {
                                likebtn.setText(Integer.toString(tot_likes)+" Likes");
                            }
                        }
                    }
                });
            }
        });

        commentbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();
                bundle.putString("pid",pid);
                bundle.putString("useruid",uid);
                CommentFragment fragment  = new CommentFragment();
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = ((MainActivity)mcontext).getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment,fragment);
                fragmentTransaction.addToBackStack(null).commit();
            }
        });

        return v;
    }

    private void WhoApplied() {

        Map<String,Object> doc = new HashMap<>();

        fstore.collection("users").document(currentuid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable @org.jetbrains.annotations.Nullable DocumentSnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {

                assert value != null;
                if(value.exists())
                {
                    UN = value.getString("Full Name");
                    UI = value.getString("Image");
                    String skills=value.getString("skills");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss ");
                    String currentTime = sdf.format(new Date());

                    //String crptime=currentTime.substring(0,20) +" "+ currentTime.substring(3,34);
                    doc.put("id",currentuid);
                    doc.put("Fullname",UN);
                    doc.put("pid",pid);
                    doc.put("time",currentTime);
                    doc.put("skills",skills);
                    if(UI.equals("noImage"))
                        doc.put("dp","https://firebasestorage.googleapis.com/v0/b/portfolio-app-6f30e.appspot.com/o/Users%2Favatar.jpg?alt=media&token=f342a8f2-bae3-4e23-a87e-401d533bcee8");
                    else
                        doc.put("dp",UI);

                    fstore.collection("Posts").document(pid).collection("Apply").document(fauth.getCurrentUser().getUid())
                            .set(doc).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid){
                            Toast.makeText(getActivity(),"Applied!",Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            }
        });


    }

    private void addtonotification(String uid, String pid, String notification) {

        String timestamp = ""+System.currentTimeMillis();

        HashMap<Object,String> hashMap = new HashMap<>();
        hashMap.put("pid",pid);
        hashMap.put("timestamp",timestamp);
        hashMap.put("puid",uid);
        hashMap.put("notification",notification);
        hashMap.put("suid",currentuid);
        hashMap.put("sname",uname);
        hashMap.put("simage",uimage);
        hashMap.put("type","like");

        fstore.collection("Notifications").document(pid+"like"+timestamp)
                .set(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        });

    }

    private void loaddata() {

        fstore.collection("Posts").document(pid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable @org.jetbrains.annotations.Nullable DocumentSnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {

                if(value.exists())
                {
                    if(value.getString("UserImage").equals("noImage"))
                    {
                        try{
                            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/portfolio-app-6f30e.appspot.com/o/Users%2Favatar.jpg?alt=media&token=f342a8f2-bae3-4e23-a87e-401d533bcee8").into(userpic);
                        }catch(Exception e)
                        {
                        }
                    }
                    else
                    {
                        try{
                            Picasso.get().load(value.getString("UserImage")).into(userpic);
                        }catch(Exception e)
                        {
                            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/portfolio-app-6f30e.appspot.com/o/Users%2Favatar.jpg?alt=media&token=f342a8f2-bae3-4e23-a87e-401d533bcee8").into(userpic);
                        }
                    }



                    try{
                        Picasso.get().load(value.getString("PostImage")).into(projectpic);
                    }catch (Exception e)
                    {
                        projectpic.setVisibility(View.GONE);
                    }


                    username.setText(value.getString("FullName"));



                    String timestamp = value.getString("pTime");
                    Calendar cal = Calendar.getInstance(Locale.getDefault());
                    cal.setTimeInMillis(Long.parseLong(timestamp));
                    String ptime = DateFormat.format("dd/MM/yyyy hh:mm:aa", cal).toString();


                    posttime.setText(ptime);

                    projectname.setText(value.getString("ProjectName"));

                    projectdetails.setText(value.getString("Detail"));

                    paid_unpaid.setText(value.getString("Paid_Unpaid"));

                    projecttype.setText(value.getString("Type_of_post"));

                    List<String> domainslist = (List<String>) value.get("Domain");

                    StringBuilder txt_domain= new StringBuilder();

                    for(int i=0;i<domainslist.size();i++)
                    {
                        if(i!=domainslist.size()-1)
                        {
                            txt_domain.append(domainslist.get(i)).append(",");
                        }
                        else
                        {
                            txt_domain.append(domainslist.get(i));
                        }
                    }

                    domain.setText(txt_domain.toString());

                    tot_likes = value.getLong("pLike").intValue();

                    setlikes(tot_likes);
                }
            }
        });

    }

    private void setlikes(int tot_likes) {

        fstore.collection("Posts").whereEqualTo("pid",pid).whereArrayContains("Likes",currentuid)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if(queryDocumentSnapshots.isEmpty())
                {
                    likebtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like,0,0,0);

                    if(tot_likes==0 || tot_likes==1)
                    {
                        likebtn.setText(Integer.toString(tot_likes)+" Like");
                    }
                    else
                    {
                        likebtn.setText(Integer.toString(tot_likes)+" Likes");
                    }
                }
                else
                {
                    likebtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_liked,0,0,0);

                    if(tot_likes==0 || tot_likes==1)
                    {
                        likebtn.setText(Integer.toString(tot_likes)+" Like");
                    }
                    else
                    {
                        likebtn.setText(Integer.toString(tot_likes)+" Likes");
                    }
                }

            }
        });


    }
}