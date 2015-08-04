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
package org.hornetq.tests.integration.spring;

import org.hornetq.jms.client.HornetQConnectionFactory;
import org.hornetq.jms.server.embedded.EmbeddedJMS;
import org.hornetq.tests.integration.IntegrationTestLogger;
import org.hornetq.tests.util.UnitTestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SpringIntegrationTest extends UnitTestCase
{
   IntegrationTestLogger log = IntegrationTestLogger.LOGGER;

   @Before
   public void setUp() throws Exception
   {
      super.setUp();
      // Need to force GC as the connection on the spring needs to be cleared
      // otherwise the sprint thread may leak here
      forceGC();
   }

   @Test
   public void testSpring() throws Exception
   {
      System.out.println("Creating bean factory...");
      ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"spring-jms-beans.xml"});
      try
      {
         MessageSender sender = (MessageSender) context.getBean("MessageSender");
         System.out.println("Sending message...");
         ExampleListener.latch.countUp();
         sender.send("Hello world");
         ExampleListener.latch.await(10, TimeUnit.SECONDS);
         Thread.sleep(500);
         Assert.assertEquals(ExampleListener.lastMessage, "Hello world");
         ((HornetQConnectionFactory) sender.getConnectionFactory()).close();
      }
      finally
      {
         try
         {
            DefaultMessageListenerContainer container = (DefaultMessageListenerContainer) context.getBean("listenerContainer");
            container.stop();
         }
         catch (Throwable ignored)
         {
            ignored.printStackTrace();
         }
         try
         {
            EmbeddedJMS jms = (EmbeddedJMS) context.getBean("EmbeddedJms");
            jms.stop();
         }
         catch (Throwable ignored)
         {
            ignored.printStackTrace();
         }
      }

   }
}
