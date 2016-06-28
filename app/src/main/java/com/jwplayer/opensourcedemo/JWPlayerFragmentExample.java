package com.jwplayer.opensourcedemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.longtailvideo.jwplayer.JWPlayerSupportFragment;
import com.longtailvideo.jwplayer.JWPlayerView;
import com.longtailvideo.jwplayer.configuration.PlayerConfig;
import com.longtailvideo.jwplayer.media.playlists.PlaylistItem;

import org.json.JSONException;
import org.json.JSONObject;

public class JWPlayerFragmentExample extends AppCompatActivity {

    /**
     * A reference to the {@link JWPlayerSupportFragment}.
     */
    private JWPlayerSupportFragment mPlayerFragment;

    /**
     * A reference to the {@link JWPlayerView} used by the JWPlayerSupportFragment.
     */
    private JWPlayerView mPlayerView;

    /**
     * An instance of our event handling class
     */
    private JWEventHandler mEventHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jwplayerfragment);

        final TextView outputTextView = (TextView)findViewById(R.id.output);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = MyApplication.getPlayerUrl();

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String m3u8Url = "";
                        JSONObject mainResponseObject = null;
                        try {
                            mainResponseObject = new JSONObject(response.toString());
                            m3u8Url = mainResponseObject.getString("url");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Construct a new JWPlayerSupportFragment (since we're using AppCompatActivity)
                        mPlayerFragment = JWPlayerSupportFragment.newInstance(new PlayerConfig.Builder()
                                .file(m3u8Url)
                                .build());

                        // Attach the Fragment to our layout
                        FragmentManager fm = getSupportFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        ft.add(R.id.fragment_container, mPlayerFragment);
                        ft.commit();

                        // Make sure all the pending fragment transactions have been completed, otherwise
                        // mPlayerFragment.getPlayer() may return null
                        fm.executePendingTransactions();

                        // Get a reference to the JWPlayerView from the fragment
                        mPlayerView = mPlayerFragment.getPlayer();

                        // Keep the screen on during playback
                        new KeepScreenOnHandler(mPlayerView, getWindow());

                        // Instantiate the JW Player event handler class
                        mEventHandler = new JWEventHandler(mPlayerView, outputTextView);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("VIDEO_URL_GETTER", "Error with the request.");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Exit fullscreen when the user pressed the Back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mPlayerView.getFullscreen()) {
                mPlayerView.setFullscreen(false, true);
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_jwplayerfragment, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switch_to_view:
                Intent i = new Intent(this, JWPlayerViewExample.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
