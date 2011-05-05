login=`cat /etc/passwd|grep $USER|cut -d ':' -f5|awk '{print $1}'|tr '[A-Z]' '[a-z]'`
cd TestPlayer
ant clean
cd ../samples/mini
ant clean
cd ../../..
cvs -d :pserver:$login@cvs.dev.java.net:/cvs checkout testplayer
cd testplayer
version=`cat TestPlayer/build.properties|grep testplayer.version|cut -d'=' -f2`
#cvs -d :pserver:$login@cvs.dev.java.net:/cvs commit -m "$*" -r $version
cvs -d :pserver:$login@cvs.dev.java.net:/cvs commit -m "$*"
rversion=`echo $version|sed "s/\\./_/g"`
cvs -d :pserver:$login@cvs.dev.java.net:/cvs tag -c "rel-${rversion}"  .
#cvs update -A
#cvs -d :pserver:$login@cvs.dev.java.net:/cvs tag -d "rel-${rversion}"  .
cd TestPlayer
ant -Duser.name=$login dist > out

cd ../../fitnesse
zip -r ../testplayer/TestPlayer/dist/TestPlayer-${version}-fitnesse-example.zip TestPlayerSample/ FitNesseRoot/TestPlayerSample/
cd ..
zip -r testplayer/TestPlayer/dist/TestPlayer-${version}-fitnesse.zip fitnesse
cd testplayer/samples/mini
sh compile.sh
cd ..
zip -r ../TestPlayer/dist/TestPlayer-${version}-sample.zip mini
pwd
cp `find . -name "*.png"` ../../TestPlayer/dist/
