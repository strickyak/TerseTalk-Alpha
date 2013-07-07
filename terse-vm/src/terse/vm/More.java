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
	
//	public static final class OldAes extends Obj {
//		byte[] key;
//		byte[] iv;
//		CryptoMode mode;
//		
//		enum CryptoMode { ECB, CBC, CHUNK };
//		
//		// =Xcls "more" OldAes Obj
//		public OldAes(Cls cls, byte[] key, CryptoMode mode) {
//			super(cls);
//			if (key.length != 16) {
//				toss("OldAes requres 16 byte key, not %d bytes", key.length);
//			}
//			this.key = key;
//			this.iv = null;
//			this.mode = mode;
//		}
//		
//		// =Xmeth OldAesCls "ctor" ecb:
//		public static OldAes ecb_(Terp terp, Bytes key) {
//			OldAes z = new OldAes(terp.wrap.clsOldAes, key.bytes, CryptoMode.ECB);
//			return z;
//		}
//		
//		// =Xmeth OldAesCls "ctor" cbc:iv:
//		public static OldAes cbc_iv_(Terp terp, Bytes key, Bytes iv) {
//			OldAes z = new OldAes(terp.wrap.clsOldAes, key.bytes, CryptoMode.ECB);
//			if (iv.bytes.length != 16) {
//				z.toss("OldAes requres 16 byte iv, not %d bytes", iv.bytes.length);
//			}
//			z.iv = iv.bytes;
//			return z;
//		}
//		
//		// =Xmeth OldAesCls "ctor" chunk:
//		public static OldAes chunk_(Terp terp, Bytes key) {
//			OldAes z = new OldAes(terp.wrap.clsOldAes, key.bytes, CryptoMode.ECB);
//			return z;
//		}
//	}
	
	


