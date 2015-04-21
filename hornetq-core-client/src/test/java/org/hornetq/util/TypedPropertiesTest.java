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

package org.hornetq.util;
import org.junit.Before;
import org.junit.After;

import org.junit.Test;
import java.util.Iterator;

import org.junit.Assert;


import org.hornetq.api.core.HornetQBuffer;
import org.hornetq.api.core.HornetQBuffers;

import org.hornetq.tests.util.RandomUtil;
import org.hornetq.tests.CoreUnitTestCase;
import org.hornetq.utils.TypedProperties;

/**
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>
 */
public class TypedPropertiesTest extends Assert
{

   private static void assertEqualsTypeProperties(final TypedProperties expected, final TypedProperties actual)
   {
      Assert.assertNotNull(expected);
      Assert.assertNotNull(actual);
      Assert.assertEquals(expected.getEncodeSize(), actual.getEncodeSize());
      Assert.assertEquals(expected.getPropertyNames(), actual.getPropertyNames());
      Iterator<String> iterator = actual.getPropertyNames().iterator();
      while (iterator.hasNext())
      {
         String key = iterator.next();
         Object expectedValue = expected.getProperty(key);
         Object actualValue = actual.getProperty(key);
         if (expectedValue instanceof byte[] && actualValue instanceof byte[])
         {
            byte[] expectedBytes = (byte[])expectedValue;
            byte[] actualBytes = (byte[])actualValue;
            CoreUnitTestCase.assertEqualsByteArrays(expectedBytes, actualBytes);
         }
         else
         {
            Assert.assertEquals(expectedValue, actualValue);
         }
      }
   }

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   private TypedProperties props;

   private String key;

   @Test
   public void testCopyContructor() throws Exception
   {
      props.putStringProperty(key, RandomUtil.randomString());

      TypedProperties copy = new TypedProperties(props);

      Assert.assertEquals(props.getEncodeSize(), copy.getEncodeSize());
      Assert.assertEquals(props.getPropertyNames(), copy.getPropertyNames());

      Assert.assertTrue(copy.containsProperty(key));
      Assert.assertEquals(props.getProperty(key), copy.getProperty(key));
   }

   @Test
   public void testRemove() throws Exception
   {
      props.putStringProperty(key, RandomUtil.randomString());

      Assert.assertTrue(props.containsProperty(key));
      Assert.assertNotNull(props.getProperty(key));

      props.removeProperty(key);

      Assert.assertFalse(props.containsProperty(key));
      Assert.assertNull(props.getProperty(key));
   }

   @Test
   public void testClear() throws Exception
   {
      props.putStringProperty(key, RandomUtil.randomString());

      Assert.assertTrue(props.containsProperty(key));
      Assert.assertNotNull(props.getProperty(key));

      props.clear();

      Assert.assertFalse(props.containsProperty(key));
      Assert.assertNull(props.getProperty(key));
   }

   @Test
   public void testKey() throws Exception
   {
      props.putBooleanProperty(key, true);
      boolean bool = (Boolean)props.getProperty(key);
      Assert.assertEquals(true, bool);

      props.putCharProperty(key, 'a');
      char c = (Character)props.getProperty(key);
      Assert.assertEquals('a', c);
   }

   @Test
   public void testGetPropertyOnEmptyProperties() throws Exception
   {
      Assert.assertFalse(props.containsProperty(key));
      Assert.assertNull(props.getProperty(key));
   }

   @Test
   public void testRemovePropertyOnEmptyProperties() throws Exception
   {
      Assert.assertFalse(props.containsProperty(key));
      Assert.assertNull(props.removeProperty(key));
   }

   @Test
   public void testNullProperty() throws Exception
   {
      props.putStringProperty(key, null);
      Assert.assertTrue(props.containsProperty(key));
      Assert.assertNull(props.getProperty(key));
   }

