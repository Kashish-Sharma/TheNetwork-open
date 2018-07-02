package com.thenetwork.app.android.thenetwork.Activities;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.thenetwork.app.android.thenetwork.Fragments.SigninFragment;
import com.thenetwork.app.android.thenetwork.Fragments.SignupFragment;
import com.thenetwork.app.android.thenetwork.R;

public class AuthActivity extends AppCompatActivity {

    private SigninFragment signinFragment;
    private SignupFragment signupFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        signinFragment = new SigninFragment();
        signupFragment = new SignupFragment();

        replaceFragment(signupFragment);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void replaceFragment(Fragment fragment){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.auth_container,fragment);
        fragmentTransaction.commit();

    }
}
