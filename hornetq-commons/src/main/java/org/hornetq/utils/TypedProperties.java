/*
 * Copyright 2009 Red Hat, Inc.
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.hornetq.utils;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hornetq.api.core.HornetQBuffer;
import org.hornetq.api.core.HornetQPropertyConversionException;
import org.hornetq.api.core.SSU;


import static org.hornetq.utils.DataConstants.BOOLEAN;
import static org.hornetq.utils.DataConstants.BYTE;
import static org.hornetq.utils.DataConstants.BYTES;
import static org.hornetq.utils.DataConstants.CHAR;
import static org.hornetq.utils.DataConstants.DOUBLE;
import static org.hornetq.utils.DataConstants.FLOAT;
import static org.hornetq.utils.DataConstants.INT;
import static org.hornetq.utils.DataConstants.LONG;
import static org.hornetq.utils.DataConstants.NULL;
import static org.hornetq.utils.DataConstants.SHORT;
import static org.hornetq.utils.DataConstants.STRING;

/**
 * Property Value Conversion.
 * <p/>
 * This implementation follows section 3.5.4 of the <i>Java Message Service<i> specification
 * (Version 1.1 April 12, 2002).
 * <p/>
 * TODO - should have typed property getters and do conversions herein
 *
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * @author <a href="mailto:clebert.suconic@jboss.com">Clebert Suconic</a>
 */
public final class TypedProperties
{

   private static final String HQ_PROPNAME = new String("_HQ_");

   private Map<String, PropertyValue> properties;

   private volatile int size;

   private boolean internalProperties;

   public TypedProperties()
   {
   }

   public int getMemoryOffset()
   {
      // The estimate is basically the encode size + 2 object references for each entry in the map
      // Note we don't include the attributes or anything else since they already included in the memory estimate
      // of the ServerMessage

      return properties == null ? 0 : size + 2 * DataConstants.SIZE_INT * properties.size();
   }

   public TypedProperties(final TypedProperties other)
   {
      properties = other.properties == null ? null : new HashMap<String, PropertyValue>(other.properties);
      size = other.size;
   }

   public boolean hasInternalProperties()
   {
      return internalProperties;
   }

   public void putBooleanProperty(final String key, final boolean value)
   {
      checkCreateProperties();
      doPutValue(key, new BooleanValue(value));
   }

   public void putByteProperty(final String key, final byte value)
   {
      checkCreateProperties();
      doPutValue(key, new ByteValue(value));
   }

   public void putBytesProperty(final String key, final byte[] value)
   {
      checkCreateProperties();
      doPutValue(key, value == null ? new NullValue() : new BytesValue(value));
   }

   public void putShortProperty(final String key, final short value)
   {
      checkCreateProperties();
      doPutValue(key, new ShortValue(value));
   }

   public void putIntProperty(final String key, final int value)
   {
      checkCreateProperties();
      doPutValue(key, new IntValue(value));
   }

   public void putLongProperty(final String key, final long value)
   {
      checkCreateProperties();
      doPutValue(key, new LongValue(value));
   }

   public void putFloatProperty(final String key, final float value)
   {
      checkCreateProperties();
      doPutValue(key, new FloatValue(value));
   }

   public void putDoubleProperty(final String key, final double value)
   {
      checkCreateProperties();
      doPutValue(key, new DoubleValue(value));
   }

   public void putStringProperty(final String key, final String value)
   {
      checkCreateProperties();
      doPutValue(key, value == null ? new NullValue() : new StringValue(value));
   }

   public void putNullValue(final String key)
   {
      checkCreateProperties();
      doPutValue(key, new NullValue());
   }

   public void putCharProperty(final String key, final char value)
   {
      checkCreateProperties();
      doPutValue(key, new CharValue(value));
   }

   public void putTypedProperties(final TypedProperties otherProps)
   {
      if (otherProps == null || otherProps.properties == null)
      {
         return;
      }

      checkCreateProperties();
      Set<Entry<String, PropertyValue>> otherEntries = otherProps.properties.entrySet();
      for (Entry<String, PropertyValue> otherEntry : otherEntries)
      {
         doPutValue(otherEntry.getKey(), otherEntry.getValue());
      }
   }

