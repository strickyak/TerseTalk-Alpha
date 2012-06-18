all:  _make_vm_  _make_a1_

_make_vm_:
	cmp prelude.txt terse-vm/prelude.txt || cp prelude.txt terse-vm/prelude.txt
	cd ./terse-vm/src/terse/vm/ && make
_make_a1_:
	cd ./terse-a1/src/terse/a1/ && make
