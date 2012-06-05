package terse.vm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.regex.Pattern;

import terse.vm.Cls.JavaMeth;
import terse.vm.Cls.Meth;
import terse.vm.Cls.UsrMeth;
import terse.vm.Expr.Seq;
import terse.vm.Expr.Send;
import terse.vm.Expr.MethTop;
import terse.vm.Expr.LvName;
import terse.vm.Expr.LvTuple;
import terse.vm.Expr.LValue;
import terse.vm.Terp.Frame;

import terse.vm.Ur.Blk;
import terse.vm.Ur.Buf;
import terse.vm.Ur.Dict;
import terse.vm.Ur.Undefined;
import terse.vm.Ur.Num;
import terse.vm.Ur.Obj;
import terse.vm.Ur.Str;
import terse.vm.Ur.Sys;
import terse.vm.Ur.Vec;
import terse.vm.Usr.Tmp;
import terse.vm.Usr.UsrCls;

public class Wrap extends Static {

  public void installMethods(final Terp terp) {


    new JavaMeth(terp.wrap.clsUr, "cls", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        Ur self = (Ur) me;
        return self.cls;
      }
    };
    new JavaMeth(terp.wrap.clsUr, "peekInstVars", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        Ur self = (Ur) me;
        return new Vec(terp, self.instVars);
      }
    };
    // CMD meth Ur "access" repr TYPE String NAME repr PARAMS 
    // CLS Ur GROUP "access" METH repr REMARK ""

    new JavaMeth(terp.wrap.clsUr, "repr", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Ur self = (Ur) me;
        String z = self.repr ();
        return (z == null) ? terp.instNil : terp.newStr(z);
      }
    };

    // CMD meth Ur "access" str TYPE String NAME toString PARAMS 
    // CLS Ur GROUP "access" METH str REMARK ""

    new JavaMeth(terp.wrap.clsUr, "str", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Ur self = (Ur) me;
        String z = self.toString ();
        return (z == null) ? terp.instNil : terp.newStr(z);
      }
    };

    // CMD meth Ur "access" truth TYPE boolean NAME truth PARAMS 
    // CLS Ur GROUP "access" METH truth REMARK ""

    new JavaMeth(terp.wrap.clsUr, "truth", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Ur self = (Ur) me;
        return terp.boolObj(self.truth ());
      }
    };

    // CMD meth Ur "access" hash TYPE int NAME hashCode PARAMS 
    // CLS Ur GROUP "access" METH hash REMARK ""

    new JavaMeth(terp.wrap.clsUr, "hash", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Ur self = (Ur) me;
        return terp.newNum(self.hashCode ());
      }
    };

    // CMD meth Ur "cmp" equals: TYPE boolean NAME equals PARAMS {Object obj}
    // CLS Ur GROUP "cmp" METH equals: REMARK ""

    new JavaMeth(terp.wrap.clsUr, "equals:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Ur self = (Ur) me;
        Object a = (Object) args[0];
        return terp.boolObj(self.equals (a));
      }
    };

    // CMD meth Ur "debug" pokeInstVarsDict: TYPE void NAME pokeInstVarsDict_ PARAMS {Dict d}
    // CLS Ur GROUP "debug" METH pokeInstVarsDict: REMARK ""

    new JavaMeth(terp.wrap.clsUr, "pokeInstVarsDict:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Ur self = (Ur) me;
        Dict a = (Dict) args[0];
        self.pokeInstVarsDict_ (a);
        return self;
      }
    };

    // CMD meth Ur "debug" peekInstVarsDict TYPE Dict NAME _peekInstVarsDict PARAMS 
    // CLS Ur GROUP "debug" METH peekInstVarsDict REMARK ""

    new JavaMeth(terp.wrap.clsUr, "peekInstVarsDict", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Ur self = (Ur) me;
        return self._peekInstVarsDict ();
      }
    };

    // CMD meth Ur "debug" dumpVarMap TYPE void NAME _dumpVarMap PARAMS 
    // CLS Ur GROUP "debug" METH dumpVarMap REMARK ""

    new JavaMeth(terp.wrap.clsUr, "dumpVarMap", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Ur self = (Ur) me;
        self._dumpVarMap ();
        return self;
      }
    };

    // CMD meth Obj "math" nearestInt  "convert a Num to the nearest int, by adding 0.5 and flooring" TYPE int NAME toNearestInt PARAMS 
    // CLS Obj GROUP "math" METH nearestInt REMARK "convert a Num to the nearest int, by adding 0.5 and flooring"

    new JavaMeth(terp.wrap.clsObj, "nearestInt", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        return terp.newNum(self.toNearestInt ());
      }
    };

    // CMD meth Obj "macro" macro:cond:  "return first X whose P is true, with body of the form P1,X2;P2,X2;P3,X3;..." TYPE Ur NAME macroCond PARAMS {Frame f} { Blk b}
    // CLS Obj GROUP "macro" METH macro:cond: REMARK "return first X whose P is true, with body of the form P1,X2;P2,X2;P3,X3;..."

    new JavaMeth(terp.wrap.clsObj, "macro:cond:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Frame a = (Frame) args[0];
        Blk b = (Blk) args[1];
        return self.macroCond (a, b);
      }
    };

    // CMD meth Obj "macro" macro:case:of: TYPE Ur NAME macroCaseOf PARAMS {Frame f} { Blk b} { Blk c}
    // CLS Obj GROUP "macro" METH macro:case:of: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "macro:case:of:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Frame a = (Frame) args[0];
        Blk b = (Blk) args[1];
        Blk c = (Blk) args[2];
        return self.macroCaseOf (a, b, c);
      }
    };

    // CMD meth Obj "macro" macro:case:of:else: TYPE Ur NAME macroCaseOfElse PARAMS {Frame f} { Blk b} { Blk c} { Blk d}
    // CLS Obj GROUP "macro" METH macro:case:of:else: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "macro:case:of:else:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Frame a = (Frame) args[0];
        Blk b = (Blk) args[1];
        Blk c = (Blk) args[2];
        Blk d = (Blk) args[3];
        return self.macroCaseOfElse (a, b, c, d);
      }
    };

    // CMD meth Obj "macro" macro:try:catch: TYPE Ur NAME macroTryCatch PARAMS {Frame f} { Blk b} { Blk c}
    // CLS Obj GROUP "macro" METH macro:try:catch: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "macro:try:catch:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Frame a = (Frame) args[0];
        Blk b = (Blk) args[1];
        Blk c = (Blk) args[2];
        return self.macroTryCatch (a, b, c);
      }
    };

    // CMD meth Obj "macro" macro:vec: "Execute the block to return a vector." TYPE Ur NAME macroVec PARAMS {Frame _} { Blk b}
    // CLS Obj GROUP "macro" METH macro:vec: REMARK "Execute the block to return a vector."

    new JavaMeth(terp.wrap.clsObj, "macro:vec:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Frame a = (Frame) args[0];
        Blk b = (Blk) args[1];
        return self.macroVec (a, b);
      }
    };

    // CMD meth Obj "macro" macro:dict: "Construct a Dict from list of pairs." TYPE Ur NAME macroDict PARAMS {Frame _} { Blk b}
    // CLS Obj GROUP "macro" METH macro:dict: REMARK "Construct a Dict from list of pairs."

    new JavaMeth(terp.wrap.clsObj, "macro:dict:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Frame a = (Frame) args[0];
        Blk b = (Blk) args[1];
        return self.macroDict (a, b);
      }
    };

    // CMD meth Obj "macro" macro:while:do: TYPE void NAME macroWhileDo PARAMS {Frame _} { Blk b} { Blk c}
    // CLS Obj GROUP "macro" METH macro:while:do: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "macro:while:do:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Frame a = (Frame) args[0];
        Blk b = (Blk) args[1];
        Blk c = (Blk) args[2];
        self.macroWhileDo (a, b, c);
        return self;
      }
    };

    // CMD meth Obj "macro" macro:for:do: TYPE Ur NAME macroForDo PARAMS {Frame _} { Blk b} { Blk c}
    // CLS Obj GROUP "macro" METH macro:for:do: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "macro:for:do:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Frame a = (Frame) args[0];
        Blk b = (Blk) args[1];
        Blk c = (Blk) args[2];
        return self.macroForDo (a, b, c);
      }
    };

    // CMD meth Obj "macro" macro:for:map: TYPE Ur NAME macroForMap PARAMS {Frame _} { Blk b} { Blk c}
    // CLS Obj GROUP "macro" METH macro:for:map: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "macro:for:map:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Frame a = (Frame) args[0];
        Blk b = (Blk) args[1];
        Blk c = (Blk) args[2];
        return self.macroForMap (a, b, c);
      }
    };

    // CMD meth Obj "macro" macro:for:map:if: TYPE Ur NAME macroForMapIf PARAMS {Frame _} { Blk b} { Blk c} { Blk d}
    // CLS Obj GROUP "macro" METH macro:for:map:if: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "macro:for:map:if:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Frame a = (Frame) args[0];
        Blk b = (Blk) args[1];
        Blk c = (Blk) args[2];
        Blk d = (Blk) args[3];
        return self.macroForMapIf (a, b, c, d);
      }
    };

    // CMD meth Obj "macro" macro:for:init:reduce: TYPE Ur NAME macroForMap PARAMS {Frame _} { Blk b} { Blk init} { Blk c}
    // CLS Obj GROUP "macro" METH macro:for:init:reduce: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "macro:for:init:reduce:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Frame a = (Frame) args[0];
        Blk b = (Blk) args[1];
        Blk c = (Blk) args[2];
        Blk d = (Blk) args[3];
        return self.macroForMap (a, b, c, d);
      }
    };

    // CMD meth Obj "macro" macro:if:then: TYPE Ur NAME macroIfThenElse PARAMS {Frame _} { Blk b} { Blk c}
    // CLS Obj GROUP "macro" METH macro:if:then: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "macro:if:then:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Frame a = (Frame) args[0];
        Blk b = (Blk) args[1];
        Blk c = (Blk) args[2];
        return self.macroIfThenElse (a, b, c);
      }
    };

    // CMD meth Obj "macro" macro:if:then:else: TYPE Ur NAME macroIfThenElse PARAMS {Frame _} { Blk b} { Blk c} { Blk d}
    // CLS Obj GROUP "macro" METH macro:if:then:else: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "macro:if:then:else:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Frame a = (Frame) args[0];
        Blk b = (Blk) args[1];
        Blk c = (Blk) args[2];
        Blk d = (Blk) args[3];
        return self.macroIfThenElse (a, b, c, d);
      }
    };

    // CMD meth Obj "macro" macro:if:then:elif:then:else: TYPE Ur NAME macroIfThenElse PARAMS {Frame _} { Blk b} { Blk c} { Blk d} { Blk e} { Blk f}
    // CLS Obj GROUP "macro" METH macro:if:then:elif:then:else: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "macro:if:then:elif:then:else:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Frame a = (Frame) args[0];
        Blk b = (Blk) args[1];
        Blk c = (Blk) args[2];
        Blk d = (Blk) args[3];
        Blk e = (Blk) args[4];
        Blk f = (Blk) args[5];
        return self.macroIfThenElse (a, b, c, d, e, f);
      }
    };

    // CMD meth Obj "macro" macro:if:then:elif:then: TYPE Ur NAME macroIfThenElse PARAMS {Frame _} { Blk b} { Blk c} { Blk d} { Blk e}
    // CLS Obj GROUP "macro" METH macro:if:then:elif:then: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "macro:if:then:elif:then:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Frame a = (Frame) args[0];
        Blk b = (Blk) args[1];
        Blk c = (Blk) args[2];
        Blk d = (Blk) args[3];
        Blk e = (Blk) args[4];
        return self.macroIfThenElse (a, b, c, d, e);
      }
    };

    // CMD meth Obj "macro" macro:and: TYPE Ur NAME macroAnd PARAMS {Frame _} { Blk b}
    // CLS Obj GROUP "macro" METH macro:and: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "macro:and:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Frame a = (Frame) args[0];
        Blk b = (Blk) args[1];
        return self.macroAnd (a, b);
      }
    };

    // CMD meth Obj "macro" macro:or: TYPE Ur NAME macroOr PARAMS {Frame _} { Blk b}
    // CLS Obj GROUP "macro" METH macro:or: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "macro:or:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Frame a = (Frame) args[0];
        Blk b = (Blk) args[1];
        return self.macroOr (a, b);
      }
    };

    // CMD meth Obj "macro" macro:ht:  "concat Str and Ht and (recursively) Vec, as Ht" TYPE Ht NAME macroHt PARAMS {Frame _} { Blk b}
    // CLS Obj GROUP "macro" METH macro:ht: REMARK "concat Str and Ht and (recursively) Vec, as Ht"

    new JavaMeth(terp.wrap.clsObj, "macro:ht:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Frame a = (Frame) args[0];
        Blk b = (Blk) args[1];
        return self.macroHt (a, b);
      }
    };

    // CMD meth Obj "macro" macro:tag:  "first element; rest are Ht, Str, or (key, value) params" TYPE Ht NAME macroTag PARAMS {Frame _} { Blk b}
    // CLS Obj GROUP "macro" METH macro:tag: REMARK "first element; rest are Ht, Str, or (key, value) params"

    new JavaMeth(terp.wrap.clsObj, "macro:tag:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Frame a = (Frame) args[0];
        Blk b = (Blk) args[1];
        return self.macroTag (a, b);
      }
    };

    // CMD meth Obj "eval" eval: "Evaluate a string as code in this receiver." TYPE Ur NAME eval PARAMS {String code}
    // CLS Obj GROUP "eval" METH eval: REMARK "Evaluate a string as code in this receiver."

    new JavaMeth(terp.wrap.clsObj, "eval:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Str a = (Str) args[0];
        return self.eval (a.str);
      }
    };

    // CMD meth Obj "eval" eval:arg:  "Evaluate a string as code in this receiver." TYPE Ur NAME eval PARAMS {String code} { Ur a}
    // CLS Obj GROUP "eval" METH eval:arg: REMARK "Evaluate a string as code in this receiver."

    new JavaMeth(terp.wrap.clsObj, "eval:arg:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Str a = (Str) args[0];
        Ur b = (Ur) args[1];
        return self.eval (a.str, b);
      }
    };

    // CMD meth Obj "eval" eval:arg:arg:  "Evaluate a string as code in this receiver." TYPE Ur NAME eval PARAMS {String code} { Ur a} { Ur b}
    // CLS Obj GROUP "eval" METH eval:arg:arg: REMARK "Evaluate a string as code in this receiver."

    new JavaMeth(terp.wrap.clsObj, "eval:arg:arg:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Str a = (Str) args[0];
        Ur b = (Ur) args[1];
        Ur c = (Ur) args[2];
        return self.eval (a.str, b, c);
      }
    };

    // CMD meth Obj "basic" must TYPE Obj NAME _must PARAMS 
    // CLS Obj GROUP "basic" METH must REMARK ""

    new JavaMeth(terp.wrap.clsObj, "must", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        return self._must ();
      }
    };

    // CMD meth Obj "basic" must: TYPE Obj NAME must_ PARAMS {Obj a}
    // CLS Obj GROUP "basic" METH must: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "must:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Obj a = (Obj) args[0];
        return self.must_ (a);
      }
    };

    // CMD meth Obj "basic" cant TYPE Obj NAME _cant PARAMS 
    // CLS Obj GROUP "basic" METH cant REMARK ""

    new JavaMeth(terp.wrap.clsObj, "cant", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        return self._cant ();
      }
    };

    // CMD meth Obj "basic" cant: TYPE Obj NAME cant_ PARAMS {Obj a}
    // CLS Obj GROUP "basic" METH cant: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "cant:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Obj a = (Obj) args[0];
        return self.cant_ (a);
      }
    };

    // CMD meth Obj "basic" err TYPE Ur NAME _err PARAMS 
    // CLS Obj GROUP "basic" METH err REMARK ""

    new JavaMeth(terp.wrap.clsObj, "err", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        return self._err ();
      }
    };

    // CMD meth Obj "basic" err: TYPE Ur NAME err_ PARAMS {Obj a}
    // CLS Obj GROUP "basic" METH err: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "err:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Obj a = (Obj) args[0];
        return self.err_ (a);
      }
    };

    // CMD meth Obj "basic" say TYPE void NAME _say PARAMS 
    // CLS Obj GROUP "basic" METH say REMARK ""

    new JavaMeth(terp.wrap.clsObj, "say", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        self._say ();
        return self;
      }
    };

    // CMD meth Obj "basic" say: TYPE void NAME say_ PARAMS {Obj a}
    // CLS Obj GROUP "basic" METH say: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "say:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Obj a = (Obj) args[0];
        self.say_ (a);
        return self;
      }
    };

    // CMD meth Obj "basic" sysHash TYPE Num NAME _syshash PARAMS 
    // CLS Obj GROUP "basic" METH sysHash REMARK ""

    new JavaMeth(terp.wrap.clsObj, "sysHash", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        return self._syshash ();
      }
    };

    // CMD meth Obj "basic" not TYPE Num NAME _not PARAMS 
    // CLS Obj GROUP "basic" METH not REMARK ""

    new JavaMeth(terp.wrap.clsObj, "not", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        return self._not ();
      }
    };

    // CMD meth Obj "control" ifNil:,ifn: TYPE Ur NAME ifNil_ PARAMS {Blk a}
    // CLS Obj GROUP "control" METH ifNil:,ifn: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "ifNil:", "ifn:") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Blk a = (Blk) args[0];
        return self.ifNil_ (a);
      }
    };

    // CMD meth Obj "control" ifNotNil:,ifnn: TYPE Ur NAME ifNotNil_ PARAMS {Blk a}
    // CLS Obj GROUP "control" METH ifNotNil:,ifnn: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "ifNotNil:", "ifnn:") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Blk a = (Blk) args[0];
        return self.ifNotNil_ (a);
      }
    };

    // CMD meth Obj "control" ifNil:ifNotNil:,ifn:ifnn: TYPE Ur NAME ifNil_ifNotNil_ PARAMS {Blk a} { Blk b}
    // CLS Obj GROUP "control" METH ifNil:ifNotNil:,ifn:ifnn: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "ifNil:ifNotNil:", "ifn:ifnn:") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Blk a = (Blk) args[0];
        Blk b = (Blk) args[1];
        return self.ifNil_ifNotNil_ (a, b);
      }
    };

    // CMD meth Obj "control" ifNotNil:ifNil:,ifnn:ifn: TYPE Ur NAME ifNotNil_ifNil_ PARAMS {Blk a} { Blk b}
    // CLS Obj GROUP "control" METH ifNotNil:ifNil:,ifnn:ifn: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "ifNotNil:ifNil:", "ifnn:ifn:") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Blk a = (Blk) args[0];
        Blk b = (Blk) args[1];
        return self.ifNotNil_ifNil_ (a, b);
      }
    };

    // CMD meth Obj "control" ifTrue:,y: TYPE Ur NAME ifTrue_ PARAMS {Blk a}
    // CLS Obj GROUP "control" METH ifTrue:,y: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "ifTrue:", "y:") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Blk a = (Blk) args[0];
        return self.ifTrue_ (a);
      }
    };

    // CMD meth Obj "control" ifFalse_:,n: TYPE Ur NAME ifFalse_ PARAMS {Blk a}
    // CLS Obj GROUP "control" METH ifFalse_:,n: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "ifFalse_:", "n:") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Blk a = (Blk) args[0];
        return self.ifFalse_ (a);
      }
    };

    // CMD meth Obj "control" ifTrue:ifFalse:,y:n: TYPE Ur NAME ifTrue_ifFalse_ PARAMS {Blk a} { Blk b}
    // CLS Obj GROUP "control" METH ifTrue:ifFalse:,y:n: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "ifTrue:ifFalse:", "y:n:") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Blk a = (Blk) args[0];
        Blk b = (Blk) args[1];
        return self.ifTrue_ifFalse_ (a, b);
      }
    };

    // CMD meth Obj "control" ifFalse:ifTrue:,n:y: TYPE Ur NAME ifFalse_ifTrue_ PARAMS {Blk a} { Blk b}
    // CLS Obj GROUP "control" METH ifFalse:ifTrue:,n:y: REMARK ""

    new JavaMeth(terp.wrap.clsObj, "ifFalse:ifTrue:", "n:y:") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Blk a = (Blk) args[0];
        Blk b = (Blk) args[1];
        return self.ifFalse_ifTrue_ (a, b);
      }
    };

    // CMD meth Obj "basic" isa:  "is self an instance of said class or a subclass of it" TYPE boolean NAME isa_ PARAMS {Cls query}
    // CLS Obj GROUP "basic" METH isa: REMARK "is self an instance of said class or a subclass of it"

    new JavaMeth(terp.wrap.clsObj, "isa:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Cls a = (Cls) args[0];
        return terp.boolObj(self.isa_ (a));
      }
    };

    // CMD meth Obj "basic" is:  "does the argument have the same identity as self" TYPE boolean NAME is_ PARAMS {Ur x}
    // CLS Obj GROUP "basic" METH is: REMARK "does the argument have the same identity as self"

    new JavaMeth(terp.wrap.clsObj, "is:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Obj self = (Obj) me;
        Ur a = (Ur) args[0];
        return terp.boolObj(self.is_ (a));
      }
    };

    // CMD meth FileCls "io" dir  "Return list of tuples: names, mtime, length." TYPE Vec NAME _dir PARAMS {Terp terp}
    // CLS FileCls GROUP "io" METH dir REMARK "Return list of tuples: names, mtime, length."

    new JavaMeth(terp.wrap.clsFileCls, "dir", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsFileCls;
        return File._dir (terp);
      }
    };

    // CMD meth FileCls "io" read: "Read a text file as one big Str." TYPE Ur NAME read_ PARAMS {Terp terp} { String filename}
    // CLS FileCls GROUP "io" METH read: REMARK "Read a text file as one big Str."

    new JavaMeth(terp.wrap.clsFileCls, "read:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsFileCls;
        Str a = (Str) args[0];
        return File.read_ (terp,a.str);
      }
    };

    // CMD meth FileCls "io" write:value: "Write a text file as one big Str." TYPE void NAME write_value_ PARAMS {Terp terp} { String filename} { 				String content}
    // CLS FileCls GROUP "io" METH write:value: REMARK "Write a text file as one big Str."

    new JavaMeth(terp.wrap.clsFileCls, "write:value:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsFileCls;
        Str a = (Str) args[0];
        Str b = (Str) args[1];
        File.write_value_ (terp,a.str, b.str);
        return self;
      }
    };

    // CMD meth FileCls "io" append:value:  "Append a text file as one big Str." TYPE void NAME append_value_ PARAMS {Terp terp} { String filename} { 				String content}
    // CLS FileCls GROUP "io" METH append:value: REMARK "Append a text file as one big Str."

    new JavaMeth(terp.wrap.clsFileCls, "append:value:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsFileCls;
        Str a = (Str) args[0];
        Str b = (Str) args[1];
        File.append_value_ (terp,a.str, b.str);
        return self;
      }
    };

    // CMD meth FileCls "io" delete: TYPE void NAME delete_ PARAMS {Terp terp} { String filename}
    // CLS FileCls GROUP "io" METH delete: REMARK ""

    new JavaMeth(terp.wrap.clsFileCls, "delete:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsFileCls;
        Str a = (Str) args[0];
        File.delete_ (terp,a.str);
        return self;
      }
    };

    // CMD meth HubCls "io" dir "List files on web with mtime and size." TYPE Vec NAME _dir PARAMS {Terp terp}
    // CLS HubCls GROUP "io" METH dir REMARK "List files on web with mtime and size."

    new JavaMeth(terp.wrap.clsHubCls, "dir", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsHubCls;
        return Hub._dir (terp);
      }
    };

    // CMD meth HubCls "io" read: "Pull a file from the Web." TYPE String NAME read_ PARAMS {Terp terp} { String filename}
    // CLS HubCls GROUP "io" METH read: REMARK "Pull a file from the Web."

    new JavaMeth(terp.wrap.clsHubCls, "read:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsHubCls;
        Str a = (Str) args[0];
        String z = Hub.read_ (terp,a.str);
        return (z == null) ? terp.instNil : terp.newStr(z);
      }
    };

    // CMD meth HubCls "io" write:value: "Push a file to the Web." TYPE void NAME write_value_ PARAMS {Terp terp} { String filename} { 				String content}
    // CLS HubCls GROUP "io" METH write:value: REMARK "Push a file to the Web."

    new JavaMeth(terp.wrap.clsHubCls, "write:value:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsHubCls;
        Str a = (Str) args[0];
        Str b = (Str) args[1];
        Hub.write_value_ (terp,a.str, b.str);
        return self;
      }
    };

    // CMD meth SysCls "debug" said TYPE Vec NAME _said PARAMS {Terp terp}
    // CLS SysCls GROUP "debug" METH said REMARK ""

    new JavaMeth(terp.wrap.clsSysCls, "said", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsSysCls;
        return Sys._said (terp);
      }
    };

    // CMD meth SysCls "usr" find: TYPE Obj NAME find_ PARAMS {Terp terp} { String oname}
    // CLS SysCls GROUP "usr" METH find: REMARK ""

    new JavaMeth(terp.wrap.clsSysCls, "find:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsSysCls;
        Str a = (Str) args[0];
        return Sys.find_ (terp,a.str);
      }
    };

    // CMD meth SysCls "sys" sleep: TYPE void NAME sleep_ PARAMS {Terp terp} { float secs}
    // CLS SysCls GROUP "sys" METH sleep: REMARK ""

    new JavaMeth(terp.wrap.clsSysCls, "sleep:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsSysCls;
        Num a = (Num) args[0];
        Sys.sleep_ (terp,(float)a.num);
        return self;
      }
    };

    // CMD meth SysCls "sys" secs TYPE Num NAME secs PARAMS {Terp terp}
    // CLS SysCls GROUP "sys" METH secs REMARK ""

    new JavaMeth(terp.wrap.clsSysCls, "secs", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsSysCls;
        return Sys.secs (terp);
      }
    };

    // CMD meth SysCls "sys" nanos TYPE Num NAME nanos PARAMS {Terp terp}
    // CLS SysCls GROUP "sys" METH nanos REMARK ""

    new JavaMeth(terp.wrap.clsSysCls, "nanos", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsSysCls;
        return Sys.nanos (terp);
      }
    };

    // CMD meth SysCls "sys" worldName "which world is loaded" TYPE String NAME _worldName PARAMS {Terp terp}
    // CLS SysCls GROUP "sys" METH worldName REMARK "which world is loaded"

    new JavaMeth(terp.wrap.clsSysCls, "worldName", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsSysCls;
        String z = Sys._worldName (terp);
        return (z == null) ? terp.instNil : terp.newStr(z);
      }
    };

    // CMD meth SysCls "sys" worldFileName "which world is loaded" TYPE String NAME _worldFileName PARAMS {Terp terp}
    // CLS SysCls GROUP "sys" METH worldFileName REMARK "which world is loaded"

    new JavaMeth(terp.wrap.clsSysCls, "worldFileName", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsSysCls;
        String z = Sys._worldFileName (terp);
        return (z == null) ? terp.instNil : terp.newStr(z);
      }
    };

    // CMD meth SysCls "sys" fail "Create a Java Exception" TYPE void NAME _fail PARAMS {Terp terp}
    // CLS SysCls GROUP "sys" METH fail REMARK "Create a Java Exception"

    new JavaMeth(terp.wrap.clsSysCls, "fail", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsSysCls;
        Sys._fail (terp);
        return self;
      }
    };

    // CMD meth SysCls "sys" fail: "Create a Java Exception" TYPE void NAME _fail PARAMS {Terp terp} { Ur msg}
    // CLS SysCls GROUP "sys" METH fail: REMARK "Create a Java Exception"

    new JavaMeth(terp.wrap.clsSysCls, "fail:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsSysCls;
        Ur a = (Ur) args[0];
        Sys._fail (terp,a);
        return self;
      }
    };

    // CMD meth SysCls "sys" trigraphs "keyboard character substitution dict" TYPE Ur NAME trigraphs PARAMS {Terp terp}
    // CLS SysCls GROUP "sys" METH trigraphs REMARK "keyboard character substitution dict"

    new JavaMeth(terp.wrap.clsSysCls, "trigraphs", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsSysCls;
        return Sys.trigraphs (terp);
      }
    };

    // CMD meth Blk "param" storeAtParam0: TYPE void NAME storeAtParam0 PARAMS {Ur x}
    // CLS Blk GROUP "param" METH storeAtParam0: REMARK ""

    new JavaMeth(terp.wrap.clsBlk, "storeAtParam0:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Blk self = (Blk) me;
        Ur a = (Ur) args[0];
        self.storeAtParam0 (a);
        return self;
      }
    };

    // CMD meth Blk "param" storeAtParam1: TYPE void NAME storeAtParam1 PARAMS {Ur x}
    // CLS Blk GROUP "param" METH storeAtParam1: REMARK ""

    new JavaMeth(terp.wrap.clsBlk, "storeAtParam1:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Blk self = (Blk) me;
        Ur a = (Ur) args[0];
        self.storeAtParam1 (a);
        return self;
      }
    };

    // CMD meth Blk "param" storeAtParam:value: TYPE void NAME storeAtParamKV PARAMS {Ur k} { Ur v}
    // CLS Blk GROUP "param" METH storeAtParam:value: REMARK ""

    new JavaMeth(terp.wrap.clsBlk, "storeAtParam:value:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Blk self = (Blk) me;
        Ur a = (Ur) args[0];
        Ur b = (Ur) args[1];
        self.storeAtParamKV (a, b);
        return self;
      }
    };

    // CMD meth Blk "eval" value TYPE Ur NAME evalWithoutArgs PARAMS 
    // CLS Blk GROUP "eval" METH value REMARK ""

    new JavaMeth(terp.wrap.clsBlk, "value", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Blk self = (Blk) me;
        return self.evalWithoutArgs ();
      }
    };

    // CMD meth Blk "eval" value: TYPE Ur NAME evalWith1Arg PARAMS {Ur arg0}
    // CLS Blk GROUP "eval" METH value: REMARK ""

    new JavaMeth(terp.wrap.clsBlk, "value:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Blk self = (Blk) me;
        Ur a = (Ur) args[0];
        return self.evalWith1Arg (a);
      }
    };

    // CMD meth Blk "eval" value:value: TYPE Ur NAME evalWith2Args PARAMS {Ur arg0} { Ur arg1}
    // CLS Blk GROUP "eval" METH value:value: REMARK ""

    new JavaMeth(terp.wrap.clsBlk, "value:value:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Blk self = (Blk) me;
        Ur a = (Ur) args[0];
        Ur b = (Ur) args[1];
        return self.evalWith2Args (a, b);
      }
    };

    // CMD meth Num "access" fmt:  "format with Java floating point format string" TYPE String NAME fmt_ PARAMS {String s}
    // CLS Num GROUP "access" METH fmt: REMARK "format with Java floating point format string"

    new JavaMeth(terp.wrap.clsNum, "fmt:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        Str a = (Str) args[0];
        String z = self.fmt_ (a.str);
        return (z == null) ? terp.instNil : terp.newStr(z);
      }
    };

    // CMD meth Num "access" chr  "covert integer to single-char Str with that unicode codepoint" TYPE String NAME _chr PARAMS 
    // CLS Num GROUP "access" METH chr REMARK "covert integer to single-char Str with that unicode codepoint"

    new JavaMeth(terp.wrap.clsNum, "chr", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        String z = self._chr ();
        return (z == null) ? terp.instNil : terp.newStr(z);
      }
    };

    // CMD meth Num "binop" ==,eq: "eq two Nums" TYPE Num NAME _eq_ PARAMS {Num a}
    // CLS Num GROUP "binop" METH ==,eq: REMARK "eq two Nums"

    new JavaMeth(terp.wrap.clsNum, "==", "eq:") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        Num a = (Num) args[0];
        return self._eq_ (a);
      }
    };

    // CMD meth Num "binop" !=,ne: "ne two Nums" TYPE Num NAME _ne_ PARAMS {Num a}
    // CLS Num GROUP "binop" METH !=,ne: REMARK "ne two Nums"

    new JavaMeth(terp.wrap.clsNum, "!=", "ne:") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        Num a = (Num) args[0];
        return self._ne_ (a);
      }
    };

    // CMD meth Num "binop" <,lt: "lt two Nums" TYPE Num NAME _lt_ PARAMS {Num a}
    // CLS Num GROUP "binop" METH <,lt: REMARK "lt two Nums"

    new JavaMeth(terp.wrap.clsNum, "<", "lt:") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        Num a = (Num) args[0];
        return self._lt_ (a);
      }
    };

    // CMD meth Num "binop" <=,le: "le two Nums" TYPE Num NAME _le_ PARAMS {Num a}
    // CLS Num GROUP "binop" METH <=,le: REMARK "le two Nums"

    new JavaMeth(terp.wrap.clsNum, "<=", "le:") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        Num a = (Num) args[0];
        return self._le_ (a);
      }
    };

    // CMD meth Num "binop" >,gt: "gt two Nums" TYPE Num NAME _gt_ PARAMS {Num a}
    // CLS Num GROUP "binop" METH >,gt: REMARK "gt two Nums"

    new JavaMeth(terp.wrap.clsNum, ">", "gt:") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        Num a = (Num) args[0];
        return self._gt_ (a);
      }
    };

    // CMD meth Num "binop" >=,ge: "ge two Nums" TYPE Num NAME _ge_ PARAMS {Num a}
    // CLS Num GROUP "binop" METH >=,ge: REMARK "ge two Nums"

    new JavaMeth(terp.wrap.clsNum, ">=", "ge:") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        Num a = (Num) args[0];
        return self._ge_ (a);
      }
    };

    // CMD meth Num "binop" + "add two Nums" TYPE Num NAME _pl_ PARAMS {Num a}
    // CLS Num GROUP "binop" METH + REMARK "add two Nums"

    new JavaMeth(terp.wrap.clsNum, "+", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        Num a = (Num) args[0];
        return self._pl_ (a);
      }
    };

    // CMD meth Num "binop" - "add two Nums" TYPE Num NAME _mi_ PARAMS {Num a}
    // CLS Num GROUP "binop" METH - REMARK "add two Nums"

    new JavaMeth(terp.wrap.clsNum, "-", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        Num a = (Num) args[0];
        return self._mi_ (a);
      }
    };

    // CMD meth Num "binop" | "bitwise-or two Nums as 32bit integers" TYPE Num NAME _or_ PARAMS {Num a}
    // CLS Num GROUP "binop" METH | REMARK "bitwise-or two Nums as 32bit integers"

    new JavaMeth(terp.wrap.clsNum, "|", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        Num a = (Num) args[0];
        return self._or_ (a);
      }
    };

    // CMD meth Num "binop" ^ "bitwise-xor two Nums as 32bit integers" TYPE Num NAME _xo_ PARAMS {Num a}
    // CLS Num GROUP "binop" METH ^ REMARK "bitwise-xor two Nums as 32bit integers"

    new JavaMeth(terp.wrap.clsNum, "^", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        Num a = (Num) args[0];
        return self._xo_ (a);
      }
    };

    // CMD meth Num "binop" * "multiply two Nums" TYPE Num NAME _ti_ PARAMS {Num a}
    // CLS Num GROUP "binop" METH * REMARK "multiply two Nums"

    new JavaMeth(terp.wrap.clsNum, "*", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        Num a = (Num) args[0];
        return self._ti_ (a);
      }
    };

    // CMD meth Num "binop" / "divide two Nums" TYPE Num NAME _di_ PARAMS {Num a}
    // CLS Num GROUP "binop" METH / REMARK "divide two Nums"

    new JavaMeth(terp.wrap.clsNum, "/", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        Num a = (Num) args[0];
        return self._di_ (a);
      }
    };

    // CMD meth Num "binop" % "modulo two Nums" TYPE Num NAME _mo_ PARAMS {Num a}
    // CLS Num GROUP "binop" METH % REMARK "modulo two Nums"

    new JavaMeth(terp.wrap.clsNum, "%", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        Num a = (Num) args[0];
        return self._mo_ (a);
      }
    };

    // CMD meth Num "binop" & "bitwise-and two Nums as 32bit integers" TYPE Num NAME _an_ PARAMS {Num a}
    // CLS Num GROUP "binop" METH & REMARK "bitwise-and two Nums as 32bit integers"

    new JavaMeth(terp.wrap.clsNum, "&", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        Num a = (Num) args[0];
        return self._an_ (a);
      }
    };

    // CMD meth NumCls "num" rand "Random float between 0 and 1." TYPE Num NAME rand PARAMS {Terp terp}
    // CLS NumCls GROUP "num" METH rand REMARK "Random float between 0 and 1."

    new JavaMeth(terp.wrap.clsNumCls, "rand", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsNumCls;
        return Num.rand (terp);
      }
    };

    // CMD meth NumCls "num" rand: "Random integer between 0 and n-1." TYPE Num NAME rand_ PARAMS {Terp terp} { int n}
    // CLS NumCls GROUP "num" METH rand: REMARK "Random integer between 0 and n-1."

    new JavaMeth(terp.wrap.clsNumCls, "rand:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsNumCls;
        Num a = (Num) args[0];
        return Num.rand_ (terp,a.toNearestInt());
      }
    };

    // CMD meth Num "num" range "vec of ints from 0 to self - 1" TYPE Vec NAME _range PARAMS 
    // CLS Num GROUP "num" METH range REMARK "vec of ints from 0 to self - 1"

    new JavaMeth(terp.wrap.clsNum, "range", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        return self._range ();
      }
    };

    // CMD meth Num "num" do:  "do the block self times, passing 1 arg, from 0 to self-1" TYPE void NAME do_ PARAMS {Blk blk}
    // CLS Num GROUP "num" METH do: REMARK "do the block self times, passing 1 arg, from 0 to self-1"

    new JavaMeth(terp.wrap.clsNum, "do:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        Blk a = (Blk) args[0];
        self.do_ (a);
        return self;
      }
    };

    // CMD meth Num "convert" num TYPE Num NAME _num PARAMS 
    // CLS Num GROUP "convert" METH num REMARK ""

    new JavaMeth(terp.wrap.clsNum, "num", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        return self._num ();
      }
    };

    // CMD meth Num "convert" neg TYPE Num NAME _neg PARAMS 
    // CLS Num GROUP "convert" METH neg REMARK ""

    new JavaMeth(terp.wrap.clsNum, "neg", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        return self._neg ();
      }
    };

    // CMD meth Num "math" sgn TYPE Num NAME _sgn PARAMS 
    // CLS Num GROUP "math" METH sgn REMARK ""

    new JavaMeth(terp.wrap.clsNum, "sgn", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        return self._sgn ();
      }
    };

    // CMD meth Num "convert" int TYPE Num NAME _int PARAMS 
    // CLS Num GROUP "convert" METH int REMARK ""

    new JavaMeth(terp.wrap.clsNum, "int", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        return self._int ();
      }
    };

    // CMD meth Num "convert" floor TYPE Num NAME _floor PARAMS 
    // CLS Num GROUP "convert" METH floor REMARK ""

    new JavaMeth(terp.wrap.clsNum, "floor", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        return self._floor ();
      }
    };

    // CMD meth Num "convert" round TYPE Num NAME _round PARAMS 
    // CLS Num GROUP "convert" METH round REMARK ""

    new JavaMeth(terp.wrap.clsNum, "round", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        return self._round ();
      }
    };

    // CMD meth Num "math" abs TYPE Num NAME _abs PARAMS 
    // CLS Num GROUP "math" METH abs REMARK ""

    new JavaMeth(terp.wrap.clsNum, "abs", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        return self._abs ();
      }
    };

    // CMD meth Num "math" sin TYPE Num NAME _sin PARAMS 
    // CLS Num GROUP "math" METH sin REMARK ""

    new JavaMeth(terp.wrap.clsNum, "sin", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        return self._sin ();
      }
    };

    // CMD meth Num "math" cos TYPE Num NAME _cos PARAMS 
    // CLS Num GROUP "math" METH cos REMARK ""

    new JavaMeth(terp.wrap.clsNum, "cos", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        return self._cos ();
      }
    };

    // CMD meth Num "math" tan TYPE Num NAME _tan PARAMS 
    // CLS Num GROUP "math" METH tan REMARK ""

    new JavaMeth(terp.wrap.clsNum, "tan", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        return self._tan ();
      }
    };

    // CMD meth Num "math" asin TYPE Num NAME _asin PARAMS 
    // CLS Num GROUP "math" METH asin REMARK ""

    new JavaMeth(terp.wrap.clsNum, "asin", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        return self._asin ();
      }
    };

    // CMD meth Num "math" acos TYPE Num NAME _acos PARAMS 
    // CLS Num GROUP "math" METH acos REMARK ""

    new JavaMeth(terp.wrap.clsNum, "acos", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        return self._acos ();
      }
    };

    // CMD meth Num "math" atan TYPE Num NAME _atan PARAMS 
    // CLS Num GROUP "math" METH atan REMARK ""

    new JavaMeth(terp.wrap.clsNum, "atan", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        return self._atan ();
      }
    };

    // CMD meth Num "math" sinh TYPE Num NAME _sinh PARAMS 
    // CLS Num GROUP "math" METH sinh REMARK ""

    new JavaMeth(terp.wrap.clsNum, "sinh", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        return self._sinh ();
      }
    };

    // CMD meth Num "math" cosh TYPE Num NAME _cosh PARAMS 
    // CLS Num GROUP "math" METH cosh REMARK ""

    new JavaMeth(terp.wrap.clsNum, "cosh", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        return self._cosh ();
      }
    };

    // CMD meth Num "math" tanh TYPE Num NAME _tanh PARAMS 
    // CLS Num GROUP "math" METH tanh REMARK ""

    new JavaMeth(terp.wrap.clsNum, "tanh", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        return self._tanh ();
      }
    };

    // CMD meth Num "math" ln TYPE Num NAME _ln PARAMS 
    // CLS Num GROUP "math" METH ln REMARK ""

    new JavaMeth(terp.wrap.clsNum, "ln", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        return self._ln ();
      }
    };

    // CMD meth Num "math" log10 TYPE Num NAME _log10 PARAMS 
    // CLS Num GROUP "math" METH log10 REMARK ""

    new JavaMeth(terp.wrap.clsNum, "log10", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        return self._log10 ();
      }
    };

    // CMD meth Num "math" exp TYPE Num NAME _exp PARAMS 
    // CLS Num GROUP "math" METH exp REMARK ""

    new JavaMeth(terp.wrap.clsNum, "exp", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        return self._exp ();
      }
    };

    // CMD meth NumCls "math" pi TYPE Num NAME _pi PARAMS {Terp terp}
    // CLS NumCls GROUP "math" METH pi REMARK ""

    new JavaMeth(terp.wrap.clsNumCls, "pi", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsNumCls;
        return Num._pi (terp);
      }
    };

    // CMD meth NumCls "math" tau TYPE Num NAME _tau PARAMS {Terp terp}
    // CLS NumCls GROUP "math" METH tau REMARK ""

    new JavaMeth(terp.wrap.clsNumCls, "tau", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsNumCls;
        return Num._tau (terp);
      }
    };

    // CMD meth NumCls "math" e TYPE Num NAME _e PARAMS {Terp terp}
    // CLS NumCls GROUP "math" METH e REMARK ""

    new JavaMeth(terp.wrap.clsNumCls, "e", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsNumCls;
        return Num._e (terp);
      }
    };

    // CMD meth Num "math" idiv: TYPE Num NAME idiv_ PARAMS {Num a}
    // CLS Num GROUP "math" METH idiv: REMARK ""

    new JavaMeth(terp.wrap.clsNum, "idiv:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        Num a = (Num) args[0];
        return self.idiv_ (a);
      }
    };

    // CMD meth Num "math" imod: TYPE Num NAME imod_ PARAMS {Num a}
    // CLS Num GROUP "math" METH imod: REMARK ""

    new JavaMeth(terp.wrap.clsNum, "imod:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        Num a = (Num) args[0];
        return self.imod_ (a);
      }
    };

    // CMD meth Num "math" pow: TYPE Num NAME pow_ PARAMS {Num a}
    // CLS Num GROUP "math" METH pow: REMARK ""

    new JavaMeth(terp.wrap.clsNum, "pow:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Num self = (Num) me;
        Num a = (Num) args[0];
        return self.pow_ (a);
      }
    };

    // CMD meth Buf "access" append:,ap: TYPE Buf NAME append_ PARAMS {Obj a}
    // CLS Buf GROUP "access" METH append:,ap: REMARK ""

    new JavaMeth(terp.wrap.clsBuf, "append:", "ap:") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Buf self = (Buf) me;
        Obj a = (Obj) args[0];
        return self.append_ (a);
      }
    };

    // CMD meth BufCls "new" new TYPE Buf NAME cls_new PARAMS {Terp t}
    // CLS BufCls GROUP "new" METH new REMARK ""

    new JavaMeth(terp.wrap.clsBufCls, "new", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsBufCls;
        return Buf.cls_new (terp);
      }
    };

    // CMD meth BufCls "new" append:,ap: TYPE Buf NAME cls_append_ PARAMS {Terp t} { Obj a}
    // CLS BufCls GROUP "new" METH append:,ap: REMARK ""

    new JavaMeth(terp.wrap.clsBufCls, "append:", "ap:") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsBufCls;
        Obj a = (Obj) args[0];
        return Buf.cls_append_ (terp,a);
      }
    };

    // CMD meth Str "access" applySubstitutions "" TYPE Str NAME applySubstitutions PARAMS 
    // CLS Str GROUP "access" METH applySubstitutions REMARK ""

    new JavaMeth(terp.wrap.clsStr, "applySubstitutions", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Str self = (Str) me;
        return self.applySubstitutions ();
      }
    };

    // CMD meth Str "access" ord  "unicode codepoint number of first char in Str" TYPE int NAME ord PARAMS 
    // CLS Str GROUP "access" METH ord REMARK "unicode codepoint number of first char in Str"

    new JavaMeth(terp.wrap.clsStr, "ord", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Str self = (Str) me;
        return terp.newNum(self.ord ());
      }
    };

    // CMD meth RexCls "new" new: TYPE Rex NAME new_ PARAMS {Terp t} { String pat}
    // CLS RexCls GROUP "new" METH new: REMARK ""

    new JavaMeth(terp.wrap.clsRexCls, "new:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsRexCls;
        Str a = (Str) args[0];
        return Rex.new_ (terp,a.str);
      }
    };

    // CMD meth Rex "rex" match: TYPE Ur NAME match PARAMS {String s}
    // CLS Rex GROUP "rex" METH match: REMARK ""

    new JavaMeth(terp.wrap.clsRex, "match:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Rex self = (Rex) me;
        Str a = (Str) args[0];
        return self.match (a.str);
      }
    };

    // CMD meth Vec "access" len "return length of the Vec" TYPE Num NAME _len PARAMS 
    // CLS Vec GROUP "access" METH len REMARK "return length of the Vec"

    new JavaMeth(terp.wrap.clsVec, "len", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Vec self = (Vec) me;
        return self._len ();
      }
    };

    // CMD meth Vec "access" at: "get element at index a, modulo length of Vec" TYPE Ur NAME at_ PARAMS {Num a}
    // CLS Vec GROUP "access" METH at: REMARK "get element at index a, modulo length of Vec"

    new JavaMeth(terp.wrap.clsVec, "at:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Vec self = (Vec) me;
        Num a = (Num) args[0];
        return self.at_ (a);
      }
    };

    // CMD meth Vec "access" at:put:,at:p:  "put element b at given a, modulo length of Vec" TYPE void NAME at_put_ PARAMS {Num a} { Ur b}
    // CLS Vec GROUP "access" METH at:put:,at:p: REMARK "put element b at given a, modulo length of Vec"

    new JavaMeth(terp.wrap.clsVec, "at:put:", "at:p:") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Vec self = (Vec) me;
        Num a = (Num) args[0];
        Ur b = (Ur) args[1];
        self.at_put_ (a, b);
        return self;
      }
    };

    // CMD meth Vec "access" append:,ap: "add new element ato end of Vec" TYPE void NAME append_ PARAMS {Ur a}
    // CLS Vec GROUP "access" METH append:,ap: REMARK "add new element ato end of Vec"

    new JavaMeth(terp.wrap.clsVec, "append:", "ap:") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Vec self = (Vec) me;
        Ur a = (Ur) args[0];
        self.append_ (a);
        return self;
      }
    };

    // CMD meth Vec "string" join:  "Join strings with given string." TYPE Str NAME join PARAMS {String a}
    // CLS Vec GROUP "string" METH join: REMARK "Join strings with given string."

    new JavaMeth(terp.wrap.clsVec, "join:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Vec self = (Vec) me;
        Str a = (Str) args[0];
        return self.join (a.str);
      }
    };

    // CMD meth Vec "string" join  "Join strings with spaces." TYPE Str NAME join PARAMS 
    // CLS Vec GROUP "string" METH join REMARK "Join strings with spaces."

    new JavaMeth(terp.wrap.clsVec, "join", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Vec self = (Vec) me;
        return self.join ();
      }
    };

    // CMD meth Vec "string" jam  "Join strings with no separator char." TYPE Str NAME jam PARAMS 
    // CLS Vec GROUP "string" METH jam REMARK "Join strings with no separator char."

    new JavaMeth(terp.wrap.clsVec, "jam", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Vec self = (Vec) me;
        return self.jam ();
      }
    };

    // CMD meth Vec "string" implode,imp  "Implode strings and ints (as chars) and subvectors." TYPE Str NAME implode PARAMS 
    // CLS Vec GROUP "string" METH implode,imp REMARK "Implode strings and ints (as chars) and subvectors."

    new JavaMeth(terp.wrap.clsVec, "implode", "imp") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Vec self = (Vec) me;
        return self.implode ();
      }
    };

    // CMD meth Vec "control" doWithEach:,do:  "Iterate the block with one argument, for each item in self." TYPE Undefined NAME doWithEach_ PARAMS {Blk b}
    // CLS Vec GROUP "control" METH doWithEach:,do: REMARK "Iterate the block with one argument, for each item in self."

    new JavaMeth(terp.wrap.clsVec, "doWithEach:", "do:") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Vec self = (Vec) me;
        Blk a = (Blk) args[0];
        return self.doWithEach_ (a);
      }
    };

    // CMD meth VecCls "access" new "create a new, empty Vec" TYPE Vec NAME cls_new PARAMS {Terp terp}
    // CLS VecCls GROUP "access" METH new REMARK "create a new, empty Vec"

    new JavaMeth(terp.wrap.clsVecCls, "new", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsVecCls;
        return Vec.cls_new (terp);
      }
    };

    // CMD meth VecCls "access" append:,ap: "add element to a new Vec" TYPE Vec NAME cls_append_ PARAMS {Terp terp} { Ur a}
    // CLS VecCls GROUP "access" METH append:,ap: REMARK "add element to a new Vec"

    new JavaMeth(terp.wrap.clsVecCls, "append:", "ap:") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsVecCls;
        Ur a = (Ur) args[0];
        return Vec.cls_append_ (terp,a);
      }
    };

    // CMD meth Ht "html" append:,ap: "take an Ht or a Str" TYPE void NAME append PARAMS {Ur x}
    // CLS Ht GROUP "html" METH append:,ap: REMARK "take an Ht or a Str"

    new JavaMeth(terp.wrap.clsHt, "append:", "ap:") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Ht self = (Ht) me;
        Ur a = (Ur) args[0];
        self.append (a);
        return self;
      }
    };

    // CMD meth HtCls "html" new: "take an Ht or a Str" TYPE Ht NAME new_ PARAMS {Terp t} { Ur a}
    // CLS HtCls GROUP "html" METH new: REMARK "take an Ht or a Str"

    new JavaMeth(terp.wrap.clsHtCls, "new:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsHtCls;
        Ur a = (Ur) args[0];
        return Ht.new_ (terp,a);
      }
    };

    // CMD meth HtCls "html" entity: TYPE Ht NAME entity PARAMS {Terp t} { String name}
    // CLS HtCls GROUP "html" METH entity: REMARK ""

    new JavaMeth(terp.wrap.clsHtCls, "entity:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsHtCls;
        Str a = (Str) args[0];
        return Ht.entity (terp,a.str);
      }
    };

    // CMD meth HtCls "html" tag:params:body: TYPE Ht NAME tag PARAMS {Terp t} { String name} { Ur params} { Ur body}
    // CLS HtCls GROUP "html" METH tag:params:body: REMARK ""

    new JavaMeth(terp.wrap.clsHtCls, "tag:params:body:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsHtCls;
        Str a = (Str) args[0];
        Ur b = (Ur) args[1];
        Ur c = (Ur) args[2];
        return Ht.tag (terp,a.str, b, c);
      }
    };

    new JavaMeth(terp.wrap.clsFrame, "prevFrame", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        Frame self = (Frame) me;
        return self.prev;
      }
    };
    new JavaMeth(terp.wrap.clsFrame, "receiver", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        Frame self = (Frame) me;
        return self.self;
      }
    };
    new JavaMeth(terp.wrap.clsFrame, "localVars", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        Frame self = (Frame) me;
        return new Vec(terp, self.locals);
      }
    };
    new JavaMeth(terp.wrap.clsFrame, "currentMethodExpr", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        Frame self = (Frame) me;
        return self.top;
      }
    };
    new JavaMeth(terp.wrap.clsFrame, "level", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        Frame self = (Frame) me;
        return terp.newNum(self.level);
      }
    };
    new JavaMeth(terp.wrap.clsExpr, "white", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        Expr self = (Expr) me;
        return terp.newStr(self.white);
      }
    };
    new JavaMeth(terp.wrap.clsExpr, "front", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        Expr self = (Expr) me;
        return terp.newStr(self.front);
      }
    };
    new JavaMeth(terp.wrap.clsExpr, "rest", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        Expr self = (Expr) me;
        return terp.newStr(self.rest);
      }
    };
    // CMD meth Expr "eval" evalFrame: TYPE Ur NAME eval PARAMS {Frame f);  	final Expr setFront(String front}
    // CLS Expr GROUP "eval" METH evalFrame: REMARK ""

    new JavaMeth(terp.wrap.clsExpr, "evalFrame:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Expr self = (Expr) me;
        Frame a = (Frame) args[0];
        return self.eval (a);
      }
    };

    // CMD meth Expr "parser" depth TYPE int NAME depth PARAMS 
    // CLS Expr GROUP "parser" METH depth REMARK ""

    new JavaMeth(terp.wrap.clsExpr, "depth", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Expr self = (Expr) me;
        return terp.newNum(self.depth ());
      }
    };

    // CMD meth LValue "parser" storeFrame:value: TYPE void NAME store PARAMS {Frame f} { Ur x); 		// =meth LValue "parser" recallFrame: 		public abstract Ur recall(Frame f);  		public abstract void fixIndices(Parser p}
    // CLS LValue GROUP "parser" METH storeFrame:value: REMARK ""

    new JavaMeth(terp.wrap.clsLValue, "storeFrame:value:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      LValue self = (LValue) me;
        Frame a = (Frame) args[0];
        Ur b = (Ur) args[1];
        self.store (a, b);
        return self;
      }
    };

    new JavaMeth(terp.wrap.clsLvName, "name", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        LvName self = (LvName) me;
        return terp.newStr(self.name);
      }
    };
    new JavaMeth(terp.wrap.clsLvName, "index", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        LvName self = (LvName) me;
        return terp.newNum(self.index);
      }
    };
    new JavaMeth(terp.wrap.clsLvTuple, "arr", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        LvTuple self = (LvTuple) me;
        return new Vec(terp, self.arr);
      }
    };
    new JavaMeth(terp.wrap.clsMethTop, "numLocals", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        MethTop self = (MethTop) me;
        return terp.newNum(self.numLocals);
      }
    };
    new JavaMeth(terp.wrap.clsMethTop, "numArgs", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        MethTop self = (MethTop) me;
        return terp.newNum(self.numArgs);
      }
    };
    new JavaMeth(terp.wrap.clsMethTop, "onCls", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        MethTop self = (MethTop) me;
        return self.onCls;
      }
    };
    new JavaMeth(terp.wrap.clsMethTop, "methName", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        MethTop self = (MethTop) me;
        return terp.newStr(self.methName);
      }
    };
    new JavaMeth(terp.wrap.clsMethTop, "body", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        MethTop self = (MethTop) me;
        return self.body;
      }
    };
    new JavaMeth(terp.wrap.clsMethTop, "source", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        MethTop self = (MethTop) me;
        return terp.newStr(self.source);
      }
    };
    // CMD meth MethTop "meth" sends TYPE Dict NAME _sends PARAMS 
    // CLS MethTop GROUP "meth" METH sends REMARK ""

    new JavaMeth(terp.wrap.clsMethTop, "sends", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      MethTop self = (MethTop) me;
        return self._sends ();
      }
    };

    new JavaMeth(terp.wrap.clsSend, "rcvr", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        Send self = (Send) me;
        return self.rcvr;
      }
    };
    new JavaMeth(terp.wrap.clsSend, "msg", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        Send self = (Send) me;
        return terp.newStr(self.msg);
      }
    };
    new JavaMeth(terp.wrap.clsSend, "args", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        Send self = (Send) me;
        return new Vec(terp, self.args);
      }
    };
    new JavaMeth(terp.wrap.clsSend, "sourceLoc", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        Send self = (Send) me;
        return new Vec(terp, self.sourceLoc);
      }
    };
    // CMD meth ClsCls "access" at: TYPE Ur NAME at__ PARAMS {Terp terp} { String s}
    // CLS ClsCls GROUP "access" METH at: REMARK ""

    new JavaMeth(terp.wrap.clsClsCls, "at:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsClsCls;
        Str a = (Str) args[0];
        return Cls.at__ (terp,a.str);
      }
    };

    // CMD meth Cls "access" at: TYPE Ur NAME at_ PARAMS {String s}
    // CLS Cls GROUP "access" METH at: REMARK ""

    new JavaMeth(terp.wrap.clsCls, "at:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Cls self = (Cls) me;
        Str a = (Str) args[0];
        return self.at_ (a.str);
      }
    };

    // CMD meth Cls "new" defSub: TYPE Cls NAME defineSubclass PARAMS {String newname}
    // CLS Cls GROUP "new" METH defSub: REMARK ""

    new JavaMeth(terp.wrap.clsCls, "defSub:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Cls self = (Cls) me;
        Str a = (Str) args[0];
        return self.defineSubclass (a.str);
      }
    };

    // CMD meth Cls "access" allMeths TYPE Dict NAME allMeths PARAMS 
    // CLS Cls GROUP "access" METH allMeths REMARK ""

    new JavaMeth(terp.wrap.clsCls, "allMeths", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Cls self = (Cls) me;
        return self.allMeths ();
      }
    };

    // CMD meth Cls "access" vars TYPE Vec NAME _vars PARAMS 
    // CLS Cls GROUP "access" METH vars REMARK ""

    new JavaMeth(terp.wrap.clsCls, "vars", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Cls self = (Cls) me;
        return self._vars ();
      }
    };

    // CMD meth Cls "access" defVars: TYPE void NAME defVars_ PARAMS {String varNames}
    // CLS Cls GROUP "access" METH defVars: REMARK ""

    new JavaMeth(terp.wrap.clsCls, "defVars:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Cls self = (Cls) me;
        Str a = (Str) args[0];
        self.defVars_ (a.str);
        return self;
      }
    };

    // CMD meth Cls "access" name TYPE String NAME _name PARAMS 
    // CLS Cls GROUP "access" METH name REMARK ""

    new JavaMeth(terp.wrap.clsCls, "name", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Cls self = (Cls) me;
        String z = self._name ();
        return (z == null) ? terp.instNil : terp.newStr(z);
      }
    };

    // CMD meth Cls "access" superCls,sup TYPE Cls NAME _superCls PARAMS 
    // CLS Cls GROUP "access" METH superCls,sup REMARK ""

    new JavaMeth(terp.wrap.clsCls, "superCls", "sup") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Cls self = (Cls) me;
        return self._superCls ();
      }
    };

    // CMD meth Cls "access" meths TYPE Dict NAME _meths PARAMS 
    // CLS Cls GROUP "access" METH meths REMARK ""

    new JavaMeth(terp.wrap.clsCls, "meths", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Cls self = (Cls) me;
        return self._meths ();
      }
    };

    // CMD meth Cls "meth" defineMethod:abbrev:doc:code:,defMeth:a:d:c: "" TYPE void NAME defMeth PARAMS {String methName} { String abbrev} { String doc} { String code}
    // CLS Cls GROUP "meth" METH defineMethod:abbrev:doc:code:,defMeth:a:d:c: REMARK ""

    new JavaMeth(terp.wrap.clsCls, "defineMethod:abbrev:doc:code:", "defMeth:a:d:c:") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Cls self = (Cls) me;
        Str a = (Str) args[0];
        Str b = (Str) args[1];
        Str c = (Str) args[2];
        Str d = (Str) args[3];
        self.defMeth (a.str, b.str, c.str, d.str);
        return self;
      }
    };

    // CMD meth Cls "meth" trace: "" TYPE void NAME trace_ PARAMS {boolean a}
    // CLS Cls GROUP "meth" METH trace: REMARK ""

    new JavaMeth(terp.wrap.clsCls, "trace:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Cls self = (Cls) me;
        boolean a = args[0].truth();
        self.trace_ (a);
        return self;
      }
    };

    // CMD meth ClsCls "meth" all "dict of all classes, by name" TYPE Dict NAME _all PARAMS {Terp terp}
    // CLS ClsCls GROUP "meth" METH all REMARK "dict of all classes, by name"

    new JavaMeth(terp.wrap.clsClsCls, "all", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = terp.wrap.clsClsCls;
        return Cls._all (terp);
      }
    };

    new JavaMeth(terp.wrap.clsMeth, "onCls", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        Meth self = (Meth) me;
        return self.onCls;
      }
    };
    new JavaMeth(terp.wrap.clsMeth, "name", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        Meth self = (Meth) me;
        return terp.newStr(self.name);
      }
    };
    new JavaMeth(terp.wrap.clsMeth, "abbrev", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        Meth self = (Meth) me;
        return terp.newStr(self.abbrev);
      }
    };
    new JavaMeth(terp.wrap.clsMeth, "doc", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        Meth self = (Meth) me;
        return terp.newStr(self.doc);
      }
    };
    // CMD meth Meth "eval" applyFrame:receiver:args: TYPE Ur NAME apply PARAMS {Frame f} { Ur r} { Ur[] args);  		public String toString(}
    // CLS Meth GROUP "eval" METH applyFrame:receiver:args: REMARK ""

    new JavaMeth(terp.wrap.clsMeth, "applyFrame:receiver:args:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Meth self = (Meth) me;
        Frame a = (Frame) args[0];
        Ur b = (Ur) args[1];
        Vec vec_c = args[2].mustVec();
        int len_c = vec_c.vec.size();
        Ur[] c = new Ur[len_c];
        for (int i_c = 0; i_c < len_c; ++i_c) {
          c[i_c] = vec_c.vec.get(i_c);
        }
        return self.apply (a, b, c);
      }
    };

    new JavaMeth(terp.wrap.clsUsrMeth, "src", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        UsrMeth self = (UsrMeth) me;
        return terp.newStr(self.src);
      }
    };
    // CMD meth UsrMeth "eval" top TYPE MethTop NAME _top PARAMS 
    // CLS UsrMeth GROUP "eval" METH top REMARK ""

    new JavaMeth(terp.wrap.clsUsrMeth, "top", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      UsrMeth self = (UsrMeth) me;
        return self._top ();
      }
    };

    // CMD meth Usr "usr" opath TYPE String NAME opath PARAMS 
    // CLS Usr GROUP "usr" METH opath REMARK ""

    new JavaMeth(terp.wrap.clsUsr, "opath", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Usr self = (Usr) me;
        String z = self.opath ();
        return (z == null) ? terp.instNil : terp.newStr(z);
      }
    };

    // CMD meth Usr "usr" oname TYPE String NAME oname PARAMS 
    // CLS Usr GROUP "usr" METH oname REMARK ""

    new JavaMeth(terp.wrap.clsUsr, "oname", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Usr self = (Usr) me;
        String z = self.oname ();
        return (z == null) ? terp.instNil : terp.newStr(z);
      }
    };

    // CMD meth Usr "access" oid TYPE int NAME oid PARAMS 
    // CLS Usr GROUP "access" METH oid REMARK ""

    new JavaMeth(terp.wrap.clsUsr, "oid", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Usr self = (Usr) me;
        return terp.newNum(self.oid ());
      }
    };

    // CMD meth Usr "access" omention TYPE int NAME omention PARAMS 
    // CLS Usr GROUP "access" METH omention REMARK ""

    new JavaMeth(terp.wrap.clsUsr, "omention", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Usr self = (Usr) me;
        return terp.newNum(self.omention ());
      }
    };

    // CMD meth UsrCls "new" new TYPE Usr NAME _new PARAMS 
    // CLS UsrCls GROUP "new" METH new REMARK ""

    new JavaMeth(terp.wrap.clsUsrCls, "new", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      UsrCls self = (UsrCls) me;
        return self._new ();
      }
    };

    // CMD meth UsrCls "usr" find: TYPE Obj NAME find PARAMS {String idOrName}
    // CLS UsrCls GROUP "usr" METH find: REMARK ""

    new JavaMeth(terp.wrap.clsUsrCls, "find:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      UsrCls self = (UsrCls) me;
        Str a = (Str) args[0];
        return self.find (a.str);
      }
    };


  }


  public void installClasses(final Terp terp) {


    this.clsUrCls = terp.tUrCls;
    this.clsUr = terp.tUr;
    this.clsObjCls = terp.tObjCls;
    this.clsObj = terp.tObj;
    this.clsUsrCls = terp.tUsrCls;
    this.clsUsr = terp.tUsr;


    
    this.clsSuperCls = terp.clss.get("supercls");
    if (this.clsSuperCls == null) {
      this.clsSuperCls = new Cls(terp.tMetacls, terp, "SuperCls", this.clsUrCls);
    }
    this.clsSuper      = terp.clss.get("super");
    if (this.clsSuper == null) {
      this.clsSuper = new Cls(this.clsSuperCls, terp, "Super", this.clsUr);
    }
            
    this.clsObjCls = terp.clss.get("objcls");
    if (this.clsObjCls == null) {
      this.clsObjCls = new Cls(terp.tMetacls, terp, "ObjCls", this.clsUrCls);
    }
    this.clsObj      = terp.clss.get("obj");
    if (this.clsObj == null) {
      this.clsObj = new Cls(this.clsObjCls, terp, "Obj", this.clsUr);
    }
            
    this.clsFileCls = terp.clss.get("filecls");
    if (this.clsFileCls == null) {
      this.clsFileCls = new Cls(terp.tMetacls, terp, "FileCls", this.clsObjCls);
    }
    this.clsFile      = terp.clss.get("file");
    if (this.clsFile == null) {
      this.clsFile = new Cls(this.clsFileCls, terp, "File", this.clsObj);
    }
            
    this.clsHubCls = terp.clss.get("hubcls");
    if (this.clsHubCls == null) {
      this.clsHubCls = new Cls(terp.tMetacls, terp, "HubCls", this.clsObjCls);
    }
    this.clsHub      = terp.clss.get("hub");
    if (this.clsHub == null) {
      this.clsHub = new Cls(this.clsHubCls, terp, "Hub", this.clsObj);
    }
            
    this.clsSysCls = terp.clss.get("syscls");
    if (this.clsSysCls == null) {
      this.clsSysCls = new Cls(terp.tMetacls, terp, "SysCls", this.clsObjCls);
    }
    this.clsSys      = terp.clss.get("sys");
    if (this.clsSys == null) {
      this.clsSys = new Cls(this.clsSysCls, terp, "Sys", this.clsObj);
    }
            
    this.clsUndefinedCls = terp.clss.get("undefinedcls");
    if (this.clsUndefinedCls == null) {
      this.clsUndefinedCls = new Cls(terp.tMetacls, terp, "UndefinedCls", this.clsObjCls);
    }
    this.clsUndefined      = terp.clss.get("undefined");
    if (this.clsUndefined == null) {
      this.clsUndefined = new Cls(this.clsUndefinedCls, terp, "Undefined", this.clsObj);
    }
            
    this.clsBlkCls = terp.clss.get("blkcls");
    if (this.clsBlkCls == null) {
      this.clsBlkCls = new Cls(terp.tMetacls, terp, "BlkCls", this.clsObjCls);
    }
    this.clsBlk      = terp.clss.get("blk");
    if (this.clsBlk == null) {
      this.clsBlk = new Cls(this.clsBlkCls, terp, "Blk", this.clsObj);
    }
            
    this.clsNumCls = terp.clss.get("numcls");
    if (this.clsNumCls == null) {
      this.clsNumCls = new Cls(terp.tMetacls, terp, "NumCls", this.clsObjCls);
    }
    this.clsNum      = terp.clss.get("num");
    if (this.clsNum == null) {
      this.clsNum = new Cls(this.clsNumCls, terp, "Num", this.clsObj);
    }
            
    this.clsBufCls = terp.clss.get("bufcls");
    if (this.clsBufCls == null) {
      this.clsBufCls = new Cls(terp.tMetacls, terp, "BufCls", this.clsObjCls);
    }
    this.clsBuf      = terp.clss.get("buf");
    if (this.clsBuf == null) {
      this.clsBuf = new Cls(this.clsBufCls, terp, "Buf", this.clsObj);
    }
            
    this.clsStrCls = terp.clss.get("strcls");
    if (this.clsStrCls == null) {
      this.clsStrCls = new Cls(terp.tMetacls, terp, "StrCls", this.clsObjCls);
    }
    this.clsStr      = terp.clss.get("str");
    if (this.clsStr == null) {
      this.clsStr = new Cls(this.clsStrCls, terp, "Str", this.clsObj);
    }
            
    this.clsRexCls = terp.clss.get("rexcls");
    if (this.clsRexCls == null) {
      this.clsRexCls = new Cls(terp.tMetacls, terp, "RexCls", this.clsObjCls);
    }
    this.clsRex      = terp.clss.get("rex");
    if (this.clsRex == null) {
      this.clsRex = new Cls(this.clsRexCls, terp, "Rex", this.clsObj);
    }
            
    this.clsVecCls = terp.clss.get("veccls");
    if (this.clsVecCls == null) {
      this.clsVecCls = new Cls(terp.tMetacls, terp, "VecCls", this.clsObjCls);
    }
    this.clsVec      = terp.clss.get("vec");
    if (this.clsVec == null) {
      this.clsVec = new Cls(this.clsVecCls, terp, "Vec", this.clsObj);
    }
            
    this.clsDictCls = terp.clss.get("dictcls");
    if (this.clsDictCls == null) {
      this.clsDictCls = new Cls(terp.tMetacls, terp, "DictCls", this.clsObjCls);
    }
    this.clsDict      = terp.clss.get("dict");
    if (this.clsDict == null) {
      this.clsDict = new Cls(this.clsDictCls, terp, "Dict", this.clsObj);
    }
            
    this.clsHtCls = terp.clss.get("htcls");
    if (this.clsHtCls == null) {
      this.clsHtCls = new Cls(terp.tMetacls, terp, "HtCls", this.clsObjCls);
    }
    this.clsHt      = terp.clss.get("ht");
    if (this.clsHt == null) {
      this.clsHt = new Cls(this.clsHtCls, terp, "Ht", this.clsObj);
    }
            
    this.clsFrameCls = terp.clss.get("framecls");
    if (this.clsFrameCls == null) {
      this.clsFrameCls = new Cls(terp.tMetacls, terp, "FrameCls", this.clsObjCls);
    }
    this.clsFrame      = terp.clss.get("frame");
    if (this.clsFrame == null) {
      this.clsFrame = new Cls(this.clsFrameCls, terp, "Frame", this.clsObj);
    }
            
    this.clsExprCls = terp.clss.get("exprcls");
    if (this.clsExprCls == null) {
      this.clsExprCls = new Cls(terp.tMetacls, terp, "ExprCls", this.clsObjCls);
    }
    this.clsExpr      = terp.clss.get("expr");
    if (this.clsExpr == null) {
      this.clsExpr = new Cls(this.clsExprCls, terp, "Expr", this.clsObj);
    }
            
    this.clsLValueCls = terp.clss.get("lvaluecls");
    if (this.clsLValueCls == null) {
      this.clsLValueCls = new Cls(terp.tMetacls, terp, "LValueCls", this.clsExprCls);
    }
    this.clsLValue      = terp.clss.get("lvalue");
    if (this.clsLValue == null) {
      this.clsLValue = new Cls(this.clsLValueCls, terp, "LValue", this.clsExpr);
    }
            
    this.clsLvNameCls = terp.clss.get("lvnamecls");
    if (this.clsLvNameCls == null) {
      this.clsLvNameCls = new Cls(terp.tMetacls, terp, "LvNameCls", this.clsLValueCls);
    }
    this.clsLvName      = terp.clss.get("lvname");
    if (this.clsLvName == null) {
      this.clsLvName = new Cls(this.clsLvNameCls, terp, "LvName", this.clsLValue);
    }
            
    this.clsLvLocalNameCls = terp.clss.get("lvlocalnamecls");
    if (this.clsLvLocalNameCls == null) {
      this.clsLvLocalNameCls = new Cls(terp.tMetacls, terp, "LvLocalNameCls", this.clsLvNameCls);
    }
    this.clsLvLocalName      = terp.clss.get("lvlocalname");
    if (this.clsLvLocalName == null) {
      this.clsLvLocalName = new Cls(this.clsLvLocalNameCls, terp, "LvLocalName", this.clsLvName);
    }
            
    this.clsLvInstNameCls = terp.clss.get("lvinstnamecls");
    if (this.clsLvInstNameCls == null) {
      this.clsLvInstNameCls = new Cls(terp.tMetacls, terp, "LvInstNameCls", this.clsLvNameCls);
    }
    this.clsLvInstName      = terp.clss.get("lvinstname");
    if (this.clsLvInstName == null) {
      this.clsLvInstName = new Cls(this.clsLvInstNameCls, terp, "LvInstName", this.clsLvName);
    }
            
    this.clsLvTupleCls = terp.clss.get("lvtuplecls");
    if (this.clsLvTupleCls == null) {
      this.clsLvTupleCls = new Cls(terp.tMetacls, terp, "LvTupleCls", this.clsLValueCls);
    }
    this.clsLvTuple      = terp.clss.get("lvtuple");
    if (this.clsLvTuple == null) {
      this.clsLvTuple = new Cls(this.clsLvTupleCls, terp, "LvTuple", this.clsLValue);
    }
            
    this.clsLvListCls = terp.clss.get("lvlistcls");
    if (this.clsLvListCls == null) {
      this.clsLvListCls = new Cls(terp.tMetacls, terp, "LvListCls", this.clsLvTupleCls);
    }
    this.clsLvList      = terp.clss.get("lvlist");
    if (this.clsLvList == null) {
      this.clsLvList = new Cls(this.clsLvListCls, terp, "LvList", this.clsLvTuple);
    }
            
    this.clsMethTopCls = terp.clss.get("methtopcls");
    if (this.clsMethTopCls == null) {
      this.clsMethTopCls = new Cls(terp.tMetacls, terp, "MethTopCls", this.clsExprCls);
    }
    this.clsMethTop      = terp.clss.get("methtop");
    if (this.clsMethTop == null) {
      this.clsMethTop = new Cls(this.clsMethTopCls, terp, "MethTop", this.clsExpr);
    }
            
    this.clsPutLValueCls = terp.clss.get("putlvaluecls");
    if (this.clsPutLValueCls == null) {
      this.clsPutLValueCls = new Cls(terp.tMetacls, terp, "PutLValueCls", this.clsExprCls);
    }
    this.clsPutLValue      = terp.clss.get("putlvalue");
    if (this.clsPutLValue == null) {
      this.clsPutLValue = new Cls(this.clsPutLValueCls, terp, "PutLValue", this.clsExpr);
    }
            
    this.clsGetInstVarCls = terp.clss.get("getinstvarcls");
    if (this.clsGetInstVarCls == null) {
      this.clsGetInstVarCls = new Cls(terp.tMetacls, terp, "GetInstVarCls", this.clsExprCls);
    }
    this.clsGetInstVar      = terp.clss.get("getinstvar");
    if (this.clsGetInstVar == null) {
      this.clsGetInstVar = new Cls(this.clsGetInstVarCls, terp, "GetInstVar", this.clsExpr);
    }
            
    this.clsGetLocalVarCls = terp.clss.get("getlocalvarcls");
    if (this.clsGetLocalVarCls == null) {
      this.clsGetLocalVarCls = new Cls(terp.tMetacls, terp, "GetLocalVarCls", this.clsExprCls);
    }
    this.clsGetLocalVar      = terp.clss.get("getlocalvar");
    if (this.clsGetLocalVar == null) {
      this.clsGetLocalVar = new Cls(this.clsGetLocalVarCls, terp, "GetLocalVar", this.clsExpr);
    }
            
    this.clsGetSelfCls = terp.clss.get("getselfcls");
    if (this.clsGetSelfCls == null) {
      this.clsGetSelfCls = new Cls(terp.tMetacls, terp, "GetSelfCls", this.clsExprCls);
    }
    this.clsGetSelf      = terp.clss.get("getself");
    if (this.clsGetSelf == null) {
      this.clsGetSelf = new Cls(this.clsGetSelfCls, terp, "GetSelf", this.clsExpr);
    }
            
    this.clsGetFrameCls = terp.clss.get("getframecls");
    if (this.clsGetFrameCls == null) {
      this.clsGetFrameCls = new Cls(terp.tMetacls, terp, "GetFrameCls", this.clsExprCls);
    }
    this.clsGetFrame      = terp.clss.get("getframe");
    if (this.clsGetFrame == null) {
      this.clsGetFrame = new Cls(this.clsGetFrameCls, terp, "GetFrame", this.clsExpr);
    }
            
    this.clsGetGlobalVarCls = terp.clss.get("getglobalvarcls");
    if (this.clsGetGlobalVarCls == null) {
      this.clsGetGlobalVarCls = new Cls(terp.tMetacls, terp, "GetGlobalVarCls", this.clsExprCls);
    }
    this.clsGetGlobalVar      = terp.clss.get("getglobalvar");
    if (this.clsGetGlobalVar == null) {
      this.clsGetGlobalVar = new Cls(this.clsGetGlobalVarCls, terp, "GetGlobalVar", this.clsExpr);
    }
            
    this.clsSendCls = terp.clss.get("sendcls");
    if (this.clsSendCls == null) {
      this.clsSendCls = new Cls(terp.tMetacls, terp, "SendCls", this.clsExprCls);
    }
    this.clsSend      = terp.clss.get("send");
    if (this.clsSend == null) {
      this.clsSend = new Cls(this.clsSendCls, terp, "Send", this.clsExpr);
    }
            
    this.clsBlockCls = terp.clss.get("blockcls");
    if (this.clsBlockCls == null) {
      this.clsBlockCls = new Cls(terp.tMetacls, terp, "BlockCls", this.clsExprCls);
    }
    this.clsBlock      = terp.clss.get("block");
    if (this.clsBlock == null) {
      this.clsBlock = new Cls(this.clsBlockCls, terp, "Block", this.clsExpr);
    }
            
    this.clsSeqCls = terp.clss.get("seqcls");
    if (this.clsSeqCls == null) {
      this.clsSeqCls = new Cls(terp.tMetacls, terp, "SeqCls", this.clsExprCls);
    }
    this.clsSeq      = terp.clss.get("seq");
    if (this.clsSeq == null) {
      this.clsSeq = new Cls(this.clsSeqCls, terp, "Seq", this.clsExpr);
    }
            
    this.clsMakeVecCls = terp.clss.get("makeveccls");
    if (this.clsMakeVecCls == null) {
      this.clsMakeVecCls = new Cls(terp.tMetacls, terp, "MakeVecCls", this.clsExprCls);
    }
    this.clsMakeVec      = terp.clss.get("makevec");
    if (this.clsMakeVec == null) {
      this.clsMakeVec = new Cls(this.clsMakeVecCls, terp, "MakeVec", this.clsExpr);
    }
            
    this.clsLitCls = terp.clss.get("litcls");
    if (this.clsLitCls == null) {
      this.clsLitCls = new Cls(terp.tMetacls, terp, "LitCls", this.clsExprCls);
    }
    this.clsLit      = terp.clss.get("lit");
    if (this.clsLit == null) {
      this.clsLit = new Cls(this.clsLitCls, terp, "Lit", this.clsExpr);
    }
            
    this.clsEmptyExprListCls = terp.clss.get("emptyexprlistcls");
    if (this.clsEmptyExprListCls == null) {
      this.clsEmptyExprListCls = new Cls(terp.tMetacls, terp, "EmptyExprListCls", this.clsExprCls);
    }
    this.clsEmptyExprList      = terp.clss.get("emptyexprlist");
    if (this.clsEmptyExprList == null) {
      this.clsEmptyExprList = new Cls(this.clsEmptyExprListCls, terp, "EmptyExprList", this.clsExpr);
    }
            
    this.clsClsCls = terp.clss.get("clscls");
    if (this.clsClsCls == null) {
      this.clsClsCls = new Cls(terp.tMetacls, terp, "ClsCls", this.clsObjCls);
    }
    this.clsCls      = terp.clss.get("cls");
    if (this.clsCls == null) {
      this.clsCls = new Cls(this.clsClsCls, terp, "Cls", this.clsObj);
    }
            
    this.clsMethCls = terp.clss.get("methcls");
    if (this.clsMethCls == null) {
      this.clsMethCls = new Cls(terp.tMetacls, terp, "MethCls", this.clsObjCls);
    }
    this.clsMeth      = terp.clss.get("meth");
    if (this.clsMeth == null) {
      this.clsMeth = new Cls(this.clsMethCls, terp, "Meth", this.clsObj);
    }
            
    this.clsJavaMethCls = terp.clss.get("javamethcls");
    if (this.clsJavaMethCls == null) {
      this.clsJavaMethCls = new Cls(terp.tMetacls, terp, "JavaMethCls", this.clsMethCls);
    }
    this.clsJavaMeth      = terp.clss.get("javameth");
    if (this.clsJavaMeth == null) {
      this.clsJavaMeth = new Cls(this.clsJavaMethCls, terp, "JavaMeth", this.clsMeth);
    }
            
    this.clsUsrMethCls = terp.clss.get("usrmethcls");
    if (this.clsUsrMethCls == null) {
      this.clsUsrMethCls = new Cls(terp.tMetacls, terp, "UsrMethCls", this.clsMethCls);
    }
    this.clsUsrMeth      = terp.clss.get("usrmeth");
    if (this.clsUsrMeth == null) {
      this.clsUsrMeth = new Cls(this.clsUsrMethCls, terp, "UsrMeth", this.clsMeth);
    }
            
    this.clsParserCls = terp.clss.get("parsercls");
    if (this.clsParserCls == null) {
      this.clsParserCls = new Cls(terp.tMetacls, terp, "ParserCls", this.clsObjCls);
    }
    this.clsParser      = terp.clss.get("parser");
    if (this.clsParser == null) {
      this.clsParser = new Cls(this.clsParserCls, terp, "Parser", this.clsObj);
    }
            
    this.clsUsrCls = terp.clss.get("usrcls");
    if (this.clsUsrCls == null) {
      this.clsUsrCls = new Cls(terp.tMetacls, terp, "UsrCls", this.clsObjCls);
    }
    this.clsUsr      = terp.clss.get("usr");
    if (this.clsUsr == null) {
      this.clsUsr = new Cls(this.clsUsrCls, terp, "Usr", this.clsObj);
    }
            
    this.clsTmpCls = terp.clss.get("tmpcls");
    if (this.clsTmpCls == null) {
      this.clsTmpCls = new Cls(terp.tMetacls, terp, "TmpCls", this.clsUsrCls);
    }
    this.clsTmp      = terp.clss.get("tmp");
    if (this.clsTmp == null) {
      this.clsTmp = new Cls(this.clsTmpCls, terp, "Tmp", this.clsUsr);
    }
            
  }
  
  public Cls clsUrCls;
  public Cls clsUr;
          
  public Cls clsSuperCls;
  public Cls clsSuper;
          
  public Cls clsObjCls;
  public Cls clsObj;
          
  public Cls clsFileCls;
  public Cls clsFile;
          
  public Cls clsHubCls;
  public Cls clsHub;
          
  public Cls clsSysCls;
  public Cls clsSys;
          
  public Cls clsUndefinedCls;
  public Cls clsUndefined;
          
  public Cls clsBlkCls;
  public Cls clsBlk;
          
  public Cls clsNumCls;
  public Cls clsNum;
          
  public Cls clsBufCls;
  public Cls clsBuf;
          
  public Cls clsStrCls;
  public Cls clsStr;
          
  public Cls clsRexCls;
  public Cls clsRex;
          
  public Cls clsVecCls;
  public Cls clsVec;
          
  public Cls clsDictCls;
  public Cls clsDict;
          
  public Cls clsHtCls;
  public Cls clsHt;
          
  public Cls clsFrameCls;
  public Cls clsFrame;
          
  public Cls clsExprCls;
  public Cls clsExpr;
          
  public Cls clsLValueCls;
  public Cls clsLValue;
          
  public Cls clsLvNameCls;
  public Cls clsLvName;
          
  public Cls clsLvLocalNameCls;
  public Cls clsLvLocalName;
          
  public Cls clsLvInstNameCls;
  public Cls clsLvInstName;
          
  public Cls clsLvTupleCls;
  public Cls clsLvTuple;
          
  public Cls clsLvListCls;
  public Cls clsLvList;
          
  public Cls clsMethTopCls;
  public Cls clsMethTop;
          
  public Cls clsPutLValueCls;
  public Cls clsPutLValue;
          
  public Cls clsGetInstVarCls;
  public Cls clsGetInstVar;
          
  public Cls clsGetLocalVarCls;
  public Cls clsGetLocalVar;
          
  public Cls clsGetSelfCls;
  public Cls clsGetSelf;
          
  public Cls clsGetFrameCls;
  public Cls clsGetFrame;
          
  public Cls clsGetGlobalVarCls;
  public Cls clsGetGlobalVar;
          
  public Cls clsSendCls;
  public Cls clsSend;
          
  public Cls clsBlockCls;
  public Cls clsBlock;
          
  public Cls clsSeqCls;
  public Cls clsSeq;
          
  public Cls clsMakeVecCls;
  public Cls clsMakeVec;
          
  public Cls clsLitCls;
  public Cls clsLit;
          
  public Cls clsEmptyExprListCls;
  public Cls clsEmptyExprList;
          
  public Cls clsClsCls;
  public Cls clsCls;
          
  public Cls clsMethCls;
  public Cls clsMeth;
          
  public Cls clsJavaMethCls;
  public Cls clsJavaMeth;
          
  public Cls clsUsrMethCls;
  public Cls clsUsrMeth;
          
  public Cls clsParserCls;
  public Cls clsParser;
          
  public Cls clsUsrCls;
  public Cls clsUsr;
          
  public Cls clsTmpCls;
  public Cls clsTmp;
          


}

