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
package org.hornetq.tests.integration.server;

import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.Message;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.HornetQClient;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.HornetQServers;
import org.hornetq.core.server.Queue;
import org.hornetq.core.settings.impl.AddressSettings;
import org.hornetq.tests.util.UnitTestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 */
public class LVQTest extends UnitTestCase
{
   private HornetQServer server;

   private ClientSession clientSession;

   private ClientSession clientSessionTxReceives;

   private ClientSession clientSessionTxSends;

   private final String address = new String("LVQTestAddress");

   private final String qName1 = new String("LVQTestQ1");

   @Test
   public void testSimple() throws Exception
   {
      ClientProducer producer = clientSession.createProducer(address);
      ClientConsumer consumer = clientSession.createConsumer(qName1);
      ClientMessage m1 = createTextMessage(clientSession, "m1");
      String rh = new String("SMID1");
      m1.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      ClientMessage m2 = createTextMessage(clientSession, "m2");
      m2.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      producer.send(m1);
      producer.send(m2);
      clientSession.start();
      ClientMessage m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m2");
   }

   @Test
   public void testMultipleMessages() throws Exception
   {
      ClientProducer producer = clientSession.createProducer(address);
      ClientConsumer consumer = clientSession.createConsumer(qName1);
      String messageId1 = new String("SMID1");
      String messageId2 = new String("SMID2");
      ClientMessage m1 = createTextMessage(clientSession, "m1");
      m1.putStringProperty(Message.HDR_LAST_VALUE_NAME, messageId1);
      ClientMessage m2 = createTextMessage(clientSession, "m2");
      m2.putStringProperty(Message.HDR_LAST_VALUE_NAME, messageId2);
      ClientMessage m3 = createTextMessage(clientSession, "m3");
      m3.putStringProperty(Message.HDR_LAST_VALUE_NAME, messageId1);
      ClientMessage m4 = createTextMessage(clientSession, "m4");
      m4.putStringProperty(Message.HDR_LAST_VALUE_NAME, messageId2);
      producer.send(m1);
      producer.send(m2);
      producer.send(m3);
      producer.send(m4);
      clientSession.start();
      ClientMessage m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m3");
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m4");
   }

   @Test
   public void testFirstMessageReceivedButAckedAfter() throws Exception
   {
      ClientProducer producer = clientSession.createProducer(address);
      ClientConsumer consumer = clientSession.createConsumer(qName1);
      ClientMessage m1 = createTextMessage(clientSession, "m1");
      String rh = new String("SMID1");
      m1.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      ClientMessage m2 = createTextMessage(clientSession, "m2");
      m2.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      producer.send(m1);
      clientSession.start();
      ClientMessage m = consumer.receive(1000);
      Assert.assertNotNull(m);
      producer.send(m2);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m1");
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m2");
   }

   @Test
   public void testFirstMessageReceivedAndCancelled() throws Exception
   {
      ClientProducer producer = clientSession.createProducer(address);
      ClientConsumer consumer = clientSession.createConsumer(qName1);
      ClientMessage m1 = createTextMessage(clientSession, "m1");
      String rh = new String("SMID1");
      m1.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      ClientMessage m2 = createTextMessage(clientSession, "m2");
      m2.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      producer.send(m1);
      clientSession.start();
      ClientMessage m = consumer.receive(1000);
      Assert.assertNotNull(m);
      Assert.assertEquals(m.getBodyBuffer().readString(), "m1");
      producer.send(m2);
      consumer.close();
      consumer = clientSession.createConsumer(qName1);
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      Assert.assertEquals("m2", m.getBodyBuffer().readString());
      m.acknowledge();
      m = consumer.receiveImmediate();
      Assert.assertNull(m);
   }

   @Test
   public void testManyMessagesReceivedAndCancelled() throws Exception
   {
      ClientProducer producer = clientSession.createProducer(address);
      ClientConsumer consumer = clientSession.createConsumer(qName1);

      String rh = new String("SMID1");
      ClientMessage m1 = createTextMessage(clientSession, "m1");
      m1.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      ClientMessage m2 = createTextMessage(clientSession, "m2");
      m2.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      ClientMessage m3 = createTextMessage(clientSession, "m3");
      m3.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      ClientMessage m4 = createTextMessage(clientSession, "m4");
      m4.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      ClientMessage m5 = createTextMessage(clientSession, "m5");
      m5.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      ClientMessage m6 = createTextMessage(clientSession, "m6");
      m6.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      clientSession.start();
      producer.send(m1);
      ClientMessage m = consumer.receive(1000);
      Assert.assertNotNull(m);
      Assert.assertEquals(m.getBodyBuffer().readString(), "m1");
      producer.send(m2);
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      Assert.assertEquals(m.getBodyBuffer().readString(), "m2");
      producer.send(m3);
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      Assert.assertEquals(m.getBodyBuffer().readString(), "m3");
      producer.send(m4);
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      Assert.assertEquals(m.getBodyBuffer().readString(), "m4");
      producer.send(m5);
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      Assert.assertEquals(m.getBodyBuffer().readString(), "m5");
      producer.send(m6);
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      Assert.assertEquals(m.getBodyBuffer().readString(), "m6");
      consumer.close();
      consumer = clientSession.createConsumer(qName1);
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m6");
      m = consumer.receiveImmediate();
      Assert.assertNull(m);
   }

