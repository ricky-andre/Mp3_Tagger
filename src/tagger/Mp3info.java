package tagger;
// reading time 75 ms

// mem occupation 1 Megabyte for 100 songs
// writing time 17 ms
// better to keep every song in memory ...

import java.io.*;
import java.util.*;

public class Mp3info {
	final static Hashtable<String, String> languagesstring = new Hashtable<String, String>();
	final private static String languagesfullstring[] = new String[] {
			"afr - Afrikaans", "aka - Akan", "alb - Albanian", "ara - Arabic",
			"arc - Aramaic", "arm - Armenian", "aze - Azerbaijani", "bat Baltic - ",
			"bel - Byelorussian", "bod - Tibetan", "bre - Breton", "bul - Bulgarian",
			"cel Celtic - ", "ces - Czech", "chu Church - Slavic", "dak - Dakota",
			"dan - Danish", "egy Egyptian - ", "eng - English",
			"epo - Esperanto", "esk Eskimo - ", "esl - Spanish", "est - Estonian",
			"eus - Basque", "fra - French", "gae Gaelic - ", "gai - Irish", "geo - Georgian",
			"ger - German", "haw - Hawaiian", "heb - Hebrew", "hun - Hungarian", "hye - Armenian",
			"ind - Indonesian", "ira Iranian - ", "isl - Icelandic", "ita - Italian",
			"jpn - Japanese", "kat - Georgian", "kor - Korean", "lit - Lithuanian",
			"mao - Maori", "mol - Moldavian", "mon - Mongolian",
			"mus - Creek", "nep - Nepali", "nor - Norwegian",
			"pli - Pali", "pol - Polish", "por - Portuguese",
			"rus - Russian", "scr - Serbo-Croatian", "slk - Slovak", "sun - Sudanese",
			"swe - Swedish", "tog Tonga - ", "tur - Turkish",
			"und - Undetermined", "vie - Vietnamese", "zho - Chinese" };
	final static MyCombo languages = (MyCombo) new MyCombo(languagesfullstring);
	static {
		for (int i = 0; i < languagesfullstring.length; i++) {
			languagesstring.put(languagesfullstring[i].substring(0, 3), languagesfullstring[i]);
		}
	}

	final static String genreList[] = new String[] {
			"Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk", "Grunge",
			"Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other", "Pop",
			"R&B", "Rap", "Reggae", "Rock", "Techno", "Industrial",
			"Alternative", "Ska", "Death Metal", "Pranks", "Soundtrack",
			"Euro-Techno", "Ambient", "Trip-Hop", "Vocal", "Jazz+Funk",
			"Fusion", "Trance", "Classical", "Instrumental", "Acid", "House",
			"Game", "Sound Clip", "Gospel", "Noise", "AlternRock", "Bass",
			"Soul", "Punk", "Space", "Meditative", "Instrumental Pop",
			"Instrumental Rock", "Ethnic", "Gothic", "Darkwave",
			"Techno-Industrial", "Electronic", "Pop-Folk", "Eurodance",
			"Dream", "Southern Rock", "Comedy", "Cult", "Gangsta", "Top 40",
			"Christian Rap", "Pop/Funk", "Jungle", "Native American",
			"Cabaret", "New Wave", "Psychadelic", "Rave", "Showtunes",
			"Trailer", "Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz", "Polka",
			"Retro", "Musical", "Rock & Roll", "Hard Rock", "Folk", "Folk-Rock",
			"National Folk", "Swing", "Fast Fusion", "Bebob", "Latin", "Revival",
			"Celtic", "Bluegrass", "Avantgarde", "Gothic Rock", "Progressive Rock",
			"Psychedelic Rock", "Symphonic Rock", "Slow Rock", "Big Band", "Chorus",
			"Easy Listening", "Acoustic", "Humour", "Speech", "Chanson", "Opera",
			"Chamber Music", "Sonata", "Symphony", "Booty Brass", "Primus",
			"Porn Groove", "Satire", "Slow Jam", "Club", "Tango", "Samba",
			"Folklore", "Ballad", "Poweer Ballad", "Rhytmic Soul", "Freestyle",
			"Duet", "Punk Rock", "Drum Solo", "A Capela", "Euro-House", "Dance Hall" };

	final static String orderedGenreList[] = new String[genreList.length];
	static {
		TreeMap<String, String> genrelist = new TreeMap<String, String>();
		for (int i = 0; i < genreList.length; i++) {
			genrelist.put(genreList[i], "");
		}

		Set<Map.Entry<String, String>> set = genrelist.entrySet();
		Iterator<Map.Entry<String, String>> iterator = set.iterator();
		int count = 0;
		while (iterator.hasNext()) {
			Map.Entry<String, String> elem = (Map.Entry<String, String>) iterator.next();
			orderedGenreList[count] = (String) elem.getKey();
			count++;

		}
	}

	final static Hashtable<String, Integer> fromGenreToIntegerHash = new Hashtable<String, Integer>();
	static {
		for (int i = 0; i < genreList.length; i++) {
			fromGenreToIntegerHash.put(genreList[i], Integer.valueOf(i));
		}
	}

	static final int miodebug = 11;
	static final int WRITEDEBUG = 20;
	static final int SYNCRODEBUG = 20;
	static final int READDEBUG = 20;
	static final int CRC_PROT = 0;
	static final int CRC_NOT_PROT = 1;

	static final int COPYRIGHT = 1;
	static final int NOT_COPYRIGHT = 0;

	static final int COPY_OF_ORIGINAL = 0;
	static final int ORIGINAL = 1;
	@SuppressWarnings("unused")

	/*
	 * Always remember to check also the constructor if
	 * a new type of reading mode is added!!!
	 */
	// checks if it is an mp3 file, read tags and song length
	static final int READALL = 0x1;
	// checks if it is an mp3 file, reads the tags
	static final int READONLYTAGS = 0x2;
	// checks if it is an mp3 file and calculates the real song length
	static final int READONLYMP3 = 0x4;
	// checks if it is an mp3 file
	static final int READONLYISMP3 = 0x8;
	// checks if it is an mp3 file, reads tags and check if it is vbR
	// without calculating the song length
	static final int READTAGSANDISVBR = 0x10;

	/*
	 * static final int jumponedimlay1[]=
	 * {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,
	 * 0,0,26,52,78,104,130,156,182,208,261,313,365,417,470,522,0,0,27,53,79,105,131
	 * ,157,
	 * 183,209,262,314,366,418,471,523,0,0,104,130,156,182,208,261,313,365,417,522,
	 * 626,
	 * 731,835,1044,0,0,105,131,157,183,209,262,314,366,418,523,627,732,836,1045,0,0
	 * ,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,24,48,72,96,120,144,168,192,240,288
	 * ,336,
	 * 384,432,480,0,0,25,49,73,97,121,145,169,193,241,289,337,385,433,481,0,0,96,
	 * 120,144,
	 * 168,192,240,288,336,384,480,576,672,768,960,0,0,97,121,145,169,193,241,289,
	 * 337,385,
	 * 481,577,673,769,961,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,36,72,108
	 * ,144,
	 * 180,216,252,288,360,432,504,576,648,720,0,0,37,73,109,145,181,217,253,289,361
	 * ,433,
	 * 505,577,649,721,0,0,144,180,216,252,288,360,432,504,576,720,864,1008,1152,
	 * 1440,0,0,
	 * 145,181,217,253,289,361,433,505,577,721,865,1009,1153,1441,0};
	 */

	/*
	 * static final int jumponedimlaynot1[]=
	 * {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,105,157,183,209,262,314,366,418,471,523,575,627,732,836
	 * ,0,0,
	 * 105,157,183,209,262,314,366,418,471,523,575,627,732,836,0,0,105,131,157,183,
	 * 209,262,
	 * 314,366,418,523,627,732,836,1045,0,0,105,131,157,183,209,262,314,366,418,523,
	 * 627,732,
	 * 836,1045,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	 * 0,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,97,145,169,193,241,
	 * 289,337,385,
	 * 433,481,529,577,673,769,0,0,97,145,169,193,241,289,337,385,433,481,529,577,
	 * 673,769,0,
	 * 0,97,121,145,169,193,241,289,337,385,481,577,673,769,961,0,0,97,121,145,169,
	 * 193,241,
	 * 289,337,385,481,577,673,769,961,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,145,217,
	 * 253,289,361,433,505,577,649,721,793,865,1009,1153,0,0,145,217,253,289,361,433
	 * ,505,577,
	 * 649,721,793,865,1009,1153,0,0,145,181,217,253,289,361,433,505,577,721,865,
	 * 1009,1153,
	 * 1441,0,0,145,181,217,253,289,361,433,505,577,721,865,1009,1153,1441,0,0,0,0,0
	 * ,0,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,108,160,186,212,265,317,369,421,474,526,578,630,
	 * 735,839,0,
	 * 0,108,160,186,212,265,317,369,421,474,526,578,630,735,839,0,0,108,212,317,421
	 * ,526,630,
	 * 735,839,944,1048,1153,1257,1362,1466,0,0,108,212,317,421,526,630,735,839,944,
	 * 1048,1153,
	 * 1257,1362,1466,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
	 * 0,0,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,100,148,172,196,
	 * 244,292,340,
	 * 388,436,484,532,580,676,772,0,0,100,148,172,196,244,292,340,388,436,484,532,
	 * 580,676,772,
	 * 0,0,100,196,292,388,484,580,676,772,868,964,1060,1156,1252,1348,0,0,100,196,
	 * 292,388,484,
	 * 580,676,772,868,964,1060,1156,1252,1348,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,0,0,0,0,
	 * 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
	 * ,0,0,0,148,
	 * 220,256,292,364,436,508,580,652,724,796,868,1012,1156,0,0,148,220,256,292,364
	 * ,436,508,580,
	 * 652,724,796,868,1012,1156,0,0,148,292,436,580,724,868,1012,1156,1300,1444,
	 * 1588,1732,1876,
	 * 2020,0,0,148,292,436,580,724,868,1012,1156,1300,1444,1588,1732,1876,2020,0};
	 */

	// accessed with:
	// rate [mp3vers][mp3lay][ratecode]
	// mp3vers=3 -> 1.0
	// mp3vers=2 -> 2.0
	// mp3vers=0 -> 2.5
	// layer=1 -> layer 3
	// layer=2 -> layer 2
	// layer=3 -> layer 1
	static private final int rate[][][] = {
			{ {},
					{ -1, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160, -2 },
					{ -1, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160, -2 },
					{ -1, 32, 48, 56, 64, 80, 96, 112, 128, 144, 160, 176, 192, 224, 256, -2 } },
			{ {}, {}, {}, {} },
			{ {},
					{ -1, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160, -2 },
					{ -1, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160, -2 },
					{ -1, 32, 48, 56, 64, 80, 96, 112, 128, 144, 160, 176, 192, 224, 256, -2 } },
			{
					{},
					{ -1, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, -2 },
					{ -1, 32, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, 384, -2 },
					{ -1, 32, 64, 96, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448, -2 },
			} };
	// accessed with:
	// samplerate[mpg_vers][samplecode]
	static private final int samplerate[][] = { { 11025, 12000, 8000, -1 }, {}, { 22050, 24000, 16000, -1 },
			{ 44100, 48000, 32000, -1 } };
	static private final int pad_value[] = { -1, 1, 1, 4 };

	// access with the formula samplerate*128+mpgversion*32+padvalue*16+bitcoderate
	// static private final int jumponedimlay1[]=new int[384];
	// access with the formula
	// layer*384+samplerate*128+mpgversion*32+padvalue*16+bitcoderate
	// static private final int jumponedimlaynot1[]=new int[1536];

	// access with the formula samplerate*128+mpgversion*32+padvalue*16+bitcoderate
	static private final int jumpmultidimlay1[][][][] = new int[3][4][2][16];
	// access with the formula
	// layer*384+samplerate*128+mpgversion*32+padvalue*16+bitcoderate
	static private final int jumpmultidimlaynot1[][][][][] = new int[4][3][4][2][16];

	static {
		int sample_rate;
		int bitrate;
		int frame_length;
		double corr_fact;

		// layer 1 calculation
		for (int mpglay = 1; mpglay < 2; mpglay++) {
			for (int sampr = 0; sampr < 3; sampr++)
				for (int mpgver = 0; mpgver < 4; mpgver++)
					for (int padv = 0; padv < 2; padv++)
						for (int bitrr = 1; bitrr < 15; bitrr++) {
							if (bitrr == 0 || bitrr == 15 || mpgver == 1) {
								// System.out.println (" layer "+mpglay+" sampleratcod "+sampr+" mpg vers
								// "+mpgver+"pad "+padv
								// +" bitrcod "+bitrr+" frame len "+"0");

							} else {
								sample_rate = samplerate[mpgver][sampr];
								bitrate = rate[mpgver][mpglay][bitrr];

								// this corr_fact is an empiric value ...
								// used to correct the frame length! It works
								// with sample_rate of 44100,48000,22050,24000,11050
								// for the others int has to be tested!
								corr_fact = 44100.0 / (double) sample_rate;
								if ((corr_fact - (int) corr_fact > 0.5))
									corr_fact = (int) (corr_fact + 1);
								else
									corr_fact = (int) corr_fact;
								if (corr_fact == 4)
									corr_fact = 2;
								frame_length = (int) (((double) bitrate * 1000 / (double) sample_rate) * 144
										/ corr_fact);
								if (padv == 1)
									frame_length += 1;

								// System.out.println (" layer "+mpglay+" sampleratcod "+sampr+" mpg vers
								// "+mpgver+"pad "+padv
								// +" bitrcod "+bitrr+" frame len "+frame_length);
								// System.out.print (frame_length+",");
								// jumponedimlay1 [sampr*128+mpgver*32+padv*16+bitrr]=frame_length;
								jumpmultidimlay1[sampr][mpgver][padv][bitrr] = frame_length;
							}
						}
		}

		// calculate for the other layers, layer code 2,3
		// and mpg version codes 0,2,3
		for (int mpglay = 2; mpglay < 4; mpglay++)
			for (int sampr = 0; sampr < 3; sampr++)
				for (int mpgver = 0; mpgver < 4; mpgver++)
					for (int padv = 0; padv < 2; padv++)
						for (int bitrr = 1; bitrr < 15; bitrr++) {
							if (mpglay == 0 || mpglay == 1 || mpgver == 1 || bitrr == 0 || bitrr == 15) {
								// System.out.println (" layer "+mpglay+" sampleratcod "+sampr+" mpg vers
								// "+mpgver+"pad "+padv
								// +" bitrcod "+bitrr+" frame len "+"0");
								// System.out.print ("0,");
							} else {
								sample_rate = samplerate[mpgver][sampr];
								bitrate = rate[mpgver][mpglay][bitrr];
								if (padv == 1)
									frame_length = (int) (144 * bitrate * 1000 / sample_rate + pad_value[mpglay]);
								else
									frame_length = (int) (144 * bitrate * 1000 / sample_rate);
								// System.out.println (" layer "+mpglay+" sampleratcod "+sampr+" mpg vers
								// "+mpgver+"pad "+padv
								// +" bitrcod "+bitrr+" frame len "+frame_length);
								// System.out.print (frame_length+",");
								// jumponedimlaynot1
								// [mpglay*384+sampr*128+mpgver*32+padv*16+bitrr]=frame_length;
								jumpmultidimlaynot1[mpglay][sampr][mpgver][padv][bitrr] = frame_length;
							}
						}
	}

	// private long end, start;
	private RandomAccessFile song = null, id3tag = null;
	private String filenamestr;
	private boolean ismp3 = false;
	private int readmode = 0;
	private boolean isvbr = false;
	private int song_length = 0;
	private int songLengthSeconds = -1;
	private int real_sync_start = -1;
	private int num_of_frames = 0;
	private int bitrate = -1;
	private int sample_rate = -1;
	private int mpg_version = -1;
	private int mpg_layer = -1;
	private int channel_type = -1;
	private int emphasys = -1;
	private int copyright = -1;
	private int copy = -1;
	private int crc = -1;

	final private static int NAMES = 0;
	final private static int V22 = 1;
	final private static int V23 = 2;
	final private static int V24 = 3;
	final private static int TYPE = 4;
	final private static int NUMEROSITY = 5;

