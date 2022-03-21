package tagger;

import javax.swing.*;

public abstract class Id3v2AbstractPanel extends JPanel
{
    final static int FILL=0;
    final static int EMPTY=1;
    final static int SINGLE_MODE=0;
    final static int VECTOR_MODE=1;

    static int singlemode=0;
    static int fillmode=0;

    String fieldId=null;
}
