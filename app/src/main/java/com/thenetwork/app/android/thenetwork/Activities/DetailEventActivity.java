package com.thenetwork.app.android.thenetwork.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.flaviofaria.kenburnsview.KenBurnsView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thenetwork.app.android.thenetwork.Adapters.EventDayAdapter;
import com.thenetwork.app.android.thenetwork.HelperClasses.Event;
import com.thenetwork.app.android.thenetwork.HelperClasses.EventDayPlan;
import com.thenetwork.app.android.thenetwork.HelperClasses.EventPerDay;
import com.thenetwork.app.android.thenetwork.HelperUtils.GlideLoadImage;
import com.thenetwork.app.android.thenetwork.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DetailEventActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    GoogleApiClient mClient;
    private LatLng mPlaceLatLng;


    private static final String TAG = "DetailEventActivity";
    private static final int CALLCODE = 12345;

    ImageView eventCallBtn, eventDirectionsBtn;
    private KenBurnsView eventImage;
    //private ShimmerLayout shimmerFrameLayout;
    private TextView descView;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    Toolbar mToolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    AppBarLayout appBarLayout;
    BottomSheetDialog mDialog;
    private String mTitle = "";

    RecyclerView bottomRecyclerView;

    EventDayAdapter eventDayAdapter;
    private List<EventDayPlan> mEventDayPlansList;
    private List<EventPerDay> mEventPerDayList;

    private Date mDateStart, mDateEnd;
    private int max = 0;

    String userId, placeId, eventId, startDate, endDate, imageUrl, time, contact;

    ImageView showRoadMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.DetailTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_event);
        mToolbar = findViewById(R.id.event_detail_toolbar);
        setSupportActionBar(mToolbar);
        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");

        userId = getIntent().getStringExtra("userId");
        placeId = getIntent().getStringExtra("placeId");
        eventId = getIntent().getStringExtra("eventId");
        startDate = getIntent().getStringExtra("startDate");
        endDate = getIntent().getStringExtra("endDate");
        time = getIntent().getStringExtra("time");
        contact = getIntent().getStringExtra("contact");
        imageUrl = getIntent().getStringExtra("imageUrl");



        collapsingToolbarLayout = findViewById(R.id.collapsingToolbarEventDetail);
        collapsingToolbarLayout.setElevation(10.0f);
        collapsingToolbarLayout.setExpandedTitleColor(ContextCompat.getColor(DetailEventActivity.this,R.color.white));
        appBarLayout = findViewById(R.id.appbarEventDetail);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                    // Collapsed
                    actionBar.setDisplayHomeAsUpEnabled(true);
                    actionBar.setTitle(mTitle);
                } else if (verticalOffset == 0) {
                    // Expanded
                    actionBar.setDisplayHomeAsUpEnabled(false);
                    actionBar.setTitle("");
                    collapsingToolbarLayout.setTitle("");
                } else {
                    actionBar.setDisplayHomeAsUpEnabled(false);
                    actionBar.setTitle("");
                    collapsingToolbarLayout.setTitle("");
                    // Somewhere in between
                }
            }
        });

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        showRoadMap = findViewById(R.id.showRoadMap);
        eventCallBtn = findViewById(R.id.event_contact);
        eventDirectionsBtn = findViewById(R.id.event_directions);
        eventImage = findViewById(R.id.event_detail_image);
        descView = findViewById(R.id.event_detail_desc);

        mEventPerDayList = new ArrayList<>();
        mEventDayPlansList = new ArrayList<>();

        mClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this,this)
                .build();


        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mClient, placeId);
        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                Place place = places.get(0);
                mPlaceLatLng = place.getLatLng();
            }
        });


        eventCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (ContextCompat.checkSelfPermission(DetailEventActivity.this, Manifest.permission.CALL_PHONE)!=
                            PackageManager.PERMISSION_GRANTED){
                        Snackbar.make(findViewById(R.id.detailEventRootLayout),
                                "Please grant permissions",Snackbar.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(DetailEventActivity.this,
                                new String[]{Manifest.permission.CALL_PHONE},CALLCODE);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:"+contact));
                        startActivity(intent);
                    }

                }
            }
        });

        eventDirectionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?daddr=%f,%f (%s)"
