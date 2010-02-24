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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import serverviewer.config.EtServer;
import serverviewer.net.data.EtServerData;
import serverviewer.net.data.EtUser;
import serverviewer.net.data.QueryData;

public class EtQueryThread extends QueryThread {

    private EtServer etServer;
    private HashMap<String, String> etSettings = new HashMap<String, String>();
    private ArrayList<EtUser> etUsers = new ArrayList<EtUser>();
    private Exception exception = null;

    public EtQueryThread(EtServer etServer) {
        this.etServer = etServer;
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket();
            Charset ascii = Charset.forName("ASCII");
            byte[] sendBuf = "aaaagetstatus".getBytes(ascii);
            sendBuf[0] = -1;
            sendBuf[1] = -1;
            sendBuf[2] = -1;
            sendBuf[3] = -1;
            DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, InetAddress.getByName(etServer.getAddress()), etServer.getPort());
            socket.send(packet);
            packet = new DatagramPacket(new byte[4096], 4096);
            socket.setSoTimeout(2000);
            socket.receive(packet);
            String x = new String(packet.getData(), 0, packet.getLength(), ascii);
            String input[] = x.split("\\n");
            byte[] compareBuff = "aaaastatusResponse".getBytes(ascii);
            compareBuff[0] = 63;
            compareBuff[1] = 63;
            compareBuff[2] = 63;
            compareBuff[3] = 63;
            byte[] toCompare = input[0].getBytes(ascii);
            if (Arrays.equals(toCompare, compareBuff)) {
                Pattern playerline = Pattern.compile("^([-0-9][0-9]*)\\s([0-9]+)\\s\"([^\"]+)\"$");
                Matcher matcher;
                EtUser etUser;
                for (String line : input) {
                    matcher = playerline.matcher(line);
                    if (matcher.matches()) {
                        etUser = new EtUser(matcher.group(3), matcher.group(2), matcher.group(1));
                        etUsers.add(etUser);
                    } else if (line.startsWith("\\")) {
                        Pattern settingsPattern = Pattern.compile("\\\\([^\\\\]+)\\\\([^\\\\]+)"); // THIS IS LOL, should match this pattern: \\([^\\]+)\\([^\\]+)
                        matcher = settingsPattern.matcher(line);
                        while(matcher.find()) {
                            etSettings.put(matcher.group(1), matcher.group(2));
                        }
                    }
                }
            } else {
                throw new Exception("invalid response from server");
            }
        } catch (Exception ex) {
            exception = ex;
        }
    }

    public QueryData getData() {
        if (this.exception == null) {
            return new EtServerData(etServer, etSettings, etUsers);
        } else {
            return new EtServerData(etServer, exception);
        }
    }
}
