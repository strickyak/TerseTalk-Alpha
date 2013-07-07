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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;


import terse.vm.Cls.JavaMeth;
import terse.vm.Ur.Obj;
import terse.vm.Terp.Frame;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public abstract class More extends Static {
	static SecureRandom Rand = new SecureRandom();
	
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

				byte[] cyp = cipher.doFinal(plain.bytes);
				
				MessageDigest md = MessageDigest.getInstance("SHA-1");
				md.update(plain.bytes);
				byte[] digest = md.digest();
				
				byte[] out = new byte[cyp.length + 32];
				CopyBytes(iv, 0, 16, out, 0);  // First 16 bytes are iv
				CopyBytes(cyp, 0, cyp.length, out, 16);  // Payload with 1 to 16 bytes padding; final byte tells how many pads.
				CopyBytes(digest, 0, 16, out, out.length-16);  // Final 16 bytes are head 16 bytes of SHA-1.
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
						cypher.bytes.length - 16);
				byte[] expectedDigest = Arrays.copyOfRange(cypher.bytes,
						cypher.bytes.length - 16, cypher.bytes.length);

				IvParameterSpec ivSpec = new IvParameterSpec(iv);
				Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
				cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
				
				byte[] plain = cipher.doFinal(cyp);

				MessageDigest md = MessageDigest.getInstance("SHA-1");
				md.update(plain);
				byte[] digest = md.digest();
				
				if (!(digest.equals(expectedDigest))) {
					toss("Aes.de: Bad digest");
				}
				
				return new Bytes(terp(), plain);
				
			} catch (Exception e) {
				toss("AES.en: " + e);
				return null;
			}
		}
	}
}