   public Object getProperty(final String key)
   {
      return doGetProperty(key);
   }

   public Boolean getBooleanProperty(final String key) throws HornetQPropertyConversionException
   {
      Object value = doGetProperty(key);
      if (value == null)
      {
         return Boolean.valueOf(null);
      }
      else if (value instanceof Boolean)
      {
         return (Boolean)value;
      }
      else if (value instanceof String)
      {
         return Boolean.valueOf(((String)value).toString());
      }
      else
      {
         throw new HornetQPropertyConversionException("Invalid conversion");
      }
   }

   public Byte getByteProperty(final String key) throws HornetQPropertyConversionException
   {
      Object value = doGetProperty(key);
      if (value == null)
      {
         return Byte.valueOf(null);
      }
      else if (value instanceof Byte)
      {
         return (Byte)value;
      }
      else if (value instanceof String)
      {
         return Byte.parseByte(((String)value).toString());
      }
      else
      {
         throw new HornetQPropertyConversionException("Invalid conversion");
      }
   }

   public Character getCharProperty(final String key) throws HornetQPropertyConversionException
   {
      Object value = doGetProperty(key);
      if (value == null)
      {
         throw new NullPointerException("Invalid conversion");
      }

      if (value instanceof Character)
      {
         return ((Character)value);
      }
      else
      {
         throw new HornetQPropertyConversionException("Invalid conversion");
      }
   }

   public byte[] getBytesProperty(final String key) throws HornetQPropertyConversionException
   {
      Object value = doGetProperty(key);
      if (value == null)
      {
         return null;
      }
      else if (value instanceof byte[])
      {
         return (byte[])value;
      }
      else
      {
         throw new HornetQPropertyConversionException("Invalid conversion");
      }
   }

   public Double getDoubleProperty(final String key) throws HornetQPropertyConversionException
   {
      Object value = doGetProperty(key);
      if (value == null)
      {
         return Double.valueOf(null);
      }
      else if (value instanceof Float)
      {
         return ((Float)value).doubleValue();
      }
      else if (value instanceof Double)
      {
         return (Double)value;
      }
      else if (value instanceof String)
      {
         return Double.parseDouble(((String)value).toString());
      }
      else
      {
         throw new HornetQPropertyConversionException("Invalid conversion");
      }
   }

   public Integer getIntProperty(final String key) throws HornetQPropertyConversionException
   {
      Object value = doGetProperty(key);
      if (value == null)
      {
         return Integer.valueOf(null);
      }
      else if (value instanceof Integer)
      {
         return (Integer)value;
      }
      else if (value instanceof Byte)
      {
         return ((Byte)value).intValue();
      }
      else if (value instanceof Short)
      {
         return ((Short)value).intValue();
      }
      else if (value instanceof String)
      {
         return Integer.parseInt(((String)value).toString());
      }
      else
      {
         throw new HornetQPropertyConversionException("Invalid conversion");
      }
   }

   public Long getLongProperty(final String key) throws HornetQPropertyConversionException
   {
      Object value = doGetProperty(key);
      if (value == null)
      {
         return Long.valueOf(null);
      }
      else if (value instanceof Long)
      {
         return (Long)value;
      }
      else if (value instanceof Byte)
      {
         return ((Byte)value).longValue();
      }
      else if (value instanceof Short)
      {
         return ((Short)value).longValue();
      }
      else if (value instanceof Integer)
      {
         return ((Integer)value).longValue();
      }
      else if (value instanceof String)
      {
         return Long.parseLong(((String)value).toString());
      }
      else
      {
         throw new HornetQPropertyConversionException("Invalid conversion");
      }
   }

   public Short getShortProperty(final String key) throws HornetQPropertyConversionException
   {
      Object value = doGetProperty(key);
      if (value == null)
      {
         return Short.valueOf(null);
      }
      else if (value instanceof Byte)
      {
         return ((Byte)value).shortValue();
      }
      else if (value instanceof Short)
      {
         return (Short)value;
      }
      else if (value instanceof String)
      {
         return Short.parseShort(((String)value).toString());
      }
      else
      {
         throw new HornetQPropertyConversionException("Invalid Conversion.");
      }
   }

