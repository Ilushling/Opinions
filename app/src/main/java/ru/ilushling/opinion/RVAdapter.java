package ru.ilushling.opinion;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.QuestionsViewHolder> {

    List<Question> questions;

    RVAdapter(List<Question> questions) {
        this.questions = questions;
    }

    public static class QuestionsViewHolder extends RecyclerView.ViewHolder {
        TextView question, opinion, percent;

        QuestionsViewHolder(View itemView) {
            super(itemView);
            question = itemView.findViewById(R.id.question);
            opinion = itemView.findViewById(R.id.opinion);
            percent = itemView.findViewById(R.id.percent);
        }
    }

    @Override
    public QuestionsViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.questions_history_item, viewGroup, false);
        return new QuestionsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(QuestionsViewHolder questionsViewHolder, int position) {
        questionsViewHolder.question.setText(questions.get(position).question);
        questionsViewHolder.opinion.setText(questions.get(position).opinions.get(Integer.parseInt(questions.get(position).opinion) - 1));
        questions = toPercentages(questions);

        questionsViewHolder.percent.setText(questions.get(position).userOpinionsPercentage.get(Integer.parseInt(questions.get(position).opinion) - 1) + "%");
    }

    List<Question> toPercentages(List<Question> questions) {
        for (int i = 0; i < questions.size(); i++) {
            int userOpinionTotal = 0;
            for (int k = 0; k < questions.get(i).userOpinionsCount.size(); k++) {
                userOpinionTotal += questions.get(i).userOpinionsCount.get(k);
            }
            for (int j = 0; j < questions.get(i).opinions.size(); j++) {
                questions.get(i).userOpinionsPercentage.add((questions.get(i).userOpinionsCount.get(j) * 100) / userOpinionTotal);
            }
        }
        return questions;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return questions != null ? questions.size() : 0;
    }
}