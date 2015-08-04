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
import org.hornetq.core.settings.impl.AddressFullMessagePolicy;
import org.hornetq.core.settings.impl.AddressSettings;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class BackupSyncPagingTest extends BackupSyncJournalTest
{

   @Override
   @Before
   public void setUp() throws Exception
   {
      super.setUp();
      setNumberOfMessages(100);
   }

   @Override
   protected HornetQServer createInVMFailoverServer(final boolean realFiles, final Configuration configuration,
            final NodeManager nodeManager, int id)
   {
      Map<String, AddressSettings> conf = new HashMap<String, AddressSettings>();
      AddressSettings as = new AddressSettings();
      as.setMaxSizeBytes(PAGE_MAX);
      as.setPageSizeBytes(PAGE_SIZE);
      as.setAddressFullMessagePolicy(AddressFullMessagePolicy.PAGE);
      conf.put(ADDRESS.toString(), as);
      return createInVMFailoverServer(realFiles, configuration, PAGE_SIZE, PAGE_MAX, conf, nodeManager, id);
   }

   @Test
   public void testReplicationWithPageFileComplete() throws Exception
   {
      // we could get a first page complete easier with this number
      setNumberOfMessages(20);
      createProducerSendSomeMessages();
      backupServer.start();
      waitForRemoteBackup(sessionFactory, BACKUP_WAIT_TIME, false, backupServer.getServer());

      sendMessages(session, producer, getNumberOfMessages());
      session.commit();

      receiveMsgsInRange(0, getNumberOfMessages());

      finishSyncAndFailover();

      receiveMsgsInRange(0, getNumberOfMessages());
      assertNoMoreMessages();
   }

}
