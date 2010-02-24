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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import serverviewer.config.Ts3Server;
import serverviewer.net.data.QueryData;
import serverviewer.net.data.Ts3ServerData;
import serverviewer.net.data.Ts3User;

public class Ts3QueryThread extends QueryThread {

    private Ts3Server ts3Server;
    private Exception exception = null;
    private ArrayList<Ts3User> ts3Users = new ArrayList<Ts3User>();
    private HashMap<String, String> ts3Settings = new HashMap<String, String>();

    public Ts3QueryThread(Ts3Server ts3Server) {
        this.ts3Server = ts3Server;
    }

    @Override
    public void run() {
        Socket ts3Socket = new Socket();
        BufferedReader inStream = null;
        PrintWriter outStream = null;
        try {
            try {
                ts3Socket.bind(null);
                ts3Socket.connect(new InetSocketAddress(ts3Server.getAddress(), ts3Server.getQueryPort()), 2000);
            } catch (IOException iOException) {
                throw new Exception("couldn't connect to ts queryport");
            }
            ts3Socket.setSoTimeout(2000);
            inStream = new BufferedReader(new InputStreamReader(ts3Socket.getInputStream()));

            if (inStream.readLine().equals("TS3")) {
                outStream = new PrintWriter(ts3Socket.getOutputStream(), true);
                outStream.println("version");
                Pattern versionPattern = Pattern.compile("version=(.+)\\sbuild=[0-9]+\\splatform=[A-Za-z]+");
                Matcher matcher;
                String line;
                while (!(line = inStream.readLine()).startsWith("error")) {
                    if (!line.trim().equals("")) {
                        matcher = versionPattern.matcher(line);
                        if (matcher.matches()) {
                            ts3Settings.put("version", matcher.group(1));
                        }
                    }
                }
                Pattern errorPattern = Pattern.compile("error\\sid=([0-9]+)\\smsg=([^\\s]+).+"); //error id=0 msg=ok

                matcher = errorPattern.matcher(line);
                if (matcher.matches()) {
                    if (!matcher.group(1).equals("0")) {
                        throw new Exception(matcher.group(2).replace("\\s", " "));
                    }
                } else {
                    throw new Exception("invalid error response");
                }
                
                Thread.sleep(1000);
                
                outStream.println(String.format("serveridgetbyport virtualserver_port=%d", ts3Server.getPort()));
                String id = null;
                Pattern idPattern = Pattern.compile("server_id=([0-9]+)");
                while (!(line = inStream.readLine()).startsWith("error")) {
                    matcher = idPattern.matcher(line);
                    if (matcher.find()) {
                        id = matcher.group(1);
                    }
                }
                matcher = errorPattern.matcher(line);
                if (matcher.matches()) {
                    if (!matcher.group(1).equals("0")) {
                        throw new Exception(String.format("couldn't get serverid %s", matcher.group(2).replace("\\s", " ")));
                    }
                } else {
                    throw new Exception("invalid error response");
                }
                outStream.println(String.format("use %s", id));
                while (!(line = inStream.readLine()).startsWith("error")) {
                //skip empty lines
                }
                matcher = errorPattern.matcher(line);
                if (matcher.matches()) {
                    if (!matcher.group(1).equals("0")) {
                        throw new Exception(String.format("couldn't select virtual server %s", matcher.group(2).replace("\\s", " ")));
                    }
                } else {
                    throw new Exception("invalid error response");
                }
                
                Thread.sleep(1000);
                
                outStream.println("channellist");
                HashMap<String, String> channels = new HashMap<String, String>();
                Pattern channelPattern = Pattern.compile("cid=([0-9]+)\\spid=[0-9]+\\schannel_order=[0-9]+\\schannel_name=([^\\s]+)\\stotal_clients=[0-9]+\\schannel_needed_subscribe_power=[0-9]+");
                //cid=1 pid=0 channel_order=0 channel_name=Default\sChannel total_clients=1 channel_needed_subscribe_power=0
                while (!(line = inStream.readLine()).startsWith("error")) {
                    if (!line.trim().equals("")) {
                        String[] lines;
                        lines = line.split("\\|");
                        for (String channel : lines) {
                            matcher = channelPattern.matcher(channel);
                            if (matcher.matches()) {
                                channels.put(matcher.group(1), matcher.group(2).replace("\\s", " ").replace("\\p", "|"));
                            }
                        }
                    }
                }
                matcher = errorPattern.matcher(line);
                if (matcher.matches()) {
                    if (!matcher.group(1).equals("0")) {
                        throw new Exception(String.format("channellist failed: %s", matcher.group(2).replace("\\s", " ")));
                    }
                } else {
                    throw new Exception("invalid error response");
                }

                Thread.sleep(1000);
                
                outStream.println("clientlist");
                Pattern realPlayerPattern = Pattern.compile("clid=[0-9]+\\scid=([0-9]+)\\sclient_database_id=[0-9]+\\sclient_nickname=([^\\s]+)\\sclient_type=0");
                // clid=6 cid=1 client_database_id=3 client_nickname=merlin1991 client_type=0
                while (!(line = inStream.readLine()).startsWith("error")) {
                    if (!line.trim().equals("")) {
                        String[] lines;
                        lines = line.split("\\|");
                        for (String player : lines) {
                            matcher = realPlayerPattern.matcher(player);
                            if (matcher.matches()) {
                                ts3Users.add(new Ts3User(matcher.group(2).replace("\\s", " ").replace("\\p", "|"), channels.get(matcher.group(1))));
                            }
                        }
                    }
                }
                matcher = errorPattern.matcher(line);
                if (matcher.matches()) {
                    if (!matcher.group(1).equals("0")) {
                        throw new Exception(String.format("clientlist failed: %s", matcher.group(2).replace("\\s", " ")));
                    }
                } else {
                    throw new Exception("invalid error response");
                }
                outStream.println("quit");
                outStream.close();
                inStream.close();
                ts3Socket.close();
            }
        } catch (Exception ex) {
            this.exception = ex;
        } finally {
            try {
                if (outStream != null) {
                    outStream.println("quit");
                    outStream.close();
                }
                if (inStream != null) {
                    inStream.close();
                }
                ts3Socket.close();
            } catch (Exception ex) {
            }
        }
    }

    @Override
    public QueryData getData() {
        if (this.exception == null) {
            return new Ts3ServerData(ts3Server, ts3Users, ts3Settings);
        } else {
            return new Ts3ServerData(ts3Server, exception);
        }
    }
}