/// 	private static int next_oid = Math.abs((int) System.currentTimeMillis()) % 1499999999 + 101;
/// 
/// 	// inst
/// 	private int usr_oid_or_zero = 0; // Transient id, while in memory.
/// 	public String usr_oname = null; // For saved, persistent objects.
/// 
/// 	// =cls "usr" Usr Obj
/// 	public Usr(Cls cls) {
/// 		super(cls);
/// 	}
/// 
/// 	@Override
/// 	public UsrCls usrCls() {
/// 		return (UsrCls) cls;
/// 	}
/// 
/// 	// =meth Usr "usr" opath
/// 	public String opath() {
/// 		return oname() + "@" + cls.cname;
/// 	}
/// 
/// 	// =meth Usr "usr" oname
/// 	public String oname() {
/// 		if (usr_oname == null) {
/// 			return Integer.toString(usr_oid_or_zero);
/// 		} else {
/// 			return usr_oname;
/// 		}
/// 	}
/// 
/// 	// =meth Usr "access" oid
/// 	public int oid() {
/// 		if (usr_oid_or_zero == 0) {
/// 			usr_oid_or_zero = next_oid;
/// 			++next_oid;
/// 		}
/// 		usrCls().cache.store(this);
/// 		return usr_oid_or_zero;
/// 	}
/// 
/// 	// =meth Usr "access" omention
/// 	public int omention() {
/// 		int z = oid();
/// 		usrCls().cache.store(this);
/// 		return z;
/// 	}
/// 
/// 	public void visit(Visitor v) {
/// 		v.visitUsr(this);
/// 	}
/// 
/// 	public Usr asUsr() {
/// 		return this;
/// 	}
/// 
/// 	public static class Tmp extends Usr {
/// 		// =cls "usr" Tmp Usr
/// 		Tmp(Terp t) {
/// 			super(t.tTmp);
/// 		}
/// 	}
/// 
/// 	public static class LRUsr {
/// 		Usr[] memory; // High index is front; 0 is back.
/// 		int size;
/// 
/// 		public LRUsr(int size) {
/// 			this.size = size;
/// 			this.memory = new Usr[size];
/// 		}
/// 
/// 		public void store(Usr u) {
/// 			for (int i = 0; i < size; i++) {
/// 				if (memory[i] == u) {
/// 					// Move u to front, so slide the rest 1 step to back.
/// 					for (int j = i; j < size - 1; j++) {
/// 						memory[j] = memory[j + 1];
/// 					}
/// 					memory[size - 1] = u;
/// 					return;
/// 				}
/// 			}
/// 			// Move everything 1 step to back.
/// 			for (int i = 0; i < size - 1; i++) {
/// 				memory[i] = memory[i + 1];
/// 			}
/// 			memory[size - 1] = u;
/// 		}
/// 
/// 		public Usr find(int id) {
/// 			for (int i = 0; i < size; i++) {
/// 				Usr obj = memory[i];
/// 				if (obj != null && obj.oid() == id) {
/// 					return obj;
/// 				}
/// 			}
/// 			return null; // Not found.
/// 		}
/// 
/// 		public Usr find(String name) {
/// 			if (name.matches("[0-9]+")) {
/// 				// Numeric name; use int.
/// 				int id = Integer.parseInt(name);
/// 				for (int i = 0; i < size; i++) {
/// 					Usr obj = memory[i];
/// 					if (obj != null && obj.oid() == id) {
/// 						return obj;
/// 					}
/// 				}
/// 				return null; // Not found.
/// 			}
/// 			// Use String.
/// 			for (int i = 0; i < size; i++) {
/// 				Usr obj = memory[i];
/// 				if (obj != null && obj.usr_oname == name) {
/// 					return obj;
/// 				}
/// 			}
/// 			return null; // Not found.
/// 		}
/// 	}
/// 
/// 	public static Pattern JUST_NAME = Pattern.compile("([-a-z0-9]+)$",
/// 			Pattern.CASE_INSENSITIVE);
/// 	public static Pattern NAME_WITH_WORLD = Pattern.compile(
/// 			"([-a-z0-9]+)_([a-z][a-z][a-z][0-9]{0,3})$",
/// 			Pattern.CASE_INSENSITIVE);
/// 
/// 	public static class UsrCls extends Cls {
/// 		LRUsr cache = new LRUsr(32);
/// 
/// 		HashMap<String, Usr> savedInsts = new HashMap<String, Usr>();
/// 		HashMap<String, String> savedInstsUnrealized = new HashMap<String, String>();
/// 
/// 		// =cls "meth" UsrCls Cls
/// 		UsrCls(Cls cls, Terp terp, String name, Cls supercls) {
/// 			super(cls, terp, name, supercls);
/// 		}
/// 
/// 		// =meth UsrCls "new" new
/// 		public Usr _new() {
/// 			Usr z = new Usr(this);
/// 			z.omention();
/// 			return z;
/// 		}
/// 
/// 		public Obj findById(int id) {
/// 			Usr z = cache.find(id);
/// 			return z == null ? terp.instNil : z;
/// 		}
/// 
/// 		// =meth UsrCls "usr" find:
/// 		public Obj find(String idOrName) {
/// 			// Returns Nil or a Usr, therefore returns Obj.
/// 			Usr z = cache.find(idOrName);
/// 			if (z != null) {
/// 				return z;
/// 			}
/// 			// If just the name, without world, append current worldName.
/// 			Matcher match_jn = JUST_NAME.matcher(idOrName);
/// 			if (match_jn.lookingAt()) {
/// 				idOrName += "_" + terp.worldName;
/// 			}
/// 			// Look up the name with world.
/// 			Matcher match_nww = NAME_WITH_WORLD.matcher(idOrName);
/// 			if (match_nww.lookingAt()) {
/// 				Usr x = savedInsts.get(idOrName.toLowerCase());
/// 				if (x != null) {
/// 					return x;
/// 				}
/// 				String state = savedInstsUnrealized.get(idOrName.toLowerCase());
/// 				if (state != null) {
/// 					x = constructFromSavedString(state);
/// 					savedInsts.put(idOrName.toLowerCase(), x);
/// 					return x;
/// 				}
/// 			}
/// 			return terp.instNil;
/// 		}
/// 
/// 		public Usr constructFromSavedString(String state) {
/// 			// For now, it's just a dict of field values.
/// 			Dict d = eval(state).mustDict();
/// 			Usr z = new Usr(this);
/// 			z.pokeInstVarsDict_(d);
/// 			return z;
/// 		}
/// 	}
}
