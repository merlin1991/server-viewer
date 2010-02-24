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
package serverviewer.gui;

import java.util.ArrayList;
import serverviewer.Main;
import serverviewer.config.ConfigServer;
import serverviewer.config.EtServer;
import serverviewer.config.Ts3Server;
import serverviewer.config.Ts2Server;
import serverviewer.net.EtQueryThread;
import serverviewer.net.Ts2QueryThread;
import serverviewer.net.QueryThread;
import serverviewer.net.Ts3QueryThread;
import serverviewer.net.data.QueryData;

public class ServerTreeUpdater implements Runnable {

    private Main mainFrame;
    private ArrayList<ConfigServer> servers = new ArrayList<ConfigServer>();

    /**
     * constructor of the ServerTreeUpdater, clones the serverList into it's own array
     * @param mainFrame the main frame to report to
     * @param serverList the list of servers to query
     */
    public ServerTreeUpdater(Main mainFrame, ArrayList<ConfigServer> serverList) {
        this.mainFrame = mainFrame;
        for (ConfigServer server : serverList) {
            servers.add(server.Clone());
        }
    }

    public void run() {
        if (servers.size() == 0) {
            ServerTreeNode rootNode = new ServerTreeNode("root", ServerTreeNodeType.Info);
            rootNode.add(new ServerTreeNode("no servers configured!", ServerTreeNodeType.Info));
            mainFrame.updateServerTree(rootNode);
        } else {
            QueryThread[] queryThreads = new QueryThread[servers.size()];
            ConfigServer configServer;
            for (int i = 0; i < queryThreads.length; i++) {
                configServer = servers.get(i);
                switch (configServer.getType()) {
                    case Et:
                        queryThreads[i] = new EtQueryThread((EtServer) configServer);
                        break;
                    case Ts:
                        queryThreads[i] = new Ts2QueryThread((Ts2Server) configServer);
                        break;
                    case Ts3:
                        queryThreads[i] = new Ts3QueryThread((Ts3Server) configServer);
                }
                queryThreads[i].start();
            }
            for (int i = 0; i < queryThreads.length; i++) {
                try {
                    queryThreads[i].join();
                } catch (InterruptedException ex) {
                }
            }
            QueryData[] queryData = new QueryData[queryThreads.length];
            for (int i = 0; i < queryThreads.length; i++) {
                queryData[i] = queryThreads[i].getData();
            }

            ServerTreeNode rootNode = new ServerTreeNode("root", ServerTreeNodeType.Info);
            for (QueryData data : queryData) {
                rootNode.add(data.getTreeNode());
            }

            mainFrame.updateServerTree(rootNode);
        }
    }
}