	private static Hashtable<String, Integer> fieldsString = new Hashtable<String, Integer>();
	private static Hashtable<String, Integer> fieldsId = new Hashtable<String, Integer>();
	/**
	 * The fields are saved to the file in the same order they are listed
	 * here. This is done to solve some problems with tag v2 readers that
	 * do not correctly parse all the fields. In this way the most important
	 * fields are preserved and will probably be correctly read by every
	 * tag v2 parser.
	 */
	final private static String fieldsTable[][] = {
			// id3v1 most important frames and general fields
			{ "comment", "COM", "COMM", "COMM", "comm", "vector" },
			{ "album", "TAL", "TALB", "TALB", "text", "single" },
			{ "genre", "TCO", "TCON", "TCON", "text", "single" },
			{ "title", "TT2", "TIT2", "TIT2", "text", "single" },
			{ "year", "TYE", "TYER", "TDRC", "text", "single" },
			{ "track", "TRK", "TRCK", "TRCK", "text", "single" },
			{ "artist", "TP1", "TPE1", "TPE1", "text", "single" },

			// original informations, composers and so on (inserted)
			{ "content group description", "TT1", "TIT1", "TIT1", "text", "single" },
			{ "subtitle/description refinement", "TT3", "TIT3", "TIT3", "text", "single" },
			{ "original album/movie/show title", "TOT", "TOAL", "TOAL", "text", "single" },
			{ "original filename", "TOF", "TOFN", "TOFN", "text", "single" },
			{ "original lyricist(s)/text writer(s)", "TOL", "TOLY", "TOLY", "text", "single" },
			{ "original artist(s)/performer(s)", "TOA", "TOPE", "TOPE", "text", "single" },
			{ "file owner/license", "", "TOWN", "TOWN", "", "" },
			{ "lyricist/text writer", "TXT", "TEXT", "TEXT", "text", "single" },
			{ "initial key", "TKE", "TKEY", "TKEY", "text", "single" }, // only three characters
			{ "musician credits list", "", "", "TMCL", "", "" },
			{ "involved people list", "IPL", "IPLS", "TIPL", "text", "single" },

			// interpreter informations
			{ "composer", "TCM", "TCOM", "TCOM", "text", "single" },
			{ "band/orchestra/accompaniment", "TP2", "TPE2", "TPE2", "text", "single" },
			{ "conductor/performer refinement", "TP3", "TPE3", "TPE3", "text", "single" },
			{ "interpreted, remixed or modified by", "TP4", "TPE4", "TPE4", "text", "single" },
			{ "part of a set", "TPA", "TPOS", "TPOS", "text", "single" },
			{ "produced notice", "", "", "TPRO", "", "" },

			// WWW useful links (inserted ina a group!)
			{ "commercial information", "WCM", "WCOM", "WCOM", "url", "vector" },
			{ "copyright/legal information", "WCP", "WCOP", "WCOP", "url", "single" },
			{ "official audio file webpage", "WAF", "WOAF", "WOAF", "url", "single" },
			{ "official artist/performer webpage", "WAR", "WOAR", "WOAR", "url", "single" },
			{ "official audio source webpage", "WAS", "WOAS", "WOAS", "url", "single" },
			{ "official internet radio station homepage", "", "WORS", "WORS", "url", "single" },
			{ "payment", "", "WPAY", "WPAY", "url", "single" },
			{ "publishers official webpage", "WPB", "WPUB", "WPUB", "url", "single" },
			{ "user url", "WXX", "WXXX", "WXXX", "userurl", "vector" },
			{ "internet radio station name", "", "TRSN", "TRSN", "text", "single" },
			{ "internet radio station owner", "", "TRSO", "TRSO", "text", "single" },

			// other songs info
			{ "play counter", "CNT", "PCNT", "PCNT", "", "" }, // number of times the song is played
			{ "size", "TSI", "TSIZ", "", "text", "single" }, // deprecated, song length in bytes without id3v2
			{ "beats per minute", "BPM", "TBPM", "TBPM", "text", "single" }, // beats per minute
			{ "length", "TLE", "TLEN", "TLEN", "text", "single" }, // milliseconds of the songs
			{ "terms of use", "", "USER", "USER", "", "" },

			// time informations
			{ "encoding time", "", "", "TDEN", "", "" },
			{ "date", "TDA", "TDAT", "", "text", "single" }, // format DDMM only 4 characters, in 2.4 general
																// representation under TDRC identifier
			{ "time", "TIM", "TIME", "", "text", "single" }, // format HHMM only 4 characters, in 2.4 general
																// representation under TDRC identifier
			{ "original release year", "TOR", "TORY", "TDOR", "text", "single" }, // only 4 characters
			{ "recording dates", "TRD", "TRDA", "TDRC", "text", "single" },
			{ "original release time", "", "", "TDOR", "", "" },
			{ "recording time", "", "", "TDRC", "", "" },
			{ "release time", "", "", "TDRL", "", "" },
			{ "tagging time", "", "", "TDTG", "", "" },

			// encryption fields
			{ "audio encryption", "CRA", "AENC", "AENC", "", "" },
			{ "encryption method registration", "", "ENCR", "ENCR", "", "" },
			{ "encrypted meta frame", "CRM", "", "", "", "" },

			// picture field (inserted)
			{ "attached picture", "PIC", "APIC", "APIC", "", "" },

			// Database song identifier (inserted)
			{ "CD identifier", "MCI", "MCDI", "MCDI", "", "" },
			{ "international standard recording code", "TRC", "TSRC", "TSRC", "text", "single" }, // 12 characters!
			{ "unique file identifier", "UFI", "UFID", "UFID", "", "" },

			// synchronization and settings info
			{ "event timing codes", "ETC", "ETCO", "ETCO", "", "" },
			{ "audio seek point index", "", "", "ASPI", "", "" },
			{ "location lookup table", "MPEG", "MPEG", "MPEG", "", "" },
			{ "seek frame", "", "", "SEEK", "", "" },
			{ "recommended buffer size", "BUF", "RBUF", "RBUF", "", "" },
			{ "software/hardware and settings used for encoding", "TSS", "TSSE", "TSSE", "text", "single" },
			{ "playlist delay", "TDY", "TDLY", "TDLY", "text", "single" }, // should use ETC frame!
			{ "position synchronization frame", "", "POSS", "POSS", "", "" },

			// ???
			{ "general encapsulated object", "GEO", "GEOB", "GEOB", "", "" },
			{ "group identification registration", "", "GRID", "GRID", "", "" },
			{ "linked information", "LNK", "LINK", "LINK", "", "" },

			{ "popularimeter", "POP", "POPM", "POPM", "", "" },

			{ "set subtitle", "", "", "TSST", "", "" },
			{ "user defined text information frame", "TXX", "TXXX", "TXXX", "usertext", "vector" },
			{ "private frame", "", "PRIV", "PRIV", "", "" },

			{ "encoded by", "TEN", "TENC", "TENC", "text", "single" },
			{ "file type", "TFT", "TFLT", "TFLT", "text", "single" }, // check the formats,Combobox!

			{ "media type", "TMT", "TMED", "TMED", "text", "single" }, // look for the format!
			{ "mood", "", "", "TMOO", "", "" },
			// ???

			// owner and copyright
			{ "signature frame", "", "", "SIGN", "", "" },
			{ "ownership frame", "", "OWNE", "OWNE", "", "" }, // info on the seller
			{ "copyright message", "TCR", "TCOP", "TCOP", "text", "single" }, // preceded with copyright@

			// order informations
			{ "album sort order", "", "", "TSOA", "", "" },
			{ "performer sort order", "", "", "TSOP", "", "" },
			{ "title sort order", "", "", "TSOT", "", "" },

			// lyrics (inserted)
			{ "synchronized lyric/text", "SLT", "SYLT", "SYLT", "", "" },
			{ "synchronized tempo codes", "STC", "SYTC", "SYTC", "", "" },
			{ "unsynchronized lyric/text transcription", "ULT", "USLT", "USLT", "comm", "vector" },
			{ "language(s)", "TLA", "TLAN", "TLAN", "text", "single" }, // max three characters

			// commercial informations
			{ "publisher", "TPB", "TPUB", "TPUB", "text", "single" },
			{ "commercial frame", "", "COMR", "COMR", "", "" },

			// sound adjustements
			{ "equalisation (2)", "", "", "EQU2", "", "" },
			{ "equalization", "EQU", "EQUA", "EQU2", "", "" },
			{ "relative volume adjustment", "RVA", "RVAD", "RVA2", "", "" },
			{ "relative volume adjustment (2)", "", "", "RVA2", "", "" },
			{ "reverb", "REV", "RVRB", "RVRB", "", "" }
	};

	static {
		for (int i = 0; i < fieldsTable.length; i++) {
			fieldsString.put(fieldsTable[i][0], Integer.valueOf(i));
			fieldsId.put(fieldsTable[i][V22], Integer.valueOf(i));
			fieldsId.put(fieldsTable[i][V23], Integer.valueOf(i));
			fieldsId.put(fieldsTable[i][V24], Integer.valueOf(i));
		}
		// some other fields have to be inserted ... for example those fields
		// that in 2.3 has been substituted by another field in 2.4, insert
		// them in both sides!

		// now that all the fields has been defined, put them into the table
		// create the hashes and put into the type field
		// ("comment","text","unsupported")
		// and also the type ("vector","single")
	}

	// only used to get empty configuration objects!
	private final static Mp3info utilmp3 = new Mp3info();

	public static Id3v2elem getConfigObject(String str) {
		// if (str.startsWith("official artist/performer"))
		// System.out.println("ciao");
		return utilmp3.id3v2.getElem(str);
	}

	private static Hashtable<String, String[]> settableFields = new Hashtable<String, String[]>();
	private static Hashtable<String, String[]> tableFields = new Hashtable<String, String[]>();
	static {
		settableFields.put("comment", new String[] { "explain", "language", "value" });
		settableFields.put("user url", new String[] { "explain", "value" });
		tableFields.put("comment", new String[] { "Explanation", "Language", "Comment" });
		tableFields.put("user url", new String[] { "Explanation", "User url" });
	}

	public final static String[] getSettableFields(String str) {
		String ret[] = (String[]) settableFields.get(str);
		return ret;
	}

	public final static String[] getTableFields(String str) {
		String ret[] = (String[]) tableFields.get(str);
		return ret;
	}

	private final static String stringend = new String(new char[] { (char) 0 });

	private static Hashtable<String, String> fieldsAlias = new Hashtable<String, String>();
	private static Hashtable<String, String> aliasfields = new Hashtable<String, String>();

	final static String getFieldType(String str) {
		String orig = getOrigField(str);
		if (orig == null)
			return orig;
		int index = ((Integer) fieldsString.get(str)).intValue();
		return fieldsTable[index][TYPE];
	}

	final static String getOrigField(String str) {
		String ret = (String) fieldsAlias.get(str);
		if (ret == null)
			return str;
		return ret;
	}

	final static String getAliasField(String str) {
		String ret = (String) fieldsAlias.get(str);
		if (ret == null)
			return str;
		return ret;
	}

	final static boolean setFieldAlias(String name, String alias) {
		StringBuffer err = new StringBuffer();
		if (!fieldsString.containsKey(name))
			err.append("Field " + name + " not contained!\n");
		if (fieldsString.containsKey(alias))
			err.append("Field " + alias + " already contained in original field names!\n");
		if (fieldsAlias.containsKey(alias) && !((String) fieldsAlias.get(alias)).equals(name))
			err.append("Error, trying to associate the same alias to two different original fields!\n");

		if (err.length() > 0)
			return false;
		else {
			fieldsAlias.put(alias, name);
			aliasfields.put(name, alias);
		}
		return true;
	}

	final static boolean setFieldAlias(String fields[][]) {
		StringBuffer err = new StringBuffer();
		for (int i = 0; i < fields.length; i++) {
			if (!fieldsString.containsKey(fields[i][0]))
				err.append("Field " + fields[i][0] + " not contained!\n");
			if (fieldsString.containsKey(fields[i][1]))
				err.append("Field " + fields[i][0] + " already contained in original field names!\n");
			if (fieldsAlias.containsKey(fields[i][1]))
				System.out.println("WARNING! " + fields[i][0] + " was already an active alias name, check if!\n");
		}
		if (err.length() > 0) {
			System.out.println(err);
			return false;
		} else {
			for (int i = 0; i < fields.length; i++) {
				aliasfields.put(fields[i][0], fields[i][1]);
				fieldsAlias.put(fields[i][1], fields[i][0]);
			}
			return true;
		}
	}

	private static Hashtable<String, Id3v2frameflags> flagsconfig = new Hashtable<String, Id3v2frameflags>();

	final static boolean setFlagsConfigObject(String id, Id3v2frameflags obj) {
		if (fieldsAlias.containsKey(id) || fieldsString.containsKey(id)) {
			flagsconfig.put(id, obj);
			return true;
		} else {
			System.out.println("Unexistent field " + id + " setting flags config!");
			return false;
		}
	}

	final static Id3v2frameflags getFlagsConfigObject(String id) {
		String str = null;
		if (id.trim().length() == 0)
			return null;
		if (fieldsAlias.containsKey(id) || fieldsString.containsKey(id))
			str = id;

		if (str == null) {
			System.out.println("id not found " + id);
			return null;
		}
		return (Id3v2frameflags) flagsconfig.get(str);
	}

	final static void printAlias() {
		Enumeration<String> hash_keys = fieldsAlias.keys();
		// to be changed with elem.getLength, sommo tutti i valori e vedo se
		// supero real_sync_start ...in quel caso riscrivo tutto il file!
		while (hash_keys.hasMoreElements()) {
			String field_id = (String) hash_keys.nextElement();
			System.out.println("Orig field: " + (String) fieldsAlias.get(field_id) + " alias: " + field_id);
		}
	}

	private static Hashtable<String, String[]> fieldGroups = new Hashtable<String, String[]>();

	final static boolean setFieldGroup(String name, String fields[]) {
		StringBuffer err = new StringBuffer();
		for (int i = 0; i < fields.length; i++) {
			if (!(fieldsAlias.containsKey(fields[i]) || fieldsString.containsKey(fields[i])))
				err.append("Field " + fields[i] + " not contained!\n");
		}
		if (err.length() > 0) {
			System.out.println(err.toString());
			return false;
		} else
			fieldGroups.put(name, fields);
		return true;
	}

	final static String[] getFieldGroup(String id) {
		// check if the fields contained in the string array are supported before
		// return them back!
		if (fieldGroups.containsKey(id)) {
			String tmp[] = (String[]) fieldGroups.get(id);
			ArrayList<String> ret = new ArrayList<String>();
			String read = "";
			for (int i = 0; i < tmp.length; i++) {
				read = tmp[i];
				int index = 0;
				if (fieldsString.containsKey(read)) {
					index = ((Integer) fieldsString.get(read)).intValue();
				} else {
					String read2 = (String) (fieldsAlias.get(read));
					if (read2 == null) {
						System.out.println("Wrong id group" + id);
						return null;
					}
					index = ((Integer) fieldsString.get(read2)).intValue();
				}
				// here is done the check for the supported fields!
				if (fieldsTable[index][TYPE].length() != 0)
					ret.add(read);
			}
			String sret[] = new String[ret.size()];
			for (int i = 0; i < sret.length; i++)
				sret[i] = (String) ret.get(i);
			return sret;
		} else {
			System.out.println("Wrong id group" + id);
			return null;
		}
	}

	static {
		// create my group fields
		String tmp[] = null;
		tmp = new String[] { "comment", "album", "genre", "title", "year", "track", "artist" };
		setFieldGroup("id3v1", tmp);

		// Database informations
		tmp = new String[] {
				"CD identifier", "international standard recording code",
				"unique file identifier" };
		setFieldGroup("Database", tmp);

		// original song informations
		tmp = new String[] {
				"lyricist/text writer", "original artist(s)/performer(s)",
				"original album/movie/show title", "original lyricist(s)/text writer(s)",
				"content group description", "initial key",
				"original filename", "subtitle/description refinement",
				"file owner/license", "musician credits list", "involved people list",
				"publisher" };
		setFieldGroup("detailed song info", tmp);

		tmp = new String[] {
				"composer", "band/orchestra/accompaniment",
				"conductor/performer refinement", "interpreted, remixed or modified by",
				"part of a set" };
		setFieldGroup("interpreters", tmp);

		// urls
		tmp = new String[] {
				"official artist/performer webpage", "official audio source webpage",
				"official audio file webpage", "commercial information",
				"publishers official webpage",
				"official internet radio station homepage",
				"internet radio station name", "internet radio station owner",
				"copyright/legal information", "payment" };
		setFieldGroup("www links", tmp);

		tmp = new String[] { "attached picture" };
		setFieldGroup("pictures", tmp);

		// lyrics
		tmp = new String[] {
				"unsynchronized lyric/text transcription", "synchronized lyric/text",
				"synchronized tempo codes", "language(s)" };
		setFieldGroup("lyrics", tmp);

		tmp = new String[] {
				"play counter", "beats per minute", "length",
				"terms of use", "encoded by", "file type", "media type" };
		// encoded by, file type and media type are comboboxes, check the type!!!
		setFieldGroup("song character", tmp);

		tmp = new String[] {
				"date", "time", "original release year",
				"recording dates", "original release time", "recording time",
				"release time", "encoding time", "tagging time" };
		setFieldGroup("time fields", tmp);

		tmp = new String[] {
				"equalization", "equalisation (2)",
				"relative volume adjustment", "relative volume adjustment (2)",
				"reverb" };
		setFieldGroup("sound adjust", tmp);

		tmp = new String[] {
				"album sort order", "title sort order",
				"performer sort order" };
		setFieldGroup("sort order", tmp);

		tmp = new String[] {
				"audio encryption", "encryption method registration",
				"encrypted meta frame" };
		setFieldGroup("encryption", tmp);

		tmp = new String[] {
				"event timing codes", "audio seek point index",
				"location lookup table", "seek frame",
				"recommended buffer size",
				"software/hardware and settings used for encoding",
				"playlist delay", "position synchronization frame" };
		setFieldGroup("syncronization", tmp);

		// owner and copyright
		tmp = new String[] {
				"signature frame", "ownership frame",
				"copyright message" };
		setFieldGroup("owner copyright", tmp);

		// other fields
		tmp = new String[] {
				"general encapsulated object",
				"group identification registration", "linked information",
				"popularimeter", "mood" };
		setFieldGroup("other", tmp);

		tmp = new String[] {
				"set subtitle", "private frame",
				"user defined text information frame" };
		setFieldGroup("user private", tmp);

		// commercial informations
		// {"publisher","TPB","TPUB","TPUB","text","single"},
		// {"commercial frame","","COMR","COMR","",""}
	}

	private final static Hashtable<String, String> tagv1fieldshash = new Hashtable<String, String>();
	static {
		String tmp[] = new String[] { "artist", "title", "album", "year", "genre", "comment", "track" };
		for (int i = 0; i < tmp.length; i++)
			tagv1fieldshash.put(tmp[i], "");
	}

	final static int FORCED = 0;
	final static int UNFORCED = 1;

	public class id3tagv2 {
		/*
		 * private class Id3v2Comparator implements Comparator {
		 * public int compare(Object fir, Object sec) {
		 * if (fir instanceof String || sec instanceof String) {
		 * System.out.println(fir + " sec " + sec);
		 * }
		 * id3v2_elem a = (id3v2_elem) fir;
		 * id3v2_elem b = (id3v2_elem) sec;
		 * if (a.index >= b.index)
		 * return 1;
		 * else
		 * return -1;
		 * }
		 * }
		 */

