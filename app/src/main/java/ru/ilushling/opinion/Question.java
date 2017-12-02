package ru.ilushling.opinion;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class Question {
    String questionID, opinionID, question, opinion;
    Bitmap thumbnail;
    List<String> opinions = new ArrayList<String>();
    List<Integer> userOpinionsCount = new ArrayList<Integer>();
    List<Integer> userOpinionsPercentage = new ArrayList<Integer>();
}
