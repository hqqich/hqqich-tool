package io.github.hqqich.tool.file.sftp;

import java.io.File;

/**
 * Created by ChenHao on 2026/4/29 is 20:57.
 *
 * @author tsinglink
 */

public class ServerItem {

    private ServerItem() {
    }

    private ServerItem(Builder builder) {
        this.username = builder.username;
        this.ip = builder.ip;
        this.port = builder.port;
        this.password = builder.password;
        this.privateKeyFile = builder.privateKeyFile;
        this.privateKey = builder.privateKey;
        this.privateKeyPassphrase = builder.privateKeyPassphrase;
    }

    public ServerItem(String username, String ip, int port, String password) {
        this.username = username;
        this.ip = ip;
        this.port = port;
        this.password = password;
    }

    public ServerItem(String username, String ip, int port, File privateKeyFile) {
        this.username = username;
        this.ip = ip;
        this.port = port;
        this.privateKeyFile = privateKeyFile;
    }

    public ServerItem(String username, String ip, int port, File privateKeyFile, String privateKeyPassphrase) {
        this(username, ip, port, privateKeyFile);
        this.privateKeyPassphrase = privateKeyPassphrase;
    }

    public ServerItem(String username, String ip, int port, String privateKey, String privateKeyPassphrase) {
        this.username = username;
        this.ip = ip;
        this.port = port;
        this.privateKey = privateKey;
        this.privateKeyPassphrase = privateKeyPassphrase;
    }

    private String username;
    private String ip;
    private int port;


    private String password;

    private File privateKeyFile;

    private String privateKey;

    private String privateKeyPassphrase;



    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public File getPrivateKeyFile() {
        return privateKeyFile;
    }

    public void setPrivateKeyFile(File privateKeyFile) {
        this.privateKeyFile = privateKeyFile;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPrivateKeyPassphrase() {
        return privateKeyPassphrase;
    }

    public void setPrivateKeyPassphrase(String privateKeyPassphrase) {
        this.privateKeyPassphrase = privateKeyPassphrase;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String username;
        private String ip;
        private int port;
        private String password;
        private File privateKeyFile;
        private String privateKey;
        private String privateKeyPassphrase;

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder privateKeyFile(File privateKeyFile) {
            this.privateKeyFile = privateKeyFile;
            return this;
        }

        public Builder privateKey(String privateKey) {
            this.privateKey = privateKey;
            return this;
        }

        public Builder privateKeyPassphrase(String privateKeyPassphrase) {
            this.privateKeyPassphrase = privateKeyPassphrase;
            return this;
        }

        public ServerItem build() {
            return new ServerItem(this);
        }
    }
}
