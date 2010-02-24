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
package serverviewer.net.data;

import java.util.ArrayList;
import serverviewer.config.Ts2Server;
import serverviewer.gui.ServerTreeNode;
import serverviewer.gui.ServerTreeNodeType;

public class Ts2ServerData implements QueryData {

    private Exception exception = null;
    private Ts2Server tsServer;
    private ArrayList<Ts2User> tsUsers;

    public Ts2ServerData(Ts2Server tsServer, Exception exception) {
        this.tsServer = tsServer;
        this.exception = exception;
    }

    public Ts2ServerData(Ts2Server tsServer, ArrayList<Ts2User> tsUsers) {
        this.tsServer = tsServer;
        this.tsUsers = tsUsers;
    }

    public ServerTreeNode getTreeNode() {
        ServerTreeNode serverNode;
        if (exception != null) {
            serverNode = new ServerTreeNode(tsServer.getName() + " Error: " + exception.getMessage(), ServerTreeNodeType.Ts2);
        } else {
            switch (tsUsers.size()) {
                case 1:
                    serverNode = new ServerTreeNode(tsServer.getName() + " (1 player)", ServerTreeNodeType.Ts2);
                    break;
                default:
                    serverNode = new ServerTreeNode(tsServer.getName() + " (" + tsUsers.size() + " players)", ServerTreeNodeType.Ts2);
                    break;
            }
            ServerTreeNode userNode;
            ServerTreeNode infoNode;
            for (Ts2User tsUser : tsUsers) {
                userNode = new ServerTreeNode(tsUser.getName(), ServerTreeNodeType.Users);
                infoNode = new ServerTreeNode("channel: " + tsUser.getChannel(), ServerTreeNodeType.Info);
                userNode.add(infoNode);
                infoNode = new ServerTreeNode("last action " + tsUser.getLastAction() + " seconds ago", ServerTreeNodeType.Info);
                userNode.add(infoNode);
                infoNode = new ServerTreeNode("logintime: " + tsUser.getLiveTime() + " seconds", ServerTreeNodeType.Info);
                userNode.add(infoNode);
                infoNode = new ServerTreeNode("ping: " + tsUser.getPing() + " ms", ServerTreeNodeType.Info);
                userNode.add(infoNode);
                serverNode.add(userNode);
            }
        }
        return serverNode;
    }
}