   public Float getFloatProperty(final String key) throws HornetQPropertyConversionException
   {
      Object value = doGetProperty(key);
      if (value == null)
         return Float.valueOf(null);
      if (value instanceof Float)
      {
         return ((Float)value);
      }
      if (value instanceof String)
      {
         return Float.parseFloat(((String)value).toString());
      }
      throw new HornetQPropertyConversionException("Invalid conversion: " + key);
   }

   public String getStringProperty(final String key) throws HornetQPropertyConversionException
   {
      Object value = doGetProperty(key);

      if (value == null)
      {
         return null;
      }

      if (value instanceof String)
      {
         return (String)value;
      }
      else if (value instanceof Boolean)
      {
         return new String(value.toString());
      }
      else if (value instanceof Character)
      {
         return new String(value.toString());
      }
      else if (value instanceof Byte)
      {
         return new String(value.toString());
      }
      else if (value instanceof Short)
      {
         return new String(value.toString());
      }
      else if (value instanceof Integer)
      {
         return new String(value.toString());
      }
      else if (value instanceof Long)
      {
         return new String(value.toString());
      }
      else if (value instanceof Float)
      {
         return new String(value.toString());
      }
      else if (value instanceof Double)
      {
         return new String(value.toString());
      }
      throw new HornetQPropertyConversionException("Invalid conversion");
   }

   public Object removeProperty(final String key)
   {
      return doRemoveProperty(key);
   }

   public boolean containsProperty(final String key)
   {
      if (size == 0)
      {
         return false;

      }
      else
      {
         return properties.containsKey(key);
      }
   }

   public Set<String> getPropertyNames()
   {
      if (size == 0)
      {
         return Collections.emptySet();
      }
      else
      {
         return properties.keySet();
      }
   }

   public synchronized void decode(final HornetQBuffer buffer)
   {
      byte b = buffer.readByte();

      if (b == DataConstants.NULL)
      {
         properties = null;
      }
      else
      {
         int numHeaders = buffer.readInt();

         properties = new HashMap<String, PropertyValue>(numHeaders);
         size = 0;

         for (int i = 0; i < numHeaders; i++)
         {
            int len = buffer.readInt();
            byte[] data = new byte[len];
            buffer.readBytes(data);
            String key = SSU.getStringFromBytes(data);

            byte type = buffer.readByte();

            PropertyValue val;

            switch (type)
            {
               case NULL:
               {
                  val = new NullValue();
                  doPutValue(key, val);
                  break;
               }
               case CHAR:
               {
                  val = new CharValue(buffer);
                  doPutValue(key, val);
                  break;
               }
               case BOOLEAN:
               {
                  val = new BooleanValue(buffer);
                  doPutValue(key, val);
                  break;
               }
               case BYTE:
               {
                  val = new ByteValue(buffer);
                  doPutValue(key, val);
                  break;
               }
               case BYTES:
               {
                  val = new BytesValue(buffer);
                  doPutValue(key, val);
                  break;
               }
               case SHORT:
               {
                  val = new ShortValue(buffer);
                  doPutValue(key, val);
                  break;
               }
               case INT:
               {
                  val = new IntValue(buffer);
                  doPutValue(key, val);
                  break;
               }
               case LONG:
               {
                  val = new LongValue(buffer);
                  doPutValue(key, val);
                  break;
               }
               case FLOAT:
               {
                  val = new FloatValue(buffer);
                  doPutValue(key, val);
                  break;
               }
               case DOUBLE:
               {
                  val = new DoubleValue(buffer);
                  doPutValue(key, val);
                  break;
               }
               case STRING:
               {
                  val = new StringValue(buffer);
                  doPutValue(key, val);
                  break;
               }
               default:
               {
                  throw HornetQUtilBundle.BUNDLE.invalidType(type);
               }
            }
         }
      }
   }

