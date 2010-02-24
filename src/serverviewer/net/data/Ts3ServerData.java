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
import serverviewer.config.Ts3Server;
import serverviewer.gui.ServerTreeNode;
import serverviewer.gui.ServerTreeNodeType;

public class Ts3ServerData implements QueryData {

    private Exception exception = null;
    private Ts3Server ts3Server;
    private ArrayList<Ts3User> ts3Users;
    private HashMap<String, String> ts3Settings;

    public Ts3ServerData(Ts3Server ts3Server, Exception exception) {
        this.ts3Server = ts3Server;
        this.exception = exception;
    }

    public Ts3ServerData(Ts3Server ts3Server, ArrayList<Ts3User> ts3Users, HashMap<String, String> ts3Settings) {
        this.ts3Server = ts3Server;
        this.ts3Users = ts3Users;
        this.ts3Settings = ts3Settings;
    }

    public ServerTreeNode getTreeNode() {
        ServerTreeNode serverNode;
        if (exception != null) {
            serverNode = new ServerTreeNode(ts3Server.getName() + " Error: " + exception.getMessage(), ServerTreeNodeType.Ts3);
        } else {
            switch (ts3Users.size()) {
                case 1:
                    if (ts3Settings.get("name") != null) {
                        serverNode = new ServerTreeNode(stripTsString(ts3Settings.get("name")) + " (1 player)", ServerTreeNodeType.Ts3);
                    } else {
                        serverNode = new ServerTreeNode(ts3Server.getName() + " (1 player)", ServerTreeNodeType.Ts3);
                    }
                    break;
                default:
                    if (ts3Settings.get("name") != null) {
                        serverNode = new ServerTreeNode(stripTsString(ts3Settings.get("name")) + " (" + ts3Users.size() + " players)", ServerTreeNodeType.Ts3);
                    } else {
                        serverNode = new ServerTreeNode(ts3Server.getName() + " (" + ts3Users.size() + " players)", ServerTreeNodeType.Ts3);
                    }
                    break;
            }
            serverNode.add(new ServerTreeNode(stripTsString(ts3Settings.get("welcomemessage")), ServerTreeNodeType.Info));
            serverNode.add(new ServerTreeNode(String.format("version: %s", ts3Settings.get("version")), ServerTreeNodeType.Info));
            serverNode.add(new ServerTreeNode(String.format("platform: %s", ts3Settings.get("platform")), ServerTreeNodeType.Info));
            serverNode.add(new ServerTreeNode(String.format("uptime: %s Seconds", ts3Settings.get("uptime")), ServerTreeNodeType.Info));
            ServerTreeNode playerNode;
            for (Ts3User player : ts3Users) {
                playerNode = new ServerTreeNode(player.getName(), ServerTreeNodeType.Users);
                playerNode.add(new ServerTreeNode("channel: " + player.getChannel(), ServerTreeNodeType.Info));
                serverNode.add(playerNode);
            }
        }
        return serverNode;
    }

    private String stripTsString(String original) {
        if (original != null) {
            return original.replace("\\s", " ").replace("\\p", "|");
        } else {
            return null;
        }
    }
}
