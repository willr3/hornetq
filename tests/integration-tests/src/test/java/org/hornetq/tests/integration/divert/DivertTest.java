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
import org.hornetq.core.message.impl.MessageImpl;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.HornetQServers;
import org.hornetq.core.settings.impl.AddressSettings;
import org.hornetq.tests.util.ServiceTestBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * A DivertTest
 *
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 *         <p/>
 *         Created 14 Jan 2009 14:05:01
 */
public class DivertTest extends ServiceTestBase
{
   private static final int TIMEOUT = 500;

   @Test
   public void testSingleNonExclusiveDivert() throws Exception
   {
      Configuration conf = createDefaultConfig();
      final String testAddress = "testAddress";

      final String forwardAddress = "forwardAddress";

      DivertConfiguration divertConf = new DivertConfiguration("divert1",
                                                               "divert1",
                                                               testAddress,
                                                               forwardAddress,
                                                               false,
                                                               null,
                                                               null);

      List<DivertConfiguration> divertConfs = new ArrayList<DivertConfiguration>();

      divertConfs.add(divertConf);

      conf.setDivertConfigurations(divertConfs);

      HornetQServer messagingService = addServer(HornetQServers.newHornetQServer(conf, false));

      messagingService.start();

      ServerLocator locator = createInVMNonHALocator();

      ClientSessionFactory sf = createSessionFactory(locator);

      ClientSession session = sf.createSession(false, true, true);

      final String queueName1 = new String("queue1");

      final String queueName2 = new String("queue2");

      session.createQueue(new String(forwardAddress), queueName1, null, false);

      session.createQueue(new String(testAddress), queueName2, null, false);

      session.start();

      ClientProducer producer = session.createProducer(new String(testAddress));

      ClientConsumer consumer1 = session.createConsumer(queueName1);

      ClientConsumer consumer2 = session.createConsumer(queueName2);

      final int numMessages = 1;

      final String propKey = new String("testkey");

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = session.createMessage(false);

         message.putIntProperty(propKey, i);

         producer.send(message);
      }

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer1.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getObjectProperty(propKey));

         message.acknowledge();
      }

      Assert.assertNull(consumer1.receiveImmediate());

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer2.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getObjectProperty(propKey));

         message.acknowledge();
      }

      Assert.assertNull(consumer2.receiveImmediate());

      session.close();

      sf.close();

      messagingService.stop();
   }


   @Test
   public void testSingleDivertWithExpiry() throws Exception
   {
      Configuration conf = createDefaultConfig();
      final String testAddress = "testAddress";

      final String forwardAddress = "forwardAddress";

      final String expiryAddress = "expiryAddress";

      conf.getAddressesSettings().clear();

      AddressSettings expirySettings = new AddressSettings();
      expirySettings.setExpiryAddress(new String(expiryAddress));

      conf.getAddressesSettings().put("#", expirySettings);

      DivertConfiguration divertConf = new DivertConfiguration("divert1",
                                                               "divert1",
                                                               testAddress,
                                                               forwardAddress,
                                                               false,
                                                               null,
                                                               null);

      List<DivertConfiguration> divertConfs = new ArrayList<DivertConfiguration>();

      divertConfs.add(divertConf);

      conf.setDivertConfigurations(divertConfs);

      HornetQServer messagingService = addServer(HornetQServers.newHornetQServer(conf, true));

      messagingService.start();


      ServerLocator locator = createInVMNonHALocator();

      ClientSessionFactory sf = createSessionFactory(locator);

      ClientSession session = sf.createSession(false, false, false);

      final String queueName1 = new String("queue1");

      final String queueName2 = new String("queue2");

      session.createQueue(new String(forwardAddress), queueName1, null, true);

      session.createQueue(new String(testAddress), queueName2, null, true);

      session.createQueue(new String(expiryAddress), new String(expiryAddress), null, true);

      session.start();

      ClientProducer producer = session.createProducer(new String(testAddress));

      ClientConsumer consumer1 = session.createConsumer(queueName1);

      ClientConsumer consumer2 = session.createConsumer(queueName2);

      final int numMessages = 1;

      final String propKey = new String("testkey");

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = session.createMessage(true);

         message.putIntProperty(propKey, i);

         message.setExpiration(System.currentTimeMillis() + 1000);

         producer.send(message);
      }
      session.commit();


      // this context is validating if these messages are routed correctly
      {
         int count1 = 0;
         ClientMessage message = null;
         while ((message = consumer1.receiveImmediate()) != null)
         {
            message.acknowledge();
            count1++;
         }

         int count2 = 0;
         message = null;
         while ((message = consumer2.receiveImmediate()) != null)
         {
            message.acknowledge();
            count2++;
         }

         assertEquals(1, count1);
         assertEquals(1, count2);
         session.rollback();
      }
      Thread.sleep(2000);

      // it must been expired by now
      assertNull(consumer1.receiveImmediate());
      // it must been expired by now
      assertNull(consumer2.receiveImmediate());

      int countOriginal1 = 0;
      int countOriginal2 = 0;
      ClientConsumer consumerExpiry = session.createConsumer(expiryAddress);

      for (int i = 0; i < numMessages * 2; i++)
      {
         ClientMessage message = consumerExpiry.receive(5000);
         System.out.println("Received message " + message);
         assertNotNull(message);

         if (message.getStringProperty(MessageImpl.HDR_ORIGINAL_QUEUE).equals("queue1"))
         {
            countOriginal1++;
         }
         else if (message.getStringProperty(MessageImpl.HDR_ORIGINAL_QUEUE).equals("queue2"))
         {
            countOriginal2++;
         }
         else
         {
            System.out.println("message not part of any expired queue" + message);
         }
      }

      assertEquals(numMessages, countOriginal1);
      assertEquals(numMessages, countOriginal2);

      session.close();

      sf.close();

      messagingService.stop();
   }

   @Test
   public void testSingleNonExclusiveDivert2() throws Exception
   {
      Configuration conf = createDefaultConfig();
      final String testAddress = "testAddress";

      final String forwardAddress = "forwardAddress";

      DivertConfiguration divertConf = new DivertConfiguration("divert1",
                                                               "divert1",
                                                               testAddress,
                                                               forwardAddress,
                                                               false,
                                                               null,
                                                               null);

      List<DivertConfiguration> divertConfs = new ArrayList<DivertConfiguration>();

      divertConfs.add(divertConf);

      conf.setDivertConfigurations(divertConfs);

      HornetQServer messagingService = addServer(HornetQServers.newHornetQServer(conf, false));

      messagingService.start();

      ServerLocator locator = createInVMNonHALocator();

      ClientSessionFactory sf = createSessionFactory(locator);

      ClientSession session = sf.createSession(false, true, true);

      final String queueName1 = new String("queue1");

      final String queueName2 = new String("queue2");

      final String queueName3 = new String("queue3");

      final String queueName4 = new String("queue4");

      session.createQueue(new String(forwardAddress), queueName1, null, false);

      session.createQueue(new String(testAddress), queueName2, null, false);

      session.createQueue(new String(testAddress), queueName3, null, false);

      session.createQueue(new String(testAddress), queueName4, null, false);

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
         ClientMessage message = session.createMessage(false);

         message.putIntProperty(propKey, i);

         producer.send(message);
      }

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer1.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getObjectProperty(propKey));

         message.acknowledge();
      }

      Assert.assertNull(consumer1.receiveImmediate());

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer2.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getObjectProperty(propKey));

         message.acknowledge();
      }

      Assert.assertNull(consumer2.receiveImmediate());

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer3.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getObjectProperty(propKey));

         message.acknowledge();
      }

      Assert.assertNull(consumer3.receiveImmediate());

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer4.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getObjectProperty(propKey));

         message.acknowledge();
      }

      Assert.assertNull(consumer4.receiveImmediate());

      session.close();

      sf.close();

      messagingService.stop();
   }

   @Test
   public void testSingleNonExclusiveDivert3() throws Exception
   {
      Configuration conf = createDefaultConfig();
      final String testAddress = "testAddress";

      final String forwardAddress = "forwardAddress";

      DivertConfiguration divertConf = new DivertConfiguration("divert1",
                                                               "divert1",
                                                               testAddress,
                                                               forwardAddress,
                                                               false,
                                                               null,
                                                               null);

      List<DivertConfiguration> divertConfs = new ArrayList<DivertConfiguration>();

      divertConfs.add(divertConf);

      conf.setDivertConfigurations(divertConfs);

      HornetQServer messagingService = addServer(HornetQServers.newHornetQServer(conf, false));

      messagingService.start();

      ServerLocator locator = createInVMNonHALocator();
      ClientSessionFactory sf = createSessionFactory(locator);

      ClientSession session = sf.createSession(false, true, true);

      final String queueName1 = new String("queue1");

      session.createQueue(new String(forwardAddress), queueName1, null, false);

      session.start();

      ClientProducer producer = session.createProducer(new String(testAddress));

      ClientConsumer consumer1 = session.createConsumer(queueName1);

      final int numMessages = 10;

      final String propKey = new String("testkey");

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = session.createMessage(false);

         message.putIntProperty(propKey, i);

         producer.send(message);
      }

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer1.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getObjectProperty(propKey));

         message.acknowledge();
      }

      Assert.assertNull(consumer1.receiveImmediate());

      session.close();

      sf.close();

      messagingService.stop();
   }

   @Test
   public void testSingleExclusiveDivert() throws Exception
   {
      Configuration conf = createDefaultConfig();
      final String testAddress = "testAddress";

      final String forwardAddress = "forwardAddress";

      DivertConfiguration divertConf = new DivertConfiguration("divert1",
                                                               "divert1",
                                                               testAddress,
                                                               forwardAddress,
                                                               true,
                                                               null,
                                                               null);

      List<DivertConfiguration> divertConfs = new ArrayList<DivertConfiguration>();

      divertConfs.add(divertConf);

      conf.setDivertConfigurations(divertConfs);

      HornetQServer messagingService = addServer(HornetQServers.newHornetQServer(conf, false));

      messagingService.start();

      ServerLocator locator = createInVMNonHALocator();
      ClientSessionFactory sf = createSessionFactory(locator);

      ClientSession session = sf.createSession(false, true, true);

      final String queueName1 = new String("queue1");

      final String queueName2 = new String("queue2");

      final String queueName3 = new String("queue3");

      final String queueName4 = new String("queue4");

      session.createQueue(new String(forwardAddress), queueName1, null, false);

      session.createQueue(new String(testAddress), queueName2, null, false);
      session.createQueue(new String(testAddress), queueName3, null, false);
      session.createQueue(new String(testAddress), queueName4, null, false);

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
         ClientMessage message = session.createMessage(false);

         message.putIntProperty(propKey, i);

         producer.send(message);
      }

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer1.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getObjectProperty(propKey));

         message.acknowledge();
      }

      Assert.assertNull(consumer1.receiveImmediate());

      Assert.assertNull(consumer2.receiveImmediate());

      Assert.assertNull(consumer3.receiveImmediate());

      Assert.assertNull(consumer4.receiveImmediate());

      session.close();

      sf.close();

      messagingService.stop();
   }

   @Test
   public void testMultipleNonExclusiveDivert() throws Exception
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

      HornetQServer messagingService = addServer(HornetQServers.newHornetQServer(conf, false));

      messagingService.start();

      ServerLocator locator = createInVMNonHALocator();
      ClientSessionFactory sf = createSessionFactory(locator);

      ClientSession session = sf.createSession(false, true, true);

      final String queueName1 = new String("queue1");

      final String queueName2 = new String("queue2");

      final String queueName3 = new String("queue3");

      final String queueName4 = new String("queue4");

      session.createQueue(new String(forwardAddress1), queueName1, null, false);

      session.createQueue(new String(forwardAddress2), queueName2, null, false);

      session.createQueue(new String(forwardAddress3), queueName3, null, false);

      session.createQueue(new String(testAddress), queueName4, null, false);

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
         ClientMessage message = session.createMessage(false);

         message.putIntProperty(propKey, i);

         producer.send(message);
      }

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer1.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getIntProperty(propKey).intValue());

         message.acknowledge();
      }

      Assert.assertNull(consumer1.receiveImmediate());

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer2.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getIntProperty(propKey).intValue());

         message.acknowledge();
      }

      Assert.assertNull(consumer2.receiveImmediate());

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer3.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getIntProperty(propKey).intValue());

         message.acknowledge();
      }

      Assert.assertNull(consumer3.receiveImmediate());

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer4.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getIntProperty(propKey).intValue());

         message.acknowledge();
      }

      Assert.assertNull(consumer4.receiveImmediate());

      session.close();

      sf.close();

      messagingService.stop();
   }

   @Test
   public void testMultipleExclusiveDivert() throws Exception
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
                                                                true,
                                                                null,
                                                                null);

      DivertConfiguration divertConf2 = new DivertConfiguration("divert2",
                                                                "divert2",
                                                                testAddress,
                                                                forwardAddress2,
                                                                true,
                                                                null,
                                                                null);

      DivertConfiguration divertConf3 = new DivertConfiguration("divert3",
                                                                "divert3",
                                                                testAddress,
                                                                forwardAddress3,
                                                                true,
                                                                null,
                                                                null);

      List<DivertConfiguration> divertConfs = new ArrayList<DivertConfiguration>();

      divertConfs.add(divertConf1);
      divertConfs.add(divertConf2);
      divertConfs.add(divertConf3);

      conf.setDivertConfigurations(divertConfs);

      HornetQServer messagingService = addServer(HornetQServers.newHornetQServer(conf, false));

      messagingService.start();

      ServerLocator locator = createInVMNonHALocator();
      ClientSessionFactory sf = createSessionFactory(locator);

      ClientSession session = sf.createSession(false, true, true);

      final String queueName1 = new String("queue1");

      final String queueName2 = new String("queue2");

      final String queueName3 = new String("queue3");

      final String queueName4 = new String("queue4");

      session.createQueue(new String(forwardAddress1), queueName1, null, false);

      session.createQueue(new String(forwardAddress2), queueName2, null, false);

      session.createQueue(new String(forwardAddress3), queueName3, null, false);

      session.createQueue(new String(testAddress), queueName4, null, false);

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
         ClientMessage message = session.createMessage(false);

         message.putIntProperty(propKey, i);

         producer.send(message);
      }

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer1.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getIntProperty(propKey).intValue());

         message.acknowledge();
      }

      Assert.assertNull(consumer1.receiveImmediate());

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer2.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getIntProperty(propKey).intValue());

         message.acknowledge();
      }

      Assert.assertNull(consumer2.receiveImmediate());

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer3.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getIntProperty(propKey).intValue());

         message.acknowledge();
      }

      Assert.assertNull(consumer3.receiveImmediate());

      Assert.assertNull(consumer4.receiveImmediate());

      session.close();

      sf.close();

      messagingService.stop();
   }

   @Test
   public void testMixExclusiveAndNonExclusiveDiverts() throws Exception
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
                                                                true,
                                                                null,
                                                                null);

      DivertConfiguration divertConf2 = new DivertConfiguration("divert2",
                                                                "divert2",
                                                                testAddress,
                                                                forwardAddress2,
                                                                true,
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

      HornetQServer messagingService = addServer(HornetQServers.newHornetQServer(conf, false));

      messagingService.start();

      ServerLocator locator = createInVMNonHALocator();
      ClientSessionFactory sf = createSessionFactory(locator);

      ClientSession session = sf.createSession(false, true, true);

      final String queueName1 = new String("queue1");

      final String queueName2 = new String("queue2");

      final String queueName3 = new String("queue3");

      final String queueName4 = new String("queue4");

      session.createQueue(new String(forwardAddress1), queueName1, null, false);

      session.createQueue(new String(forwardAddress2), queueName2, null, false);

      session.createQueue(new String(forwardAddress3), queueName3, null, false);

      session.createQueue(new String(testAddress), queueName4, null, false);

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
         ClientMessage message = session.createMessage(false);

         message.putIntProperty(propKey, i);

         producer.send(message);
      }

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer1.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getIntProperty(propKey).intValue());

         message.acknowledge();
      }

      Assert.assertNull(consumer1.receiveImmediate());

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer2.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getIntProperty(propKey).intValue());

         message.acknowledge();
      }

      Assert.assertNull(consumer2.receiveImmediate());

      Assert.assertNull(consumer3.receiveImmediate());

      Assert.assertNull(consumer4.receiveImmediate());

      session.close();

      sf.close();

      messagingService.stop();
   }

   // If no exclusive diverts match then non exclusive ones should be called
   @Test
   public void testSingleExclusiveNonMatchingAndNonExclusiveDiverts() throws Exception
   {
      Configuration conf = createDefaultConfig();
      final String testAddress = "testAddress";

      final String forwardAddress1 = "forwardAddress1";
      final String forwardAddress2 = "forwardAddress2";
      final String forwardAddress3 = "forwardAddress3";

      final String filter = "animal='antelope'";

      DivertConfiguration divertConf1 = new DivertConfiguration("divert1",
                                                                "divert1",
                                                                testAddress,
                                                                forwardAddress1,
                                                                true,
                                                                filter,
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

      HornetQServer messagingService = addServer(HornetQServers.newHornetQServer(conf, false));

      messagingService.start();

      ServerLocator locator = createInVMNonHALocator();
      ClientSessionFactory sf = createSessionFactory(locator);

      ClientSession session = sf.createSession(false, true, true);

      final String queueName1 = new String("queue1");

      final String queueName2 = new String("queue2");

      final String queueName3 = new String("queue3");

      final String queueName4 = new String("queue4");

      session.createQueue(new String(forwardAddress1), queueName1, null, false);

      session.createQueue(new String(forwardAddress2), queueName2, null, false);

      session.createQueue(new String(forwardAddress3), queueName3, null, false);

      session.createQueue(new String(testAddress), queueName4, null, false);

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
         ClientMessage message = session.createMessage(false);

         message.putStringProperty(new String("animal"), new String("giraffe"));

         message.putIntProperty(propKey, i);

         producer.send(message);
      }

      // for (int i = 0; i < numMessages; i++)
      // {
      // ClientMessage message = consumer1.receive(200);
      //
      // assertNotNull(message);
      //
      // assertEquals((Integer)i, (Integer)message.getProperty(propKey));
      //
      // message.acknowledge();
      // }

      Assert.assertNull(consumer1.receiveImmediate());

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer2.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getObjectProperty(propKey));

         message.acknowledge();
      }

      Assert.assertNull(consumer2.receiveImmediate());

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer3.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getObjectProperty(propKey));

         message.acknowledge();
      }

      Assert.assertNull(consumer3.receiveImmediate());

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer4.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getObjectProperty(propKey));

         message.acknowledge();
      }

      Assert.assertNull(consumer4.receiveImmediate());

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = session.createMessage(false);

         message.putStringProperty(new String("animal"), new String("antelope"));

         message.putIntProperty(propKey, i);

         producer.send(message);
      }

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer1.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getObjectProperty(propKey));

         message.acknowledge();
      }

      Assert.assertNull(consumer1.receiveImmediate());

      Assert.assertNull(consumer2.receiveImmediate());

      Assert.assertNull(consumer3.receiveImmediate());

      Assert.assertNull(consumer4.receiveImmediate());

      session.close();

      sf.close();

      messagingService.stop();
   }

   @Test
   public void testRoundRobinDiverts() throws Exception
   {
      Configuration conf = createDefaultConfig();
      final String testAddress = "testAddress";

      final String forwardAddress1 = "forwardAddress1";
      final String forwardAddress2 = "forwardAddress2";
      final String forwardAddress3 = "forwardAddress3";

      DivertConfiguration divertConf1 = new DivertConfiguration("divert1",
                                                                "thename",
                                                                testAddress,
                                                                forwardAddress1,
                                                                false,
                                                                null,
                                                                null);

      DivertConfiguration divertConf2 = new DivertConfiguration("divert2",
                                                                "thename",
                                                                testAddress,
                                                                forwardAddress2,
                                                                false,
                                                                null,
                                                                null);

      DivertConfiguration divertConf3 = new DivertConfiguration("divert3",
                                                                "thename",
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

      HornetQServer messagingService = addServer(HornetQServers.newHornetQServer(conf, false));

      messagingService.start();

      ServerLocator locator = createInVMNonHALocator();
      ClientSessionFactory sf = createSessionFactory(locator);

      ClientSession session = sf.createSession(false, true, true);

      final String queueName1 = new String("queue1");

      final String queueName2 = new String("queue2");

      final String queueName3 = new String("queue3");

      final String queueName4 = new String("queue4");

      session.createQueue(new String(forwardAddress1), queueName1, null, false);

      session.createQueue(new String(forwardAddress2), queueName2, null, false);

      session.createQueue(new String(forwardAddress3), queueName3, null, false);

      session.createQueue(new String(testAddress), queueName4, null, false);

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
         ClientMessage message = session.createMessage(false);

         message.putIntProperty(propKey, i);

         producer.send(message);
      }

      for (int i = 0; i < numMessages; )
      {
         ClientMessage message = consumer1.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getObjectProperty(propKey));

         message.acknowledge();

         i++;

         if (i == numMessages)
         {
            break;
         }

         message = consumer2.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getObjectProperty(propKey));

         message.acknowledge();

         i++;

         if (i == numMessages)
         {
            break;
         }

         message = consumer3.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getObjectProperty(propKey));

         message.acknowledge();

         i++;
      }

      Assert.assertNull(consumer1.receiveImmediate());
      Assert.assertNull(consumer2.receiveImmediate());
      Assert.assertNull(consumer3.receiveImmediate());

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer4.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getObjectProperty(propKey));

         message.acknowledge();
      }

      Assert.assertNull(consumer4.receiveImmediate());

      session.close();

      sf.close();

      messagingService.stop();
   }

   @Test
   public void testDeployDivertsSameUniqueName() throws Exception
   {
      Configuration conf = createDefaultConfig();

      final String testAddress = "testAddress";

      final String forwardAddress1 = "forwardAddress1";
      final String forwardAddress2 = "forwardAddress2";
      final String forwardAddress3 = "forwardAddress3";

      DivertConfiguration divertConf1 = new DivertConfiguration("divert1",
                                                                "thename1",
                                                                testAddress,
                                                                forwardAddress1,
                                                                false,
                                                                null,
                                                                null);

      DivertConfiguration divertConf2 = new DivertConfiguration("divert1",
                                                                "thename2",
                                                                testAddress,
                                                                forwardAddress2,
                                                                false,
                                                                null,
                                                                null);

      DivertConfiguration divertConf3 = new DivertConfiguration("divert2",
                                                                "thename3",
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

      HornetQServer messagingService = addServer(HornetQServers.newHornetQServer(conf, false));

      messagingService.start();

      // Only the first and third should be deployed

      ServerLocator locator = createInVMNonHALocator();
      ClientSessionFactory sf = createSessionFactory(locator);

      ClientSession session = sf.createSession(false, true, true);

      final String queueName1 = new String("queue1");

      final String queueName2 = new String("queue2");

      final String queueName3 = new String("queue3");

      final String queueName4 = new String("queue4");

      session.createQueue(new String(forwardAddress1), queueName1, null, false);

      session.createQueue(new String(forwardAddress2), queueName2, null, false);

      session.createQueue(new String(forwardAddress3), queueName3, null, false);

      session.createQueue(new String(testAddress), queueName4, null, false);

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
         ClientMessage message = session.createMessage(false);

         message.putIntProperty(propKey, i);

         producer.send(message);
      }

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer1.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getIntProperty(propKey).intValue());

         message.acknowledge();
      }

      Assert.assertNull(consumer1.receiveImmediate());

      Assert.assertNull(consumer2.receiveImmediate());

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer3.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getIntProperty(propKey).intValue());

         message.acknowledge();
      }

      Assert.assertNull(consumer3.receiveImmediate());

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = consumer4.receive(DivertTest.TIMEOUT);

         Assert.assertNotNull(message);

         Assert.assertEquals(i, message.getIntProperty(propKey).intValue());

         message.acknowledge();
      }

      Assert.assertNull(consumer4.receiveImmediate());

      session.close();

      sf.close();

      messagingService.stop();
   }

}
