package org.hornetq.api.core;

import org.hornetq.utils.DataConstants;

/**
 * Created by wreicher on 3/17/15.
 */
public class SSU
{

   public static byte[] getData(String string)
   {
      return SSU.getBytesFromString(string);
   }
   public static byte[] getBytesFromString( String string )
   {
      int len = string.length();
      byte[] rtrn = new byte[len << 1];
      int j = 0;
      for ( int i = 0; i < len; i++ )
      {
         char c = string.charAt(i);
         byte low = (byte)(c & 0xFF); // low byte
         rtrn[j++] = low;
         byte high = (byte)(c >> 8 & 0xFF); //high byte
         rtrn[j++] = high;
      }
      return rtrn;
   }

   public static String getStringFromBytes( byte[] bytes )
   {
      int len = bytes.length >> 1;
      char[] chars = new char[len];
      int j = 0;
      for ( int i = 0; i < len; i++ )
      {
         int low = bytes[j++] & 0xFF;
         int high = bytes[j++] << 8 & 0xFF00;
         chars[i] = (char)(low | high);
      }
      String rtrn = new String(chars);
      return rtrn;
   }
   public static int sizeof( String str )
   {
      if ( str == null )
         return 1;
      return DataConstants.SIZE_INT + 2 * str.length();
   }

}
