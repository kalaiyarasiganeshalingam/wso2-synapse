/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.synapse.transport.http.conn;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.NHttpServerEventHandler;
/**
 * Decorator class intended to transparently extend an {@link NHttpServerEventHandler}
 * with basic event logging capabilities using Commons Logging. 
 */
class LoggingNHttpServiceHandler implements NHttpServerEventHandler {

    private final Log log;
    private final NHttpServerEventHandler handler;

    public LoggingNHttpServiceHandler(
            final Log log,
            final NHttpServerEventHandler handler) {
        super();
        if (handler == null) {
            throw new IllegalArgumentException("HTTP service handler may not be null");
        }
        this.handler = handler;
        this.log = LogFactory.getLog(handler.getClass());
    }

    public void connected(
            final NHttpServerConnection conn) throws IOException, HttpException {
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + ": Connected");
        }
        this.handler.connected(conn);
    }

    public void closed(final NHttpServerConnection conn) {
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + ": Closed");
        }
        this.handler.closed(conn);
    }

    public void exception(final NHttpServerConnection conn, final Exception ex) {
        String msg = ex.getMessage();
        if (msg == null) {
            msg = "";
        }
        if (ex instanceof ConnectionClosedException ||
                msg.contains("Connection reset") ||
                msg.contains("forcibly closed")) {
            if (this.log.isDebugEnabled()) {
                this.log.debug(conn + ": " + msg +
                        " (Probably the keepalive connection was closed)");
            }
        } else {
            this.log.error(conn + ": " + msg, ex);
        }
        this.handler.exception(conn, ex);
    }

    public void requestReceived(
            final NHttpServerConnection conn) throws IOException, HttpException {
        HttpRequest request = conn.getHttpRequest();
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + ": " + request.getRequestLine());
        }
        this.handler.requestReceived(conn);
    }

    public void outputReady(
            final NHttpServerConnection conn,
            final ContentEncoder encoder) throws IOException, HttpException {
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + ": Output ready");
        }
        this.handler.outputReady(conn, encoder);
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + ": Content encoder " + encoder);
        }
    }

    public void responseReady(
            final NHttpServerConnection conn) throws IOException, HttpException {
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + ": Response ready");
        }
        this.handler.responseReady(conn);
    }

    public void inputReady(
            final NHttpServerConnection conn,
            final ContentDecoder decoder) throws IOException, HttpException {
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + ": Input ready");
        }
        this.handler.inputReady(conn, decoder);
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + ": Content decoder " + decoder);
        }
    }

    public void timeout(
            final NHttpServerConnection conn) throws IOException {
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + ": Timeout");
        }
        this.handler.timeout(conn);
    }

    public void endOfInput(
            final NHttpServerConnection conn) throws IOException {
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + ": End of input");
        }
        this.handler.endOfInput(conn);
    }

}