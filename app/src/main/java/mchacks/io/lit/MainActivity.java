package mchacks.io.lit;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements
        LocationListener {

    public static HashMap<Integer, Integer> commentImages = new HashMap<>();
    public static HashMap<Integer, VoteState> commentVotes = new HashMap<>();
    public static HashMap<Integer, VoteState> postVotes = new HashMap<>();


    public static void saveLocalData(Context context) {
        try {
            File file = new File(context.getDir("data", MODE_PRIVATE), "map");
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(commentImages);
            outputStream.writeObject(commentVotes);
            outputStream.writeObject(postVotes);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void readLocalData(Context context) {
        try {
            File file = new File(context.getDir("data", MODE_PRIVATE), "map");
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(file));
            commentImages = (HashMap<Integer, Integer>) is.readObject();
            commentVotes = (HashMap<Integer, VoteState>) is.readObject();
            postVotes = (HashMap<Integer, VoteState>) is.readObject();

            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int STORAGE_PERMISSION_REQUEST = 102;
    public static AmazonS3Client s3Client = new AmazonS3Client(new BasicAWSCredentials(
            Key.AWS_KEY, Key.AWS_SECRET));

    private ViewPager mViewPager;
    private TabLayout tabLayout;
    String mCurrentPhotoPath, imageFileName;
    Uri output = null;
    File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    private File picFile = null;
    private File croppedPicFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.getBackground().setAlpha(255);

        ActionBar bar = getSupportActionBar();

        if (bar != null) {
            // bar.hide();
            bar.setTitle("");
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    takePictureIntent();
                }
            }
        });
    }

    private void takePictureIntent() {
        Intent takePictureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile;
            try {
                photoFile = createImageFile();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyMMdd-HHmmss", Locale.CANADA).format(new Date());
        imageFileName = "JPEG_" + timeStamp + ".jpg";
        File image = new File(storageDir + File.separator + imageFileName);
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void cropCapturedImage(Uri uri) {
        Log.d("Alex", "Cropping captured image");

        // Captured Pictures
        picFile = (imageFileName == null) ? new File(this.getCacheDir(), "ppic-cropped.jpg")
                : new File(storageDir + File.separator
                + imageFileName.substring(0, imageFileName.lastIndexOf("."))
                + "-cropped" + imageFileName.substring(imageFileName.lastIndexOf("."), imageFileName.length()));
        output = Uri.fromFile(picFile);
        Crop.of(uri, output).start(this);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE: //crop image that has been captured
                if (data != null) {
                    File file = new File(storageDir + File.separator + imageFileName);
                    try {
                        cropCapturedImage(Uri.fromFile(file));
                    } catch (ActivityNotFoundException aNFE) {
                        aNFE.printStackTrace();
                    }
                } else {
                    Log.d("Alex", "data is null!!!!");
                }
                break;
            case Crop.REQUEST_CROP:
                if (data != null) {
                    try {

                        Bitmap toCompress = BitmapFactory.decodeStream(new FileInputStream(picFile));
                        FileOutputStream stream = new FileOutputStream(picFile);
                        Log.d("MainActivity", "w - " + String.valueOf(toCompress.getWidth()));
                        Log.d("MainActivity", "h - " + String.valueOf(toCompress.getHeight()));
                        if (toCompress.getWidth() >= toCompress.getHeight()) {
                            if (toCompress.getWidth() >= 512) {
                                float ratio = (float) toCompress.getWidth() / toCompress.getHeight();
                                Log.d("MainActivity", "ratio = " + String.valueOf(512 / ratio));
                                toCompress = Bitmap.createScaledBitmap(toCompress, 512, (int) (512 / ratio), true);
                            }
                        } else {
                            if (toCompress.getHeight() >= 512) {
                                float ratio = (float) toCompress.getHeight() / toCompress.getWidth();
                                Log.d("MainActivity", "ratio = " + String.valueOf(512 / ratio));
                                toCompress = Bitmap.createScaledBitmap(toCompress, (int) (512 / ratio), 512, true);
                            }
                        }
                        Log.d("MainActivity", "w - " + String.valueOf(toCompress.getWidth()));
                        Log.d("MainActivity", "h - " + String.valueOf(toCompress.getHeight()));
                        toCompress.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        uploadPic();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void uploadPic() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("secret", Key.SECRET);
        Pair<Double, Double> latLon = updateLocation();
        if (latLon != null) {
            params.put("lat", String.valueOf(latLon.first));
            params.put("lon", String.valueOf(latLon.second));
            try {
                String key = new PostTask(S.SITE_ROOT + "upload_pic.php", this, params).execute().get();
                new AWSUploadTask().execute(key);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets user's location and updates the corresponding EditText
     */
    private Pair<Double, Double> updateLocation() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkLocationEnabled(lm)) {
            return updateLocationOneTime(lm);
        }
        return null;
    }

    /**
     * Checks if the location settings are enabled on the user's device and prompts him to change
     * them if not. If so, it updates his location.
     * Called from updateLocation()
     *
     * @param lm mandatory LocationManager
     */
    private boolean checkLocationEnabled(LocationManager lm) {
        boolean network = false;
        boolean gps = false;
        try {
            gps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            network = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!gps && !network) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage(getResources().getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton(this.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                }
            });
            dialog.show();
        } else {
            return true;
        }
        return false;
    }

    @SuppressWarnings("ResourceType")
    /**
     * Updates location
     */
    private Pair<Double, Double> updateLocationOneTime(LocationManager lm) {
        double lat = -1, lon = -1;
        lm.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
        Location loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (loc != null) {
            lat = loc.getLatitude();
            lon = loc.getLongitude();
            Log.d("MainActivity", "lat: " + lat);
            Log.d("MainActivity", "lon: " + lon);
        }

        lat = lat == -1 ? -1 : Math.toRadians(lat);
        lon = lon == -1 ? -1 : Math.toRadians(lon);

        return Pair.create(lat, lon);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    /**
     * Uploads a picture
     */
    private class AWSUploadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            PutObjectRequest por = new PutObjectRequest(
                    "lit-images", params[0], picFile);
            s3Client.putObject(por);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements
            SwipeRefreshLayout.OnRefreshListener,
            LocationListener {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        private int page = 0;
        private boolean loading = false;
        private SwipeRefreshLayout swipeRefreshLayout;
        private RecyclerView recList;

        @Override
        public void onRefresh() {
            new DownloadPostsTask(true).execute();
        }

        /**
         * Gets user's location and updates the corresponding EditText
         */
        private Pair<Double, Double> updateLocation() {
            LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            if (checkLocationEnabled(lm)) {
                return updateLocationOneTime(lm);
            }
            return null;
        }

        /**
         * Checks if the location settings are enabled on the user's device and prompts him to change
         * them if not. If so, it updates his location.
         * Called from updateLocation()
         *
         * @param lm mandatory LocationManager
         */
        private boolean checkLocationEnabled(LocationManager lm) {
            boolean network = false;
            boolean gps = false;
            try {
                gps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                network = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (!gps && !network) {
                // notify user
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setMessage(getResources().getString(R.string.gps_network_not_enabled));
                dialog.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                        //get gps
                    }
                });
                dialog.setNegativeButton(this.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
                dialog.show();
            } else {
                return true;
            }
            return false;
        }

        @SuppressWarnings("ResourceType")
        /**
         * Updates location
         */
        private Pair<Double, Double> updateLocationOneTime(LocationManager lm) {
            double lat = -1, lon = -1;
            lm.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
            Location loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (loc != null) {
                lat = loc.getLatitude();
                lon = loc.getLongitude();
                Log.d("MainActivity", "lat: " + lat);
                Log.d("MainActivity", "lon: " + lon);
            }

            lat = lat == -1 ? -1 : Math.toRadians(lat);
            lon = lon == -1 ? -1 : Math.toRadians(lon);

            return Pair.create(lat, lon);
        }

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        public void downloadPosts(boolean refreshing) {
            new DownloadPostsTask(refreshing).execute();
        }

        class DownloadPostsTask extends PostTask {

            private boolean isRefreshing = false;

            public DownloadPostsTask(boolean isRefreshing) {
                super(S.SITE_ROOT + "download_images.php", getActivity());
                this.isRefreshing = isRefreshing;
                params.put("secret", "8f98d2eaf16a86d28178ca782cf4fe73b64cd318c4d03b2b9d325088c20e8eb8");
                Pair<Double, Double> latLon = updateLocation();
                if (latLon != null) {
                    params.put("lat", String.valueOf(latLon.first));
                    params.put("lon", String.valueOf(latLon.second));
                }
            }

            public DownloadPostsTask(Map<String, String> params) {
                super(S.SITE_ROOT + "download_images.php");

            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (!isRefreshing) {
                    page++;
                }
                ArrayList<Post> posts = new ArrayList<>();
                try {
                    JSONArray jsonPosts = new JSONArray(s);
                    for (int i = 0; i < jsonPosts.length(); i++) {
                        JSONArray jsonPost = jsonPosts.getJSONArray(i);
                        String key = (String) jsonPost.get(1);
                        posts.add(new Post(Timestamp.valueOf((String) jsonPost.get(0)).getTime(), Integer.parseInt((String) jsonPost.get(2)), Integer.parseInt((String) jsonPost.get(1))));
                    }
                    ((PostAdapter) recList.getAdapter()).setData(posts);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                loading = false;
                swipeRefreshLayout.setRefreshing(false);
            }
        }

        public void loadNewPosts(ArrayList<Post> info) {
            int[] toExclude = new int[info.size()];
            for (int i = 0; i < info.size(); i++) {
                toExclude[i] = info.get(i).imageId;
            }
            JSONArray jsonExclude = new JSONArray(Arrays.asList(toExclude));
            Map<String, String> params = new LinkedHashMap<>();
            params.put("excluded", jsonExclude.toString());
            new DownloadPostsTask(false, params).execute();
        }

        public void loadLitPosts(ArrayList<Post> info) {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);

            swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
            swipeRefreshLayout.setOnRefreshListener(this);

            recList = (RecyclerView) rootView.findViewById(R.id.cardList);
            LinearLayoutManager llm = new LinearLayoutManager(getActivity());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            recList.setLayoutManager(llm);

            Log.d("Alex", "Section Number: " + sectionNumber);

            if (sectionNumber < 3) {
                ArrayList<Post> info = new ArrayList<Post>();

                if (sectionNumber == 1) {
                    loadNewPosts(info);
                } else {
                    loadLitPosts(info);
                }

                recList.setAdapter(new PostAdapter(info));
            } else {
//                ArrayList<LitLocation> locs = new ArrayList<>();
//
//                ArrayList<Post> posts = new ArrayList<Post>();
//                loadLitPosts(posts);
//
//                LitLocation l1 = new LitLocation("321 Lester", 432, posts);
//                locs.add(l1);
//
//                posts = new ArrayList<>();
//
//                Post p1 = new Post(System.currentTimeMillis(), 34, R.drawable.dog, new ArrayList<Comment>());
//                Post p2 = new Post(System.currentTimeMillis(), 57, R.drawable.cat, new ArrayList<Comment>());
//
//                posts.add(p1);
//                posts.add(p2);
//
//                LitLocation l2 = new LitLocation("Zoo?", 231, posts);
//                locs.add(l2);
//
//
//                posts = new ArrayList<>();
//                Post p3 = new Post(System.currentTimeMillis(), 64, R.drawable.santa, new ArrayList<Comment>());
//
//                posts.add(p1);
//                posts.add(p2);
//                posts.add(p3);
//
//                LitLocation l3 = new LitLocation("North Pole", 75, posts);
//                locs.add(l3);
//
//                recList.setAdapter(new LocationAdapter(locs));
            }

            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "NEW";
                case 1:
                    return "LIT";
                case 2:
                    return "EVENTS";
            }
            return null;
        }
    }
}
