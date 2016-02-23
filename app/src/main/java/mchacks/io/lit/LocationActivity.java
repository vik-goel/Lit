package mchacks.io.lit;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class LocationActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static LitLocation loc;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.location_main);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();

        if (bar != null) {
            bar.setTitle(loc.address);
            bar.setDisplayHomeAsUpEnabled(true);
        }

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        final RecyclerView recList = (RecyclerView) findViewById(R.id.image_list);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        recList.setAdapter(new PostAdapter(loc.posts));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double long1 = 20.344;
                double lat1 = 34.34;
                double long2 = 20.5666;
                double lat2 = 45.345;

                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?saddr=" + long1 + "," + lat1 + "&daddr=" + long2 + "," + lat2));
                startActivity(intent);
            }
        });

        TextView textView = (TextView)findViewById(R.id.lit_score);

        int emoji = 0x1F525;
        String s = new String(Character.toChars(emoji)) + String.valueOf(loc.votes);
        textView.setText(s);
    }

    public void onRefresh() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
