import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class al implements Comparable<al> {
   private final ak a;
   private final List<n<Date, Double>> b = new ArrayList();
   private final List<n<Date, Double>> c = new ArrayList();
   private double d = 0.0;
   private double e = 0.0;
   private double f = 0.0;
   private static final String[] g;

   public al(ak var1) {
      this.a = var1;
   }

   protected static long a(Date param0, Date param1, TimeUnit param2) {
      // $FF: Couldn't be decompiled
   }

   protected static <T> T a(List<T> param0) {
      // $FF: Couldn't be decompiled
   }

   protected static <T> T b(List<T> param0) {
      // $FF: Couldn't be decompiled
   }

   public void a(double param1) {
      // $FF: Couldn't be decompiled
   }

   public ak a() {
      return this.a;
   }

   public List<n<Date, Double>> b() {
      return this.b;
   }

   public List<n<Date, Double>> c() {
      return this.c;
   }

   public double d() {
      return this.d;
   }

   public double e() {
      try {
         if (this.b.isEmpty()) {
            return 0.0;
         }
      } catch (a_ var1) {
         throw var1;
      }

      return this.e / (double)this.b.size();
   }

   public double f() {
      try {
         if (this.c.isEmpty()) {
            return 0.0;
         }
      } catch (a_ var1) {
         throw var1;
      }

      return this.f / (double)this.c.size();
   }

   public String toString() {
      StringBuilder var1 = new StringBuilder(this.a.a());

      try {
         var1.append(g[2]);
         var1.append(this.a.b());
         if (this.a.c() != null) {
            var1.append(g[1]);
            var1.append(this.a.c());
            var1.append("]");
         }
      } catch (a_ var2) {
         throw var2;
      }

      var1.append(g[0]);
      var1.append(DecimalFormat.getNumberInstance().format(this.d()));
      var1.append(" ");
      var1.append(DecimalFormat.getNumberInstance().format(this.e()));
      var1.append(" ");
      var1.append(DecimalFormat.getNumberInstance().format(this.f()));
      return var1.toString();
   }

   protected static boolean a(Object param0, Object param1) {
      // $FF: Couldn't be decompiled
   }

   public int a(al var1) {
      try {
         if (var1 == null) {
            return 1;
         }
      } catch (a_ var2) {
         throw var2;
      }

      try {
         if (a(this.a().a(), var1.a().a())) {
            return this.a().b().compareTo(var1.a().b());
         }
      } catch (a_ var3) {
         throw var3;
      }

      return this.a().a().compareTo(var1.a().a());
   }

   static {
      String[] var10000 = new String[3];
      char[] var10003 = "\u0007Y".toCharArray();
      int var10005 = var10003.length;
      int var1 = 0;
      char[] var41 = var10003;
      int var8 = var10005;
      char[] var71;
      int var10006;
      char var10007;
      byte var10008;
      if (var10005 <= 1) {
         var71 = var10003;
         var10006 = var1;
         var10007 = var10003[var1];
         switch (var1 % 5) {
            case 0:
               var10008 = 61;
               break;
            case 1:
               var10008 = 121;
               break;
            case 2:
               var10008 = 103;
               break;
            case 3:
               var10008 = 113;
               break;
            default:
               var10008 = 123;
         }
      } else {
         var41 = var10003;
         var8 = var10005;
         if (var10005 <= var1) {
            label127: {
               var10000[0] = (new String(var10003)).intern();
               char[] var26 = "\u001d\"".toCharArray();
               int var98 = var26.length;
               var1 = 0;
               var41 = var26;
               int var29 = var98;
               char[] var101;
               if (var98 <= 1) {
                  var101 = var26;
                  var10006 = var1;
               } else {
                  var41 = var26;
                  var29 = var98;
                  if (var98 <= var1) {
                     break label127;
                  }

                  var101 = var26;
                  var10006 = var1;
               }

               while(true) {
                  var10007 = var101[var10006];
                  switch (var1 % 5) {
                     case 0:
                        var10008 = 61;
                        break;
                     case 1:
                        var10008 = 121;
                        break;
                     case 2:
                        var10008 = 103;
                        break;
                     case 3:
                        var10008 = 113;
                        break;
                     default:
                        var10008 = 123;
                  }

                  var101[var10006] = (char)(var10007 ^ var10008);
                  ++var1;
                  if (var29 == 0) {
                     var10006 = var29;
                     var101 = var41;
                  } else {
                     if (var29 <= var1) {
                        break;
                     }

                     var101 = var41;
                     var10006 = var1;
                  }
               }
            }

            var10000[1] = (new String(var41)).intern();
            char[] var33 = "\u001dTG".toCharArray();
            int var108 = var33.length;
            var1 = 0;
            var41 = var33;
            int var36 = var108;
            char[] var111;
            if (var108 <= 1) {
               var111 = var33;
               var10006 = var1;
            } else {
               var41 = var33;
               var36 = var108;
               if (var108 <= var1) {
                  var10000[2] = (new String(var33)).intern();
                  g = var10000;
                  return;
               }

               var111 = var33;
               var10006 = var1;
            }

            while(true) {
               var10007 = var111[var10006];
               switch (var1 % 5) {
                  case 0:
                     var10008 = 61;
                     break;
                  case 1:
                     var10008 = 121;
                     break;
                  case 2:
                     var10008 = 103;
                     break;
                  case 3:
                     var10008 = 113;
                     break;
                  default:
                     var10008 = 123;
               }

               var111[var10006] = (char)(var10007 ^ var10008);
               ++var1;
               if (var36 == 0) {
                  var10006 = var36;
                  var111 = var41;
               } else {
                  if (var36 <= var1) {
                     var10000[2] = (new String(var41)).intern();
                     g = var10000;
                     return;
                  }

                  var111 = var41;
                  var10006 = var1;
               }
            }
         }

         var71 = var10003;
         var10006 = var1;
         var10007 = var10003[var1];
         switch (var1 % 5) {
            case 0:
               var10008 = 61;
               break;
            case 1:
               var10008 = 121;
               break;
            case 2:
               var10008 = 103;
               break;
            case 3:
               var10008 = 113;
               break;
            default:
               var10008 = 123;
         }
      }

      while(true) {
         var71[var10006] = (char)(var10007 ^ var10008);
         ++var1;
         if (var8 == 0) {
            var10006 = var8;
            var71 = var41;
            var10007 = var41[var8];
            switch (var1 % 5) {
               case 0:
                  var10008 = 61;
                  break;
               case 1:
                  var10008 = 121;
                  break;
               case 2:
                  var10008 = 103;
                  break;
               case 3:
                  var10008 = 113;
                  break;
               default:
                  var10008 = 123;
            }
         } else {
            if (var8 <= var1) {
               label65: {
                  var10000[0] = (new String(var41)).intern();
                  char[] var12 = "\u001d\"".toCharArray();
                  int var78 = var12.length;
                  var1 = 0;
                  var41 = var12;
                  int var15 = var78;
                  char[] var81;
                  if (var78 <= 1) {
                     var81 = var12;
                     var10006 = var1;
                  } else {
                     var41 = var12;
                     var15 = var78;
                     if (var78 <= var1) {
                        break label65;
                     }

                     var81 = var12;
                     var10006 = var1;
                  }

                  while(true) {
                     var10007 = var81[var10006];
                     switch (var1 % 5) {
                        case 0:
                           var10008 = 61;
                           break;
                        case 1:
                           var10008 = 121;
                           break;
                        case 2:
                           var10008 = 103;
                           break;
                        case 3:
                           var10008 = 113;
                           break;
                        default:
                           var10008 = 123;
                     }

                     var81[var10006] = (char)(var10007 ^ var10008);
                     ++var1;
                     if (var15 == 0) {
                        var10006 = var15;
                        var81 = var41;
                     } else {
                        if (var15 <= var1) {
                           break;
                        }

                        var81 = var41;
                        var10006 = var1;
                     }
                  }
               }

               var10000[1] = (new String(var41)).intern();
               char[] var19 = "\u001dTG".toCharArray();
               int var88 = var19.length;
               var1 = 0;
               var41 = var19;
               int var22 = var88;
               char[] var91;
               if (var88 <= 1) {
                  var91 = var19;
                  var10006 = var1;
               } else {
                  var41 = var19;
                  var22 = var88;
                  if (var88 <= var1) {
                     var10000[2] = (new String(var19)).intern();
                     g = var10000;
                     return;
                  }

                  var91 = var19;
                  var10006 = var1;
               }

               while(true) {
                  var10007 = var91[var10006];
                  switch (var1 % 5) {
                     case 0:
                        var10008 = 61;
                        break;
                     case 1:
                        var10008 = 121;
                        break;
                     case 2:
                        var10008 = 103;
                        break;
                     case 3:
                        var10008 = 113;
                        break;
                     default:
                        var10008 = 123;
                  }

                  var91[var10006] = (char)(var10007 ^ var10008);
                  ++var1;
                  if (var22 == 0) {
                     var10006 = var22;
                     var91 = var41;
                  } else {
                     if (var22 <= var1) {
                        var10000[2] = (new String(var41)).intern();
                        g = var10000;
                        return;
                     }

                     var91 = var41;
                     var10006 = var1;
                  }
               }
            }

            var71 = var41;
            var10006 = var1;
            var10007 = var41[var1];
            switch (var1 % 5) {
               case 0:
                  var10008 = 61;
                  break;
               case 1:
                  var10008 = 121;
                  break;
               case 2:
                  var10008 = 103;
                  break;
               case 3:
                  var10008 = 113;
                  break;
               default:
                  var10008 = 123;
            }
         }
      }
   }
}