		int version = -1;
		boolean exists = false;
		// hash from wich you can get elem from field identifier
		TreeMap<String, id3v2_elem> hash_elem = new TreeMap<String, id3v2_elem>();// new Id3v2Comparator());
		// contains the association between the field used in the setElem function
		// and the corresponing code used in the standard, for example
		// "comment"->"COMM",
		// "artist"->"TPE1" and so on
		int total_fields_length = 0;
		int unsynchronization;
		int kompression;
		int extended_header;
		int experimental_bit;
		int footer;

		final static int DEFAULT_VERSION = 3;

		private Hashtable<String, String> unsupported = new Hashtable<String, String>();
		private Hashtable<String, String> lostFields = new Hashtable<String, String>();

		public void removeLostFields() {
			lostFields = new Hashtable<String, String>();
		}

		public void removeUnsupportedFields() {
			unsupported = new Hashtable<String, String>();
		}

		public void addConvUnsupported(String fld) {
			unsupported.put(fld, "1");
		}

		public void addConvLost(String fld) {
			lostFields.put(fld, "1");
		}

		// returns all the existend fields that are not present in tag v1
		// and are supported
		public String[] getOtherFields() {
			ArrayList<String> arr = new ArrayList<String>();
			id3v2_elem elem = null;
			Set<Map.Entry<String, Mp3info.id3tagv2.id3v2_elem>> set = hash_elem.entrySet();
			Iterator<Map.Entry<String, Mp3info.id3tagv2.id3v2_elem>> iterator = set.iterator();
			String str = null;
			while (iterator.hasNext()) {
				Map.Entry<String, Mp3info.id3tagv2.id3v2_elem> hashpair = (Map.Entry<String, Mp3info.id3tagv2.id3v2_elem>) iterator
						.next();
				elem = (id3v2_elem) hashpair.getValue();
				if (!(elem instanceof id3v2_unsupported)) {
					// field_ID is the symbol of the field (COMM, TPE1, ...)
					Integer integ = (Integer) fieldsId.get(elem.field_ID);
					if (integ != null) {
						int index = integ.intValue();
						str = fieldsTable[index][NAMES];
						if (!tagv1fieldshash.containsKey(str))
							arr.add(str);
					}
				}
			}
			String ret[] = new String[arr.size()];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = (String) arr.get(i);
			}
			return ret;
		}

