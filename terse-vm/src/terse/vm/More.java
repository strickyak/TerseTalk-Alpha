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

import java.util.Arrays;
import java.util.HashMap;

import javax.crypto.Cipher;


import terse.vm.Ur.Obj;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public abstract class More extends Static {
	static SecureRandom Rand = new SecureRandom();
	
	public static final class Curly extends Obj {
		// =cls "more" Curly Obj
		public Curly(Cls cls) {
			super(cls);
			toss("Do not instantiate Curly.");
		}
		
		// =meth CurlyCls "encode" en:
		public static Bytes en_(Terp terp, String s) {
			return new Bytes(terp, StringToCurly(s));
		}
		
		// =meth CurlyCls "decode" de:
		public static Str de_(Terp terp, Bytes b) {
			return new Str(terp, CurlyToString(b.bytes));
		}
	}
	
	public static final class Utf8 extends Obj {
		// =cls "more" Utf8 Obj
		public Utf8(Cls cls) {
			super(cls);
			toss("Do not instantiate Utf8.");
		}
		
		// =meth Utf8Cls "encode" en:
		public static Bytes en_(Terp terp, String s) {
			return new Bytes(terp, StringToUtf8(s));
		}
		
		// =meth Utf8Cls "decode" de:
		public static Str de_(Terp terp, Bytes b) {
			return new Str(terp, Utf8ToString(b.bytes));
		}
	}
	
	public static final class Hex extends Obj {
		// =cls "more" Hex Obj
		public Hex(Cls cls) {
			super(cls);
			toss("Do not instantiate Hex.");
		}
		
		// =meth HexCls "encode" en:
		public static Bytes en_(Terp terp, Obj b) {
			if (b instanceof Bytes) {
				return new Bytes(terp, BytesToHex(((Bytes)b).bytes));
			} else if (b instanceof Str) {
				return new Bytes(terp, BytesToHex(StringToLow8(((Str)b).str)));
			} else {
				terp.toss("Hex.en: expects arg either Bytes or String, got %s", b.cls.toString());
				return null;
			}
		}
		
		// =meth HexCls "decode" de:
		public static Bytes de_(Terp terp, Bytes b) {
			return new Bytes(terp, HexToBytes(b.bytes));
		}
	}
	
	public static final class Sha1 extends Obj {
		// =cls "more" Sha1 Obj
		public Sha1(Cls cls) {
			super(cls);
			toss("Do not instantiate Sha1.");
		}
		
		// =meth Sha1Cls "digest" en:
		public static Bytes en_(Terp terp, Bytes b) {
			try {
				MessageDigest md = MessageDigest.getInstance("SHA-1");
				md.update(b.bytes);
				byte[] bb = md.digest();
				return new Bytes(terp, bb);
			} catch (NoSuchAlgorithmException e) {
				b.toss("Sha1: %s", e);
				return null;
			}
		}
	}
	
	public static final class DhSecret extends Obj {
        public static final int NumRandomBitsPerDHKey = 1535;
        public static final String Rfc3526Modulus1536Bits = ""
                        + "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1"
                        + "29024E088A67CC74020BBEA63B139B22514A08798E3404DD"
                        + "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245"
                        + "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED"
                        + "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D"
                        + "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F"
                        + "83655D23DCA3AD961C62F356208552BB9ED529077096966D"
                        + "670C354E4ABC9804F1746C08CA237327FFFFFFFFFFFFFFFF";
        /** Generator */
        public static BigInteger G = new BigInteger("2");
        /** Modulus */
        public static BigInteger M = new BigInteger(
                        Rfc3526Modulus1536Bits, 16);

		BigInteger secret;
		
		// =cls "more" DhSecret Obj
		private DhSecret(Cls cls) {
			super(cls);
		}
		
		// =meth DhSecretCls "ctor" new:
		public static DhSecret new_(Terp terp, String hex) {
			DhSecret z = new DhSecret(terp.wrap.clsDhSecret);
			z.secret = new BigInteger(hex, 16);
			return z;
		}
		
		// =meth DhSecretCls "ctor" rand
		public static DhSecret _rand(Terp terp) {
			DhSecret z = new DhSecret(terp.wrap.clsDhSecret);
			z.secret = new BigInteger(NumRandomBitsPerDHKey, Rand);
			return z;
		}
		
		@Override
		public String toString() {
			return secret.toString(16);
		}
		
		@Override
		public String repr() {
			return fmt("(DhSecret new: %s)", secret.toString(16));
		}
		
		// =meth DhSecret "dh" pub
		public String _pub() {
			return G.modPow(this.secret, M).toString(16);
		}
		
		// =meth DhSecret "dh" mutual:
		public String mutual_(String pub) {
			return new BigInteger(pub, 16).modPow(this.secret, M).toString(16);
		}
	}
	
	
	public static final class Aes extends Obj {
		SecretKeySpec keySpec;
		
		// =cls "more" Aes Obj
		public Aes(Cls cls, byte[] key) {
			super(cls);
			if (key.length != 16) {
				toss("OldAes requres 16 byte key, not %d bytes", key.length);
			}
            keySpec = new SecretKeySpec(key, 0, 16, "AES");
		}

		// =meth AesCls "ctor" key:
		public static Aes key_(Terp terp, Bytes key) {
			Aes z = new Aes(terp.wrap.clsAes, key.bytes);
			return z;
		}
		
		// =meth Aes "encrypt" en:
		public Bytes en_(Bytes plain) {
			try {
				byte[] iv = new byte[16];
				Rand.nextBytes(iv);
				IvParameterSpec ivSpec = new IvParameterSpec(iv);
				Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
				cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

				MessageDigest md = MessageDigest.getInstance("SHA-1");
				md.update(plain.bytes);
				byte[] digest = md.digest();

				byte[] c1 = cipher.update(digest, 0, 16);
				byte[] c2 = cipher.doFinal(plain.bytes);
				
				byte[] out = new byte[16 + c1.length + c2.length];
				CopyBytes(iv, 0, 16, out, 0);  // First 16 bytes are iv
				CopyBytes(c1, 0, c1.length, out, 16);
				CopyBytes(c2, 0, c2.length, out, 16 + c1.length);   // Payload with 1 to 16 bytes padding; final byte tells how many pads.
				return new Bytes(terp(), out);
			} catch (Exception e) {
				toss("AES.en: " + e);
				return null;
			}
		}
		
		// =meth Aes "decrypt" de:
		public Bytes de_(Bytes cypher) {
			try {
				byte[] iv = Arrays.copyOfRange(cypher.bytes, 0, 16);
				byte[] cyp = Arrays.copyOfRange(cypher.bytes, 16,
						cypher.bytes.length);

				IvParameterSpec ivSpec = new IvParameterSpec(iv);
				Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
				cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
				
				byte[] composite = cipher.doFinal(cyp);
				byte[] expected_digest = Arrays.copyOfRange(composite, 0, 16);
				byte[] plain = Arrays.copyOfRange(composite, 16, composite.length);

				MessageDigest md = MessageDigest.getInstance("SHA-1");
				md.update(plain);
				byte[] digest = md.digest();
				byte[] new_digest = Arrays.copyOfRange(digest, 0, 16);

				if (!(Arrays.equals(new_digest, expected_digest))) {
					toss("Aes.de: Bad digest");
				}
				
				return new Bytes(terp(), plain);
				
			} catch (Exception e) {
				toss("AES.en: " + e);
				return null;
			}
		}
	}
	
	public static final class Client extends Obj {
		String hostname;
		int port;
		
		// =cls "net" Client Obj
		public Client(Cls cls, String hostname, int port) {
			super(cls);
			this.hostname = hostname;
			this.port = port;
		}
		
		// =meth ClientCls "ctor" host:port:
		public static Client host_port_(Terp terp, String hostname, int port) {
			return new Client(terp.wrap.clsClient, hostname, port);
		}
		
		// =meth Client "net" get:query:
		public String get(String path, Dict query) {
			HashMap<String, String> map = new HashMap<String, String>();
			for (Ur k : query.dict.keySet()) {
				Ur v = query.dict.get(k);
				map.put(k.toString(), v.toString());
			}
			try {
				return go(path, map, null, false);
			} catch (Exception ex) {
				terp().toss("Client.get:query: throws %s", ex);
				return null;
			}
		}
		
		// =meth Client "net" post:query:form:
		public String post(String path, Dict query, Dict form) {
			HashMap<String, String> qmap = new HashMap<String, String>();
			for (Ur k : query.dict.keySet()) {
				Ur v = query.dict.get(k);
				qmap.put(k.toString(), v.toString());
			}
			HashMap<String, String> fmap = new HashMap<String, String>();
			for (Ur k : form.dict.keySet()) {
				Ur v = form.dict.get(k);
				fmap.put(k.toString(), v.toString());
			}
			try {
				return go(path, qmap, fmap, true);
			} catch (Exception ex) {
				terp().toss("Client.post:query:form: throws %s", ex);
				return null;
			}
		}
		
		
		public String go(String path, HashMap<String, String> query, HashMap<String, String> form, boolean post) throws Exception {
			if (path.charAt(0) != '/') throw new RuntimeException("path must begin with '/'");
			if (path.contains("?")) throw new RuntimeException("path cannot contain '?'");
			if (path.contains(";")) throw new RuntimeException("path cannot contain ';'");
			
			URL url = new URL("http", hostname, port, path + "?" + Static.makeQueryString(query));
			URLConnection conn = url.openConnection();
			conn.setDoOutput(post);
			conn.setDoInput(true);
			
			if (post) {
				OutputStream os = conn.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
				BufferedWriter bw = new BufferedWriter(osw);
				bw.write(Static.makeQueryString(form));
				bw.close();
			}
			
			InputStream is = conn.getInputStream();
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			StringBuilder sb = new StringBuilder();
			while (true) {
				String line = br.readLine();
				if (line == null) break;
				sb.append(line);
			}
			return sb.toString();
		}
	}
}
