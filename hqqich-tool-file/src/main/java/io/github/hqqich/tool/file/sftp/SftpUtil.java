package io.github.hqqich.tool.file.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

/**
 * Created by ChenHao on 2026/4/29 is 20:57.
 *
 * @author tsinglink
 */

public class SftpUtil {

    private static final int CONNECT_TIMEOUT_MILLIS = 30_000;

    public void sendSFTP(String filePath, String dest, ServerItem server) {
        sendSFTP(new File(filePath), dest, server);
    }

    public void sendSFTP(File file, String dest, ServerItem server) {
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            sendSFTP(inputStream, dest, server);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendSFTP(InputStream inputStream, String dest, ServerItem server) {
        System.out.println("Sending " + dest);

        Session jschSession = null;
        ChannelSftp channel = null;

        try {
            JSch jsch = new JSch();
            configIdentity(jsch, server);

            jschSession = jsch.getSession(
                    server.getUsername(),
                    server.getIp(),
                    server.getPort()
            );

            // 禁用主机密钥检查
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("PreferredAuthentications", buildPreferredAuthentications(server));
            jschSession.setConfig(config);

            if (StringUtils.isNotBlank(server.getPassword()) && !hasPrivateKey(server)) {
                jschSession.setPassword(server.getPassword());
            }

            jschSession.connect(CONNECT_TIMEOUT_MILLIS);

            channel = (ChannelSftp) jschSession.openChannel("sftp");
            channel.connect(CONNECT_TIMEOUT_MILLIS);

            channel.put(inputStream, dest);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (jschSession != null && jschSession.isConnected()) {
                jschSession.disconnect();
            }
        }
    }

    private void configIdentity(JSch jsch, ServerItem server) throws Exception {
        if (hasPrivateKeyFile(server)) {
            File privateKeyFile = server.getPrivateKeyFile();
            byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
            jsch.addIdentity(
                    privateKeyFile.getAbsolutePath(),
                    privateKeyBytes,
                    null,
                    getPrivateKeyPassphraseBytes(server)
            );
            return;
        }
        if (hasPrivateKeyContent(server)) {
            jsch.addIdentity(
                    server.getUsername(),
                    server.getPrivateKey().getBytes(StandardCharsets.UTF_8),
                    null,
                    getPrivateKeyPassphraseBytes(server)
            );
            return;
        }
        if (StringUtils.isBlank(server.getPassword())) {
            throw new JSchException("SFTP authentication requires password or private key.");
        }
    }

    private byte[] getPrivateKeyPassphraseBytes(ServerItem server) {
        String passphrase = server.getPrivateKeyPassphrase();
        if (StringUtils.isBlank(passphrase) && hasPrivateKey(server)) {
            passphrase = server.getPassword();
        }
        return StringUtils.isBlank(passphrase) ? null : passphrase.getBytes(StandardCharsets.UTF_8);
    }

    private String buildPreferredAuthentications(ServerItem server) {
        if (hasPrivateKey(server)) {
            return StringUtils.isNotBlank(server.getPassword())
                    ? "publickey,password,keyboard-interactive"
                    : "publickey";
        }
        return "password,keyboard-interactive";
    }

    private boolean hasPrivateKey(ServerItem server) {
        return hasPrivateKeyFile(server) || hasPrivateKeyContent(server);
    }

    private boolean hasPrivateKeyFile(ServerItem server) {
        File privateKeyFile = server.getPrivateKeyFile();
        return privateKeyFile != null && privateKeyFile.isFile();
    }

    private boolean hasPrivateKeyContent(ServerItem server) {
        return StringUtils.isNotBlank(server.getPrivateKey());
    }

}
