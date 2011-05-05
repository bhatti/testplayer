jar -xf ../testplayer/TestPlayer/lib/log4j-1.2.12.jar 
javac -classpath . -d . `find src -name "*.java"`
rm -rf org/apache/log4j/
jar -cf ../testplayer/TestPlayer/lib/sequence.jar *
