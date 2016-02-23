package mchacks.io.lit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PictureActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static Random random = new Random();

    private int page = 0;
    private int imageId = -1;
    private boolean loading = false;
    private SwipeRefreshLayout swipeRefreshLayout;

    public static Post post;

    private static final int[] commentIcons = new int[]{
            R.drawable.comment_icon1a, R.drawable.comment_icon1b, R.drawable.comment_icon1c, R.drawable.comment_icon1d,
            R.drawable.comment_icon2a, R.drawable.comment_icon2b, R.drawable.comment_icon2c, R.drawable.comment_icon2d,
            R.drawable.comment_icon3a, R.drawable.comment_icon3b, R.drawable.comment_icon3c, R.drawable.comment_icon3d,
            R.drawable.comment_icon4a, R.drawable.comment_icon4b, R.drawable.comment_icon4c, R.drawable.comment_icon4d,
            R.drawable.comment_icon5a, R.drawable.comment_icon5b, R.drawable.comment_icon5c, R.drawable.comment_icon5d,
            R.drawable.comment_icon6a, R.drawable.comment_icon6b, R.drawable.comment_icon6c, R.drawable.comment_icon6d,
            R.drawable.comment_icon7a, R.drawable.comment_icon7b, R.drawable.comment_icon7c, R.drawable.comment_icon7d,
            R.drawable.comment_icon8a, R.drawable.comment_icon8b, R.drawable.comment_icon8c, R.drawable.comment_icon8d,
            R.drawable.comment_icon9a, R.drawable.comment_icon9b, R.drawable.comment_icon9c, R.drawable.comment_icon9d,
            R.drawable.comment_icon10a, R.drawable.comment_icon10b, R.drawable.comment_icon10c, R.drawable.comment_icon10d,
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Comment.loadBitmaps(getResources());

        setContentView(R.layout.image_main);

        Intent intent = getIntent();
        ImageView view = (ImageView) findViewById(R.id.image_view_big);
        byte[] byteArray = intent.getByteArrayExtra("picture");
        view.setImageBitmap(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length));
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        view.requestLayout();
        view.setMaxHeight(height / 2);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();

        if (bar != null) {
            bar.setTitle("");
            bar.setDisplayHomeAsUpEnabled(true);
        }


        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        final RecyclerView recList = (RecyclerView) findViewById(R.id.comment_list);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        recList.setAdapter(post.adapter);

        final EditText editText = (EditText) findViewById(R.id.comment_box);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable text = editText.getText();

                int imageId = -1;
                int commentId = Comment.tempId;

                boolean[] takenIds = new boolean[commentIcons.length];

                for (Comment comment : post.comments) {
                    for (int i = 0; i < commentIcons.length; i++) {
                        if (commentIcons[i] == comment.iconId) {
                            takenIds[i] = true;
                        }
                    }

                    Integer id = MainActivity.commentImages.get(comment.voteData.id);

                    if (id != null) {
                        imageId = id;
                        break;
                    }
                }

                if (imageId == -1) {
                    while (true) {
                        imageId = random.nextInt(commentIcons.length);
                        if (!takenIds[imageId]) {
                            imageId = commentIcons[imageId];
                            break;
                        }
                    }
                }

                MainActivity.commentImages.put(commentId, imageId);

                post.addComment(new Comment(System.currentTimeMillis(), text.toString(), imageId, Comment.tempId++, 0));

                editText.setText("");
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(editText.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        if (!loading) {
            new LoadCommentsTask(true).execute();
        }
    }

    class LoadCommentsTask extends PostTask {

        private boolean isRefreshing;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = true;
        }

        public LoadCommentsTask(boolean isRefreshing) {
            super(S.SITE_ROOT + "load_comments.php", PictureActivity.this);
            this.isRefreshing = isRefreshing;
            params.put("secret", Key.SECRET);
            params.put("photo_id", String.valueOf(imageId));
            params.put("page", String.valueOf(page));
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!isRefreshing) {
                page++;
            }
            ArrayList<Comment> comments = new ArrayList<>();
            try {
                JSONArray jsonComments = new JSONArray(s);
                for (int i = 0; i < jsonComments.length(); i++) {
                    JSONArray jsonComment = jsonComments.getJSONArray(i);
                    comments.add(new Comment(Timestamp.valueOf((String) jsonComment.get(0)).getTime(), (String) jsonComment.get(1), (int) jsonComment.get(2), (int) jsonComment.get(3), (int) jsonComment.get(4)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            loading = false;
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
