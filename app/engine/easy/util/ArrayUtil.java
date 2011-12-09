package engine.easy.util;

/**
 * This is a ArrayUtil class which provide utility methods to convert bytes into int arrays.
 * And also convert the ArrayList into byte array.
 * 
 * Author: Adnan Urooj
 * 
 */

import java.io.*;
import java.util.ArrayList;

public class ArrayUtil
{

    public ArrayUtil()
    {
    }

	/** 
     * Returns the int array from the given bytes array.
     *
     * @return the converted int array
     * @throws IOException if the inputstream through an exception,
     */
    public static int[] toInts(byte bytes[]) throws IOException
    {
        if(bytes == null)
            return null;
        if(bytes.length == 0)
            return new int[0];
        int size = bytes.length / 4;
        int q[] = new int[size];
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
        for(int i = 0; i < size; i++)
            q[i] = dis.readInt();

        return q;
    }

	/** 
     * Returns the int array for the specified window inside the bytes array.
     *
     * @return the converted int array of given window.
     * @throws IOException if the inputstream through an exception,
     */
    public static int[] toInts(byte bytes[], int start, int end) throws IOException
    {
        if(bytes == null)
            return null;
        if(bytes.length == 0)
            return new int[0];
        int size = (end - start) + 1;
        int q[] = new int[size];
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
        dis.skipBytes(start * 4);
        for(int i = 0; i < size; i++)
            q[i] = dis.readInt();

        return q;
    }

	/** 
     * Returns the byte array from the given int array.
     *
     * @return the converted byte array
     * @throws IOException if the inputstream through an exception,
     */
    public static byte[] toBytes(int ints[]) throws IOException
    {
        if(ints == null)
            return null;
        if(ints.length == 0)
            return new byte[0];
        int size = 4 * ints.length;
        ByteArrayOutputStream output = new ByteArrayOutputStream(size);
        DataOutputStream dos = new DataOutputStream(output);
        for(int i = 0; i < ints.length; i++)
            dos.writeInt(ints[i]);

        byte results[] = output.toByteArray();
        return results;
    }

	/** 
     * Returns the byte array from the given arraylist.
     *
     * @return the converted byte array of given arraylist.
     * @throws IOException if the inputstream through an exception,
     */
    public static byte[] toBytes(ArrayList ints) throws IOException
    {
        if(ints == null)
            return null;
        if(ints.size() == 0)
            return new byte[0];
        int size = 4 * ints.size();
        ByteArrayOutputStream output = new ByteArrayOutputStream(size);
        DataOutputStream dos = new DataOutputStream(output);
        for(int i = 0; i < ints.size(); i++)
            dos.writeInt(((Integer)ints.get(i)).intValue());

        byte results[] = output.toByteArray();
        return results;
    }
}