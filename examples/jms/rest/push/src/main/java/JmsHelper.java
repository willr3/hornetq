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

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.HornetQClient;
import org.hornetq.core.config.impl.FileConfiguration;
import org.hornetq.jms.client.HornetQJMSConnectionFactory;

import javax.jms.ConnectionFactory;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class JmsHelper
{
   public static ConnectionFactory createConnectionFactory(String configFile) throws Exception
   {
      FileConfiguration config = new FileConfiguration();
      config.setConfigurationUrl(configFile);
      config.start();
      TransportConfiguration transport = config.getConnectorConfigurations().get("netty-connector");
      return new HornetQJMSConnectionFactory(HornetQClient.createServerLocatorWithoutHA(transport));

   }
}