   public synchronized void encode(final HornetQBuffer buffer)
   {
      if (properties == null)
      {
         buffer.writeByte(DataConstants.NULL);
      }
      else
      {
         buffer.writeByte(DataConstants.NOT_NULL);

         buffer.writeInt(properties.size());

         for (Map.Entry<String, PropertyValue> entry : properties.entrySet())
         {
            String s = entry.getKey();
            byte[] data = SSU.getBytesFromString(s);
            buffer.writeInt(data.length);
            buffer.writeBytes(data);

            entry.getValue().write(buffer);
         }
      }
   }

   public int getEncodeSize()
   {
      if (properties == null)
      {
         return DataConstants.SIZE_BYTE;
      }
      else
      {
         return DataConstants.SIZE_BYTE + DataConstants.SIZE_INT + size;
      }
   }

   public void clear()
   {
      if (properties != null)
      {
         properties.clear();
      }
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder("TypedProperties[");


      if (properties != null)
      {

         Iterator<Entry<String, PropertyValue>> iter = properties.entrySet().iterator();

         while (iter.hasNext())
         {
            Entry<String, PropertyValue> iterItem = iter.next();
            sb.append(iterItem.getKey() + "=");

            // it seems weird but it's right!!
            // The first getValue is from the EntrySet
            // The second is to convert the PropertyValue into the actual value
            Object theValue = iterItem.getValue().getValue();


            if (theValue == null)
            {
               sb.append("NULL-value");
            }
            else if (theValue instanceof byte[])
            {
               sb.append("[" + ByteUtil.maxString(ByteUtil.bytesToHex((byte [])theValue, 2), 150) + ")");

               if (iterItem.getKey().toString().startsWith("_HQ_ROUTE_TO"))
               {
                  sb.append(",bytesAsLongs(");
                  try
                  {
                     ByteBuffer buff = ByteBuffer.wrap((byte[]) theValue);
                     while (buff.hasRemaining())
                     {
                        long bindingID = buff.getLong();
                        sb.append(bindingID);
                        if (buff.hasRemaining())
                        {
                           sb.append(",");
                        }
                     }
                  }
                  catch (Throwable e)
                  {
                     sb.append("error-converting-longs=" + e.getMessage());
                  }
                  sb.append("]");
               }
            }
            else
            {
               sb.append(theValue.toString());
            }


            if (iter.hasNext())
            {
               sb.append(",");
            }
         }
      }

      return sb.append("]").toString();
   }

   // Private ------------------------------------------------------------------------------------

   private void checkCreateProperties()
   {
      if (properties == null)
      {
         properties = new HashMap<String, PropertyValue>();
      }
   }

   private synchronized void doPutValue(final String key, final PropertyValue value)
   {
      if (key.startsWith(HQ_PROPNAME))
      {
         internalProperties = true;
      }

      PropertyValue oldValue = properties.put(key, value);
      if (oldValue != null)
      {
         size += value.encodeSize() - oldValue.encodeSize();
      }
      else
      {
         size += SSU.sizeof(key) + value.encodeSize();
      }
   }

   private synchronized Object doRemoveProperty(final String key)
   {
      if (properties == null)
      {
         return null;
      }

      PropertyValue val = properties.remove(key);

      if (val == null)
      {
         return null;
      }
      else
      {
         size -= SSU.sizeof(key) + val.encodeSize();

         return val.getValue();
      }
   }

   private synchronized Object doGetProperty(final Object key)
   {
      if (size == 0)
      {
         return null;
      }

      PropertyValue val = properties.get(key);

      if (val == null)
      {
         return null;
      }
      else
      {
         return val.getValue();
      }
   }

   // Inner classes ------------------------------------------------------------------------------

   private abstract static class PropertyValue
   {
      abstract Object getValue();

      abstract void write(HornetQBuffer buffer);

      abstract int encodeSize();

      @Override
      public String toString()
      {
         return "" + getValue();
      }
   }

   private static final class NullValue extends PropertyValue
   {
      public NullValue()
      {
      }

      @Override
      public Object getValue()
      {
         return null;
      }

      @Override
      public void write(final HornetQBuffer buffer)
      {
         buffer.writeByte(DataConstants.NULL);
      }

      @Override
      public int encodeSize()
      {
         return DataConstants.SIZE_BYTE;
      }

   }

   private static final class BooleanValue extends PropertyValue
   {
      final boolean val;

