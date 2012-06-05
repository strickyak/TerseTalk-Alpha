# --------------------------------------------------------------------------
# Copyright (c) 2012 Henry Strickland & Thomas Shanks
# 
# Permission is hereby granted, free of charge, to any person obtaining a
# copy of this software and associated documentation files (the "Software"),
# to deal in the Software without restriction, including without limitation
# the rights to use, copy, modify, merge, publish, distribute, sublicense,
# and/or sell copies of the Software, and to permit persons to whom the
# Software is furnished to do so, subject to the following conditions:
# 
# The above copyright notice and this permission notice shall be included
# in all copies or substantial portions of the Software.
# 
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
# THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
# OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
# ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
# OTHER DEALINGS IN THE SOFTWARE.
# --------------------------------------------------------------------------

all : Wrap.java ../../../../terse-web/prelude.txt ../../../../terse-a1/res/raw/prelude.txt ANDY

J = Ur.java Terp.java Expr.java Cls.java Parser.java Usr.java
	
Wrap.java : wrap.tcl $J
	tclsh wrap.tcl Wrap $J > Wrap.java

ANDY:
	cd ../../../../terse-a1/src/terse/a1 ; make

../../../../terse-web/prelude.txt : ../../../prelude.txt
	cp ../../../prelude.txt ../../../../terse-web/prelude.txt

../../../../terse-a1/res/raw/prelude.txt : ../../../prelude.txt
	mkdir -p ../../../../terse-a1/res/raw
	cp ../../../prelude.txt ../../../../terse-a1/res/raw/prelude.txt
