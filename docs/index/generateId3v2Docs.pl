open IN, "FramesNames.txt" or die"";

$gothalf=0;
$secname="";
$name="";

while (<IN>)
{
    s /\n//;
    s /\"//g;

    @fields = split /,/;
    $val{$fields[3]}=$fields[0];
    # print "key ".$fields[3]." value ".$fields[0]."\n";
}

close IN;

open IN, "Id3v2Frames.txt" or die"";

while (<IN>)
{
    s /\n//;
    
    if ($val{$_})
    {
	# print "Frame ".$val{$_}."\n";
	$exits=1;
	
	$aux=$_;
	
	$_=$val{$_};
	s /\// or /g;
	s /\\/ or /g;	
	
	$filename=$_.".html";
	
	s/^([a-z])/chr(ord($1)-32)/e;
	$temporaryFrameName=$_;

	print "\"".$val{$aux}."\",indexDir+\"".$filename."\",\n";
	
	$out=$filename;	

	open OUT, ">$out" or die "Cannot open $out for write :$!";
	print OUT "<h1 align=center><font color=#ff6600>Tag v2</font></h1><p>\n";
	print OUT "This is one of the frames introduced by tag version 2 standard.<p>\n";
	print OUT "Tag version 2 technical specifications can be found at \"www.id3.org\".<p>";
	print OUT "Not all the frames defined by this standard are yet supported by\n";
	print OUT "\"Mp3 studio\", but most of them are described here for completeness reasons.\n";
	print OUT "You can set the supported frames using the \"Advanced\n";
	print OUT "mass tag window\" or the \"Advanced edit tag window\". <p>";
	print OUT "<h2><font color=#ff6600 align=center>".$temporaryFrameName."</font></h2><p>\n";
	
	while ($exits)
	{
	    $_=<IN>;
	    if (/[A-Za-z]+/)
	    {
		print OUT $_."\n";
		# print $_."\n";
	    }
	    else
	    {
		# print "\n";
		$exits=0;
	    }
	}
	close OUT;
    }
}



