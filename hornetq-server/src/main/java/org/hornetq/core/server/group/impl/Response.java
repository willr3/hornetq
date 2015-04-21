/*
 * Copyright 2009 Red Hat, Inc.
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package org.hornetq.core.server.group.impl;

/**
 * A response to a proposal
 *
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 */
public class Response
{
   private final boolean accepted;

   private final String clusterName;

   private final String alternativeClusterName;

   private final String groupId;

   private volatile long timeUsed;

   public Response(final String groupId, final String clusterName)
   {
      this(groupId, clusterName, null);
   }

   public Response(final String groupId, final String clusterName, final String alternativeClusterName)
   {
      this.groupId = groupId;
      accepted = alternativeClusterName == null;
      this.clusterName = clusterName;
      this.alternativeClusterName = alternativeClusterName;
      use();
   }

   public void use()
   {
      timeUsed = System.currentTimeMillis();
   }

   public long getTimeUsed()
   {
      return timeUsed;
   }

   public boolean isAccepted()
   {
      return accepted;
   }

   public String getClusterName()
   {
      return clusterName;
   }

   public String getAlternativeClusterName()
   {
      return alternativeClusterName;
   }

   public String getChosenClusterName()
   {
      return alternativeClusterName != null ? alternativeClusterName : clusterName;
   }

   @Override
   public String toString()
   {
      return "accepted = " + accepted +
             " groupid = "  + groupId +
             " clusterName = " +
             clusterName +
             " alternativeClusterName = " +
             alternativeClusterName;
   }

   public String getGroupId()
   {
      return groupId;
   }
}
