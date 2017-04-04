package com.rafo.hall.utils;

import java.io.*;

/**
 * Created by YL.
 * Date: 16-10-6
 */
public final class SerializeUtil
{
    /**
     * 序列化对象
     *
     * @return
     * @throws Exception
     */
    public static byte[] serializeObject(Object object) throws IOException
    {
        ByteArrayOutputStream aos = new ByteArrayOutputStream();

        ObjectOutputStream oos = new ObjectOutputStream(aos);


        oos.writeObject(object);

        oos.flush();

        return aos.toByteArray();

    }

    /**
     * 反序列化对象
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */

    public static Object deserializeObject(byte[] buf) throws IOException, ClassNotFoundException
    {

        Object object;

        ByteArrayInputStream ais = new ByteArrayInputStream(buf);

        ObjectInputStream ois = new ObjectInputStream(ais);

        object = ois.readObject();

        return object;

    }
}