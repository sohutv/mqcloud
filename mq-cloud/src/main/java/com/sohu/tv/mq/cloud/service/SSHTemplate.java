package com.sohu.tv.mq.cloud.service;

import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.SSHException;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.scp.client.ScpClient;
import org.apache.sshd.scp.client.ScpClientCreator;
import org.apache.sshd.scp.common.helpers.ScpTimestampCommandDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * SSH操作模板类
 * 
 * @Description:
 * @author yongfeigao
 * @date 2018年7月18日
 */
@Service
public class SSHTemplate {
    private static final Logger logger = LoggerFactory.getLogger(SSHTemplate.class);

    public static final List<PosixFilePermission> PERMS = Arrays.asList(PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE);

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;
    
    @Autowired
    private GenericKeyedObjectPool<String, ClientSession> clientSessionPool;

    /**
     * 校验ip是否合法
     * 
     * @param ip
     * @param callback
     * @return
     * @throws SSHException
     */
    public boolean validate(String ip) throws SSHException {
        SSHResult result = execute(ip,
                new SSHCallback() {
                    public SSHResult call(SSHSession session) {
                        return session.executeCommand("date");
                    }
                });
        return result.isSuccess();
    }

    /**
     * 通过回调执行命令
     * 
     * @param ip
     * @param callback 可以使用Session执行多个命令
     * @throws SSHException
     */
    public SSHResult execute(String ip, SSHCallback callback) throws SSHException {
        ClientSession session = null;
        try {
            session = clientSessionPool.borrowObject(ip);
            return callback.call(new SSHSession(session, ip));
        } catch (Exception e) {
            throw new SSHException("SSH err: " + e.getMessage(), e);
        } finally {
            close(ip, session);
        }
    }

    private DefaultLineProcessor generateDefaultLineProcessor(StringBuilder buffer) {
        return new DefaultLineProcessor() {
            public void process(String line, int lineNum) throws Exception {
                if (lineNum > 1) {
                    buffer.append(System.lineSeparator());
                }
                buffer.append(line);
            }
        };
    }

