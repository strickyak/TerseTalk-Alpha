#!/usr/bin/tclsh
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


set CLASS [lindex $argv 0]
set argv [lrange $argv 1 end]

if {$CLASS == "WrapAndy"} {

  # # Disable it for now.
  # puts "package terse.a1;" 
  # puts "public class $CLASS { }"
  # exit 0
  # # Disable it for now.

  set FIELD ((AndyTerp)terp).wrapandy
} else {
  set FIELD terp.wrap
}

proc WrapMarkedItemsInJavaFile {file} {
  global FIELD
  set f [open $file]
  while {[gets $f line] >= 0} {
    if [regexp {^// =(.*)} [string trim $line] - cmd] {
      puts stderr "Triggers on <$line>"
      while {[gets $f line2] >= 0} {
        if [regexp {^//(.*)} [string trim $line2] - cmd2] {
          append cmd " $cmd2"
        } else {
          break
        }
      }

      set verb [lindex $cmd 0]
      puts stderr "Verb is <$verb>"
      puts stderr "Cmd is <$cmd>"

      if {$verb == "meth"} {
        puts stderr "Got meth."

        set cls [lindex $cmd 1]
        set group [lindex $cmd 2]
        set kws [lindex $cmd 3]
        set remark [lindex $cmd 4]

        set kww [split $kws ","]
        set kw0 [lindex $kww 0]
        set kw1 [lindex $kww 1]

        set decl $line2
        while {[gets $f line3] >= 0} {
          append decl " " $line3
          if [regexp {\}$} [string trim $line3] - cmd2] {
            break
          }
        }
        if [regexp {^\s*public\s+(abstract\s+)?(static\s+)?(synchronized\s+)?(\w+)\s+(\w+)\s*[(]([^{}]*)[)]} $decl - abstract static sync type name params] {
           set pp [split $params ","]
           set lpp [llength $pp]
           puts "    // CMD $cmd TYPE $type NAME $name PARAMS $pp"
           puts "    // CLS $cls GROUP \"$group\" METH $kws REMARK \"$remark\""
	   puts ""
	   puts "    new JavaMeth($FIELD.cls$cls, \"$kw0\", \"$kw1\") {"
	   puts "      public Ur apply(Frame frame, Ur me, Ur\[\] args) {"
	   if {[string match {[A-Za-z_]*Cls} $cls] && $cls != "UsrCls"} {
	     # Cls methods.
	     if {![string match static* $static]} {error "Should be static"}
	     if {[string match {Terp *} [lindex $pp 0]]} {
	       # Most Cls methods.
	       puts "        Cls self = $FIELD.cls$cls;"
	       set call "[string range $cls 0 end-3].$name (terp"  ;# Drop the trailing Cls.
	       set pp [lrange $pp 1 end]  ;# Pop off Terp.
	     } elseif {[string match {Cls *} [lindex $pp 0]]} {
	       # Special UsrCls methods.
	       set p [lindex $pp 0]
	       set atype [lindex $p 0]
	       set aname [lindex $p 1]
	       puts "        Cls self = (Cls) me;"
	       set call "[string range $cls 0 end-3].$name (self"  ;# Drop the trailing Cls.
	       set pp [lrange $pp 1 end]  ;# Pop off Cls.
	     } else {
	       error "Expected Terp or Cls as first arg for static methods on Cls cls."
	     }
	     set comma ","
	     set nparams [expr $lpp-1]
	   } else {
	     # Inst methods.
	     if {$static != ""} {error "Should not be static"}
	     puts "      $cls self = ($cls) me;"
	     set call "self.$name ("
	     set comma ""
	     set nparams $lpp
	   }
	   # Check arity.
	   foreach kw $kww {
	     if [string match {[a-z_]*} $kw] {
	       set ncolons [string length [regsub -all {[^:]+} $kw {}]]
	       if { $ncolons != $nparams } { error "kw <$kw> has $ncolons colons, but $cls :: $name has $nparams params." }
	     } else {
	       # Must be binop like + * ==
	       if { 1 != $nparams } { error "binop <$kw> needs 1 param, but $cls :: $name has $nparams params." }
	     }
	   }
	   set i 0
	   foreach p $pp {
	     set atype [lindex $p 0]
	     set aname [lindex $p 1]
	     append call "$comma"
	     set c [format "%c" [expr 97+$i]]
             if {$atype eq "String"} {
               # Special String case.
	       puts "        Str $c = (Str) args\[$i\];"
	       append call "$c.str"
             } elseif {$atype eq "int"} {
               # Special int case.
	       puts "        Num $c = (Num) args\[$i\];"
	       append call "$c.toNearestInt()"
             } elseif {$atype eq "double"} {
               # Special double case.
	       puts "        Num $c = (Num) args\[$i\];"
	       append call "$c.num"
             } elseif {$atype eq "float"} {
               # Special float case.
	       puts "        Num $c = (Num) args\[$i\];"
	       append call "(float)$c.num"
             } elseif {$atype eq "boolean"} {
               # Special boolean case.
	       puts "        boolean $c = args\[$i\].truth();"
	       append call "$c"
             } elseif {$atype eq {Ur[]}} {
               # Special Ur[] case -- expects a Vec.
	       puts "        Vec vec_$c = args\[$i\].mustVec();"
	       puts "        int len_$c = vec_$c.vec.size();"
	       puts "        Ur\[\] $c = new Ur\[len_$c\];"
	       puts "        for (int i_$c = 0; i_$c < len_$c; ++i_$c) {"
	       puts "          $c\[i_$c\] = vec_$c.vec.get(i_$c);"
	       puts "        }"
	       append call "$c"
             } else {
               # Normal TerseTalk types.
	       puts "        $atype $c = ($atype) args\[$i\];"
	       append call "$c"
             }
	     incr i
	     set comma ", "
	   }
	   append call ")"
	   if {$type == "void"} {
	     puts "        $call;"
	     puts "        return self;"
	   } elseif {$type == "String"} {
	     puts "        String z = $call;"
	     puts "        return (z == null) ? terp.instNil : terp.newStr(z);"
	   } elseif {$type == "boolean"} {
	     puts "        return terp.boolObj($call);"
	   } elseif {$type == "int"} {
	     puts "        return terp.newNum($call);"
	   } elseif {$type == "double"} {
	     puts "        return terp.newNum($call);"
	   } elseif {$type == "float"} {
	     puts "        return terp.newNum($call);"
	   } else {
	     puts "        return $call;"
	   }
	   puts "      }"
	   puts "    };"
	   puts ""
        } else {
          error "Did not find public meth pattern for <$decl>."
        }


      } elseif {$verb == "cls"} {
        set category [lindex $cmd 1]
        set cls [lindex $cmd 2]
        set supercls [lindex $cmd 3]

        # UsrCls is different; don't make or install or slot it.
	if {$cls != "UsrCls"} {
          append ::slots [subst {
  public Cls cls${cls}Cls;
  public Cls cls${cls};
          }]

          if [string length $supercls] {
            append ::installClasses [subst {
    this.cls${cls}Cls = terp.clss.get("[string tolower ${cls}Cls]");
    if (this.cls${cls}Cls == null) {
      this.cls${cls}Cls = new Cls(terp.tMetacls, terp, "${cls}Cls", this.cls${supercls}Cls);
    }
    this.cls$cls      = terp.clss.get("[string tolower $cls]");
    if (this.cls$cls == null) {
      this.cls${cls} = new Cls(this.cls${cls}Cls, terp, "${cls}", this.cls${supercls});
    }
            }]
	  }
	}

      } elseif {$verb == "get"} {
        set cls [lindex $cmd 1]
        set type [lindex $cmd 2]
        set slot [lindex $cmd 3]
        set meth [lindex $cmd 4]
        set doc [lindex $cmd 5]

	   puts "    new JavaMeth($FIELD.cls$cls, \"$meth\", \"\") {"
	   puts "      public Ur apply(Frame f, Ur me, Ur\[\] args) {"
	   puts "        $cls self = ($cls) me;" 

	   if { $type == "int" } {
	     puts "        return terp.newNum(self.$slot);"
	   } elseif { $type == "double" } {
	     puts "        return terp.newNum(self.$slot);"
	   } elseif { $type == "float" } {
	     puts "        return terp.newNum(self.$slot);"
	   } elseif { $type == "String" } {
	     puts "        return terp.newStr(self.$slot);"

	   } elseif { $type == {Ur[]} } {
	     puts "        return new Vec(terp, self.$slot);"
	   } elseif { $type == {Expr[]} } {
	     puts "        return new Vec(terp, self.$slot);"
	   } elseif { $type == {int[]} } {
	     puts "        return new Vec(terp, self.$slot);"
	   #} elseif { $type == {Ur[]} } {
	   #  puts "        return new Vec(terp, Static.pros(self.$slot));"
	   #} elseif { $type == {Expr[]} } {
	   #  puts "        return new Vec(terp, Static.exprs(self.$slot));"
	   #} elseif { $type == {int[]} } {
	   #  puts "        return new Vec(terp, Static.ints(self.$slot));"

	   } elseif { $type == "." } {
	     puts "        return self.$slot;"
	   } else {
	     error "Unknown type <$type> in getter for cls $cls slot $slot"
	   }

	   puts "      }"
	   puts "    };"

      } else {
        error "Dont understand method decl <$decl> after <$cmd>"
      }
    }
  }
}

if {$CLASS == "WrapAndy"} {
  puts "package terse.a1;
  import terse.vm.Static;
  import terse.vm.Ur;
  import terse.vm.Terp;
  import terse.vm.Expr;
  import terse.vm.Cls;
  import terse.a1.TerseActivity.AndyTerp;
  import terse.a1.TerseActivity.Ink;
  import terse.a1.TerseActivity.Motion;
  import terse.a1.TerseActivity.Screen;
  import terse.a1.TerseActivity.Node;
  import terse.a1.TerseActivity.Group;
  import terse.a1.TerseActivity.Prim;
  import terse.a1.TerseActivity.Cube;
  import terse.a1.TerseActivity.Strip;
  import terse.a1.TerseActivity.Fan;
  import terse.a1.TerseActivity.Lines;
  import terse.a1.TerseActivity.Mat;

  import terse.a1.TerseActivity.DualView.FnordView.GGl;
"
} else {
  puts "package terse.vm;"
}


puts "
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

public class $CLASS extends Static \{

  public void installMethods(final Terp terp) \{

"

foreach j $argv {
  WrapMarkedItemsInJavaFile $j
}

puts "
  \}
"

puts "
  public void installClasses(final Terp terp) \{
"
if {1 || $CLASS != "Wrap"} {
puts "
    this.clsUrCls = terp.tUrCls;
    this.clsUr = terp.tUr;
    this.clsObjCls = terp.tObjCls;
    this.clsObj = terp.tObj;
    this.clsUsrCls = terp.tUsrCls;
    this.clsUsr = terp.tUsr;
"
}
puts "
    $::installClasses
  \}
  $::slots
"
if {$CLASS != "Wrap"} {
  puts "
  public Cls clsUrCls;
  public Cls clsUr;
  public Cls clsObjCls;
  public Cls clsObj;
  public Cls clsUsrCls;
  public Cls clsUsr;
  "
}

puts "
\}
"
