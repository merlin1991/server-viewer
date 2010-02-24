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

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import serverviewer.Main;

public class ServerTreeRenderer extends DefaultTreeCellRenderer {

    private ImageIcon tsIcon = new ImageIcon(Main.class.getResource("icons/ts.gif"));
    private ImageIcon ts3Icon = new ImageIcon(Main.class.getResource("icons/ts3.gif"));
    private ImageIcon etIcon = new ImageIcon(Main.class.getResource("icons/et.gif"));
    private ImageIcon tceIcon = new ImageIcon(Main.class.getResource("icons/tce.gif"));
    private ImageIcon infoIcon = new ImageIcon(Main.class.getResource("icons/leaf.gif"));
    private ImageIcon playerIcon = new ImageIcon(Main.class.getResource("icons/player.gif"));

    public ServerTreeRenderer() {
        this.setLeafIcon(infoIcon);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        ServerTreeNode treeNode = (ServerTreeNode) value;
        switch (treeNode.getType()) {
            case Et:
                setIcon(etIcon);
                break;
            case Tce:
                setIcon(tceIcon);
                break;
            case Ts2:
                setIcon(tsIcon);
                break;
            case Ts3:
                setIcon(ts3Icon);
                break;
            case Users:
                setIcon(playerIcon);
                break;
            }
        return this;
    }
}
