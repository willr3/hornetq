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

package org.hornetq.core.protocol;

import org.hornetq.api.core.HornetQBuffer;
import org.hornetq.core.protocol.core.Packet;
import org.hornetq.core.protocol.core.impl.PacketDecoder;
import org.hornetq.core.protocol.core.impl.PacketImpl;
import org.hornetq.core.protocol.core.impl.wireformat.BackupRegistrationMessage;
import org.hornetq.core.protocol.core.impl.wireformat.BackupReplicationStartFailedMessage;
import org.hornetq.core.protocol.core.impl.wireformat.ReplicationAddMessage;
import org.hornetq.core.protocol.core.impl.wireformat.ReplicationAddTXMessage;
import org.hornetq.core.protocol.core.impl.wireformat.ReplicationCommitMessage;
import org.hornetq.core.protocol.core.impl.wireformat.ReplicationDeleteMessage;
import org.hornetq.core.protocol.core.impl.wireformat.ReplicationDeleteTXMessage;
import org.hornetq.core.protocol.core.impl.wireformat.ReplicationLargeMessageBeginMessage;
import org.hornetq.core.protocol.core.impl.wireformat.ReplicationLargeMessageEndMessage;
import org.hornetq.core.protocol.core.impl.wireformat.ReplicationLargeMessageWriteMessage;
import org.hornetq.core.protocol.core.impl.wireformat.ReplicationLiveIsStoppingMessage;
import org.hornetq.core.protocol.core.impl.wireformat.ReplicationPageEventMessage;
import org.hornetq.core.protocol.core.impl.wireformat.ReplicationPageWriteMessage;
import org.hornetq.core.protocol.core.impl.wireformat.ReplicationPrepareMessage;
import org.hornetq.core.protocol.core.impl.wireformat.ReplicationResponseMessage;
import org.hornetq.core.protocol.core.impl.wireformat.ReplicationStartSyncMessage;
import org.hornetq.core.protocol.core.impl.wireformat.ReplicationSyncFileMessage;
import org.hornetq.core.protocol.core.impl.wireformat.SessionSendLargeMessage;
import org.hornetq.core.protocol.core.impl.wireformat.SessionSendMessage;
import org.hornetq.core.server.impl.ServerMessageImpl;

import static org.hornetq.core.protocol.core.impl.PacketImpl.REPLICATION_APPEND;
import static org.hornetq.core.protocol.core.impl.PacketImpl.REPLICATION_APPEND_TX;
import static org.hornetq.core.protocol.core.impl.PacketImpl.REPLICATION_COMMIT_ROLLBACK;
import static org.hornetq.core.protocol.core.impl.PacketImpl.REPLICATION_DELETE;
import static org.hornetq.core.protocol.core.impl.PacketImpl.REPLICATION_DELETE_TX;
import static org.hornetq.core.protocol.core.impl.PacketImpl.REPLICATION_LARGE_MESSAGE_BEGIN;
import static org.hornetq.core.protocol.core.impl.PacketImpl.REPLICATION_LARGE_MESSAGE_END;
import static org.hornetq.core.protocol.core.impl.PacketImpl.REPLICATION_LARGE_MESSAGE_WRITE;
import static org.hornetq.core.protocol.core.impl.PacketImpl.REPLICATION_PAGE_EVENT;
import static org.hornetq.core.protocol.core.impl.PacketImpl.REPLICATION_PAGE_WRITE;
import static org.hornetq.core.protocol.core.impl.PacketImpl.REPLICATION_PREPARE;
import static org.hornetq.core.protocol.core.impl.PacketImpl.REPLICATION_RESPONSE;
import static org.hornetq.core.protocol.core.impl.PacketImpl.SESS_SEND;
import static org.hornetq.core.protocol.core.impl.PacketImpl.SESS_SEND_LARGE;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 *         10/12/12
 */
public class ServerPacketDecoder extends PacketDecoder
{
   private static final long serialVersionUID = 3348673114388400766L;
   public static final ServerPacketDecoder INSTANCE = new ServerPacketDecoder();

   @Override
   public Packet decode(final HornetQBuffer in)
   {
      final byte packetType = in.readByte();

      Packet packet;

      switch (packetType)
      {

         case SESS_SEND:
         {
            packet = new SessionSendMessage(new ServerMessageImpl());
            break;
         }
         case SESS_SEND_LARGE:
         {
            packet = new SessionSendLargeMessage(new ServerMessageImpl());
            break;
         }
         case REPLICATION_APPEND:
         {
            packet = new ReplicationAddMessage();
            break;
         }
         case REPLICATION_APPEND_TX:
         {
            packet = new ReplicationAddTXMessage();
            break;
         }
         case REPLICATION_DELETE:
         {
            packet = new ReplicationDeleteMessage();
            break;
         }
         case REPLICATION_DELETE_TX:
         {
            packet = new ReplicationDeleteTXMessage();
            break;
         }
         case REPLICATION_PREPARE:
         {
            packet = new ReplicationPrepareMessage();
            break;
         }
         case REPLICATION_COMMIT_ROLLBACK:
         {
            packet = new ReplicationCommitMessage();
            break;
         }
         case REPLICATION_RESPONSE:
         {
            packet = new ReplicationResponseMessage();
            break;
         }
         case REPLICATION_PAGE_WRITE:
         {
            packet = new ReplicationPageWriteMessage();
            break;
         }
         case REPLICATION_PAGE_EVENT:
         {
            packet = new ReplicationPageEventMessage();
            break;
         }
         case REPLICATION_LARGE_MESSAGE_BEGIN:
         {
            packet = new ReplicationLargeMessageBeginMessage();
            break;
         }
         case REPLICATION_LARGE_MESSAGE_END:
         {
            packet = new ReplicationLargeMessageEndMessage();
            break;
         }
         case REPLICATION_LARGE_MESSAGE_WRITE:
         {
            packet = new ReplicationLargeMessageWriteMessage();
            break;
         }
         case PacketImpl.BACKUP_REGISTRATION:
         {
            packet = new BackupRegistrationMessage();
            break;
         }
         case PacketImpl.BACKUP_REGISTRATION_FAILED:
         {
            packet = new BackupReplicationStartFailedMessage();
            break;
         }
         case PacketImpl.REPLICATION_START_FINISH_SYNC:
         {
            packet = new ReplicationStartSyncMessage();
            break;
         }
         case PacketImpl.REPLICATION_SYNC_FILE:
         {
            packet = new ReplicationSyncFileMessage();
            break;
         }
         case PacketImpl.REPLICATION_SCHEDULED_FAILOVER:
         {
            packet = new ReplicationLiveIsStoppingMessage();
            break;
         }
         default:
         {
            packet = super.decode(packetType);
         }
      }

      packet.decode(in);

      return packet;
   }

}
