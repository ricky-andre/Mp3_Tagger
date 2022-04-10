package tagger;

import java.util.*;
import java.awt.*;

import javax.swing.*;
// import javax.swing.border.*;

public class WarnPanel extends JPanel {
    private ArrayList<JLabel> labels = new ArrayList<JLabel>();
    private StringBuffer linetext = new StringBuffer();
    private Color textcolors[] = new Color[] { Color.black, Color.black, Color.black };
    private ImageIcon img[] = new ImageIcon[] { Utils.getImage("warnpanel", "ok"),
            Utils.getImage("warnpanel", "warning"), Utils.getImage("warnpanel", "error") };
    private Color bgcolor = null;
    private boolean autoscroll = false;
    private boolean alwaysupdateui = true;

    final static int OK = 0;
    final static int WARNING = 1;
    final static int ERROR = 2;
    final static int NO_AUTOSCROLL = 0;
    final static int AUTOSCROLL = 1;

    WarnPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // setLayout(new FlowLayout(FlowLayout.LEFT));
        bgcolor = Color.white;
        setBackground(Color.white);
    }

    public void scroll() {
        JScrollPane jsp = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this.getParent());
        if (jsp != null) {
            try {
                JViewport jvp = jsp.getViewport();
                int nowy = jvp.getViewPosition().y;
                int portHeight = jvp.getSize().height; // height pixels
                int h = (int) jvp.getPreferredSize().getHeight();
                if (nowy < h - portHeight && h - portHeight > 0) {
                    jvp.setViewPosition(new Point(0, h - portHeight + 5));
                    updateUI();
                }
            } catch (Exception e) {
            }
        }
    }

    public void setUpdateUI(boolean val) {
        alwaysupdateui = val;
    }

    private JLabel createLabel(String text, ImageIcon image) {
        JLabel tmp;
        if (image != null)
            tmp = new JLabel(text, image, JLabel.CENTER);
        else
            tmp = new JLabel(text);
        tmp.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 0));
        tmp.setBackground(bgcolor);
        tmp.setHorizontalAlignment(SwingConstants.LEFT);
        tmp.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        labels.add(tmp);
        return tmp;
    }

    private String getstr(int type) {
        String color = Integer.toHexString(textcolors[type].getRGB());
        return ("<html><B><font size=-1 color=#" + color.substring(2, 8) + ">"
                + linetext.substring(0, linetext.length()) + "");
    }

    private String getstr(Color col) {
        String color = Integer.toHexString(col.getRGB());
        return ("<html><B><font size=-1 color=#" + color.substring(2, 8) + ">"
                + linetext.substring(0, linetext.length()) + "");
    }

    public void setAutoScroll(boolean val) {
        autoscroll = val;
    }

    public void setBgColor(Color color) {
        bgcolor = color;
        setBackground(color);
        for (int i = 0; i < labels.size(); i++)
            ((JLabel) labels.get(i)).setBackground(color);
    }

    public void clearString() {
        linetext = new StringBuffer("");
    }

    public void clear() {
        while (labels.size() > 0) {
            remove((JComponent) labels.get(0));
            labels.remove(0);
        }
        if (alwaysupdateui)
            updateUI();
    }

    public void append(String text) {
        linetext.append(text);
        if (alwaysupdateui)
            updateUI();
    }

    public void append(String text, Color color) {
        String strcolor = Integer.toHexString(color.getRGB());
        linetext.append("<font color=#" + strcolor.substring(2, 8) + ">" + text + "</font>");
    }

    public void setDefaultIcon(int type) {
        if (type == OK)
            img[0] = Utils.getImage("warnpanel", "ok");
        else if (type == WARNING)
            img[1] = Utils.getImage("warnpanel", "warning");
        else if (type == ERROR)
            img[2] = Utils.getImage("warnpanel", "error");
    }

    public void setDefaultIcon() {
        img = new ImageIcon[] { Utils.getImage("warnpanel", "ok"), Utils.getImage("warnpanel", "warning"),
                Utils.getImage("warnpanel", "error") };
    }

    public void setIcon(int type, ImageIcon image) {
        if (type >= 0 && type < img.length)
            img[type] = image;
    }

    public void setColor(int type, Color col) {
        if (type >= 0 && type < textcolors.length)
            textcolors[type] = col;
    }

    public void addline() {
        add(createLabel(getstr(OK), img[0]));
        linetext = new StringBuffer("");
        if (alwaysupdateui)
            updateUI();
        if (autoscroll)
            scroll();
    }

    public void addline(int type) {
        if (type >= 0 && type < img.length)
            add(createLabel(getstr(type), img[type]));
        else
            add(createLabel(getstr(OK), img[0]));
        linetext = new StringBuffer("");
        if (alwaysupdateui)
            updateUI();
        if (autoscroll)
            scroll();
    }

    public void addline(String text, Color color) {
        String strcolor = Integer.toHexString(color.getRGB());
        linetext = new StringBuffer("<font color=#" + strcolor.substring(2, 8) + ">" + text + "</font>");
        add(createLabel(getstr(color), null));
        linetext = new StringBuffer("");
        if (alwaysupdateui)
            updateUI();
        if (autoscroll)
            scroll();
    }

    public void addline(JComponent label) {
        label.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        add(label);
        if (alwaysupdateui)
            updateUI();
        if (autoscroll)
            scroll();
    }

    /*
     * public void addline (JLabel label)
     * {
     * add(label);
     * updateUI();
     * if (autoscroll)
     * scroll();
     * }
     */

    public void addline(int type, JLabel label) {
        label.setIcon(img[type]);
        label.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        add(label);
        if (alwaysupdateui)
            updateUI();
        if (autoscroll)
            scroll();
    }

    public void addline(int type, String string) {
        if (type >= 0 && type < img.length)
            add(createLabel(string, img[type]));
        else
            add(createLabel(string, img[0]));
        if (alwaysupdateui)
            updateUI();
        if (autoscroll)
            scroll();
    }
}