      public BooleanValue(final boolean val)
      {
         this.val = val;
      }

      public BooleanValue(final HornetQBuffer buffer)
      {
         val = buffer.readBoolean();
      }

      @Override
      public Object getValue()
      {
         return val;
      }

      @Override
      public void write(final HornetQBuffer buffer)
      {
         buffer.writeByte(DataConstants.BOOLEAN);
         buffer.writeBoolean(val);
      }

      @Override
      public int encodeSize()
      {
         return DataConstants.SIZE_BYTE + DataConstants.SIZE_BOOLEAN;
      }

   }

   private static final class ByteValue extends PropertyValue
   {
      final byte val;

      public ByteValue(final byte val)
      {
         this.val = val;
      }

      public ByteValue(final HornetQBuffer buffer)
      {
         val = buffer.readByte();
      }

      @Override
      public Object getValue()
      {
         return val;
      }

      @Override
      public void write(final HornetQBuffer buffer)
      {
         buffer.writeByte(DataConstants.BYTE);
         buffer.writeByte(val);
      }

      @Override
      public int encodeSize()
      {
         return DataConstants.SIZE_BYTE + DataConstants.SIZE_BYTE;
      }
   }

   private static final class BytesValue extends PropertyValue
   {
      final byte[] val;

      public BytesValue(final byte[] val)
      {
         this.val = val;
      }

      public BytesValue(final HornetQBuffer buffer)
      {
         int len = buffer.readInt();
         val = new byte[len];
         buffer.readBytes(val);
      }

      @Override
      public Object getValue()
      {
         return val;
      }

      @Override
      public void write(final HornetQBuffer buffer)
      {
         buffer.writeByte(DataConstants.BYTES);
         buffer.writeInt(val.length);
         buffer.writeBytes(val);
      }

      @Override
      public int encodeSize()
      {
         return DataConstants.SIZE_BYTE + DataConstants.SIZE_INT + val.length;
      }

   }

   private static final class ShortValue extends PropertyValue
   {
      final short val;

      public ShortValue(final short val)
      {
         this.val = val;
      }

      public ShortValue(final HornetQBuffer buffer)
      {
         val = buffer.readShort();
      }

      @Override
      public Object getValue()
      {
         return val;
      }

      @Override
      public void write(final HornetQBuffer buffer)
      {
         buffer.writeByte(DataConstants.SHORT);
         buffer.writeShort(val);
      }

      @Override
      public int encodeSize()
      {
         return DataConstants.SIZE_BYTE + DataConstants.SIZE_SHORT;
      }
   }

   private static final class IntValue extends PropertyValue
   {
      final int val;

      public IntValue(final int val)
      {
         this.val = val;
      }

      public IntValue(final HornetQBuffer buffer)
      {
         val = buffer.readInt();
      }

      @Override
      public Object getValue()
      {
         return val;
      }

      @Override
      public void write(final HornetQBuffer buffer)
      {
         buffer.writeByte(DataConstants.INT);
         buffer.writeInt(val);
      }

      @Override
      public int encodeSize()
      {
         return DataConstants.SIZE_BYTE + DataConstants.SIZE_INT;
      }
   }

   private static final class LongValue extends PropertyValue
   {
      final long val;

      public LongValue(final long val)
      {
         this.val = val;
      }

      public LongValue(final HornetQBuffer buffer)
      {
         val = buffer.readLong();
      }

      @Override
      public Object getValue()
      {
         return val;
      }

      @Override
      public void write(final HornetQBuffer buffer)
      {
         buffer.writeByte(DataConstants.LONG);
         buffer.writeLong(val);
      }

      @Override
      public int encodeSize()
      {
         return DataConstants.SIZE_BYTE + DataConstants.SIZE_LONG;
      }
   }

   private static final class FloatValue extends PropertyValue
   {
      final float val;

      public FloatValue(final float val)
      {
         this.val = val;
      }

      public FloatValue(final HornetQBuffer buffer)
      {
         val = Float.intBitsToFloat(buffer.readInt());
      }

      @Override
      public Object getValue()
      {
         return val;
      }

