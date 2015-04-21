/*
 * Copyright 2009 Red Hat, Inc.
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

import java.io.Serializable;
import java.util.List;


import org.hornetq.core.server.HornetQMessageBundle;

/**
 * A QueueInfo
 *
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 *
 * Created 21 Jan 2009 20:55:06
 *
 *
 */
public class QueueInfo implements Serializable
{
   private static final long serialVersionUID = 3451892849198803182L;

   private final String routingName;

   private final String clusterName;

   private final String address;

   private final String filterString;

   private final long id;

   private List<String> filterStrings;

   private int numberOfConsumers;

   private final int distance;

   public QueueInfo(final String routingName,
                    final String clusterName,
                    final String address,
                    final String filterString,
                    final long id,
                    final int distance)
   {
      if (routingName == null)
      {
         throw HornetQMessageBundle.BUNDLE.routeNameIsNull();
      }
      if (clusterName == null)
      {
         throw HornetQMessageBundle.BUNDLE.clusterNameIsNull();
      }
      if (address == null)
      {
         throw HornetQMessageBundle.BUNDLE.addressIsNull();
      }

      this.routingName = routingName;
      this.clusterName = clusterName;
      this.address = address;
      this.filterString = filterString;
      this.id = id;
      this.distance = distance;
   }

   public String getRoutingName()
   {
      return routingName;
   }

   public String getClusterName()
   {
      return clusterName;
   }

   public String getAddress()
   {
      return address;
   }

   public String getFilterString()
   {
      return filterString;
   }

   public int getDistance()
   {
      return distance;
   }

   public long getID()
   {
      return id;
   }

   public List<String> getFilterStrings()
   {
      return filterStrings;
   }

   public void setFilterStrings(final List<String> filterStrings)
   {
      this.filterStrings = filterStrings;
   }

   public int getNumberOfConsumers()
   {
      return numberOfConsumers;
   }

   public void incrementConsumers()
   {
      numberOfConsumers++;
   }

   public void decrementConsumers()
   {
      numberOfConsumers--;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "QueueInfo [routingName=" + routingName +
             ", clusterName=" +
             clusterName +
             ", address=" +
             address +
             ", filterString=" +
             filterString +
             ", id=" +
             id +
             ", filterStrings=" +
             filterStrings +
             ", numberOfConsumers=" +
             numberOfConsumers +
             ", distance=" +
             distance +
             "]";
   }


}
