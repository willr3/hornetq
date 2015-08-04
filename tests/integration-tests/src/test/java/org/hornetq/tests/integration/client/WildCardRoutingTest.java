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
package org.hornetq.tests.integration.client;
import org.junit.Before;
import org.junit.After;

import org.junit.Test;

import org.junit.Assert;

import org.hornetq.api.core.HornetQException;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.HornetQClient;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.HornetQServers;
import org.hornetq.tests.util.UnitTestCase;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 */
public class WildCardRoutingTest extends UnitTestCase
{
   private HornetQServer server;
   private ServerLocator locator;
   private ClientSession clientSession;
   private ClientSessionFactory sessionFactory;

   @Test
   public void testBasicWildcardRouting() throws Exception
   {
      String addressAB = new String("a.b");
      String addressAC = new String("a.c");
      String address = new String("a.*");
      String queueName1 = new String("Q1");
      String queueName2 = new String("Q2");
      String queueName = new String("Q");
      clientSession.createQueue(addressAB, queueName1, null, false);
      clientSession.createQueue(addressAC, queueName2, null, false);
      clientSession.createQueue(address, queueName, null, false);
      ClientProducer producer = clientSession.createProducer(addressAB);
      ClientProducer producer2 = clientSession.createProducer(addressAC);
      ClientConsumer clientConsumer = clientSession.createConsumer(queueName);
      clientSession.start();
      producer.send(createTextMessage(clientSession, "m1"));
      producer2.send(createTextMessage(clientSession, "m2"));
      ClientMessage m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m1", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m2", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receiveImmediate();
      Assert.assertNull(m);
   }

   @Test
   public void testBasicWildcardRoutingQueuesDontExist() throws Exception
   {
      String addressAB = new String("a.b");
      String addressAC = new String("a.c");
      String address = new String("a.*");
      String queueName = new String("Q");
      clientSession.createQueue(address, queueName, null, false);
      ClientProducer producer = clientSession.createProducer(addressAB);
      ClientProducer producer2 = clientSession.createProducer(addressAC);
      ClientConsumer clientConsumer = clientSession.createConsumer(queueName);
      clientSession.start();
      producer.send(createTextMessage(clientSession, "m1"));
      producer2.send(createTextMessage(clientSession, "m2"));
      ClientMessage m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m1", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m2", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receiveImmediate();
      Assert.assertNull(m);
      clientConsumer.close();
      clientSession.deleteQueue(queueName);

      Assert.assertEquals(0, server.getPostOffice().getBindingsForAddress(addressAB).getBindings().size());
      Assert.assertEquals(0, server.getPostOffice().getBindingsForAddress(addressAC).getBindings().size());
      Assert.assertEquals(0, server.getPostOffice().getBindingsForAddress(address).getBindings().size());
   }

   @Test
   public void testBasicWildcardRoutingQueuesDontExist2() throws Exception
   {
      String addressAB = new String("a.b");
      String addressAC = new String("a.c");
      String address = new String("a.*");
      String queueName = new String("Q");
      String queueName2 = new String("Q2");
      clientSession.createQueue(address, queueName, null, false);
      clientSession.createQueue(address, queueName2, null, false);
      ClientProducer producer = clientSession.createProducer(addressAB);
      ClientProducer producer2 = clientSession.createProducer(addressAC);
      ClientConsumer clientConsumer = clientSession.createConsumer(queueName);
      clientSession.start();
      producer.send(createTextMessage(clientSession, "m1"));
      producer2.send(createTextMessage(clientSession, "m2"));
      ClientMessage m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m1", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m2", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receiveImmediate();
      Assert.assertNull(m);
      clientConsumer.close();
      clientSession.deleteQueue(queueName);

      Assert.assertEquals(1, server.getPostOffice().getBindingsForAddress(addressAB).getBindings().size());
      Assert.assertEquals(1, server.getPostOffice().getBindingsForAddress(addressAC).getBindings().size());
      Assert.assertEquals(1, server.getPostOffice().getBindingsForAddress(address).getBindings().size());

      clientSession.deleteQueue(queueName2);

      Assert.assertEquals(0, server.getPostOffice().getBindingsForAddress(addressAB).getBindings().size());
      Assert.assertEquals(0, server.getPostOffice().getBindingsForAddress(addressAC).getBindings().size());
      Assert.assertEquals(0, server.getPostOffice().getBindingsForAddress(address).getBindings().size());
   }

