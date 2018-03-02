package com.example.cpu10661.navigationinapp;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cpu10661.navigationinapp.Utils.DirectionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cpu10661 on 3/2/18.
 */

public class StepListAdapter extends RecyclerView.Adapter<StepListAdapter.StepViewHolder> {

    private List<Step> mSteps;

    public StepListAdapter(List<Step> steps) {
        mSteps = steps;
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.instruction_row_item, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        Step step = mSteps.get(position);
        holder.mIconImageView.setImageResource(DirectionUtil.getDirectionIconRes(step.getManeuver()));
        Spanned instruction = Build.VERSION.SDK_INT < Build.VERSION_CODES.N ?
                Html.fromHtml(step.getInstruction()) :
                Html.fromHtml(step.getInstruction(), Html.FROM_HTML_SEPARATOR_LINE_BREAK_DIV);
        holder.mInstructionTextView.setText(trimTrailingWhitespace(instruction));
        holder.mDistanceTextView.setText(step.getDistance());
    }

    /**
     * Trims trailing whitespace. Removes any of these characters:
     * 0009, HORIZONTAL TABULATION
     * 000A, LINE FEED
     * 000B, VERTICAL TABULATION
     * 000C, FORM FEED
     * 000D, CARRIAGE RETURN
     * 001C, FILE SEPARATOR
     * 001D, GROUP SEPARATOR
     * 001E, RECORD SEPARATOR
     * 001F, UNIT SEPARATOR
     *
     * @return "" if source is null, otherwise string with all trailing whitespace removed
     */
    private static CharSequence trimTrailingWhitespace(CharSequence source) {
        if (source == null)
            return "";
        int i = source.length();
        // loop back to the first non-whitespace character
        while (--i >= 0 && Character.isWhitespace(source.charAt(i)));
        return source.subSequence(0, i + 1);
    }

    @Override
    public int getItemCount() {
        return mSteps.size();
    }

    class StepViewHolder extends RecyclerView.ViewHolder {
        ImageView mIconImageView;
        TextView mInstructionTextView;
        TextView mDistanceTextView;

        StepViewHolder(View itemView) {
            super(itemView);
            mIconImageView = itemView.findViewById(R.id.iv_direction_icon);
            mInstructionTextView = itemView.findViewById(R.id.tv_instruction);
            mDistanceTextView = itemView.findViewById(R.id.tv_distance);
        }
    }
}
