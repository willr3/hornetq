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
package org.hornetq.tests.integration.ra;


import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.core.server.Queue;
import org.hornetq.ra.HornetQResourceAdapter;
import org.hornetq.ra.inflow.HornetQActivation;
import org.hornetq.ra.inflow.HornetQActivationSpec;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class HornetQClusteredTest extends HornetQRAClusteredTestBase
{

   /*
   * the second server has no queue so this tests for partial initialisation
   * */
   @Test
   public void testShutdownOnPartialConnect() throws Exception
   {
      HornetQResourceAdapter qResourceAdapter = newResourceAdapter();
      MyBootstrapContext ctx = new MyBootstrapContext();
      qResourceAdapter.setHA(true);
      qResourceAdapter.start(ctx);
      HornetQActivationSpec spec = new HornetQActivationSpec();
      spec.setResourceAdapter(qResourceAdapter);
      spec.setUseJNDI(false);
      spec.setSetupAttempts(0);
      spec.setDestinationType("javax.jms.Queue");
      spec.setDestination(MDBQUEUE);
      spec.setHA(true);
      qResourceAdapter.setConnectorClassName(INVM_CONNECTOR_FACTORY + "," + INVM_CONNECTOR_FACTORY);
      qResourceAdapter.setConnectionParameters("server-id=0, server-id=1");
      CountDownLatch latch = new CountDownLatch(1);
      DummyMessageEndpoint endpoint = new DummyMessageEndpoint(latch);
      DummyMessageEndpointFactory endpointFactory = new DummyMessageEndpointFactory(endpoint, false);
      qResourceAdapter.endpointActivation(endpointFactory, spec);
      //make sure thet activation didnt start, i.e. no MDB consumers
      assertEquals(((Queue)server.getPostOffice().getBinding(MDBQUEUEPREFIXEDSIMPLE).getBindable()).getConsumerCount(), 0);
      qResourceAdapter.endpointDeactivation(endpointFactory, spec);

      qResourceAdapter.stop();
   }


   /**
    * https://bugzilla.redhat.com/show_bug.cgi?id=1029076
    * Look at the logs for this test, if you see exceptions it's an issue.
    * @throws Exception
    */
   @Test
   public void testNonDurableInCluster() throws Exception
   {
      HornetQResourceAdapter qResourceAdapter = newResourceAdapter();
      MyBootstrapContext ctx = new MyBootstrapContext();
      qResourceAdapter.start(ctx);
      HornetQActivationSpec spec = new HornetQActivationSpec();
      spec.setResourceAdapter(qResourceAdapter);
      spec.setUseJNDI(false);
      spec.setDestinationType("javax.jms.Topic");
      spec.setDestination("mdbTopic");
      qResourceAdapter.setConnectorClassName(INVM_CONNECTOR_FACTORY);
      CountDownLatch latch = new CountDownLatch(1);
      DummyMessageEndpoint endpoint = new DummyMessageEndpoint(latch);
      DummyMessageEndpointFactory endpointFactory = new DummyMessageEndpointFactory(endpoint, false);
      qResourceAdapter.endpointActivation(endpointFactory, spec);
      ClientSession session = locator.createSessionFactory().createSession();
      ClientProducer clientProducer = session.createProducer("jms.topic.mdbTopic");
      ClientMessage message = session.createMessage(true);
      message.getBodyBuffer().writeString("test");
      clientProducer.send(message);


      HornetQActivation activation =  lookupActivation(qResourceAdapter);

      String tempQueue = activation.getTopicTemporaryQueue();

      assertNotNull(server.locateQueue(tempQueue));
      assertNotNull(secondaryServer.locateQueue(tempQueue));


      latch.await(5, TimeUnit.SECONDS);

      assertNotNull(endpoint.lastMessage);
      assertEquals(endpoint.lastMessage.getCoreMessage().getBodyBuffer().readString(), "test");

      qResourceAdapter.endpointDeactivation(endpointFactory, spec);
      qResourceAdapter.stop();

      assertNull(server.locateQueue(tempQueue));
      assertNull(secondaryServer.locateQueue(tempQueue));

   }
}
