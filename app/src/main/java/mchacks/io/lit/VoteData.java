package mchacks.io.lit;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.HashMap;

public class VoteData {

    public int id;
    public int votes;
    public HashMap<Integer, VoteState> voteMap;
    public Post post;
    public int arrayIndex;

    public VoteData(int id, int votes, HashMap<Integer, VoteState> voteMap) {
        this.id = id;
        this.votes = votes;
        this.voteMap = voteMap;
    }

    public void vote(VoteState state, ImageView upvoteButton, ImageView downvoteButton) {
        VoteState curState = MainActivity.commentVotes.get(id);
        MainActivity.commentVotes.remove(id);

        if(curState == state) {
            state = VoteState.NONE;
            upvoteButton.setImageDrawable(ContextCompat.getDrawable(upvoteButton.getContext(), R.drawable.ic_keyboard_arrow_up_white_24dp));
            downvoteButton.setImageDrawable(ContextCompat.getDrawable(downvoteButton.getContext(), R.drawable.ic_keyboard_arrow_down_white_24dp));
        }
        else {
            switch (state) {
                case UPVOTE:
                    upvoteButton.setImageDrawable(ContextCompat.getDrawable(upvoteButton.getContext(), R.drawable.ic_keyboard_arrow_up_accent_24dp));
                    break;
                case DOWNVOTE:
                    downvoteButton.setImageDrawable(ContextCompat.getDrawable(downvoteButton.getContext(), R.drawable.ic_keyboard_arrow_down_accent_24dp));
                    break;
            }
            MainActivity.commentVotes.put(id, state);
        }

        if(curState == null) {
            curState = VoteState.NONE;
        }

        int dVote = 0;

        switch(curState) {
            case DOWNVOTE:
                switch(state) {
                    case UPVOTE:
                        dVote = 2;
                        break;
                    case NONE:
                        dVote = 1;
                        break;
                }
                break;
            case UPVOTE:
                switch(state) {
                    case DOWNVOTE:
                        dVote = -2;
                        break;
                    case NONE:
                        dVote = -1;
                        break;
                }
                break;
            case NONE:
                switch(state) {
                    case UPVOTE:
                        dVote = 1;
                        break;
                    case DOWNVOTE:
                        dVote = -1;
                        break;
                }
                break;
        }


        if(dVote != 0) {
            votes += dVote;
        }

        post.adapter.notifyItemChanged(arrayIndex);

        RecyclerView.Adapter postAdapter = post.myAdapter;

        if(postAdapter != null) {
            postAdapter.notifyItemChanged(arrayIndex);
        }
    }

    public void initVoteButtons(View v) {
        final ImageView upvoteButton = (ImageView) v.findViewById(R.id.upvote_button);
        final ImageView downvoteButton = (ImageView) v.findViewById(R.id.downvote_button);

        upvoteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vote(VoteState.UPVOTE, upvoteButton, downvoteButton);
            }
        });

        downvoteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vote(VoteState.DOWNVOTE, upvoteButton, downvoteButton);
            }
        });
    }

}
