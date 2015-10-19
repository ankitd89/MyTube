package com.adevani.helper;

import android.content.Context;
import android.util.Log;

import com.adevani.model.Video;
import com.adevani.mytube.MainActivity;
import com.adevani.mytube.R;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.common.base.Joiner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ankitdevani on 10/17/15.
 */
public class YoutubeConnector {

    public static final String TAG = YoutubeConnector.class.getSimpleName();
    private YouTube youtube;
    private YouTube.Search.List query;
    public static final String KEY = "AIzaSyD7DB-rO5WHOHPj0EfxT8qtB-SYf3udbG8";

    public YoutubeConnector(Context context) {

        youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest hr) throws IOException {}
        }).setApplicationName(context.getString(R.string.app_name)).build();

        try{
            query = youtube.search().list("id,snippet");
            query.setOauthToken(MainActivity.FETCH_TOKEN);
            query.setType("video");
            query.setFields("items(id/videoId,snippet/title,snippet/publishedAt,snippet/thumbnails/default/url)");
        }catch(IOException e){
            Log.d(TAG, "Could not initialize: " + e);
        }
    }

    public List<Video> search(String keywords){
        query.setQ(keywords);
        List<String> videoIds = new ArrayList<>();
        List<Video> items = new ArrayList<Video>();
        try{
            SearchListResponse response = query.execute();
            List<SearchResult> results = response.getItems();

            if (results != null) {
                for (SearchResult searchResult : results) {
                    videoIds.add(searchResult.getId().getVideoId());
                }
                Joiner stringJoiner = Joiner.on(',');
                String videoId = stringJoiner.join(videoIds);

                YouTube.Videos.List listVideosRequest = youtube.videos().list("snippet, statistics," +
                        " recordingDetails").setId(videoId);
                listVideosRequest.setOauthToken(MainActivity.FETCH_TOKEN);
                VideoListResponse listResponse = listVideosRequest.execute();
                List<com.google.api.services.youtube.model.Video> videoList = listResponse.getItems();
                if (videoList != null) {
                    for (int i=0; i< videoList.size(); i++) {
                        com.google.api.services.youtube.model.Video retrievedVideo = videoList.get(i);
                        Video item = new Video();
                        item.setTitle(retrievedVideo.getSnippet().getTitle());
                        item.setPublishedDate(retrievedVideo.getSnippet().getPublishedAt());
                        item.setViewCount(retrievedVideo.getStatistics().getViewCount());
                        item.setThumbnailURL(retrievedVideo.getSnippet().getThumbnails().getDefault().getUrl());
                        item.setId(retrievedVideo.getId());
                        items.add(item);
                    }
                }


            }

        }catch(IOException e){
            Log.d(TAG, "Could not search: "+e);

        }
        return items;
    }
}
