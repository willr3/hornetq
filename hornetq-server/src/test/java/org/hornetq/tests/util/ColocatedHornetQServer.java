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
package org.hornetq.tests.util;

import org.hornetq.core.asyncio.impl.AsynchronousFileImpl;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.FileConfiguration;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.JournalType;
import org.hornetq.core.server.NodeManager;
import org.hornetq.core.server.impl.AIOFileLockNodeManager;
import org.hornetq.core.server.impl.FileLockNodeManager;
import org.hornetq.core.server.impl.HornetQServerImpl;
import org.hornetq.spi.core.security.HornetQSecurityManager;

import javax.management.MBeanServer;
import java.util.Map;
import java.util.Set;


public class ColocatedHornetQServer extends HornetQServerImpl
{
   private final NodeManager nodeManagerLive;
   private final NodeManager nodeManagerBackup;
   boolean backup = false;
   public ColocatedHornetQServer backupServer;

   public ColocatedHornetQServer(FileConfiguration fc, HornetQSecurityManager sm, NodeManager nodeManagerLive, NodeManager nodeManagerBackup)
   {
      super(fc, sm);
      this.nodeManagerLive = nodeManagerLive;
      this.nodeManagerBackup = nodeManagerBackup;
   }

   public ColocatedHornetQServer(Configuration backupServerConfiguration, HornetQServer parentServer, NodeManager nodeManagerBackup, NodeManager nodeManagerLive)
   {
      super(backupServerConfiguration, null, null, parentServer);
      this.nodeManagerLive = nodeManagerLive;
      this.nodeManagerBackup = nodeManagerBackup;
   }

   public ColocatedHornetQServer(Configuration configuration, MBeanServer platformMBeanServer, HornetQSecurityManager securityManager,
                                 NodeManager nodeManagerLive, NodeManager nodeManagerBackup)
   {
      super(configuration, platformMBeanServer, securityManager);
      this.nodeManagerLive = nodeManagerLive;
      this.nodeManagerBackup = nodeManagerBackup;
   }


   @Override
   protected NodeManager
   createNodeManager(final String directory, final String nodeGroupName, boolean replicatingBackup)
   {
      if (replicatingBackup)
      {
         NodeManager manager;
         if (getConfiguration().getJournalType() == JournalType.ASYNCIO && AsynchronousFileImpl.isLoaded())
         {
            return new AIOFileLockNodeManager(directory, replicatingBackup);
         }
         else
         {
            return new FileLockNodeManager(directory, replicatingBackup);
         }
      }
      else
      {
         if (backup)
         {
            nodeManagerBackup.setNodeGroupName(nodeGroupName);
            return nodeManagerBackup;
         }
         else
         {
            nodeManagerLive.setNodeGroupName(nodeGroupName);
            return nodeManagerLive;
         }
      }
   }

   protected void startBackupServers(Configuration configuration, Map<String, HornetQServer> backupServers)
   {
      Set<Configuration> backupServerConfigurations = configuration.getBackupServerConfigurations();
      for (Configuration backupServerConfiguration : backupServerConfigurations)
      {
         ColocatedHornetQServer backup = new ColocatedHornetQServer(backupServerConfiguration, this, nodeManagerBackup, nodeManagerLive);
         backup.backup = true;
         this.backupServer = backup;
         backupServers.put(backupServerConfiguration.getName(), backup);
      }

      for (HornetQServer hornetQServer : backupServers.values())
      {
         try
         {
            hornetQServer.start();
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
   }
}
