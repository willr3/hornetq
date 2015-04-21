/*
 * Copyright 2009 Red Hat, Inc.
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
package org.hornetq.tests.integration.client;
import org.junit.Before;

import org.junit.Test;

import org.junit.Assert;


import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.Queue;
import org.hornetq.tests.util.ServiceTestBase;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 * @author <a href="mailto:clebert.suconic@jboss.org">Clebert Suconic</a>
 */
public class QueueBrowserTest extends ServiceTestBase
{
   private HornetQServer server;

   private final String QUEUE = new String("ConsumerTestQueue");

   private ServerLocator locator;

   @Override
   @Before
   public void setUp() throws Exception
   {
      super.setUp();

      server = createServer(false);

      server.start();

      locator = createInVMNonHALocator();
   }

   private ClientSessionFactory sf;

   @Test
   public void testSimpleConsumerBrowser() throws Exception
   {
      locator.setBlockOnNonDurableSend(true);

      sf = createSessionFactory(locator);
      ClientSession session = sf.createSession(false, true, true);

      session.createQueue(QUEUE, QUEUE, null, false);

      ClientProducer producer = session.createProducer(QUEUE);

      final int numMessages = 100;

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = createTextMessage(session, "m" + i);
         producer.send(message);
      }

      ClientConsumer consumer = session.createConsumer(QUEUE, null, true);

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message2 = consumer.receive(1000);

