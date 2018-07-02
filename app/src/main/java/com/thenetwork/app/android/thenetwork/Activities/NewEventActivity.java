package com.thenetwork.app.android.thenetwork.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.thenetwork.app.android.thenetwork.HelperClasses.EventPerDay;
import com.thenetwork.app.android.thenetwork.R;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

import static com.thenetwork.app.android.thenetwork.HelperUtils.Constants.monthShort;


public class NewEventActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
        , DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {


    private static final int READCODE = 1;
    private static final int WRITECODE = 2;
    private static final int LOCATION_GRANT_CODE = 667;
    public static final int PLACE_PICKER_REQUEST = 657;
    private static final String TAG = "NewEventActivity";

    private GoogleApiClient mClient;

    //Bottom sheet first
    private BottomSheetDialog mDialogNext;
    private BottomSheetDialog mDialog;
    private Switch isSingleDayEvent;
    public Menu mMenu;
    public Button confirm_details;
    public TextView eventDateTo, eventDateFrom;
    public EditText eventTitle,eventDesc, organizerContact;
    private ImageView eventImage;
    public Uri mEventUri = null;
    public FloatingActionButton placePicker;
    private ProgressBar confirmProgress;
    private String mTitle;
    private String mDescription;
    private String mDateTo;
    private String mDateFrom;
    private String mPhoneNumber;
    private Boolean isSingle = false ;

    //Bottom sheet second
    private TextView daySpinner;
    private ImageView addDay, removeDay;
    private EditText eventDayName;
    private TextView eventDayTimeTo, eventDayTimeFrom;
    private Button eventAddBtn, eventConfirmBtn;
    private ProgressBar eventConfirmProgress;
    private LinearLayout daySelectorLinearLayout;
    private Date mDateStart, mDateEnd;
    private Map<String, String> eventPerDaysMap = new HashMap<>();
    private int mCount = 0;
    private String current_user_id;
    private Bitmap compressedImageFile;
    private Bitmap compressedThumbFile;
    private String mPlaceId;



    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.MainTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        eventTitle = findViewById(R.id.event_title);
        eventDesc = findViewById(R.id.event_desc);
        eventImage = findViewById(R.id.event_card_image);
        placePicker = findViewById(R.id.place_picker);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        placePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    try {
                        onAddPlacePickerButtonClicked();
                    } catch (GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException e) {
                        Log.i(TAG,e.getMessage());
                        e.printStackTrace();
                    }
                }

        });

        eventImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                    if (ContextCompat.checkSelfPermission(NewEventActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)!=
                            PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(NewEventActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                            PackageManager.PERMISSION_GRANTED){
                        Snackbar.make(findViewById(R.id.newEventRootLayout),"Please grant permissions",Snackbar.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(NewEventActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},READCODE);
                        ActivityCompat.requestPermissions(NewEventActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITECODE);

                    } else {

                        bringImagePicker();
                    }

                } else {
                    bringImagePicker();
                }

            }
        });



         mClient = new GoogleApiClient.Builder(this)
                 .enableAutoManage(this,this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this,this)
                .build();

    }

    @Override
    protected void onStop() {
        super.onStop();
        // stop GoogleApiClient
        if (mClient.isConnected()) {
            mClient.disconnect();
        }
    }

    private void onAddPlacePickerButtonClicked() throws GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {
        if (ContextCompat.checkSelfPermission(NewEventActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(NewEventActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar.make(findViewById(R.id.newEventRootLayout)
                        , "You need to enable permissions first", Snackbar.LENGTH_SHORT).show();
                return;
            }

        }

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        Intent intent = builder.build(this);
        startActivityForResult(intent,PLACE_PICKER_REQUEST);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.new_post_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_post_blog :
                mTitle = eventTitle.getText().toString();
                mDescription = eventDesc.getText().toString();

                View view = this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                if (!TextUtils.isEmpty(mTitle) && !TextUtils.isEmpty(mDescription) && mEventUri!=null){

                    showBottomSheetDialog();

                }else {
                    Snackbar.make(findViewById(R.id.newEventRootLayout),"Title, image and description are required.", Snackbar.LENGTH_LONG).show();
                }


                return true;
            default:
                return  false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK){
            Place place = PlacePicker.getPlace(this,data);
            if (place == null){
                Log.i(TAG,"No place selected");
                return;
            }

            String placeAddress = place.getAddress().toString();
            String placeId = place.getId();

            mPlaceId = placeId;

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mEventUri = result.getUri();

                eventImage.setImageURI(mEventUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

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

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

        String month = monthShort[monthOfYear];
        String date = dayOfMonth+" "+month+", "+year;
        String eDate = year+"-"+(monthOfYear+1)+"-"+dayOfMonth;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        switch (view.getTag()){
            case "eventDateFrom":

                try {
                    mDateStart = dateFormat.parse(eDate);
                    mDateEnd = mDateStart;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                eventDateFrom.setText(date);
                break;

            case "eventDateTo":

                try {
                    mDateEnd = dateFormat.parse(eDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                eventDateTo.setText(date);
                break;
        }

    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        String time = hourOfDay+":"+minute;

        switch (view.getTag()){
            case "eventTimeFrom":
                eventDayTimeFrom.setText(time);
                break;
            case "eventTimeTo":
                eventDayTimeTo.setText(time);
                break;
        }

    }

    private void bringImagePicker(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAllowFlipping(true)
                .setAllowCounterRotation(true)
                .setMultiTouchEnabled(true)
                .setMinCropResultSize(512,512)
                .start(NewEventActivity.this);
    }

    private void bringDatePicker(String tag){
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        dpd.setVersion(DatePickerDialog.Version.VERSION_2);
        dpd.show(getFragmentManager(), tag);
    }

    private void bringTimePicker(String tag){
        Calendar now = Calendar.getInstance();
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                now.get(Calendar.SECOND),
                true);
        tpd.setVersion(TimePickerDialog.Version.VERSION_2);
        tpd.show(getFragmentManager(), tag);
    }

    private void disableOnConfirm(BottomSheetDialog dialog){
        daySelectorLinearLayout.setEnabled(false);
        eventDayName.setEnabled(false);
        eventDayTimeTo.setEnabled(false);
        eventDayTimeFrom.setEnabled(false);
        eventConfirmProgress.setVisibility(View.VISIBLE);
        eventAddBtn.setEnabled(false);
        eventAddBtn.setBackgroundColor(ContextCompat.getColor(this,R.color.grey));
        eventConfirmBtn.setVisibility(View.GONE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }

    private void enableOnConfirm(BottomSheetDialog dialog){
        daySelectorLinearLayout.setEnabled(true);
        eventDayName.setEnabled(true);
        eventDayTimeTo.setEnabled(true);
        eventDayTimeFrom.setEnabled(true);
        eventConfirmProgress.setVisibility(View.GONE);
        eventAddBtn.setEnabled(true);
        eventAddBtn.setBackgroundColor(ContextCompat.getColor(this,R.color.colorAccent));
        eventConfirmBtn.setVisibility(View.VISIBLE);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
    }

    public void showBottomSheetDialog() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_event, null);

        mDialog = new BottomSheetDialog(this);
        mDialog.setContentView(view);

        isSingleDayEvent = mDialog.findViewById(R.id.single_day_event);

        eventDateTo = mDialog.findViewById(R.id.event_date_to);
        eventDateFrom = mDialog.findViewById(R.id.event_date_from);

        organizerContact = mDialog.findViewById(R.id.user_phone);
        confirm_details = mDialog.findViewById(R.id.confirm_details);

        confirmProgress = mDialog.findViewById(R.id.confirm_details_progress);

        isSingleDayEvent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isSingle = b;

                if (isSingle){
                    eventDateTo.setVisibility(View.GONE);
                } else {
                    eventDateTo.setVisibility(View.VISIBLE);
                }

            }
        });


        eventDateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bringDatePicker("eventDateTo");
            }
        });
        eventDateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bringDatePicker("eventDateFrom");
            }
        });


