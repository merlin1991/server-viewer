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
package serverviewer.config;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class Config {

    private ArrayList<ConfigServer> servers = new ArrayList<ConfigServer>();

    public void loadConfig() throws FileNotFoundException, IOException {
        servers.clear();
        IniReader configReader = new IniReader("server-viewer.ini");
        ArrayList<HashMap<String, String>> configData;
        configData = configReader.getIniContent();
        String sectionType;
        for (HashMap<String, String> configSection : configData) {
            if (configSection.get("name") != null) {
                if ((sectionType = configSection.get("type")) != null) {
                    if (sectionType.equals("et")) {
                        if (configSection.containsKey("address")) {
                            loadEtServer(configSection);
                        }
                    } else if (sectionType.equals("ts")) {
                        if (configSection.containsKey("address")) {
                            loadTs2Server(configSection);
                        }
                    } else if (sectionType.equals("ts3")) {
                        if (configSection.containsKey("address")) {
                            loadTs3Server(configSection);
                        }
                    }
                }
            }
        }
    }

    private void loadEtServer(HashMap<String, String> settings) {
        try {
            EtServer etServer = new EtServer(settings.get("name"));
            etServer.setAddress(settings.get("address"));
            if (settings.containsKey("port")) {
                etServer.setPort(Integer.parseInt(settings.get("port")));
            }
            servers.add(etServer);

        } catch (NumberFormatException numberFormatException) {
        }
    }

    private void loadTs2Server(HashMap<String, String> settings) {
        try {
            Ts2Server tsServer = new Ts2Server(settings.get("name"));
            tsServer.setAddress(settings.get("address"));
            if (settings.containsKey("queryPort")) {
                tsServer.setQueryPort(Integer.parseInt(settings.get("queryPort")));
            }
            if (settings.containsKey("port")) {
                tsServer.setPort(Integer.parseInt(settings.get("port")));
            }
            servers.add(tsServer);
        } catch (NumberFormatException numberFormatException) {
        }
    }

    private void loadTs3Server(HashMap<String, String> settings) {
        try {
            Ts3Server ts3Server = new Ts3Server(settings.get("name"));
            ts3Server.setAddress(settings.get("address"));
            if (settings.containsKey("queryPort")) {
                ts3Server.setQueryPort(Integer.parseInt(settings.get("queryPort")));
            }
            if (settings.containsKey("port")) {
                ts3Server.setPort(Integer.parseInt(settings.get("port")));
            }
            servers.add(ts3Server);
        } catch (NumberFormatException numberFormatException) {
        }
    }

    public void saveConfig() throws IOException {
        PrintWriter configWriter = new PrintWriter(new FileWriter("server-viewer.ini"));
        configWriter.write("//this is a config file  for the server-viewer by merlin1991\n//don't edit manualy, use the editor that is inside the tool\n");
        EtServer etServer;
        Ts2Server tsServer;
        Ts3Server ts3Server;
        for (ConfigServer server : servers) {
            switch (server.getType()) {
                case Et:
                    etServer = (EtServer) server;
                    configWriter.write("[" + etServer.getName() + "]\n");
                    configWriter.write("type = et\n");
                    configWriter.write("address = " + etServer.getAddress() + '\n');
                    configWriter.write("port = " + etServer.getPort() + '\n');
                    break;
                case Ts:
                    tsServer = (Ts2Server) server;
                    configWriter.write("[" + tsServer.getName() + "]\n");
                    configWriter.write("type = ts\n");
                    configWriter.write("address = " + tsServer.getAddress() + '\n');
                    configWriter.write("port = " + tsServer.getPort() + '\n');
                    configWriter.write("queryPort = " + tsServer.getQueryPort() + '\n');
                    break;
                case Ts3:
                    ts3Server = (Ts3Server) server;
                    configWriter.write("[" + ts3Server.getName() + "]\n");
                    configWriter.write("type = ts3\n");
                    configWriter.write("address = " + ts3Server.getAddress() + '\n');
                    configWriter.write("port = " + ts3Server.getPort() + '\n');
                    configWriter.write("queryPort = " + ts3Server.getQueryPort() + '\n');
                    break;
            }
        }
        configWriter.close();
    }

    public void createExampleConfig() {
        PrintWriter configWriter = null;
        try {
            configWriter = new PrintWriter(new FileWriter("server-viewer.ini"));
            configWriter.write("//this is a config file  for the server-viewer by merlin1991\n//don't edit manualy, use the editor that is inside the tool\n");
            configWriter.write("[example server]\ntype = ts\naddress = www.example.org");
            Ts2Server tsServer = new Ts2Server("example server");
            tsServer.setAddress("www.example.org");
            servers.clear();
            servers.add(tsServer);
        } catch (IOException ex) {
        } finally {
            configWriter.close();
        }

    }

    public ArrayList<ConfigServer> getServers() {
        return servers;
    }
}
