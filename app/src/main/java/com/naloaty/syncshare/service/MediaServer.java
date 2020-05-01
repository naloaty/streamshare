package com.naloaty.syncshare.service;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.naloaty.syncshare.communication.SimpleServerResponse;
import com.naloaty.syncshare.config.AppConfig;
import com.naloaty.syncshare.config.MediaServerKeyword;
import com.naloaty.syncshare.database.SSDevice;
import com.naloaty.syncshare.database.SSDeviceRepository;
import com.naloaty.syncshare.security.CustomServerSocketFactory;
import com.naloaty.syncshare.security.SecurityManager;
import com.naloaty.syncshare.security.SecurityUtils;
import com.naloaty.syncshare.util.AppUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
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
        /*Log.d(TAG, "Serve HEADERS: " + session.getHeaders());
        Log.d(TAG, "Serve PARAMS: " + session.getParameters());*/

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
                //TODO: here it is respond requested media
                break;

            default:
                return getBadRequestResponse();
        }

        return getBadRequestResponse();
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

    private Response getBadRequestResponse() {
        return newFixedLengthResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT,
                "Error 400, request doesn't match SyncShare requirements");
    }

    protected Response getNotFoundResponse() {
        return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Error 404, requested resource not found");
    }
}
