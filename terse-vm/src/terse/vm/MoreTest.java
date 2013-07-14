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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import terse.vm.Terp;
import terse.vm.Terp.Frame;
import terse.vm.Ur.Bytes;
import terse.vm.Ur.Obj;
import junit.framework.TestCase;

public class MoreTest extends TestCase {
	Terp t;
	Frame f;

	public MoreTest(String arg0) throws IOException {
		super(arg0);
			t = new Terp.PosixTerp(false, "");
	}
	
	private Ur eval(String code) {
        return t.newTmp().eval(code);
	}

	public void testHexEn() {
		Ur u = eval("Hex en: '1 2 3' bytes");
		assertEquals("3120322033", u.toString());
	}

	public void testHexDe() {
		Ur u = eval("Hex de: '3120322033' bytes");
		assertEquals("1 2 3", u.toString());
	}

	public void testUtf8En() {  // TODO: confirm this is correct.
		Ur u = eval("Hex en: (Utf8 en: '\u0001\u007f\u0088\u0888\u8888')");
		assertEquals("017fc288e0a288e8a288", Static.Low8ToString(((Bytes)u).bytes) );
	}

	public void testUtf8De() {  // TODO: confirm this is correct.
		Ur u = eval("Utf8 de: (Hex de: '017fc288e0a288e8a288' bytes)");
		assertEquals("'\u0001\u007f\u0088\u0888\u8888'", u.repr());
	}

	public void testCurlyEn() {
		Ur u = eval("Curly en: '\u11111 {2b} 3\r\n'");
		assertEquals("{4369}1{32}{123}2b{125}{32}3{13}{10}", u.toString());
	}

	public void testCurlyDe() {
		Ur u = eval("Curly de: '{4369}1{32}{123}2b{125}{32}3{13}{10}' bytes");
		assertEquals("\u11111 {2b} 3\r\n", u.toString());
	}

	public void testDhSecret() {
		Ur u = eval("d1= DhSecret rand . d2= DhSecret rand . m1= d1 mutual: d2 pub . m2 = d2 mutual: d1 pub . m1 equals: m2");
		assertEquals("1", u.repr());
	}

	public void testAES() {
		Ur u = eval("key= DhSecret rand str bytes tail: 16 . a= AES key: key . p= 'abcdefghijklmnopqr' bytes . c= a en: p . p equals: (a de: (c))");
		assertEquals("1", u.repr());
	}

	public void testSHA1() {
		Ur u = eval("Hex en: (Sha1 en: 'testing' bytes)");
		assertEquals("dc724af18fbdd4e59189f5fe768a5f8311527050", u.toString());
		// Confirm with $ echo -n testing | sha1sum -> dc724af18fbdd4e59189f5fe768a5f8311527050 -
	}
	
	public void testJsonEn() {
		Ur u = eval("Json en: DICT('abc', VEC(1, 2, 3); 555, VEC('five', nil, 'five');)");
		
		String expected = "{555.0:[\"five\", null, \"five\"],\n" +
"\"abc\":[1.0, 2.0, 3.0]}\n";
		
		assertEquals(expected, u.toString());
	}
	
	public void testJsonDe() {
		Ur u = eval("Json de: '{\"abc\": [1.0, 2.0, 3.0], 555.0: [\"five\", null, \"five\"]}'");
		assertEquals("DICT((555), (VEC('five'; Nil; 'five'; ) ); ('abc'), (VEC(1; 2; 3; ) ); ) ", u.toString());
	}
	
	public void testPickle() {
		Ur a = eval(
				"USR defSub:'Foo'. Foo defSub:'Bar'." +
				"Foo defVars: 'p q'. Bar defVars: 'r s t'." +
				"Foo defmeth:'pq:' a:'' d:'' c:'p,q=a. me'. " +
				"Bar defmeth:'rst:' a:'' d:'' c:'r,s,t=a. me'. " +
				"(f= Foo new) pq: 5@10.   (b= Bar new) pq: 7@21 $ rst: f@b@nil. " +
				"Pickle en: b.  "
				);
		String result = a.toString().replaceAll("[@][0-9]+[@]", "@@");
		
		String expected = "{\"0@@Bar\":{\"p\":7.0,\n" +
"\"q\":21.0,\n" +
"\"r\":\"1@@Foo\",\n" +
"\"s\":\"0@@Bar\",\n" +
"\"t\":null}\n" +
",\n" +
"\"1@@Foo\":{\"p\":5.0,\n" +
"\"q\":10.0}\n" +
"}\n";
		
		assertEquals(expected, result);
		
		Usr b = (Usr) eval(Static.fmt("Pickle de: '%s'", result));
		
		assertEquals("Bar", b.cls.cname);
		assertEquals("28", b.eval("p + q").toString());
		
		assertEquals("Foo", b.eval("r cls").toString());
		assertEquals("15", b.eval("r eval: 'p+q'").toString());
	}
}