    /**
     * 从流中获取内容
     * 
     * @param is
     */
    private void processStream(InputStream is, LineProcessor lineProcessor) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            String line = null;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                try {
                    lineProcessor.process(line, lineNum);
                } catch (Exception e) {
                    logger.error("err line:" + line, e);
                }
                if (lineProcessor instanceof DefaultLineProcessor) {
                    ((DefaultLineProcessor) lineProcessor).setLineNum(lineNum);
                }
                lineNum++;
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            close(reader);
        }
    }

    private void close(BufferedReader read) {
        if (read != null) {
            try {
                read.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void close(String ip, ClientSession session) {
        if (session != null) {
            try {
                clientSessionPool.returnObject(ip, session);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 可以调用多次executeCommand， 并返回结果
     */
    public class SSHSession {

        private String address;
        private ClientSession clientSession;

        private SSHSession(ClientSession clientSession, String address) {
            this.clientSession = clientSession;
            this.address = address;
        }

        /**
         * 执行命令并返回结果，可以执行多次
         * 
         * @param cmd
         * @return 执行成功Result为true，并携带返回信息,返回信息可能为null 执行失败Result为false，并携带失败信息
         *         执行异常Result为false，并携带异常
         */
        public SSHResult executeCommand(String cmd) {
            return executeCommand(cmd, mqCloudConfigHelper.getServerOPTimeout());
        }

        public SSHResult executeCommand(String cmd, int timoutMillis) {
            return executeCommand(cmd, null, timoutMillis);
        }

        public SSHResult executeCommand(String cmd, LineProcessor lineProcessor) {
            return executeCommand(cmd, lineProcessor, mqCloudConfigHelper.getServerOPTimeout());
        }

        /**
         * 执行命令并返回结果，可以执行多次
         *
         * @param cmd
         * @param lineProcessor 回调处理行
         * @return 如果lineProcessor不为null,那么永远返回Result.true
         */
        public SSHResult executeCommand(String cmd, LineProcessor lineProcessor, int timoutMillis) {
            try (ByteArrayOutputStream stdout = new ByteArrayOutputStream();
                 ByteArrayOutputStream stderr = new ByteArrayOutputStream();
                 ClientChannel channel = clientSession.createExecChannel(cmd)) {
                channel.setOut(stdout);
                channel.setErr(stderr);
                channel.open().verify(timoutMillis);
                // Wait (forever) for the channel to close - signalling command finished
                channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 0L);
                LineProcessor tmpLP = lineProcessor;
                // 如果客户端需要进行行处理，则直接进行回调
                if (tmpLP != null) {
                    processStream(new ByteArrayInputStream(stdout.toByteArray()), tmpLP);
                } else {
                    StringBuilder buffer = new StringBuilder();
                    tmpLP = generateDefaultLineProcessor(buffer);
                    processStream(new ByteArrayInputStream(stdout.toByteArray()), tmpLP);
                    if (buffer.length() > 0) {
                        return new SSHResult(true, buffer.toString());
                    }
                }
                if(tmpLP.lineNum() == 0) {
                    // 返回为null代表可能有异常，需要检测标准错误输出，以便记录日志
                    SSHResult errResult = tryLogError(new ByteArrayInputStream(stderr.toByteArray()), cmd);
                    if (errResult != null) {
                        return errResult;
                    }
                }
                return new SSHResult(true, null);
            } catch (IllegalStateException e) {
                if (e.getMessage().contains("closed")) {
                    logger.error("execute ip:{} cmd:{} session closed", address, cmd, e);
                    try {
                        clientSession.close();
                    } catch (IOException ex) {
                        logger.error("close session err", ex);
                    }
                    return new SSHResult(false, "ssh session closed");
                }
                logger.error("execute ip:{} cmd:{}", address, cmd, e);
                return new SSHResult(e);
            } catch (Exception e) {
                logger.error("execute ip:{} cmd:{}", address, cmd, e);
                return new SSHResult(e);
            }
        }

        private SSHResult tryLogError(InputStream is, String cmd) {
            StringBuilder buffer = new StringBuilder();
            LineProcessor lp = generateDefaultLineProcessor(buffer);
            processStream(is, lp);
            String errInfo = buffer.length() > 0 ? buffer.toString() : null;
            if (errInfo != null) {
                logger.error("address " + address + " execute cmd:({}), err:{}", cmd, errInfo);
                return new SSHResult(false, errInfo);
            }
            return null;
        }

        public SSHResult scpToDir(String localFile, String remoteTargetDirectory) {
            ScpClient client = ScpClientCreator.instance().createScpClient(clientSession);
            try {
                client.upload(localFile, remoteTargetDirectory, ScpClient.Option.TargetIsDirectory);
                return new SSHResult(true);
            } catch (Exception e) {
                logger.error("scp scpToDir from:{} to:{}", localFile, remoteTargetDirectory, e);
                return new SSHResult(e);
            }
        }

        public SSHResult scpToFile(String localFile, String remoteFile) {
            ScpClient client = ScpClientCreator.instance().createScpClient(clientSession);
            try {
                client.upload(localFile, remoteFile);
                client.getSession().executeRemoteCommand("chmod 0744 " + remoteFile);
                return new SSHResult(true);
            } catch (Exception e) {
                logger.error("scpToFile from:{} to:{}", localFile, remoteFile, e);
                return new SSHResult(e);
            }
        }

        public SSHResult scpToFile(byte[] data, String remoteFile) {
            ScpClient client = ScpClientCreator.instance().createScpClient(clientSession);
            long now = System.currentTimeMillis();
            try {
                client.upload(data, remoteFile, PERMS, new ScpTimestampCommandDetails(now, now));
                return new SSHResult(true);
            } catch (Exception e) {
                logger.error("scpByteToFile {}", remoteFile, e);
                return new SSHResult(e);
            }
        }
    }

    /**
     * 结果封装
     */
    public class SSHResult {
        private boolean success;
        private String result;
        private Exception excetion;

        public SSHResult(boolean success) {
            this.success = success;
        }

        public SSHResult(boolean success, String result) {
            this.success = success;
            this.result = result;
        }

        public SSHResult(Exception excetion) {
            this.success = false;
            this.excetion = excetion;
        }

        public Exception getExcetion() {
            return excetion;
        }

        public void setExcetion(Exception excetion) {
            this.excetion = excetion;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        @Override
        public String toString() {
            return "Result [success=" + success + ", result=" + result
                    + ", excetion=" + excetion + "]";
        }
    }

    /**
     * 执行命令回调
     */
    public interface SSHCallback {
        /**
         * 执行回调
         * 
         * @param session
         */
        SSHResult call(SSHSession session);
    }

    /**
     * 从流中直接解析数据
     */
    public static interface LineProcessor {
        /**
         * 处理行
         * 
         * @param line 内容
         * @param lineNum 行号，从1开始
         * @throws Exception
         */
        void process(String line, int lineNum) throws Exception;

        /**
         * 返回内容的行数，如果为0需要检测错误流
         * @return
         */
        int lineNum();
    }

    public static abstract class DefaultLineProcessor implements LineProcessor {
        protected int lineNum;

        @Override
        public int lineNum() {
            return lineNum;
        }

        public void setLineNum(int lineNum) {
            this.lineNum = lineNum;
        }
    }
}