   @Test
   public void testBasicWildcardRoutingWithHash() throws Exception
   {
      String addressAB = new String("a.b");
      String addressAC = new String("a.c");
      String address = new String("a.#");
      String queueName1 = new String("Q1");
      String queueName2 = new String("Q2");
      String queueName = new String("Q");
      clientSession.createQueue(addressAB, queueName1, null, false);
      clientSession.createQueue(addressAC, queueName2, null, false);
      clientSession.createQueue(address, queueName, null, false);
      ClientProducer producer = clientSession.createProducer(addressAB);
      ClientProducer producer2 = clientSession.createProducer(addressAC);
      ClientConsumer clientConsumer = clientSession.createConsumer(queueName);
      clientSession.start();
      producer.send(createTextMessage(clientSession, "m1"));
      producer2.send(createTextMessage(clientSession, "m2"));
      ClientMessage m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m1", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m2", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receiveImmediate();
      Assert.assertNull(m);
   }

   @Test
   public void testWildcardRoutingQueuesAddedAfter() throws Exception
   {
      String addressAB = new String("a.b");
      String addressAC = new String("a.c");
      String address = new String("a.*");
      String queueName1 = new String("Q1");
      String queueName2 = new String("Q2");
      String queueName = new String("Q");
      clientSession.createQueue(address, queueName, null, false);
      ClientProducer producer = clientSession.createProducer(addressAB);
      ClientProducer producer2 = clientSession.createProducer(addressAC);
      ClientConsumer clientConsumer = clientSession.createConsumer(queueName);
      clientSession.createQueue(addressAB, queueName1, null, false);
      clientSession.createQueue(addressAC, queueName2, null, false);
      clientSession.start();
      producer.send(createTextMessage(clientSession, "m1"));
      producer2.send(createTextMessage(clientSession, "m2"));
      ClientMessage m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m1", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m2", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receiveImmediate();
      Assert.assertNull(m);
   }

