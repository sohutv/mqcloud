package com.sohu.tv.mq.cloud.ssh;

import com.google.common.io.ByteStreams;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.scp.client.ScpClient;
import org.apache.sshd.scp.client.ScpClientCreator;
import org.apache.sshd.scp.common.helpers.ScpTimestampCommandDetails;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 * @Auther: yongfeigao
 * @Date: 2023/10/23
 */
public class SSHClientTest {

    private String serverUser = "mqcloud";
    private String serverPassword = "9j7t4SDJOIusddca+Mzd6Q==";
    private String ip = "test.mqcloud.com";
    private Integer serverPort = 22;
    private Integer serverConnectTimeout = 5000;
    private SSHClient sshClient;

    @Before
    public void init() throws GeneralSecurityException, IOException, URISyntaxException {
        sshClient = new SSHClient();
        sshClient.setServerUser(serverUser);
        sshClient.setServerPassword(serverPassword);
        sshClient.setServerPort(serverPort);
        sshClient.setServerConnectTimeout(serverConnectTimeout);
        byte[] bytes = Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource("rsa").toURI()));
        String content = new String(bytes, StandardCharsets.UTF_8);
        sshClient.setPrivateKey(content);
        sshClient.init();
    }

    @Test
    public void test() throws IOException {
        ClientSession clientSession = sshClient.connect(ip);
        String result = clientSession.executeRemoteCommand("date");
        Assert.assertNotNull(result);
        clientSession.close();
    }

    @Test
    public void testScp() throws IOException {
        String file = getClass().getClassLoader().getResource("rsa").getFile();
        ClientSession clientSession = sshClient.connect(ip);
        ScpClient client = ScpClientCreator.instance().createScpClient(clientSession);
        client.upload(file, "/tmp", ScpClient.Option.TargetIsDirectory);
    }

    @Test
    public void testScpFile() throws IOException {
        String file = getClass().getClassLoader().getResource("rsa").getFile();
        ClientSession clientSession = sshClient.connect(ip);
        ScpClient client = ScpClientCreator.instance().createScpClient(clientSession);
        client.upload(file, "/tmp/a");
    }

    @Test
    public void testScpByte() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("rsa");
        byte[] bytes = ByteStreams.toByteArray(inputStream);
        ClientSession clientSession = sshClient.connect(ip);
        ScpClient client = ScpClientCreator.instance().createScpClient(clientSession);
        long now = System.currentTimeMillis();
        client.upload(bytes, "/tmp/b", Arrays.asList(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE), new ScpTimestampCommandDetails(now, now));
    }
}