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

import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.tests.integration.cluster.util.TestableServer;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 */
public class ReplicatedMultipleServerFailoverExtraBackupsTest extends ReplicatedMultipleServerFailoverTest
{
   @Override
   @Test
   public void testStartLiveFirst() throws Exception
   {
      backupServers.get(2).getServer().getConfiguration().setBackupGroupName(getNodeGroupName() + "-0");
      backupServers.get(3).getServer().getConfiguration().setBackupGroupName(getNodeGroupName() + "-1");

      startServers(liveServers);
      startServers(backupServers);
      waitForBackups();

      sendCrashReceive();
      waitForTopology(backupServers.get(0).getServer(), liveServers.size(), 2);
      sendCrashBackupReceive();
   }

   private void waitForBackups() throws InterruptedException
   {
      for (TestableServer backupServer : backupServers)
      {
         waitForComponent(backupServer.getServer(), 5);
      }
   }

   private void startServers(List<TestableServer> servers) throws Exception
   {
      for (TestableServer testableServer : servers)
      {
         testableServer.start();
      }
   }

   @Override
   @Test
   public void testStartBackupFirst() throws Exception
   {
      backupServers.get(2).getServer().getConfiguration().setBackupGroupName(getNodeGroupName() + "-0");
      backupServers.get(3).getServer().getConfiguration().setBackupGroupName(getNodeGroupName() + "-1");

      startServers(backupServers);
      startServers(liveServers);
      waitForBackups();

      waitForTopology(liveServers.get(0).getServer(), liveServers.size(), 2);
      sendCrashReceive();
   }

   protected void sendCrashBackupReceive() throws Exception
   {
      ServerLocator locator0 = getBackupServerLocator(0);
      ServerLocator locator1 = getBackupServerLocator(1);

      ClientSessionFactory factory0 = createSessionFactory(locator0);
      ClientSessionFactory factory1 = createSessionFactory(locator1);

      ClientSession session0 = factory0.createSession(false, true, true);
      ClientSession session1 = factory1.createSession(false, true, true);

      ClientProducer producer = session0.createProducer(MultipleServerFailoverTestBase.ADDRESS);

      for (int i = 0; i < 200; i++)
      {
         ClientMessage message = session0.createMessage(true);

         setBody(i, message);

         message.putIntProperty("counter", i);

         producer.send(message);
      }

      producer.close();

      waitForDistribution(MultipleServerFailoverTestBase.ADDRESS, backupServers.get(0).getServer(), 100);
      waitForDistribution(MultipleServerFailoverTestBase.ADDRESS, backupServers.get(1).getServer(), 100);

      List<TestableServer> toCrash = new ArrayList<TestableServer>();
      for (TestableServer backupServer : backupServers)
      {
         if (!backupServer.getServer().getConfiguration().isBackup())
         {
            toCrash.add(backupServer);
         }
      }

      for (TestableServer testableServer : toCrash)
      {
         testableServer.crash();
      }

      ClientConsumer consumer0 = session0.createConsumer(MultipleServerFailoverTestBase.ADDRESS);
      ClientConsumer consumer1 = session1.createConsumer(MultipleServerFailoverTestBase.ADDRESS);
      session0.start();
      session1.start();


      for (int i = 0; i < 100; i++)
      {
         ClientMessage message = consumer0.receive(1000);
         Assert.assertNotNull("expecting durable msg " + i, message);
         message.acknowledge();
         consumer1.receive(1000);
         Assert.assertNotNull("expecting durable msg " + i, message);
         message.acknowledge();

      }
   }


   @Override
   public int getBackupServerCount()
   {
      return 4;
   }
}
