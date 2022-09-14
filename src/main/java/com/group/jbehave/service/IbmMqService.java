package com.group.jbehave.service;

import com.group.jbehave.utilities.Util;
import com.ibm.mq.*;
import com.ibm.mq.constants.CMQC;
import com.ibm.broker.config.proxy.*;

import com.group.bdd.framework.LogUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.group.bdd.framework.Asserts.assertThat;
import static com.group.bdd.framework.ConfigLoader.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IbmMqService {

    private static final Logger LOG = LogManager.getLogger(IbmMqService.class);

    private static String env = config().getString("test.environment");
    public static final String HEX_CHARS = "0123456789ABCDEF";
    private Util util = new Util();

    public static String[] getMessageWithToken(String messageFlow,
                                               String messageKey, String qName) {
        String env = config().getString("test.environment");
        String getMessageWithToken[] = null;
        String qManager = config().getString(env + "." + messageFlow + ".qManager");
        String qChannel = config().getString(env + "." + messageFlow + ".qChannel");
        String qHost = config().getString(env + "." + messageFlow + ".qHost");
        String qPort = config().getString(env + "." + messageFlow + ".qPort");
        String qOutputMessageQueue = config().getString(env + "." + messageFlow + "." + qName);

        if (qOutputMessageQueue != null) {
            qName = qOutputMessageQueue;
        }

        LogUtil.log("Queue Manager Name    : " + qManager);
        LogUtil.log("Queue Manager Channel : " + qChannel);
        LogUtil.log("Queue Manager Host    : " + qHost);
        LogUtil.log("Queue Manager Port    : " + qPort);
        LogUtil.log("READING QUEUE Name      : " + qName);

        getMessageWithToken = getMessage(qManager, qName, qChannel, qHost,
                qPort, messageKey);

        return getMessageWithToken;
    }

    public static String[] getMessageWithTokenByText(String messageFlow,
                                                     String messageKey, String qName) {
        String env = config().getString("test.environment");
        String getMessageWithToken[] = null;
        String qManager = config().getString(env + "." + messageFlow + ".qManager");
        String qChannel = config().getString(env + "." + messageFlow + ".qChannel");
        String qHost = config().getString(env + "." + messageFlow + ".qHost");
        String qPort = config().getString(env + "." + messageFlow + ".qPort");
        String qOutputMessageQueue = config().getString(env + "." + messageFlow + "." + qName);

        if (qOutputMessageQueue != null) {
            qName = qOutputMessageQueue;
        }

        LogUtil.log("READING Queue: " + qName + " in Queue Manager: " + qManager);

        for (int i = 0; i < 5; i++) {
            try {
                getMessageWithToken = getMessageByText(qManager, qName, qChannel, qHost,
                        qPort, messageKey);
                if (getMessageWithToken[0] == null) {
                    Util.sleep(2000);
                } else {
                    break;
                }
            } catch (Exception e) {
                LogUtil.log(e.getLocalizedMessage());
            }
        }

        return getMessageWithToken;
    }

    public static String[] getNoMessageWithTokenByText(String messageFlow,
                                                       String messageKey, String qName) {
        String env = config().getString("test.environment");
        String getMessageWithToken[] = null;
        String qManager = config().getString(env + "." + messageFlow + ".qManager");
        String qChannel = config().getString(env + "." + messageFlow + ".qChannel");
        String qHost = config().getString(env + "." + messageFlow + ".qHost");
        String qPort = config().getString(env + "." + messageFlow + ".qPort");
        String qOutputMessageQueue = config().getString(env + "." + messageFlow + "." + qName);

        if (qOutputMessageQueue != null) {
            qName = qOutputMessageQueue;
        }

        LogUtil.log("READING Queue: " + qName + " in Queue Manager: " + qManager);
        for (int i = 0; i < 2; i++) {
            try {
                getMessageWithToken = getMessageByText(qManager, qName, qChannel, qHost,
                        qPort, messageKey);
                if (getMessageWithToken[0] == null) {
                    Util.sleep(500);
                } else {
                    break;
                }
            } catch (Exception e) {
                LogUtil.log(e.getLocalizedMessage());
            }
        }

        return getMessageWithToken;
    }

    public static String putMessageWithToken(String messageFlow, String queueNameKey, String messageText) {
        String env = config().getString("test.environment");
        String returnPutMessageID = "";
        String qManager = config().getString(env + "." + messageFlow + ".qManager");
        String qChannel = config().getString(env + "." + messageFlow + ".qChannel");
        String qHost = config().getString(env + "." + messageFlow + ".qHost");
        String qPort = config().getString(env + "." + messageFlow + ".qPort");
        String qName = config().getString(env + "." + messageFlow + "." + queueNameKey);

        if (qName == null) {
            qName = queueNameKey;
        }

        LogUtil.log("Queue Manager Name    : " + qManager);
        LogUtil.log("Queue Manager Channel : " + qChannel);
        LogUtil.log("Queue Manager Host    : " + qHost);
        LogUtil.log("Queue Manager Port    : " + qPort);
        LogUtil.log("INPUT QUEUE Name      : " + qName);

        int ccsid = 1208;
        returnPutMessageID = putMessage(qManager, qName, qChannel, qHost,
                qPort, messageText, ccsid);

        return returnPutMessageID;
    }

    public static int getCCSID() {
        return 1208;
    }

    public static String putMessageWithToken(String messageFlow, String messageText) {
        String env = config().getString("test.environment");
        String returnPutMessageID = "";
        String qManager = config().getString(env + "." + messageFlow + ".qManager");
        String qChannel = config().getString(env + "." + messageFlow + ".qChannel");
        String qHost = config().getString(env + "." + messageFlow + ".qHost");
        String qPort = config().getString(env + "." + messageFlow + ".qPort");
        String qName = config().getString(env + "." + messageFlow + ".qInputQueueName");

        LogUtil.log("Queue Manager Name    : " + qManager);
        LogUtil.log("Queue Manager Channel : " + qChannel);
        LogUtil.log("Queue Manager Host    : " + qHost);
        LogUtil.log("Queue Manager Port    : " + qPort);
        LogUtil.log("INPUT QUEUE Name      : " + qName);
        int ccsid = getCCSID();
        returnPutMessageID = putMessage(qManager, qName, qChannel, qHost,
                qPort, messageText, ccsid);

        return returnPutMessageID;
    }

    private static void setMQEnvironment(String qChannel, String qHost,
                                         String qPort) {
        com.ibm.mq.MQEnvironment.hostname = qHost;
        com.ibm.mq.MQEnvironment.channel = qChannel;
        com.ibm.mq.MQEnvironment.port = Integer.parseInt(qPort);
    }

    private static String[] getMessageByText(String qManager, String qName,
                                             String qChannel, String qHost, String qPort, String messageKey) {

        MQQueueManager qMgr;

        String returngetMessageID;
        int OpenMQqueueOptions;
        MQQueue OpenMQQueue;
        MQMessage MqMessage;
        MQPutMessageOptions pmo;
        MQGetMessageOptions gmo;
        int MQqueueDepth;

        boolean matchKeyStatus = false;
        MQqueueDepth = 0;
        returngetMessageID = null;
        setMQEnvironment(qChannel, qHost, qPort);

        String returnMessage[] = new String[2];

        try {

            MQqueueDepth = getQueueDepth(qManager, qName, qChannel, qHost, qPort);

            qMgr = null;
            qMgr = new MQQueueManager(qManager);
            OpenMQqueueOptions = 8202;
            OpenMQQueue = qMgr.accessQueue(qName, OpenMQqueueOptions, null, null, null);
            gmo = new MQGetMessageOptions();
            MqMessage = new MQMessage();
            gmo.options = 8224;
            gmo.matchOptions = 0;

            for (int i = 0; i < MQqueueDepth; i++) {

                OpenMQQueue.get(MqMessage, gmo);

                String messageText = MqMessage.readStringOfByteLength(MqMessage
                        .getMessageLength());

                returngetMessageID = new String(MqMessage.messageId);
                String[] key = messageKey.split(";");

                for (int keyindex = 0; keyindex <= key.length - 1; keyindex++) {
                    if (messageText.contains(key[keyindex])) {
                        matchKeyStatus = true;

                    } else {
                        matchKeyStatus = false;
                        break;
                    }
                }

                if (matchKeyStatus) {
                    String s = bytesToHex(MqMessage.messageId);
                    LogUtil.log("Message ID: " + s);
                    returnMessage[0] = messageText;

                    if (qName.contains("EXCEPTION")) {
                        MqMessage.messageId = MqMessage.messageId;
                        gmo.options = CMQC.MQGMO_NO_WAIT + CMQC.MQGMO_FAIL_IF_QUIESCING;
                        gmo.matchOptions = CMQC.MQMO_MATCH_MSG_ID;
                        try {
                            OpenMQQueue.get(MqMessage, gmo);
                        } catch (MQException e) {
                            LOG.info("MQException : " + e.getLocalizedMessage());
                            returnMessage[1] = null;
                            break;
                        }

                        for (int j = 0; j < MQqueueDepth; j++) {
                            try {
                                try {
                                    Object usrExceptionMessage = MqMessage.getObjectProperty("Exception");
                                    String excepMsgId = bytesToHex(MqMessage.messageId);
                                    if (usrExceptionMessage.toString().contains(excepMsgId.toLowerCase())) {
                                        LogUtil.log("Exception Message ID: " + excepMsgId);
                                        returnMessage[1] = usrExceptionMessage.toString();
                                        break;
                                    } else {
                                        if (usrExceptionMessage.toString().contains(messageKey)) {
                                            returnMessage[1] = usrExceptionMessage.toString();
                                            break;
                                        } else {
                                            returnMessage[1] = usrExceptionMessage.toString();
                                        }
                                    }
                                } catch (MQException e) {
                                    returnMessage[1] = "";
                                }

                            } catch (Exception e) {
                                LOG.info("Exception : " + e.getLocalizedMessage());
                                returnMessage[1] = null;
                                break;
                            }
                        }

                    } else {
                        returnMessage[1] = null;
                    }

                    gmo.options = CMQC.MQGMO_MSG_UNDER_CURSOR;
                    OpenMQQueue.get(MqMessage, gmo);
                    break;
                }
            }

            OpenMQQueue.close();
            qMgr.commit();
            qMgr.close();
            qMgr.disconnect();

        } catch (MQException e) {
            assertThat("Error: " + e, false);
            returnMessage[1] = null;
            returnMessage[0] = null;
            LogUtil.log("MQ Error code: " + e.getLocalizedMessage());
        } catch (Exception e) {
            returnMessage[1] = null;
            returnMessage[0] = null;
            LogUtil.log(e.getLocalizedMessage());
        }

        return returnMessage;
    }

    public static String bytesToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++)
            buf.append(byteToHex(data[i]));

        return buf.toString();
    }

    public static String byteToHex(byte data) {
        int hi = (data & 0xF0) >> 4;
        int lo = (data & 0x0F);
        return "" + HEX_CHARS.charAt(hi) + HEX_CHARS.charAt(lo);
    }

    private static String[] getMessage(String qManager, String qName,
                                       String qChannel, String qHost, String qPort, String messageKey) {

        MQQueueManager qMgr;
        int OpenMQqueueOptions = 0;
        MQQueue OpenMQQueue;
        MQMessage MqMessage;
        MQGetMessageOptions gmo;

        setMQEnvironment(qChannel, qHost, qPort);

        String returnMessage[] = new String[2];

        try {
            for (int i = 0; i < 3; i++) {
                qMgr = null;
                qMgr = new MQQueueManager(qManager);
                OpenMQqueueOptions = 8202;
                OpenMQQueue = qMgr.accessQueue(qName, OpenMQqueueOptions, null, null, null);
                gmo = new MQGetMessageOptions();
                MqMessage = new MQMessage();
                byte[] array = messageKey.getBytes();
                MqMessage.messageId = messageKey.getBytes();
                gmo.options = CMQC.MQGMO_NO_WAIT + CMQC.MQGMO_FAIL_IF_QUIESCING;
                gmo.matchOptions = CMQC.MQMO_MATCH_MSG_ID;
                try {
                    OpenMQQueue.get(MqMessage, gmo);

                    try {
                        Object usrExceptionMessage = MqMessage.getObjectProperty("Exception");
                        returnMessage[1] = usrExceptionMessage.toString();
                        String s = bytesToHex(MqMessage.messageId);
                        LogUtil.log("Exception Message ID: " + s);
                    } catch (MQException e) {
                        returnMessage[1] = "";
                    }

                    String messageText = MqMessage.readStringOfByteLength(MqMessage
                            .getMessageLength());

                    returnMessage[0] = messageText;

                } catch (MQException e) {
                    returnMessage[1] = null;
                    returnMessage[0] = null;
                }

                OpenMQQueue.close();
                qMgr.commit();
                qMgr.close();
                qMgr.disconnect();

                if (returnMessage[0] != null) {
                    break;
                }
            }


        } catch (MQException e) {
            assertThat("Error: " + e, false);
            returnMessage = null;
            LogUtil.log(e.getLocalizedMessage());
        } catch (Exception e) {
            returnMessage = null;
            LogUtil.log(e.getLocalizedMessage());
        }

        return returnMessage;
    }


    private static int getQueueDepth(String qManager, String qName, String qChannel, String qHost, String qPort) {
        int getQueueDepth = -1;
        MQQueueManager qMgr;
        int OpenMQqueueOptions = 0;
        MQQueue OpenMQQueue;

        setMQEnvironment(qChannel, qHost, qPort);
        try {
            qMgr = null;
            qMgr = new MQQueueManager(qManager);
            OpenMQqueueOptions = 8232;
            OpenMQQueue = qMgr.accessQueue(qName, OpenMQqueueOptions, null,
                    null, null);
            getQueueDepth = Integer.valueOf(OpenMQQueue.getCurrentDepth());
            OpenMQQueue.close();
            qMgr.disconnect();
        } catch (MQException e) {
            getQueueDepth = -1;
        }

        return getQueueDepth;

    }


    private static String putMessage(String qManager, String qName,
                                     String qChannel, String qHost, String qPort, String messageText, int ccsid) {

        String returnPutMessageID = "";
        MQQueueManager qMgr;
        int OpenMQqueueOptions = 0;
        MQQueue OpenMQQueue;
        MQMessage MqMessage;
        MQPutMessageOptions pmo;

        setMQEnvironment(qChannel, qHost, qPort);

        try {
            qMgr = null;
            qMgr = new MQQueueManager(qManager);

            OpenMQqueueOptions = 16;
            OpenMQQueue = qMgr.accessQueue(qName, OpenMQqueueOptions, null,
                    null, null);
            MqMessage = new MQMessage();
            pmo = new MQPutMessageOptions();
            MqMessage.format = "MQSTR";
            MqMessage.characterSet = ccsid;

            MqMessage.writeString(messageText);
            OpenMQQueue.put(MqMessage, pmo);
            returnPutMessageID = new String(MqMessage.messageId);

            OpenMQQueue.close();
            qMgr.commit();
            qMgr.disconnect();

            Util.sleep(config().getInt("msg_drop_wait_time", 5000));

        } catch (MQException e) {
            returnPutMessageID = "";
            LogUtil.log("MQ Error code: " + e.getLocalizedMessage());
        } catch (Exception e) {
            assertThat("Error: " + e, false);
            returnPutMessageID = "";
            LogUtil.log("MQ Error code: " + e.getLocalizedMessage());
        }
        return returnPutMessageID;
    }

    public static String reStartBroker8ExecutionGroup(String bBrokerName, String bBrokerHost, String bBrokerPort, String bBrokerExecutionGroupID, String bQueueChannel) {
        String message = "";

        boolean bBrokerStatus = false;
        MQBrokerConnectionParameters cmcp = new MQBrokerConnectionParameters(bBrokerHost, Integer.parseInt(bBrokerPort), bBrokerName);
        cmcp.setAdvancedConnectionParameters(bQueueChannel, null, null, -1, -1, null);

        try {

            BrokerProxy cmp = BrokerProxy.getInstance(cmcp);
            ExecutionGroupProxy ep = cmp.getExecutionGroupByName(bBrokerExecutionGroupID);
            ep.stop();
            for (int i = 0; i < 5; i++) {
                Util.sleep(10000);
                if (ep.isRunning() == false) {
                    ep.start();
                    for (int j = 0; j < 5; j++) {
                        Util.sleep(10000);
                        if (ep.isRunning()) {
                            bBrokerStatus = true;
                            break;
                        }
                    }
                    break;
                }
            }

            if (bBrokerStatus == false) {
                message = "There are some issues with reStarted the Broker Executions Group: " + bBrokerName + " And Status " + ep.isRunning();
            } else {
                message = "Restarted the Execution Group: " + bBrokerExecutionGroupID + " in Broker: " + bBrokerName + " And Status: " + ep.isRunning();
            }
            cmp.disconnect();
        } catch (ConfigManagerProxyLoggedException e1) {
            message = "MQ Error code: " + e1.getLocalizedMessage();
        } catch (ConfigManagerProxyPropertyNotInitializedException e1) {
            message = "MQ Error code: " + e1.getLocalizedMessage();
        } catch (Exception e1) {
            message = "MQ Error code: " + e1.getLocalizedMessage();
        }
        return message;
    }

    public static void reStartBroker8ExecutionGroupTest(String bBrokerName, String bBrokerHost, String bBrokerPort, String bBrokerExecutionGroupID, String bQueueChannel) {
        try {
            String fileLocations = new File("src/main/resources/jars").getAbsoluteFile().toString();
            fileLocations = "\"" + fileLocations + "/wmb8.jar\"";
            Process proc = Runtime.getRuntime().exec("java -jar " + fileLocations + " " + bBrokerName + " " + bBrokerHost + " " + bBrokerPort + " " + bBrokerExecutionGroupID + " " + bQueueChannel);

            proc.waitFor(60, TimeUnit.SECONDS);
            Util.sleep(30000);
            InputStream err = proc.getErrorStream();
            InputStream in = proc.getInputStream();
            StringBuilder sb = new StringBuilder();
            byte[] inStr = new byte[1024];
            in.read(inStr);
            byte[] errStr = new byte[1024];
            err.read(errStr);
            if (proc.isAlive()) {
                proc.destroy();
            }
            if (!inStr.equals("")) {
                sb.append("Input: " + new String(inStr) + ";");
                LogUtil.log(new String(inStr));
            }
            if (!new String(errStr).trim().equals("")) {
                sb.append("Error: " + new String(errStr) + ";");
                assertThat("Error: " + new String(errStr) + ";", false);
            }

        } catch (IOException e) {
            assertThat("Eror: " + e.getMessage(), false);
        } catch (InterruptedException e) {
            assertThat("Eror: " + e.getMessage(), false);
        }
    }

    public static void reStartBrokerExecutionGroup(String messageFlow) {
        String env = config().getString("test.environment");
        String bBrokerName = config().getString(env + "." + messageFlow + ".qManager");
        String bBrokerHost = config().getString(env + "." + messageFlow + ".qHost");
        String bBrokerPort = config().getString(env + "." + messageFlow + ".bBrokerPort");
        String bBrokerExecutionGroupID = config().getString(env + "." + messageFlow + ".bBrokerExecutionGroupID");
        boolean bBrokerStatus = false;
        BrokerConnectionParameters cmcp = new MQBrokerConnectionParameters(bBrokerHost, Integer.parseInt(bBrokerPort), bBrokerName);

        try {

            BrokerProxy cmp = BrokerProxy.getInstance(cmcp);
            ExecutionGroupProxy ep = cmp.getExecutionGroupByName(bBrokerExecutionGroupID);
            ep.stop();
            for (int i = 0; i < 5; i++) {
                Util.sleep(10000);
                if (ep.isRunning() == false) {
                    ep.start();
                    for (int j = 0; j < 5; j++) {
                        Util.sleep(10000);
                        if (ep.isRunning()) {
                            LogUtil.log("Restarted the Execution Group: " + bBrokerExecutionGroupID + " in Broker: " + bBrokerName + " And Status: " + ep.isRunning());
                            bBrokerStatus = true;
                            break;
                        }
                    }
                    break;
                }
            }

            if (bBrokerStatus == false) {
                LogUtil.log("There are some issues with reStarted the Broker Executions Group: " + bBrokerName + " And Status " + ep.isRunning());
            }
            cmp.disconnect();
        } catch (ConfigManagerProxyLoggedException e1) {
            LogUtil.log("MQ Error code: " + e1.getLocalizedMessage());
        } catch (ConfigManagerProxyPropertyNotInitializedException e1) {
            LogUtil.log("MQ Error code: " + e1.getLocalizedMessage());
        } catch (Exception e1) {
            LogUtil.log("MQ Error code: " + e1.getLocalizedMessage());
        }
    }

    public static void reStartBrokerExecutionGroupTest(String messageFlow) {
        String env = config().getString("test.environment");
        String bBrokerName = config().getString(env + "." + messageFlow + ".qManager");
        String bBrokerHost = config().getString(env + "." + messageFlow + ".qHost");
        String bBrokerPort = config().getString(env + "." + messageFlow + ".bBrokerPort");
        String bBrokerExecutionGroupID = config().getString(env + "." + messageFlow + ".bBrokerExecutionGroupID");
        boolean bBrokerStatus = false;
        BrokerConnectionParameters cmcp = new IIB10Connector(bBrokerHost, Integer.parseInt(bBrokerPort), bBrokerName);
        try {
            BrokerProxy cmp = BrokerProxy.getInstance(cmcp);
            ExecutionGroupProxy ep = cmp.getExecutionGroupByName(bBrokerExecutionGroupID);
            ep.stop();
            for (int i = 0; i < 5; i++) {
                Util.sleep(10000);
                if (ep.isRunning() == false) {
                    ep.start();
                    for (int j = 0; j < 5; j++) {
                        Util.sleep(10000);
                        if (ep.isRunning()) {
                            LogUtil.log("Restarted the Execution Group: " + bBrokerExecutionGroupID + " in Broker: " + bBrokerName + " And Status: " + ep.isRunning());
                            bBrokerStatus = true;
                            break;
                        }
                    }
                    break;
                }
            }

            if (bBrokerStatus == false) {
                LogUtil.log("There are some issues with reStarted the Broker Executions Group: " + bBrokerName + " And Status " + ep.isRunning());
            }
            cmp.disconnect();
        } catch (ConfigManagerProxyLoggedException e1) {
            LogUtil.log("MQ Error code: " + e1.getLocalizedMessage());
        } catch (ConfigManagerProxyPropertyNotInitializedException e1) {
            LogUtil.log("MQ Error code: " + e1.getLocalizedMessage());
        } catch (Exception e1) {
            LogUtil.log("MQ Error code: " + e1.getLocalizedMessage());
        }
    }

    static class IIB10Connector extends IntegrationNodeConnectionParameters {
        protected IIB10Connector(String ip, int port, String brokerName) {
            super(ip, port, brokerName);
        }
    }

    public static void clearMQQueue(String messageFlow, String qQueueName) {
        String env = config().getString("test.environment");
        String qManager = config().getString(env + "." + messageFlow + ".qManager");
        String qChannel = config().getString(env + "." + messageFlow + ".qChannel");
        String qHost = config().getString(env + "." + messageFlow + ".qHost");
        String qPort = config().getString(env + "." + messageFlow + ".qPort");
        String qName = qQueueName;

        MQQueueManager qMgr;
        qMgr = null;
        MQMessage msg = null;

        setMQEnvironment(qChannel, qHost, qPort);

        try {
            int queuedepth = getQueueDepth(qManager, qName, qChannel, qHost, qPort);
            qMgr = null;
            qMgr = new MQQueueManager(qManager);
            MQQueue my_queue = qMgr.accessQueue(qName, 8202, null, null, null);
            MQGetMessageOptions gmo = new MQGetMessageOptions();
            msg = new MQMessage();
            gmo.options = 17;
            gmo.matchOptions = 0;
            for (int i = 0; i < queuedepth; i++) {
                my_queue.get(msg, gmo);
                gmo.options = 257;
                my_queue.get(msg, gmo);
                gmo.options = 33;
            }

            my_queue.close();
            qMgr.commit();
            qMgr.close();
            qMgr.disconnect();
        } catch (MQException e) {
            assertThat("Error while cleaning the queue: " + e.getMessage(), false);
        }
    }

    public static String reStartBroker10ExecutionGroupThreads(String bBrokerName, String bBrokerHost, String bBrokerPort, String bBrokerExecutionGroupID) {
        String isRestarted = "false";
        BrokerConnectionParameters cmcp = new IIB10Connector(bBrokerHost, Integer.parseInt(bBrokerPort), bBrokerName);
        try {
            BrokerProxy cmp = BrokerProxy.getInstance(cmcp);
            ExecutionGroupProxy ep = cmp.getExecutionGroupByName(bBrokerExecutionGroupID);
            ep.stop();
            for (int i = 0; i < 5; i++) {
                Util.sleep(10000);
                if (ep.isRunning() == false) {
                    ep.start();
                    for (int j = 0; j < 10; j++) {
                        Util.sleep(10000);
                        if (ep.isRunning()) {
                            isRestarted = "SUCCESS - Restarted the Execution Group: " + bBrokerExecutionGroupID + " in Broker: " + bBrokerName + " And Status: " + ep.isRunning();
                            break;
                        }
                    }
                    break;
                }
            }

            if (isRestarted.equals("false")) {
                isRestarted = "No response while restarting - EG:" + bBrokerExecutionGroupID + " in Broker: " + bBrokerName;
            }
            cmp.disconnect();
        } catch (ConfigManagerProxyLoggedException e1) {
            assertThat("MQ Error code: " + e1.getLocalizedMessage(), false);
        } catch (ConfigManagerProxyPropertyNotInitializedException e1) {
            assertThat("MQ Error code: " + e1.getLocalizedMessage(), false);
        } catch (Exception e1) {
            assertThat("MQ Error code: " + e1.getLocalizedMessage(), false);
        }
        return isRestarted;
    }

    public static String reStartBroker8ExecutionGroupThreads(String bBrokerName, String bBrokerHost, String bBrokerPort, String bBrokerExecutionGroupID, String bQueueChannel) {
        String isRestarted = "false";
        try {
            String fileLocations = new File("src/main/resources/jars").getAbsoluteFile().toString();

            fileLocations = "\"" + fileLocations + "/wmb8.jar\"";
            Process proc = Runtime.getRuntime().exec("java -jar " + fileLocations + " " + bBrokerName + " " + bBrokerHost + " " + bBrokerPort + " " + bBrokerExecutionGroupID + " " + bQueueChannel);

            proc.waitFor(60, TimeUnit.SECONDS);

            InputStream err = proc.getErrorStream();
            InputStream in = proc.getInputStream();
            StringBuilder sb = new StringBuilder();
            byte[] inStr = new byte[1024];
            in.read(inStr);
            byte[] errStr = new byte[1024];
            err.read(errStr);
            if (proc.isAlive()) {
                proc.destroy();
            }
            String inlineString = new String(inStr).trim();
            if (inlineString.equals("")) {
                isRestarted = "No response while restarting - EG:" + bBrokerExecutionGroupID + " in Broker: " + bBrokerName;
            } else {
                sb.append("Input: " + new String(inStr) + ";");
                isRestarted = "SUCCESS - " + new String(inStr);
            }
            if (!new String(errStr).trim().equals("")) {
                sb.append("Error: " + new String(errStr) + ";");
                assertThat("Error: " + new String(errStr) + ";", false);
            }

        } catch (IOException e) {
            assertThat("Error: " + e.getMessage(), false);
        } catch (InterruptedException e) {
            assertThat("Error: " + e.getMessage(), false);
        }
        return isRestarted;
    }

    public static void callRestartTheBrokerExecutionGroupThread(String brokerGroup, int version) {
        String[] getQueueKey = brokerGroup.split(";");
        Map<Integer, String> result = new HashMap<>();
        int i = 0;
        for (int keyindex = 0; keyindex <= getQueueKey.length - 1; keyindex++) {
            String bBrokerName = config().getString(env + "." + getQueueKey[keyindex] + ".qManager");
            String bBrokerHost = config().getString(env + "." + getQueueKey[keyindex] + ".qHost");
            String bBrokerPort = config().getString(env + "." + getQueueKey[keyindex] + ".bBrokerPort");
            String[] bBrokerExecutionGroupID = config().getString(env + "." + getQueueKey[keyindex] + ".bBrokerExecutionGroupID").split(";");
            String bQueueChannel = config().getString(env + "." + getQueueKey[keyindex] + ".qChannel");
            for (int egIndex = 0; egIndex <= bBrokerExecutionGroupID.length - 1; egIndex++) {
                int index = egIndex;
                int count = i;
                if (version == 8) {
                    new Thread("" + index) {
                        public void run() {
                            String val = IbmMqService.reStartBroker8ExecutionGroupThreads(bBrokerName, bBrokerHost, bBrokerPort,
                                    bBrokerExecutionGroupID[index],
                                    bQueueChannel);
                            result.put(count, val);
                        }
                    }.start();
                } else if (version == 10) {
                    new Thread("" + index) {
                        public void run() {
                            String val = IbmMqService.reStartBroker10ExecutionGroupThreads(bBrokerName, bBrokerHost, bBrokerPort,
                                    bBrokerExecutionGroupID[index]);
                            result.put(count, val);
                        }
                    }.start();
                }
                i++;
            }
        }
        if (version == 8) {
            Util.sleep(60000);
        } else if (version == 10) {
            Util.sleep(90000);
        }

        String runBroker = config().getString("bdd.broker.env", "notdefined");
        if (runBroker.equals("")) {
            runBroker = "notdefined";
        }
        boolean isFailed = false;
        for (Map.Entry<Integer, String> entry : result.entrySet()) {
            if (!entry.getValue().startsWith("SUCCESS - Restarted")) {
                LogUtil.log(entry.getValue());
                if (entry.getValue().contains(runBroker)) {
                    isFailed = true;
                }
            } else {
                LogUtil.log(entry.getValue());
            }
        }

        if (isFailed && !runBroker.equals("notdefined")) {
            assertThat("Issue in restarting the running broker's execution group...", false);
        }
    }

    public static void startIIB10BrokerExecutionGroup(String messageFlow) {
        String env = config().getString("test.environment");
        String bBrokerName = config().getString(env + "." + messageFlow + ".qManager");
        String bBrokerHost = config().getString(env + "." + messageFlow + ".qHost");
        String bBrokerPort = config().getString(env + "." + messageFlow + ".bBrokerPort");
        String bBrokerExecutionGroupID = config().getString(env + "." + messageFlow + ".bBrokerExecutionGroupID");
        boolean bBrokerStatus = false;
        BrokerConnectionParameters cmcp = new IIB10Connector(bBrokerHost, Integer.parseInt(bBrokerPort), bBrokerName);

        try {

            BrokerProxy cmp = BrokerProxy.getInstance(cmcp);
            ExecutionGroupProxy ep = cmp.getExecutionGroupByName(bBrokerExecutionGroupID);
            ep.start();
            for (int i = 0; i < 10; i++) {
                if (ep.isRunning()) {
                    LogUtil.log("Started the Execution Group: " + bBrokerExecutionGroupID + " in Broker: " + bBrokerName + " And Status: " + ep.isRunning());
                    bBrokerStatus = true;
                    break;
                }
                Util.sleep(3000);
            }

            if (bBrokerStatus == false) {
                LogUtil.log("There are some issues in starting the Broker Execution Group's: " + bBrokerName + " And Status " + ep.isRunning());
            }
            cmp.disconnect();
        } catch (ConfigManagerProxyLoggedException e1) {
            LogUtil.log("MQ Error code: " + e1.getLocalizedMessage());
        } catch (ConfigManagerProxyPropertyNotInitializedException e1) {
            LogUtil.log("MQ Error code: " + e1.getLocalizedMessage());
        } catch (Exception e1) {
            LogUtil.log("MQ Error code: " + e1.getLocalizedMessage());
        }
    }

    public static void stopIIB10BrokerExecutionGroup(String messageFlow) {
        String env = config().getString("test.environment");
        String bBrokerName = config().getString(env + "." + messageFlow + ".qManager");
        String bBrokerHost = config().getString(env + "." + messageFlow + ".qHost");
        String bBrokerPort = config().getString(env + "." + messageFlow + ".bBrokerPort");
        String bBrokerExecutionGroupID = config().getString(env + "." + messageFlow + ".bBrokerExecutionGroupID");
        boolean bBrokerStatus = false;
        BrokerConnectionParameters cmcp = new IIB10Connector(bBrokerHost, Integer.parseInt(bBrokerPort), bBrokerName);

        try {

            BrokerProxy cmp = BrokerProxy.getInstance(cmcp);
            ExecutionGroupProxy ep = cmp.getExecutionGroupByName(bBrokerExecutionGroupID);
            ep.stop();
            for (int i = 0; i < 10; i++) {
                if (ep.isRunning() == false) {
                    LogUtil.log("Stopped the Execution Group: " + bBrokerExecutionGroupID + " in Broker: " + bBrokerName + " And Status: " + ep.isRunning());
                    bBrokerStatus = true;
                    break;
                }
                Util.sleep(3000);
            }

            if (bBrokerStatus == false) {
                LogUtil.log("There are some issues with stopping the Broker Execution Group's: " + bBrokerName + " And Status " + ep.isRunning());
            }
            cmp.disconnect();
        } catch (ConfigManagerProxyLoggedException e1) {
            LogUtil.log("MQ Error code: " + e1.getLocalizedMessage());
        } catch (ConfigManagerProxyPropertyNotInitializedException e1) {
            LogUtil.log("MQ Error code: " + e1.getLocalizedMessage());
        } catch (Exception e1) {
            LogUtil.log("MQ Error code: " + e1.getLocalizedMessage());
        }
    }

    public static void checkMessagesInQueue(String inputQueue, String queueKey, String messageSearchKey) {
        String env = config().getString("test.environment");
        String[] getQueueKey = queueKey.split(";");
        String[] getTextMessage = null;

        LogUtil.log("Message Search Key: " + messageSearchKey);
        for (int i = 1; i <= 3; i++) {
            for (int keyindex = 0; keyindex <= getQueueKey.length - 1; keyindex++) {
                getTextMessage = (IbmMqService.getMessageWithTokenByText(getQueueKey[keyindex], messageSearchKey, inputQueue));
                if (getTextMessage[0] != null) {
                    break;
                }
            }
            if (getTextMessage[0] != null) {
                break;
            } else {
                Util.sleep(3000);
            }

        }
        String outputORExceptionQName = config().getString(env + "." + getQueueKey[0] + "." + inputQueue);

        if (getTextMessage[0] == null && getTextMessage[1] == null) {
            LogUtil.log("No Records in " + outputORExceptionQName);
        } else {
            LogUtil.logAttachment("Error Message", getTextMessage[0]);
            LogUtil.logAttachment("EXCEPTION Error Message", getTextMessage[1]);
        }
    }

    public static void verifyNoErrorMessage(String[] getText, String outputORExceptionQName) {
        if (getText[0] == null && getText[1] == null) {
            LogUtil.log("No Records in " + outputORExceptionQName + " - Passed");
        } else {
            LogUtil.logAttachment("Error Message", getText[0]);
            LogUtil.logAttachment("EXCEPTION Error Message", getText[1]);
            assertThat("Records in " + outputORExceptionQName, false);
        }
    }

    public static String[] theMessageShouldBeInMQQueue(String inputQueue, String queueKey, String messageSearchKey) {
        String[] getMessageReturnText = null;
        String[] getQueueKey = queueKey.split(";");
        String[] getTextMessage = null;
        LogUtil.log("Message Search Key: " + messageSearchKey);
        for (int i = 1; i <= 3; i++) {
            for (int keyindex = 0; keyindex <= getQueueKey.length - 1; keyindex++) {
                getMessageReturnText = getMessageWithTokenByText(getQueueKey[keyindex], messageSearchKey, inputQueue);
                getTextMessage = getMessageReturnText;
                if (getTextMessage[0] != null) {
                    break;
                }
            }
            if (getTextMessage[0] != null) {
                break;
            } else {
                try {
                    Util.sleep(3000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        String outputORExceptionQName = inputQueue;
        String[] getText = getMessageReturnText;

        if (getText[0] == null && getText[1] == null) {
            assertThat("No Records in " + outputORExceptionQName, false);
        } else {
            LogUtil.logAttachment("Output Message for key: " + messageSearchKey, getText[0]);
            if (getText[1] != null) {
                LogUtil.logAttachment("Exception Message: " + messageSearchKey, getText[1]);
            }
        }
        return getMessageReturnText;
    }

}