   @Test
   public void testWildcardRoutingQueuesAddedThenDeleted() throws Exception
   {
      String addressAB = new String("a.b");
      String addressAC = new String("a.c");
      String address = new String("a.*");
      String queueName1 = new String("Q1");
      String queueName2 = new String("Q2");
      String queueName = new String("Q");
      clientSession.createQueue(addressAB, queueName1, null, false);
      clientSession.createQueue(addressAC, queueName2, null, false);
      clientSession.createQueue(address, queueName, null, false);
      ClientProducer producer = clientSession.createProducer(addressAB);
      ClientProducer producer2 = clientSession.createProducer(addressAC);
      ClientConsumer clientConsumer = clientSession.createConsumer(queueName);
      clientSession.start();
      clientSession.deleteQueue(queueName1);
      // the wildcard binding should still exist
      Assert.assertEquals(server.getPostOffice().getBindingsForAddress(addressAB).getBindings().size(), 1);
      producer.send(createTextMessage(clientSession, "m1"));
      producer2.send(createTextMessage(clientSession, "m2"));
      ClientMessage m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m1", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m2", m.getBodyBuffer().readString());
      m.acknowledge();
      clientConsumer.close();
      clientSession.deleteQueue(queueName);
      Assert.assertEquals(server.getPostOffice().getBindingsForAddress(addressAB).getBindings().size(), 0);
   }

   @Test
   public void testWildcardRoutingLotsOfQueuesAddedThenDeleted() throws Exception
   {
      String addressAB = new String("a.b");
      String addressAC = new String("a.c");
      String addressAD = new String("a.d");
      String addressAE = new String("a.e");
      String addressAF = new String("a.f");
      String addressAG = new String("a.g");
      String addressAH = new String("a.h");
      String addressAJ = new String("a.j");
      String addressAK = new String("a.k");
      String address = new String("a.*");
      String queueName1 = new String("Q1");
      String queueName2 = new String("Q2");
      String queueName3 = new String("Q3");
      String queueName4 = new String("Q4");
      String queueName5 = new String("Q5");
      String queueName6 = new String("Q6");
      String queueName7 = new String("Q7");
      String queueName8 = new String("Q8");
      String queueName9 = new String("Q9");
      String queueName = new String("Q");
      clientSession.createQueue(addressAB, queueName1, null, false);
      clientSession.createQueue(addressAC, queueName2, null, false);
      clientSession.createQueue(addressAD, queueName3, null, false);
      clientSession.createQueue(addressAE, queueName4, null, false);
      clientSession.createQueue(addressAF, queueName5, null, false);
      clientSession.createQueue(addressAG, queueName6, null, false);
      clientSession.createQueue(addressAH, queueName7, null, false);
      clientSession.createQueue(addressAJ, queueName8, null, false);
      clientSession.createQueue(addressAK, queueName9, null, false);
      clientSession.createQueue(address, queueName, null, false);
      ClientProducer producer = clientSession.createProducer();
      ClientConsumer clientConsumer = clientSession.createConsumer(queueName);
      clientSession.start();
      producer.send(addressAB, createTextMessage(clientSession, "m1"));
      producer.send(addressAC, createTextMessage(clientSession, "m2"));
      producer.send(addressAD, createTextMessage(clientSession, "m3"));
      producer.send(addressAE, createTextMessage(clientSession, "m4"));
      producer.send(addressAF, createTextMessage(clientSession, "m5"));
      producer.send(addressAG, createTextMessage(clientSession, "m6"));
      producer.send(addressAH, createTextMessage(clientSession, "m7"));
      producer.send(addressAJ, createTextMessage(clientSession, "m8"));
      producer.send(addressAK, createTextMessage(clientSession, "m9"));

      ClientMessage m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m1", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m2", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m3", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m4", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m5", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m6", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m7", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m8", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m9", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receiveImmediate();
      Assert.assertNull(m);
      // now remove all the queues
      clientSession.deleteQueue(queueName1);
      clientSession.deleteQueue(queueName2);
      clientSession.deleteQueue(queueName3);
      clientSession.deleteQueue(queueName4);
      clientSession.deleteQueue(queueName5);
      clientSession.deleteQueue(queueName6);
      clientSession.deleteQueue(queueName7);
      clientSession.deleteQueue(queueName8);
      clientSession.deleteQueue(queueName9);
      clientConsumer.close();
      clientSession.deleteQueue(queueName);
   }

   @Test
   public void testWildcardRoutingLotsOfQueuesAddedThenDeletedHash() throws Exception
   {
      String addressAB = new String("a.b");
      String addressAC = new String("a.c");
      String addressAD = new String("a.d");
      String addressAE = new String("a.e");
      String addressAF = new String("a.f");
      String addressAG = new String("a.g");
      String addressAH = new String("a.h");
      String addressAJ = new String("a.j");
      String addressAK = new String("a.k");
      String address = new String("#");
      String queueName1 = new String("Q1");
      String queueName2 = new String("Q2");
      String queueName3 = new String("Q3");
      String queueName4 = new String("Q4");
      String queueName5 = new String("Q5");
      String queueName6 = new String("Q6");
      String queueName7 = new String("Q7");
      String queueName8 = new String("Q8");
      String queueName9 = new String("Q9");
      String queueName = new String("Q");
      clientSession.createQueue(addressAB, queueName1, null, false);
      clientSession.createQueue(addressAC, queueName2, null, false);
      clientSession.createQueue(addressAD, queueName3, null, false);
      clientSession.createQueue(addressAE, queueName4, null, false);
      clientSession.createQueue(addressAF, queueName5, null, false);
      clientSession.createQueue(addressAG, queueName6, null, false);
      clientSession.createQueue(addressAH, queueName7, null, false);
      clientSession.createQueue(addressAJ, queueName8, null, false);
      clientSession.createQueue(addressAK, queueName9, null, false);
      clientSession.createQueue(address, queueName, null, false);
      ClientProducer producer = clientSession.createProducer();
      ClientConsumer clientConsumer = clientSession.createConsumer(queueName);
      clientSession.start();
      producer.send(addressAB, createTextMessage(clientSession, "m1"));
      producer.send(addressAC, createTextMessage(clientSession, "m2"));
      producer.send(addressAD, createTextMessage(clientSession, "m3"));
      producer.send(addressAE, createTextMessage(clientSession, "m4"));
      producer.send(addressAF, createTextMessage(clientSession, "m5"));
      producer.send(addressAG, createTextMessage(clientSession, "m6"));
      producer.send(addressAH, createTextMessage(clientSession, "m7"));
      producer.send(addressAJ, createTextMessage(clientSession, "m8"));
      producer.send(addressAK, createTextMessage(clientSession, "m9"));

      ClientMessage m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m1", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m2", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m3", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m4", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m5", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m6", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m7", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m8", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m9", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receiveImmediate();
      Assert.assertNull(m);
      // now remove all the queues
      clientSession.deleteQueue(queueName1);
      clientSession.deleteQueue(queueName2);
      clientSession.deleteQueue(queueName3);
      clientSession.deleteQueue(queueName4);
      clientSession.deleteQueue(queueName5);
      clientSession.deleteQueue(queueName6);
      clientSession.deleteQueue(queueName7);
      clientSession.deleteQueue(queueName8);
      clientSession.deleteQueue(queueName9);
      clientConsumer.close();
      clientSession.deleteQueue(queueName);
   }

   @Test
   public void testWildcardRoutingWithSingleHash() throws Exception
   {
      String addressAB = new String("a.b");
      String addressAC = new String("a.c");
      String address = new String("#");
      String queueName1 = new String("Q1");
      String queueName2 = new String("Q2");
      String queueName = new String("Q");
      clientSession.createQueue(addressAB, queueName1, null, false);
      clientSession.createQueue(addressAC, queueName2, null, false);
      clientSession.createQueue(address, queueName, null, false);
      ClientProducer producer = clientSession.createProducer(addressAB);
      ClientProducer producer2 = clientSession.createProducer(addressAC);
      ClientConsumer clientConsumer = clientSession.createConsumer(queueName);
      clientSession.start();
      producer.send(createTextMessage(clientSession, "m1"));
      producer2.send(createTextMessage(clientSession, "m2"));
      ClientMessage m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m1", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m2", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receiveImmediate();
      Assert.assertNull(m);
   }

   @Test
   public void testWildcardRoutingWithHash() throws Exception
   {
      String addressAB = new String("a.b.f");
      String addressAC = new String("a.c.f");
      String address = new String("a.#.f");
      String queueName1 = new String("Q1");
      String queueName2 = new String("Q2");
      String queueName = new String("Q");
      clientSession.createQueue(addressAB, queueName1, null, false);
      clientSession.createQueue(addressAC, queueName2, null, false);
      clientSession.createQueue(address, queueName, null, false);
      ClientProducer producer = clientSession.createProducer(addressAB);
      ClientProducer producer2 = clientSession.createProducer(addressAC);
      ClientConsumer clientConsumer = clientSession.createConsumer(queueName);
      clientSession.start();
      producer.send(createTextMessage(clientSession, "m1"));
      producer2.send(createTextMessage(clientSession, "m2"));
      ClientMessage m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m1", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m2", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receiveImmediate();
      Assert.assertNull(m);
   }

   @Test
   public void testWildcardRoutingWithHashMultiLengthAddresses() throws Exception
   {
      String addressAB = new String("a.b.c.f");
      String addressAC = new String("a.c.f");
      String addressAD = new String("a.d");
      String address = new String("a.#.f");
      String queueName1 = new String("Q1");
      String queueName2 = new String("Q2");
      String queueName = new String("Q");
      clientSession.createQueue(addressAB, queueName1, null, false);
      clientSession.createQueue(addressAC, queueName2, null, false);
      clientSession.createQueue(address, queueName, null, false);
      ClientProducer producer = clientSession.createProducer(addressAB);
      ClientProducer producer2 = clientSession.createProducer(addressAC);
      ClientConsumer clientConsumer = clientSession.createConsumer(queueName);
      clientSession.start();
      producer.send(createTextMessage(clientSession, "m1"));
      producer2.send(createTextMessage(clientSession, "m2"));
      ClientMessage m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m1", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m2", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receiveImmediate();
      Assert.assertNull(m);
   }

   @Test
   public void testWildcardRoutingWithDoubleStar() throws Exception
   {
      String addressAB = new String("a.b");
      String addressAC = new String("a.c");
      String address = new String("*.*");
      String queueName1 = new String("Q1");
      String queueName2 = new String("Q2");
      String queueName = new String("Q");
      clientSession.createQueue(addressAB, queueName1, null, false);
      clientSession.createQueue(addressAC, queueName2, null, false);
      clientSession.createQueue(address, queueName, null, false);
      ClientProducer producer = clientSession.createProducer(addressAB);
      ClientProducer producer2 = clientSession.createProducer(addressAC);
      ClientConsumer clientConsumer = clientSession.createConsumer(queueName);
      clientSession.start();
      producer.send(createTextMessage(clientSession, "m1"));
      producer2.send(createTextMessage(clientSession, "m2"));
      ClientMessage m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m1", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m2", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receiveImmediate();
      Assert.assertNull(m);
   }

   @Test
   public void testWildcardRoutingPartialMatchStar() throws Exception
   {
      String addressAB = new String("a.b");
      String addressAC = new String("a.c");
      String address = new String("*.b");
      String queueName1 = new String("Q1");
      String queueName2 = new String("Q2");
      String queueName = new String("Q");
      clientSession.createQueue(addressAB, queueName1, null, false);
      clientSession.createQueue(addressAC, queueName2, null, false);
      clientSession.createQueue(address, queueName, null, false);
      ClientProducer producer = clientSession.createProducer(addressAB);
      ClientProducer producer2 = clientSession.createProducer(addressAC);
      ClientConsumer clientConsumer = clientSession.createConsumer(queueName);
      clientSession.start();
      producer.send(createTextMessage(clientSession, "m1"));
      producer2.send(createTextMessage(clientSession, "m2"));
      ClientMessage m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m1", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receiveImmediate();
      Assert.assertNull(m);
   }

   @Test
   public void testWildcardRoutingVariableLengths() throws Exception
   {
      String addressAB = new String("a.b.c");
      String addressAC = new String("a.c");
      String address = new String("a.#");
      String queueName1 = new String("Q1");
      String queueName2 = new String("Q2");
      String queueName = new String("Q");
      clientSession.createQueue(addressAB, queueName1, null, false);
      clientSession.createQueue(addressAC, queueName2, null, false);
      clientSession.createQueue(address, queueName, null, false);
      ClientProducer producer = clientSession.createProducer(addressAB);
      ClientProducer producer2 = clientSession.createProducer(addressAC);
      ClientConsumer clientConsumer = clientSession.createConsumer(queueName);
      clientSession.start();
      producer.send(createTextMessage(clientSession, "m1"));
      producer2.send(createTextMessage(clientSession, "m2"));
      ClientMessage m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m1", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m2", m.getBodyBuffer().readString());
      m.acknowledge();
   }

   @Test
   public void testWildcardRoutingVariableLengthsStar() throws Exception
   {
      String addressAB = new String("a.b.c");
      String addressAC = new String("a.c");
      String address = new String("a.*");
      String queueName1 = new String("Q1");
      String queueName2 = new String("Q2");
      String queueName = new String("Q");
      clientSession.createQueue(addressAB, queueName1, null, false);
      clientSession.createQueue(addressAC, queueName2, null, false);
      clientSession.createQueue(address, queueName, null, false);
      ClientProducer producer = clientSession.createProducer(addressAB);
      ClientProducer producer2 = clientSession.createProducer(addressAC);
      ClientConsumer clientConsumer = clientSession.createConsumer(queueName);
      clientSession.start();
      producer.send(createTextMessage(clientSession, "m1"));
      producer2.send(createTextMessage(clientSession, "m2"));
      ClientMessage m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m2", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receiveImmediate();
      Assert.assertNull(m);
   }

   @Test
   public void testWildcardRoutingMultipleStars() throws Exception
   {
      String addressAB = new String("a.b.c");
      String addressAC = new String("a.c");
      String address = new String("*.*");
      String queueName1 = new String("Q1");
      String queueName2 = new String("Q2");
      String queueName = new String("Q");
      clientSession.createQueue(addressAB, queueName1, null, false);
      clientSession.createQueue(addressAC, queueName2, null, false);
      clientSession.createQueue(address, queueName, null, false);
      ClientProducer producer = clientSession.createProducer(addressAB);
      ClientProducer producer2 = clientSession.createProducer(addressAC);
      ClientConsumer clientConsumer = clientSession.createConsumer(queueName);
      clientSession.start();
      producer.send(createTextMessage(clientSession, "m1"));
      producer2.send(createTextMessage(clientSession, "m2"));
      ClientMessage m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m2", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receiveImmediate();
      Assert.assertNull(m);
   }

   @Test
   public void testWildcardRoutingStarInMiddle() throws Exception
   {
      String addressAB = new String("a.b.c");
      String addressAC = new String("a.c");
      String address = new String("*.b.*");
      String queueName1 = new String("Q1");
      String queueName2 = new String("Q2");
      String queueName = new String("Q");
      clientSession.createQueue(addressAB, queueName1, null, false);
      clientSession.createQueue(addressAC, queueName2, null, false);
      clientSession.createQueue(address, queueName, null, false);
      ClientProducer producer = clientSession.createProducer(addressAB);
      ClientProducer producer2 = clientSession.createProducer(addressAC);
      ClientConsumer clientConsumer = clientSession.createConsumer(queueName);
      clientSession.start();
      producer.send(createTextMessage(clientSession, "m1"));
      producer2.send(createTextMessage(clientSession, "m2"));
      ClientMessage m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m1", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receiveImmediate();
      Assert.assertNull(m);
   }

   @Test
   public void testWildcardRoutingStarAndHash() throws Exception
   {
      String addressAB = new String("a.b.c.d");
      String addressAC = new String("a.c");
      String address = new String("*.b.#");
      String queueName1 = new String("Q1");
      String queueName2 = new String("Q2");
      String queueName = new String("Q");
      clientSession.createQueue(addressAB, queueName1, null, false);
      clientSession.createQueue(addressAC, queueName2, null, false);
      clientSession.createQueue(address, queueName, null, false);
      ClientProducer producer = clientSession.createProducer(addressAB);
      ClientProducer producer2 = clientSession.createProducer(addressAC);
      ClientConsumer clientConsumer = clientSession.createConsumer(queueName);
      clientSession.start();
      producer.send(createTextMessage(clientSession, "m1"));
      producer2.send(createTextMessage(clientSession, "m2"));
      ClientMessage m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m1", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receiveImmediate();
      Assert.assertNull(m);
   }

   @Test
   public void testWildcardRoutingHashAndStar() throws Exception
   {
      String addressAB = new String("a.b.c");
      String addressAC = new String("a.c");
      String address = new String("#.b.*");
      String queueName1 = new String("Q1");
      String queueName2 = new String("Q2");
      String queueName = new String("Q");
      clientSession.createQueue(addressAB, queueName1, null, false);
      clientSession.createQueue(addressAC, queueName2, null, false);
      clientSession.createQueue(address, queueName, null, false);
      ClientProducer producer = clientSession.createProducer(addressAB);
      ClientProducer producer2 = clientSession.createProducer(addressAC);
      ClientConsumer clientConsumer = clientSession.createConsumer(queueName);
      clientSession.start();
      producer.send(createTextMessage(clientSession, "m1"));
      producer2.send(createTextMessage(clientSession, "m2"));
      ClientMessage m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m1", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receiveImmediate();
      Assert.assertNull(m);
   }

   @Test
   public void testLargeWildcardRouting() throws Exception
   {
      String addressAB = new String("a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p.q.r.s.t.u.v.w.x.y.z");
      String addressAC = new String("a.c");
      String address = new String("a.#");
      String queueName1 = new String("Q1");
      String queueName2 = new String("Q2");
      String queueName = new String("Q");
      clientSession.createQueue(addressAB, queueName1, null, false);
      clientSession.createQueue(addressAC, queueName2, null, false);
      clientSession.createQueue(address, queueName, null, false);
      Assert.assertEquals(2, server.getPostOffice().getBindingsForAddress(addressAB).getBindings().size());
      Assert.assertEquals(2, server.getPostOffice().getBindingsForAddress(addressAC).getBindings().size());
      Assert.assertEquals(1, server.getPostOffice().getBindingsForAddress(address).getBindings().size());
      ClientProducer producer = clientSession.createProducer(addressAB);
      ClientProducer producer2 = clientSession.createProducer(addressAC);
      ClientConsumer clientConsumer = clientSession.createConsumer(queueName);
      clientSession.start();
      producer.send(createTextMessage(clientSession, "m1"));
      producer2.send(createTextMessage(clientSession, "m2"));
      ClientMessage m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m1", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receive(500);
      Assert.assertNotNull(m);
      Assert.assertEquals("m2", m.getBodyBuffer().readString());
      m.acknowledge();
      m = clientConsumer.receiveImmediate();
      Assert.assertNull(m);
      clientConsumer.close();
      clientSession.deleteQueue(queueName);
      Assert.assertEquals(1, server.getPostOffice().getBindingsForAddress(addressAB).getBindings().size());
      Assert.assertEquals(1, server.getPostOffice().getBindingsForAddress(addressAC).getBindings().size());
      Assert.assertEquals(0, server.getPostOffice().getBindingsForAddress(address).getBindings().size());
   }

   @Override
   @Before
   public void setUp() throws Exception
   {
      super.setUp();

      Configuration configuration = createDefaultConfig();
      configuration.setWildcardRoutingEnabled(true);
      configuration.setSecurityEnabled(false);
      configuration.setTransactionTimeoutScanPeriod(500);
      TransportConfiguration transportConfig = new TransportConfiguration(UnitTestCase.INVM_ACCEPTOR_FACTORY);
      configuration.getAcceptorConfigurations().add(transportConfig);
      server = HornetQServers.newHornetQServer(configuration, false);
      // start the server
      server.start();
      server.getManagementService().enableNotifications(false);
      // then we create a client as normal
      locator = HornetQClient.createServerLocatorWithoutHA(new TransportConfiguration(UnitTestCase.INVM_CONNECTOR_FACTORY));
      sessionFactory = createSessionFactory(locator);
      clientSession = sessionFactory.createSession(false, true, true);
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
      closeSessionFactory(sessionFactory);
      stopComponent(server);
      closeServerLocator(locator);
      locator = null;
      server = null;
      clientSession = null;
      super.tearDown();
   }
}
