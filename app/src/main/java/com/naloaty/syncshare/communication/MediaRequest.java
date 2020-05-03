package com.naloaty.syncshare.communication;

import com.naloaty.syncshare.config.MediaServerKeyword;
import com.naloaty.syncshare.database.device.SSDevice;
import com.naloaty.syncshare.database.media.Album;
import com.naloaty.syncshare.media.Media;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MediaRequest {

    /*
     * device/information
     */
    @GET(MediaServerKeyword.REQUEST_TARGET_MEDIA + "/" + MediaServerKeyword.REQUEST_ALBUMS)
    Call<List<Album>> getAlbumsList();


    /*
     * media/medialist?albumId=***
     */
    @GET(MediaServerKeyword.REQUEST_TARGET_MEDIA + "/" + MediaServerKeyword.REQUEST_MEDIA_LIST)
    Call<List<Media>> getMediaList(@Query(MediaServerKeyword.GET_ALBUM_ID) String albumId);
}