   @Test
   public void testSimpleInTx() throws Exception
   {

      ClientProducer producer = clientSessionTxReceives.createProducer(address);
      ClientConsumer consumer = clientSessionTxReceives.createConsumer(qName1);
      ClientMessage m1 = createTextMessage(clientSession, "m1");
      String rh = new String("SMID1");
      m1.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      ClientMessage m2 = createTextMessage(clientSession, "m2");
      m2.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      producer.send(m1);
      producer.send(m2);
      clientSessionTxReceives.start();
      ClientMessage m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m2");
   }

   @Test
   public void testMultipleMessagesInTx() throws Exception
   {
      ClientProducer producer = clientSessionTxReceives.createProducer(address);
      ClientConsumer consumer = clientSessionTxReceives.createConsumer(qName1);
      String messageId1 = new String("SMID1");
      String messageId2 = new String("SMID2");
      ClientMessage m1 = createTextMessage(clientSession, "m1");
      m1.putStringProperty(Message.HDR_LAST_VALUE_NAME, messageId1);
      ClientMessage m2 = createTextMessage(clientSession, "m2");
      m2.putStringProperty(Message.HDR_LAST_VALUE_NAME, messageId2);
      ClientMessage m3 = createTextMessage(clientSession, "m3");
      m3.putStringProperty(Message.HDR_LAST_VALUE_NAME, messageId1);
      ClientMessage m4 = createTextMessage(clientSession, "m4");
      m4.putStringProperty(Message.HDR_LAST_VALUE_NAME, messageId2);
      producer.send(m1);
      producer.send(m2);
      producer.send(m3);
      producer.send(m4);
      clientSessionTxReceives.start();
      ClientMessage m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m3");
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m4");
      clientSessionTxReceives.commit();
      m = consumer.receiveImmediate();
      Assert.assertNull(m);
   }

   @Test
   public void testMultipleMessagesInTxRollback() throws Exception
   {
      ClientProducer producer = clientSessionTxReceives.createProducer(address);
      ClientConsumer consumer = clientSessionTxReceives.createConsumer(qName1);
      String messageId1 = new String("SMID1");
      String messageId2 = new String("SMID2");
      ClientMessage m1 = createTextMessage(clientSession, "m1");
      m1.putStringProperty(Message.HDR_LAST_VALUE_NAME, messageId1);
      ClientMessage m2 = createTextMessage(clientSession, "m2");
      m2.putStringProperty(Message.HDR_LAST_VALUE_NAME, messageId2);
      ClientMessage m3 = createTextMessage(clientSession, "m3");
      m3.putStringProperty(Message.HDR_LAST_VALUE_NAME, messageId1);
      ClientMessage m4 = createTextMessage(clientSession, "m4");
      m4.putStringProperty(Message.HDR_LAST_VALUE_NAME, messageId2);
      producer.send(m1);
      producer.send(m2);
      clientSessionTxReceives.start();
      ClientMessage m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m1");
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m2");
      producer.send(m3);
      producer.send(m4);
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m3");
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m4");
      clientSessionTxReceives.rollback();
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m3");
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m4");
   }

   @Test
   public void testSingleTXRollback() throws Exception
   {
      ClientProducer producer = clientSessionTxReceives.createProducer(address);
      ClientConsumer consumer = clientSessionTxReceives.createConsumer(qName1);
      String messageId1 = new String("SMID1");
      ClientMessage m1 = createTextMessage(clientSession, "m1");
      m1.putStringProperty(Message.HDR_LAST_VALUE_NAME, messageId1);
      producer.send(m1);
      clientSessionTxReceives.start();
      ClientMessage m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      clientSessionTxReceives.rollback();
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m1");
      Assert.assertNull(consumer.receiveImmediate());
   }

