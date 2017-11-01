package ru.ilushling.opinion;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.analytics.FirebaseAnalytics;


public class MainActivity extends FragmentActivity implements Profile.onProfileEventListener, GoogleApiClient.OnConnectionFailedListener {

    String TAG = "MainActivity";
    private static MainActivity mInstance;
    private FirebaseAnalytics mFirebaseAnalytics;
    // Signing
    public static final int SIGNED_IN = 0, RC_SIGN_IN = 0, STATE_SIGNING_IN = 1, STATE_IN_PROGRESS = 2, SIGNED_OUT = 3;
    public GoogleApiClient mGoogleApiClient;
    public int mSignInProgress;
    private PendingIntent mSignInIntent;
    private ProgressDialog mProgressDialog;
    // Menu
    TabLayout tabLayout;
    // Fragments control
    public FragmentTransaction ft;
    // Fragments
    Questions Questions;
    Achievements Achievements;
    Profile Profile;
    // Keep variables
    public SignIn mSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInstance = this;

        // Fragments
        Questions = new Questions();
        Achievements = new Achievements();
        Profile = new Profile();
        //mGoogleSignInActivity = new GoogleSignInActivity(this);

        // Obtain the FirebaseAnalytics instance.
        //mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mSignIn = new SignIn();

        mGoogleApiClient = buildGoogleApiClient();

        // UI
        // Menu
        tabLayout = findViewById(R.id.tabLayout);
        // Start from [fragment]
        toProfile();
        //mGoogleSignInActivity = new GoogleSignInActivity(getApplicationContext());

        // Menu Listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                changeFragment(position);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    // [START change fragment]
    void changeFragment(int position) {
        switch (position) {
            case 0:
                toQuestions();
                break;
            case 1:
                toAchievements();
                break;
            case 2:
                toProfile();
                break;
        }
    }

    void toQuestions() {
        // Change menu
        tabLayout.getTabAt(0).select();
        // Change fragment
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, Questions);
        ft.commit();
    }

    void toAchievements() {
        // Change menu
        tabLayout.getTabAt(1).select();
        // Change fragment
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, Achievements);
        ft.commit();
    }

    void toProfile() {
        // Change menu
        tabLayout.getTabAt(2).select();
        // Change fragment
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, Profile);
        ft.commit();
    }
    // [END change fragment]

    @Override
    public void signInEvent() {
        //mGoogleSignInActivity.signIn();
        signIn();
        this.Questions.signInEvent(mSignIn);
    }

    @Override
    public void signOutEvent() {
        signOut();
        //mGoogleSignInActivity.signOut();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check for Sign-in previously
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // [Cached SignIn]
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.e(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            Log.e(TAG, getResources().getString(R.string.signing_in));
            // [First SignIn or SignIn has expired]
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }


    // Dialog of loading expired cache signIn
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getResources().getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    public GoogleApiClient buildGoogleApiClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to GoogleSignIn.API and the options above.
        return new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, "Connectiong Failed: " + result);
        if (mSignInProgress != STATE_IN_PROGRESS) {
            mSignInIntent = result.getResolution();
            if (mSignInProgress == STATE_SIGNING_IN) {
                resolveSignInError(result);
            }
        }
        // Will implement shortly
        signOut();
    }

    private void resolveSignInError(ConnectionResult mConnectionResult) {
        if (mSignInIntent != null) {
            try {
                mSignInProgress = STATE_IN_PROGRESS;
                mConnectionResult.startResolutionForResult(this, mSignInProgress);
            } catch (IntentSender.SendIntentException e) {
                mSignInProgress = STATE_SIGNING_IN;
                mGoogleApiClient.connect();
            }
        } else {
            // You have a play services error -- inform the user
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RC_SIGN_IN:
                // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
                if (requestCode == RC_SIGN_IN) {
                    GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                    handleSignInResult(result);
                }
                break;
        }
    }

    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult result) {
        //Log.e(TAG, "handleSignInResult: " + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount account = result.getSignInAccount();
            mGoogleApiClient.connect();
            mSignInProgress = SIGNED_IN;

            mSignIn = new SignIn();
            mSignIn.clientID = account.getId();
            mSignIn.token = account.getIdToken();
            mSignIn.name = account.getDisplayName();
            mSignIn.email = account.getEmail();
            mSignIn.photoUrl = account.getPhotoUrl().toString();
            // UI
            updateUI(true);

            //Log.e(TAG, "ID Token:" + mSignIn.token);
        } else {
            // Signed out, show unauthenticated UI.
            mSignInProgress = SIGNED_OUT;
            updateUI(false);
            /*
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Отладка")
                    .setMessage(result.getStatus() + " : " + result.getSignInAccount())
                    .setCancelable(false)
                    .setNegativeButton("Закрыть",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
            */

            Log.e(TAG, "result: " + result.getStatus());

            signOut();
        }
    }
    // [END handleSignInResult]

    // [START signIn]
    public void signIn() {
        if (mGoogleApiClient != null) {
            Profile.mSignInButton.setText(R.string.signing_in);
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
    }
    // [END signIn]

    // [START signOut]
    public void signOut() {
        if (mGoogleApiClient.isConnected()) {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient);
            mGoogleApiClient.clearDefaultAccountAndReconnect();
            mGoogleApiClient.stopAutoManage(this);
            mGoogleApiClient.disconnect();
            mSignIn = null;
            mSignInProgress = SIGNED_OUT;
            // Update Profile UI to reflect user signed out.
            updateUI(false);
        } else {
            mSignIn = null;
            updateUI(false);
            Log.e(TAG, getResources().getString(R.string.cant_sign_out));
        }
    }
    // [END signOut]

    // [START revokeAccess]
    public void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient);
        // Update the UI to reflect that the user is signed out.
        updateUI(false);
        Profile.mStatus.setText("Revoke");
    }
    // [END revokeAccess]

    public void updateUI(boolean signedIn) {
        Questions.mSignIn = mSignIn;
        Achievements.mSignIn = mSignIn;
        Profile.mSignIn = mSignIn;

        Profile.updateUI(mSignInProgress);

        if (signedIn) {
            // Send signIn to server
            BackgroundConnection backgroundConnection = new BackgroundConnection(this, "signIn", mSignIn);
            backgroundConnection.execute();
        }
    }
}
