package com.thenetwork.app.android.thenetwork.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thenetwork.app.android.thenetwork.R;

import java.util.HashMap;
import java.util.Map;

public class LoginStatusActivity extends AppCompatActivity {


    private TextView txt_email, txt_status;
    private Menu mMenu;

    private Toolbar mToolbar;

    private FirebaseUser user;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private Snackbar mSnackbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_status);

        user = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();


        txt_email = findViewById(R.id.login_email_check);
        txt_status = findViewById(R.id.login_status_check);

        mToolbar = findViewById(R.id.login_status_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account verification");
        getSupportActionBar().setElevation(10.0f);

        mSnackbar = Snackbar.make(findViewById(R.id.login_status_layout),"Please refresh after verifying email link.",Snackbar.LENGTH_INDEFINITE);
        mSnackbar.show();

        if (firebaseAuth.getCurrentUser()!=null){
            if (!user.isEmailVerified()){
                FirebaseAuth.getInstance().getCurrentUser()
                        .sendEmailVerification()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){

                                    Toast.makeText(LoginStatusActivity.this,"Verification sent to "+ FirebaseAuth.getInstance().getCurrentUser().getEmail(),Toast.LENGTH_SHORT).show();

                                } else {

                                    if (mSnackbar!=null){
                                        mSnackbar.dismiss();
                                    }

                                    mSnackbar = Snackbar.make(findViewById(R.id.login_status_layout), "Your account is not verified, please verify to post.", Snackbar.LENGTH_LONG)
                                            .setAction("TRY AGAIN", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    FirebaseAuth.getInstance().getCurrentUser()
                                                            .sendEmailVerification();
                                                }
                                            });
                                    mSnackbar.show();

                                }

                            }
                        });
            }
        }

        setInfo();


    }

    private void setInfo(){

        txt_email.setText(": "+ user.getEmail());
        txt_status.setText(new StringBuilder(": ").append(String.valueOf(user.isEmailVerified())));

        if(user.isEmailVerified()){
            sendToMain();
        }

    }

    private void sendToMain() {
        Intent mainIntent = new Intent(LoginStatusActivity.this,MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.refresh_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case R.id.login_status_refresh:
                mMenu.findItem(R.id.login_status_refresh).setEnabled(false);
                FirebaseAuth.getInstance().getCurrentUser()
                        .reload()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                setInfo();
                            }
                        });
                mMenu.findItem(R.id.login_status_refresh).setEnabled(true);
                return true;

            case R.id.login_status_logout:
                firebaseAuth.signOut();
                sendToLogin();
                return true;

            default:
                return false;

        }
    }

    private void sendToLogin(){
        Intent intent = new Intent(LoginStatusActivity.this,AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
