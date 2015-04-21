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

package org.hornetq.tests.stress.journal;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.hornetq.api.config.HornetQDefaultConfiguration;

import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.JournalType;
import org.hornetq.tests.util.ServiceTestBase;
import org.hornetq.tests.util.UnitTestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * A MultiThreadConsumerStressTest
 * <p/>
 * This test validates consuming / sending messages while compacting is working
 *
 * @author <mailto:clebert.suconic@jboss.org">Clebert Suconic</a>
 */
public class MultiThreadConsumerStressTest extends ServiceTestBase
{

   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   final String ADDRESS = new String("SomeAddress");

   final String QUEUE = new String("SomeQueue");

   private HornetQServer server;

   private ClientSessionFactory sf;

   @Override
   @Before
   public void setUp() throws Exception
   {
      super.setUp();
      setupServer(JournalType.NIO);
   }

   @Test
   public void testProduceAndConsume() throws Throwable
   {
      int numberOfConsumers = 5;
      // this test assumes numberOfConsumers == numberOfProducers
      int numberOfProducers = numberOfConsumers;
      int produceMessage = 10000;
      int commitIntervalProduce = 100;
      int consumeMessage = (int)(produceMessage * 0.9);
      int commitIntervalConsume = 100;

      ClientSession session = sf.createSession(false, false);
      session.createQueue("compact", "compact-queue", true);

      ClientProducer producer = session.createProducer("compact");

      for (int i = 0; i < 100; i++)
      {
         producer.send(session.createMessage(true));
      }

      session.commit();

      // Number of messages expected to be received after restart
      int numberOfMessagesExpected = (produceMessage - consumeMessage) * numberOfConsumers;

      CountDownLatch latchReady = new CountDownLatch(numberOfConsumers + numberOfProducers);

      CountDownLatch latchStart = new CountDownLatch(1);

      ArrayList<BaseThread> threads = new ArrayList<BaseThread>();

      ProducerThread[] prod = new ProducerThread[numberOfProducers];
      for (int i = 0; i < numberOfProducers; i++)
      {
         prod[i] = new ProducerThread(i, latchReady, latchStart, produceMessage, commitIntervalProduce);
         prod[i].start();
         threads.add(prod[i]);
      }

      ConsumerThread[] cons = new ConsumerThread[numberOfConsumers];

      for (int i = 0; i < numberOfConsumers; i++)
      {
         cons[i] = new ConsumerThread(i, latchReady, latchStart, consumeMessage, commitIntervalConsume);
         cons[i].start();
         threads.add(cons[i]);
      }

      UnitTestCase.waitForLatch(latchReady);
      latchStart.countDown();

      for (BaseThread t : threads)
      {
         t.join();
         if (t.e != null)
         {
            throw t.e;
         }
      }

      server.stop();

      setupServer(JournalType.NIO);

      ClientSession sess = sf.createSession(true, true);

      ClientConsumer consumer = sess.createConsumer(QUEUE);

      sess.start();

      for (int i = 0; i < numberOfMessagesExpected; i++)
      {
         ClientMessage msg = consumer.receive(5000);
         Assert.assertNotNull(msg);

         if (i % 1000 == 0)
         {
            System.out.println("Received #" + i + "  on thread before end");
         }
         msg.acknowledge();
      }

      Assert.assertNull(consumer.receiveImmediate());

      sess.close();

   }

   private void setupServer(final JournalType journalType) throws Exception
   {
      Configuration config = createDefaultConfig(true);

      config.setJournalType(journalType);
      config.setJMXManagementEnabled(true);

      config.setJournalFileSize(HornetQDefaultConfiguration.getDefaultJournalFileSize());
      config.setJournalMinFiles(HornetQDefaultConfiguration.getDefaultJournalMinFiles());

      config.setJournalCompactMinFiles(2);
      config.setJournalCompactPercentage(50);

      server = createServer(true, config);

      server.start();

      ServerLocator locator = createNettyNonHALocator();

      locator.setBlockOnDurableSend(false);

      locator.setBlockOnNonDurableSend(false);

      locator.setBlockOnAcknowledge(false);

      sf = createSessionFactory(locator);

      ClientSession sess = sf.createSession();

      try
      {
         sess.createQueue(ADDRESS, QUEUE, true);
      }
      catch (Exception ignored)
      {
      }

      sess.close();
      locator.close();
      locator = createInVMNonHALocator();
      sf = createSessionFactory(locator);
   }

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   class BaseThread extends Thread
   {
      Throwable e;

      final CountDownLatch latchReady;

      final CountDownLatch latchStart;

      final int numberOfMessages;

      final int commitInterval;

      BaseThread(final String name,
                 final CountDownLatch latchReady,
                 final CountDownLatch latchStart,
                 final int numberOfMessages,
                 final int commitInterval)
      {
         super(name);
         this.latchReady = latchReady;
         this.latchStart = latchStart;
         this.commitInterval = commitInterval;
         this.numberOfMessages = numberOfMessages;
      }

   }

   class ProducerThread extends BaseThread
   {
      ProducerThread(final int id,
                     final CountDownLatch latchReady,
                     final CountDownLatch latchStart,
                     final int numberOfMessages,
                     final int commitInterval)
      {
         super("ClientProducer:" + id, latchReady, latchStart, numberOfMessages, commitInterval);
      }

      @Override
      public void run()
      {
         ClientSession session = null;
         latchReady.countDown();
         try
         {
            UnitTestCase.waitForLatch(latchStart);
            session = sf.createSession(false, false);
            ClientProducer prod = session.createProducer(ADDRESS);
            for (int i = 0; i < numberOfMessages; i++)
            {
               if (i % commitInterval == 0)
               {
                  session.commit();
               }
               if (i % 1000 == 0)
               {
                  // System.out.println(Thread.currentThread().getName() + "::received #" + i);
               }
               ClientMessage msg = session.createMessage(true);
               prod.send(msg);
            }

            session.commit();

            System.out.println("Thread " + Thread.currentThread().getName() +
                                  " sent " +
                                  numberOfMessages +
                                  "  messages");
         }
         catch (Throwable e)
         {
            e.printStackTrace();
            this.e = e;
         }
         finally
         {
            try
            {
               session.close();
            }
            catch (Throwable e)
            {
               e.printStackTrace();
            }
         }
      }
   }

   class ConsumerThread extends BaseThread
   {
      ConsumerThread(final int id,
                     final CountDownLatch latchReady,
                     final CountDownLatch latchStart,
                     final int numberOfMessages,
                     final int commitInterval)
      {
         super("ClientConsumer:" + id, latchReady, latchStart, numberOfMessages, commitInterval);
      }

      @Override
      public void run()
      {
         ClientSession session = null;
         latchReady.countDown();
         try
         {
            UnitTestCase.waitForLatch(latchStart);
            session = sf.createSession(false, false);
            session.start();
            ClientConsumer cons = session.createConsumer(QUEUE);
            for (int i = 0; i < numberOfMessages; i++)
            {
               ClientMessage msg = cons.receive(60 * 1000);
               msg.acknowledge();
               if (i % commitInterval == 0)
               {
                  session.commit();
               }
               if (i % 1000 == 0)
               {
                  // System.out.println(Thread.currentThread().getName() + "::sent #" + i);
               }
            }

            System.out.println("Thread " + Thread.currentThread().getName() +
                                  " received " +
                                  numberOfMessages +
                                  " messages");

            session.commit();
         }
         catch (Throwable e)
         {
            this.e = e;
         }
         finally
         {
            try
            {
               session.close();
            }
            catch (Throwable e)
            {
               this.e = e;
            }
         }
      }
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}
