package com.adevani.mytube;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.adevani.helper.PlaylistRequests;
import com.adevani.helper.YoutubeConnector;
import com.adevani.model.PlaylistVideo;
import com.adevani.model.Video;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Date;
import java.util.List;


public class SearchFragment extends Fragment {
    private static final String TAG = SearchFragment.class.getSimpleName();
    private EditText searchInput;
    private ListView videosFound;
    private List<Video> searchResults;
    private List<PlaylistVideo> playlistVideosList;
    ProgressDialog mDialog;
    private Handler handler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        searchInput = (EditText)rootView.findViewById(R.id.search_input);
        videosFound = (ListView)rootView.findViewById(R.id.videos_found);

        handler = new Handler();

        /*searchInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (searchInput.getRight() -
                            searchInput.getCompoundDrawables()
                                    [DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        hideKeyBoard();
                        searchYoutube(searchInput.getText().toString());
                        return true;
                    }
                }
                return false;
            }
        });*/

        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    searchYoutube(v.getText().toString(), savedInstanceState);
                    return false;
                }
                return true;
            }
        });
        addClickListener();
        return rootView;
    }

    private void searchYoutube(final String keywords, final Bundle savedInstanceState) {
        mDialog = new ProgressDialog(getActivity());
        mDialog.setMessage("Searching for Videos");
        mDialog.setCancelable(false);
        mDialog.show();
        new Thread(){
            public void run(){
                PlaylistRequests.listAllPlaylist(getActivity().getApplicationContext());
                YoutubeConnector yc = new YoutubeConnector(getActivity());
                searchResults = yc.search(keywords);
                playlistVideosList = PlaylistRequests.listPlaylistItem(getActivity().getApplicationContext());
                handler.post(new Runnable(){
                    public void run(){
                        updateVideosFound(savedInstanceState);

                    }
                });
            }
        }.start();
    }

    private void updateVideosFound(final Bundle savedInstanceState) {
        mDialog.dismiss();
        ArrayAdapter<Video> adapter = new ArrayAdapter<Video>(getActivity().getApplicationContext(),
                R.layout.row_video, searchResults){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getLayoutInflater(savedInstanceState).inflate(R.layout.row_video, parent, false);
                }
                ImageView thumbnail = (ImageView)convertView.findViewById(R.id.video_image);
                TextView title = (TextView)convertView.findViewById(R.id.video_title);
                TextView publishedDate = (TextView)convertView.findViewById(R.id.video_publishedDate);
                TextView viewCount = (TextView)convertView.findViewById(R.id.video_viewCount);
                Video searchResult = searchResults.get(position);

                final ImageButton imageButton = (ImageButton) convertView.findViewById(R.id.starImage);
                imageButton.setFocusable(false);
                imageButton.setTag(new Boolean(false));
                imageButton.setOnClickListener(new ImageButton.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        if(!(Boolean)imageButton.getTag()) {
                            insertVideoToPlayList(v);
                            imageButton.setImageResource(R.drawable.star_pressed);
                            imageButton.setTag(new Boolean(true));
                        } else {
                            removeVideoFromPlaylist(v);
                            imageButton.setImageResource(R.drawable.star_normal);
                            imageButton.setTag(new Boolean(false));
                        }
                    }
                });
                Picasso.with(getActivity().getApplicationContext()).load(searchResult.getThumbnailURL()).into(thumbnail);
                title.setText(searchResult.getTitle());
                Long dt = searchResult.getPublishedDate().getValue();
                Date date = new Date(dt);
                publishedDate.setText(date.toString());
                viewCount.setText(searchResult.getViewCount().toString());
                if(blnVideoInPlaylist(searchResult.getId())) {
                    imageButton.setImageResource(R.drawable.star_pressed);
                    imageButton.setTag(new Boolean(true));
                } else {
                    imageButton.setImageResource(R.drawable.star_normal);
                    imageButton.setTag(new Boolean(false));
                }
                return convertView;
            }
        };

        videosFound.setAdapter(adapter);
    }

    private void addClickListener(){
        videosFound.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos, long id) {

                Intent intent = new Intent(getActivity().getApplicationContext(), PlayerActivity.class);
                intent.putExtra("VIDEO_ID", searchResults.get(pos).getId());
                startActivity(intent);
            }

        });
    }

    public void hideKeyBoard() {
        InputMethodManager inputManager = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public boolean blnVideoInPlaylist(String videoId) {
        if(playlistVideosList != null) {
            for (PlaylistVideo playlistVideo : playlistVideosList) {
                if(playlistVideo.getId().equalsIgnoreCase(videoId)) {
                    return true;
                }
            }
        }

        return false;
    }

    private String getPlaylistVideoId(String videoId) {
        for (PlaylistVideo playlistVideo : playlistVideosList) {
            Log.d(TAG, "in FOR " + playlistVideo.getId());
            if(playlistVideo.getId().equalsIgnoreCase(videoId)) {
                Log.d(TAG, "in If " + playlistVideo.getPlaylistItemVideoId());
                return playlistVideo.getPlaylistItemVideoId();
            }
        }
        return null;
    }

    private int getPlaylistVideoPosition(String videoId) {
        for (int i = 0; i< playlistVideosList.size(); i++) {
            PlaylistVideo playlistVideo = playlistVideosList.get(i);
            if(playlistVideo.getId().equalsIgnoreCase(videoId)) {
                return i;
            }
        }
        return 0;
    }

    public void insertVideoToPlayList(final View v) {
        new Thread() {
            public void run() {
                try {
                    View parentRow = (View) v.getParent();
                    ListView listView = (ListView) parentRow.getParent();
                    final int position = listView.getPositionForView(parentRow);
                    final String videoId = searchResults.get(position).getId();
                    Log.d(TAG, "PlaylistId = " + PlaylistRequests.PLAYLIST_ID + " Video Id" + videoId);
                    PlaylistVideo playlistVideo = PlaylistRequests.insertPlaylistItem(PlaylistRequests.PLAYLIST_ID,
                            videoId, getActivity().getApplicationContext());
                    playlistVideosList.add(playlistVideo);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void removeVideoFromPlaylist(final View v) {
        new Thread() {
            public void run() {
                try {
                    View parentRow = (View) v.getParent();
                    ListView listView = (ListView) parentRow.getParent();
                    final int position = listView.getPositionForView(parentRow);
                    final String videoId = searchResults.get(position).getId();
                    final String playListVideoId = getPlaylistVideoId(videoId);
                    Log.d(TAG, "PlaylistId = " + PlaylistRequests.PLAYLIST_ID + " Video Id" + playListVideoId);
                    if(!Strings.isNullOrEmpty(playListVideoId)) {
                        PlaylistRequests.deletePlaylistVideo(getActivity().getApplicationContext(), playListVideoId);
                        playlistVideosList.remove(getPlaylistVideoPosition(videoId));
                    } else {
                        Log.e(TAG, "PlaylistVideoId is null or empty cannot delete");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

}
