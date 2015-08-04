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
package org.hornetq.tests.integration.cluster.failover;

import org.hornetq.core.config.Configuration;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.NodeManager;
import org.hornetq.core.settings.impl.AddressSettings;
import org.junit.Test;

import java.util.HashMap;

public class ReplicatedPagedFailoverTest extends ReplicatedFailoverTest
{
   @Override
   protected HornetQServer createInVMFailoverServer(final boolean realFiles, final Configuration configuration,
                                                    final NodeManager nodeManager, int id)
   {
      return createInVMFailoverServer(realFiles, configuration, PAGE_SIZE, PAGE_MAX,
                                      new HashMap<String, AddressSettings>(), nodeManager, id);
   }

   @Override
   @Test
   public void testFailWithBrowser() throws Exception
   {
      // paged messages are not available for browsing
   }
}
