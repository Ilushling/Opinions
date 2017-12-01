package ru.ilushling.opinion;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class Profile extends Fragment implements View.OnClickListener {

    String TAG = "FragmentProfile";
    // UI
    public Button mSignInButton;
    public TextView mStatus;
    RadioButton radioRU, radioEN;
    ListView lvMain;

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
        // находим список
        lvMain = getActivity().findViewById(R.id.lvMain);

        // Sign In
        mSignInButton = getActivity().findViewById(R.id.sign_in_button);
        mStatus = getActivity().findViewById(R.id.statuslabel);

        // Add click listeners for the buttons
        mSignInButton.setOnClickListener(this);
        mSignInButton.setEnabled(true);

        // Language
        RadioGroup radioGroup = getActivity().findViewById(R.id.language);
        radioRU = getActivity().findViewById(R.id.RU);
        radioEN = getActivity().findViewById(R.id.EN);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.RU:
                        if (mSignIn != null) {
                            mSignIn.language = "RU";
                        } else {
                            radioRU.setChecked(false);
                            radioEN.setChecked(false);
                        }
                        break;
                    case R.id.EN:
                        if (mSignIn != null) {
                            mSignIn.language = "EN";
                        } else {
                            radioRU.setChecked(false);
                            radioEN.setChecked(false);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        // END language

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
        mSignIn = null;
        profileEventListener.signOutEvent();
    }

    public void updateUI(int signedIn) {
        if (mSignInButton != null) {
            if (signedIn == 0 && mSignIn != null) {
                // Signed In
                mSignInButton.setText(R.string.sign_out);
                mStatus.setText(mSignIn.name);

                // Language
                Log.e(TAG, Locale.getDefault().getLanguage());
                if (Locale.getDefault().getLanguage().equals("ru") && mSignIn.language == null) {
                    mSignIn.language = "RU";
                    radioRU.setChecked(true);
                } else {
                    mSignIn.language = "EN";
                    radioEN.setChecked(true);
                }

                updateQuestionsHistory();
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

    public interface QuestionsBackgroundConnectionQuestionsHistory {
        void loadQuestionQuestionsHistory(List<Question> questions);
    }

    // Update Questions History
    public void updateQuestionsHistory() {
        BackgroundConnection backgroundConnection = new BackgroundConnection(new QuestionsBackgroundConnectionQuestionsHistory() {
            @Override
            public void loadQuestionQuestionsHistory(List<Question> questions) {
                List<String> questionsList = new ArrayList<String>();
                List<String> opinionsList = new ArrayList<String>();
                for (int i = 0; i < questions.size(); i++) {
                    if (questions.get(i) != null) {
                        questionsList.add(questions.get(i).question);
                        opinionsList.add(questions.get(i).opinion);
                    }
                }
                // создаем адаптер
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.questions_history_item, questionsList);
                // присваиваем адаптер списку
                lvMain.setAdapter(adapter);
            }
        }, getActivity(), "loadQuestionsHistory", mSignIn, 0);
        backgroundConnection.execute();
    }

    String getLanguage() {
        return mSignIn.language;
    }
}
