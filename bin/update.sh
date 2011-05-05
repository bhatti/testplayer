#login=`cat /etc/passwd|grep $USER|cut -d ':' -f5|awk '{print $1}'|tr '[A-Z]' '[a-z]'`|sed "s/,.*//g"
login=`cat /etc/passwd|grep $USER|cut -d ':' -f5|awk '{print $1}'|tr '[A-Z]' '[a-z]'`
cd TestPlayer
ant clean
cd ../samples/mini
ant clean
cd ../../..
cvs -d :pserver:$login@cvs.dev.java.net:/cvs checkout testplayer
cd testplayer
cvs -d :pserver:$login@cvs.dev.java.net:/cvs commit -m "$*"
echo $login
