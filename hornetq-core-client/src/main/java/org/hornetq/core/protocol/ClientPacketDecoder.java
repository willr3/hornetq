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
import org.hornetq.core.client.impl.ClientLargeMessageImpl;
import org.hornetq.core.client.impl.ClientMessageImpl;
import org.hornetq.core.protocol.core.Packet;
import org.hornetq.core.protocol.core.impl.PacketDecoder;
import org.hornetq.core.protocol.core.impl.wireformat.SessionReceiveClientLargeMessage;
import org.hornetq.core.protocol.core.impl.wireformat.SessionReceiveMessage;

import static org.hornetq.core.protocol.core.impl.PacketImpl.SESS_RECEIVE_LARGE_MSG;
import static org.hornetq.core.protocol.core.impl.PacketImpl.SESS_RECEIVE_MSG;
/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 *         10/12/12
 */
public class ClientPacketDecoder extends PacketDecoder
{
   private static final long serialVersionUID = 6952614096979334582L;
   public static final ClientPacketDecoder INSTANCE = new ClientPacketDecoder();

   @Override
   public  Packet decode(final HornetQBuffer in)
   {
      final byte packetType = in.readByte();

      Packet packet;

      switch (packetType)
      {
         case SESS_RECEIVE_MSG:
         {
            packet = new SessionReceiveMessage(new ClientMessageImpl());
            break;
         }
         case SESS_RECEIVE_LARGE_MSG:
         {
            packet = new SessionReceiveClientLargeMessage(new ClientLargeMessageImpl());
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
