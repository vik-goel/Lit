package mchacks.io.lit;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.ArrayList;

public class Comment {

    private static Bitmap defaultUp, accentUp, defaultDown, accentDown;

    public static void loadBitmaps(Resources res) {
        if(defaultUp != null) {
            return;
        }

        defaultUp = BitmapFactory.decodeResource(res, R.drawable.ic_keyboard_arrow_up_white_24dp);
        accentUp = BitmapFactory.decodeResource(res, R.drawable.ic_keyboard_arrow_up_accent_24dp);
        defaultDown = BitmapFactory.decodeResource(res, R.drawable.ic_keyboard_arrow_down_white_24dp);
        accentDown = BitmapFactory.decodeResource(res, R.drawable.ic_keyboard_arrow_down_accent_24dp);
    }

    public static int tempId = 1;

    public String text;
    public int iconId;

    public VoteData voteData;
    public long timeStamp;

    public Comment(long timeStamp, String text, int iconId, int id, int votes) {
        this.timeStamp = timeStamp;
        this.text = text;
        this.iconId = iconId;
        voteData = new VoteData(id, votes, MainActivity.commentVotes);
    }



}
