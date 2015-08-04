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
package org.hornetq.tests.util;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.core.config.Configuration;

import java.util.Set;

public final class ReplicatedBackupUtils
{
   public static final String LIVE_NODE_NAME = "hqLIVE";
   public static final String BACKUP_NODE_NAME = "hqBackup";
   private ReplicatedBackupUtils()
   {
      // Utility class
   }

   public static void configureReplicationPair(Configuration backupConfig,
                                               TransportConfiguration backupConnector,
                                               TransportConfiguration backupAcceptor,
                                               Configuration liveConfig,
                                               TransportConfiguration liveConnector,
                                               TransportConfiguration liveAcceptor)
   {
      if (backupAcceptor != null)
      {
         Set<TransportConfiguration> backupAcceptorSet = backupConfig.getAcceptorConfigurations();
         backupAcceptorSet.clear();
         backupAcceptorSet.add(backupAcceptor);
      }

      if (liveAcceptor != null)
      {
         Set<TransportConfiguration> liveAcceptorSet = liveConfig.getAcceptorConfigurations();
         liveAcceptorSet.clear();
         liveAcceptorSet.add(liveAcceptor);
      }

      backupConfig.getConnectorConfigurations().put(BACKUP_NODE_NAME, backupConnector);
      backupConfig.getConnectorConfigurations().put(LIVE_NODE_NAME, liveConnector);

      UnitTestCase.basicClusterConnectionConfig(backupConfig, BACKUP_NODE_NAME, LIVE_NODE_NAME);

      backupConfig.setSharedStore(false);
      backupConfig.setBackup(true);

      liveConfig.setName(LIVE_NODE_NAME);
      liveConfig.getConnectorConfigurations().put(LIVE_NODE_NAME, liveConnector);
      liveConfig.getConnectorConfigurations().put(BACKUP_NODE_NAME, backupConnector);
      liveConfig.setSecurityEnabled(false);
      liveConfig.setSharedStore(false);
      UnitTestCase.basicClusterConnectionConfig(liveConfig, LIVE_NODE_NAME, BACKUP_NODE_NAME);
   }
}
