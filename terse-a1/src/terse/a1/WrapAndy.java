package terse.a1;
  import terse.vm.Static;
  import terse.vm.Ur;
  import terse.vm.Terp;
  import terse.vm.Expr;
  import terse.vm.Cls;
  import terse.a1.TerseActivity.AndyTerp;
  import terse.a1.TerseActivity.Ink;
  import terse.a1.TerseActivity.Motion;
  import terse.a1.TerseActivity.Screen;


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

public class WrapAndy extends Static {

  public void installMethods(final Terp terp) {


    // CMD meth Motion "access" x TYPE double NAME _x PARAMS 
    // CLS Motion GROUP "access" METH x REMARK ""

    new JavaMeth(((AndyTerp)terp).wrapandy.clsMotion, "x", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Motion self = (Motion) me;
        return terp.newNum(self._x ());
      }
    };

    // CMD meth Motion "access" y TYPE double NAME _y PARAMS 
    // CLS Motion GROUP "access" METH y REMARK ""

    new JavaMeth(((AndyTerp)terp).wrapandy.clsMotion, "y", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Motion self = (Motion) me;
        return terp.newNum(self._y ());
      }
    };

    // CMD meth Motion "access" action TYPE int NAME _action PARAMS 
    // CLS Motion GROUP "access" METH action REMARK ""

    new JavaMeth(((AndyTerp)terp).wrapandy.clsMotion, "action", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Motion self = (Motion) me;
        return terp.newNum(self._action ());
      }
    };

    new JavaMeth(((AndyTerp)terp).wrapandy.clsScreen, "width", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        Screen self = (Screen) me;
        return terp.newNum(self.width);
      }
    };
    new JavaMeth(((AndyTerp)terp).wrapandy.clsScreen, "height", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        Screen self = (Screen) me;
        return terp.newNum(self.height);
      }
    };
    // CMD meth Screen . newInk: TYPE Ink NAME newInk_ PARAMS {int colorRgbDecimal}
    // CLS Screen GROUP "." METH newInk: REMARK ""

    new JavaMeth(((AndyTerp)terp).wrapandy.clsScreen, "newInk:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Screen self = (Screen) me;
        Num a = (Num) args[0];
        return self.newInk_ (a.toNearestInt());
      }
    };

    // CMD meth Screen "draw" fps TYPE Obj NAME _fps PARAMS 
    // CLS Screen GROUP "draw" METH fps REMARK ""

    new JavaMeth(((AndyTerp)terp).wrapandy.clsScreen, "fps", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Screen self = (Screen) me;
        return self._fps ();
      }
    };

    // CMD meth Screen "draw" post TYPE void NAME post PARAMS 
    // CLS Screen GROUP "draw" METH post REMARK ""

    new JavaMeth(((AndyTerp)terp).wrapandy.clsScreen, "post", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Screen self = (Screen) me;
        self.post ();
        return self;
      }
    };

    // CMD meth Screen "draw" clear: TYPE void NAME clear_ PARAMS {int rgbDecimal}
    // CLS Screen GROUP "draw" METH clear: REMARK ""

    new JavaMeth(((AndyTerp)terp).wrapandy.clsScreen, "clear:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Screen self = (Screen) me;
        Num a = (Num) args[0];
        self.clear_ (a.toNearestInt());
        return self;
      }
    };

    // CMD meth ScreenCls "sys" work:  "stop any running WorkThread and start the block arg." TYPE void NAME work_ PARAMS {Terp terp} { Blk block}
    // CLS ScreenCls GROUP "sys" METH work: REMARK "stop any running WorkThread and start the block arg."

    new JavaMeth(((AndyTerp)terp).wrapandy.clsScreenCls, "work:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
        Cls self = ((AndyTerp)terp).wrapandy.clsScreenCls;
        Blk a = (Blk) args[0];
        Screen.work_ (terp,a);
        return self;
      }
    };

    new JavaMeth(((AndyTerp)terp).wrapandy.clsInk, "scr", "") {
      public Ur apply(Frame f, Ur me, Ur[] args) {
        Ink self = (Ink) me;
        return self.scr;
      }
    };
    // CMD meth Ink "live" fontsize: TYPE void NAME setFontSize PARAMS {int a}
    // CLS Ink GROUP "live" METH fontsize: REMARK ""

    new JavaMeth(((AndyTerp)terp).wrapandy.clsInk, "fontsize:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Ink self = (Ink) me;
        Num a = (Num) args[0];
        self.setFontSize (a.toNearestInt());
        return self;
      }
    };

    // CMD meth Ink "live" thick: TYPE void NAME setThickness PARAMS {int pixels}
    // CLS Ink GROUP "live" METH thick: REMARK ""

    new JavaMeth(((AndyTerp)terp).wrapandy.clsInk, "thick:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Ink self = (Ink) me;
        Num a = (Num) args[0];
        self.setThickness (a.toNearestInt());
        return self;
      }
    };

    // CMD meth Ink "live" color: TYPE void NAME setColor PARAMS {int rgbDecimal}
    // CLS Ink GROUP "live" METH color: REMARK ""

    new JavaMeth(((AndyTerp)terp).wrapandy.clsInk, "color:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Ink self = (Ink) me;
        Num a = (Num) args[0];
        self.setColor (a.toNearestInt());
        return self;
      }
    };

    // CMD meth Ink "draw" line:to: TYPE void NAME line_to_ PARAMS {Vec a} { Vec b}
    // CLS Ink GROUP "draw" METH line:to: REMARK ""

    new JavaMeth(((AndyTerp)terp).wrapandy.clsInk, "line:to:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Ink self = (Ink) me;
        Vec a = (Vec) args[0];
        Vec b = (Vec) args[1];
        self.line_to_ (a, b);
        return self;
      }
    };

    // CMD meth Ink "draw" rect:to: TYPE void NAME rect_to_ PARAMS {Vec a} { Vec b}
    // CLS Ink GROUP "draw" METH rect:to: REMARK ""

    new JavaMeth(((AndyTerp)terp).wrapandy.clsInk, "rect:to:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Ink self = (Ink) me;
        Vec a = (Vec) args[0];
        Vec b = (Vec) args[1];
        self.rect_to_ (a, b);
        return self;
      }
    };

    // CMD meth Ink "draw" circ:r: TYPE void NAME circ_r_ PARAMS {Vec a} { Num b}
    // CLS Ink GROUP "draw" METH circ:r: REMARK ""

    new JavaMeth(((AndyTerp)terp).wrapandy.clsInk, "circ:r:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Ink self = (Ink) me;
        Vec a = (Vec) args[0];
        Num b = (Num) args[1];
        self.circ_r_ (a, b);
        return self;
      }
    };

    // CMD meth Ink "draw" dot: TYPE void NAME dot_ PARAMS {Vec a}
    // CLS Ink GROUP "draw" METH dot: REMARK ""

    new JavaMeth(((AndyTerp)terp).wrapandy.clsInk, "dot:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Ink self = (Ink) me;
        Vec a = (Vec) args[0];
        self.dot_ (a);
        return self;
      }
    };

    // CMD meth Ink "draw" dots: TYPE void NAME dots_ PARAMS {Vec a}
    // CLS Ink GROUP "draw" METH dots: REMARK ""

    new JavaMeth(((AndyTerp)terp).wrapandy.clsInk, "dots:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Ink self = (Ink) me;
        Vec a = (Vec) args[0];
        self.dots_ (a);
        return self;
      }
    };

    // CMD meth Ink "draw" text:sw: TYPE void NAME drawText PARAMS {Str s} { Vec b}
    // CLS Ink GROUP "draw" METH text:sw: REMARK ""

    new JavaMeth(((AndyTerp)terp).wrapandy.clsInk, "text:sw:", "") {
      public Ur apply(Frame frame, Ur me, Ur[] args) {
      Ink self = (Ink) me;
        Str a = (Str) args[0];
        Vec b = (Vec) args[1];
        self.drawText (a, b);
        return self;
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


    
    this.clsMotionCls = terp.clss.get("motioncls");
    if (this.clsMotionCls == null) {
      this.clsMotionCls = new Cls(terp.tMetacls, terp, "MotionCls", this.clsObjCls);
    }
    this.clsMotion      = terp.clss.get("motion");
    if (this.clsMotion == null) {
      this.clsMotion = new Cls(this.clsMotionCls, terp, "Motion", this.clsObj);
    }
            
    this.clsScreenCls = terp.clss.get("screencls");
    if (this.clsScreenCls == null) {
      this.clsScreenCls = new Cls(terp.tMetacls, terp, "ScreenCls", this.clsUsrCls);
    }
    this.clsScreen      = terp.clss.get("screen");
    if (this.clsScreen == null) {
      this.clsScreen = new Cls(this.clsScreenCls, terp, "Screen", this.clsUsr);
    }
            
    this.clsInkCls = terp.clss.get("inkcls");
    if (this.clsInkCls == null) {
      this.clsInkCls = new Cls(terp.tMetacls, terp, "InkCls", this.clsUsrCls);
    }
    this.clsInk      = terp.clss.get("ink");
    if (this.clsInk == null) {
      this.clsInk = new Cls(this.clsInkCls, terp, "Ink", this.clsUsr);
    }
            
  }
  
  public Cls clsMotionCls;
  public Cls clsMotion;
          
  public Cls clsScreenCls;
  public Cls clsScreen;
          
  public Cls clsInkCls;
  public Cls clsInk;
          


  public Cls clsUrCls;
  public Cls clsUr;
  public Cls clsObjCls;
  public Cls clsObj;
  public Cls clsUsrCls;
  public Cls clsUsr;
  

}

