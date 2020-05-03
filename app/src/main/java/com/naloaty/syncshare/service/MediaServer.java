package com.naloaty.syncshare.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;

import com.google.gson.Gson;
import com.naloaty.syncshare.communication.SimpleServerResponse;
import com.naloaty.syncshare.config.AppConfig;
import com.naloaty.syncshare.config.MediaServerKeyword;
import com.naloaty.syncshare.database.device.SSDevice;
import com.naloaty.syncshare.database.device.SSDeviceRepository;
import com.naloaty.syncshare.database.media.Album;
import com.naloaty.syncshare.media.MediaObject;
import com.naloaty.syncshare.media.MediaProvider;
import com.naloaty.syncshare.security.CustomServerSocketFactory;
import com.naloaty.syncshare.security.SecurityManager;
import com.naloaty.syncshare.security.SecurityUtils;
import com.naloaty.syncshare.util.AppUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.SimpleWebServer;

public class MediaServer extends SimpleWebServer {

    private static final String TAG = "MediaServer";

    private Context mContext;

    public MediaServer(Context context) {
        super(null, AppConfig.MEDIA_SERVER_PORT, new File("/sdcard/"), true);

        mContext = context;

        SSLContext sslContext = SecurityUtils.getSSLContext(new SecurityManager(context), context.getFilesDir());

        if (sslContext != null) {
            SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
            setServerSocketFactory(new CustomServerSocketFactory(factory, null));
        }
        else
            Log.w(TAG, "Cannot start media server in secure mode");
    }

    @Override
    public Response serve(IHTTPSession session) {

        Log.d(TAG, "Serve URI: " + session.getUri());
        Log.d(TAG, "Serve HEADERS: " + session.getHeaders());
        Log.d(TAG, "Serve PARAMS: " + session.getParameters());

        Log.d(TAG, "Hostname: " + session.getRemoteHostName() + " IP: " + session.getRemoteIpAddress());

        Map<String, String> map = new HashMap<String, String>();
        Method method = session.getMethod();

        if (Method.PUT.equals(method) || Method.POST.equals(method)) {
            try
            {
                session.parseBody(map);
            }
            catch (IOException e)
            {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + e.getMessage());
            }
            catch (ResponseException e)
            {
                return newFixedLengthResponse(e.getStatus(), MIME_PLAINTEXT, e.getMessage());
            }
        }

