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

package org.hornetq.ra.recovery;

import org.hornetq.jms.client.HornetQConnectionFactory;
import org.hornetq.jms.server.recovery.HornetQRegistryBase;
import org.hornetq.jms.server.recovery.XARecoveryConfig;
import org.hornetq.ra.HornetQRALogger;
import org.hornetq.utils.ClassloadingUtil;
import org.hornetq.utils.ConcurrentHashSet;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Set;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 *         9/21/11
 */
public final class RecoveryManager
{
   private HornetQRegistryBase registry;

   private static final String RESOURCE_RECOVERY_CLASS_NAMES = "org.jboss.as.messaging.jms.AS7RecoveryRegistry;"
            + "org.jboss.as.integration.hornetq.recovery.AS5RecoveryRegistry";

   private final Set<XARecoveryConfig> resources = new ConcurrentHashSet<XARecoveryConfig>();

   public void start(final boolean useAutoRecovery)
   {
      if (useAutoRecovery)
      {
         locateRecoveryRegistry();
      }
      else
      {
         registry = null;
      }
   }

   public XARecoveryConfig register(HornetQConnectionFactory factory, String userName, String password)
   {
      HornetQRALogger.LOGGER.debug("registering recovery for factory : " + factory);

      XARecoveryConfig config = XARecoveryConfig.newConfig(factory, userName, password);
      resources.add(config);
      if (registry != null)
      {
         registry.register(config);
      }
      return config;
   }


   public void unRegister(XARecoveryConfig resourceRecovery)
   {
      if (registry != null)
      {
         registry.unRegister(resourceRecovery);
      }
   }

   public void stop()
   {
      if (registry != null)
      {
         for (XARecoveryConfig recovery : resources)
         {
            registry.unRegister(recovery);
         }
         registry.stop();
      }


      resources.clear();
   }

   private void locateRecoveryRegistry()
   {
      String[] locatorClasses = RESOURCE_RECOVERY_CLASS_NAMES.split(";");

      for (String locatorClasse : locatorClasses)
      {
         try
         {
            registry = (HornetQRegistryBase) safeInitNewInstance(locatorClasse);
         }
         catch (Throwable e)
         {
            HornetQRALogger.LOGGER.debug("unable to load  recovery registry " + locatorClasse, e);
         }
         if (registry != null)
         {
            break;
         }
      }

      if (registry != null)
      {
         HornetQRALogger.LOGGER.debug("Recovery Registry located = " + registry);
      }
   }

   /** This seems duplicate code all over the place, but for security reasons we can't let something like this to be open in a
    *  utility class, as it would be a door to load anything you like in a safe VM.
    *  For that reason any class trying to do a privileged block should do with the AccessController directly.
    */
   private static Object safeInitNewInstance(final String className)
   {
      return AccessController.doPrivileged(new PrivilegedAction<Object>()
      {
         public Object run()
         {
            return ClassloadingUtil.newInstanceFromClassLoader(className);
         }
      });
   }

   public Set<XARecoveryConfig> getResources()
   {
      return resources;
   }
}
