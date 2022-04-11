package tagger;

import java.io.*;
import java.util.*;

public class MyFile extends File {
    final static int RECURSEALL = 0x7fffffff;

    // this object is thought to be created but set by the extern
    // in particular from the getFile object. For example the
    // rec_level, the rel_path and the errors are set from the outside,
    // such as the mp3 object
    boolean match = true;
    int rec_level = 0;
    // String start_path=new String("");
    String rel_path = new String("");
    Hashtable<String, String> hasherror = new Hashtable<String, String>();
    Mp3info mp3 = null;

    MyFile(String dir) {
        super(dir);
    }

    MyFile(String dir, String name) {
        super(dir, name);
    }

    public boolean renameTo(File file2) {
        boolean res = super.renameTo(file2);
        if (!(file2 instanceof MyFile))
            return res;
        if (res) {
            mp3.renameTo(file2.getAbsolutePath());
            ((MyFile) file2).mp3 = mp3;
        } else
            return res;
        return res;
    }

    public boolean copyTo(File destfile) {
        if (destfile.exists())
            return false;
        RandomAccessFile file1 = null;
        RandomAccessFile file2 = null;
        try {
            file1 = new RandomAccessFile(getAbsolutePath(), "rw");
            file2 = new RandomAccessFile(destfile.getAbsolutePath(), "rw");
            file2.setLength(file1.length());
            file1.seek(0);
            file2.seek(0);
            byte buf[] = new byte[100000];
            int blk = 0;
            int filelen = (int) file1.length();
            for (blk = 0; (blk + 1) * 100000 < filelen; blk++) {
                file1.read(buf);
                file2.write(buf);
            }
            buf = new byte[filelen - blk];
            file1.read(buf);
            file2.write(buf);
            file1.close();
            file2.close();
            return true;
        } catch (Exception e) {
            try {
                file1.close();
                file2.close();
            } catch (Exception e2) {
                return false;
            }
            return false;
        }
    }

    public void setError(String err, String val) {
        if (val.trim().length() > 0) {
            hasherror.put(err, val);
        }
    }

    public String getError(String err) {
        if (err != null) {
            if (hasherror.containsKey(err)) {
                return (String) hasherror.get(err);
            } else
                return (new String(""));
        } else
            return (new String(""));
    }

    public String[] getErrorTypes() {
        String ret[] = new String[hasherror.size()];
        Enumeration<String> hashKeys = hasherror.keys();
        int i = 0;
        while (hashKeys.hasMoreElements()) {
            ret[i] = (String) hashKeys.nextElement();
            i++;
        }
        return ret;
    }
}
