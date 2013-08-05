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

import terse.vm.Expr.MethTop;
import terse.vm.Parser.TLex;
import terse.vm.Terp;
import junit.framework.TestCase;

public class ParserTest extends TestCase {
	Terp t;

	protected void setUp() throws Exception {
		super.setUp();
		t = new Terp.PosixTerp(false, "" /* no name for tests */);
	}

	public void testTLex() {
		TLex lex = new TLex(t, "  88.5 ");
		assertEquals(Parser.Pat.NUMBER, lex.t);
		assertEquals("88.5", lex.w);
	}

	public void testTuple() {
		Cls cls = t.tTmp;
		MethTop a = Parser.parseMethod(cls, "bogus", " 111, 222, 333 ");
		assertEquals(" (111, 222, 333, ) ", a.body.toString());
	}

	public void testTupleList() {
		Cls cls = t.tTmp;
		MethTop a = Parser.parseMethod(cls, "bogus", " 111, 222; 333, 444; ");
		assertEquals(" ( (111, 222, ) ;  (333, 444, ) ; ) ", a.body.toString());
	}

	public void testSimpleBinop() {
		Cls cls = t.tTmp;
		MethTop a = Parser.parseMethod(cls, "bogus", " 111 * 2, 222 + 3, 333 > 4");
		assertEquals(" (111 * 2 , 222 + 3 , 333 > 4 , ) ", a.body.toString());
	}
	public void testLayeredBinop() {
		Cls cls = t.tTmp;
		MethTop a = Parser.parseMethod(cls, "bogus", "0 at: 1 != 2 + 3 * 4");
		assertEquals("0 at: (1 != (2 + (3 * 4 ) ) ) ", a.body.toString());
	}
	public void testLayeredBinopMore() {
		Cls cls = t.tTmp;
		MethTop a = Parser.parseMethod(cls, "bogus", "0 at: 1 != 2 + 3 * 4 * 5 + 6 == 7");
		assertEquals("0 at: ((1 != ((2 + ((3 * 4 ) * 5 ) ) + 6 ) ) == 7 ) ", a.body.toString());
	}	
	public void testTrigraphs() {
		assertEquals("a<b>c", Parser.trigraphSubst("a/ltb/gtc"));
	}
	public void testBigraphs() {
		assertEquals("a:b;c;d$e=f(g)h", Parser.bigraphSubst("a..b,.c.,d,,e--f!g?h"));
	}
	public void testBigraphVsNotEqual() {
		assertEquals("a:b;c;d$e=f!=g", Parser.bigraphSubst("a..b,.c.,d,,e--f!=g"));
	}
	public void testAssignTuple() {
		assertEquals(t.newNum(25), t.newTmp().eval("x, y = Vec(3; 4;).   x * x + y * y."));
	}
	public void testInterpolate() {
		String s = t.newTmp().eval("x,y= 23,42.  'x[x]y[y]sum[x + y].[[z]].'").toString();
		assertEquals("x23y42sum65.[z].", s);
	}
}


