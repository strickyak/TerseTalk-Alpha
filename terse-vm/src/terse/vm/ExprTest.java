// --------------------------------------------------------------------------
// Copyright (c) 2012 Henry Strickland & Thomas Shanks
// 
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
// OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
// ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
// --------------------------------------------------------------------------
package terse.vm;

import terse.vm.Terp;
import terse.vm.Terp.Frame;
import terse.vm.Ur.Vec;
import junit.framework.TestCase;

public class ExprTest extends TestCase {
    Terp t;
    Cls usrCls;
    Usr usrInst;

    protected void setUp() throws Exception {
        super.setUp();
        t = new Terp.PosixTerp(false, "" /* no name for tests */);
        usrCls = t.tUsr;
        usrInst = new Usr(t.tUsr);
    }
    protected Ur eval(Expr.MethTop top) {
        Frame f = t.newFrame(null, usrInst, top);
        return top.eval(f);
    }
    protected Ur evalExpectingExceptionsWhileProcessing(Expr.MethTop top) {
		Ur z = null;
    	try {
        	++ t.expectingTerseException;
    		z = eval(top);
    	} finally {
    		--t.expectingTerseException;
    	}
        return z;
    }

    public void testLit888() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " 888 ");
        t.say("8888888888 -> TOP %s", top);
        Ur obj = eval(top);
        assertEquals(888.0, obj.asNum().num);
    }
    public void testLitString() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " 'foo''bar' ");
        Ur obj = eval(top);
        assertEquals("foo'bar", obj.asStr().str);
    }
    public void testTwoStmts() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " . 23 . 42 . . . ");
        assertEquals(42.0, eval(top).asNum().num);
    }
    public void testUnaryNeg() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " . 23 . 42 neg . . . ");
        assertEquals(-42.0, eval(top).asNum().num);
    }
    public void testUnaryNegSgn() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " . 23 . 42 neg sgn ");
        assertEquals(-1.0, eval(top).asNum().num);
    }
    public void testPlusTwoNums() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " 23 + 42  ");
        assertEquals(65.0, eval(top).asNum().num);
    }
    public void testPlusTwoNumsWithNeg() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " 23 + 42 neg  ");
        assertEquals(-19.0, eval(top).asNum().num);
    }
    public void testPlusTwoNumsWithOtherNeg() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " 23 neg + 42  ");
        assertEquals(19.0, eval(top).asNum().num);
    }
    public void testStoreAndFetch() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " n= 23 + 42 . 13 . n  ");
        assertEquals(65.0, eval(top).asNum().num);
    }
    public void testBlock() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " [ 23 + 42 . ] value  ");
        assertEquals(65.0, eval(top).asNum().num);
    }
    public void testYesNoToTrue() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " 1 y: [ e= 42 ] . 1 n: [ e= 13 ] . e");
        assertEquals(42.0, eval(top).asNum().num);
    }
    public void testYesNoToFalse() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " 0 y: [ a= 42 ] . 0 n: [ a= 13 ] . a");
        assertEquals(13.0, eval(top).asNum().num);
    }
    public void testDefiningUsrSubclass() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " Usr defSub: 'Foo' . Foo new cls name");
        assertEquals("Foo", eval(top).asStr().str);
    }
    public void testDefiningUsrMethod() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " Usr defsub: 'Foo' . "
                + "Foo defmeth: 'xyz' a: '' d: '' c: '888' . " + "Foo new xyz ");
        assertEquals(888.0, eval(top).asNum().num);
    }
    public void testInstVar() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", "Usr defSub: 'Foo' . " + "Foo defVars: ' x ' . "
                + "Foo defineMethod: 'stor:' abbrev: '' doc: 'Store in x.' code: 'x= a' . "
                + "Foo defmeth: 'rcl' a: '' d: '' c: 'x' . " + "h = Foo new . " + "h stor: 56 . " + "h rcl neg ");
        assertEquals(-56.0, eval(top).asNum().num);
    }
    public void testVec() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", "a= Vec(0, 0, 0, 0, 0). a at: 3 put: 33 . a at: 3 ");
        assertEquals(33.0, eval(top).asNum().num);
    }
    public void testDict() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", "a= Dict new . a at: 'foo' put: 333 . a at: 'foo' ");
        assertEquals(333.0, eval(top).asNum().num);
    }
    public void testEq() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " 5 eq: 2 ");
        assertEquals(t.instFalse, eval(top));
    }
    public void testNe() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " 5 ne: 2 ");
        assertEquals(t.instTrue, eval(top));
    }
    public void testLt() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " 5 lt: 2 ");
        assertEquals(t.instFalse, eval(top));
    }
    public void testLe() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " 5 le: 5 ");
        assertEquals(t.instTrue, eval(top));
    }
    public void testGt() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " 5 gt: 2 ");
        assertEquals(t.instTrue, eval(top));
    }
    public void testGe() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " 5 ge: 6 ");
        assertEquals(t.instFalse, eval(top));
    }
    public void testStrEq() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " 'a' eq: 'x' ");
        assertEquals(t.instFalse, eval(top));
    }
    public void testStrNe() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " 'x' ne: 'xx' ");
        assertEquals(t.instTrue, eval(top));
    }
    public void testStrLt() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " 'xxx' lt: 'xxx' ");
        assertEquals(t.instFalse, eval(top));
    }
    public void testStrLe() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " 'xx' le: 'xxx' ");
        assertEquals(t.instTrue, eval(top));
    }
    public void testStrGt() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " '5' gt: '20' ");
        assertEquals(t.instTrue, eval(top));
    }
    public void testStrGe() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " '' ge: '!' ");
        assertEquals(t.instFalse, eval(top));
    }
    public void testDoForRange() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " s= 0 . 5 do: [i : s= s + i ] . s ");
        assertEquals(10.0, eval(top).asNum().num);
    }
    public void testDoForVec() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " s = 0.  v = Vec ap: 3. v do: [i : s= s + i ] . s ");
        assertEquals(3.0, eval(top).asNum().num);
    }
    public void testStrSplit() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " 'one:more:time' split: ':' $ at: 2 ");
        assertEquals("time", eval(top).asStr().str);
    }
    public void testBuf() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " Buf ap: 'one:' ,, ap: 'more:' $ ap: 'time' $ str");
        assertEquals("one:more:time", eval(top).asStr().str);
    }
    public void testBlkToVec() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", "( 1 + 1 ; 2 + 2 ; 3 + 3 ; 4 + 4 ) at: 2 ");
        assertEquals(6.0, eval(top).asNum().num);
    }
    public void testTriangleNumber() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", "USR defSub: 'Foo' . Foo trace: 1 . "
                + " Foo defmeth: 'trIaNgle:' a: '' d: '' c:  ' " + "   g = 0 . "
                + "   a gt: 0 $ y: [ g = se triangle: ( a - 1 ) $+ a  ] . " + "   g' . "
                + "   Foo new TRIANGLE: 6  ");
        assertEquals(21.0, eval(top).asNum().num);
    }
    public void testSuper() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", "USR defSub:'Foo'. Foo defSub:'Bar'. "
                + "Foo defmeth:'mumble' a:'' d:'' c:' ''Hello'' '.  "
                + "Bar defmeth:'mumble' a:'' d:'' c:' super mumble ap: '' World'' '.  " + "Bar new mumble");
        assertEquals("Hello World", eval(top).asStr().str);
    }
    public void testMacroVec() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", "Vec() len; Vec(Vec('mumble')) len; Vec(3,4,5,6) len");
        assertEquals("VEC(0; 1; 4; ) ", eval(top).toString());
    }
    public void testMacroDict() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " dict( 1 + 1, 2 + 2; 3 + 3, 4 + 4; ) at: 2 ");
        assertEquals(4.0, eval(top).asNum().num);
    }
    public void testMacroIfThenElseT() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", "IF(2 <= 2)THEN(42)ELSE(23)");
        assertEquals("42", eval(top).toString());
    }
    public void testMacroIfThenElseF() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", "IF(2 < 2)THEN(42)ELSE(23)");
        assertEquals("23", eval(top).toString());
    }
    public void testMacroForDo() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", "s=0. for(i:100)do(s=s+i+1). s");
        assertEquals("5050", eval(top).toString());
    }
    public void testMacroForMap() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", "for(i:6)map(i*i)");
        assertEquals("VEC(0; 1; 4; 9; 16; 25; ) ", eval(top).toString());
    }
    public void testMacroForInitReduce() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", "for(i:101)init(s:0)reduce(s+i)");
        assertEquals("5050", eval(top).toString());
    }
    public void testMacroOr() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", "n = 4. OR(n == 3; n == 4;)");
        assertEquals("1", eval(top).toString());
    }
    public void testMacroOr2() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", "n = 3. OR(n == 3; n == 4;)");
        assertEquals("1", eval(top).toString());
    }
    public void testMacroOr3() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", "n =2. OR(n == 3; n == 4;)");
        assertEquals("0", eval(top).toString());
    }
    public void testRexF() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", "r = Rex new: '([a-z]*)([0-9]*)\\.txt'.  r match: '###'. ");
        assertEquals("Nil", eval(top).toString());
    }
    public void testRexT() {
        Expr.MethTop top = Parser.parseMethod(usrCls, "temp", "r = Rex new: '([a-z]*)([0-9]*)\\.txt'.  r match: 'abc123.txt'. ");
        assertEquals("VEC('abc123.txt'; 'abc'; '123'; ) ", eval(top).toString());
    }
    public void testInstVars() {
    	Expr.MethTop top = Parser.parseMethod(usrCls, "temp", "Usr defSub: 'Foo'. Foo defSub: 'Bar'. Foo defVars: 'p q'. Bar defVars: 'r s t'. Foo new, Bar new.");
    	Vec pair = eval(top).mustVec();
    	Usr x = (Usr) pair.vec.get(0);
    	Usr y = (Usr) pair.vec.get(1);
    	assertEquals("Foo", x.cls.cname);
    	assertEquals("Bar", y.cls.cname);
    	assertEquals("p", x.cls.myVarNames[0]);
    	assertEquals("q", x.cls.myVarNames[1]);
    	assertEquals("r", y.cls.myVarNames[0]);
    	assertEquals("s", y.cls.myVarNames[1]);
    	assertEquals("t", y.cls.myVarNames[2]);
    }
    public void testTryCatch() {
		Expr.MethTop top = Parser.parseMethod(usrCls, "temp", "x=3. TRY(x=4. x=5/0 - 7/0.) CATCH(e: x='got:' ap: e).");
        assertEquals("got:IsInfinite", evalExpectingExceptionsWhileProcessing(top).toString().split("\n")[0]);
    }
    public void testAt() {
		Expr.MethTop top = Parser.parseMethod(usrCls, "temp", "a,b,c= 1 + 1 @ 2 * 2 @ 4 - 1. a + c - b");
        assertEquals("1", eval(top).toString());
    }
    public void testFailTwoLiterals() {
    	String err = null;
    	t.expectingTerseException = 1;
    	try {
    		Expr.MethTop top = Parser.parseMethod(usrCls, "temp", " 23 42 ");
    	} catch (RuntimeException e) {
    		err = e.toString();
    	}
        assert(err.matches("Leftover word after parsing"));
    }
}
