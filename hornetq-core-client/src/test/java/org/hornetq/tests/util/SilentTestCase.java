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
package org.hornetq.tests.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Test case that hijacks sys-out and sys-err.
 * <p>
 * It is meant to avoid cluttering either during test execution when the tested code (expectedly)
 * writes to these.
 */
public abstract class SilentTestCase extends Assert
{
   private PrintStream origSysOut;
   private PrintStream origSysErr;

   private PrintStream sysOut;
   private PrintStream sysErr;

   @Before
   public void setUp() throws Exception
   {

      origSysOut = System.out;
      origSysErr = System.err;
      sysOut = new PrintStream(new ByteArrayOutputStream());
      System.setOut(sysOut);
      sysErr = new PrintStream(new ByteArrayOutputStream());
      System.setErr(sysErr);
   }

   @After
   public void tearDown() throws Exception
   {
      System.setOut(origSysOut);
      System.setErr(origSysErr);

   }
}
