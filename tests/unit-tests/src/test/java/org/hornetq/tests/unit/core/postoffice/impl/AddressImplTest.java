/*
 * Copyright 2005-2014 Red Hat, Inc.
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
package org.hornetq.tests.unit.core.postoffice.impl;

import org.hornetq.core.postoffice.Address;
import org.hornetq.core.postoffice.impl.AddressImpl;
import org.hornetq.tests.util.UnitTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 */
public class AddressImplTest extends UnitTestCase
{
   @Test
   public void testNoDots()
   {
      String s1 = new String("abcde");
      String s2 = new String("abcde");
      Address a1 = new AddressImpl(s1);
      Address a2 = new AddressImpl(s2);
      Assert.assertTrue(a1.matches(a2));
   }

   @Test
   public void testDotsSameLength2()
   {
      String s1 = new String("a.b");
      String s2 = new String("a.b");
      Address a1 = new AddressImpl(s1);
      Address a2 = new AddressImpl(s2);
      Assert.assertTrue(a1.matches(a2));
   }

   @Test
   public void testA()
   {
      String s1 = new String("a.b.c");
      String s2 = new String("a.b.c.d.e.f.g.h.i.j.k.l.m.n.*");
      Address a1 = new AddressImpl(s1);
      Address a2 = new AddressImpl(s2);
      Assert.assertFalse(a1.matches(a2));
   }

   @Test
   public void testB()
   {
      String s1 = new String("a.b.c.d");
      String s2 = new String("a.b.x.e");
      String s3 = new String("a.b.c.*");
      Address a1 = new AddressImpl(s1);
      Address a2 = new AddressImpl(s2);
      Address w = new AddressImpl(s3);
      Assert.assertTrue(a1.matches(w));
      Assert.assertFalse(a2.matches(w));
   }

   @Test
   public void testC()
   {
      String s1 = new String("a.b.c.d");
      String s2 = new String("a.b.c.x");
      String s3 = new String("a.b.*.d");
      Address a1 = new AddressImpl(s1);
      Address a2 = new AddressImpl(s2);
      Address w = new AddressImpl(s3);
      Assert.assertTrue(a1.matches(w));
      Assert.assertFalse(a2.matches(w));
   }

   @Test
   public void testD()
   {
      String s1 = new String("a.b.c.d.e");
      String s2 = new String("a.b.c.x.e");
      String s3 = new String("a.b.*.d.*");
      Address a1 = new AddressImpl(s1);
      Address a2 = new AddressImpl(s2);
      Address w = new AddressImpl(s3);
      Assert.assertTrue(a1.matches(w));
      Assert.assertFalse(a2.matches(w));
   }

   @Test
   public void testE()
   {
      String s1 = new String("a.b.c.d.e.f");
      String s2 = new String("a.b.c.x.e.f");
      String s3 = new String("a.b.*.d.*.f");
      Address a1 = new AddressImpl(s1);
      Address a2 = new AddressImpl(s2);
      Address w = new AddressImpl(s3);
      Assert.assertTrue(a1.matches(w));
      Assert.assertFalse(a2.matches(w));
   }

   @Test
   public void testF()
   {
      String s1 = new String("a.b.c.d.e.f");
      String s2 = new String("a.b.c.x.e.f");
      String s3 = new String("#");
      Address a1 = new AddressImpl(s1);
      Address a2 = new AddressImpl(s2);
      Address w = new AddressImpl(s3);
      Assert.assertTrue(a1.matches(w));
      Assert.assertTrue(a2.matches(w));
   }

   @Test
   public void testG()
   {
      String s1 = new String("a.b.c.d.e.f");
      String s2 = new String("a.b.c.x.e.f");
      String s3 = new String("a.#");
      Address a1 = new AddressImpl(s1);
      Address a2 = new AddressImpl(s2);
      Address w = new AddressImpl(s3);
      Assert.assertTrue(a1.matches(w));
      Assert.assertTrue(a2.matches(w));
   }

   @Test
   public void testH()
   {
      String s1 = new String("a.b.c.d.e.f");
      String s2 = new String("a.b.c.x.e.f");
      String s3 = new String("#.b.#");
      Address a1 = new AddressImpl(s1);
      Address a2 = new AddressImpl(s2);
      Address w = new AddressImpl(s3);
      Assert.assertTrue(a1.matches(w));
      Assert.assertTrue(a2.matches(w));
   }