         Assert.assertEquals("m" + i, message2.getBodyBuffer().readString());
      }

      consumer.close();

      consumer = session.createConsumer(QUEUE, null, true);

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message2 = consumer.receive(1000);

         Assert.assertEquals("m" + i, message2.getBodyBuffer().readString());
      }

      consumer.close();

      session.close();


   }

   @Test
   public void testConsumerBrowserWithSelector() throws Exception
   {

      ClientSessionFactory sf = createSessionFactory(locator);

      ClientSession session = sf.createSession(false, true, true);

      session.createQueue(QUEUE, QUEUE, null, false);

      ClientProducer producer = session.createProducer(QUEUE);

      final int numMessages = 100;

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = createTextMessage(session, "m" + i);
         message.putIntProperty(new String("x"), i);
         producer.send(message);
      }

      ClientConsumer consumer = session.createConsumer(QUEUE, new String("x >= 50"), true);

      for (int i = 50; i < numMessages; i++)
      {
         ClientMessage message2 = consumer.receive(1000);

         Assert.assertEquals("m" + i, message2.getBodyBuffer().readString());
      }

      consumer.close();

      consumer = session.createConsumer(QUEUE, null, true);

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message2 = consumer.receive(1000);

         Assert.assertEquals("m" + i, message2.getBodyBuffer().readString());
      }

      consumer.close();

      session.close();
   }

   @Test
   public void testConsumerBrowserWithStringSelector() throws Exception
   {

      ClientSessionFactory sf = createSessionFactory(locator);

      ClientSession session = sf.createSession(false, true, true);

      session.createQueue(QUEUE, QUEUE, null, false);

      ClientProducer producer = session.createProducer(QUEUE);

      final int numMessages = 100;

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = createTextMessage(session, "m" + i);
         if (i % 2 == 0)
         {
            message.putStringProperty(new String("color"), new String("RED"));
         }
         producer.send(message);
      }

      ClientConsumer consumer = session.createConsumer(QUEUE, new String("color = 'RED'"), true);

      for (int i = 0; i < numMessages; i += 2)
      {
         ClientMessage message2 = consumer.receive(1000);

         Assert.assertEquals("m" + i, message2.getBodyBuffer().readString());
      }

      session.close();


   }

   @Test
   public void testConsumerMultipleBrowser() throws Exception
   {

      ClientSessionFactory sf = createSessionFactory(locator);

      ClientSession session = sf.createSession(false, true, true);

      session.createQueue(QUEUE, QUEUE, null, false);

      ClientProducer producer = session.createProducer(QUEUE);

      final int numMessages = 100;

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = createTextMessage(session, "m" + i);
         producer.send(message);
      }

      ClientConsumer consumer = session.createConsumer(QUEUE, null, true);
      ClientConsumer consumer2 = session.createConsumer(QUEUE, null, true);
      ClientConsumer consumer3 = session.createConsumer(QUEUE, null, true);

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message2 = consumer.receive(1000);
         Assert.assertEquals("m" + i, message2.getBodyBuffer().readString());
         message2 = consumer2.receive(1000);
         Assert.assertEquals("m" + i, message2.getBodyBuffer().readString());
         message2 = consumer3.receive(1000);
         Assert.assertEquals("m" + i, message2.getBodyBuffer().readString());
      }

      session.close();

   }

   @Test
   public void testConsumerMultipleBrowserWithSelector() throws Exception
   {

      ClientSessionFactory sf = createSessionFactory(locator);

      ClientSession session = sf.createSession(false, true, true);

      session.createQueue(QUEUE, QUEUE, null, false);

      ClientProducer producer = session.createProducer(QUEUE);

      final int numMessages = 100;

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = createTextMessage(session, "m" + i);
         message.putIntProperty(new String("x"), i);
         producer.send(message);
      }

      ClientConsumer consumer = session.createConsumer(QUEUE, new String("x < 50"), true);
      ClientConsumer consumer2 = session.createConsumer(QUEUE, new String("x >= 50"), true);
      ClientConsumer consumer3 = session.createConsumer(QUEUE, null, true);

      for (int i = 0; i < 50; i++)
      {
         ClientMessage message2 = consumer.receive(1000);
         Assert.assertEquals("m" + i, message2.getBodyBuffer().readString());
      }
      for (int i = 50; i < numMessages; i++)
      {
         ClientMessage message2 = consumer2.receive(1000);
         Assert.assertEquals("m" + i, message2.getBodyBuffer().readString());
      }
      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message2 = consumer3.receive(1000);
         Assert.assertEquals("m" + i, message2.getBodyBuffer().readString());
      }

      session.close();


   }

   @Test
   public void testConsumerBrowserMessages() throws Exception
   {
      testConsumerBrowserMessagesArentAcked(false);
   }

   @Test
   public void testConsumerBrowserMessagesPreACK() throws Exception
   {
      testConsumerBrowserMessagesArentAcked(false);
   }

   private void testConsumerBrowserMessagesArentAcked(final boolean preACK) throws Exception
   {
      ClientSessionFactory sf = createSessionFactory(locator);

      ClientSession session = sf.createSession(null, null, false, true, true, preACK, 0);

      session.createQueue(QUEUE, QUEUE, null, false);

      ClientProducer producer = session.createProducer(QUEUE);

      final int numMessages = 100;

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = createTextMessage(session, "m" + i);
         producer.send(message);
      }

      ClientConsumer consumer = session.createConsumer(QUEUE, null, true);

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message2 = consumer.receive(1000);

         Assert.assertEquals("m" + i, message2.getBodyBuffer().readString());
      }
      // assert that all the messages are there and none have been acked
      Assert.assertEquals(0, ((Queue)server.getPostOffice().getBinding(QUEUE).getBindable()).getDeliveringCount());
      Assert.assertEquals(100, ((Queue)server.getPostOffice().getBinding(QUEUE).getBindable()).getMessageCount());

      session.close();

   }

   @Test
   public void testConsumerBrowserMessageAckDoesNothing() throws Exception
   {
      ClientSessionFactory sf = createSessionFactory(locator);

      ClientSession session = sf.createSession(false, true, true);

      session.createQueue(QUEUE, QUEUE, null, false);

      ClientProducer producer = session.createProducer(QUEUE);

      final int numMessages = 100;

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = createTextMessage(session, "m" + i);
         producer.send(message);
      }

      ClientConsumer consumer = session.createConsumer(QUEUE, null, true);

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message2 = consumer.receive(1000);

         message2.acknowledge();

         Assert.assertEquals("m" + i, message2.getBodyBuffer().readString());
      }
      // assert that all the messages are there and none have been acked
      Assert.assertEquals(0, ((Queue)server.getPostOffice().getBinding(QUEUE).getBindable()).getDeliveringCount());
      Assert.assertEquals(100, ((Queue)server.getPostOffice().getBinding(QUEUE).getBindable()).getMessageCount());

      session.close();

   }

   @Test
   public void testBrowseWithZeroConsumerWindowSize() throws Exception
   {
      locator.setConsumerWindowSize(0);

      ClientSessionFactory sf = createSessionFactory(locator);

      ClientSession session = sf.createSession(false, true, true);

      session.createQueue(QUEUE, QUEUE, null, false);

      ClientProducer producer = session.createProducer(QUEUE);

      final int numMessages = 100;

      byte[] bytes = new byte[240];

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = session.createMessage(false);

         message.getBodyBuffer().writeBytes(bytes);

         message.putIntProperty("foo", i);

         producer.send(message);
      }

      //Create a normal non browsing consumer
      session.createConsumer(QUEUE);

      session.start();

      ClientConsumer browser = session.createConsumer(QUEUE, true);

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message2 = browser.receive(1000);

         assertEquals(i, message2.getIntProperty("foo").intValue());
      }

      session.close();
   }

}
