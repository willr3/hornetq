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
package org.hornetq.core.persistence.config;

import org.hornetq.api.core.HornetQBuffer;
import org.hornetq.api.core.SSU;
import org.hornetq.core.journal.EncodingSupport;

/**
 * A ConfiguredRoles
 *
 * @author <mailto:clebert.suconic@jboss.org">Clebert Suconic</a>
 *
 *
 */
public class PersistedRoles implements EncodingSupport
{

   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   private long storeId;

   private String addressMatch;

   private String sendRoles;

   private String consumeRoles;

   private String createDurableQueueRoles;

   private String deleteDurableQueueRoles;

   private String createNonDurableQueueRoles;

   private String deleteNonDurableQueueRoles;

   private String manageRoles;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public PersistedRoles()
   {
   }

   /**
    * @param address
    * @param addressMatch
    * @param sendRoles
    * @param consumeRoles
    * @param createDurableQueueRoles
    * @param deleteDurableQueueRoles
    * @param createNonDurableQueueRoles
    * @param deleteNonDurableQueueRoles
    * @param manageRoles
    */
   public PersistedRoles(final String addressMatch,
                         final String sendRoles,
                         final String consumeRoles,
                         final String createDurableQueueRoles,
                         final String deleteDurableQueueRoles,
                         final String createNonDurableQueueRoles,
                         final String deleteNonDurableQueueRoles,
                         final String manageRoles)
   {
      super();
      this.addressMatch = (addressMatch);
      this.sendRoles = (sendRoles);
      this.consumeRoles = (consumeRoles);
      this.createDurableQueueRoles = (createDurableQueueRoles);
      this.deleteDurableQueueRoles = (deleteDurableQueueRoles);
      this.createNonDurableQueueRoles = (createNonDurableQueueRoles);
      this.deleteNonDurableQueueRoles = (deleteNonDurableQueueRoles);
      this.manageRoles = (manageRoles);
   }

   // Public --------------------------------------------------------

   public long getStoreId()
   {
      return storeId;
   }

   public void setStoreId(final long id)
   {
      storeId = id;
   }

   /**
    * @return the addressMatch
    */
   public String getAddressMatch()
   {
      return addressMatch;
   }

   /**
    * @return the sendRoles
    */
   public String getSendRoles()
   {
      return sendRoles.toString();
   }

   /**
    * @return the consumeRoles
    */
   public String getConsumeRoles()
   {
      return consumeRoles.toString();
   }

   /**
    * @return the createDurableQueueRoles
    */
   public String getCreateDurableQueueRoles()
   {
      return createDurableQueueRoles.toString();
   }

   /**
    * @return the deleteDurableQueueRoles
    */
   public String getDeleteDurableQueueRoles()
   {
      return deleteDurableQueueRoles.toString();
   }

   /**
    * @return the createNonDurableQueueRoles
    */
   public String getCreateNonDurableQueueRoles()
   {
      return createNonDurableQueueRoles.toString();
   }

   /**
    * @return the deleteNonDurableQueueRoles
    */
   public String getDeleteNonDurableQueueRoles()
   {
      return deleteNonDurableQueueRoles.toString();
   }

   /**
    * @return the manageRoles
    */
   public String getManageRoles()
   {
      return manageRoles.toString();
   }

   @Override
   public void encode(final HornetQBuffer buffer)
   {
      buffer.writeSimpleString(addressMatch);
      buffer.writeNullableSimpleString(sendRoles);
      buffer.writeNullableSimpleString(consumeRoles);
      buffer.writeNullableSimpleString(createDurableQueueRoles);
      buffer.writeNullableSimpleString(deleteDurableQueueRoles);
      buffer.writeNullableSimpleString(createNonDurableQueueRoles);
      buffer.writeNullableSimpleString(deleteNonDurableQueueRoles);
      buffer.writeNullableSimpleString(manageRoles);
   }

