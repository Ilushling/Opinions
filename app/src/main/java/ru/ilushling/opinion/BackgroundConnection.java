package ru.ilushling.opinion;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class BackgroundConnection extends AsyncTask<String, String, Question> {

    // Common
    private String TAG = "Background";
    private Context context;

    // Variables
    private URL url;
    private HttpURLConnection httpURLConnection;
    private String method, post_data;
    private SignIn mSignIn;
    private Question mQuestion;
    // Connection
    String responce, logs, server;
    Boolean cache = false;

    // Questions
    private Questions.QuestionsBackgroundConnectionLoad mQuestionsBackgroundConnectionLoad;
    private Questions.QuestionsBackgroundConnectionSend mQuestionsBackgroundConnectionSend;
    // Achivements
    private Achievements.BackgroundConnectionAchievementsLoad mBackgroundConnectionAchievementsLoad;

    // Load or SignIn
    public BackgroundConnection(Context context, String method, SignIn mSignIn) {
        this.context = context;
        this.method = method;
        this.mSignIn = mSignIn;
        this.mQuestion = new Question();
    }

    // Questions
    // Load
    public BackgroundConnection(Questions.QuestionsBackgroundConnectionLoad backgroundConnectionLoad, Context context, String method, SignIn mSignIn, Boolean cache) {
        this.mQuestionsBackgroundConnectionLoad = backgroundConnectionLoad;
        this.context = context;
        this.method = method;
        this.mSignIn = mSignIn;
        this.cache = cache;
    }

    // Send
    public BackgroundConnection(Questions.QuestionsBackgroundConnectionSend backgroundConnectionSend, Context context, String method, SignIn mSignIn, Question mQuestion) {
        this.mQuestionsBackgroundConnectionSend = backgroundConnectionSend;
        this.context = context;
        this.method = method;
        this.mSignIn = mSignIn;
        this.mQuestion = mQuestion;
    }

    // Achivements
    public BackgroundConnection(Achievements.BackgroundConnectionAchievementsLoad backgroundConnectionLoad, Context context, String method, SignIn mSignIn) {
        this.mBackgroundConnectionAchievementsLoad = backgroundConnectionLoad;
        this.context = context;
        this.method = method;
        this.mSignIn = mSignIn;
    }


    @Override
    protected Question doInBackground(String... params) {
        // Variables
        //server = "http://lifeschool.ddns.net";
        server = "http://ilushling.cloudns.cc";
        String connection_url = server + "/api/v1/questions";
        //Log.e(TAG, "method = " + method);

        if (method != null && mSignIn != null && mSignIn.token != null) {
            try {
                // Open Connection
                url = new URL(connection_url);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

                // Send request to server
                send();

                // Receive responce from server
                switch (method) {
                    case "signIn":
                        receive();
                        // Disconnect
                        httpURLConnection.disconnect();
                        return mQuestion;
                    case "loadQuestion":
                        mQuestion = receive();
                        // Disconnect
                        httpURLConnection.disconnect();
                        return mQuestion;
                    case "sendQuestion":
                        Log.e(TAG, "sending...");
                        receive();
                        httpURLConnection.disconnect();
                        break;
                }
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Log.e(TAG, "Not signed for connection");
        }
        return null;
    }

    // [START Send]
    private void send() {
        try {
            // Sending info (POST)
            switch (method) {
                case "signIn":
                    post_data = URLEncoder.encode("method", "UTF-8") + "=" + URLEncoder.encode(method, "UTF-8") + "&" +
                            URLEncoder.encode("token", "UTF-8") + "=" + URLEncoder.encode(mSignIn.token, "UTF-8") + "&" +
                            URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(mSignIn.email, "UTF-8") + "&" +
                            URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(mSignIn.name, "UTF-8") + "&" +
                            URLEncoder.encode("photoUrl", "UTF-8") + "=" + URLEncoder.encode(mSignIn.photoUrl, "UTF-8");
                    break;
                case "loadQuestion":
                    post_data = URLEncoder.encode("method", "UTF-8") + "=" + URLEncoder.encode(method, "UTF-8") + "&" +
                            URLEncoder.encode("token", "UTF-8") + "=" + URLEncoder.encode(mSignIn.token, "UTF-8") + "&" +
                            URLEncoder.encode("cache", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(cache), "UTF-8") + "&" +
                            URLEncoder.encode("language", "UTF-8") + "=" + URLEncoder.encode(mSignIn.language, "UTF-8");
                    break;
                case "loadStatistic":
                    post_data = URLEncoder.encode("method", "UTF-8") + "=" + URLEncoder.encode(method, "UTF-8") + "&" +
                            URLEncoder.encode("token", "UTF-8") + "=" + URLEncoder.encode(mSignIn.token, "UTF-8");
                    break;
                case "sendQuestion":
                    if (mQuestion.question != null && mQuestion.opinion != null) {
                        post_data = URLEncoder.encode("method", "UTF-8") + "=" + URLEncoder.encode(method, "UTF-8") + "&" +
                                URLEncoder.encode("token", "UTF-8") + "=" + URLEncoder.encode(mSignIn.token, "UTF-8") + "&" +
                                URLEncoder.encode("questionID", "UTF-8") + "=" + URLEncoder.encode(mQuestion.questionID, "UTF-8") + "&" +
                                URLEncoder.encode("opinionID", "UTF-8") + "=" + URLEncoder.encode(mQuestion.opinionID, "UTF-8");
                    } else {
                        Log.e(TAG, "sendQuestion: null");
                    }
                    break;
            }

            if (post_data != null) {
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Send" + e);
        }
    }
    // [END Send]

    // [START Receive]
    private Question receive() {
        try {
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            String line = "";
            responce = "";

            while ((line = bufferedReader.readLine()) != null) {
                responce += line;
            }
            bufferedReader.close();
            inputStream.close();

            Log.e(TAG, "RAW PARSING [HTML]: " + responce);

            if (responce != "") {
                try {
                    switch (method) {
                        case "signIn":
                            logs = responce;
                            break;
                        case "loadQuestion":
                            // Parsing JSON
                            String jsonString = responce;
                            JSONObject jsonObject = new JSONObject(jsonString);
                            // Prepare mQuestion object for receive
                            mQuestion = new Question();
                            JSONArray jsonQuestions = jsonObject.getJSONArray("questions");
                            for (int i = 0; i < jsonQuestions.length(); i++) {
                                JSONObject jsonQuestion = jsonQuestions.getJSONObject(i);
                                // ID question
                                mQuestion.questionID = jsonQuestion.getString("questionID");
                                // Question
                                mQuestion.question = jsonQuestion.getString("question");
                                // Opinions
                                JSONArray jsonOpinions = jsonQuestion.getJSONArray("opinions");
                                // User opinions count
                                JSONObject jsonOpinionsCount = jsonQuestion.getJSONObject("userOpinionsCount");
                                // Parsing array of opinions and put user opinions to each opinion
                                for (int j = 0; j < jsonOpinions.length(); j++) {
                                    mQuestion.opinions.add(jsonOpinions.getString(j));
                                    mQuestion.userOpinionsCount.add(Integer.valueOf(jsonOpinionsCount.getString(String.valueOf(j + 1))));
                                }
                                try {
                                    // Thumbnail
                                    if (jsonQuestion.has("thumbnail") && jsonQuestion.getString("thumbnail") != null) {
                                        Log.e(TAG, "loading thumbnail");
                                        InputStream in = new java.net.URL(server + "/" + jsonQuestion.getString("thumbnail")).openStream();
                                        mQuestion.thumbnail = BitmapFactory.decodeStream(in);
                                        Log.e(TAG, "thumbnail loaded");
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "error load thumbnail: " + e);
                                }
                            }
                            return mQuestion;
                        case "sendQuestion":
                            Log.e(TAG, "sendQuestion: " + responce);
                            break;
                        case "loadStatistic":
                            // Prepare mQuestion object for receive
                            mQuestion = new Question();
                            // / Parsing JSON
                            jsonObject = new JSONObject(responce);
                            // Getting (Object)
                            // ID question
                            mQuestion.questionID = jsonObject.getString("questionID");
                            // Question
                            mQuestion.question = jsonObject.getString("question");
                            // Opinions
                            mQuestion.opinion = jsonObject.getString("opinion");
                            break;
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Receive JSON: " + e.toString());
                    return null;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Receive: " + e);
            return null;
        }
        return null;
    }
    // [END Receive]

    ProgressDialog pDialog;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        switch (method) {
            case "loadQuestion":
                if (!cache) {
                    pDialog = new ProgressDialog(context);
                    pDialog.setMessage(context.getString(R.string.loading_question));
                    pDialog.setIndeterminate(false);
                    pDialog.setCancelable(false);
                    pDialog.show();
                } else {
                    //Log.e(TAG, "caching...");
                }
                break;
            case "sendQuestion":
                // Animation sending
                pDialog = new ProgressDialog(context);
                pDialog.setMessage(context.getString(R.string.sending_question));
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(false);
                pDialog.show();
                break;
        }
    }

    @Override
    protected void onPostExecute(Question question) {
        super.onPostExecute(question);
        // update UI
        try {
            switch (method) {
                case "signIn":
                    break;
                case "loadQuestion":
                    // Animation loading end
                    if (!cache) {
                        pDialog.dismiss();
                    } else {
                        //Log.e(TAG, "cached");
                    }

                    if (question != null && question.question != "") {
                        mQuestionsBackgroundConnectionLoad.updateUIAsync(question);
                    } else {
                        mQuestionsBackgroundConnectionLoad.updateUIAsync(null);
                    }
                    break;
                case "sendQuestion":
                    // Animation sending end
                    pDialog.dismiss();

                    String userResult = "";
                    for (int i = 0; i < mQuestion.opinions.size(); i++) {
                        userResult += "За \"" + mQuestion.opinions.get(i) + "\" проголовало " + mQuestion.userOpinionsCount.get(i) + " человек\n";
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Статистика")
                            .setMessage(userResult)
                            .setCancelable(false)
                            .setNegativeButton("Следующий вопрос",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            mQuestionsBackgroundConnectionSend.loadNextQuestion();
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                    break;
                case "loadStatistic":
                    //mBackgroundConnectionLoad.updateUIAsync(question);
                    break;
            }
        } catch (ClassCastException e) {
            Log.e(TAG, "onPost: " + e);
        }
    }

}
