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
package org.hornetq.tests.unit.util;

import org.hornetq.core.paging.PageTransactionInfo;
import org.hornetq.core.paging.PagingManager;
import org.hornetq.core.paging.PagingStore;
import org.hornetq.core.postoffice.PostOffice;
import org.hornetq.core.server.ServerMessage;

import java.util.Collection;
import java.util.Map;

public final class FakePagingManager implements PagingManager
{

   public void activate()
   {
   }

   public long addSize(final long size)
   {
      return 0;
   }

   public void addTransaction(final PageTransactionInfo pageTransaction)
   {
   }

   public PagingStore createPageStore(final String destination) throws Exception
   {
      return null;
   }

   public long getTotalMemory()
   {
      return 0;
   }

   public String[] getStoreNames()
   {
      return null;
   }

   public long getMaxMemory()
   {
      return 0;
   }

   public PagingStore getPageStore(final String address) throws Exception
   {
      return null;
   }

   public void deletePageStore(String storeName) throws Exception
   {
   }

   public PageTransactionInfo getTransaction(final long transactionID)
   {
      return null;
   }

   public boolean isBackup()
   {
      return false;
   }

   public boolean isGlobalPageMode()
   {
      return false;
   }

   public boolean isPaging(final String destination) throws Exception
   {
      return false;
   }

   public boolean page(final ServerMessage message, final boolean duplicateDetection) throws Exception
   {
      return false;
   }

   public boolean page(final ServerMessage message, final long transactionId, final boolean duplicateDetection) throws Exception
   {
      return false;
   }

   public void reloadStores() throws Exception
   {
   }

   public void removeTransaction(final long transactionID)
   {

   }

   public void setGlobalPageMode(final boolean globalMode)
   {
   }

   public void setPostOffice(final PostOffice postOffice)
   {
   }

   public void resumeDepages()
   {
   }

   public void sync(final Collection<String> destinationsToSync) throws Exception
   {
   }

   public boolean isStarted()
   {
      return false;
   }

   public void start() throws Exception
   {
   }

   public void stop() throws Exception
   {
   }

   /*
    * (non-Javadoc)
    * @see org.hornetq.core.paging.PagingManager#isGlobalFull()
    */
   public boolean isGlobalFull()
   {
      return false;
   }

   /*
    * (non-Javadoc)
    * @see org.hornetq.core.paging.PagingManager#getTransactions()
    */
   public Map<Long, PageTransactionInfo> getTransactions()
   {
      return null;
   }

   /*
    * (non-Javadoc)
    * @see org.hornetq.core.paging.PagingManager#processReload()
    */
   public void processReload()
   {
   }

   @Override
   public void disableCleanup()
   {
   }

   @Override
   public void resumeCleanup()
   {
   }

   /*
    * (non-Javadoc)
    * @see org.hornetq.core.settings.HierarchicalRepositoryChangeListener#onChange()
    */
   public void onChange()
   {
   }

   @Override
   public void lock()
   {
      // no-op
   }

   @Override
   public void unlock()
   {
      // no-op
   }

}
