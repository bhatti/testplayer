version=`cat build.properties|grep testplayer.version|cut -d'=' -f2`
rm -rf tmp 2>/dev/null
mkdir tmp
cd tmp
for f in `ls ../lib/*jar|egrep -v "jdk5|jdk15|junit-4|easymock"`
do
  jar -xf $f
done
jar -xf ../target/TestPlayer-$version.jar
name=TestPlayer-all-$version-jdk14.jar
jar -cf ../target/$name * 
cd ..


rm -rf tmp 2>/dev/null

mkdir tmp
cd tmp
for f in `ls ../lib/*jar|egrep -v "jdk14|junit-3"`
do
  jar -xf $f
done
jar -xf ../target/TestPlayer-$version.jar
name=TestPlayer-all-$version-jdk15.jar
jar -cf ../target/$name * 
cd ..
rm -rf tmp
