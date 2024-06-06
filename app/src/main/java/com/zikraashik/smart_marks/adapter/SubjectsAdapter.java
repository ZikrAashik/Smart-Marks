package com.zikraashik.smart_marks.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zikraashik.smart_marks.R;
import com.zikraashik.smart_marks.model.Subject;

import java.util.List;

public class SubjectsAdapter extends RecyclerView.Adapter<SubjectsAdapter.SubjectViewHolder> {

    private List<Subject> subjectList;
    private Context context;

    public SubjectsAdapter(Context context, List<Subject> subjectList) {
        this.context = context;
        this.subjectList = subjectList;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Subject subject = subjectList.get(position);

        holder.subjectName.setText(subject.getName());
        holder.subjectMarks.setText(String.valueOf(subject.getMarks()));

        holder.btnRemoveSubject.setOnClickListener(v -> {
            subjectList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, subjectList.size());
        });
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    public static class SubjectViewHolder extends RecyclerView.ViewHolder {
        EditText subjectName, subjectMarks;
        ImageView btnRemoveSubject;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectName = itemView.findViewById(R.id.tv_subject_name);
            subjectMarks = itemView.findViewById(R.id.et_subject_marks);
            btnRemoveSubject = itemView.findViewById(R.id.btn_remove_subject);
        }
    }
}


