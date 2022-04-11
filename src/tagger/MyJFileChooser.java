package tagger;

import javax.swing.*;
import java.io.*;
import java.util.*;

public class MyJFileChooser extends JFileChooser {
    private static Icon icon = (Icon) Utils.getImage("tree", "folder");

    MyJFileChooser() {
        super();
        setLocale(Locale.US);
    }

    MyJFileChooser(String str) {
        super(str);
        setLocale(Locale.US);
    }

    MyJFileChooser(File file) {
        super(file);
        setLocale(Locale.US);
    }

    public Icon getIcon(File f) {
        if (f.isDirectory())
            return icon;
        else
            return super.getIcon(f);
    }
}
