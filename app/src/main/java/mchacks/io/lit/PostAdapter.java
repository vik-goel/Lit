package mchacks.io.lit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ContactViewHolder> {

    private List<Post> contactList;
    public static AmazonS3Client s3Client = new AmazonS3Client(new BasicAWSCredentials(
            Key.AWS_KEY, Key.AWS_SECRET));

    public PostAdapter(List<Post> contactList) {
        this.contactList = contactList;
    }

    public PostAdapter() {
        contactList = new ArrayList<>();
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }



    public void setData(ArrayList<Post> posts) {
        contactList.clear();
        for (int i = 0; i < posts.size(); i++) {
            posts.get(i).voteData.arrayIndex = i;
            contactList.add(posts.get(i));
        }
        notifyDataSetChanged();
    }


    @Override
    public void onBindViewHolder(ContactViewHolder contactViewHolder, int i) {
        Post post = contactList.get(i);

        contactViewHolder.commentText.setText(String.valueOf(post.comments.size()));
        try {
            contactViewHolder.imageView.setImageBitmap(new GetFromAWSTask().execute(post.imageId).get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        contactViewHolder.timeText.setText(Util.getTimeString(post.timestamp));
        contactViewHolder.upvoteText.setText(String.valueOf(post.voteData.votes));
        contactViewHolder.imageId = post.imageId;
    }

    private class GetFromAWSTask extends AsyncTask<Integer, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Integer... params) {
            Log.d("PostAdapter", String.valueOf(params[0]));
            GetObjectRequest gor = new GetObjectRequest("lit-images", String.valueOf(params[0]));
            S3ObjectInputStream inputStream = s3Client.getObject(gor).getObjectContent();
            return BitmapFactory.decodeStream(inputStream);
        }
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.post_card, viewGroup, false);

        return new ContactViewHolder(itemView, contactList.get(i));
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected ImageView imageView;
        protected TextView commentText;
        protected TextView timeText;
        protected TextView upvoteText;
        protected int imageId;
        protected Post post;

        public ContactViewHolder(View v, Post post) {
            super(v);

            imageView =  (ImageView) v.findViewById(R.id.imageView);
            commentText = (TextView)  v.findViewById(R.id.commentText);
            timeText = (TextView)  v.findViewById(R.id.timeText);
            upvoteText = (TextView) v.findViewById(R.id.upvoteText);
            this.post = post;

            post.voteData.initVoteButtons(v);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), PictureActivity.class);

            Bitmap bmp = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            intent.putExtra("picture", byteArray);

            Pair<View, String> p1 =  Pair.create(v, "image");
            Pair<View, String> p2 = Pair.create(((Activity) v.getContext()).findViewById(R.id.fab), "fab");

            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation((Activity)v.getContext(), p1, p2);

            PictureActivity.post = post;

            v.getContext().startActivity(intent, options.toBundle());
        }
    }
}
