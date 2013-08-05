JUNIT=/home/*/adt-bundle-linux-x86_64-*/eclipse/plugins/org.junit_3.*/junit.jar

A=XXX
echo $A
pid=

trap 'kill $pid' 0 1 2 3
while sleep 5
do
  B=$(find prelude.txt -printf '%T+')

  if [ $A != $B ]
  then
	  test -z $pid || kill $pid
	  wait
	  make all
	  java -cp "_tmp_:${JUNIT}" terse.web.WebServer 8000 &
	  pid=$!
  fi
  A=$B
done