		// returns all the unsupported fields that are not present in tag v1
		public String[] getOtherUnsupportedFields() {
			id3v2_elem elem = null;
			ArrayList<String> arr = new ArrayList<String>();
			Set<Map.Entry<String, Mp3info.id3tagv2.id3v2_elem>> set = hash_elem.entrySet();
			Iterator<Map.Entry<String, Mp3info.id3tagv2.id3v2_elem>> iterator = set.iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Mp3info.id3tagv2.id3v2_elem> hashpair = (Map.Entry<String, Mp3info.id3tagv2.id3v2_elem>) iterator
						.next();
				elem = (id3v2_elem) hashpair.getValue();
				if (elem instanceof id3v2_unsupported) {
					// field_ID is the symbol of the field (COMM, TPE1, ...)
					Integer integ = (Integer) fieldsId.get(elem.field_ID);
					if (integ != null) {
						int index = integ.intValue();
						arr.add(fieldsTable[index][NAMES]);
					}
				}
			}
			String ret[] = new String[arr.size()];
			for (int i = 0; i < ret.length; i++)
				ret[i] = (String) arr.get(i);
			return ret;
		}

		public String[] getSupportedVersions(String fld) {
			Integer row = (Integer) fieldsString.get(fld);
			if (row == null)
				return new String[0];
			int index = row.intValue();
			ArrayList<String> tmp = new ArrayList<String>();
			if (!fieldsTable[index][V22].equals("")) {
				tmp.add("2.2");
			}
			if (!fieldsTable[index][V23].equals("")) {
				tmp.add("2.3");
			}
			if (!fieldsTable[index][V24].equals("")) {
				tmp.add("2.4");
			}
			String ret[] = new String[tmp.size()];
			for (int i = 0; i < ret.length; i++)
				ret[i] = (String) tmp.get(i);
			return ret;
		}

		String[] getUnsupportedFields() {
			String ret[] = new String[unsupported.size()];
			Enumeration<String> hash_keys = unsupported.keys();
			int i = 0;
			while (hash_keys.hasMoreElements()) {
				ret[i] = (String) hash_keys.nextElement();
				i++;
			}
			return ret;
		}

		String[] getlostFields() {
			String ret[] = new String[lostFields.size()];
			Enumeration<String> hash_keys = lostFields.keys();
			int i = 0;
			while (hash_keys.hasMoreElements()) {
				ret[i] = (String) hash_keys.nextElement();
				i++;
			}
			return ret;
		}

		boolean convertVersion(int vers, int mode) {
			if (mode != FORCED && mode != UNFORCED)
				return false;

			if (vers == version) {
				return true;
			} else if (vers == 2) {
				// no conversion to old deprecated versions!
				return false;
			}
			// converts all the fields to the new fields .. this means that
			// the field length has to be changed to that of the new version!
			// if some of them are no more supported in the new version,
			// they set an error and return false!
			// call the convert function to every element in hash_elem ...
			// every one will check if it is supported in the new version and
			// append the error to the buffer!
			Set<Map.Entry<String, Mp3info.id3tagv2.id3v2_elem>> set = hash_elem.entrySet();
			Iterator<Map.Entry<String, Mp3info.id3tagv2.id3v2_elem>> iterator = set.iterator();

			boolean ret = true;
			// if the mode is FORCED, than this function has to remove
			// the elements that return false!!! else, this function
			// has to update the error object by inserting the fields
			// that should be deleted!
			// the single elements will put into the error object the
			// field identifier and the supporting version(s)
			while (iterator.hasNext()) {
				Map.Entry<String, Mp3info.id3tagv2.id3v2_elem> elem = (Map.Entry<String, Mp3info.id3tagv2.id3v2_elem>) iterator
						.next();
				String field_id = (String) elem.getKey();
				if (!((id3v2_elem) elem.getValue()).convertVersion(vers, mode)) {
					ret = false;
					if (mode == FORCED)
						hash_elem.remove(field_id);
				}
			}
			if (mode == FORCED)
				version = vers;
			return ret;
		}

		public String getVersionString() {
			if (version == 2)
				return "2.2";
			else if (version == 3)
				return "2.3";
			else if (version == 4)
				return "2.4";
			return "none";
		}

		public int getVersion() {
			if (version == 2)
				return 2;
			else if (version == 3)
				return 3;
			else if (version == 4)
				return 4;
			return -1;
		}

		private int getVersionTableIndex(int vers) {
			if (vers == 2)
				return V22;
			else if (vers == 3)
				return V23;
			else if (vers == 4)
				return V24;
			return -1;
		}

		// ADD ANOTHER TYPE OF FRAME, modify this part of the code to generate
		// the correct object!
		public id3v2_elem gimmeid3v2_array(String str) {
			String origstr = getOrigField(str);
			Integer integer = (Integer) fieldsString.get(origstr);
			if (integer == null) {
				System.out.println("Wrong string id " + str + " in function gimme array!");
				return null;
			}
			int index = integer.intValue();
			String type = fieldsTable[index][TYPE];
			// field identifier, 3 or 4 characters ...
			int vers = getVersionTableIndex(version);
			if (vers == -1)
				vers = DEFAULT_VERSION;
			String id3v2_id = fieldsTable[index][vers];
			// String numer = fieldsTable[index][NUMEROSITY];

			id3v2_array tmp = new id3v2_array();
			tmp.arrayfields.fieldName = str;
			tmp.field_ID = id3v2_id;

			if (type.equals("comm")) {
				tmp.arrayfields.cloneobj = new Id3v2comment();
				((Id3v2elem) tmp.arrayfields.cloneobj).fieldName = str;
				tmp.cloneobj = new id3v2_comment();
				tmp.cloneobj.field_ID = id3v2_id;
			} else if (type.equals("userurl")) {
				tmp.arrayfields.cloneobj = new Id3v2userurl();
				((Id3v2elem) tmp.arrayfields.cloneobj).fieldName = str;
				tmp.cloneobj = new id3v2_userurl();
				tmp.cloneobj.field_ID = id3v2_id;
			} else {
				tmp.arrayfields.cloneobj = new Id3v2elem();
				((Id3v2elem) tmp.arrayfields.cloneobj).fieldName = str;
				tmp.cloneobj = new id3v2_unsupported();
				tmp.cloneobj.field_ID = id3v2_id;
			}
			return tmp;
		}

		public id3v2_elem gimmeid3v2_elem(String str) {
			String origstr = getOrigField(str);
			Integer integer = (Integer) fieldsString.get(origstr);
			if (integer == null) {
				System.out.println("Wrong field " + str + "in function gimme leme!");
				return null;
			}
			int index = integer.intValue();
			String type = fieldsTable[index][TYPE];
			// field identifier, 3 or 4 characters ...
			int vers = getVersionTableIndex(version);
			if (vers == -1)
				vers = DEFAULT_VERSION;
			String id3v2_id = fieldsTable[index][vers];

			// str is the field_identifier ...
			if (type.equals("text")) {
				id3v2_elem tmp_elem = new id3v2_elem();
				tmp_elem.field_ID = id3v2_id;
				tmp_elem.data.fieldName = str;
				return tmp_elem;
			} else if (type.equals("comm")) {
				id3v2_comment tmp_elem = new id3v2_comment();
				tmp_elem.field_ID = id3v2_id;
				tmp_elem.data.fieldName = str;
				return tmp_elem;
			} else if (type.equals("userurl")) {
				id3v2_userurl tmp_elem = new id3v2_userurl();
				tmp_elem.field_ID = id3v2_id;
				tmp_elem.data.fieldName = str;
				return tmp_elem;
			} else if (type.equals("url")) {
				id3v2_url tmp_elem = new id3v2_url();
				tmp_elem.field_ID = id3v2_id;
				tmp_elem.data.fieldName = str;
				return tmp_elem;
			} else if (type.equals("usertext")) {
				id3v2_usertext tmp_elem = new id3v2_usertext();
				tmp_elem.field_ID = id3v2_id;
				tmp_elem.data.fieldName = str;
				return tmp_elem;
			} else {
				id3v2_unsupported tmp_elem = new id3v2_unsupported();
				tmp_elem.field_ID = id3v2_id;
				tmp_elem.data.fieldName = str;
				return tmp_elem;
			}
		}

		// to be debugged!
		private byte[] undoUnsyncronization(byte src[]) {
			// if 0xff00xx is found this must become 0xffxx!
			int len = src.length;
			int count = 0;
			byte ret[] = new byte[len];
			for (int i = 0; i < len - 2;) {
				if (((int) src[i] & 0xff) == 0xff && src[i + 1] == 0) {
					ret[count] = src[i];
					ret[count + 1] = src[i + 2];
					i += 3;
					count += 2;
				} else {
					ret[count] = src[i];
					i++;
					count++;
				}

			}
			for (; count < ret.length; count++)
				ret[count] = 0;
			return ret;
		}

		// to be debugged!
		private byte[] unsyncronize(byte src[]) {
			int len = src.length;
			int count = 0;
			for (int i = 0; i < src.length - 1; i++) {
				if (((int) src[i] & 0xff) == 0xff && (((int) src[i + 1] & 0xff) >= 0xe0 || src[i + 1] == 0))
					count++;
			}
			byte ret[] = new byte[len + count];
			count = 0;
			for (int i = 0; i < src.length - 1; i++) {
				if (((int) src[i] & 0xff) == 0xff && (((int) src[i + 1] & 0xff) >= 0xe0 || src[i + 1] == 0)) {
					ret[count] = src[i];
					ret[count + 1] = 0;
					ret[count + 2] = src[i + 1];
					count += 3;
					i += 2;
				} else {
					ret[count] = src[i];
					ret[count + 1] = src[i + 1];
					count += 2;
					i += 2;
				}
			}
			return ret;
		}

		public class id3v2_elem {
			Id3v2elem data = null;
			String field_ID = "";
			// added to order the elements, a comparator considers this index to order
			// the elems in the hash_keys !!!
			int index = 0x7fffffff;
			Id3v2frameflags flags = new Id3v2frameflags();

			id3v2_elem() {
				data = new Id3v2elem();
			}

			int length = 0; // length of the field, 10 bytes of header included!
			int posonfile = -1; // if it remains -1 the field is in memory, else has been read!

			id3v2_elem getClone() {
				id3v2_elem tmp = new id3v2_elem();
				tmp.copy(this);
				return tmp;
			}

			void copy(id3v2_elem elem) {
				field_ID = elem.field_ID;
				flags = elem.flags;
			}

			Id3v2elem getData() {
				return data;
			}

			void setData(Object obj) {
				data = (Id3v2elem) obj;
			}

			boolean leave_field_on_file(id3v2_elem tmp) {
				try {
					return false;
					/*
					 * // length has already been stored!
					 * int tableindex=((Integer)fieldsId.get(field_ID)).intValue();
					 * String type=fieldsTable[tableindex][TYPE];
					 * if (type.length()==0)
					 * return true;
					 * if (length>5000)
					 * return true;
					 * return false;
					 */
				} catch (Exception e) {
					return true;
				}
			}

			// this function is useful only before writing the tag to disk ...
			// so this function could calculate the real length of the field,
			// considering string encoding and synchronization ...
			int total_field_length() {
				return length + field_header_length(version);
			}

			// this string could take into account the text encoding!
			void update_length() {
				length = data.getValue().length() + 1;
			}

			void clear() {
				(getData()).clear();
				update_length();
			}

			boolean convertVersion(int vers, int mode) {
				// check if the field exists in the new version
				// if so and mode is FORCED, do it!
				// else put the field id in the lostFields hash!
				Integer row = (Integer) fieldsId.get(field_ID);
				if (row == null)
					return false;
				int index = row.intValue();
				String new_id = fieldsTable[index][getVersionTableIndex(vers)];
				if (!new_id.equals("")) {
					if (mode == FORCED) {
						// convert the length, the field_ID, the flag bits
						length = data.getValue().length() + 1 + field_header_length(vers);
						field_ID = new_id;
						return true;
					} else
						return true;
				} else
					return false;
			}

			void set_flags_by_config(String field) {
				Id3v2frameflags tmp = getFlagsConfigObject(field);
				if (tmp == null) {
					flags.tag_alter_pres = 0;
					flags.file_alter_pres = 0;
					flags.read_only = 0;
					flags.group_identity = 0;
					flags.kompression = 0;
					flags.encryption = 0;
					flags.unsynchronization = 0;
					flags.data_length = 0;
				} else if (tmp.leave_existent) {
				} else if (tmp.always_these) {
					flags.copy(getFlagsConfigObject(field));
				}
			}

			// equal for every frame, do not override!
			void read_flags(byte byte_buf[], int pos) {
				if (version == 3) {
					flags.tag_alter_pres = (byte_buf[pos + 8] & 0x80);
					flags.file_alter_pres = (byte_buf[pos + 8] & 0x40);
					flags.read_only = (byte_buf[8] & 0x20);
					flags.kompression = (byte_buf[9] & 0x80);
					flags.encryption = (byte_buf[pos + 9] & 0x40);
					flags.group_identity = (byte_buf[pos + 9] & 0x20);
				} else if (version == 4) {
					flags.tag_alter_pres = (byte_buf[pos + 8] & 0x40);
					flags.file_alter_pres = (byte_buf[pos + 8] & 0x20);
					flags.read_only = (byte_buf[pos + 8] & 0x10);
					flags.group_identity = (byte_buf[pos + 9] & 0x40);
					flags.kompression = (byte_buf[pos + 9] & 0x08);
					flags.encryption = (byte_buf[pos + 9] & 0x04);
					flags.unsynchronization = (byte_buf[pos + 9] & 0x02);
					flags.data_length = (byte_buf[pos + 9] & 0x01);
				}
			}

			void write_flags(byte byte_buf[], int pos) {
				if (version == 3) {
					byte_buf[pos + 8] = (byte) 0;
					byte_buf[pos + 9] = (byte) 0;
					int towrite = (flags.tag_alter_pres << 15) | (flags.file_alter_pres << 14)
							| (flags.read_only << 13) | (flags.kompression << 7)
							| (flags.encryption << 6) | (flags.group_identity << 5);
					byte_buf[pos + 8] = (byte) ((towrite & 0xff00) >> 8);
					byte_buf[pos + 9] = (byte) (towrite & 0xff);
				} else if (version == 4) {
					int towrite = (flags.tag_alter_pres << 14) | (flags.file_alter_pres << 13)
							| (flags.read_only << 12) | (flags.group_identity << 6)
							| (flags.kompression << 3) | (flags.encryption << 2)
							| (flags.unsynchronization << 1) | (flags.data_length);
					byte_buf[pos + 8] = (byte) ((towrite & 0xff00) >> 8);
					byte_buf[pos + 9] = (byte) (towrite & 0xff);
				}
			}

			byte[] get_encoded_text_string(String str) {
				// encodes the string str (with terminator if exists)
				// and writes it to buf starting from byte pos
				// the number of written bytes is returned
				if (flags.text_encoding == 0 || flags.text_encoding == 3) {
					return Utils.getBytes(str);
				} else if (flags.text_encoding == 1 || flags.text_encoding == 3) {
					// add the big endian or little endian write mode!
					boolean bigendian = true;
					int pos = 0;
					byte tmp[] = null;
					if (flags.text_encoding == 1) {
						tmp = new byte[str.length() * 2 + 2];
						pos = 2;
					} else
						tmp = new byte[str.length() * 2];

					for (int i = pos; i < str.length(); i += 2) {
						if (bigendian) {
							tmp[i] = (byte) ((((int) str.charAt(i)) >> 8) & 0xff);
							tmp[i + 1] = (byte) (((int) str.charAt(i)) & 0xff);
						}
					}
					return tmp;
				} else
					return new byte[0];
			}

			// buf is the field content, pos is where the string have to start
			int get_decoded_text_string(byte buf[], int pos, StringBuffer str) {
				if (pos > (buf.length - 1))
					return 0;
				str.setLength(0);
				int end = 0;
				int i = 0;

				boolean terminated = false;
				if (flags.text_encoding == 1 || flags.text_encoding == 2) // unicode
				{
					for (i = pos; i < buf.length - 1; i++)
						if (buf[i] == 0 && buf[i + 1] == 0) {
							end = i + 2;
							terminated = true;
							break;
						}
				} else if (flags.text_encoding == 0 || flags.text_encoding == 3) {
					for (i = pos; i < buf.length; i++)
						if (buf[i] == 0) {
							end = i + 1;
							terminated = true;
							break;
						}
				} else {
					System.out.println("Wrong text encoding " + flags.text_encoding);
				}

				if (end == 0)
					end = i;

				int len = end - pos;
				if (flags.text_encoding == 1 || flags.text_encoding == 2) // unicode
				{
					// be careful ... the first two bytes are fffe or feff to identify
					// big endian or little endian mode!
					int count = 0;
					// big endian or little endian
					boolean bigendian = true;

					if (flags.text_encoding == 1) {
						if (buf[pos] == 0xff && buf[pos + 1] == 0xfe)
							bigendian = true;
						else if (buf[pos] == 0xfe && buf[pos + 1] == 0xff)
							bigendian = false;
					}
					for (i = pos + 1; i < end; i += 2) {
						if (bigendian)
							str.setCharAt(count, (char) ((((int) buf[i]) << 8) + (((int) buf[i + 1]) & 0xff)));
						else
							str.setCharAt(count, (char) ((((int) buf[i + 1]) << 8) + (((int) buf[i]) & 0xff)));
						count++;
					}
					return (len);
				} else {
					str.append(new String(buf, pos, len));
					return len;
				}
			}

			byte[] read_header_and_get_field_content(byte buf[], int pos) {
				int head_len = field_header_length(version);
				int field_len = id3v2_read_field_length(buf, pos);
				length = field_len;
				read_flags(buf, pos);
				// set the field ID, 3 or 4 characters
				field_ID = id3v2_read_field_ID(buf, pos);
				if (leave_field_on_file((id3v2_elem) this)) {
					posonfile = pos;
					return null;
				} else {
					// after flags are set, if sync bit is set do unsync!
					byte tmpb[] = new byte[field_len];
					System.arraycopy(buf, pos + head_len, tmpb, 0, field_len);
					if (this.flags.unsynchronization == 1)
						tmpb = undoUnsyncronization(tmpb);
					return tmpb;
				}
			}

			void read_field(byte buf[], int pos) {
				byte tmpb[] = read_header_and_get_field_content(buf, pos);
				// tmpb now contains only the field pure bytes!
				if (tmpb != null) {
					flags.text_encoding = (int) tmpb[0];
					StringBuffer str = new StringBuffer();
					get_decoded_text_string(tmpb, 1, str);
					data.setValue(str.toString());
					if (miodebug > READDEBUG) {
						System.out.println(" read value " + data.getValue());
					}
				}
			}

			Id3v2elem get_elem() {
				return data.getConfigObject();
			}

			boolean set_elem(Object str) {
				if (!(str.getClass()).equals(String.class)) {
					try {
						data.copy((Id3v2elem) str);
						return true;
					} catch (Exception e) {
						System.out.println("Class cast exception!");
						return false;
					}
				}
				// check if the field is supported in this id3v2 version!
				// if it is not, add the field to the unsupported strings for
				// actual version!

				// 5 more bytes, 3 for the language, two for the zeroes
				data.setValue((String) str);
				// 2 more bytes, one for the language, two for the zeroes
				update_length();
				posonfile = -1;
				return true;
			}

			byte[] get_header_and_frame_bytes(byte content[]) {
				if (posonfile != -1) {
					// id3v2_write_field_ID(tmptag,pos,field_ID);
					// return get_frame_bytes_from_file((id3v2_elem)this,content,pos,posonfile);
					return null;
				} else {
					// if unsynchronization, encryption and so on must be done,
					// do them now. Get the flag from configuration, if
					// no configuration is declared, do not do unsync
					if (flags.unsynchronization == 1) {
						content = unsyncronize(content);
					}

					int pos = 0;
					byte[] ret = new byte[content.length + field_header_length(version)];
					id3v2_write_field_ID(ret, pos, field_ID);
					// write the flags reading them from configuration if needed
					write_flags(ret, pos);

					id3v2_write_field_length(ret, pos, content.length);
					pos += field_header_length(version);

					// here unsynchronization could be done if the flag is set ...
					// this would also influence the field length ...

					System.arraycopy(content, 0, ret, pos, content.length);
					// remember that if the field has not been loaded it must be read from the file!
					return (ret);
				}
			}

			byte[] get_frame_bytes() {
				Id3v2elem tmpdata = getData();
				if (tmpdata.isEmpty())
					return new byte[0];
				set_flags_by_config(tmpdata.fieldName);
				// if the field has not been stored but is readable from the file,
				// read it from there calling a function
				if (posonfile != -1) {
					// return write_header_and_field_content (null);
					return null;
				} else {
					byte tmparr[] = null;
					byte content[] = null;
					// writes the field length, flags and value in vector vet starting from pos!
					// text type get the string encoded if necessary!
					tmparr = get_encoded_text_string(data.getValue());
					content = new byte[tmparr.length + 1];
					content[0] = (byte) flags.text_encoding;
					System.arraycopy(tmparr, 0, content, 1, tmparr.length);
					return get_header_and_frame_bytes(content);
				}
			}
		} // id3v2_elem

		public class id3v2_comment extends id3v2_elem {
			private Id3v2comment data = null;

			id3v2_comment() {
				data = new Id3v2comment();
			}

			id3v2_elem getClone() {
				id3v2_comment tmp = new id3v2_comment();
				tmp.copy(this);
				return tmp;
			}

			Id3v2elem getData() {
				return data;
			}

			void setData(Object obj) {
				data = (Id3v2comment) obj;
			}

			Id3v2elem get_elem() {
				return data.getConfigObject();
			}

			// no more important!
			void update_length() {
				length = (data.getValue() + data.getElem("explain") + data.getElem("language")).length() + 2;
			}

			void clear() {
				(getData()).clear();
				update_length();
			}

			// fields read_field and write_field are overwritten!
			void read_field(byte buf[], int pos) {
				byte tmpb[] = read_header_and_get_field_content(buf, pos);
				if (tmpb != null) {
					StringBuffer str = new StringBuffer();
					flags.text_encoding = (int) tmpb[0];
					if (tmpb.length > 3) {
						data.setElem("language", new String(tmpb, 1, 4));
						int fld_pos = 4;
						fld_pos += get_decoded_text_string(tmpb, fld_pos, str);
						data.setElem("explain", str.toString());
						fld_pos += get_decoded_text_string(tmpb, fld_pos, str);
						data.setValue(str.toString());
					}
					if (miodebug > READDEBUG) {
						System.out.println(" lan " + data.getElem("language") + " expl " + data.getElem("explain")
								+ " read value " + data.getValue());
					}
				}
			}

			boolean set_elem(Object str) {
				if (!(str.getClass()).equals(String.class)) {
					try {
						data.copy((Id3v2comment) str);
						return true;
					}
					// set an error ... or print it!
					catch (Exception e) {
						System.out.println("Cast exception, wrong configuration object");
						return false;
					}
				}
				data.setValue((String) str);
				// 5 more bytes, 3 for the language, two for the zeroes
				update_length();
				posonfile = -1;
				return true;
			}

			byte[] get_frame_bytes() {
				Id3v2elem tmpdata = getData();
				if (tmpdata.isEmpty())
					return new byte[0];
				set_flags_by_config(tmpdata.fieldName);
				if (posonfile != -1) {
					// return write_header_and_field_content (tmptag,pos,null);
					return null;
				} else {
					byte tmparr[] = null;
					byte content[] = null;
					// writes the field length, flags and value in vector vet starting from pos!
					// text type get the string encoded if necessary!
					tmparr = get_encoded_text_string((String) data.getElem("language")
							+ (String) data.getElem("explain") + stringend + (String) data.getValue());
					content = new byte[tmparr.length + 1];
					content[0] = (byte) flags.text_encoding;
					System.arraycopy(tmparr, 0, content, 1, tmparr.length);
					return get_header_and_frame_bytes(content);
				}
			}

			/*
			 * int write_field (byte tmptag[],int pos)
			 * {
			 * // if pos on file !=-1 call the function write_header_and_field_content(byte)
			 * // else:
			 * // if text encoding should be applied, call the function
			 * // encode text (String texttoencode) that returns a byte array encoded based
			 * // on the flags.text_encoding value, call it for every
			 * // should be redone ... call write_header_and_field_content(byte)
			 * if (posonfile!=-1)
			 * {
			 * // return write_header_and_field_content (tmptag,pos,null);
			 * return 0;
			 * }
			 * else
			 * {
			 * byte tmparr[]=null;
			 * tmparr=(new String(new char[] {(char)flags.text_encoding})+
			 * data.getElem("language")+data.getElem("explain")+
			 * stringend+data.getValue()).getBytes();
			 * if (miodebug>WRITEDEBUG)
			 * System.out.println(" wrote field  "+data.getValue());
			 * return write_header_and_field_content (tmptag,pos,tmparr);
			 * }
			 * }
			 */
		} // id3v2_comment

		public class id3v2_url extends id3v2_elem {
			private Id3v2elem data = null;

			id3v2_url() {
				data = new Id3v2elem();
			}

			id3v2_elem getClone() {
				id3v2_elem tmp = new id3v2_elem();
				tmp.copy(this);
				return tmp;
			}

			Id3v2elem getData() {
				return data;
			}

			void setData(Object obj) {
				data = (Id3v2elem) obj;
			}

			Id3v2elem get_elem() {
				return data.getConfigObject();
			}

			// no more important!
			void update_length() {
				length = (data.getValue() + data.getElem("explain")).length() + 2; // text encoding more the second
																					// zero!
			}

			void clear() {
				(getData()).clear();
				update_length();
			}

			// fields read_field and write_field are overwritten!
			void read_field(byte buf[], int pos) {
				byte tmpb[] = read_header_and_get_field_content(buf, pos);
				if (tmpb != null) {
					data.setValue(new String(tmpb));
					if (miodebug > READDEBUG) {
						System.out.println(" read value " + data.getValue());
					}
				}
			}

			boolean set_elem(Object str) {
				if (!(str instanceof String)) {
					try {
						data.copy((Id3v2elem) str);
						return true;
					}
					// set an error ... or print it!
					catch (Exception e) {
						System.out.println("Cast exception, wrong configuration object");
						return false;
					}
				}
				data.setValue((String) str);
				// 5 more bytes, 3 for the language, two for the zeroes
				update_length();
				posonfile = -1;
				return true;
			}

			byte[] get_frame_bytes() {
				Id3v2elem tmpdata = getData();
				if (tmpdata.isEmpty())
					return new byte[0];
				set_flags_by_config(tmpdata.fieldName);
				if (posonfile != -1) {
					// return write_header_and_field_content (tmptag,pos,null);
					return null;
				} else {
					// writes the field length, flags and value in vector vet starting from pos!
					// text type get the string encoded if necessary!
					byte content[] = Utils.getBytes(data.getValue());
					return get_header_and_frame_bytes(content);
				}
			}
		}

		public class id3v2_userurl extends id3v2_elem {
			private Id3v2userurl data = null;

			id3v2_userurl() {
				data = new Id3v2userurl();
			}

			id3v2_elem getClone() {
				id3v2_userurl tmp = new id3v2_userurl();
				tmp.copy(this);
				return tmp;
			}

			Id3v2elem getData() {
				return data;
			}

			void setData(Object obj) {
				data = (Id3v2userurl) obj;
			}

			Id3v2elem get_elem() {
				return data.getConfigObject();
			}

			// no more important!
			void update_length() {
				length = (data.getValue() + data.getElem("explain")).length() + 2; // text encoding more the second
																					// zero!
			}

			void clear() {
				(getData()).clear();
				update_length();
			}

			// fields read_field and write_field are overwritten!
			void read_field(byte buf[], int pos) {
				byte tmpb[] = read_header_and_get_field_content(buf, pos);
				if (tmpb != null) {
					int fld_pos = 1;
					StringBuffer str = new StringBuffer();
					flags.text_encoding = (int) tmpb[0];
					fld_pos += get_decoded_text_string(tmpb, fld_pos, str);
					;
					data.setElem("explain", str.toString());
					data.setValue(new String(tmpb, fld_pos, tmpb.length - fld_pos));
					if (miodebug > READDEBUG) {
						System.out.println(" expl " + data.getElem("explain") + " read value " + data.getValue());
					}
				}
			}

			boolean set_elem(Object str) {
				if (!(str.getClass()).equals(String.class)) {
					try {
						data.copy((Id3v2userurl) str);
						return true;
					}
					// set an error ... or print it!
					catch (Exception e) {
						System.out.println("Cast exception, wrong configuration object");
						return false;
					}
				}
				data.setValue((String) str);
				// 5 more bytes, 3 for the language, two for the zeroes
				update_length();
				posonfile = -1;
				return true;
			}

			byte[] get_frame_bytes() {
				Id3v2elem tmpdata = getData();
				if (tmpdata.isEmpty())
					return new byte[0];
				set_flags_by_config(tmpdata.fieldName);
				if (posonfile != -1) {
					// return write_header_and_field_content (tmptag,pos,null);
					return null;
				} else {
					byte tmparr[] = null;
					byte content[] = null;
					// writes the field length, flags and value in vector vet starting from pos!
					// text type get the string encoded if necessary!
					tmparr = Utils
							.join(new byte[][] { get_encoded_text_string((String) data.getElem("explain") + stringend),
									Utils.getBytes(data.getValue()) });
					content = new byte[tmparr.length + 1];
					content[0] = (byte) flags.text_encoding;
					System.arraycopy(tmparr, 0, content, 1, tmparr.length);
					return get_header_and_frame_bytes(content);
				}
			}
		}

		public class id3v2_usertext extends id3v2_elem {
			private Id3v2userurl data = null;

			id3v2_usertext() {
				data = new Id3v2userurl();
			}

			id3v2_elem getClone() {
				id3v2_usertext tmp = new id3v2_usertext();
				tmp.copy(this);
				return tmp;
			}

			Id3v2elem getData() {
				return data;
			}

			void setData(Object obj) {
				data = (Id3v2userurl) obj;
			}

			Id3v2elem get_elem() {
				return data.getConfigObject();
			}

			// no more important!
			void update_length() {
				length = (data.getValue() + data.getElem("explain")).length() + 2; // text encoding more the second
																					// zero!
			}

			void clear() {
				(getData()).clear();
				update_length();
			}

			// fields read_field and write_field are overwritten!
			void read_field(byte buf[], int pos) {
				byte tmpb[] = read_header_and_get_field_content(buf, pos);
				if (tmpb != null) {
					int fld_pos = 1;
					StringBuffer str = new StringBuffer();
					flags.text_encoding = (int) tmpb[0];
					fld_pos += get_decoded_text_string(tmpb, fld_pos, str);
					data.setElem("explain", str.toString());
					fld_pos += get_decoded_text_string(tmpb, fld_pos, str);
					data.setValue(str.toString());
					if (miodebug > READDEBUG) {
						System.out.println(" expl " + data.getElem("explain") + " read value " + data.getValue());
					}
				}
			}

			boolean set_elem(Object str) {
				if (!(str.getClass()).equals(String.class)) {
					try {
						data.copy((Id3v2userurl) str);
						return true;
					}
					// set an error ... or print it!
					catch (Exception e) {
						System.out.println("Cast exception, wrong configuration object");
						return false;
					}
				}
				data.setValue((String) str);
				// 5 more bytes, 3 for the language, two for the zeroes
				update_length();
				posonfile = -1;
				return true;
			}

			byte[] get_frame_bytes() {
				Id3v2elem tmpdata = getData();
				if (tmpdata.isEmpty())
					return new byte[0];
				set_flags_by_config(tmpdata.fieldName);
				if (posonfile != -1) {
					// return write_header_and_field_content (tmptag,pos,null);
					return null;
				} else {
					byte tmparr[] = null;
					byte content[] = null;
					// writes the field length, flags and value in vector vet starting from pos!
					// text type get the string encoded if necessary!
					tmparr = get_encoded_text_string(
							(String) data.getElem("explain") + stringend + (String) data.getValue());
					content = new byte[tmparr.length + 1];
					content[0] = (byte) flags.text_encoding;
					System.arraycopy(tmparr, 0, content, 1, tmparr.length);
					return get_header_and_frame_bytes(content);
				}
			}
		}

		public class id3v2_unsupported extends id3v2_elem {
			private Id3v2elem data = null;

			id3v2_unsupported() {
				data = new Id3v2elem();
			}

			Id3v2elem getData() {
				return data;
			}

			void setData(Object obj) {
				data = (Id3v2elem) obj;
			}

			byte[] bytedata = null;

			// fields read_field and write_field are overwritten!
			void read_field(byte buf[], int pos) {
				int field_len = id3v2_read_field_length(buf, pos);

				// shift to the pure field value
				bytedata = new byte[field_header_length(version) + field_len];
				System.arraycopy(buf, pos, bytedata, 0, bytedata.length);
				// getData().value=getString(tmpbuf);
			}

			byte[] get_frame_bytes() {
				return bytedata;
				/*
				 * Id3v2elem tmpdata=getData();
				 * return Utils.getBytes(tmpdata.value);
				 */
			}

			int total_field_length() {
				return bytedata.length;
			}
		}

		private class id3v2_array extends id3v2_elem {
			id3v2_elem cloneobj = null;

			// stores the array of id3v2_ elements
			// the other object is an Id3v2array object
			// from which the values are retrieved!
			ArrayList<id3v2_elem> array = new ArrayList<id3v2_elem>();
			Id3v2array arrayfields = new Id3v2array();

			/*
			 * Class getObjectClass() {
			 * return arrayfields.cloneobj.getClass();
			 * }
			 */

			Id3v2elem get_elem() {
				return arrayfields;
			}

			// copies the arrayfields objects into the array object!
			private void copyarray() {
				int i = 0;
				// the array with the elements will point to a copy
				// of the objects contained in the vector!
				for (i = 0; i < arrayfields.size(); i++) {
					id3v2_elem tmp = null;
					if (array.size() > i) {
						Id3v2elem tmp2 = arrayfields.getConfigObject(i);
						tmp = (id3v2_elem) array.get(i);
						tmp.setData(tmp2);
						array.set(i, tmp);
					} else {
						tmp = cloneobj.getClone();
						Id3v2elem tmp2 = arrayfields.getConfigObject(i);
						tmp.setData(tmp2);
						array.add(tmp);
					}
				}
				for (; i < array.size(); i++)
					array.remove(i);
			}

			boolean set_elem(Object obj) {
				if (obj instanceof Id3v2array) {
					// the passed array is pointed by an internal pointer
					arrayfields = (Id3v2array) obj;
					copyarray();
					return true;
				} else {
					id3v2_elem tmp = null;
					if (array.size() == 0) {
						tmp = cloneobj.getClone();
						array.add(tmp);
					} else
						tmp = (id3v2_elem) array.get(0);
					tmp.set_elem(obj);
					arrayfields.setValue((String) obj);
				}
				// if an element exist, set it calling set_elem to that value
				return true;
			}

			void clear() {
				arrayfields.clear();
				copyarray();
			}

			ArrayList<byte[]> getFrameBytes() {
				ArrayList<byte[]> tmp = new ArrayList<byte[]>();
				for (int i = 0; i < array.size(); i++) {
					tmp.add(((id3v2_elem) array.get(i)).get_frame_bytes());
				}
				return tmp;
			}

			boolean convertVersion(int vers, int mode) {
				// check if the field exists in the new version
				// if so and mode is FORCED, do it!
				// else put the field id in the lostFields hash!
				if (array.size() == 0)
					return true;

				// String fld = ((id3v2_elem) array.get(0)).field_ID;
				Integer row = (Integer) fieldsId.get(field_ID);
				if (row == null)
					return false;
				int index = row.intValue();
				if (!fieldsTable[index][getVersionTableIndex(vers)].equals("")) {
					for (int i = 0; i < array.size(); i++) {
						((id3v2_elem) array.get(i)).convertVersion(vers, mode);
					}
					return true;
				} else
					return false;
			}

			/*
			 * int length() {
			 * int len = 0;
			 * for (int i = 0; i < array.size(); i++) {
			 * len += ((id3v2_elem) array.get(i)).total_field_length();
			 * }
			 * return len;
			 * }
			 */

			void update_length() {
				for (int i = 0; i < array.size(); i++) {
					((id3v2_elem) array.get(i)).update_length();
				}
			}
		}

		// here will be defined a class called id3v2picture element ...

		id3tagv2() {
			version = DEFAULT_VERSION;
		}

		// buf contains the fields, pos is the position from wich
		// it starts reading
		private int readAndStoreField(byte buf[], int pos) {
			// store header length and field length (depend from the version)
			int head_len = field_header_length(version);
			int field_len = id3v2_read_field_length(buf, pos);
			// check if the field length value is valid!
			if (!(pos + field_len < real_sync_start))
				return buf.length;
			else if (!(field_len > 0))
				return head_len;

			String str_buf = id3v2_read_field_ID(buf, pos);

			if (miodebug > READDEBUG) {
				System.out.print(" Reading field " + str_buf + " len " + field_len);
			}

			// the field is read in a different way if it is a text field
			// or a comment field, or something else ... here should
			// retrieve the field type from the version and the str_buf
			// and then make some if and else to allocate the right object,
			// or eventually the unsupported object!

			// for fields supporting more than a field, the internal
			// representation must be done with arrays to handle more than
			// one field. When a get will be done it will return
			// an array of structures!

			// the elem is only created, than the read_field passes the
			// buf value and the pos where the field starts, the elem
			// created writes everything, flags, length and all!

			// get the table index
			Integer integer = (Integer) fieldsId.get(str_buf);
			if (integer == null) {
				System.out.println("Not found " + str_buf);
				return (head_len + field_len);
			}
			int tableindex = integer.intValue();
			String fieldname = (String) aliasfields.get(fieldsTable[tableindex][NAMES]);
			if (fieldname == null)
				fieldname = fieldsTable[tableindex][NAMES];
			String aliasname = getAliasField(fieldname);
			id3v2_elem tmp_elem = gimmeid3v2_elem(aliasname);
			tmp_elem.read_field(buf, pos);

			// reads the field parsing it in the correct mode since
			// the element has been converted!

			// String type = fieldsTable[tableindex][TYPE];
			// if the type is unsupported, the field should NOT be read
			// and should be put in an "unsupported array" ...
			if (fieldsTable[tableindex][NUMEROSITY].equals("single")) {
				tmp_elem.index = integer.intValue();
				hash_elem.put(aliasname, tmp_elem);
			} else if (fieldsTable[tableindex][NUMEROSITY].equals("vector")) {
				// look if a vector has already been inserted
				// call a function and pass the name to it to create
				// an array with all the necessary info! field_ID, class name,
				// columns contained inside
				id3v2_array tmparr = null;
				tmparr = (id3v2_array) hash_elem.get(aliasname);
				if (tmparr == null) {
					tmparr = (id3v2_array) gimmeid3v2_array(aliasname);
					tmparr.index = integer.intValue();
				}
				// add the data element to the array field, than set it to update
				// the internal array references!
				tmparr.arrayfields.add(tmp_elem.getData().getConfigObject());
				tmparr.array.add(tmp_elem);
				hash_elem.put(aliasname, tmparr);
			} else {
				id3v2_array tmparr = null;
				tmparr = (id3v2_array) hash_elem.get("unsupported");
				if (tmparr == null) {
					tmparr = new id3v2_array();
					tmparr.index = 0x7ffffff;
					// tmparr.arrayfields.cloneobj=new Id3v2elem();
					// ((Id3v2elem)tmparr.arrayfields.cloneobj).fieldName=aliasname;
					tmparr.cloneobj = new id3v2_unsupported();
				}
				tmparr.array.add(tmp_elem);
				hash_elem.put("unsupported", tmparr);
			}
			return tmp_elem.total_field_length();
		}

		/*
		 * private byte[] get_frame_bytes_from_file (id3v2_elem elem)
		 * {
		 * // called when an unsupported frame is found ... this should return the frame
		 * AS IS
		 * // all included with the header!
		 * // the only check is that the field_ID should correspond, or the wrong bytes
		 * could be
		 * // copied ... if it is wrong, return null and do not write the tag, write an
		 * error!
		 * try
		 * {
		 * id3tag.seek((long)posinfile);
		 * byte tmpb[]=new byte[field_header_length(version)];
		 * id3tag.read(tmpb);
		 * int field_len=id3v2_read_field_length(tmpb,0);
		 * 
		 * // shift to the pure field value
		 * id3tag.seek((long)(posinfile));
		 * byte[] buf=new byte[field_header_length(version)+field_len];
		 * id3tag.read(buf);
		 * // the position in file should now be updated ...
		 * // but this should happen only if the write operation is successful!
		 * return (buf);
		 * }
		 * catch (Exception e)
		 * {
		 * System.out.println("Wrong reading the field from file "+e);
		 * return null;
		 * }
		 * }
		 */

		/*
		 * private byte[] get_frame_bytes_from_array(byte buf[], int pos) {
		 * // called when an unsupported frame is found ... this should return the frame
		 * AS
		 * // IS
		 * // all included with the header!
		 * try {
		 * int field_len = id3v2_read_field_length(buf, pos);
		 * 
		 * // shift to the pure field value
		 * byte[] tmpbuf = new byte[field_header_length(version) + field_len];
		 * System.arraycopy(buf, pos, tmpbuf, 0, tmpbuf.length);
		 * return (buf);
		 * } catch (Exception e) {
		 * System.out.println("Wrong reading the field from file " + e);
		 * return null;
		 * }
		 * }
		 */

		// to be revised
		/*
		 * private byte[] get_frame_bytes_from_file (id3v2_elem elem,byte buf[],int
		 * pos,int posinfile)
		 * {
		 * // called when an unsupported frame is found ... this should return the frame
		 * AS IS
		 * // all included with the header!
		 * // the only check is that the field_ID should correspond, or the wrong bytes
		 * could be
		 * // copied ... if it is wrong, return null and do not write the tag, write an
		 * error!
		 * int old_version=version;
		 * try
		 * {
		 * // retrieve the version, it could have been changed in a conversion!
		 * //
		 * id3tag.seek(0);
		 * // re-read the version
		 * byte tmpb[]=new byte[header_length()];
		 * id3tag.read(tmpb);
		 * version=((int)tmpb[3]) & 0xff;
		 * // re-read the field header
		 * id3tag.seek((long)posinfile);
		 * tmpb=new byte[header_length()];
		 * id3tag.read(tmpb);
		 * int old_head_len=field_header_length(version);
		 * int field_len=id3v2_read_field_length(tmpb,0);
		 * version=old_version;
		 * // now re-writes the header depending from the version
		 * int new_head_len=field_header_length(version);
		 * id3v2_write_field_length(buf,pos,field_len);
		 * // should write also the flags, write the same flags that were set
		 * // in the old version! TO BE DONE!
		 * 
		 * // shift to the pure field value
		 * id3tag.seek((long)(posinfile+old_head_len));
		 * id3tag.read(buf,pos+new_head_len,field_len);
		 * 
		 * // the position in file should now be updated ...
		 * // but this should happen only if the write operation is successful!
		 * elem.posonfile=pos;
		 * return (new_head_len+field_len);
		 * }
		 * catch (Exception e)
		 * {
		 * version=old_version;
		 * System.out.println("Wrong reading the field from file "+e);
		 * return 0;
		 * }
		 * }
		 */
		// these functions will change with id3v2 changes!!!!
		private void set_header_flags(byte buf[]) {
			if (version == 2) {
				unsynchronization = (buf[6] & 0x80);
				kompression = (buf[6] & 0x40);
			} else if (version == 3) {
				unsynchronization = (buf[6] & 0x80);
				extended_header = (buf[6] & 0x40);
				experimental_bit = (buf[6] & 0x20);
			} else if (version == 4) {
				unsynchronization = (buf[6] & 0x80);
				extended_header = (buf[6] & 0x40);
				experimental_bit = (buf[6] & 0x20);
				footer = (buf[6] & 0x1);
			}
			if (extended_header == 1 || footer == 1) {
				System.out.println("flag not supported");
				// System.exit(0);
			}
		}

		private int id3v2_read_header_length(byte buf[], int pos) {
			if (version == 3) {
				int field[] = new int[4];
				for (int i = 0; i < 4; i++) {
					field[i] = ((int) buf[pos + 6 + i]) & 0xff;
				}
				return ((field[0] << 24) + (field[1] << 16) + (field[2] << 8) + (field[3]));
			} else if (version == 4 || version == 2) {
				return (((buf[pos + 6] & 0x7f) << 24) + ((buf[pos + 7] & 0x7f) << 16) + ((buf[pos + 8] & 0x7f) << 8)
						+ (buf[pos + 9] & 0x7f));
			} else
				return -1;
		}

		private int field_header_length(int vers) {
			if (vers == 2)
				return 6;
			else if (vers == 3 || vers == 4)
				return 10;
			else
				return -1;
		}

		private String id3v2_read_field_ID(byte buf[], int pos) {
			if (version == 2) {
				return new String(buf, pos, 3);
			} else if (version == 3 || version == 4) {
				return new String(buf, pos, 4);
			} else
				return "";
		}

		private void id3v2_write_field_ID(byte buf[], int pos, String str) {
			for (int i = 0; i < str.length(); i++) {
				buf[pos + i] = (byte) (str.charAt(i));
			}
		}

		private int id3v2_read_field_length(byte buf[], int pos) {
			if (version == 2) {
				int new_pos = pos + 3;
				int field[] = new int[3];
				for (int i = 0; i < 3; i++) {
					field[i] = ((int) buf[new_pos + i]) & 0xff;
				}
				return ((field[0] << 16) + (field[1] << 8) + field[2]);
			} else if (version == 3) {
				int new_pos = pos + 4;
				int field[] = new int[4];
				for (int i = 0; i < 4; i++) {
					field[i] = ((int) buf[new_pos + i]) & 0xff;
				}
				return ((field[0] << 24) + (field[1] << 16) + (field[2] << 8) + (field[3]));
			} else if (version == 4) {
				return (((buf[pos + 4] & 0x7f) << 24) + ((buf[pos + 5] & 0x7f) << 16) + ((buf[pos + 6] & 0x7f) << 8)
						+ (buf[pos + 7] & 0x7f));
			} else
				return -1;
		}

		private void id3v2_write_header_length(byte buf[], int pos, int len) {
			try {
				if (version == 3) {
					buf[pos + 6] = (byte) (len >> 24);
					buf[pos + 7] = (byte) ((len >> 16) & 0xff);
					buf[pos + 8] = (byte) ((len >> 8) & 0xff);
					buf[pos + 9] = (byte) (len & 0xff);
				} else if (version == 4 || version == 2) {
					buf[pos + 6] = (byte) ((len & 0x0fe00000) >> 21);
					buf[pos + 7] = (byte) ((len & 0x001fc000) >> 14);
					buf[pos + 8] = (byte) ((len & 0x00003f80) >> 7);
					buf[pos + 9] = (byte) ((len & 0x0000007f));
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}

		private void id3v2_write_field_length(byte buf[], int pos, int len) {
			try {
				if (version == 2) {
					buf[pos + 3] = (byte) ((len >> 16) & 0xff);
					buf[pos + 4] = (byte) ((len >> 8) & 0xff);
					buf[pos + 5] = (byte) (len & 0xff);
				} else if (version == 3) {
					buf[pos + 4] = (byte) (len >> 24);
					buf[pos + 5] = (byte) ((len >> 16) & 0xff);
					buf[pos + 6] = (byte) ((len >> 8) & 0xff);
					buf[pos + 7] = (byte) (len & 0xff);
				} else if (version == 4) {
					buf[pos + 4] = (byte) ((len & 0x0fe00000) >> 21);
					buf[pos + 5] = (byte) ((len & 0x001fc000) >> 14);
					buf[pos + 6] = (byte) ((len & 0x00003f80) >> 7);
					buf[pos + 7] = (byte) ((len & 0x0000007f));
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}

		private int header_length() {
			return 10;
		}

		int read() {
			// the function reads the tagv2 fields. It stores the header
			// until the start of the mp3 file in an array and then
			// parses the array!
			try {
				int pos = 0;
				byte byte_buf[] = new byte[header_length()];
				// int field_length;
				// int start = 0;
				int end = 0;
				id3tag.seek(0);
				id3tag.read(byte_buf);
				if ((new String(byte_buf, 0, 3)).equals("ID3")) {
					exists = true;
					version = ((int) byte_buf[3]) & 0xff;

					if (version == 0xff) {
						System.out.println("ERROR: id3 version value not valid!");
						return 1;
					}
					if (((int) byte_buf[4] & 0xff) != 0) {
						System.out.println("ERROR: id3 header is not valid!");
						return 2;
					}

					// set flags contained in the header, such as unsynchronization,
					// header presence, footer and so on
					set_header_flags(byte_buf);

					// if some things are not supported return without reading, for
					// example unsynchronization and so on ...

					// if unsynchronization is done, call the function to perform
					// the inverse operation before parsing the header

					if (extended_header == 1 || footer == 1) {
						System.out.println("unsupp header or footer!");
						return 1;
					}
					int id3v2headerlength = id3v2_read_header_length(byte_buf, 0);

					total_fields_length = header_length();
					if (id3v2headerlength + header_length() > real_sync_start)
						end = real_sync_start;
					else
						end = id3v2headerlength + header_length();
					if (miodebug > READDEBUG) {
						System.out.println("Found id3v2 header length " + id3v2headerlength + " bytes " + byte_buf[6]
								+ ":" + byte_buf[7] + ":" + byte_buf[8] + ":" + byte_buf[9]);
						System.out.println("versione " + version);
					}

					byte_buf = new byte[end];
					id3tag.seek(0);
					id3tag.read(byte_buf);
					// now that all the tag has been read, do unsync if necessary!
					if (unsynchronization == 1)
						byte_buf = undoUnsyncronization(byte_buf);

					// here extension header should be read ...
					pos += header_length();
					while (pos < end - header_length()) {
						// readandstore reads the field and return the number
						// of bytes that has been read!
						pos += readAndStoreField(byte_buf, pos);
					} // chiusura while
				} else {
					exists = false;
					return 0;
				}
			} catch (Exception e) {
				System.out.println("error reading " + e);
				return 1;
			}
			return 0;
		}

		int write() {
			try {
				id3tag = new RandomAccessFile(filenamestr, "rw");

				Set<Map.Entry<String, Mp3info.id3tagv2.id3v2_elem>> set = hash_elem.entrySet();
				Iterator<Map.Entry<String, Mp3info.id3tagv2.id3v2_elem>> iterator = set.iterator();

				// here write the flags.text_encoding mode if it can be changed, and THEN
				// calculate the header length! Unicode occupies double characters!

				// to be changed with elem.getLength, sommo tutti i valori e vedo se
				// supero real_sync_start ...in quel caso riscrivo tutto il file!

				// to speed up things, if unsynchronization is not done in the header
				// nor in none of the frames (most of the times) a byte array with the correct
				// size
				// can be created, using the function below ...
				ArrayList<byte[]> frames = new ArrayList<byte[]>();
				while (iterator.hasNext()) {
					Map.Entry<String, Mp3info.id3tagv2.id3v2_elem> elem = (Map.Entry<String, Mp3info.id3tagv2.id3v2_elem>) iterator
							.next();
					// String field_id = (String) elem.getKey();
					id3v2_elem tmpelem = (id3v2_elem) elem.getValue();

					if (miodebug > WRITEDEBUG)
						System.out.println("Writing " + tmpelem.getData().fieldName +
								" field ID " + tmpelem.field_ID + " value " + tmpelem.getData().getValue());

					if (tmpelem instanceof id3v2_array) {
						ArrayList<byte[]> tmparr = ((id3v2_array) tmpelem).getFrameBytes();
						for (int i = 0; i < tmparr.size(); i++)
							frames.add(tmparr.get(i));
					} else
						frames.add(tmpelem.get_frame_bytes());
					if (miodebug > WRITEDEBUG) {
						byte buf[] = tmpelem.get_frame_bytes();
						for (int i = 0; i < buf.length; i++)
							System.out.print(buf[i] + " ");
						System.out.println();
					}
				}

				int total_length = 0;
				for (int i = 0; i < frames.size(); i++) {
					byte tmp[] = (byte[]) frames.get(i);
					if (tmp != null)
						total_length += ((byte[]) frames.get(i)).length;
					else {
						System.out.println("Null return in frame bytes!");
						return 0;
					}
				}

				byte id3v2tag[] = null;
				int pos = 0;
				if (unsynchronization == 0) {
					id3v2tag = new byte[total_length + header_length()];
					pos = header_length();
					// copy all the frames
				} else {
					id3v2tag = new byte[total_length];
				}

				// copy all the frames, then do unsync!
				for (int i = 0; i < frames.size(); i++) {
					byte tmp[] = (byte[]) frames.get(i);
					System.arraycopy(tmp, 0, id3v2tag, pos, tmp.length);
					pos += tmp.length;
				}

				if (unsynchronization == 1) {
					byte tmp[] = unsyncronize(id3v2tag);
					id3v2tag = new byte[tmp.length + header_length()];
					System.arraycopy(tmp, 0, id3v2tag, header_length(), tmp.length);
					// total length must not be modified, is the length before
					// unsynchronization!
				}

				// if extended header is present total_length must be higher!

				// write the header ...
				id3v2tag[0] = (byte) 'I';
				id3v2tag[1] = (byte) 'D';
				id3v2tag[2] = (byte) '3';
				id3v2tag[3] = (byte) version;
				// header flags .. could call a function!
				id3v2tag[4] = (byte) 0;
				id3v2tag[5] = (byte) 0;

				// tag header excluded from length field!
				id3v2_write_header_length(id3v2tag, 0, total_length);

				// here I have an array list with all the converted frames,
				// only the header is missing ... all the bytes should be copied
				// into one unic byte array, and then unsynchronization must be
				// performed on the whole array if requested
				// then I have the real length of the id3v2 tag ... now I can decide
				// if the whole file has to be re-written, and then I can write
				// down the tag. This is even a better behaviour ... the process
				// of building up the tag is always the same!
				// If one of the elements put in the array is null, it means that an
				// unrecoverable error has occured and the tag will not be written
				// (for example if an image left on the disk cannot be retrieved!)

				if (id3v2tag.length > real_sync_start) {
					// id3tag.setLength(id3tag.length()+id3v2tag.length*2);
					if (id3v2tag.length < 1500) {
						rewrite_file(1500 + id3v2tag.length);
						real_sync_start = 1500 + id3v2tag.length;
					} else {
						rewrite_file(id3v2tag.length * 2);
						real_sync_start = id3v2tag.length * 2;
					}
				}

				id3tag.seek(0);
				id3tag.write(id3v2tag);

				// write all zeroes until the end (case when no file rewriting was requested!)
				if (id3v2tag.length < real_sync_start) {
					byte tmp[] = new byte[real_sync_start - id3v2tag.length - 1];
					for (int i = 0; i < tmp.length; i++)
						tmp[i] = 0;
					id3tag.seek((long) id3v2tag.length);
					id3tag.write(tmp);
				}
				id3tag.close();
				exists = true;
				return 0;
			} catch (Exception filenotfound) {
				try {
					if (id3tag != null)
						id3tag.close();
					id3tag = null;
				} catch (Exception boh) {
					System.out.println(boh + " file not close!");
				}
				return 1;
			}
		}

		/*
		 * private void write_id3tag2_tofile ()
		 * {
		 * // should modify so that the whole tag v2 is read in a time from the file,
		 * and
		 * // then the parsing is done on this vector ...
		 * 
		 * int pos;
		 * // copy header to memory and rewrite the whole tag!
		 * byte tmptag[]=new byte[real_sync_start];
		 * byte field_buf[];
		 * int found;
		 * 
		 * try
		 * {
		 * pos=0;
		 * id3tag.seek(0);
		 * id3tag.read(tmptag,pos,10);
		 * // here the version could be changed ...
		 * 
		 * tmptag[3]=(byte)(version);
		 * // change also the unsync flag!
		 * 
		 * // here the real length of the id3v2 header should be written ...
		 * id3v2_write_header_length(tmptag,0,real_sync_start-10);
		 * 
		 * pos=10;
		 * if (((tmptag[5] & 0x40)>>6)==1)
		 * {
		 * System.out.println("to do,ext header present!");
		 * // System.exit(0);
		 * }
		 * 
		 * if (miodebug>WRITEDEBUG)
		 * System.out.println("leggero fino a "+(real_sync_start-10));
		 * 
		 * Enumeration hash_keys=hash_elem.keys();
		 * while (hash_keys.hasMoreElements())
		 * {
		 * String field_id=(String)(hash_keys.nextElement());
		 * pos+=((id3v2_elem)hash_elem.get(field_id)).write_field(tmptag,pos);
		 * }
		 * 
		 * for (found=pos;found<real_sync_start;found++)
		 * {
		 * tmptag[found]=0;
		 * }
		 * id3tag.seek(0);
		 * id3tag.write(tmptag);
		 * }
		 * catch (Exception e)
		 * {
		 * System.out.println(e+" exception in function write id3tag2 to file");
		 * }
		 * }
		 */

		private void rewrite_file(int offset) {
			// rewrites the mp3 file starting from the offset mp3start, filling with zeros
			// the
			// bytes left empty!
			int blk = 100000;
			int blk_cnt;
			// char* blk_buf=(char*)malloc((blk+1)*sizeof(char));
			byte blk_buf[] = new byte[blk];
			try {
				int song_len = (int) id3tag.length();
				if (offset > real_sync_start) {
					blk_cnt = 1;
					while (blk_cnt * blk < song_len - real_sync_start) {
						id3tag.seek(song_len - blk_cnt * blk);
						// System.out.print("\nleggo da "+id3tag.getFilePointer());
						id3tag.read(blk_buf);
						id3tag.seek(song_len - blk_cnt * blk + offset);
						// System.out.print("\nscrivo da "+id3tag.getFilePointer());
						id3tag.write(blk_buf);
						// System.out.print("blk*cnt "+blk_cnt*blk);
						blk_cnt++;
					}
					blk_cnt--;
					// write the last block shorter than 100000!
					id3tag.seek(song_len - blk_cnt * blk);
					// System.out.print("\nrimanenti da copiare "+id3tag.getFilePointer());
					blk = (int) id3tag.getFilePointer();
					id3tag.seek(real_sync_start);
					// System.out.print("\nda copiare con sync start "+id3tag.getFilePointer());
					id3tag.read(blk_buf, 0, blk - real_sync_start);
					id3tag.seek(offset);
					if (miodebug > WRITEDEBUG)
						System.out.println("Riscritto il file mp3 che ora parte da " + id3tag.getFilePointer());
					id3tag.write(blk_buf, 0, blk - real_sync_start);

					blk_buf = new byte[offset - real_sync_start];
					for (int i = 0; i < offset - real_sync_start; i++) {
						blk_buf[i] = (byte) 0;
					}
					id3tag.seek(real_sync_start);
					id3tag.write(blk_buf);
				} else {
					blk_cnt = 0;
					while ((blk_cnt + 1) * blk + real_sync_start < song_len) {
						id3tag.seek(blk_cnt * blk + real_sync_start);
						// printf("\nleggo da %d",ftell(id3tag));
						id3tag.read(blk_buf, 0, blk);
						id3tag.seek(blk_cnt * blk);
						// printf("\nscrivo da %d",ftell(id3tag));
						id3tag.write(blk_buf);
						// printf("blk*cnt %d file-real %d
						// ",blk_cnt*blk,file_length-Mp3info.real_sync_start);
						blk_cnt++;
					}

					id3tag.seek(blk_cnt * blk + real_sync_start);
					// printf("\nda copiare con sync start %d",ftell(id3tag));
					int rest = song_len - (int) id3tag.getFilePointer();
					// printf("\nrimanenti da copiare %d",rest);
					id3tag.read(blk_buf, 0, rest);
					id3tag.seek(blk_cnt * blk);
					// printf("\nriscrivo da %d",ftell(id3tag));
					id3tag.write(blk_buf, 0, rest);
					// printf("\nsono a %d",ftell(id3tag));
					id3tag.setLength(song_len - real_sync_start + offset);
				}
				real_sync_start = offset;
			} catch (Exception e) {
				System.out.println(e + " exception in function rewrite_file");
			}
		}

		void clear() {
			id3v2_elem tmp;
			Set<Map.Entry<String, Mp3info.id3tagv2.id3v2_elem>> set = hash_elem.entrySet();
			Iterator<Map.Entry<String, Mp3info.id3tagv2.id3v2_elem>> iterator = set.iterator();

			while (iterator.hasNext()) {
				Map.Entry<String, Mp3info.id3tagv2.id3v2_elem> elem = (Map.Entry<String, Mp3info.id3tagv2.id3v2_elem>) iterator
						.next();
				// String id = (String) elem.getKey();
				tmp = (id3v2_elem) elem.getValue();
				tmp.clear();
			}
		}

		void delete() {
			if (real_sync_start > 0) {
				try {
					id3tag = new RandomAccessFile(filenamestr, "rw");
					byte buf[] = new byte[real_sync_start - 1];
					for (int i = 0; i < buf.length; i++)
						buf[i] = 0;
					id3tag.write(buf);
					id3tag.close();
					id3tag = null;
				} catch (Exception ioex) {
					try {
						if (id3tag != null)
							id3tag.close();
						id3tag = null;
					} catch (Exception ioex2) {
					}
				}
			}
			changeSong(filenamestr); // reloads id3v2 info!
		}

		boolean setElem(String id, Object value) {
			// this is an bastraction layer function ...
			// the second parameter is an object and its type is determined
			// by the first field value (for example if id=="image") then
			// the second parameter is an object with the image, the type
			// and so on!
			// it then will call the element set_elem function to set
			// all the values!

			// check if the field is of type vector or single
			// if it is a vector type, check if there is already one
			// if not create the arraylist and insert the new element
			// else insert in the head the new element
			// if is single, retrieve the element and set it!

			// to be added the case of adding the totaltracknumber field ...
			Integer tableind = ((Integer) fieldsString.get(id));
			if (tableind == null)
				return false;
			int tableindex = tableind.intValue();

			// check if the field is unsupported return false
			if (fieldsTable[tableindex][TYPE].length() == 0)
				return false;

			if (id.equals("genre")) {
				try {
					int i = Integer.parseInt((String) value);
					if (i > -1 && i < 126)
						value = new String("(" + i + ")" + genreList[i]);
				} catch (Exception e) {
				}
			} else if (id.equals("total track number")) {
				try {
					String val = (String) value;
					Integer.parseInt(val);
					String num = getElem("track").getValue().trim();
					if (num.indexOf("/") == -1) {
						try {
							setElem("track", num + "/" + val);
							return true;
						} catch (Exception exc) {
							return false;
						}
					} else {
						try {
							setElem("track", num.substring(0, num.indexOf("/")) + "/" + val);
							return true;
						} catch (Exception exc) {
							return false;
						}
					}
				} catch (Exception ex) {
					return false;
				}
			}

			// this function should call a function in id3v2 since this
			// should think about the different types, it should call
			// something like id3v2.setElem(id,value)

			id3v2_elem tmp_elem = null;
			tmp_elem = (id3v2_elem) hash_elem.get(id);
			boolean ret = false;
			if (tmp_elem == null) {
				// check if the field exist in the current version ...
				// if it is not save an error and return false
				tmp_elem = (id3v2_elem) gimmeid3v2_elem(id);

				if (tmp_elem == null) {
					// save that field id is not supported in current id3v2 version!
					lostFields.put(id, "1");
					return false;
				}
				if (fieldsTable[tableindex][NUMEROSITY].equals("single")) {
					ret = tmp_elem.set_elem(value);
					if (ret)
						hash_elem.put(id, tmp_elem);
				} else if (fieldsTable[tableindex][NUMEROSITY].equals("vector")) {
					// look if a vector has already been inserted
					id3v2_array tmparr = (id3v2_array) gimmeid3v2_array(id);
					ret = tmparr.set_elem(value);
					if (ret)
						hash_elem.put(id, tmparr);
				}
				// print unexistent field!
				return ret;
			} else {
				return tmp_elem.set_elem(value);
			}
		}

		void clearElem(String id) {
			// something should be changed! for vector elements,
			// if nothing is specified, all the array has to be deleted!
			// else the index of the delete item should be passed ...
			id = getOrigField(id);
			if (hash_elem.containsKey(id)) {
				id3v2_elem tmp = (id3v2_elem) hash_elem.get(id);
				if (tmp != null)
					tmp.clear();
				else {
					// print or store an error!
					System.out.println("Element " + id + " does not exists,can't clear!");
				}
			}
		}

		// should be changed ... this function could return something that is
		// different from a string, it could be an array of strings, or images or
		// something
		// that is even more complex!
		Id3v2elem getElem(String id) {
			// check if the element is a single type one or a vector type one
			// in the first case return the element's value
			// in the last case, return the first element of the list!
			// an object instead of a list could be passed, then
			// compares the object to a String or to a String[]
			if (id.equals("track_asis")) {
				id = "track";
				id3v2_elem elem_tmp = (id3v2_elem) (hash_elem.get(id));
				if (elem_tmp != null)
					return elem_tmp.get_elem();
				else
					return new Id3v2elem();
			} else if (id.equals("genre_asis")) {
				id = "genre";
				id3v2_elem elem_tmp = (id3v2_elem) (hash_elem.get(id));
				if (elem_tmp != null)
					return elem_tmp.get_elem();
				else
					return new Id3v2elem();
			} else if (hash_elem.containsKey(id)) {
				id3v2_elem elem_tmp = (id3v2_elem) (hash_elem.get(id));
				if (id.equals("track")) {
					String res = null;
					res = elem_tmp.get_elem().getValue();
					try {
						Integer.parseInt(res);
						return new Id3v2elem(res);
					} catch (Exception e) {
						String tmpres = null;
						int min = res.indexOf("/");
						if (min != -1 && res.length() > 2) {
							tmpres = res.substring(0, min);
							try {
								Integer.parseInt(tmpres);
								return new Id3v2elem(tmpres);
							} catch (Exception exc) {
								return new Id3v2elem();
							}
						}
						return new Id3v2elem();
					}
				} else if (id.equals("genre")) {
					String res = null;
					res = elem_tmp.get_elem().getValue();
					try {
						Integer.parseInt(res);
						return new Id3v2elem(res);
					} catch (Exception e) {
						String tmpres = null;
						int min = res.indexOf("(");
						int max = res.indexOf(")");
						if (min != -1 && max != -1 && min < max && res.length() > 2) {
							tmpres = res.substring(min + 1, max);
							try {
								Integer.parseInt(tmpres);
								return new Id3v2elem(tmpres);
							} catch (Exception ex) {
								return new Id3v2elem();
							}
						}
					}
					return new Id3v2elem();
				} else
					return elem_tmp.get_elem();
			} else {
				// this function creates the correct object
				// eventually an array, and returns the correct object!
				Integer integ = (Integer) fieldsString.get(id);
				if (integ == null) {
					System.out.println("Wrong id " + id + " in function get elem!");
					return null;
				}
				int tableindex = integ.intValue();

				if (fieldsTable[tableindex][NUMEROSITY].equals("single")) {
					id3v2_elem tmp = (id3v2_elem) gimmeid3v2_elem(id);
					return tmp.getData();
				} else if (fieldsTable[tableindex][NUMEROSITY].equals("vector")) {
					// (id3v2_array)gimmeid3v2_array(fieldsTable[tableindex][NAMES]);
					id3v2_array tmp = (id3v2_array) gimmeid3v2_array(id);
					return tmp.arrayfields;
				} else {
					System.out.println("Wrong numerosity " + fieldsTable[tableindex][NUMEROSITY].equals("vector"));
					return null;
				}
			}
		}

		void print() {
			if (exists) {
				String value[] = new String[] { "Artist     : ", "Title      : ", "Album      : ", "Year       : ",
						"Genre      : ", "Comment    : ", "Song num   : " };
				String identifiers[] = new String[] { "TPE1", "TIT2", "TALB", "TYER", "TCOM", "COMM", "TRCK" };
				System.out.println("Id3 tag version 2." + version);
				if (version == 3 || version == 4) {
					id3v2_elem elem_tmp;
					for (int i = 0; i < value.length; i++) {
						if (hash_elem.containsKey(identifiers[i])) {
							elem_tmp = (id3v2_elem) (hash_elem.get(identifiers[i]));
							if (elem_tmp.data.getValue().length() > 0)
								System.out.println(value[i] + elem_tmp.data.getValue());
						}
					}
				}
				/*
				 * if (composer.value.length()!=0)
				 * System.out.println("Composer   : "+composer);
				 * if (origartist.value.length()!=0)
				 * System.out.println("Or.artist  : "+origartist);
				 * if (userurl.value.length()!=0)
				 * System.out.println("User url   : "+userurl);
				 * if (copyright.value.length()!=0)
				 * System.out.println("Copyright  : "+copyright);
				 * if (encoder.value.length()!=0)
				 * System.out.println("Encoder    : "+encoder);
				 */
			}
		}
	} // fine id3v2

	public class id3tagv1 {
		boolean exists = false;
		private StringBuffer artist = new StringBuffer("");
		private StringBuffer title = new StringBuffer("");
		private StringBuffer album = new StringBuffer("");
		private StringBuffer year = new StringBuffer("");
		private StringBuffer comment = new StringBuffer("");
		private int genre = -1;
		private int track_num = 0;
		private int version = -1;
		Hashtable<String, StringBuffer> hash_elem = new Hashtable<String, StringBuffer>();
		Hashtable<String, Integer> hash_length = new Hashtable<String, Integer>();

		id3tagv1() {
			hash_elem.put("artist", artist);
			hash_elem.put("title", title);
			hash_elem.put("album", album);
			hash_elem.put("year", year);
			hash_elem.put("comment", comment);
			Integer tmp = Integer.valueOf(30);
			hash_length.put("artist", tmp);
			hash_length.put("title", tmp);
			hash_length.put("album", tmp);
			hash_length.put("year", Integer.valueOf(4));
			hash_length.put("comment", tmp);

			artist.setLength(30);
			title.setLength(30);
			album.setLength(30);
			year.setLength(4);
			comment.setLength(30);
		}

		void clear() {
			setElem("artist", "");
			setElem("title", "");
			setElem("album", "");
			setElem("year", "");
			setElem("comment", "");
			genre = 255;
			track_num = 0;
			version = 0;
		}

		void read() {
			try {
				id3tag.seek(id3tag.length() - 128);
				byte byte_buf[] = new byte[128];
				if (id3tag.read(byte_buf) != 128) {
					System.out.print("Unexpected error in reading last 128 bytes of file!\n");
				}

				if ((char) byte_buf[0] == 'T' && (char) byte_buf[1] == 'A' && (char) byte_buf[2] == 'G') {
					exists = true;
					title = new StringBuffer(new String(byte_buf, 3, 30));
					artist = new StringBuffer(new String(byte_buf, 33, 30));
					album = new StringBuffer(new String(byte_buf, 63, 30));
					year = new StringBuffer(new String(byte_buf, 93, 4));
					comment = new StringBuffer(new String(byte_buf, 97, 30));
					hash_elem.put("artist", artist);
					hash_elem.put("title", title);
					hash_elem.put("album", album);
					hash_elem.put("year", year);
					hash_elem.put("comment", comment);
					int tmp = (int) (comment.charAt(29));

					if (tmp != 0) {
						if ((tmp & 0x80) > 0) {
							tmp = (tmp & 0x70) + 128;
						}
						track_num = tmp;
						version = 1;
					} else {
						version = 0;
					}
					tmp = (int) byte_buf[127];
					if ((tmp & 0x80) > 0) {
						tmp = (tmp & 0x70) + 128;
					}
					if (tmp < 126)
						genre = tmp;
					else
						genre = -1;
				} else
					exists = false;
			} catch (Exception e) {
				System.out.println(e + " exception in function read of tag v1");
			}
		}

		void write() {
			try {
				id3tag = new RandomAccessFile(filenamestr, "rw");
				boolean exists_onfile = false;
				byte tmp_buf[] = new byte[128];
				id3tag.seek(id3tag.length() - 128);
				id3tag.read(tmp_buf);
				if ((new String(tmp_buf, 0, 3)).equals("TAG")) {
					exists_onfile = true;
				}

				if (exists_onfile) {
					id3tag.seek(id3tag.length() - 128);
				} else {
					id3tag.seek(id3tag.length());
					id3tag.setLength(id3tag.length() + 128);
				}
				System.arraycopy(("TAG").getBytes(), 0, tmp_buf, 0, 3);
				System.arraycopy(getBufElem("title").substring(0, 30).getBytes(), 0, tmp_buf, 3, 30);
				System.arraycopy(getBufElem("artist").substring(0, 30).getBytes(), 0, tmp_buf, 33, 30);
				System.arraycopy(getBufElem("album").substring(0, 30).getBytes(), 0, tmp_buf, 63, 30);
				System.arraycopy(getBufElem("year").substring(0, 4).getBytes(), 0, tmp_buf, 93, 4);
				if (track_num != -1) {
					System.arraycopy(getBufElem("comment").substring(0, 29).getBytes(), 0, tmp_buf, 97, 29);
					tmp_buf[126] = (byte) track_num;
				} else {
					System.arraycopy(getBufElem("comment").substring(0, 30).getBytes(), 0, tmp_buf, 97, 30);
				}
				tmp_buf[127] = (byte) genre;
				/*
				 * id3tag.writeBytes("TAG");
				 * id3tag.writeBytes(getBufElem("title").substring(0,30));
				 * id3tag.writeBytes(getBufElem("artist").substring(0,30));
				 * id3tag.writeBytes(getBufElem("album").substring(0,30));
				 * id3tag.writeBytes(getBufElem("year").substring(0,4));
				 * 
				 * //func.comment.setCharAt(29,n);
				 * if (track_num!=-1)
				 * {
				 * id3tag.writeBytes(getBufElem("comment").substring(0,29));
				 * id3tag.writeByte(track_num);
				 * }
				 * else
				 * {
				 * id3tag.writeBytes(getBufElem("comment").substring(0,30));
				 * }
				 * id3tag.writeByte(genre);
				 */
				id3tag.write(tmp_buf);
				id3tag.close();
				id3tag = null;
				exists = true;
			} catch (IOException e) {
				try {
					if (id3tag != null)
						id3tag.close();
					id3tag = null;
				} catch (Exception io2) {
					System.out.println(io2 + " failed to close file after writing tag v1");
				}
			}
		}

		void delete() {
			try {
				if (exists) {
					id3tag = new RandomAccessFile(filenamestr, "rw");
					id3tag.setLength(id3tag.length() - 128);
					id3tag.close();
					id3tag = null;
					changeSong(filenamestr);
				}
			} catch (Exception e) {
				try {
					if (id3tag != null)
						id3tag.close();
					id3tag = null;
				} catch (Exception exc) {
					System.out.println(exc + " error deleting tag v1");
				}
			}
		}

		void clearElem(String id) {
			if (id.equals("track")) {
				track_num = 0;
			} else if (id.equals("genre")) {
				genre = 255;
			} else if (hash_elem.containsKey(id)) {
				StringBuffer elem_tmp = (StringBuffer) (hash_elem.get(id));
				elem_tmp = new StringBuffer("");
				int len = ((Integer) (hash_length.get(id))).intValue();
				elem_tmp.setLength(len);
				hash_elem.put(id, elem_tmp);
			} else {
				// eventually print an error!
			}
		}

		boolean setElem(String id, String value) {
			if (id.equals("track")) {
				try {
					int val = Integer.parseInt(value);
					if (val > 0 && val < 255) {
						track_num = val;
						return true;
					} else
						return false;
				} catch (Exception numexc) {
					return false;
				}
			} else if (id.equals("genre")) {
				try {
					int val = Integer.parseInt(value);
					if (val > -1 && val < 126) {
						genre = val;
						return true;
					} else
						return false;
				} catch (Exception exp) {
					if (fromGenreToIntegerHash.containsKey(value)) {
						genre = ((Integer) fromGenreToIntegerHash.get(value)).intValue();
						return true;
					} else
						return false;
				}
			} else if (hash_elem.containsKey(id)) {
				StringBuffer elem_tmp = (StringBuffer) (hash_elem.get(id));
				elem_tmp = new StringBuffer(value);
				int len = ((Integer) (hash_length.get(id))).intValue();
				elem_tmp.setLength(len);
				hash_elem.put(id, elem_tmp);
				return true;
			} else
				return false;
		}

		String getGenre(int gen) {
			if (gen > 0 && gen < 126) {
				return genreList[gen];
			} else
				return new String("");
		}

		private StringBuffer getBufElem(String id) {
			if (hash_elem.containsKey(id))
				return (StringBuffer) (hash_elem.get(id));
			else {
				System.out.println("wrong key request in id3v1");
				return null;
			}
		}

		String getElem(String id) {
			if (id.equals("track")) {
				if (track_num != 0)
					return String.valueOf(track_num);
				else
					return (new String(""));
			} else if (id.equals("genre")) {
				if (genre < 126 && genre >= 0)
					return String.valueOf(genre);
				else
					return (new String(""));
			} else if (id.equals("genrestring")) {
				if (genre < 126 && genre > -1)
					return genreList[genre];
				else
					return (new String(""));
			} else if (hash_elem.containsKey(id)) {
				StringBuffer elem_tmp = (StringBuffer) (hash_elem.get(id));
				int len = ((Integer) (hash_length.get(id))).intValue();
				int real_len = (elem_tmp.substring(0, len)).indexOf(0);
				if (real_len == -1)
					real_len = ((Integer) (hash_length.get(id))).intValue();
				return (elem_tmp.substring(0, real_len));
			}
			return (new String(""));
		}

		int getMaxFieldLength(String id) {
			if (hash_length.containsKey(id))
				return ((Integer) (hash_length.get(id))).intValue();
			else if (id.equals("track"))
				return 3;
			else if (id.equals("genre"))
				return 3;
			return 0;
		}

		void print() {
			if (exists) {
				// da mettere a posto la lunghezza da stampare!
				System.out.println("Id3 tag version 1." + version);
				System.out.println("Artist     : " + artist);
				System.out.println("Title:     : " + title);
				System.out.println("Album:     : " + album);
				System.out.println("Year:      : " + year);
				System.out.println("Comment:   : " + comment);
				if (track_num != 0) {
					System.out.println("Song num   : " + track_num);
				} else {
					System.out.println("Song num   : ");
				}
				if (genre != -1) {
					System.out.println("Genre:     : " + genre);
				} else {
					System.out.println("Genre:     : ");
				}
			}
		}
	}

	id3tagv1 id3v1;
	id3tagv2 id3v2;

	void copyid3v1toid3v2() {
		id3v2.exists = true;
		id3v2.setElem("artist", id3v1.getElem("artist"));
		id3v2.setElem("title", id3v1.getElem("title"));
		id3v2.setElem("album", id3v1.getElem("album"));
		id3v2.setElem("comment", id3v1.getElem("comment"));
		id3v2.setElem("year", id3v1.getElem("year"));
		if (id3v1.genre >= 0)
			id3v2.setElem("genre", "(" + id3v1.getElem("genre") + ")" + id3v1.getElem("genrestring"));
		if (Integer.parseInt(id3v1.getElem("track")) > 0)
			id3v2.setElem("track", id3v1.getElem("track"));
	}

	void copyid3v2toid3v1() {
		id3v1.setElem("artist", id3v2.getElem("artist").getValue());
		id3v1.setElem("title", id3v2.getElem("title").getValue());
		id3v1.setElem("album", id3v2.getElem("album").getValue());
		id3v1.setElem("comment", id3v2.getElem("comment").getValue());
		id3v1.setElem("year", id3v2.getElem("year").getValue());
		id3v1.setElem("track", id3v2.getElem("track").getValue());
		id3v1.setElem("genre", id3v2.getElem("genre").getValue());
		id3v1.exists = true;
		// lacks the genre!
		// if (Pattern.matches("^[0-9]+$",id3v2.getGenre()))
		// {
		// id3v1.setIntGenre(Integer.parseInt(id3v2.getGenre()));
		// }
		// String gen=id3v2.getGenre();
		// Pattern p=Pattern.compile("^[0-9]+$");
		// Regex reg=new Regex();
	}

	private void initialize_variables() {
		ismp3 = false;
		song_length = -1;
		real_sync_start = -1;
		num_of_frames = -1;
		bitrate = -1;
		sample_rate = -1;
		mpg_version = -1;
		mpg_layer = -1;
		channel_type = -1;
		emphasys = -1;
		copyright = -1;
		copy = -1;
		crc = -1;

		id3v1 = new id3tagv1();
		id3v2 = new id3tagv2();
	}

	Mp3info() {
		filenamestr = null;
		initialize_variables();
	}

	Mp3info(String s, int mode) {
		File file = new File(s);
		if (file.exists()) {
			if (mode == READONLYTAGS || mode == READONLYMP3 || mode == READONLYISMP3 || mode == READTAGSANDISVBR)
				readmode = mode;
			else
				readmode = READALL;
			filenamestr = s;
			create_object();
		}
	}

	Mp3info(String s) {
		filenamestr = s;
		File file = new File(s);
		if (file.exists())
			create_object();
	}

	private void create_object() {
		// int i;
		initialize_variables();
		// this function opens the file and gets tag info
		// and mp3 informations!

		// reads only the tags, uses a file handler instead of reading the whole file!
		try {
			song = new RandomAccessFile(filenamestr, "r");
			id3tag = new RandomAccessFile(filenamestr, "r");

			ismp3 = find_syncro();

			if (readmode != READONLYISMP3) {
				if (ismp3 && readmode != READONLYMP3) {
					id3v1.read();
					id3v2.read();
				}

				if (ismp3 && readmode != READONLYTAGS) {
					// check also the song lenght! ... calls a function that checks
					// if the rate is constant, useing a file pointer. If it is,
					// it sets the song length and returns true. If it is not,
					// another function is called, passing the sync_start to
					// be faster, and calculates the real rate for a VBR file,
					// much slower and spending memory.
					if (isVbr(real_sync_start, mpg_layer, mpg_version)) {
						if (readmode != READTAGSANDISVBR)
							find_real_song_length(real_sync_start);
					}
				}
			}
			song.close();
			id3tag.close();
			song = null;
			id3tag = null;
		} // chiusura del try
		catch (Exception e) {
			if (song != null) {
				try {
					song.close();
					id3tag.close();
					song = null;
					id3tag = null;
				} catch (Exception ioexp) {
				}
			}
			System.out.println(e);
		}
	}

	private boolean isVbr(int start, int layer, int mpgver) {
		RandomAccessFile song = null;
		try {
			song = new RandomAccessFile(filenamestr, "r");
			song.seek((long) start + 1);
			int codbitrate, oldcodbitrate;
			int samprcode, pad_bit, oldbitrate;
			int read = ((int) song.read()) & 0x00ff;
			if ((read & 0x18) >> 3 != mpgver) {
				System.out.println("wrong read the version");
				song.close();
				return false;
			}
			if ((read & 0x06) >> 1 != layer) {
				System.out.println("wrong read the layer");
				song.close();
				return false;
			}

			read = ((int) song.read()) & 0x00ff;
			// if (f_sinc.getFilePointer()-f_sinc.length()>=0)
			// break;

			// bitrate e' variabile globale!
			oldcodbitrate = read >> 4;
			bitrate = (int) rate[mpg_version][mpg_layer][oldcodbitrate];
			oldbitrate = bitrate;
			if (bitrate < 0) {
				song.close();
				return false;
			}
			codbitrate = oldcodbitrate;

			samprcode = (read & 0x0c) >> 2;
			pad_bit = (read & 0x02) >> 1;

			boolean changedrate = false;

			// take two points in the file, one at 1/3 of file length and
			// at 2/3 of file length, go there and find the sync, then compare the rate
			// to the rate of the first frame .. if they are different it is Vbr,
			// alse it is constant bit rate!
			int filepoints[] = new int[] { start + (((int) song.length()) - start) / 3,
					start + (((int) song.length()) - start) * 2 / 3 };
			for (int steps = 0; steps < filepoints.length; steps++) {
				int sinc = 0;
				int pos = filepoints[steps];
				song.seek(pos);
				byte buf[] = new byte[2100];
				song.read(buf);
				boolean found = false;
				while (sinc < buf.length - 1) {
					if (((int) buf[sinc] & 0xff) == 255 && ((int) buf[sinc + 1] >>> 29) == 7) {
						// System.out.println("possible sync at byte last chance "+(sinc+pos));
						try {
							id3tag.seek(sinc + pos + 2);
							if (check_syncro(id3tag, (int) buf[sinc + 1] & 0xff)) {
								found = true;
								// System.out.println("sync found at byte "+(sinc+pos));
								break;
							}
						} catch (Exception e) {
							System.out.println("Exc in func checksync! error:" + e);
						}
					}
					sinc++;
				}
				if (!found) {
					System.out.println("Syncro not found in func isVbr ... it should not happen!");
				} else {
					if (bitrate != oldbitrate) {
						changedrate = true;
						break;
					}
				}
			}

			// old alghoritm, parse the first 150 frames and look if the rate chnges
			// it is slow and can make wrong decisions!
			/*
			 * while (counter<150)
			 * {
			 * if (mpg_layer==1)
			 * {
			 * song.seek(song.getFilePointer()+jumpfact*jumpmultidimlay1[samprcode][
			 * mpg_version][pad_bit][codbitrate]-1);
			 * }
			 * else
			 * {
			 * song.seek(song.getFilePointer()+jumpfact*jumpmultidimlaynot1[mpg_layer][
			 * samprcode][mpg_version][pad_bit][codbitrate]-1);
			 * }
			 * if (song.getFilePointer()>song.length())
			 * {
			 * changedrate=true;
			 * break;
			 * }
			 * read=((int)song.read()) & 0x00ff;
			 * codbitrate=read>>4;
			 * pad_bit=(read & 0x02)>>1;
			 * if (codbitrate!=oldcodbitrate)
			 * {
			 * changedrate=true;
			 * // System.out.println("difference at frame "+counter);
			 * break;
			 * }
			 * counter+=jumpfact;
			 * }
			 */

			// System.out.println("count "+counter+" oldrate "+bitrate+" newbit
			// "+(int)rate[mpg_version][mpg_layer][codbitrate]);
			/*
			 * System.out.println(filenamestr);
			 * if (mpg_layer==1 &&
			 * jumpmultidimlay1[samprcode][mpg_version][0][codbitrate]==0)
			 * {
			 * System.out.println("lay "+mpg_layer+" sampcode "+samprcode+" rate "
			 * +codbitrate+" name "+filenamestr);
			 * }
			 * else if
			 * (jumpmultidimlaynot1[mpg_layer][samprcode][mpg_version][0][codbitrate]==0)
			 * {
			 * System.out.println("lay "+mpg_layer+" sampcode "+samprcode+" rate "
			 * +codbitrate+" name "+filenamestr);
			 * }
			 */

			if (changedrate) {
				song.close();
				isvbr = true;
				return true;
			} else {
				songLengthSeconds = ((int) song.length() - start) / (bitrate / 8 * 1000);

				if (mpg_layer == 1)
					num_of_frames = ((int) song.length() - start)
							/ jumpmultidimlay1[samprcode][mpg_version][0][codbitrate];
				else
					num_of_frames = ((int) song.length() - start)
							/ jumpmultidimlaynot1[mpg_layer][samprcode][mpg_version][0][codbitrate];
				song.close();
				return false;
			}
		} catch (Exception e) {
			if (song != null) {
				try {
					song.close();
				} catch (Exception ex) {
				}
			}
			System.out.println("Exception " + e + " function isVbr");
			return false;
		}
	}

	void changeSong(String s) {
		filenamestr = s;
		create_object();
	}

	private boolean find_syncro() {
		// first read the first two bytes, if you don't find nothing read
		// the first two kilobyt. If no sync is found, read the last 15
		// kilobytes. If no sync is found the file is not an mp3 file.
		// If the sync is found, start jumping in the file to retrieve the
		// syncronization
		try {
			int sinc = 0;
			boolean found = false;
			long pos = 0, suplimit = 0, inflimit = 0, lasthop = 0;
			byte buf[] = new byte[5000];

			if (song.length() < 20000)
				return false;

			/*
			 * song.read(buf);
			 * for (int i=0;i<buf.length;i++)
			 * System.out.println("pos "+i+" val "+(((int)buf[i]) & 0xff));
			 */

			song.read(buf, 0, 2);
			id3tag.seek(song.getFilePointer());
			if (((int) buf[0] & 0xff) == 255 && ((int) buf[1] >>> 29) == 7
					&& check_syncro(id3tag, (int) buf[1] & 0xff)) {
				real_sync_start = 0;
				return true;
			}
			// try to find the sync in the first two chilobytes
			song.read(buf);
			pos = 2;

			while (sinc < buf.length - 1) {
				if (((int) buf[sinc] & 0xff) == 255 && ((int) buf[sinc + 1] >>> 29) == 7) {
					// System.out.println("possible sync at byte "+(sinc+pos));
					id3tag.seek(sinc + pos + 2);
					if (check_syncro(id3tag, (int) buf[sinc + 1] & 0xff)) {
						found = true;
						real_sync_start = (int) (sinc + pos);
						return true;
					}
				}
				sinc++;
			}
			// if the song is long enough ...
			if (!(song.length() > 20000))
				return false;

			// check if the file is an mp3. Start from half of the file and go on
			// until you find the sync!
			suplimit = song.length();
			inflimit = buf.length;
			buf = new byte[2100];
			while (suplimit - inflimit > 10000) {
				pos = inflimit + (suplimit - inflimit) / 2;
				song.seek(pos);
				song.read(buf);
				sinc = 0;
				found = false;
				while (sinc < buf.length - 1) {
					if (((int) buf[sinc] & 0xff) == 255 && ((int) buf[sinc + 1] >>> 29) == 7) {
						// System.out.println("possible sync at byte "+(pos+sinc));
						try {
							id3tag.seek(sinc + pos + 2);
							if (check_syncro(id3tag, (int) buf[sinc + 1] & 0xff)) {
								found = true;
								// System.out.println("sync found at byte "+(pos+sinc));
								suplimit = pos + sinc;
								lasthop = pos + sinc;
								break;
							}
						} catch (Exception e) {
							System.out.println("Exc in func checksync! error:" + e);
						}
					}
					sinc++;
				}
				if (!found)
					inflimit = pos + sinc;
			}

			// identified the zone ... now find the exact ...
			sinc = 0;
			pos = inflimit;
			song.seek(pos);
			buf = new byte[10000];
			song.read(buf);
			found = false;
			while (sinc < buf.length - 1) {
				if (((int) buf[sinc] & 0xff) == 255 && ((int) buf[sinc + 1] >>> 29) == 7) {
					// System.out.println("possible sync at byte last chance "+(sinc+pos));
					try {
						id3tag.seek(sinc + pos + 2);
						if (check_syncro(id3tag, (int) buf[sinc + 1] & 0xff)) {
							found = true;
							real_sync_start = (int) (sinc + pos);
							// System.out.println("sync found at byte "+(sinc+pos));
							return true;
						}
					} catch (Exception e) {
						System.out.println("Exc in func checksync! error:" + e);
					}
				}
				sinc++;
			}
			if (!found) {
				if (lasthop != 0) {
					real_sync_start = (int) lasthop;
					return true;
				} else
					return false;
			}
			return true;
		} catch (Exception e) {
			System.out.println("Exc in func checksync! error:" + e);
			return false;
		}
	}

	private boolean check_syncro(RandomAccessFile f_sinc, int read) throws Exception {
		// number of frames controlled
		int frames_number = 0;
		int pad_bit;
		// length of the current frame
		int codbitrate = 0;
		int samprcode = 0;

		// long start=System.currentTimeMillis();
		for (frames_number = 0; frames_number < 5; frames_number++) {
			// if (f_sinc.getFilePointer()-f_sinc.length()>=0)
			// break;
			mpg_version = (read & 0x18) >> 3;
			mpg_layer = (read & 0x06) >> 1;
			crc = (read & 0x01);

			// if (mpg_version>4 || mpg_layer>4 || mpg_version==1 || mpg_layer==0)
			// return 0;
			if (mpg_version == 1 || mpg_layer == 0)
				return false;

			read = ((int) f_sinc.read()) & 0x00ff;
			// if (f_sinc.getFilePointer()-f_sinc.length()>=0)
			// break;

			codbitrate = read >> 4;
			bitrate = (int) rate[mpg_version][mpg_layer][codbitrate];
			if (bitrate < 0)
				return false;

			samprcode = (read & 0x0c) >> 2;
			if (samprcode == 3)
				return false;
			sample_rate = samplerate[mpg_version][samprcode];
			// if (sample_rate<=0)
			// return 0;

			pad_bit = (read & 0x02) >> 1;
			// if (pad_bit!=0 && pad_bit!=1)
			// break;

			// System.out.print ("num: %d mpg_versions: %d mpg_layer: %d bitrate: %d
			// samplerate: %d\n",frames_number,mpg_version,mpg_layer,read>>4,(read &
			// 0x0c)>>2);
			// if (miodebug>SYNCRODEBUG)
			// System.out.println("num: "+frames_number+" mpg_versions: "+mpg_version+"
			// mpg_layer: "+mpg_layer+" bitrate: "+bitrate+" samplerate: "+sample_rate);

			read = ((int) f_sinc.read()) & 0x00ff;
			// if (f_sinc.getFilePointer()-f_sinc.length()>=0)
			// break;

			channel_type = (read & 0xc0) >> 6;
			// if (channel_type>4)
			// break;

			copyright = (read & 0x08) >> 3;
			// if (copyright!=1 && copyright!=0)
			// break;

			copy = (read & 0x04) >> 2;
			// if (copy!=1 && copy!=0)
			// break;

			emphasys = (read & 0x03);

			// if (miodebug>SYNCRODEBUG)
			// System.out.println("fr_num: "+frames_number+" pad bit: "+pad_bit+"
			// frame_length: "+frame_length);

			if (mpg_layer == 1) {
				f_sinc.seek(
						f_sinc.getFilePointer() + jumpmultidimlay1[samprcode][mpg_version][pad_bit][codbitrate] - 4);
				// System.out.println(" frame len layer 1
				// "+jumpmultidimlay1[samprcode][mpg_version][pad_bit][codbitrate]+" now pos in
				// file "+f_sinc.getFilePointer());

			} else {
				f_sinc.seek(f_sinc.getFilePointer()
						+ jumpmultidimlaynot1[mpg_layer][samprcode][mpg_version][pad_bit][codbitrate] - 4);
				// f_sinc.seek(f_sinc.getFilePointer()+jumpmultidimlaynot1[mpg_layer][samprcode][mpg_version][pad_bit][codbitrate]-100);
				// System.out.println(" frame len oth layer
				// "+jumpmultidimlaynot1[mpg_layer][samprcode][mpg_version][pad_bit][codbitrate]);
			}
			/*
			 * for (int k=0;k<120;k++)
			 * {
			 * read=((int)f_sinc.read()) & 0x00ff;
			 * System.out.println("pos "+f_sinc.getFilePointer()+" value "+read);
			 * }
			 */
			read = ((int) f_sinc.read()) & 0x00ff;

			// System.out.println("fr_num: "+frames_number+" pad bit: "+pad_bit+"
			// frame_length: "+frame_length);

			if (read == 0xff)
				read = ((int) f_sinc.read()) & 0x00ff;
			else
				return false;
		}
		return true;
	}

	private boolean find_real_song_length(int start) {
		RandomAccessFile song = null;
		int filepos = 0, bufferlength = 0;
		byte file[] = null;
		int limit = 4000000;
		int totallen = 0;
		try {
			song = new RandomAccessFile(filenamestr, "r");
			totallen = (int) song.length();
			song.seek((long) start);
			Runtime.getRuntime().gc();
			int mem = (int) Runtime.getRuntime().freeMemory();
			if (mem < limit) {
				song.close();
				return false;
			}
			if ((int) song.length() - start > mem - limit) {
				file = new byte[mem - limit];
			} else
				file = new byte[(int) song.length() - start];
			song.read(file);
			song.close();
		} catch (Exception e) {
			if (song != null) {
				try {
					song.close();
				} catch (Exception exc) {
				}
			}
			return false;
		}
		// catch (Exception exc){System.out.println(exc);}

		filepos = 1;
		bufferlength = file.length;

		int codbitrate, bit_rate, bitratesum = 0, samprcode, pad_bit, counter = 0;
		int read = ((int) file[filepos]) & 0x00ff;
		filepos++;
		if ((read & 0x18) >> 3 != mpg_version) {
			System.out.println("wrong read the version");
			// System.exit(0);
		}
		if ((read & 0x06) >> 1 != mpg_layer) {
			System.out.println("wrong read the layer");
			// System.exit(0);
		}

		read = ((int) file[filepos]) & 0x00ff;
		filepos++;
		// if (f_sinc.getFilePointer()-f_sinc.length()>=0)
		// break;

		codbitrate = read >> 4;
		bit_rate = rate[mpg_version][mpg_layer][codbitrate];
		samprcode = (read & 0x0c) >> 2;
		pad_bit = (read & 0x02) >> 1;

		if (mpg_layer == 1) {
			while (filepos < bufferlength) {
				if (bit_rate < 0)
					break;
				else {
					counter++;
					bitratesum += bit_rate;
				}

				filepos += jumpmultidimlay1[samprcode][mpg_version][pad_bit][codbitrate] - 1;
				// filepos+=jumponedimlay1[samprcode*128+mpg_version*32+pad_bit*16+codbitrate]-1;

				/*
				 * if (frames_number>50)
				 * System.out.println("fr num "+frames_number+" bit_rate "+bit_rate+
				 * " frame len "+jumpmultidimlay1[samprcode][mpg_version][pad_bit][codbitrate]
				 * +" file pos "+f_sinc.getFilePointer());
				 */
				if (filepos < bufferlength) {
					read = ((int) file[filepos]) & 0x00ff;
					codbitrate = (read & 0xf0) >> 4;
					bit_rate = rate[mpg_version][mpg_layer][codbitrate];
					pad_bit = (read & 0x02) >> 1;
					filepos++;
				} else
					break;
			}
		} else {
			while (filepos < bufferlength) {
				if (bit_rate < 0)
					break;
				else {
					counter++;
					bitratesum += bit_rate;
				}

				// filepos+=jumponedimlay1[mpg_layer*384+samprcode*128+mpg_version*32+pad_bit*16+codbitrate]-1;
				filepos += jumpmultidimlaynot1[mpg_layer][samprcode][mpg_version][pad_bit][codbitrate] - 1;
				/*
				 * if (frames_number>50)
				 * System.out.println("fr num "+frames_number+" bitrate "+bitrate+
				 * " frame len "+jumpmultidimlay1[samprcode][mpg_version][pad_bit][codbitrate]
				 * +" file pos "+f_sinc.getFilePointer());
				 */
				if (filepos < bufferlength) {
					read = ((int) file[filepos]) & 0x00ff;
					codbitrate = (read & 0xf0) >> 4;
					bit_rate = rate[mpg_version][mpg_layer][codbitrate];
					pad_bit = (read & 0x02) >> 1;
					filepos++;
				} else
					break;
			}
		}
		// static final int jumponedimlay1[]=new int[384];
		// access with the formula
		// layer*384+samplerate*128+mpgversion*32+padvalue*16+bitcoderate
		// static final int jumponedimlaynot1[]=new int[1536];

		// access with the formula samplerate*128+mpgversion*32+padvalue*16+bitcoderate
		// static final int jumpmultidimlay1[][][][]=new int [3][4][2][16];
		// access with the formula
		// layer*384+samplerate*128+mpgversion*32+padvalue*16+bitcoderate
		// static final int jumpmultidimlaynot1[][][][][]=new int [4][3][4][2][16];

		num_of_frames = (int) (counter * ((float) (totallen - start) / (float) file.length));
		bitrate = (int) (bitratesum / ((float) counter));

		// System.out.print("bitratesum "+bitratesum+" fr num "+frames_number+" song len
		// "+(((float)f_sinc.length())/(((float)bitrate)/8.0*1000.0)));
		// calculate the bitrate if the boolean value checkVbr is true!
		// System.out.print(" song len no float
		// "+(int)(f_sinc.length()/(bitrate/8.0*1000.0)));
		songLengthSeconds = (int) ((totallen - start) / ((bitratesum / ((float) counter)) / 8.0 * 1000.0));
		file = null;
		// System.out.println("before gc "+Runtime.getRuntime().freeMemory());
		// Runtime.getRuntime().gc();
		// System.out.println("after gc "+Runtime.getRuntime().freeMemory());
		return true;
		// System.out.println(" bitrate float
		// "+(((float)bitratesum)/((float)frames_number))+" bitrate int
		// "+bitratesum/frames_number);
	}

	void print() {
		if (ismp3) {
			System.out.print("MPEG info\n");
			System.out.print("mp3 version        : ");
			switch (mpg_version) {
				case 2:
					System.out.print("MPEG2,");
					break;
				case 3:
					System.out.print("MPEG1,");
					break;
				case 0:
					System.out.print("MPEG 2.5Mb,");
					break;
			}
			switch (mpg_layer) {
				case 1:
					System.out.println(" layer 3");
					break;
				case 2:
					System.out.println(" layer 2");
					break;
				case 3:
					System.out.println(" layer 1");
					break;
				default:
					System.out.print("Unknown layer");
					break;
			}
			System.out.println("Sync found at byte : " + real_sync_start);
			System.out.println("Rate               : " + bitrate + " kbit/s");
			System.out.println("Sample rate        : " + sample_rate + " Hz");
			System.out.print("Channel type       : ");
			switch (channel_type) {
				case 0:
					System.out.println("stereo");
					break;
				case 1:
					System.out.println("joint stereo");
					break;
				case 2:
					System.out.println("dual channel");
					break;
				case 3:
					System.out.println("single channel");
					break;
				default:
					System.out.print("Unknown layer");
					break;
			}
			System.out.print("Emphasys           : ");
			switch (channel_type) {
				case 0:
					System.out.print("none");
					break;
				case 1:
					System.out.println("50/15 ms");
					break;
				case 3:
					System.out.println("CCIT J.17");
					break;
				default:
					System.out.println("Invalid Emphasys");
					break;
			}
			System.out.print("Copyrighted        : ");
			if (copyright == COPYRIGHT)
				System.out.println("yes");
			else if (copyright == NOT_COPYRIGHT)
				System.out.println("no");
			System.out.print("Song status        : ");
			if (copy == ORIGINAL)
				System.out.println("original");
			else if (copy == COPY_OF_ORIGINAL)
				System.out.println("copy of original");
		}
	}

	public boolean renameTo(String str) {
		File file = new File(str);
		if (file.exists()) {
			filenamestr = str;
			return true;
		} else {
			ismp3 = false;
			return false;
		}
	}

	public boolean isMp3() {
		return ismp3;
	}

	public boolean isVbr() {
		return isvbr;
	}

	public int getSongLength() {
		return songLengthSeconds;
	}

	public String getSyncStart() {
		return String.valueOf(real_sync_start);
	}

	public String getNumFrames() {
		return String.valueOf(num_of_frames);
	}

	public String getBitRate() {
		return String.valueOf(bitrate);
	}

	public String getSampleRate() {
		return String.valueOf(sample_rate);
	}

	public String getMpgVersion() {
		if (mpg_version == 3)
			return "1.0";
		else if (mpg_version == 2)
			return "2.0";
		else if (mpg_version == 0)
			return "2.5";
		return "-1";
	}

	public String getMpgLayer() {
		if (mpg_layer == 1)
			return "3";
		else if (mpg_layer == 2)
			return "2";
		else if (mpg_layer == 3)
			return "1";
		return "-1";
	}

	public String getChannelType() {
		switch (channel_type) {
			case 0:
				return "stereo";
			case 1:
				return "joint stereo";
			case 2:
				return "dual channel";
			case 3:
				return "single channel";
			default:
				return "Unknown";
		}
	}

	public String getEmphasys() {
		switch (channel_type) {
			case 0:
				return "none";
			case 1:
				return "50/15 ms";
			case 3:
				return "CCIT J.17";
			default:
				return "Invalid Emphasys";
		}
	}

	public String getCopyright() {
		if (copyright == 1)
			return "yes";
		else
			return "no";
	}

	public String getCopy() {
		if (copy == 1)
			return "yes";
		else
			return "no";
	}

	public String getCrc() {
		if (crc == CRC_PROT)
			return "yes";
		else
			return "no";
	}

	public void deleteId3v1() {
		id3v1.delete();
	}

	public void deleteId3v2() {
		id3v2.delete();
	}

	public void writeId3v1() {
		id3v1.write();
	}

	public void writeId3v2() {
		id3v2.write();
	}
}
