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
/**
 *
 */
package org.hornetq.jms.client;

import org.hornetq.api.jms.HornetQJMSConstants;
import org.hornetq.utils.ReferenceCounter;
import org.hornetq.utils.ReferenceCounterUtil;

import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.Session;
import javax.jms.XAJMSContext;

public abstract class HornetQConnectionForContextImpl implements HornetQConnectionForContext
{

   final Runnable closeRunnable = new Runnable()
   {
      public void run()
      {
         try
         {
            close();
         }
         catch (JMSException e)
         {
            throw JmsExceptionUtils.convertToRuntimeException(e);
         }
      }
   };

   final ReferenceCounter refCounter = new ReferenceCounterUtil(closeRunnable);

   protected final ThreadAwareContext threadAwareContext = new ThreadAwareContext();

   public JMSContext createContext(int sessionMode)
   {
      switch (sessionMode)
      {
         case Session.AUTO_ACKNOWLEDGE:
         case Session.CLIENT_ACKNOWLEDGE:
         case Session.DUPS_OK_ACKNOWLEDGE:
         case Session.SESSION_TRANSACTED:
         case HornetQJMSConstants.INDIVIDUAL_ACKNOWLEDGE:
         case HornetQJMSConstants.PRE_ACKNOWLEDGE:
            break;
         default:
            throw new JMSRuntimeException("Invalid ackmode: " + sessionMode);
      }
      refCounter.increment();

      return new HornetQJMSContext(this, sessionMode, threadAwareContext);
   }

   public XAJMSContext createXAContext()
   {
      refCounter.increment();

      return new HornetQXAJMSContext(this, threadAwareContext);
   }

   @Override
   public void closeFromContext()
   {
      refCounter.decrement();
   }

   protected void incrementRefCounter()
   {
      refCounter.increment();
   }

   public ThreadAwareContext getThreadAwareContext()
   {
      return threadAwareContext;
   }
}
