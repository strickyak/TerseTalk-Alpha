all:  _make_vm_  _make_a1_

JUNIT=/home/*/adt-bundle-linux-x86_64-*/eclipse/plugins/org.junit_3.*/junit.jar

_make_vm_:
	cmp prelude.txt terse-vm/prelude.txt || cp prelude.txt terse-vm/prelude.txt
	cd ./terse-vm/src/terse/vm/ && make
_make_a1_:
	cd ./terse-a1/src/terse/a1/ && make

web: _make_vm_
	mkdir -p _tmp_
	javac\
		-g \
		-cp $(JUNIT) \
		-d _tmp_ \
		terse-vm/src/terse/vm/*.java\
		terse-web/src/terse/web/WebServer.java
	touch w_web.txt
	java -cp _tmp_:$(JUNIT) terse.web.WebServer 8000

reweb: _make_vm_
	java -cp _tmp_:$(JUNIT) terse.web.WebServer 8000
