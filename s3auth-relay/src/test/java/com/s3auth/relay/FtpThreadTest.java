/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import com.s3auth.hosts.Host;
import com.s3auth.hosts.Hosts;
import com.s3auth.hosts.Range;
import com.s3auth.hosts.Resource;
import com.s3auth.hosts.ResourceMocker;
import com.s3auth.hosts.Version;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

/**
 * Test case for {@link FtpThread}.
 *
 * @since 0.0.1
 */
final class FtpThreadTest {

    /**
     * Domain handled by the mocked hosts.
     */
    private static final String DOMAIN = "ftp.example.com";

    /**
     * Valid credentials, in the user:password format.
     */
    private static final String CREDS = "alex:secret";

    /**
     * Content of the resource served by the mocked host.
     */
    private static final String CONTENT = "hello, world!";

    /**
     * FtpThread can greet the client on connect and say bye on QUIT.
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
    void greetsAndSaysByeOnQuit() throws Exception {
        try (
            ServerSocket server = new ServerSocket(0);
            Socket client = new Socket("localhost", server.getLocalPort());
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    client.getInputStream(), StandardCharsets.UTF_8
                )
            )
        ) {
            final BlockingQueue<Socket> sockets = new LinkedBlockingQueue<>();
            sockets.put(server.accept());
            final Thread worker = FtpThreadTest.dispatch(
                new FtpThread(sockets, FtpThreadTest.mockHosts())
            );
            MatcherAssert.assertThat(
                reader.readLine(),
                Matchers.startsWith("220 ")
            );
            FtpThreadTest.write(client, "QUIT");
            MatcherAssert.assertThat(
                reader.readLine(),
                Matchers.startsWith("221 ")
            );
            worker.join();
        }
    }

    /**
     * FtpThread can reject an unknown domain in the USER command.
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    void rejectsUnknownDomain() throws Exception {
        final Hosts hosts = FtpThreadTest.mockHosts();
        Mockito.doThrow(new Hosts.NotFoundException("no such domain"))
            .when(hosts)
            .find("unknown.example.org");
        try (
            ServerSocket server = new ServerSocket(0);
            Socket client = new Socket("localhost", server.getLocalPort());
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    client.getInputStream(), StandardCharsets.UTF_8
                )
            )
        ) {
            final BlockingQueue<Socket> sockets = new LinkedBlockingQueue<>();
            sockets.put(server.accept());
            final Thread worker = FtpThreadTest.dispatch(
                new FtpThread(sockets, hosts)
            );
            reader.readLine();
            FtpThreadTest.write(client, "USER unknown.example.org");
            MatcherAssert.assertThat(
                reader.readLine(),
                Matchers.startsWith("530 ")
            );
            FtpThreadTest.write(client, "QUIT");
            reader.readLine();
            worker.join();
        }
    }

    /**
     * FtpThread can authenticate a user with valid credentials.
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    void acceptsValidCredentials() throws Exception {
        try (
            ServerSocket server = new ServerSocket(0);
            Socket client = new Socket("localhost", server.getLocalPort());
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    client.getInputStream(), StandardCharsets.UTF_8
                )
            )
        ) {
            final BlockingQueue<Socket> sockets = new LinkedBlockingQueue<>();
            sockets.put(server.accept());
            final Thread worker = FtpThreadTest.dispatch(
                new FtpThread(sockets, FtpThreadTest.mockHosts())
            );
            reader.readLine();
            FtpThreadTest.login(client, reader);
            FtpThreadTest.write(client, "QUIT");
            MatcherAssert.assertThat(
                reader.readLine(),
                Matchers.startsWith("221 ")
            );
            worker.join();
        }
    }

    /**
     * FtpThread can reject invalid credentials in the PASS command.
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    void rejectsInvalidCredentials() throws Exception {
        try (
            ServerSocket server = new ServerSocket(0);
            Socket client = new Socket("localhost", server.getLocalPort());
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    client.getInputStream(), StandardCharsets.UTF_8
                )
            )
        ) {
            final BlockingQueue<Socket> sockets = new LinkedBlockingQueue<>();
            sockets.put(server.accept());
            final Thread worker = FtpThreadTest.dispatch(
                new FtpThread(sockets, FtpThreadTest.mockHosts())
            );
            reader.readLine();
            FtpThreadTest.write(
                client,
                String.format("USER %s", FtpThreadTest.DOMAIN)
            );
            reader.readLine();
            FtpThreadTest.write(client, "PASS alex:wrong");
            MatcherAssert.assertThat(
                reader.readLine(),
                Matchers.startsWith("530 ")
            );
            FtpThreadTest.write(client, "QUIT");
            reader.readLine();
            worker.join();
        }
    }

    /**
     * FtpThread can reject the PASS command sent before USER.
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    void rejectsPassWithoutUser() throws Exception {
        try (
            ServerSocket server = new ServerSocket(0);
            Socket client = new Socket("localhost", server.getLocalPort());
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    client.getInputStream(), StandardCharsets.UTF_8
                )
            )
        ) {
            final BlockingQueue<Socket> sockets = new LinkedBlockingQueue<>();
            sockets.put(server.accept());
            final Thread worker = FtpThreadTest.dispatch(
                new FtpThread(sockets, FtpThreadTest.mockHosts())
            );
            reader.readLine();
            FtpThreadTest.write(
                client,
                String.format("PASS %s", FtpThreadTest.CREDS)
            );
            MatcherAssert.assertThat(
                reader.readLine(),
                Matchers.startsWith("530 ")
            );
            FtpThreadTest.write(client, "QUIT");
            reader.readLine();
            worker.join();
        }
    }

    /**
     * FtpThread can reject the RETR command sent before login.
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    void rejectsRetrBeforeLogin() throws Exception {
        try (
            ServerSocket server = new ServerSocket(0);
            Socket client = new Socket("localhost", server.getLocalPort());
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    client.getInputStream(), StandardCharsets.UTF_8
                )
            )
        ) {
            final BlockingQueue<Socket> sockets = new LinkedBlockingQueue<>();
            sockets.put(server.accept());
            final Thread worker = FtpThreadTest.dispatch(
                new FtpThread(sockets, FtpThreadTest.mockHosts())
            );
            reader.readLine();
            FtpThreadTest.write(client, "RETR /index.html");
            MatcherAssert.assertThat(
                reader.readLine(),
                Matchers.startsWith("530 ")
            );
            FtpThreadTest.write(client, "QUIT");
            reader.readLine();
            worker.join();
        }
    }

    /**
     * FtpThread can reject the RETR command sent without a data
     * connection negotiated by PORT.
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    void rejectsRetrWithoutPort() throws Exception {
        try (
            ServerSocket server = new ServerSocket(0);
            Socket client = new Socket("localhost", server.getLocalPort());
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    client.getInputStream(), StandardCharsets.UTF_8
                )
            )
        ) {
            final BlockingQueue<Socket> sockets = new LinkedBlockingQueue<>();
            sockets.put(server.accept());
            final Thread worker = FtpThreadTest.dispatch(
                new FtpThread(sockets, FtpThreadTest.mockHosts())
            );
            reader.readLine();
            FtpThreadTest.login(client, reader);
            FtpThreadTest.write(client, "RETR /index.html");
            MatcherAssert.assertThat(
                reader.readLine(),
                Matchers.startsWith("425 ")
            );
            FtpThreadTest.write(client, "QUIT");
            reader.readLine();
            worker.join();
        }
    }

    /**
     * FtpThread can relay a resource over the data connection on RETR.
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    void relaysResourceOnRetr() throws Exception {
        try (
            ServerSocket server = new ServerSocket(0);
            Socket client = new Socket("localhost", server.getLocalPort());
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    client.getInputStream(), StandardCharsets.UTF_8
                )
            )
        ) {
            final BlockingQueue<Socket> sockets = new LinkedBlockingQueue<>();
            sockets.put(server.accept());
            final Thread worker = FtpThreadTest.dispatch(
                new FtpThread(sockets, FtpThreadTest.mockHosts())
            );
            reader.readLine();
            FtpThreadTest.login(client, reader);
            FtpThreadTest.dataDance(client, reader);
            FtpThreadTest.write(client, "QUIT");
            MatcherAssert.assertThat(
                reader.readLine(),
                Matchers.startsWith("221 ")
            );
            worker.join();
        }
    }

    /**
     * FtpThread can report a failure when the resource can't be fetched.
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    void reportsFailureOnBrokenFetch() throws Exception {
        final Host host = Mockito.mock(Host.class);
        Mockito.doReturn(true)
            .when(host)
            .authorized("alex", "secret");
        Mockito.doThrow(new IOException("boom")).when(host).fetch(
            Mockito.any(URI.class),
            Mockito.any(Range.class),
            Mockito.any(Version.class)
        );
        final Hosts hosts = Mockito.mock(Hosts.class);
        Mockito.doReturn(host).when(hosts).find(FtpThreadTest.DOMAIN);
        try (
            ServerSocket server = new ServerSocket(0);
            Socket client = new Socket("localhost", server.getLocalPort());
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    client.getInputStream(), StandardCharsets.UTF_8
                )
            )
        ) {
            final BlockingQueue<Socket> sockets = new LinkedBlockingQueue<>();
            sockets.put(server.accept());
            final Thread worker = FtpThreadTest.dispatch(
                new FtpThread(sockets, hosts)
            );
            reader.readLine();
            FtpThreadTest.login(client, reader);
            FtpThreadTest.write(client, "PORT 127,0,0,1,1,1");
            reader.readLine();
            FtpThreadTest.write(client, "RETR /missing.html");
            MatcherAssert.assertThat(
                reader.readLine(),
                Matchers.startsWith("550 ")
            );
            FtpThreadTest.write(client, "QUIT");
            reader.readLine();
            worker.join();
        }
    }

    /**
     * FtpThread can reject commands it does not support.
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    void rejectsUnsupportedCommand() throws Exception {
        try (
            ServerSocket server = new ServerSocket(0);
            Socket client = new Socket("localhost", server.getLocalPort());
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    client.getInputStream(), StandardCharsets.UTF_8
                )
            )
        ) {
            final BlockingQueue<Socket> sockets = new LinkedBlockingQueue<>();
            sockets.put(server.accept());
            final Thread worker = FtpThreadTest.dispatch(
                new FtpThread(sockets, FtpThreadTest.mockHosts())
            );
            reader.readLine();
            FtpThreadTest.write(client, "LIST");
            MatcherAssert.assertThat(
                reader.readLine(),
                Matchers.startsWith("502 ")
            );
            FtpThreadTest.write(client, "QUIT");
            reader.readLine();
            worker.join();
        }
    }

    /**
     * FtpThread can reject a PORT command pointing at an address other
     * than the control connection peer (FTP bounce attack).
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
    void rejectsPortToForeignAddress() throws Exception {
        try (
            ServerSocket server = new ServerSocket(0);
            Socket client = new Socket("localhost", server.getLocalPort());
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    client.getInputStream(), StandardCharsets.UTF_8
                )
            )
        ) {
            final BlockingQueue<Socket> sockets = new LinkedBlockingQueue<>();
            sockets.put(server.accept());
            final Thread worker = FtpThreadTest.dispatch(
                new FtpThread(sockets, FtpThreadTest.mockHosts())
            );
            reader.readLine();
            FtpThreadTest.login(client, reader);
            FtpThreadTest.write(client, "PORT 10,0,0,1,1,1");
            MatcherAssert.assertThat(
                reader.readLine(),
                Matchers.startsWith("504 ")
            );
            FtpThreadTest.write(client, "RETR /index.html");
            MatcherAssert.assertThat(
                reader.readLine(),
                Matchers.startsWith("425 ")
            );
            FtpThreadTest.write(client, "QUIT");
            reader.readLine();
            worker.join();
        }
    }

    /**
     * FtpThread can drop the authorized session state when USER fails
     * after a successful login.
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
    void resetsSessionOnFailedUser() throws Exception {
        final Hosts hosts = FtpThreadTest.mockHosts();
        Mockito.doThrow(new Hosts.NotFoundException("no such domain"))
            .when(hosts)
            .find("unknown.example.org");
        try (
            ServerSocket server = new ServerSocket(0);
            Socket client = new Socket("localhost", server.getLocalPort());
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    client.getInputStream(), StandardCharsets.UTF_8
                )
            )
        ) {
            final BlockingQueue<Socket> sockets = new LinkedBlockingQueue<>();
            sockets.put(server.accept());
            final Thread worker = FtpThreadTest.dispatch(
                new FtpThread(sockets, hosts)
            );
            reader.readLine();
            FtpThreadTest.login(client, reader);
            FtpThreadTest.write(client, "USER unknown.example.org");
            MatcherAssert.assertThat(
                reader.readLine(),
                Matchers.startsWith("530 ")
            );
            FtpThreadTest.write(client, "RETR /index.html");
            MatcherAssert.assertThat(
                reader.readLine(),
                Matchers.startsWith("530 ")
            );
            FtpThreadTest.write(client, "QUIT");
            reader.readLine();
            worker.join();
        }
    }

    /**
     * FtpThread can drop the authorized session state when PASS fails
     * after a successful login.
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
    void resetsAuthOnFailedPass() throws Exception {
        try (
            ServerSocket server = new ServerSocket(0);
            Socket client = new Socket("localhost", server.getLocalPort());
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    client.getInputStream(), StandardCharsets.UTF_8
                )
            )
        ) {
            final BlockingQueue<Socket> sockets = new LinkedBlockingQueue<>();
            sockets.put(server.accept());
            final Thread worker = FtpThreadTest.dispatch(
                new FtpThread(sockets, FtpThreadTest.mockHosts())
            );
            reader.readLine();
            FtpThreadTest.login(client, reader);
            FtpThreadTest.write(client, "PASS alex:wrong");
            MatcherAssert.assertThat(
                reader.readLine(),
                Matchers.startsWith("530 ")
            );
            FtpThreadTest.write(client, "RETR /index.html");
            MatcherAssert.assertThat(
                reader.readLine(),
                Matchers.startsWith("530 ")
            );
            FtpThreadTest.write(client, "QUIT");
            reader.readLine();
            worker.join();
        }
    }

    /**
     * FtpThread can reject RETR paths that are empty or not valid URIs.
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
    void rejectsInvalidRetrPaths() throws Exception {
        try (
            ServerSocket server = new ServerSocket(0);
            Socket client = new Socket("localhost", server.getLocalPort());
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    client.getInputStream(), StandardCharsets.UTF_8
                )
            )
        ) {
            final BlockingQueue<Socket> sockets = new LinkedBlockingQueue<>();
            sockets.put(server.accept());
            final Thread worker = FtpThreadTest.dispatch(
                new FtpThread(sockets, FtpThreadTest.mockHosts())
            );
            reader.readLine();
            FtpThreadTest.login(client, reader);
            try (ServerSocket data = new ServerSocket(0)) {
                final int port = data.getLocalPort();
                FtpThreadTest.write(
                    client,
                    String.format("PORT 127,0,0,1,%d,%d", port >> 8, port & 0xFF)
                );
                MatcherAssert.assertThat(
                    reader.readLine(),
                    Matchers.startsWith("200 ")
                );
            }
            FtpThreadTest.write(client, "RETR /hello world.html");
            MatcherAssert.assertThat(
                reader.readLine(),
                Matchers.startsWith("550 ")
            );
            FtpThreadTest.write(client, "RETR");
            MatcherAssert.assertThat(
                reader.readLine(),
                Matchers.startsWith("501 ")
            );
            FtpThreadTest.write(client, "QUIT");
            reader.readLine();
            worker.join();
        }
    }

    private static void dataDance(final Socket client,
        final BufferedReader reader) throws IOException {
        try (ServerSocket data = new ServerSocket(0)) {
            final int port = data.getLocalPort();
            FtpThreadTest.write(
                client,
                String.format("PORT 127,0,0,1,%d,%d", port >> 8, port & 0xFF)
            );
            MatcherAssert.assertThat(
                reader.readLine(),
                Matchers.startsWith("200 ")
            );
            FtpThreadTest.write(client, "RETR /index.html");
            MatcherAssert.assertThat(
                reader.readLine(),
                Matchers.startsWith("150 ")
            );
            MatcherAssert.assertThat(
                FtpThreadTest.readContent(data),
                Matchers.equalTo(FtpThreadTest.CONTENT)
            );
            MatcherAssert.assertThat(
                reader.readLine(),
                Matchers.startsWith("226 ")
            );
        }
    }

    private static String readContent(final ServerSocket data)
        throws IOException {
        try (
            Socket connection = data.accept();
            InputStream stream = connection.getInputStream()
        ) {
            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        }
    }

    private static void login(final Socket client, final BufferedReader reader)
        throws IOException {
        FtpThreadTest.write(
            client,
            String.format("USER %s", FtpThreadTest.DOMAIN)
        );
        MatcherAssert.assertThat(
            reader.readLine(),
            Matchers.startsWith("331 ")
        );
        FtpThreadTest.write(
            client,
            String.format("PASS %s", FtpThreadTest.CREDS)
        );
        MatcherAssert.assertThat(
            reader.readLine(),
            Matchers.startsWith("230 ")
        );
    }

    private static Thread dispatch(final FtpThread thread) {
        final Thread worker = new Thread(
            () -> {
                try {
                    thread.dispatch();
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(ex);
                }
            }
        );
        worker.start();
        return worker;
    }

    private static Hosts mockHosts() throws IOException {
        final Host host = Mockito.mock(Host.class);
        Mockito.doReturn(true)
            .when(host)
            .authorized("alex", "secret");
        Mockito.doAnswer(
            (Answer<Resource>) inv -> new ResourceMocker()
                .init().withContent(FtpThreadTest.CONTENT).mock()
        ).when(host).fetch(
            Mockito.any(URI.class),
            Mockito.any(Range.class),
            Mockito.any(Version.class)
        );
        final Hosts hosts = Mockito.mock(Hosts.class);
        Mockito.doReturn(host).when(hosts).find(FtpThreadTest.DOMAIN);
        return hosts;
    }

    private static void write(final Socket client, final String line)
        throws IOException {
        client.getOutputStream().write(
            String.format("%s%c%c", line, '\r', '\n')
                .getBytes(StandardCharsets.UTF_8)
        );
        client.getOutputStream().flush();
    }
}
