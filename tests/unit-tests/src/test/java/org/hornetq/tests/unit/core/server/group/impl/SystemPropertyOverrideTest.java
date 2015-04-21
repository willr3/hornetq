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
