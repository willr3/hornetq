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
package org.hornetq.tests.integration.divert;

import org.hornetq.api.core.client.*;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.DivertConfiguration;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.HornetQServers;
import org.hornetq.tests.util.ServiceTestBase;
import org.hornetq.tests.util.UnitTestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * A PersistentDivertTest
 *
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 *
 * Created 14 Jan 2009 14:05:01
 *
 *
 */
public class PersistentDivertTest extends ServiceTestBase
{
   final int minLargeMessageSize = HornetQClient.DEFAULT_MIN_LARGE_MESSAGE_SIZE * 2;

   @Test
   public void testPersistentDivert() throws Exception
   {
      doTestPersistentDivert(false);
   }

   @Test
   public void testPersistentDiverLargeMessage() throws Exception
   {
      doTestPersistentDivert(true);
   }

   public void doTestPersistentDivert(final boolean largeMessage) throws Exception
   {
      Configuration conf = createDefaultConfig();

      final String testAddress = "testAddress";

      final String forwardAddress1 = "forwardAddress1";

      final String forwardAddress2 = "forwardAddress2";

      final String forwardAddress3 = "forwardAddress3";

      DivertConfiguration divertConf1 = new DivertConfiguration("divert1",
                                                                "divert1",
                                                                testAddress,
                                                                forwardAddress1,
                                                                false,
                                                                null,
                                                                null);

      DivertConfiguration divertConf2 = new DivertConfiguration("divert2",
                                                                "divert2",
                                                                testAddress,
                                                                forwardAddress2,
                                                                false,
                                                                null,
                                                                null);

      DivertConfiguration divertConf3 = new DivertConfiguration("divert3",
                                                                "divert3",
                                                                testAddress,
                                                                forwardAddress3,
                                                                false,
                                                                null,
                                                                null);

      List<DivertConfiguration> divertConfs = new ArrayList<DivertConfiguration>();

      divertConfs.add(divertConf1);
      divertConfs.add(divertConf2);
      divertConfs.add(divertConf3);

      conf.setDivertConfigurations(divertConfs);

      HornetQServer messagingService = addServer(HornetQServers.newHornetQServer(conf));

      messagingService.start();

      ServerLocator locator = createInVMNonHALocator();

      locator.setBlockOnAcknowledge(true);
      locator.setBlockOnNonDurableSend(true);
      locator.setBlockOnDurableSend(true);

      ClientSessionFactory sf = createSessionFactory(locator);

      ClientSession session = sf.createSession(true, true, 0);

      final String queueName1 = new String("queue1");

      final String queueName2 = new String("queue2");

      final String queueName3 = new String("queue3");

      final String queueName4 = new String("queue4");

      session.createQueue(new String(forwardAddress1), queueName1, null, true);

      session.createQueue(new String(forwardAddress2), queueName2, null, true);

      session.createQueue(new String(forwardAddress3), queueName3, null, true);

      session.createQueue(new String(testAddress), queueName4, null, true);

      session.start();

      ClientProducer producer = session.createProducer(new String(testAddress));

      ClientConsumer consumer1 = session.createConsumer(queueName1);

      ClientConsumer consumer2 = session.createConsumer(queueName2);

      ClientConsumer consumer3 = session.createConsumer(queueName3);

      ClientConsumer consumer4 = session.createConsumer(queueName4);

      final int numMessages = 10;

      final String propKey = new String("testkey");

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = session.createMessage(true);

         if (largeMessage)
         {
            message.setBodyInputStream(UnitTestCase.createFakeLargeStream(minLargeMessageSize));
         }

         message.putIntProperty(propKey, i);

         producer.send(message);
      }

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer1.receive(5000);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getObjectProperty(propKey));

         if (largeMessage)
         {
            checkLargeMessage(message);
         }

         message.acknowledge();
      }

      Assert.assertNull(consumer1.receiveImmediate());

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer2.receive(5000);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getObjectProperty(propKey));

         if (largeMessage)
         {
            checkLargeMessage(message);
         }

         message.acknowledge();
      }

      Assert.assertNull(consumer2.receiveImmediate());

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer3.receive(5000);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getObjectProperty(propKey));

         if (largeMessage)
         {
            checkLargeMessage(message);
         }

         message.acknowledge();
      }

      Assert.assertNull(consumer3.receiveImmediate());

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer4.receive(5000);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getObjectProperty(propKey));

         if (largeMessage)
         {
            checkLargeMessage(message);
         }

         message.acknowledge();
      }

      Assert.assertNull(consumer4.receiveImmediate());
      session.close();

      sf.close();
   }

   /**
    * @param message
    */
   private void checkLargeMessage(final ClientMessage message)
   {
      for (int j = 0; j < minLargeMessageSize; j++)
      {
         Assert.assertEquals(UnitTestCase.getSamplebyte(j), message.getBodyBuffer().readByte());
      }
   }

   @Test
   public void testPersistentDivertRestartBeforeConsume() throws Exception
   {
      doTestPersistentDivertRestartBeforeConsume(false);
   }

   @Test
   public void testPersistentDivertRestartBeforeConsumeLargeMessage() throws Exception
   {
      doTestPersistentDivertRestartBeforeConsume(true);
   }

   public void doTestPersistentDivertRestartBeforeConsume(final boolean largeMessage) throws Exception
   {
      Configuration conf = createDefaultConfig();

      final String testAddress = "testAddress";

      final String forwardAddress1 = "forwardAddress1";

      final String forwardAddress2 = "forwardAddress2";

      final String forwardAddress3 = "forwardAddress3";

      DivertConfiguration divertConf1 = new DivertConfiguration("divert1",
                                                                "divert1",
                                                                testAddress,
                                                                forwardAddress1,
                                                                false,
                                                                null,
                                                                null);

      DivertConfiguration divertConf2 = new DivertConfiguration("divert2",
                                                                "divert2",
                                                                testAddress,
                                                                forwardAddress2,
                                                                false,
                                                                null,
                                                                null);

      DivertConfiguration divertConf3 = new DivertConfiguration("divert3",
                                                                "divert3",
                                                                testAddress,
                                                                forwardAddress3,
                                                                false,
                                                                null,
                                                                null);

      List<DivertConfiguration> divertConfs = new ArrayList<DivertConfiguration>();

      divertConfs.add(divertConf1);
      divertConfs.add(divertConf2);
      divertConfs.add(divertConf3);

      conf.setDivertConfigurations(divertConfs);

      HornetQServer messagingService = addServer(HornetQServers.newHornetQServer(conf));

      messagingService.start();

      ServerLocator locator = createInVMNonHALocator();
      locator.setBlockOnAcknowledge(true);
      locator.setBlockOnNonDurableSend(true);
      locator.setBlockOnDurableSend(true);

      ClientSessionFactory sf = createSessionFactory(locator);

      ClientSession session = sf.createSession(true, true, 0);

      final String queueName1 = new String("queue1");

      final String queueName2 = new String("queue2");

      final String queueName3 = new String("queue3");

      final String queueName4 = new String("queue4");

      session.createQueue(new String(forwardAddress1), queueName1, null, true);

      session.createQueue(new String(forwardAddress2), queueName2, null, true);

      session.createQueue(new String(forwardAddress3), queueName3, null, true);

      session.createQueue(new String(testAddress), queueName4, null, true);

      ClientProducer producer = session.createProducer(new String(testAddress));

      final int numMessages = 10;

      final String propKey = new String("testkey");

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = session.createMessage(true);

         message.putIntProperty(propKey, i);

         if (largeMessage)
         {
            message.setBodyInputStream(UnitTestCase.createFakeLargeStream(minLargeMessageSize));
         }

         producer.send(message);
      }

      session.close();

      sf.close();

      messagingService.stop();

      messagingService.start();

      ServerLocator locator2 = createInVMNonHALocator();
      locator2.setBlockOnDurableSend(true);

      sf = createSessionFactory(locator2);
      session = sf.createSession(false, true, true);

      session.start();

      ClientConsumer consumer1 = session.createConsumer(queueName1);

      ClientConsumer consumer2 = session.createConsumer(queueName2);

      ClientConsumer consumer3 = session.createConsumer(queueName3);

      ClientConsumer consumer4 = session.createConsumer(queueName4);

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer1.receive(5000);

         Assert.assertNotNull(message);

         if (largeMessage)
         {
            checkLargeMessage(message);
         }

         Assert.assertEquals(i, message.getObjectProperty(propKey));

         message.acknowledge();
      }

      Assert.assertNull(consumer1.receiveImmediate());

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer2.receive(5000);

         Assert.assertNotNull(message);

         if (largeMessage)
         {
            checkLargeMessage(message);
         }

         Assert.assertEquals(i, message.getObjectProperty(propKey));

         message.acknowledge();
      }

      Assert.assertNull(consumer2.receiveImmediate());

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer3.receive(5000);

         Assert.assertNotNull(message);

         if (largeMessage)
         {
            checkLargeMessage(message);
         }

         Assert.assertEquals(i, message.getObjectProperty(propKey));

         message.acknowledge();
      }

      Assert.assertNull(consumer3.receiveImmediate());

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer4.receive(5000);

         Assert.assertNotNull(message);

         if (largeMessage)
         {
            checkLargeMessage(message);
         }

         Assert.assertEquals(i, message.getObjectProperty(propKey));

         message.acknowledge();
      }

      Assert.assertNull(consumer4.receiveImmediate());

      session.close();

      sf.close();

      messagingService.stop();

      messagingService.start();

      ServerLocator locator3 = createInVMNonHALocator();
      locator3.setBlockOnDurableSend(true);

      sf = createSessionFactory(locator3);

      session = sf.createSession(false, true, true);

      consumer1 = session.createConsumer(queueName1);

      consumer2 = session.createConsumer(queueName2);

      consumer3 = session.createConsumer(queueName3);

      consumer4 = session.createConsumer(queueName4);

      Assert.assertNull(consumer1.receiveImmediate());

      Assert.assertNull(consumer2.receiveImmediate());

      Assert.assertNull(consumer3.receiveImmediate());

      Assert.assertNull(consumer4.receiveImmediate());

      session.close();

      sf.close();
   }

}
