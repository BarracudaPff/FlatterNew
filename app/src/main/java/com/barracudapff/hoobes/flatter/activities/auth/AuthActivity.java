package com.barracudapff.hoobes.flatter.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.barracudapff.hoobes.flatter.R;
import com.barracudapff.hoobes.flatter.adapters.AuthPagerAdapter;
import com.barracudapff.hoobes.flatter.fragments.auth.AuthSignBaseFragment;
import com.barracudapff.hoobes.flatter.fragments.auth.AuthSignInFragment;
import com.barracudapff.hoobes.flatter.fragments.auth.AuthSignUpFragment;
import com.barracudapff.hoobes.flatter.support.NonSwipeableViewPager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.concurrent.Executor;

public class AuthActivity extends AppCompatActivity
        implements AuthSignInFragment.OnAuthSignInFragmentInteractionListener
        , AuthSignUpFragment.OnAuthSignUpFragmentInteractionListener {

    AuthPagerAdapter adapter;
    NonSwipeableViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        adapter = new AuthPagerAdapter(getSupportFragmentManager());

        pager = findViewById(R.id.auth_main_content);
        pager.setAdapter(adapter);
        pager.setCurrentItem(0, false);

        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                ((AuthSignBaseFragment) adapter.getItem(position)).start();
            }
        });
    }

    @Override
    public void OnAuthSignInInteraction() {
        pager.setCurrentItem(1, false);
    }

    @Override
    public void OnAuthSignUpFragmentInteraction() {
        pager.setCurrentItem(0, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 102) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
        }
        if (requestCode == 103) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                System.out.println("Google sign in failed" + e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener((Executor) this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    } else {
                        System.out.println("Authentication Failed.");
                    }
                });
    }
}