   @Test
   public void testI()
   {
      String s1 = new String("a.b.c.d.e.f");
      String s2 = new String("a.b.c.x.e.f");
      String s3 = new String("a.#.b.#");
      Address a1 = new AddressImpl(s1);
      Address a2 = new AddressImpl(s2);
      Address w = new AddressImpl(s3);
      Assert.assertTrue(a1.matches(w));
      Assert.assertTrue(a2.matches(w));
   }

   @Test
   public void testJ()
   {
      String s1 = new String("a.b.c.d.e.f");
      String s2 = new String("a.b.c.x.e.f");
      String s3 = new String("a.#.c.d.e.f");
      Address a1 = new AddressImpl(s1);
      Address a2 = new AddressImpl(s2);
      Address w = new AddressImpl(s3);
      Assert.assertTrue(a1.matches(w));
      Assert.assertFalse(a2.matches(w));
   }

   @Test
   public void testK()
   {
      String s1 = new String("a.b.c.d.e.f");
      String s2 = new String("a.b.c.d.e.x");
      String s3 = new String("a.#.c.d.e.*");
      Address a1 = new AddressImpl(s1);
      Address a2 = new AddressImpl(s2);
      Address w = new AddressImpl(s3);
      Assert.assertTrue(a1.matches(w));
      Assert.assertTrue(a2.matches(w));
   }

   @Test
   public void testL()
   {
      String s1 = new String("a.b.c.d.e.f");
      String s2 = new String("a.b.c.d.e.x");
      String s3 = new String("a.#.c.d.*.f");
      Address a1 = new AddressImpl(s1);
      Address a2 = new AddressImpl(s2);
      Address w = new AddressImpl(s3);
      Assert.assertTrue(a1.matches(w));
      Assert.assertFalse(a2.matches(w));
   }

   @Test
   public void testM()
   {
      String s1 = new String("a.b.c");
      String s2 = new String("a.b.x.e");
      String s3 = new String("a.b.c.#");
      Address a1 = new AddressImpl(s1);
      Address a2 = new AddressImpl(s2);
      Address w = new AddressImpl(s3);
      Assert.assertTrue(a1.matches(w));
      Assert.assertFalse(a2.matches(w));
   }

   @Test
   public void testN()
   {
      String s1 = new String("usd.stock");
      String s2 = new String("a.b.x.e");
      String s3 = new String("*.stock.#");
      Address a1 = new AddressImpl(s1);
      Address a2 = new AddressImpl(s2);
      Address w = new AddressImpl(s3);
      Assert.assertTrue(a1.matches(w));
      Assert.assertFalse(a2.matches(w));
   }

   @Test
   public void testO()
   {
      String s1 = new String("a.b.c.d");
      String s2 = new String("a.b.x.e");
      String s3 = new String("a.b.c.*");
      Address a1 = new AddressImpl(s1);
      Address a2 = new AddressImpl(s2);
      Address w = new AddressImpl(s3);
      Assert.assertTrue(a1.matches(w));
      Assert.assertFalse(a2.matches(w));
   }

   @Test
   public void testP()
   {
      String s1 = new String("a.b.c.d");
      String s3 = new String("a.b.c#");
      Address a1 = new AddressImpl(s1);
      Address w = new AddressImpl(s3);
      Assert.assertFalse(a1.matches(w));
   }

   @Test
   public void testQ()
   {
      String s1 = new String("a.b.c.d");
      String s3 = new String("#a.b.c");
      Address a1 = new AddressImpl(s1);
      Address w = new AddressImpl(s3);
      Assert.assertFalse(a1.matches(w));
   }

   @Test
   public void testR()
   {
      String s1 = new String("a.b.c.d");
      String s3 = new String("#*a.b.c");
      Address a1 = new AddressImpl(s1);
      Address w = new AddressImpl(s3);
      Assert.assertFalse(a1.matches(w));
   }

   @Test
   public void testS()
   {
      String s1 = new String("a.b.c.d");
      String s3 = new String("a.b.c*");
      Address a1 = new AddressImpl(s1);
      Address w = new AddressImpl(s3);
      Assert.assertFalse(a1.matches(w));
   }

   @Test
   public void testT()
   {
      String s1 = new String("a.b.c.d");
      String s3 = new String("*a.b.c");
      Address a1 = new AddressImpl(s1);
      Address w = new AddressImpl(s3);
      Assert.assertFalse(a1.matches(w));
   }

   @Test
   public void testU()
   {
      String s1 = new String("a.b.c.d");
      String s3 = new String("*a.b.c");
      Address a1 = new AddressImpl(s1);
      Address w = new AddressImpl(s3);
      Assert.assertFalse(a1.matches(w));
   }

}
