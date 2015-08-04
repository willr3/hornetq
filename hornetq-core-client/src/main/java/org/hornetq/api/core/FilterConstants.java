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
package org.hornetq.api.core;

/**
 * Constants representing pre-defined message attributes that can be referenced in HornetQ core
 * filter expressions.
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 */
public final class FilterConstants
{
   /**
    * Name of the HornetQ UserID header.
    */
   public static final String HORNETQ_USERID = new String("HQUserID");

   /**
    * Name of the HornetQ Message expiration header.
    */
   public static final String HORNETQ_EXPIRATION = new String("HQExpiration");

   /**
    * Name of the HornetQ Message durable header.
    */
   public static final String HORNETQ_DURABLE = new String("HQDurable");

   /**
    * Value for the Durable header when the message is non-durable.
    */
   public static final String NON_DURABLE = new String("NON_DURABLE");

   /**
    * Value for the Durable header when the message is durable.
    */
   public static final String DURABLE = new String("DURABLE");

   /**
    * Name of the HornetQ Message timestamp header.
    */
   public static final String HORNETQ_TIMESTAMP = new String("HQTimestamp");

   /**
    * Name of the HornetQ Message priority header.
    */
   public static final String HORNETQ_PRIORITY = new String("HQPriority");

   /**
    * Name of the HornetQ Message size header.
    */
   public static final String HORNETQ_SIZE = new String("HQSize");

   /**
    * All HornetQ headers are prepended by this prefix.
    */
   public static final String HORNETQ_PREFIX = new String("HQ");

   private FilterConstants()
   {
      // Utility class
   }
}