   @Test
   public void testMultipleMessagesInTxSend() throws Exception
   {
      ClientProducer producer = clientSessionTxSends.createProducer(address);
      ClientConsumer consumer = clientSessionTxSends.createConsumer(qName1);
      String rh = new String("SMID1");
      ClientMessage m1 = createTextMessage(clientSession, "m1");
      m1.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      ClientMessage m2 = createTextMessage(clientSession, "m2");
      m2.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      ClientMessage m3 = createTextMessage(clientSession, "m3");
      m3.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      ClientMessage m4 = createTextMessage(clientSession, "m4");
      m4.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      ClientMessage m5 = createTextMessage(clientSession, "m5");
      m5.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      ClientMessage m6 = createTextMessage(clientSession, "m6");
      m6.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      producer.send(m1);
      producer.send(m2);
      producer.send(m3);
      producer.send(m4);
      producer.send(m5);
      producer.send(m6);
      clientSessionTxSends.commit();
      clientSessionTxSends.start();
      ClientMessage m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m6");
   }

   @Test
   public void testMultipleMessagesPersistedCorrectly() throws Exception
   {
      ClientProducer producer = clientSession.createProducer(address);
      ClientConsumer consumer = clientSession.createConsumer(qName1);
      String rh = new String("SMID1");
      ClientMessage m1 = createTextMessage(clientSession, "m1");
      m1.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m1.setDurable(true);
      ClientMessage m2 = createTextMessage(clientSession, "m2");
      m2.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m2.setDurable(true);
      ClientMessage m3 = createTextMessage(clientSession, "m3");
      m3.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m3.setDurable(true);
      ClientMessage m4 = createTextMessage(clientSession, "m4");
      m4.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m4.setDurable(true);
      ClientMessage m5 = createTextMessage(clientSession, "m5");
      m5.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m5.setDurable(true);
      ClientMessage m6 = createTextMessage(clientSession, "m6");
      m6.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m6.setDurable(true);
      producer.send(m1);
      producer.send(m2);
      producer.send(m3);
      producer.send(m4);
      producer.send(m5);
      producer.send(m6);
      clientSession.start();
      ClientMessage m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m6");
      m = consumer.receiveImmediate();
      Assert.assertNull(m);
   }

   @Test
   public void testMultipleMessagesPersistedCorrectlyInTx() throws Exception
   {
      ClientProducer producer = clientSessionTxSends.createProducer(address);
      ClientConsumer consumer = clientSessionTxSends.createConsumer(qName1);
      String rh = new String("SMID1");
      ClientMessage m1 = createTextMessage(clientSession, "m1");
      m1.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m1.setDurable(true);
      ClientMessage m2 = createTextMessage(clientSession, "m2");
      m2.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m2.setDurable(true);
      ClientMessage m3 = createTextMessage(clientSession, "m3");
      m3.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m3.setDurable(true);
      ClientMessage m4 = createTextMessage(clientSession, "m4");
      m4.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m4.setDurable(true);
      ClientMessage m5 = createTextMessage(clientSession, "m5");
      m5.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m5.setDurable(true);
      ClientMessage m6 = createTextMessage(clientSession, "m6");
      m6.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m6.setDurable(true);
      producer.send(m1);
      producer.send(m2);
      producer.send(m3);
      producer.send(m4);
      producer.send(m5);
      producer.send(m6);
      clientSessionTxSends.commit();
      clientSessionTxSends.start();
      ClientMessage m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m6");
      m = consumer.receiveImmediate();
      Assert.assertNull(m);
   }

   @Test
   public void testMultipleAcksPersistedCorrectly() throws Exception
   {

      Queue queue = server.locateQueue(qName1);
      ClientProducer producer = clientSession.createProducer(address);
      ClientConsumer consumer = clientSession.createConsumer(qName1);
      String rh = new String("SMID1");
      ClientMessage m1 = createTextMessage(clientSession, "m1");
      m1.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m1.setDurable(true);
      ClientMessage m2 = createTextMessage(clientSession, "m2");
      m2.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m2.setDurable(true);
      ClientMessage m3 = createTextMessage(clientSession, "m3");
      m3.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m3.setDurable(true);
      ClientMessage m4 = createTextMessage(clientSession, "m4");
      m4.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m4.setDurable(true);
      ClientMessage m5 = createTextMessage(clientSession, "m5");
      m5.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m5.setDurable(true);
      ClientMessage m6 = createTextMessage(clientSession, "m6");
      m6.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m6.setDurable(true);
      clientSession.start();
      producer.send(m1);
      ClientMessage m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m1");
      producer.send(m2);
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m2");
      producer.send(m3);
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m3");
      producer.send(m4);
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m4");
      producer.send(m5);
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m5");
      producer.send(m6);
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m6");

      assertEquals(0, queue.getDeliveringCount());
   }

