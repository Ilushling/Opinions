package ru.ilushling.opinion;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;


public class Questions extends Fragment implements View.OnClickListener {

    String TAG = "Questions";
    // ADS
    private AdView mAdView;
    // UI Questions
    public TextView textViewQuestion;
    public ImageView Thumbnail;
    public Button opinionButton_1, opinionButton_2, opinionButton_3, opinionButton_4;
    // Variables
    Question[] mQuestions = new Question[2];
    Question mQuestion;
    SignIn mSignIn;

    BackgroundConnection backgroundConnection;

    public Questions() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myFragmentView = inflater.inflate(R.layout.fragment_questions, container, false);
        return myFragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // UI
        // Question textview
        textViewQuestion = getActivity().findViewById(R.id.question);
        // Thumbnail
        Thumbnail = getActivity().findViewById(R.id.questionImage);
        // Question opinion buttons
        opinionButton_1 = getActivity().findViewById(R.id.opinionButton_1);
        opinionButton_2 = getActivity().findViewById(R.id.opinionButton_2);
        opinionButton_3 = getActivity().findViewById(R.id.opinionButton_3);
        opinionButton_4 = getActivity().findViewById(R.id.opinionButton_4);
        // Question opinion listeners
        opinionButton_1.setOnClickListener(this);
        opinionButton_2.setOnClickListener(this);
        opinionButton_3.setOnClickListener(this);
        opinionButton_4.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // First load question
        try {
            loadQuestion();
            loadADS();
        } catch (Exception e) {
            Log.e(TAG, "load: " + e.toString());
        }
    }

    @Override
    public void onClick(View view) {
        // Get Signin and check variables
        mQuestion = mQuestions[0];

        if (mSignIn.clientID != null && mQuestion.question != null && !mQuestion.opinions.isEmpty()) {
            switch (view.getId()) {
                case R.id.opinionButton_1:
                    mQuestion.opinion = mQuestion.opinions.get(0);
                    mQuestion.userOpinionsCount.set(0, mQuestion.userOpinionsCount.get(0) + 1);
                    mQuestion.opinionID = "1";
                    sendQuestion();
                    break;
                case R.id.opinionButton_2:
                    mQuestion.opinion = mQuestion.opinions.get(1);
                    mQuestion.userOpinionsCount.set(1, mQuestion.userOpinionsCount.get(1) + 1);
                    mQuestion.opinionID = "2";
                    sendQuestion();
                    break;
                case R.id.opinionButton_3:
                    mQuestion.opinion = mQuestion.opinions.get(2);
                    mQuestion.userOpinionsCount.set(2, mQuestion.userOpinionsCount.get(2) + 1);
                    mQuestion.opinionID = "3";
                    sendQuestion();
                    break;
                case R.id.opinionButton_4:
                    mQuestion.opinion = mQuestion.opinions.get(3);
                    mQuestion.userOpinionsCount.set(3, mQuestion.userOpinionsCount.get(3) + 1);
                    mQuestion.opinionID = "4";
                    sendQuestion();
                    break;
            }
        }
    }

    public interface QuestionsBackgroundConnectionLoad {
        void updateUIAsync(Question question);
    }

    // [START Load Question]
    void loadQuestion() {
        // Cache
        Boolean cache;
        if (mQuestions[0] == null) {
            // No cache
            cache = false;
            Log.e(TAG, "first");
        } else {
            // Cached
            cache = true;
            if (mQuestions[1] != null) {
                mQuestions[0] = mQuestions[1];
                mQuestions[1] = null;
                updateUI(mQuestions[0]);
                Log.e(TAG, "from cache: " + mQuestions[0].question);
            }
        }



        // Load question from server
        backgroundConnection = new BackgroundConnection(new QuestionsBackgroundConnectionLoad() {
            @Override
            public void updateUIAsync(Question question) {
                if (mQuestions[0] == null) {
                    mQuestions[0] = question;
                    updateUI(mQuestions[0]);
                    Log.e(TAG, "first question: " + mQuestions[0].question);
                    // First cache
                    loadQuestion();
                } else {
                    // Cache
                    if (mQuestions[1] == null) {
                        // First cache
                        mQuestions[1] = question;
                        Log.e(TAG, "cached: " + mQuestions[1].question);
                    } else {
                        // Refresh cache
                        mQuestions[0] = mQuestions[1];
                        mQuestions[1] = question;

                        updateUI(mQuestions[0]);
                        Log.e(TAG, "from cache: " + mQuestions[0].question);
                        Log.e(TAG, "refresh cache: " + mQuestions[1].question);
                    }
                }
            }
        }, getActivity(), "loadQuestion", mSignIn, cache);

        backgroundConnection.execute();
    }

    // Update UI retrieved question from server
    public void updateUI(Question mQuestion) {
        // Prepare UI
        textViewQuestion.setText(R.string.loading_question);
        Thumbnail.setImageBitmap(null);

        opinionButton_1.setVisibility(View.GONE);
        opinionButton_2.setVisibility(View.GONE);
        opinionButton_3.setVisibility(View.GONE);
        opinionButton_4.setVisibility(View.GONE);

        if (mQuestion != null) {
            // Question
            textViewQuestion.setText(mQuestion.question);
            // Thumbnail
            if (mQuestion.thumbnail != null) {
                Thumbnail.setImageBitmap(mQuestion.thumbnail);
            }

            // Opinions
            switch (mQuestion.opinions.size()) {
                case 2:
                    opinionButton_1.setVisibility(View.VISIBLE);
                    opinionButton_2.setVisibility(View.VISIBLE);

                    opinionButton_1.setText(mQuestion.opinions.get(0));
                    opinionButton_2.setText(mQuestion.opinions.get(1));
                    break;
                case 3:
                    opinionButton_1.setVisibility(View.VISIBLE);
                    opinionButton_2.setVisibility(View.VISIBLE);
                    opinionButton_3.setVisibility(View.VISIBLE);

                    opinionButton_1.setText(mQuestion.opinions.get(0));
                    opinionButton_2.setText(mQuestion.opinions.get(1));
                    opinionButton_3.setText(mQuestion.opinions.get(2));
                    break;
                case 4:
                    opinionButton_1.setVisibility(View.VISIBLE);
                    opinionButton_2.setVisibility(View.VISIBLE);
                    opinionButton_3.setVisibility(View.VISIBLE);
                    opinionButton_4.setVisibility(View.VISIBLE);

                    opinionButton_1.setText(mQuestion.opinions.get(0));
                    opinionButton_2.setText(mQuestion.opinions.get(1));
                    opinionButton_3.setText(mQuestion.opinions.get(2));
                    opinionButton_4.setText(mQuestion.opinions.get(3));
                    break;
            }
        } else {
            // No Questions
            textViewQuestion.setText(R.string.no_questions);
            opinionButton_1.setVisibility(View.GONE);
            opinionButton_2.setVisibility(View.GONE);
            opinionButton_3.setVisibility(View.GONE);
            opinionButton_4.setVisibility(View.GONE);
        }
    }
    // [End load question]

    public interface QuestionsBackgroundConnectionSend {
        void loadNextQuestion();
    }

    // Send Question
    void sendQuestion() {
        BackgroundConnection backgroundConnection = new BackgroundConnection(new QuestionsBackgroundConnectionSend() {
            @Override
            public void loadNextQuestion() {
                loadQuestion();
            }
        }, getContext(), "sendQuestion", mSignIn, mQuestion);
        backgroundConnection.execute();
    }

    // ADS
    void loadADS() {
        MobileAds.initialize(getActivity(), getResources().getString(R.string.banner_ad_top_id));
        mAdView = getActivity().findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    // signIn
    void signInEvent(SignIn signIn) {
        mSignIn = signIn;
    }
}
