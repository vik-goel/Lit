package mchacks.io.lit;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private List<LitLocation> locList;

    public LocationAdapter(List<LitLocation> locList) {
        this.locList = locList;
    }

    @Override
    public int getItemCount() {
        return locList.size();
    }

    @Override
    public void onBindViewHolder(LocationViewHolder locViewHolder, int position) {
       LitLocation loc = locList.get(position);
        locViewHolder.loc = loc;

        int emoji = 0x1F525;
        String scoreString = new String(Character.toChars(emoji)) + String.valueOf(loc.votes);

        locViewHolder.scoreText.setText(scoreString);
        locViewHolder.addressText.setText(loc.address);
        locViewHolder.imageView1.setImageResource(loc.posts.get(0).imageId);

        LinearLayout imageHolder = (LinearLayout)locViewHolder.imageView1.getParent();

        int numImages = Math.min(3, loc.posts.size());

       for(int i = 1; i < numImages; i++) {
          Post post = loc.posts.get(i);

           ImageView view = new ImageView(imageHolder.getContext());
           view.setImageResource(post.imageId);

           LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
           view.setLayoutParams(params);
           view.setAdjustViewBounds(true);
           view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

           imageHolder.addView(view);
       }

        imageHolder.setWeightSum(numImages);
    }

    @Override
    public LocationViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
       View itemView = LayoutInflater.
        from(viewGroup.getContext()).
                inflate(R.layout.location_card, viewGroup, false);

        return new LocationViewHolder(itemView);
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected ImageView imageView1;
        protected TextView addressText;
        protected TextView scoreText;
        protected LitLocation loc;

        public LocationViewHolder(View v) {
            super(v);

            imageView1 =  (ImageView) v.findViewById(R.id.lit_pic1);
            addressText = (TextView)  v.findViewById(R.id.address);
            scoreText = (TextView)  v.findViewById(R.id.loc_score);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            LocationActivity.loc = loc;

            Intent intent = new Intent(v.getContext(), LocationActivity.class);

            Pair<View, String> p1 = Pair.create(((Activity) v.getContext()).findViewById(R.id.fab), "fab");

            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation((Activity)v.getContext(), p1);

            v.getContext().startActivity(intent, options.toBundle());
        }
    }
}