      @Override
      public void write(final HornetQBuffer buffer)
      {
         buffer.writeByte(DataConstants.FLOAT);
         buffer.writeInt(Float.floatToIntBits(val));
      }

      @Override
      public int encodeSize()
      {
         return DataConstants.SIZE_BYTE + DataConstants.SIZE_FLOAT;
      }

   }

   private static final class DoubleValue extends PropertyValue
   {
      final double val;

      public DoubleValue(final double val)
      {
         this.val = val;
      }

      public DoubleValue(final HornetQBuffer buffer)
      {
         val = Double.longBitsToDouble(buffer.readLong());
      }

      @Override
      public Object getValue()
      {
         return val;
      }

      @Override
      public void write(final HornetQBuffer buffer)
      {
         buffer.writeByte(DataConstants.DOUBLE);
         buffer.writeLong(Double.doubleToLongBits(val));
      }

      @Override
      public int encodeSize()
      {
         return DataConstants.SIZE_BYTE + DataConstants.SIZE_DOUBLE;
      }
   }

   private static final class CharValue extends PropertyValue
   {
      final char val;

      public CharValue(final char val)
      {
         this.val = val;
      }

      public CharValue(final HornetQBuffer buffer)
      {
         val = (char)buffer.readShort();
      }

      @Override
      public Object getValue()
      {
         return val;
      }

      @Override
      public void write(final HornetQBuffer buffer)
      {
         buffer.writeByte(DataConstants.CHAR);
         buffer.writeShort((short)val);
      }

      @Override
      public int encodeSize()
      {
         return DataConstants.SIZE_BYTE + DataConstants.SIZE_CHAR;
      }
   }

   private static final class StringValue extends PropertyValue
   {
      final String val;

      public StringValue(final String val)
      {
         this.val = val;
      }

      public StringValue(final HornetQBuffer buffer)
      {
         val = buffer.readString().toString();
      }

      @Override
      public Object getValue()
      {
         return val;
      }

      @Override
      public void write(final HornetQBuffer buffer)
      {
         buffer.writeByte(DataConstants.STRING);

         buffer.writeString(val);
      }

      @Override
      public int encodeSize()
      {
         return DataConstants.SIZE_BYTE + SSU.sizeof(val);
      }
   }

   public Map<String, Object> getMap()
   {
      Map<String, Object> m = new HashMap<String, Object>();
      for (Entry<String, PropertyValue> entry : properties.entrySet())
      {
         Object val = entry.getValue().getValue();
         if (val instanceof String)
         {
            m.put(entry.getKey().toString(), ((String)val).toString());
         }
         else
         {
            m.put(entry.getKey().toString(), val);
         }
      }
      return m;
   }

   /**
    * Helper for {link MapMessage#setObjectProperty(String, Object)}
    *
    * @param key
    * @param value
    * @param properties
    */
   public static void setObjectProperty(final String key, final Object value,
                                       final TypedProperties properties)
   {
      if (value == null)
      {
         properties.putNullValue(key);
      }
      else if (value instanceof Boolean)
      {
         properties.putBooleanProperty(key, (Boolean)value);
      }
      else if (value instanceof Byte)
      {
         properties.putByteProperty(key, (Byte)value);
      }
      else if (value instanceof Character)
      {
         properties.putCharProperty(key, (Character)value);
      }
      else if (value instanceof Short)
      {
         properties.putShortProperty(key, (Short)value);
      }
      else if (value instanceof Integer)
      {
         properties.putIntProperty(key, (Integer)value);
      }
      else if (value instanceof Long)
      {
         properties.putLongProperty(key, (Long)value);
      }
      else if (value instanceof Float)
      {
         properties.putFloatProperty(key, (Float)value);
      }
      else if (value instanceof Double)
      {
         properties.putDoubleProperty(key, (Double)value);
      }
//      else if (value instanceof String)
//      {
//         properties.putStringProperty(key, new String((String)value));
//      }
      else if (value instanceof String)
      {
         properties.putStringProperty(key, (String) value);
      }
      else if (value instanceof byte[])
      {
         properties.putBytesProperty(key, (byte[])value);
      }
      else
      {
         throw new HornetQPropertyConversionException(value.getClass() + " is not a valid property type");
      }
   }
}
