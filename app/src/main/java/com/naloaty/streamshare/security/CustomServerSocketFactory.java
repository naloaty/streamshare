package com.naloaty.streamshare.security;

import java.io.IOException;
import java.net.ServerSocket;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import fi.iki.elonen.NanoHTTPD;

/**
 * This class represents custom server socket factory, which creates a socket that requires client authentication.
 * @see com.naloaty.streamshare.service.MediaServer
 */
public class CustomServerSocketFactory implements NanoHTTPD.ServerSocketFactory {

    private SSLServerSocketFactory sslServerSocketFactory;

    private String[] sslProtocols;

    public CustomServerSocketFactory(SSLServerSocketFactory sslServerSocketFactory, String[] sslProtocols) {
        this.sslServerSocketFactory = sslServerSocketFactory;
        this.sslProtocols = sslProtocols;
    }

    @Override
    public ServerSocket create() throws IOException {
        SSLServerSocket ss;

        ss = (SSLServerSocket) this.sslServerSocketFactory.createServerSocket();

        if (this.sslProtocols != null) {
            ss.setEnabledProtocols(this.sslProtocols);
        } else {
            ss.setEnabledProtocols(ss.getSupportedProtocols());
        }

        ss.setUseClientMode(false);

        //Enable client authentication
        ss.setNeedClientAuth(true);
        return ss;
    }
}
