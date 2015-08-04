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

package org.hornetq.tests.integration.jms.client;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.HornetQServers;
import org.hornetq.jms.client.HornetQConnectionFactory;
import org.hornetq.jms.server.impl.JMSServerManagerImpl;
import org.hornetq.tests.unit.util.InVMNamingContext;
import org.hornetq.tests.util.ServiceTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

/**
 * test Written to replicate https://issues.jboss.org/browse/HORNETQ-1312
 * @author Clebert Suconic
 */
public class RemoteConnectionStressTest extends ServiceTestBase
{


   HornetQServer server;
   MBeanServer mbeanServer;
   JMSServerManagerImpl jmsServer;

   @Before
   public void setUp() throws Exception
   {
      super.setUp();

      Configuration conf = ServiceTestBase.createBasicConfigNoDataFolder();
      conf.getAcceptorConfigurations().add(new TransportConfiguration("org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory"));

      mbeanServer = MBeanServerFactory.createMBeanServer();

      server = HornetQServers.newHornetQServer(conf, mbeanServer, false);

      InVMNamingContext namingContext = new InVMNamingContext();
      jmsServer = new JMSServerManagerImpl(server);
      jmsServer.setContext(namingContext);

      jmsServer.start();

      jmsServer.createQueue(true, "SomeQueue", null, true, "/jms/SomeQueue");
   }

   @After
   public void tearDown() throws Exception
   {
      jmsServer.stop();

      super.tearDown();
   }

   @Test
   public void testSimpleRemoteConnections() throws Exception
   {
      for (int i = 0; i < 1000; i++)
      {


         TransportConfiguration config = new TransportConfiguration(NETTY_CONNECTOR_FACTORY);
         HornetQConnectionFactory cf = HornetQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, config);
         cf.setInitialConnectAttempts(10);
         cf.setRetryInterval(100);

         Connection conn = cf.createConnection();

         Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

         Queue queue = session.createQueue("SomeQueue");

         MessageProducer producer = session.createProducer(queue);

         TextMessage msg = session.createTextMessage();
         msg.setText("Message " + i);

         producer.send(msg);

         producer.close();
         session.close();
         conn.close();

         cf.close();

      }
   }

}
