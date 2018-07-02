package com.thenetwork.app.android.thenetwork.Activities;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.thenetwork.app.android.thenetwork.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText regEmailText;
    private EditText regPassword;
    private EditText regConfirmPassword;

    private Button signupBtn;

    private ProgressBar regProgressBar;

    //firebase
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.LoginTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        regEmailText = (EditText) findViewById(R.id.signup_email);
        regPassword = (EditText) findViewById(R.id.signup_password);
        regConfirmPassword = (EditText) findViewById(R.id.signup_confirm_password);
        signupBtn = (Button)findViewById(R.id.signup_btn);
        regProgressBar = (ProgressBar)findViewById(R.id.reg_progress);


        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = regEmailText.getText().toString();
                String password = regPassword.getText().toString();
                String confirmPassword = regConfirmPassword.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirmPassword)){

                    if (password.equals(confirmPassword)){
                        regProgressBar.setVisibility(View.VISIBLE);
                        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    sendToStatus();
                                } else {

                                    String exception = task.getException().getMessage();
                                    Snackbar.make(findViewById(R.id.main_reg_layout), "Error : "+exception, Snackbar.LENGTH_SHORT).show();
                                }
                                regProgressBar.setVisibility(View.INVISIBLE);
                            }
                        });

                    } else {
                        regProgressBar.setVisibility(View.INVISIBLE);
                        Snackbar.make(findViewById(R.id.main_reg_layout), "Please check your password", Snackbar.LENGTH_SHORT).show();
                    }

                }

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null){
            sendToMain();
        }
        
    }

    private void sendToMain() {

        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();

    }

    private void sendToStatus(){

        Intent statusCheckIntent = new Intent(RegisterActivity.this,LoginStatusActivity.class);
        statusCheckIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(statusCheckIntent);
    }
}