   @Test
   public void testRemoveMessageThroughManagement() throws Exception
   {

      Queue queue = server.locateQueue(qName1);
      ClientProducer producer = clientSession.createProducer(address);
      ClientConsumer consumer = clientSession.createConsumer(qName1);
      String rh = new String("SMID1");
      ClientMessage m1 = createTextMessage(clientSession, "m1");
      m1.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m1.setDurable(true);
      producer.send(m1);

      queue.deleteAllReferences();

      producer.send(m1);

      clientSession.start();
      ClientMessage m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m1");

      assertEquals(0, queue.getDeliveringCount());
   }

   @Test
   public void testMultipleAcksPersistedCorrectly2() throws Exception
   {

      Queue queue = server.locateQueue(qName1);
      ClientProducer producer = clientSession.createProducer(address);
      ClientConsumer consumer = clientSession.createConsumer(qName1);
      String rh = new String("SMID1");
      ClientMessage m1 = createTextMessage(clientSession, "m1");
      m1.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m1.setDurable(true);
      ClientMessage m2 = createTextMessage(clientSession, "m2");
      m2.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m2.setDurable(true);
      clientSession.start();
      producer.send(m1);
      ClientMessage m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m1");
      producer.send(m2);
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m2");

      assertEquals(0, queue.getDeliveringCount());
   }

   @Test
   public void testMultipleAcksPersistedCorrectlyInTx() throws Exception
   {
      ClientProducer producer = clientSessionTxReceives.createProducer(address);
      ClientConsumer consumer = clientSessionTxReceives.createConsumer(qName1);
      String rh = new String("SMID1");
      ClientMessage m1 = createTextMessage(clientSession, "m1");
      m1.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m1.setDurable(true);
      ClientMessage m2 = createTextMessage(clientSession, "m2");
      m2.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m2.setDurable(true);
      ClientMessage m3 = createTextMessage(clientSession, "m3");
      m3.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m3.setDurable(true);
      ClientMessage m4 = createTextMessage(clientSession, "m4");
      m4.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m4.setDurable(true);
      ClientMessage m5 = createTextMessage(clientSession, "m5");
      m5.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m5.setDurable(true);
      ClientMessage m6 = createTextMessage(clientSession, "m6");
      m6.putStringProperty(Message.HDR_LAST_VALUE_NAME, rh);
      m6.setDurable(true);
      clientSessionTxReceives.start();
      producer.send(m1);
      ClientMessage m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m1");
      producer.send(m2);
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m2");
      producer.send(m3);
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m3");
      producer.send(m4);
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m4");
      producer.send(m5);
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m5");
      producer.send(m6);
      m = consumer.receive(1000);
      Assert.assertNotNull(m);
      m.acknowledge();
      Assert.assertEquals(m.getBodyBuffer().readString(), "m6");
      clientSessionTxReceives.commit();
   }

   @Override
   @After
   public void tearDown() throws Exception
   {
      if (clientSession != null)
      {
         try
         {
            clientSession.close();
         }
         catch (HornetQException e1)
         {
            //
         }
      }

      if (clientSessionTxReceives != null)
      {
         try
         {
            clientSessionTxReceives.close();
         }
         catch (HornetQException e1)
         {
            //
         }
      }

      if (clientSessionTxSends != null)
      {
         try
         {
            clientSessionTxSends.close();
         }
         catch (HornetQException e1)
         {
            //
         }
      }
      if (server != null && server.isStarted())
      {
         try
         {
            server.stop();
         }
         catch (Exception e1)
         {
            //
         }
      }
      server = null;
      clientSession = null;

      super.tearDown();
   }

   @Override
   @Before
   public void setUp() throws Exception
   {
      super.setUp();

      ConfigurationImpl configuration = createBasicConfig();
      configuration.setSecurityEnabled(false);
      TransportConfiguration transportConfig = new TransportConfiguration(UnitTestCase.INVM_ACCEPTOR_FACTORY);
      configuration.getAcceptorConfigurations().add(transportConfig);
      server = HornetQServers.newHornetQServer(configuration, false);
      // start the server
      server.start();

      AddressSettings qs = new AddressSettings();
      qs.setLastValueQueue(true);
      server.getAddressSettingsRepository().addMatch(address.toString(), qs);
      // then we create a client as normalServer
      ServerLocator locator = HornetQClient.createServerLocatorWithoutHA(new TransportConfiguration(UnitTestCase.INVM_CONNECTOR_FACTORY));
      locator.setBlockOnAcknowledge(true);
      locator.setAckBatchSize(0);
      ClientSessionFactory sessionFactory = createSessionFactory(locator);
      clientSession = sessionFactory.createSession(false, true, true);
      clientSessionTxReceives = sessionFactory.createSession(false, true, false);
      clientSessionTxSends = sessionFactory.createSession(false, false, true);
      clientSession.createQueue(address, qName1, null, true);
   }
}
