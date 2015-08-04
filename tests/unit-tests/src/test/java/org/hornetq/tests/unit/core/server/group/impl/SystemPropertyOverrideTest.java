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
package org.hornetq.tests.unit.core.server.group.impl;


import org.hornetq.core.server.group.impl.GroupingHandlerConfiguration;
import org.hornetq.tests.util.UnitTestCase;

public class SystemPropertyOverrideTest extends UnitTestCase
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   public void testSystemPropertyOverride() throws Exception
   {
      final String groupTimeoutPropertyValue = "1234";
      final String reaperPeriodPropertyValue = "5678";

      System.setProperty(GroupingHandlerConfiguration.GROUP_TIMEOUT_PROP_NAME, groupTimeoutPropertyValue);
      System.setProperty(GroupingHandlerConfiguration.REAPER_PERIOD_PROP_NAME, reaperPeriodPropertyValue);

      GroupingHandlerConfiguration groupingHandlerConfiguration = new GroupingHandlerConfiguration(new String("test"), GroupingHandlerConfiguration.TYPE.LOCAL, new String("address"));

      assertEquals(groupingHandlerConfiguration.getGroupTimeout(), Long.parseLong(groupTimeoutPropertyValue));
      assertEquals(groupingHandlerConfiguration.getReaperPeriod(), Long.parseLong(reaperPeriodPropertyValue));
   }
}
