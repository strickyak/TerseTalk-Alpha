all:  _make_vm_  _make_a1_

JARS=$$(echo /usr/lib/jvm/java-6-openjdk/jre/lib/*.jar | tr ' ' :)
JUNIT=$$(echo /home/*/adt-bundle-linux-x86*/eclipse/plugins/org.junit_3.*/junit.jar)

_make_vm_:
	cmp prelude.txt terse-vm/prelude.txt || cp prelude.txt terse-vm/prelude.txt
	cd ./terse-vm/src/terse/vm/ && make
_make_a1_:
	cd ./terse-a1/src/terse/a1/ && make

web: _make_vm_
	mkdir -p _tmp_
	set -x; javac -source 1.6 -target 1.6 -warn:none \
		-g \
		-cp $(JUNIT):$(JARS) \
		-d _tmp_ \
		terse-vm/src/terse/vm/*.java\
		terse-web/src/terse/web/WebServer.java
	touch w_web.txt
	java -cp _tmp_:$(JUNIT) terse.web.WebServer 8080

reweb: _make_vm_
	java -cp _tmp_:$(JUNIT) terse.web.WebServer 8080

reweb1: _make_vm_
	java -cp _tmp_:$(JUNIT) terse.web.WebServer 8081

reweb2: _make_vm_
	java -cp _tmp_:$(JUNIT) terse.web.WebServer 8082
