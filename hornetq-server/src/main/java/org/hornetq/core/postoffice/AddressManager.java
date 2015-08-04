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
package org.hornetq.core.postoffice;

import org.hornetq.core.transaction.Transaction;

import java.util.Map;

/**
 * Used to maintain addresses and BindingsImpl.
 *
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * @author <a href="jmesnil@redhat.com">Jeff Mesnil</a>
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 */
public interface AddressManager
{
   boolean addBinding(Binding binding) throws Exception;

   /**
    * This will use a Transaction as we need to confirm the queue was removed
    * @param uniqueName
    * @param tx
    * @return
    * @throws Exception
    */
   Binding removeBinding(String uniqueName, Transaction tx) throws Exception;

   Bindings getBindingsForRoutingAddress(String address) throws Exception;

   Bindings getMatchingBindings(String address) throws Exception;

   void clear();

   Binding getBinding(String queueName);

   Map<String, Binding> getBindings();
}
