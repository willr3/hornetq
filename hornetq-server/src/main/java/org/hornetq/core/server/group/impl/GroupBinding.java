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
package org.hornetq.core.server.group.impl;



/**
 * A group binding
 *
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 *         Created Oct 19, 2009
 */
public class GroupBinding
{
   private long id;

   private final String groupId;

   private final String clusterName;

   volatile long timeUsed;

   public GroupBinding(final String groupId, final String clusterName)
   {
      this.groupId = groupId;
      this.clusterName = clusterName;
      use();
   }

   public GroupBinding(final long id, final String groupId, final String clusterName)
   {
      this.id = id;
      this.groupId = groupId;
      this.clusterName = clusterName;
      use();
   }

   public long getId()
   {
      return id;
   }

   public void setId(final long id)
   {
      this.id = id;
   }

   public String getGroupId()
   {
      return groupId;
   }

   public String getClusterName()
   {
      return clusterName;
   }

   public long getTimeUsed()
   {
      return timeUsed;
   }

   public void use()
   {
      timeUsed = System.currentTimeMillis();
   }

   @Override
   public String toString()
   {
      return id + ":" + groupId + ":" + clusterName;
   }
}
