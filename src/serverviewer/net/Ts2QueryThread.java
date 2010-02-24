/*
 * Copyright (c) 2009 Christian Ratzenhofer <christian_ratzenhofer@yahoo.de>
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * */
package serverviewer.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import serverviewer.config.Ts2Server;
import serverviewer.net.data.QueryData;
import serverviewer.net.data.Ts2User;
import serverviewer.net.data.Ts2ServerData;

public class Ts2QueryThread extends QueryThread {

    private Ts2Server tsServer;
    private ArrayList<Ts2User> tsUsers = new ArrayList<Ts2User>();
    private Exception exception = null;

    public Ts2QueryThread(Ts2Server tsServer) {
        this.tsServer = tsServer;
    }

    @Override
    public void run() {
        try {
            Socket tsSocket = new Socket();
            try {
                //tsSocket = new Socket(tsServer.getAddress(), tsServer.getQueryPort());
                tsSocket.bind(null);
                tsSocket.connect(new InetSocketAddress(tsServer.getAddress(), tsServer.getQueryPort()), 2000);
            } catch (IOException iOException) {
                throw new Exception("couldn't connect to ts queryport");
            }
            tsSocket.setSoTimeout(2000);
            BufferedReader inStream = new BufferedReader(new InputStreamReader(tsSocket.getInputStream()));

            if (inStream.readLine().equals("[TS]")) {
                PrintWriter outStream = new PrintWriter(tsSocket.getOutputStream(), true);
                outStream.println("sel " + tsServer.getPort());
                if (inStream.readLine().equals("OK")) {
                    String buffer;
                    String[] tokens;
                    String[] tokens2;

                    //get channel info
                    outStream.println("cl");
                    inStream.readLine();    // read 1 line to keep channel-data-syntax out of result 
                    //line contains: id        codec        parent        order        maxusers        name        flags        password        topic
                    HashMap<String, String> channels = new HashMap<String, String>();
                    while (!(buffer = inStream.readLine()).equals("OK")) {
                        tokens = buffer.split("\"");
                        tokens2 = buffer.split("\\s");
                        channels.put(tokens2[0], tokens[1]);
                    }

                    //get player info
                    outStream.println("pl");
                    inStream.readLine();    //read 1 line to keep play-data-syntax out of result 
                    //line contains: p_id        c_id        ps        bs        pr        br        pl        ping        logintime        idletime        cprivs        pprivs        pflags        ip        nick        loginname
                    Ts2User bufferPlayer = null;
                    while (!(buffer = inStream.readLine()).equals("OK")) {
                        tokens = buffer.split("\"");
                        tokens2 = buffer.split("\\s");
                        bufferPlayer = new Ts2User(tokens[3], channels.get(tokens2[1]), tokens2[9], tokens2[8], tokens2[7]);
                        tsUsers.add(bufferPlayer);
                    }

                    //close connection
                    inStream.close();
                    outStream.println("quit");
                    outStream.close();
                    tsSocket.close();
                } else {
                    inStream.close();
                    outStream.println("quit");
                    outStream.close();
                    tsSocket.close();
                    throw new Exception("couldn't select virtual server");
                }
            } else {
                inStream.close();
                tsSocket.close();
                throw new Exception("server is no ts server");
            }

        } catch (Exception ex) {
            this.exception = ex;
        }
    }

    public QueryData getData() {
        if (this.exception == null) {
            return new Ts2ServerData(tsServer, tsUsers);
        } else {
            return new Ts2ServerData(tsServer, exception);
        }
    }
}