        if (session.getMethod().equals(Method.GET))
            return defaultGETRespond(Collections.unmodifiableMap(session.getHeaders()), session, session.getUri());
        else
            return defaultPOSTRespond(Collections.unmodifiableMap(map), session, session.getUri());
    }

    private Response defaultPOSTRespond(Map<String, String> postBody, IHTTPSession session, String uri) {

        // Remove URL arguments
        uri = uri.trim().replace(File.separatorChar, '/');
        uri = uri.substring(1);
        if (uri.indexOf('?') >= 0) {
            uri = uri.substring(0, uri.indexOf('?'));
        }

        Log.d(TAG, "Parsing request: " + uri);
        String[] request = uri.split("/");

        if (request.length < 2){
            Log.w(TAG, "Bad request from remote device");
            return getBadRequestResponse();
        }


        switch (request[0]) {
            case MediaServerKeyword.REQUEST_TARGET_DEVICE:
                if (request.length > 2)
                    return getBadRequestResponse();

                return devicePOSTRespond(request[1], postBody.get("postData").toString());

            default:
                return getBadRequestResponse();
        }
    }

    private Response devicePOSTRespond(String request, String postParams) {

        if (postParams == null)
            return getBadRequestResponse();

        switch (request) {
            case MediaServerKeyword.REQUEST_INFORMATION:
                Gson converter = new Gson();
                SSDevice ssDevice = converter.fromJson(postParams, SSDevice.class);
                ssDevice.setAccessAllowed(true);

                SSDeviceRepository repository = new SSDeviceRepository(mContext);
                repository.publish(ssDevice);

                SimpleServerResponse resp = new SimpleServerResponse();
                resp.setDescription("Device added");

                return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, converter.toJson(resp));

            default:
                return getNotFoundResponse();
        }
    }


    private Response defaultGETRespond(Map<String, String> headers, IHTTPSession session, String uri) {
        // Remove URL arguments
        uri = uri.trim().replace(File.separatorChar, '/');
        uri = uri.substring(1);
        if (uri.indexOf('?') >= 0) {
            uri = uri.substring(0, uri.indexOf('?'));
        }

        Log.d(TAG, "Parsing request: " + uri);
        String[] request = uri.split("/");

        if (request.length < 2){
            Log.w(TAG, "Bad request from remote device");
            return getBadRequestResponse();
        }


        switch (request[0]) {
            case MediaServerKeyword.REQUEST_TARGET_DEVICE:

                if (request.length > 2)
                    return getBadRequestResponse();

                return deviceGETRespond(request[1]);

            case MediaServerKeyword.REQUEST_TARGET_MEDIA:
                if (request.length > 3)
                    return getBadRequestResponse();

                return mediaGETRespond(request, headers, session);

            default:
                return getBadRequestResponse();
        }

    }

    private Response deviceGETRespond(String request) {

        switch (request) {
            case MediaServerKeyword.REQUEST_INFORMATION:
                SSDevice myDevice = AppUtils.getLocalDevice(mContext);
                Gson converter = new Gson();
                String json = converter.toJson(myDevice);

                return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, json);

            default:
                return getNotFoundResponse();
        }
    }

    private Response mediaGETRespond(String[] request, Map<String, String> headers, IHTTPSession session) {

        switch (request[1]) {
            case MediaServerKeyword.REQUEST_ALBUMS:

                try
                {
                    List<Album> albums = MediaProvider.getSharedAlbums(mContext);

                    Gson converter = new Gson();
                    String json = converter.toJson(albums);

                    Log.d(TAG, "Albums fetched with success. Items count is " + albums.size());
                    Log.d(TAG, "Response is " + json);

                    return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, json);
                }
                catch (Exception e) {
                    Log.w(TAG, "Cannot serve albums list: " + e);
                    return getInternalErrorResponse();
                }

            case MediaServerKeyword.REQUEST_MEDIA_LIST:
                Map<String, List<String>> parameters = session.getParameters();

                if (!parameters.containsKey(MediaServerKeyword.GET_ALBUM_ID))
                    return getBadRequestResponse();

                String albumId = parameters.get(MediaServerKeyword.GET_ALBUM_ID).get(0);
                //TODO: get media list by album id

                return getInternalErrorResponse();

            case MediaServerKeyword.REQUEST_THUMBNAIL:
                try {
                    MediaObject mediaObject = MediaProvider.getMediaObjectById(mContext, request[2]);

                    if (mediaObject.isVideo()) {
                        Log.d(TAG, "Serving thumbnail");
                        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(mediaObject.getPath(), MediaStore.Images.Thumbnails.MINI_KIND);
                        return returnThumbnailResponse(thumb);
                    }


                    Log.d(TAG, "serving file " + request[2] + " by path " + mediaObject.getPath());
                    return serveFile(headers, new File(mediaObject.getPath()), getMimeTypeForFile(mediaObject.getPath()));
                }
                catch (Exception e) {
                    Log.d(TAG, "Cannot serve file " + request[2] + " because " + e);
                    return getInternalErrorResponse();
                }

            default:
                return getNotFoundResponse();
        }
    }

    Response serveFile(Map<String, String> header, File file, String mime) {
        Response res;
        try {
            // Calculate etag
            String etag = Integer.toHexString((file.getAbsolutePath() + file.lastModified() + "" + file.length()).hashCode());

            // Support (simple) skipping:
            long startFrom = 0;
            long endAt = -1;
            String range = header.get("range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range.substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            // get if-range header. If present, it must match etag or else we
            // should ignore the range request
            String ifRange = header.get("if-range");
            boolean headerIfRangeMissingOrMatching = (ifRange == null || etag.equals(ifRange));

            String ifNoneMatch = header.get("if-none-match");
            boolean headerIfNoneMatchPresentAndMatching = ifNoneMatch != null && ("*".equals(ifNoneMatch) || ifNoneMatch.equals(etag));

            // Change return code and add Content-Range header when skipping is
            // requested
            long fileLen = file.length();

            if (headerIfRangeMissingOrMatching && range != null && startFrom >= 0 && startFrom < fileLen) {
                // range request that matches current etag
                // and the startFrom of the range is satisfiable
                if (headerIfNoneMatchPresentAndMatching) {
                    // range request that matches current etag
                    // and the startFrom of the range is satisfiable
                    // would return range from file
                    // respond with not-modified
                    res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1;
                    }
                    long newLen = endAt - startFrom + 1;
                    if (newLen < 0) {
                        newLen = 0;
                    }

                    FileInputStream fis = new FileInputStream(file);
                    fis.skip(startFrom);

                    res = newFixedLengthResponse(Response.Status.PARTIAL_CONTENT, mime, fis, newLen);
                    res.addHeader("Accept-Ranges", "bytes");
                    res.addHeader("Content-Length", "" + newLen);
                    res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                    res.addHeader("ETag", etag);
                }
            } else {

                if (headerIfRangeMissingOrMatching && range != null && startFrom >= fileLen) {
                    // return the size of the file
                    // 4xx responses are not trumped by if-none-match
                    res = newFixedLengthResponse(Response.Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "");
                    res.addHeader("Content-Range", "bytes */" + fileLen);
                    res.addHeader("ETag", etag);
                } else if (range == null && headerIfNoneMatchPresentAndMatching) {
                    // full-file-fetch request
                    // would return entire file
                    // respond with not-modified
                    res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else if (!headerIfRangeMissingOrMatching && headerIfNoneMatchPresentAndMatching) {
                    // range request that doesn't match current etag
                    // would return entire (different) file
                    // respond with not-modified

                    res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else {
                    // supply the file
                    res = newFixedFileResponse(file, mime);
                    res.addHeader("Content-Length", "" + fileLen);
                    res.addHeader("ETag", etag);
                }
            }
        } catch (IOException ioe) {
            res = getForbiddenResponse("Reading file failed.");
        }

        return res;
    }

    private Response returnThumbnailResponse(Bitmap bitmap) throws IOException {
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, 160, 230);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapData = bos.toByteArray();
        bos.close();

        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapData);
        return createResponse(Response.Status.OK, MIME_TYPES.get("png"), bs);
    }

    private Response createResponse(Response.Status status, String mimeType, InputStream message) {
        Response res = newChunkedResponse(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    private Response newFixedFileResponse(File file, String mime) throws FileNotFoundException {
        Response res;
        res = newFixedLengthResponse(Response.Status.OK, mime, new FileInputStream(file), (int) file.length());
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    private Response getBadRequestResponse() {
        return newFixedLengthResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT,
                "Error 400, request doesn't match SyncShare requirements");
    }

    private Response getForbiddenResponse() {
        return newFixedLengthResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT,
                "Error 403, access to media not allowed by user");
    }

    private Response getInternalErrorResponse() {
        return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                "Error 500, request execution error");
    }

    protected Response getNotFoundResponse() {
        return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Error 404, requested resource not found");
    }
}
