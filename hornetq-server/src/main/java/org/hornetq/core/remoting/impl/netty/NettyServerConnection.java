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
package org.hornetq.core.remoting.impl.netty;

import io.netty.channel.Channel;
import org.hornetq.api.core.HornetQBuffer;
import org.hornetq.core.buffers.impl.ChannelBufferWrapper;
import org.hornetq.spi.core.remoting.ConnectionLifeCycleListener;

import java.util.Map;

/**
 * @author <a href="mailto:nmaurer@redhat.com">Norman Maurer</a>
 */
public class NettyServerConnection extends NettyConnection
{
   public NettyServerConnection(Map<String, Object> configuration, Channel channel, ConnectionLifeCycleListener listener, boolean batchingEnabled, boolean directDeliver)
   {
      super(configuration, channel, listener, batchingEnabled, directDeliver);
   }

   @Override
   public HornetQBuffer createTransportBuffer(int size)
   {
      return new ChannelBufferWrapper(channel.alloc().directBuffer(size), true);
   }
}
