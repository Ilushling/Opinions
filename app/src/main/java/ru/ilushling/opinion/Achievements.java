package ru.ilushling.opinion;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class Achievements extends Fragment {

    String TAG = "Achievements";

    SignIn mSignIn;

    BackgroundConnection backgroundConnection;

    public Achievements() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_achievements, container, false);
    }

    public interface BackgroundConnectionAchievementsLoad {
        public void updateUIAsync(Question question);
    }

    @Override
    public void onStart() {
        super.onStart();

        backgroundConnection = new BackgroundConnection(new BackgroundConnectionAchievementsLoad() {
            @Override
            public void updateUIAsync(Question question) {
                updateUI(question);
            }
        }, getActivity(), "loadStatistic", mSignIn);

        backgroundConnection.execute();
    }

    void updateUI(Question question) {
        Log.e(TAG, "daun");
    }

    ;

    // signIn
    void signInEvent(SignIn signIn) {
        mSignIn = signIn;
    }
}
