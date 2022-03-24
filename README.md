# Mp3_Tagger_Songs_DB
Java Software Platform Indipendent written to manage mp3 tags on mp3 files, and much more.
For example, you can create and manage a Database, you can easily rename files, optimize a CD
occupation before masterizing it (through a 'Knapsack algorithm' development).

Source files, images and documentation have been uploaded, classes should be generated through Java.
This software has been written by me in 2001-2002. A package with the software can be downloaded easily 
through "Tagger.jar", you should be simply able to double click on it to launch it, even on windows.

From command line:
git clone "https://github.com/ricky-andre/Mp3_Tagger"
(the above command will create an Mp3_Tagger directory)

The 'Tagger.jar' file should be double-clickable on windows, otherwise create a '.bat' file:
cd <path to Mp3_Tagger>\Mp3_Tagger
"C:\Program Files\Java\jre1.8.0_321\bin\java.exe" -jar tagger.jar

If you wish to compile the source files and launch the software, remember to install also
the JDK (Java Development Kit to have 'javac' command) and not only the JRE.

cd <path to Mp3_Tagger>\Mp3_Tagger\src
javac -d ..\ tagger\MainClass.java
cd ..
java tagger.MainClass
