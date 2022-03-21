package tagger;

public class Id3v2frameflags
{
    boolean leave_existent=false;
    boolean always_these=false;

    
    int text_encoding=0;    
    int tag_alter_pres=0;
    int file_alter_pres=0;
    int read_only=0;
    int group_identity=0;
    int kompression=0;
    int encryption=0;
    int unsynchronization=0;
    int data_length=0;

    void copy (Id3v2frameflags obj)
    {
	text_encoding=obj.text_encoding;
	tag_alter_pres=obj.tag_alter_pres;
	file_alter_pres=obj.file_alter_pres;
	read_only=obj.read_only;
	group_identity=obj.group_identity;
	kompression=obj.kompression;
	encryption=obj.encryption;
	unsynchronization=obj.unsynchronization;
	data_length=obj.data_length;
    }
}
