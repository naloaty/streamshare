package com.naloaty.syncshare.security;

import java.io.IOException;
import java.net.ServerSocket;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import fi.iki.elonen.NanoHTTPD;

public class CustomServerSocketFactory implements NanoHTTPD.ServerSocketFactory {

    private SSLServerSocketFactory sslServerSocketFactory;

    private String[] sslProtocols;

    public CustomServerSocketFactory(SSLServerSocketFactory sslServerSocketFactory, String[] sslProtocols) {
        this.sslServerSocketFactory = sslServerSocketFactory;
        this.sslProtocols = sslProtocols;
    }

    @Override
    public ServerSocket create() throws IOException {
        SSLServerSocket ss = null;
        ss = (SSLServerSocket) this.sslServerSocketFactory.createServerSocket();
        if (this.sslProtocols != null) {
            ss.setEnabledProtocols(this.sslProtocols);
        } else {
            ss.setEnabledProtocols(ss.getSupportedProtocols());
        }

        //Client certificate is required by SyncShare
        ss.setUseClientMode(false);
        //ss.setWantClientAuth(true);
        ss.setNeedClientAuth(true);
        return ss;
    }
}
