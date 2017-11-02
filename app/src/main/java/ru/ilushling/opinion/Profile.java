package ru.ilushling.opinion;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Field;


public class Profile extends Fragment implements View.OnClickListener {

    String TAG = "FragmentProfile";
    // UI
    public Button mSignInButton;
    public TextView mStatus;

    // Signing
    SignIn mSignIn;

    public Profile() {
        // Required empty public constructor
    }

    // [START Send signIn to MainActivity]
    public interface onProfileEventListener {
        void signInEvent();

        void signOutEvent();
    }

    onProfileEventListener profileEventListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            profileEventListener = (onProfileEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onSomeEventListener");
        }
    }
    // [END  Send signIn to MainActivity]

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Sign In
        // Get references to all of the UI views
        mSignInButton = getActivity().findViewById(R.id.sign_in_button);
        mStatus = getActivity().findViewById(R.id.statuslabel);

        // Add click listeners for the buttons
        mSignInButton.setOnClickListener(this);
        mSignInButton.setEnabled(true);

        if (mSignIn != null) {
            updateUI(MainActivity.SIGNED_IN);
        } else {
            updateUI(MainActivity.SIGNED_OUT);
        }

        //mGoogleSignInActivity = new GoogleSignInActivity(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                if (mSignIn == null) {
                    Log.e(TAG, getResources().getString(R.string.sign_in));
                    signIn();
                } else {
                    Log.e(TAG, getResources().getString(R.string.sign_out));
                    signOut();
                }
                break;
            //case R.id.revoke_access_button:
                //revokeAccess();
            //break;
        }
    }

    void signIn() {
        profileEventListener.signInEvent();
    }

    void signOut() {
        profileEventListener.signOutEvent();
    }


    public void updateUI(int signedIn) {
        if (mSignInButton != null) {
            if (signedIn == 0 && mSignIn != null) {
                // Update the UI to reflect that the user is signed out.
                // Buttons
                mSignInButton.setText(R.string.sign_out);
                // Text
                mStatus.setText(mSignIn.name);
            } else {
                // Signed Out
                // Buttons
                mSignInButton.setText(R.string.sign_in);
                // Text
                mStatus.setText("");
            }
        } else {
            Log.e(TAG, "UI Denined");
        }
    }
}