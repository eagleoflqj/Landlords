package com.liumeo.landlords;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;

public final class MessageWrapper {
  //包装信息
    public static final int TIMEOUT = Integer.MAX_VALUE;
    //设置超时时间为3s
    private static final byte[] HEADER = {(byte) 0xff, (byte) 0xfe, (byte) 0xfd, (byte) 0xfc};
    //信息头

    public static void write(OutputStream out, Serializable object) throws IOException {
      //封装并传输信息
        ObjectOutputStream o = new ObjectOutputStream(out);
        o.write(HEADER);
        o.writeObject(object);
    }

    public static Object read(InputStream in) {
      //拆装并读取信息
        try {
            ObjectInputStream i = new ObjectInputStream(in);
            byte[] header = new byte[4];
            int res = i.read(header, 0, 4);
            if (res == 4 && Arrays.equals(header, HEADER))
                return i.readObject();
        } catch (Exception ignored) {
        }
        return null;
    }
}