   @Override
   public int getEncodeSize()
   {
      return SSU.sizeof(addressMatch) + SSU.sizeof(sendRoles) +
             SSU.sizeof(consumeRoles) +
             SSU.sizeof(createDurableQueueRoles) +
             SSU.sizeof(deleteDurableQueueRoles) +
             SSU.sizeof(createNonDurableQueueRoles) +
             SSU.sizeof(deleteNonDurableQueueRoles) +
             SSU.sizeof(manageRoles);

   }

   @Override
   public void decode(final HornetQBuffer buffer)
   {
      addressMatch = buffer.readSimpleString();
      sendRoles = buffer.readNullableSimpleString();
      consumeRoles = buffer.readNullableSimpleString();
      createDurableQueueRoles = buffer.readNullableSimpleString();
      deleteDurableQueueRoles = buffer.readNullableSimpleString();
      createNonDurableQueueRoles = buffer.readNullableSimpleString();
      deleteNonDurableQueueRoles = buffer.readNullableSimpleString();
      manageRoles = buffer.readNullableSimpleString();
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((addressMatch == null) ? 0 : addressMatch.hashCode());
      result = prime * result + ((consumeRoles == null) ? 0 : consumeRoles.hashCode());
      result = prime * result + ((createDurableQueueRoles == null) ? 0 : createDurableQueueRoles.hashCode());
      result = prime * result + ((createNonDurableQueueRoles == null) ? 0 : createNonDurableQueueRoles.hashCode());
      result = prime * result + ((deleteDurableQueueRoles == null) ? 0 : deleteDurableQueueRoles.hashCode());
      result = prime * result + ((deleteNonDurableQueueRoles == null) ? 0 : deleteNonDurableQueueRoles.hashCode());
      result = prime * result + ((manageRoles == null) ? 0 : manageRoles.hashCode());
      result = prime * result + ((sendRoles == null) ? 0 : sendRoles.hashCode());
      result = prime * result + (int)(storeId ^ (storeId >>> 32));
      return result;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      PersistedRoles other = (PersistedRoles)obj;
      if (addressMatch == null)
      {
         if (other.addressMatch != null)
            return false;
      }
      else if (!addressMatch.equals(other.addressMatch))
         return false;
      if (consumeRoles == null)
      {
         if (other.consumeRoles != null)
            return false;
      }
      else if (!consumeRoles.equals(other.consumeRoles))
         return false;
      if (createDurableQueueRoles == null)
      {
         if (other.createDurableQueueRoles != null)
            return false;
      }
      else if (!createDurableQueueRoles.equals(other.createDurableQueueRoles))
         return false;
      if (createNonDurableQueueRoles == null)
      {
         if (other.createNonDurableQueueRoles != null)
            return false;
      }
      else if (!createNonDurableQueueRoles.equals(other.createNonDurableQueueRoles))
         return false;
      if (deleteDurableQueueRoles == null)
      {
         if (other.deleteDurableQueueRoles != null)
            return false;
      }
      else if (!deleteDurableQueueRoles.equals(other.deleteDurableQueueRoles))
         return false;
      if (deleteNonDurableQueueRoles == null)
      {
         if (other.deleteNonDurableQueueRoles != null)
            return false;
      }
      else if (!deleteNonDurableQueueRoles.equals(other.deleteNonDurableQueueRoles))
         return false;
      if (manageRoles == null)
      {
         if (other.manageRoles != null)
            return false;
      }
      else if (!manageRoles.equals(other.manageRoles))
         return false;
      if (sendRoles == null)
      {
         if (other.sendRoles != null)
            return false;
      }
      else if (!sendRoles.equals(other.sendRoles))
         return false;
      if (storeId != other.storeId)
         return false;
      return true;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "PersistedRoles [storeId=" + storeId +
             ", addressMatch=" +
             addressMatch +
             ", sendRoles=" +
             sendRoles +
             ", consumeRoles=" +
             consumeRoles +
             ", createDurableQueueRoles=" +
             createDurableQueueRoles +
             ", deleteDurableQueueRoles=" +
             deleteDurableQueueRoles +
             ", createNonDurableQueueRoles=" +
             createNonDurableQueueRoles +
             ", deleteNonDurableQueueRoles=" +
             deleteNonDurableQueueRoles +
             ", manageRoles=" +
             manageRoles +
             "]";
   }



   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}