//                        , mPlaceLatLng.latitude, mPlaceLatLng.longitude, "Here's the event");
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
//                intent.setPackage("com.google.android.apps.maps");
//                startActivity(intent);
                Uri.Builder directionsBuilder = new Uri.Builder()
                        .scheme("https")
                        .authority("www.google.com")
                        .appendPath("maps")
                        .appendPath("dir")
                        .appendPath("")
                        .appendQueryParameter("api", "1")
                        .appendQueryParameter("destination", mPlaceLatLng.latitude + "," + mPlaceLatLng.longitude);

                startActivity(new Intent(Intent.ACTION_VIEW, directionsBuilder.build()));
            }
        });

        firebaseFirestore.collection("Events").document(eventId).get()
                .addOnCompleteListener(DetailEventActivity.this, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();

                    final Event event = documentSnapshot.toObject(Event.class);

                    assert event != null;
                    GlideLoadImage.loadImage(DetailEventActivity.this,eventImage
                            ,event.getThumb_url(),event.getImage_url());
                    mTitle = event.getTitle();
                    descView.setText(event.getDesc());
                }
            }
        });

        eventImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailEventActivity.this, FullScreenImageActivity.class);
                intent.putExtra("image_url",imageUrl);
                intent.putExtra("user_id",userId);
                intent.putExtra("time",time);
                startActivity(intent);

            }
        });
        //Getting data for sheet recyclerview
        firebaseFirestore.collection("Events").document(eventId).get()
                .addOnCompleteListener(DetailEventActivity.this, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()){

                    DocumentSnapshot documentSnapshot = task.getResult();
                    Event event = documentSnapshot.toObject(Event.class);
                    int count = 0;

                    assert event != null;
                    Map<String, String> roadMapData = event.getSubEvents();

                    if (roadMapData.size() > 1){
                        count = Integer.parseInt(roadMapData.get("count"));

                        for (int i = 0; i < count ; i++){
                            String singleDayMaxData = roadMapData.get(""+max);
                            String[] partsMax = singleDayMaxData.split("--0--");
                            int dayMax = Integer.parseInt(partsMax[0]);

                            String singleDayData = roadMapData.get(""+i);
                            String[] parts = singleDayData.split("--0--");
                            int day = Integer.parseInt(parts[0]);
                            String timeFrom = parts[1];
                            String timeTo = parts[2];
                            String name = parts[3];
                            if (dayMax < day){
                                max = i;
                            }
                            EventPerDay eventPerDay = new EventPerDay(day,timeFrom,timeTo,name);
                            mEventPerDayList.add(eventPerDay);
                        }

                        String singleDayMaxData = roadMapData.get(""+max);
                        String[] partsMax = singleDayMaxData.split("--0--");
                        int dayMax = Integer.parseInt(partsMax[0]);

                        for (int i = 1; i <= dayMax; i++){
                            List<EventPerDay> tempList = new ArrayList<>();

                            for (int j = 1; j <= mEventPerDayList.size(); j++){
                                if (mEventPerDayList.get(j-1).getDay() == i){
                                    tempList.add(mEventPerDayList.get(j-1));
                                }
                            }
                            mEventDayPlansList.add(new EventDayPlan("Day "+i,tempList));
                        }
                    }
                }
            }
        });

        showRoadMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetDialog(mEventDayPlansList);
            }
        });



    }

    @Override
    protected void onStop() {
        super.onStop();
        // stop GoogleApiClient
        if (mClient.isConnected()) {
            mClient.disconnect();
        }
    }

    public void showBottomSheetDialog(List<EventDayPlan> eventDayPlanList) {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_detail_event, null);

        mDialog = new BottomSheetDialog(this);
        mDialog.setContentView(view);

        bottomRecyclerView = mDialog.findViewById(R.id.road_map_recycler_view);
        eventDayAdapter = new EventDayAdapter(eventDayPlanList, this);
        bottomRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bottomRecyclerView.setAdapter(eventDayAdapter);

        mDialog.show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG,"API client connection successful");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG,"API client connection suspended !!! ");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG,"API client connection failed ! ");
    }
}
