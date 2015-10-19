package com.adevani.mytube;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.adevani.helper.PlaylistRequests;
import com.adevani.model.PlaylistVideo;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class FavouriteFragment extends Fragment implements ActionMode.Callback{

    public static final String TAG = FavouriteFragment.class.getSimpleName();
    private List<PlaylistVideo> playlistVideosList;
    private HashMap<String, PlaylistVideo> playListVideoIdToBeDeletedList = new HashMap<>();
    private ListView favouriteVideos;
    private Handler handler;
    ProgressDialog mDialog;
    ArrayAdapter<PlaylistVideo> adapter;
    protected Object mActionMode;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favourite, container, false);
        favouriteVideos = (ListView) rootView.findViewById(R.id.favourite_videos);
        favouriteVideos.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        mDialog = new ProgressDialog(getActivity());
        mDialog.setMessage("Loading...");
        mDialog.setCancelable(false);

        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);

        handler = new Handler();
        Log.d(TAG, "Fetch Playlist Item");
        getPlayListItem(savedInstanceState);
        addClickListener();

        return rootView;
    }

    private void addClickListener() {
        favouriteVideos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
                Intent intent = new Intent(getActivity().getApplicationContext(), PlayerActivity.class);
                intent.putExtra("VIDEO_ID", playlistVideosList.get(pos).getId());
                startActivity(intent);
            }
        });
    }

    public void getPlayListItem(final Bundle savedInstanceState) {
        mDialog.show();
        new Thread() {
            @Override
            public void run() {

                if (Strings.isNullOrEmpty(PlaylistRequests.PLAYLIST_ID)){
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                playlistVideosList = PlaylistRequests.listPlaylistItem(getActivity().getApplicationContext());
                if (playlistVideosList != null) {
                    handler.post(new Runnable() {
                        public void run() {
                            updateFavouriteVideos(savedInstanceState);
                        }
                    });
                }
            }
        }.start();
    }

    private void updateFavouriteVideos(final Bundle savedInstanceState) {
        mDialog.dismiss();
        adapter = new ArrayAdapter<PlaylistVideo>(getActivity().getApplicationContext(),
                R.layout.row_video_playlist, playlistVideosList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater(savedInstanceState).inflate(R.layout.row_video_playlist, parent, false);
                }
                ImageView thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
                TextView title = (TextView) convertView.findViewById(R.id.title);
                TextView publishedDate = (TextView) convertView.findViewById(R.id.published_date);
                TextView viewCount = (TextView) convertView.findViewById(R.id.views);
                CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);

                //checkBox.setChecked(false);   // Initialize
                checkBox.setTag(position);
                checkBox.setFocusable(false); // To make the whole row selectable
                final PlaylistVideo favouriteResult = playlistVideosList.get(position);
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        String tag = buttonView.getTag().toString();
                        String[] pos = tag.split(",");
                        if (isChecked) {
                            if (mActionMode == null)
                                mActionMode = getActivity().startActionMode(FavouriteFragment.this);
                            playListVideoIdToBeDeletedList.put(favouriteResult.getPlaylistItemVideoId(),favouriteResult);
                        }
                        else {
                            if (mActionMode != null) {  // Only operate when mActionMode is available
                                //mActionMode.finish();
                                playListVideoIdToBeDeletedList.remove(favouriteResult.getPlaylistItemVideoId());
                                mActionMode = null;
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });

                //PlaylistVideo favouriteResult = playlistVideosList.get(position);

                Picasso.with(getActivity().getApplicationContext()).load(favouriteResult.getThumbnailURL()).into(thumbnail);
                title.setText(favouriteResult.getTitle());
                Long dt = favouriteResult.getPublishedDate().getValue();
                Date date = new Date(dt);
                publishedDate.setText(date.toString());
                if(favouriteResult.getViewCount() != null)
                    viewCount.setText(favouriteResult.getViewCount().toString());
                return convertView;
            }
        };

        favouriteVideos.setAdapter(adapter);

    }

    public void deleteSelectedVideos() {
        Log.i(TAG, "Delete Clicked");
        SparseBooleanArray checked = favouriteVideos.getCheckedItemPositions();
        for (int i=0; i<checked.size(); i++) {
            if (checked.valueAt(i)) {
                PlaylistVideo selectedPlaylistVideo = (PlaylistVideo) favouriteVideos.getItemAtPosition(checked.keyAt(i));
                Log.i(TAG, selectedPlaylistVideo.getPlaylistItemVideoId());

            }
        }
    }


    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        Log.d(TAG, "creating action mode");
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        menu.findItem(R.id.action_delete).setVisible(true);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        Log.i(TAG, "Preparing the action menu");

        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_delete:
                Log.i(TAG, "Delete button clicked");
                mDialog.setTitle("Deleting Videos from Playlist");
                mDialog.show();
                new Thread() {
                    @Override
                    public void run() {
                        for (String key : playListVideoIdToBeDeletedList.keySet()) {
                            PlaylistRequests.deletePlaylistVideo(getActivity().getApplicationContext(), key);
                            Log.d(TAG, key + "adap dele");
                            for (int i=0; i<playlistVideosList.size(); i++) {
                                Log.d(TAG, playlistVideosList.get(i).getPlaylistItemVideoId() + "adap");
                                if(playlistVideosList.get(i).getPlaylistItemVideoId().equalsIgnoreCase(key)) {
                                    playlistVideosList.remove(i);
                                }
                            }
                        }

                        playListVideoIdToBeDeletedList.clear();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mDialog.dismiss();
                                Log.d(TAG, playlistVideosList.size()+"");
                                adapter.notifyDataSetChanged();
                                favouriteVideos.setAdapter(adapter);
                            }
                        });
                    }
                }.start();

               /* mDialog.dismiss();
                Log.d(TAG, playlistVideosList.size()+"");
                adapter.notifyDataSetChanged();
                favouriteVideos.setAdapter(adapter);
                mode.finish();*/
                mode.finish();
                return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        Log.i(TAG, "On Destroying Action Mode");

    }


}