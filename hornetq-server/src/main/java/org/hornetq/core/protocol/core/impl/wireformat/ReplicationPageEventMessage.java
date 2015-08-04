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
package org.hornetq.core.protocol.core.impl.wireformat;

import org.hornetq.api.core.HornetQBuffer;
import org.hornetq.core.protocol.core.impl.PacketImpl;

/**
 * A ReplicationPageWrite
 * @author <mailto:clebert.suconic@jboss.org">Clebert Suconic</a>
 */
public class ReplicationPageEventMessage extends PacketImpl
{

   private int pageNumber;

   private String storeName;

   /**
    * True = delete page, False = close page
    */
   private boolean isDelete;

   public ReplicationPageEventMessage()
   {
      super(PacketImpl.REPLICATION_PAGE_EVENT);
   }

   public ReplicationPageEventMessage(final String storeName, final int pageNumber, final boolean isDelete)
   {
      this();
      this.pageNumber = pageNumber;
      this.isDelete = isDelete;
      this.storeName = storeName;
   }

   @Override
   public void encodeRest(final HornetQBuffer buffer)
   {
      buffer.writeSimpleString(storeName);
      buffer.writeInt(pageNumber);
      buffer.writeBoolean(isDelete);
   }

   @Override
   public void decodeRest(final HornetQBuffer buffer)
   {
      storeName = buffer.readSimpleString();
      pageNumber = buffer.readInt();
      isDelete = buffer.readBoolean();
   }

   /**
    * @return the pageNumber
    */
   public int getPageNumber()
   {
      return pageNumber;
   }

   /**
    * @return the storeName
    */
   public String getStoreName()
   {
      return storeName;
   }

   /**
    * @return the isDelete
    */
   public boolean isDelete()
   {
      return isDelete;
   }

   @Override
   public String toString()
   {
      return ReplicationPageEventMessage.class.getSimpleName() + "(channel=" + channelID + ", isDelete=" + isDelete +
               ", storeName=" + storeName + ", pageNumber=" + pageNumber + ")";
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + (isDelete ? 1231 : 1237);
      result = prime * result + pageNumber;
      result = prime * result + ((storeName == null) ? 0 : storeName.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (getClass() != obj.getClass())
         return false;
      ReplicationPageEventMessage other = (ReplicationPageEventMessage)obj;
      if (isDelete != other.isDelete)
         return false;
      if (pageNumber != other.pageNumber)
         return false;
      if (storeName == null)
      {
         if (other.storeName != null)
            return false;
      }
      else if (!storeName.equals(other.storeName))
         return false;
      return true;
   }
}
