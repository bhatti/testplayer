CLASSPATH=target/Mini-0.1.jar:lib/log4j-1.2.12.jar:lib/aspectjweaver.jar:target/classes:lib/sequence.jar 
CLASSPATH=`echo $CLASSPATH|tr ':' ';'`
ASPECTPATH=../../TestPlayer/target/TestPlayer-0.1.jar:$CLASSPATH
ASPECTPATH=`echo $ASPECTPATH|tr ':' ';'`
java -classpath $CLASSPATH -Djava.system.class.loader=org.aspectj.weaver.WeavingURLClassLoader -Daj.class.path=$ASPECTPATH -Daj.aspect.path=$ASPECTPATH  com.plexobject.mini.Main
