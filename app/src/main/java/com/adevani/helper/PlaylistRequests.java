package com.adevani.helper;

import android.content.Context;
import android.util.Log;

import com.adevani.model.PlaylistVideo;
import com.adevani.mytube.MainActivity;
import com.adevani.mytube.R;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.common.base.Joiner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ankitdevani on 10/17/15.
 */
public class PlaylistRequests {
    public static final String TAG = PlaylistRequests.class.getSimpleName();
    public static final String PLAYLIST_TITLE = "SJSU-CMPE-277";
    public static String PLAYLIST_ID;
    public static final String PLAYLIST_URL = "https://www.googleapis.com/youtube/v3/playlists?";
    public static void listAllPlaylist(final Context context) {
        String token = MainActivity.FETCH_TOKEN;
        String url = String.format(PLAYLIST_URL +"part=%s&mine=%b&access_token=%s","id,snippet",true,token);
        Log.d(TAG, "LIST URL-" + url);
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject itemsObject;
                            JSONObject snippetobject;
                            String title;
                            JSONArray jsonArray = response.getJSONArray("items");
                            for (int i = 0; i<jsonArray.length(); i++ ) {
                                itemsObject = (JSONObject) jsonArray.get(i);
                                snippetobject = itemsObject.getJSONObject("snippet");
                                title = snippetobject.getString("title");
                                Log.d(TAG, title);
                                if(title.equalsIgnoreCase(PLAYLIST_TITLE)) {
                                    Log.d(TAG, title);
                                    PLAYLIST_ID = itemsObject.getString("id");
                                    Log.d(TAG, PLAYLIST_ID);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
        );
        Volley.newRequestQueue(context).add(jsonRequest);
    }

    public static PlaylistVideo insertPlaylistItem(String playlistId, String videoId, Context context) throws IOException {
        YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest hr) throws IOException {
            }
        }).setApplicationName(context.getString(R.string.app_name)).build();
        // Define a resourceId that identifies the video being added to the
        // playlist.
        ResourceId resourceId = new ResourceId();
        resourceId.setKind("youtube#video");
        resourceId.setVideoId(videoId);

        // Set fields included in the playlistItem resource's "snippet" part.
        PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
        playlistItemSnippet.setTitle("First video in the test playlist");
        playlistItemSnippet.setPlaylistId(playlistId);
        playlistItemSnippet.setResourceId(resourceId);

        // Create the playlistItem resource and set its snippet to the
        // object created above.
        PlaylistItem playlistItem = new PlaylistItem();
        playlistItem.setSnippet(playlistItemSnippet);

        // Call the API to add the playlist item to the specified playlist.
        // In the API call, the first argument identifies the resource parts
        // that the API response should contain, and the second argument is
        // the playlist item being inserted.
        YouTube.PlaylistItems.Insert playlistItemsInsertCommand =
                youtube.playlistItems().
                        insert("id,snippet,contentDetails", playlistItem).
                        setOauthToken(MainActivity.FETCH_TOKEN);

        PlaylistItem returnedPlaylistItem = playlistItemsInsertCommand.execute();

        // Print data from the API response and return the new playlist
        // item's unique playlistItem ID.

        System.out.println("New PlaylistItem name: " + returnedPlaylistItem.getSnippet().getTitle());
        System.out.println(" - Video id: " + returnedPlaylistItem.getSnippet().getResourceId().getVideoId());
        System.out.println(" - Posted: " + returnedPlaylistItem.getSnippet().getPublishedAt());
        System.out.println(" - Channel: " + returnedPlaylistItem.getSnippet().getChannelId());
        PlaylistVideo playlistVideo = new PlaylistVideo();
        playlistVideo.setPlaylistItemVideoId(returnedPlaylistItem.getId());
        playlistVideo.setTitle(returnedPlaylistItem.getSnippet().getTitle());
        playlistVideo.setPublishedDate(returnedPlaylistItem.getSnippet().getPublishedAt());
        playlistVideo.setId(returnedPlaylistItem.getContentDetails().getVideoId());
        playlistVideo.setThumbnailURL(returnedPlaylistItem.getSnippet().getThumbnails().getDefault().getUrl());
        return playlistVideo;

    }

    public static List<PlaylistVideo> listPlaylistItem(Context context) {
        if(!Strings.isNullOrEmpty(PlaylistRequests.PLAYLIST_ID)) {
            List<PlaylistItem> playlistItemList = new ArrayList<PlaylistItem>();
            YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest hr) throws IOException {
                }
            }).setApplicationName(context.getString(R.string.app_name)).build();

            // Retrieve the playlist of the channel's uploaded videos.
            YouTube.PlaylistItems.List playlistItemRequest = null;
            try {
                playlistItemRequest = youtube.playlistItems().list("id,contentDetails,snippet").setOauthToken(MainActivity.FETCH_TOKEN);
            } catch (IOException e) {
                e.printStackTrace();
            }
            playlistItemRequest.setPlaylistId(PlaylistRequests.PLAYLIST_ID);

            // Only retrieve data used in this application, thereby making
            // the application more efficient. See:
            // https://developers.google.com/youtube/v3/getting-started#partial
            playlistItemRequest.setFields(
                    "items(id,contentDetails/videoId,snippet/title,snippet/publishedAt,snippet/thumbnails/default/url),nextPageToken,pageInfo");

            String nextToken = "";

            // Call the API one or more times to retrieve all items in the
            // list. As long as the API response returns a nextPageToken,
            // there are still more items to retrieve.
            do {
                playlistItemRequest.setPageToken(nextToken);
                PlaylistItemListResponse playlistItemResult = null;
                try {
                    playlistItemResult = playlistItemRequest.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                playlistItemList.addAll(playlistItemResult.getItems());
                nextToken = playlistItemResult.getNextPageToken();
            } while (nextToken != null);

            List<PlaylistVideo> playlistVideoList = new ArrayList<>();
            for (int i = 0; i < playlistItemList.size(); i++) {
                PlaylistItem playlistItem = playlistItemList.get(i);
                PlaylistVideo playlistVideo = new PlaylistVideo();
                playlistVideo.setPlaylistItemVideoId(playlistItem.getId());
                playlistVideo.setTitle(playlistItem.getSnippet().getTitle());
                playlistVideo.setPublishedDate(playlistItem.getSnippet().getPublishedAt());
                playlistVideo.setId(playlistItem.getContentDetails().getVideoId());
                playlistVideo.setThumbnailURL(playlistItem.getSnippet().getThumbnails().getDefault().getUrl());
                playlistVideoList.add(playlistVideo);

            }

            List<String> videoIds = new ArrayList<>();
            //Fetch View Count
            for (PlaylistItem playlistItem : playlistItemList) {
                videoIds.add(playlistItem.getContentDetails().getVideoId());
            }
            Joiner stringJoiner = Joiner.on(',');
            String videoId = stringJoiner.join(videoIds);

            YouTube.Videos.List listVideosRequest = null;
            try {
                listVideosRequest = youtube.videos().list("snippet, statistics," +
                        " recordingDetails").setId(videoId);
                listVideosRequest.setOauthToken(MainActivity.FETCH_TOKEN);
                VideoListResponse listResponse = listVideosRequest.execute();
                List<com.google.api.services.youtube.model.Video> videoList = listResponse.getItems();
                if (videoList != null) {
                    for (int i = 0; i < videoList.size(); i++) {
                        com.google.api.services.youtube.model.Video retrievedVideo = videoList.get(i);
                        for (int j = 0; j < playlistVideoList.size(); j++) {
                            if (playlistVideoList.get(j).getId().equalsIgnoreCase(retrievedVideo.getId())) {
                                playlistVideoList.get(j).setViewCount(retrievedVideo.getStatistics().getViewCount());
                                break;
                            }
                        }
                    }

                }
                Log.d(TAG, playlistItemList.size() + "");
            } catch (IOException e) {
                e.printStackTrace();
            }


            return playlistVideoList;
        }
        return null;
    }

    public static boolean deletePlaylistVideo(Context context, final String videoId) {
        YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest hr) throws IOException {
            }
        }).setApplicationName(context.getString(R.string.app_name)).build();
        YouTube.PlaylistItems.Delete playlistItemRequest;
        try {
            playlistItemRequest = youtube.playlistItems().delete(videoId).setOauthToken(MainActivity.FETCH_TOKEN);
            playlistItemRequest.execute();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }
}
