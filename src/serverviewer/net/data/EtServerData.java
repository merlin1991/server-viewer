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
import java.util.HashMap;
import serverviewer.config.EtServer;
import serverviewer.gui.ServerTreeNode;
import serverviewer.gui.ServerTreeNodeType;

public class EtServerData implements QueryData {

    private Exception exception = null;
    private EtServer etServer;
    private HashMap<String, String> etSettings;
    private ArrayList<EtUser> etUsers;

    public EtServerData(EtServer etServer, Exception exception) {
        this.etServer = etServer;
        this.exception = exception;
    }

    public EtServerData(EtServer etServer, HashMap<String, String> etSettings, ArrayList<EtUser> etUsers) {
        this.etServer = etServer;
        this.etSettings = etSettings;
        this.etUsers = etUsers;
    }

    public ServerTreeNode getTreeNode() {
        ServerTreeNode serverNode;
        if (exception != null) {
            serverNode = new ServerTreeNode(etServer.getName() + " Error: " + exception.getMessage(), ServerTreeNodeType.Et);
        } else {
            ServerTreeNodeType serverType = ServerTreeNodeType.Et;
            if (etSettings.get("gamename") != null) {
                if (etSettings.get("gamename").equals("tcetest")) {
                    serverType = ServerTreeNodeType.Tce;
                }
            }
            switch (etUsers.size()) {
                case 1:
                    serverNode = new ServerTreeNode(etSettings.get("sv_hostname").replaceAll("\\^[^^]", "") + " (1 player)", serverType);
                    break;
                default:
                    serverNode = new ServerTreeNode(etSettings.get("sv_hostname").replaceAll("\\^[^^]", "") + " (" + etUsers.size() + " players)", serverType);
                    break;
            }
            ServerTreeNode infoNode;
            infoNode = new ServerTreeNode("max players: " + etSettings.get("sv_maxclients"), ServerTreeNodeType.Info);
            serverNode.add(infoNode);
            infoNode = new ServerTreeNode("private slots: " + etSettings.get("sv_privateClients"), ServerTreeNodeType.Info);
            serverNode.add(infoNode);
            infoNode = new ServerTreeNode("map: " + etSettings.get("mapname"), ServerTreeNodeType.Info);
            serverNode.add(infoNode);
            infoNode = new ServerTreeNode("mod: " + etSettings.get("gamename"), ServerTreeNodeType.Info);
            serverNode.add(infoNode);
            infoNode = new ServerTreeNode("punkbuster: " + etSettings.get("sv_punkbuster"), ServerTreeNodeType.Info);
            serverNode.add(infoNode);
            if (etUsers.size() != 0) {
                ServerTreeNode userNode;
                ServerTreeNode usersNode = new ServerTreeNode("players", ServerTreeNodeType.Folder);
                for (EtUser etUser : etUsers) {
                    userNode = new ServerTreeNode(etUser.getName().replaceAll("\\^[^^]", ""), ServerTreeNodeType.Users);
                    infoNode = new ServerTreeNode("ping: " + etUser.getPing(), ServerTreeNodeType.Info);
                    userNode.add(infoNode);
                    infoNode = new ServerTreeNode("score: " + etUser.getScore(), ServerTreeNodeType.Info);
                    userNode.add(infoNode);
                    usersNode.add(userNode);
                }
                serverNode.add(usersNode);
            } else {
                serverNode.add(new ServerTreeNode("noone online!", ServerTreeNodeType.Info));
            }
        }
        return serverNode;
    }
}
