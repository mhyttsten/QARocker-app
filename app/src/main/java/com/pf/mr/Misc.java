package com.pf.mr;

import java.lang.reflect.Field;

/**
 * Created by magnushyttsten on 3/25/16.
 */
public class Misc {


    public static String toString(Object o) throws Exception {
        IndentWriter iw = new IndentWriter();
        toStringObject(iw, o);
        return iw.toString();
    }

    public static void toStringObject(IndentWriter iw, Object o) throws Exception {
        Class c = o.getClass();
        iw.setIndentChar('.');
        iw.println("[" + o.getClass().getSimpleName() + "] {");
        iw.push();

        Field[] fields = c.getFields();
        if (fields != null) {
            for (Field f: fields) {
                iw.print(f.getName() + ": ");
                Class ctype = f.getType();
                if (ctype.equals(Boolean.class)) {
                    iw.println(String.valueOf(f.getBoolean(o)));
                } else if (ctype.equals(Byte.class)) {
                    iw.println(String.valueOf(f.getByte(o)));
                } else if (ctype.equals(Double.class)) {
                    iw.println(String.valueOf(f.getDouble(o)));
                } else if (ctype.equals(Float.class)) {
                    iw.println(String.valueOf(f.getFloat(o)));
                } else if (ctype.equals(Integer.class)) {
                    iw.println(String.valueOf(f.getInt(o)));
                } else if (ctype.equals(Long.class)) {
                    iw.println(String.valueOf(f.getLong(o)));
                } else if (ctype.equals(Short.class)) {
                    iw.println(String.valueOf(f.getShort(o)));
                } else {
                    toStringObject(iw, o);
                }
            }
            iw.println("");
        }

        iw.pop();
        iw.println("}");
   }
}


