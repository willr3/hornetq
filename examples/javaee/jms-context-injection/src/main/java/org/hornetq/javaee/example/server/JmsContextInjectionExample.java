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
package org.hornetq.javaee.example.server;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.TextMessage;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 */
@MessageDriven(name = "MDB_JMS_CONTEXT", activationConfig = { @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
                                                                       @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/testQueue"),
                                                                       @ActivationConfigProperty(propertyName = "consumerMaxRate", propertyValue = "1")})
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(value = TransactionAttributeType.REQUIRED)
public class JmsContextInjectionExample implements MessageListener
{
   // 1. Inject the JMSContext
   @Inject
   javax.jms.JMSContext context;

   // 2. Map the reply queue
   @Resource(mappedName = "java:/queue/replyQueue")
   Queue replyQueue;

   public void onMessage(final Message message)
   {
      try
      {
         // Step 9. We know the client is sending a text message so we cast
         TextMessage textMessage = (TextMessage)message;

         // Step 10. we print out the message text
         System.out.println("message " + textMessage.getText() + " received");

         // Step 11. we create a JMSProducer and send a message
         context.createProducer().send(replyQueue, "this is a reply");
      }
      catch (JMSException e)
      {
         e.printStackTrace();
      }
   }
}