   @Test
   public void testBytesPropertyWithNull() throws Exception
   {
      props.putBytesProperty(key, null);

      Assert.assertTrue(props.containsProperty(key));
      byte[] bb = (byte[])props.getProperty(key);
      Assert.assertNull(bb);
   }

   @Test
   public void testTypedProperties() throws Exception
   {
      String longKey = RandomUtil.randomString();
      long longValue = RandomUtil.randomLong();
      String StringKey = RandomUtil.randomString();
      String StringValue = RandomUtil.randomString();
      TypedProperties otherProps = new TypedProperties();
      otherProps.putLongProperty(longKey, longValue);
      otherProps.putStringProperty(StringKey, StringValue);

      props.putTypedProperties(otherProps);

      long ll = props.getLongProperty(longKey);
      Assert.assertEquals(longValue, ll);
      String ss = props.getStringProperty(StringKey);
      Assert.assertEquals(StringValue, ss);
   }

   @Test
   public void testEmptyTypedProperties() throws Exception
   {
      Assert.assertEquals(0, props.getPropertyNames().size());

      props.putTypedProperties(new TypedProperties());

      Assert.assertEquals(0, props.getPropertyNames().size());
   }

   @Test
   public void testNullTypedProperties() throws Exception
   {
      Assert.assertEquals(0, props.getPropertyNames().size());

      props.putTypedProperties(null);

      Assert.assertEquals(0, props.getPropertyNames().size());
   }

   @Test
   public void testEncodeDecode() throws Exception
   {
      props.putByteProperty(RandomUtil.randomString(), RandomUtil.randomByte());
      props.putBytesProperty(RandomUtil.randomString(), RandomUtil.randomBytes());
      props.putBytesProperty(RandomUtil.randomString(), null);
      props.putBooleanProperty(RandomUtil.randomString(), RandomUtil.randomBoolean());
      props.putShortProperty(RandomUtil.randomString(), RandomUtil.randomShort());
      props.putIntProperty(RandomUtil.randomString(), RandomUtil.randomInt());
      props.putLongProperty(RandomUtil.randomString(), RandomUtil.randomLong());
      props.putFloatProperty(RandomUtil.randomString(), RandomUtil.randomFloat());
      props.putDoubleProperty(RandomUtil.randomString(), RandomUtil.randomDouble());
      props.putCharProperty(RandomUtil.randomString(), RandomUtil.randomChar());
      props.putStringProperty(RandomUtil.randomString(), RandomUtil.randomString());
      props.putStringProperty(RandomUtil.randomString(), null);
      String keyToRemove = RandomUtil.randomString();
      props.putStringProperty(keyToRemove, RandomUtil.randomString());

      HornetQBuffer buffer = HornetQBuffers.dynamicBuffer(1024);
      props.encode(buffer);

      Assert.assertEquals(props.getEncodeSize(), buffer.writerIndex());

      TypedProperties decodedProps = new TypedProperties();
      decodedProps.decode(buffer);

      TypedPropertiesTest.assertEqualsTypeProperties(props, decodedProps);

      buffer.clear();

      // After removing a property, you should still be able to encode the Property
      props.removeProperty(keyToRemove);
      props.encode(buffer);

      Assert.assertEquals(props.getEncodeSize(), buffer.writerIndex());
   }

   @Test
   public void testEncodeDecodeEmpty() throws Exception
   {
      TypedProperties emptyProps = new TypedProperties();

      HornetQBuffer buffer = HornetQBuffers.dynamicBuffer(1024);
      emptyProps.encode(buffer);

      Assert.assertEquals(props.getEncodeSize(), buffer.writerIndex());

      TypedProperties decodedProps = new TypedProperties();
      decodedProps.decode(buffer);

      TypedPropertiesTest.assertEqualsTypeProperties(emptyProps, decodedProps);
   }

   @Before
   public void setUp() throws Exception
   {


      props = new TypedProperties();
      key = RandomUtil.randomString();
   }

   @After
   public void tearDown() throws Exception
   {
      key = null;
      props = null;


   }
}
