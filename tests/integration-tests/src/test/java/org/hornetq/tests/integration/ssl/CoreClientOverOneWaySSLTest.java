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
package org.hornetq.tests.integration.ssl;

import org.hornetq.api.core.HornetQConnectionTimedOutException;
import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.HornetQNotConnectedException;
import org.hornetq.api.core.Message;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.HornetQClient;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.hornetq.core.remoting.impl.ssl.SSLSupport;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.tests.integration.IntegrationTestLogger;
import org.hornetq.tests.util.RandomUtil;
import org.hornetq.tests.util.ServiceTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:jmesnil@redhat.com">Jeff Mesnil</a>
 * @version <tt>$Revision: 3716 $</tt>
 */
public class CoreClientOverOneWaySSLTest extends ServiceTestBase
{
   // Constants -----------------------------------------------------

   public static final String QUEUE = new String("QueueOverSSL");

   public static final String SERVER_SIDE_KEYSTORE = "server-side.keystore";
   public static final String CLIENT_SIDE_TRUSTSTORE = "client-side.truststore";
   public static final String PASSWORD = "secureexample";

   private HornetQServer server;

   private TransportConfiguration tc;

   @Test
   public void testOneWaySSL() throws Exception
   {
      createCustomSslServer();
      String text = RandomUtil.randomString();

      tc.getParams().put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, CLIENT_SIDE_TRUSTSTORE);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, PASSWORD);

      ServerLocator locator = addServerLocator(HornetQClient.createServerLocatorWithoutHA(tc));
      ClientSessionFactory sf = addSessionFactory(createSessionFactory(locator));
      ClientSession session = addClientSession(sf.createSession(false, true, true));
      session.createQueue(CoreClientOverOneWaySSLTest.QUEUE, CoreClientOverOneWaySSLTest.QUEUE, false);
      ClientProducer producer = addClientProducer(session.createProducer(CoreClientOverOneWaySSLTest.QUEUE));

      ClientMessage message = createTextMessage(session, text);
      producer.send(message);

      ClientConsumer consumer = addClientConsumer(session.createConsumer(CoreClientOverOneWaySSLTest.QUEUE));
      session.start();

      Message m = consumer.receive(1000);
      Assert.assertNotNull(m);
      Assert.assertEquals(text, m.getBodyBuffer().readString());
   }

   @Test
   public void testOneWaySSLWithBadClientCipherSuite() throws Exception
   {
      createCustomSslServer();
      tc.getParams().put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, CLIENT_SIDE_TRUSTSTORE);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, PASSWORD);
      tc.getParams().put(TransportConstants.ENABLED_CIPHER_SUITES_PROP_NAME, "myBadCipherSuite");

      ServerLocator locator = addServerLocator(HornetQClient.createServerLocatorWithoutHA(tc));
      try
      {
         createSessionFactory(locator);
         Assert.fail();
      }
      catch (HornetQNotConnectedException e)
      {
         Assert.assertTrue(true);
      }
   }

   @Test
   public void testOneWaySSLWithBadServerCipherSuite() throws Exception
   {
      createCustomSslServer("myBadCipherSuite", null);
      tc.getParams().put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, CLIENT_SIDE_TRUSTSTORE);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, PASSWORD);

      ServerLocator locator = addServerLocator(HornetQClient.createServerLocatorWithoutHA(tc));
      try
      {
         createSessionFactory(locator);
         Assert.fail();
      }
      catch (HornetQNotConnectedException e)
      {
         Assert.assertTrue(true);
      }
   }

   @Test
   public void testOneWaySSLWithMismatchedCipherSuites() throws Exception
   {
      createCustomSslServer(getEnabledCipherSuites()[0], "TLSv1.2");
      tc.getParams().put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, CLIENT_SIDE_TRUSTSTORE);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, PASSWORD);
      tc.getParams().put(TransportConstants.ENABLED_CIPHER_SUITES_PROP_NAME, getEnabledCipherSuites()[1]);
      tc.getParams().put(TransportConstants.ENABLED_PROTOCOLS_PROP_NAME, "TLSv1.2");

      ServerLocator locator = addServerLocator(HornetQClient.createServerLocatorWithoutHA(tc));
      try
      {
         createSessionFactory(locator);
         Assert.fail();
      }
      catch (HornetQNotConnectedException e)
      {
         Assert.assertTrue(true);
      }
   }

   @Test
   public void testOneWaySSLWithBadClientProtocol() throws Exception
   {
      createCustomSslServer();
      tc.getParams().put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, CLIENT_SIDE_TRUSTSTORE);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, PASSWORD);
      tc.getParams().put(TransportConstants.ENABLED_PROTOCOLS_PROP_NAME, "myBadProtocol");

      ServerLocator locator = addServerLocator(HornetQClient.createServerLocatorWithoutHA(tc));
      try
      {
         createSessionFactory(locator);
         Assert.fail();
      }
      catch (HornetQNotConnectedException e)
      {
         Assert.assertTrue(true);
      }
   }

   @Test
   public void testOneWaySSLWithBadServerProtocol() throws Exception
   {
      createCustomSslServer(null, "myBadProtocol");
      tc.getParams().put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, CLIENT_SIDE_TRUSTSTORE);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, PASSWORD);

      ServerLocator locator = addServerLocator(HornetQClient.createServerLocatorWithoutHA(tc));
      try
      {
         createSessionFactory(locator);
         Assert.fail();
      }
      catch (HornetQNotConnectedException e)
      {
         Assert.assertTrue(true);
      }
   }

   @Test
   public void testOneWaySSLWithMismatchedProtocols() throws Exception
   {
      createCustomSslServer(null, "TLSv1");
      tc.getParams().put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, CLIENT_SIDE_TRUSTSTORE);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, PASSWORD);
      tc.getParams().put(TransportConstants.ENABLED_PROTOCOLS_PROP_NAME, "TLSv1.2");

      ServerLocator locator = addServerLocator(HornetQClient.createServerLocatorWithoutHA(tc));
      try
      {
         createSessionFactory(locator);
         Assert.fail();
      }
      catch (HornetQNotConnectedException e)
      {
         Assert.assertTrue(true);
      }
   }

   @Test
   // http://www.oracle.com/technetwork/topics/security/poodlecve-2014-3566-2339408.html
   public void testPOODLE() throws Exception
   {
      createCustomSslServer(null, "SSLv3");
      tc.getParams().put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, CLIENT_SIDE_TRUSTSTORE);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, PASSWORD);
      tc.getParams().put(TransportConstants.ENABLED_PROTOCOLS_PROP_NAME, "SSLv3");

      ServerLocator locator = addServerLocator(HornetQClient.createServerLocatorWithoutHA(tc));
      try
      {
         createSessionFactory(locator);
         Assert.fail();
      }
      catch (HornetQNotConnectedException e)
      {
         Assert.assertTrue(true);
      }
   }

   @Test
   public void disabled_testOneWaySSLWithGoodClientCipherSuite() throws Exception
   {
      createCustomSslServer();
      String text = RandomUtil.randomString();

      tc.getParams().put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, CLIENT_SIDE_TRUSTSTORE);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, PASSWORD);
      tc.getParams().put(TransportConstants.ENABLED_CIPHER_SUITES_PROP_NAME, getSuitableCipherSuite());
      tc.getParams().put(TransportConstants.ENABLED_PROTOCOLS_PROP_NAME, "TLSv1.2");

      ServerLocator locator = addServerLocator(HornetQClient.createServerLocatorWithoutHA(tc));
      ClientSessionFactory sf = null;
      try
      {
         sf = createSessionFactory(locator);
      }
      catch (HornetQNotConnectedException e)
      {
         Assert.fail();
      }

      ClientSession session = sf.createSession(false, true, true);
      session.createQueue(CoreClientOverOneWaySSLTest.QUEUE, CoreClientOverOneWaySSLTest.QUEUE, false);
      ClientProducer producer = session.createProducer(CoreClientOverOneWaySSLTest.QUEUE);

      ClientMessage message = createTextMessage(session, text);
      producer.send(message);

      ClientConsumer consumer = session.createConsumer(CoreClientOverOneWaySSLTest.QUEUE);
      session.start();

      Message m = consumer.receive(1000);
      Assert.assertNotNull(m);
      Assert.assertEquals(text, m.getBodyBuffer().readString());
   }

   @Test
   public void disabled_testOneWaySSLWithGoodServerCipherSuite() throws Exception
   {
      createCustomSslServer(getSuitableCipherSuite(), null);
      String text = RandomUtil.randomString();

      tc.getParams().put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, CLIENT_SIDE_TRUSTSTORE);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, PASSWORD);
      tc.getParams().put(TransportConstants.ENABLED_PROTOCOLS_PROP_NAME, "TLSv1.2");

      ServerLocator locator = addServerLocator(HornetQClient.createServerLocatorWithoutHA(tc));
      ClientSessionFactory sf = null;
      try
      {
         sf = createSessionFactory(locator);
      }
      catch (HornetQNotConnectedException e)
      {
         Assert.fail();
      }

      ClientSession session = sf.createSession(false, true, true);
      session.createQueue(CoreClientOverOneWaySSLTest.QUEUE, CoreClientOverOneWaySSLTest.QUEUE, false);
      ClientProducer producer = session.createProducer(CoreClientOverOneWaySSLTest.QUEUE);

      ClientMessage message = createTextMessage(session, text);
      producer.send(message);

      ClientConsumer consumer = session.createConsumer(CoreClientOverOneWaySSLTest.QUEUE);
      session.start();

      Message m = consumer.receive(1000);
      Assert.assertNotNull(m);
      Assert.assertEquals(text, m.getBodyBuffer().readString());
   }

   @Test
   public void testOneWaySSLWithGoodClientProtocol() throws Exception
   {
      createCustomSslServer();
      String text = RandomUtil.randomString();

      tc.getParams().put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, CLIENT_SIDE_TRUSTSTORE);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, PASSWORD);
      tc.getParams().put(TransportConstants.ENABLED_PROTOCOLS_PROP_NAME, "TLSv1");

      ServerLocator locator = addServerLocator(HornetQClient.createServerLocatorWithoutHA(tc));
      ClientSessionFactory sf = null;
      try
      {
         sf = createSessionFactory(locator);
         Assert.assertTrue(true);
      }
      catch (HornetQNotConnectedException e)
      {
         Assert.fail();
      }

      ClientSession session = sf.createSession(false, true, true);
      session.createQueue(CoreClientOverOneWaySSLTest.QUEUE, CoreClientOverOneWaySSLTest.QUEUE, false);
      ClientProducer producer = session.createProducer(CoreClientOverOneWaySSLTest.QUEUE);

      ClientMessage message = createTextMessage(session, text);
      producer.send(message);

      ClientConsumer consumer = session.createConsumer(CoreClientOverOneWaySSLTest.QUEUE);
      session.start();

      Message m = consumer.receive(1000);
      Assert.assertNotNull(m);
      Assert.assertEquals(text, m.getBodyBuffer().readString());
   }

   @Test
   public void testOneWaySSLWithGoodServerProtocol() throws Exception
   {
      createCustomSslServer(null, "TLSv1");
      String text = RandomUtil.randomString();

      tc.getParams().put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, CLIENT_SIDE_TRUSTSTORE);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, PASSWORD);

      ServerLocator locator = addServerLocator(HornetQClient.createServerLocatorWithoutHA(tc));
      ClientSessionFactory sf = null;
      try
      {
         sf = createSessionFactory(locator);
         Assert.assertTrue(true);
      }
      catch (HornetQNotConnectedException e)
      {
         Assert.fail();
      }

      ClientSession session = sf.createSession(false, true, true);
      session.createQueue(CoreClientOverOneWaySSLTest.QUEUE, CoreClientOverOneWaySSLTest.QUEUE, false);
      ClientProducer producer = session.createProducer(CoreClientOverOneWaySSLTest.QUEUE);

      ClientMessage message = createTextMessage(session, text);
      producer.send(message);

      ClientConsumer consumer = session.createConsumer(CoreClientOverOneWaySSLTest.QUEUE);
      session.start();

      Message m = consumer.receive(1000);
      Assert.assertNotNull(m);
      Assert.assertEquals(text, m.getBodyBuffer().readString());
   }

   public static String getSuitableCipherSuite() throws Exception
   {
      String result = "";

      String[] suites = getEnabledCipherSuites();

      // The certs are generated using Java keytool using RSA and not ECDSA but the JVM prefers ECDSA over RSA so we have
      // to look through the cipher suites until we find one that's suitable for us.
      // If the JVM running this test is version 7 from Oracle then this cipher suite will will almost certainly require
      // TLSv1.2 (which is not enabled on the client by default).
      // See http://docs.oracle.com/javase/7/docs/technotes/guides/security/SunProviders.html#SunJSSEProvider for the
      // preferred cipher suites.
      for (int i = 0; i < suites.length; i++)
      {
         String suite = suites[i];
         if (!suite.contains("ECDSA") && suite.contains("RSA"))
         {
            result = suite;
            break;
         }
      }

      IntegrationTestLogger.LOGGER.info("Using suite: " + result);
      return result;
   }

   public static String[] getEnabledCipherSuites() throws Exception
   {
      SSLContext context = SSLSupport.createContext("JKS", SERVER_SIDE_KEYSTORE, PASSWORD, "JKS", CLIENT_SIDE_TRUSTSTORE, PASSWORD);
      SSLEngine engine = context.createSSLEngine();
      return engine.getEnabledCipherSuites();
   }

   @Test
   public void testOneWaySSLWithoutTrustStore() throws Exception
   {
      createCustomSslServer();
      tc.getParams().put(TransportConstants.SSL_ENABLED_PROP_NAME, true);

      ServerLocator locator = addServerLocator(HornetQClient.createServerLocatorWithoutHA(tc));
      try
      {
         createSessionFactory(locator);
         Assert.fail();
      }
      catch (HornetQNotConnectedException se)
      {
         //ok
      }
      catch (HornetQException e)
      {
         fail("Invalid Exception type:" + e.getType());
      }
   }

   @Test
   public void testOneWaySSLWithIncorrectTrustStorePassword() throws Exception
   {
      createCustomSslServer();
      tc.getParams().put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, CLIENT_SIDE_TRUSTSTORE);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, "invalid password");

      ServerLocator locator = addServerLocator(HornetQClient.createServerLocatorWithoutHA(tc));
      try
      {
         ClientSessionFactory sf = createSessionFactory(locator);
         Assert.fail();
      }
      catch (HornetQNotConnectedException se)
      {
         //ok
      }
      catch (HornetQException e)
      {
         fail("Invalid Exception type:" + e.getType());
      }
   }

   @Test
   public void testOneWaySSLWithIncorrectTrustStorePath() throws Exception
   {
      createCustomSslServer();
      tc.getParams().put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
      tc.getParams().put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, "incorrect path");
      tc.getParams().put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, PASSWORD);

      ServerLocator locator = addServerLocator(HornetQClient.createServerLocatorWithoutHA(tc));
      try
      {
         ClientSessionFactory sf = createSessionFactory(locator);
         Assert.fail();
      }
      catch (HornetQNotConnectedException se)
      {
         //ok
      }
      catch (HornetQException e)
      {
         fail("Invalid Exception type:" + e.getType());
      }
   }

   // see https://jira.jboss.org/jira/browse/HORNETQ-234
   @Test
   public void testPlainConnectionToSSLEndpoint() throws Exception
   {
      createCustomSslServer();
      tc.getParams().put(TransportConstants.SSL_ENABLED_PROP_NAME, false);

      ServerLocator locator = addServerLocator(HornetQClient.createServerLocatorWithoutHA(tc));
      locator.setCallTimeout(2000);
      try
      {
         createSessionFactory(locator);
         fail("expecting exception");
      }
      catch (HornetQNotConnectedException se)
      {
         //ok
      }
      catch (HornetQConnectionTimedOutException ctoe)
      {
         //ok
      }
      catch (HornetQException e)
      {
         fail("Invalid Exception type:" + e.getType());
      }
   }

   // Package protected ---------------------------------------------

   @Override
   @Before
   public void setUp() throws Exception
   {
      super.setUp();
   }

   private void createCustomSslServer() throws Exception
   {
      createCustomSslServer(null, null);
   }

   private void createCustomSslServer(String cipherSuites, String protocols) throws Exception
   {
      ConfigurationImpl config = createBasicConfig();
      config.setSecurityEnabled(false);
      Map<String, Object> params = new HashMap<String, Object>();
      params.put(TransportConstants.SSL_ENABLED_PROP_NAME, true);
      params.put(TransportConstants.KEYSTORE_PATH_PROP_NAME, SERVER_SIDE_KEYSTORE);
      params.put(TransportConstants.KEYSTORE_PASSWORD_PROP_NAME, PASSWORD);

      if (cipherSuites != null)
      {
         params.put(TransportConstants.ENABLED_CIPHER_SUITES_PROP_NAME, cipherSuites);
      }

      if (protocols != null)
      {
         params.put(TransportConstants.ENABLED_PROTOCOLS_PROP_NAME, protocols);
      }

      config.getAcceptorConfigurations().add(new TransportConfiguration(NETTY_ACCEPTOR_FACTORY, params));
      server = createServer(false, config);
      server.start();
      waitForServer(server);
      tc = new TransportConfiguration(NETTY_CONNECTOR_FACTORY);
   }
}
