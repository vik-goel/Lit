package mchacks.io.lit;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import java.util.ArrayList;

public class Post {

    public long timestamp;
    public ArrayList<Comment> comments = new ArrayList<>();
    public RecyclerView.Adapter adapter;
    public int imageId;

    public RecyclerView.Adapter myAdapter;
    public VoteData voteData;

    public Post(long timestamp, int votes, int imageId) {

        this.timestamp = timestamp;
        this.imageId = imageId;
        this.comments = new ArrayList<>();

        voteData = new VoteData(imageId, votes, MainActivity.postVotes);
        voteData.post = this;

        adapter = new CommentAdapter(comments);
    }

    public void addComment(Comment comment) {
        comments.add(comment);
        comment.voteData.post = this;
        comment.voteData.arrayIndex = comments.size() - 1;
        adapter.notifyItemInserted(comments.size());
    }
}