//        eventTimeFrom.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (isSingle){
//                    bringTimePicker("eventTimeFrom");
//                }
//            }
//        });
//        eventTimeTo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (isSingle){
//                    bringTimePicker("eventTimeTo");
//                }
//            }
//        });

        confirm_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDateFrom = eventDateFrom.getText().toString();
                if (isSingle){
                    mDateTo = "nothing";
                } else {
                    mDateTo = eventDateTo.getText().toString();
                }

                mPhoneNumber = organizerContact.getText().toString();

                if (!TextUtils.isEmpty(mDateFrom) && !TextUtils.isEmpty(mDateTo) && !TextUtils.isEmpty(mPhoneNumber)){

                        mDialog.dismiss();

                        if (isSingle){
                            showBottomNextDialog();
                        } else {
                            showBottomNextDialog();
                        }

                } else {
                    Toast.makeText(NewEventActivity.this,"All fields are required.",Toast.LENGTH_SHORT).show();
                }

            }
        });

        mDialog.show();
    }

    public void showBottomNextDialog(){

        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_event_plan, null);
        mDialogNext = new BottomSheetDialog(this);
        mDialogNext.setContentView(view);

        daySelectorLinearLayout = mDialogNext.findViewById(R.id.day_selecter_linear_layout);
        daySpinner = mDialogNext.findViewById(R.id.event_date_spinner);
        eventDayName = mDialogNext.findViewById(R.id.event_day_name);
        eventDayTimeTo = mDialogNext.findViewById(R.id.event_day_time_to);
        eventDayTimeFrom = mDialogNext.findViewById(R.id.event_day_time_from);
        eventAddBtn = mDialogNext.findViewById(R.id.event_day_add_btn);
        eventConfirmBtn = mDialogNext.findViewById(R.id.confirm_event);
        eventConfirmProgress = mDialogNext.findViewById(R.id.confirm_event_progress);
        addDay = mDialogNext.findViewById(R.id.addDay);
        removeDay = mDialogNext.findViewById(R.id.removeDay);

        eventDayTimeFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bringTimePicker("eventTimeFrom");
            }
        });

        eventDayTimeTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bringTimePicker("eventTimeTo");
            }
        });

        addDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int diffInDays = (int) ((mDateEnd.getTime() - mDateStart.getTime()) / (1000 * 60 * 60 * 24));
                Log.i(TAG,diffInDays+" is the difference in days");
                String day = daySpinner.getText().toString();
                int dayCount = Integer.parseInt(day);
                if (dayCount >= diffInDays+1){
                    Toast.makeText(NewEventActivity.this,"It's last day of your event.",Toast.LENGTH_SHORT).show();
                } else {
                    dayCount++;
                    daySpinner.setText(dayCount+"");
                }
            }
        });

        removeDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int diffInDays = (int) ((mDateEnd.getTime() - mDateStart.getTime()) / (1000 * 60 * 60 * 24));
                Log.i(TAG,diffInDays+" is the difference in days");
                String day = daySpinner.getText().toString();
                int dayCount = Integer.parseInt(day);
                if (dayCount == 1){
                    Toast.makeText(NewEventActivity.this,"It's first day of your event.",Toast.LENGTH_SHORT).show();
                } else {
                    dayCount--;
                    daySpinner.setText(dayCount+"");
                }
            }
        });


        eventAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int whichDay = Integer.parseInt(daySpinner.getText().toString());
                String timeFrom = eventDayTimeFrom.getText().toString();
                String timeTo = eventDayTimeTo.getText().toString();
                String name = eventDayName.getText().toString();

                if (!TextUtils.isEmpty(timeFrom) && !TextUtils.isEmpty(timeTo) && !TextUtils.isEmpty(name)){
                    eventPerDaysMap.put(""+ mCount++,whichDay+"--0--"+timeFrom+"--0--"+timeTo+"--0--"+name);
                    eventDayName.setText("");
                    eventDayTimeTo.setText("");
                    eventDayTimeFrom.setText("");
                    Toast.makeText(NewEventActivity.this,"Added",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(NewEventActivity.this,"All fields are required.",Toast.LENGTH_SHORT).show();
                }


            }
        });

        eventConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventPerDaysMap.put("count",mCount+"");
                mCount = 0;
                disableOnConfirm(mDialogNext);
                uploadEventToFirestore();

            }
        });

        mDialogNext.show();
    }

    private void uploadEventToFirestore(){

        File newImageFile = new File(mEventUri.getPath());
        try {
            compressedImageFile = new Compressor(NewEventActivity.this)
                    .setQuality(75)
                    .compressToBitmap(newImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compressedImageFile.compress(Bitmap.CompressFormat.JPEG,75,baos);
        byte[] imageData = baos.toByteArray();

        //Conversion complete
        final String randomName = UUID.randomUUID().toString();
        final StorageReference filePath = storageReference.child("event_images").child(randomName + ".jpg");
        filePath.putBytes(imageData).addOnCompleteListener(NewEventActivity.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                final String downloadUrl = filePath.getDownloadUrl().toString();
                if (task.isSuccessful()){

                    File newThumbFile = new File(mEventUri.getPath());
//
                    try {
                        compressedThumbFile = new Compressor(NewEventActivity.this)
                                .setMaxWidth(200)
                                .setMaxHeight(200)
                                .setQuality(2)
                                .compressToBitmap(newThumbFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedThumbFile.compress(Bitmap.CompressFormat.JPEG,2,baos);
                    byte[] thumbData = baos.toByteArray();

                    final UploadTask uploadTask = storageReference.child("event_images/thumbs")
                            .child(randomName + ".jpg").putBytes(thumbData);

                    uploadTask.addOnSuccessListener(NewEventActivity.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            filePath.getDownloadUrl().addOnSuccessListener(NewEventActivity.this, new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri uriImage) {

                                    storageReference.child("event_images/thumbs")
                                            .child(randomName + ".jpg").getDownloadUrl().addOnSuccessListener(NewEventActivity.this, new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {

                                            String downloadUrl = uriImage.toString();
                                            String downloadThumbUri = uri.toString();

                                            Map<String,Object> eventMap = new HashMap<>();
                                            eventMap.put("image_url",downloadUrl);
                                            eventMap.put("thumb_url",downloadThumbUri);
                                            eventMap.put("title",mTitle);
                                            eventMap.put("desc",mDescription);
                                            eventMap.put("user_id",current_user_id);
                                            eventMap.put("timestamp",System.currentTimeMillis());
                                            eventMap.put("image_name",randomName);
                                            eventMap.put("eventDateTo",mDateTo);
                                            eventMap.put("eventDateFrom",mDateFrom);
                                            eventMap.put("eventContact",mPhoneNumber);
                                            eventMap.put("isSingle",isSingle);
                                            eventMap.put("subEvents",eventPerDaysMap);
                                            eventMap.put("eventPlaceId",mPlaceId);

                                            firebaseFirestore.collection("Events").add(eventMap)
                                                    .addOnCompleteListener(NewEventActivity.this, new OnCompleteListener<DocumentReference>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentReference> task) {

                                                    if (task.isSuccessful()){
                                                        enableOnConfirm(mDialogNext);
                                                        finish();
                                                    } else {
                                                        Toast.makeText(NewEventActivity.this
                                                                ,"Some unexpected error occurred, please try again after sometime."
                                                                ,Toast.LENGTH_SHORT).show();
                                                        enableOnConfirm(mDialogNext);
                                                    }

                                                }
                                            });

                                        }
                                    });

                                }
                            });

                        }
                    }).addOnFailureListener(NewEventActivity.this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            enableOnConfirm(mDialogNext);
                            Toast.makeText(NewEventActivity.this
                                    ,"Some unexpected error occurred, please try again after sometime."
                                    ,Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    enableOnConfirm(mDialogNext);
                    Toast.makeText(NewEventActivity.this
                            ,"Some unexpected error occurred, please try again after sometime."
                            ,Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


}

