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
package org.hornetq.tests.performance.sends;

import org.hornetq.api.jms.HornetQJMSConstants;

import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;

/**
 * @author clebertsuconic
 */

public class PreACKPerf extends AbstractSendReceivePerfTest
{
   @Override
   protected void consumeMessages(Connection c, String qName) throws Exception
   {
      int mode = 0;
      mode = HornetQJMSConstants.PRE_ACKNOWLEDGE;

      System.out.println("Receiver: Using PRE-ACK mode");

      Session s = c.createSession(false, mode);
      Queue q = s.createQueue(qName);
      MessageConsumer consumer = s.createConsumer(q, null, false);

      c.start();

      Message m = null;


      long start = System.currentTimeMillis();

      long nmessages = 0;
      long timeout = System.currentTimeMillis() + 30 * 1000;
      while (timeout > System.currentTimeMillis())
      {
         m = consumer.receive(5000);

         nmessages++;

         if (m == null)
         {
            throw new Exception("Failed with m = null");
         }

         if (nmessages % 10000 == 0)
         {
            printMsgsSec(start, nmessages);
         }

      }

      long end = System.currentTimeMillis();

      printMsgsSec(start, nmessages);
   }



   protected void printMsgsSec(final long start, final double nmessages)
   {

      long end = System.currentTimeMillis();
      double elapsed = ((double) end - (double) start) / 1000f;

      double messagesPerSecond = nmessages / elapsed;

      System.out.println("end = " + end + ", start=" + start + ", numberOfMessages="
                            + nmessages + ", elapsed=" + elapsed + " msgs/sec= " + messagesPerSecond);

   }


}
