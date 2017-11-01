package ru.ilushling.opinion;

import java.util.ArrayList;
import java.util.List;

public class Question {
    String questionID, opinionID;
    String question;
    List<String> opinions = new ArrayList<String>();
    List<Integer> userOpinionsCount = new ArrayList<Integer>();
    String opinion;
}
