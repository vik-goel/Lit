package mchacks.io.lit;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private ArrayList<Comment> commentList = new ArrayList<Comment>();

    public CommentAdapter(ArrayList<Comment> commentList) {
        this.commentList = commentList;
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    @Override
    public void onBindViewHolder(CommentViewHolder commentViewHolder, int i) {
        Comment comment = commentList.get(i);

        commentViewHolder.upvoteText.setText(String.valueOf(comment.voteData.votes));
        commentViewHolder.commentText.setText(comment.text);
        commentViewHolder.iconView.setImageResource(comment.iconId);
        commentViewHolder.comment = comment;
        commentViewHolder.timeText.setText(Util.getTimeString(comment.timeStamp));

        comment.voteData.initVoteButtons((View)commentViewHolder.commentText.getParent());
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.comment_card, viewGroup, false);

        return new CommentViewHolder(itemView);
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        protected ImageView iconView;
        protected TextView commentText;
        protected TextView upvoteText;
        protected TextView timeText;
        protected Comment comment;

        public CommentViewHolder(View v) {
            super(v);

            iconView = (ImageView) v.findViewById(R.id.comment_icon);
            commentText = (TextView) v.findViewById(R.id.comment_text);
            upvoteText = (TextView) v.findViewById(R.id.upvoteText);
            timeText = (TextView) v.findViewById(R.id.time_since_comment_posted);
        }
    }
}

