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
package org.hornetq.tests.unit.core.server.cluster.impl;

import org.hornetq.core.server.Queue;
import org.hornetq.core.server.cluster.impl.RemoteQueueBindingImpl;
import org.hornetq.tests.unit.core.postoffice.impl.FakeQueue;
import org.hornetq.tests.util.RandomUtil;
import org.hornetq.tests.util.UnitTestCase;
import org.junit.Test;

/**
 * A RemoteQueueBindImplTest
 *
 * @author clebertsuconic
 *
 *
 */
public class RemoteQueueBindImplTest extends UnitTestCase
{

   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   @Test
   public void testAddRemoveConsumer() throws Exception
   {

      final long id = RandomUtil.randomLong();
      final String address = RandomUtil.randomSimpleString();
      final String uniqueName = RandomUtil.randomSimpleString();
      final String routingName = RandomUtil.randomSimpleString();
      final Long remoteQueueID = RandomUtil.randomLong();
      final String filterString = new String("A>B");
      final Queue storeAndForwardQueue = new FakeQueue(null);
      final String bridgeName = RandomUtil.randomSimpleString();
      final int distance = 0;
      RemoteQueueBindingImpl binding = new RemoteQueueBindingImpl(id,
                                                                  address,
                                                                  uniqueName,
                                                                  routingName,
                                                                  remoteQueueID,
                                                                  filterString,
                                                                  storeAndForwardQueue,
                                                                  bridgeName,
                                                                  distance);

      for (int i = 0; i < 100; i++)
      {
         binding.addConsumer(new String("B" + i + "<A"));
      }

      assertEquals(100, binding.getFilters().size());

      for (int i = 0; i < 100; i++)
      {
         binding.removeConsumer(new String("B" + i + "<A"));
      }

      assertEquals(0, binding.getFilters().size());

   }
}
