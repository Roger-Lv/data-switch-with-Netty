package org.bdware.sw;

import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.zz.gmhelper.SM3Util;

public class SM3Tool {
    public static String toSM3(String str) {
        return ByteUtils.toHexString(SM3Util.hash(str.getBytes()));
    }
}
