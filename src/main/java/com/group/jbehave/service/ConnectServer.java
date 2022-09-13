package com.group.jbehave.service;

import com.group.jbehave.utilities.Util;
import com.jcraft.jsch.*;
import com.group.bdd.framework.LogUtil;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.group.bdd.framework.Asserts.assertThat;
import static com.group.bdd.framework.ConfigLoader.config;

public class ConnectServer {

    public static void sftpFileTransfer(String std18Message, String sftpLocation, String sftpFileName, String sftpKey, boolean isdataFile, boolean isflag) {
        ChannelSftp sftp = new ChannelSftp();
        String env = config().getString("test.environment");
        String server = config().getString(env + "." + "SFTP." + sftpKey + ".Server");
        String user = config().getString(env + "." + "SFTP." + sftpKey + ".User");
        String pass = config().getString(env + "." + "SFTP." + sftpKey + ".Pass");
        int port = Integer.parseInt(config().getString(env + "." + "SFTP." + sftpKey + ".Port"));
        int userGroup = Integer.parseInt(config().getString(env + "." + "SFTP." + sftpKey + ".Group"));

        LogUtil.log("Location: " + sftpLocation);
        LogUtil.log("Server: " + server);

        String rfile = sftpLocation + sftpFileName;
        String rflagFile = sftpLocation + sftpFileName + ".flag";
        String std18MessageFlag = "";

        try {
            JSch jsch = new JSch();
            jsch.getSession(user, server, port);
            Session sshSession = jsch.getSession(user, server, port);
            sshSession.setPassword(pass);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);

            sshSession.connect();
            Channel channel = sshSession.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;
            Charset windows1252 = Charset.forName("utf-8");

            if (isdataFile) {
                LogUtil.log("Sending file: " + rfile);
                InputStream std18Text = new ByteArrayInputStream(std18Message.getBytes(windows1252));
                sftp.put(std18Text, rfile, 0775);
            }
            if (isflag) {
                LogUtil.log("Sending file: " + rflagFile);
                InputStream std18TextFlag = new ByteArrayInputStream(std18MessageFlag.getBytes(windows1252));
                sftp.put(std18TextFlag, rflagFile, 0775);
            }

            try {
                if (isdataFile) {
                    sftp.chmod(0775, rfile);
                }
                if (isflag) {
                    sftp.chmod(0775, rflagFile);
                }
            } catch (SftpException e) {
                LogUtil.log("Warning - Unable to set permissions: " + e.getMessage());
            }

            try {
                if (isdataFile) {
                    sftp.chgrp(userGroup, rfile);
                }
                if (isflag) {
                    sftp.chgrp(userGroup, rflagFile);
                }
            } catch (SftpException e) {
                LogUtil.log("Unable to setGroup. Error: " + e.getMessage());
            }

            sftp.disconnect();
            sshSession.disconnect();
            LogUtil.log("Send file(s) completed. - Pass");
        } catch (JSchException e) {
            assertThat("Send file failed. Error: " + e.getMessage(), false);
        } catch (SftpException e) {
            assertThat("Send file failed. Error: " + e.getMessage(), false);
        } catch (Exception e) {
            assertThat("Send file failed. Error: " + e.getMessage(), false);
        }
    }

    public static String sftpReadFile(String sftpLocation, String sftpFileName, String sftpKey) {
        ChannelSftp sftp = new ChannelSftp();
        String env = config().getString("test.environment");
        String server = config().getString(env + "." + "SFTP." + sftpKey + ".Server");
        String user = config().getString(env + "." + "SFTP." + sftpKey + ".User");
        String pass = config().getString(env + "." + "SFTP." + sftpKey + ".Pass");
        int port = Integer.parseInt(config().getString(env + "." + "SFTP." + sftpKey + ".Port"));
        String result = "empty", error = "";
        String needFile = sftpLocation + sftpFileName;
        LogUtil.log("SFTP Location: " + sftpLocation + ", SFTP FileName: " + sftpFileName);
        LogUtil.log("Server: '" + server + "' Searching File: " + needFile);
        try {
            JSch jsch = new JSch();
            jsch.getSession(user, server, port);
            Session sshSession = jsch.getSession(user, server, port);
            sshSession.setPassword(pass);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);

            sshSession.connect();
            Channel channel = sshSession.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;
            int retryLimit = 10;

            for (int i = 1; i <= retryLimit; i++) {
                if (result.equals("empty")) {
                    error = "";
                    try {
                        InputStream inStream = sftp.get(needFile);
                        result = IOUtils.toString(inStream, StandardCharsets.UTF_8);
                    } catch (SftpException e) {
                        result = "empty";
                        error = e.getLocalizedMessage();
                    } catch (IOException e) {
                        result = "empty";
                        error = e.getLocalizedMessage();
                    }
                }

                if (result.equals("empty")) {
                    Util.sleep(3000);
                } else {
                    break;
                }

                i++;
            }

            sftp.disconnect();
            sshSession.disconnect();

            if (!error.equals("") && !error.toLowerCase().contains("permission")) {
                assertThat("File Not Found. Error: " + error, false);
            } else {
                LogUtil.log("File Found!. " + sftpFileName);
                if (!result.equals("empty")) {
                    LogUtil.logAttachment("Click here to see the file: " + sftpFileName, result);
                }
            }
        } catch (JSchException e) {
            assertThat("Read file '" + sftpFileName + "' failed. Error: " + e.getMessage(), false);
        }

        return result;
    }

    public static String sftpDeleteFileIfExists(String sftpLocation, String sftpFileName, String sftpKey) {
        ChannelSftp sftp = new ChannelSftp();
        String env = config().getString("test.environment");
        String server = config().getString(env + "." + "SFTP." + sftpKey + ".Server");
        String user = config().getString(env + "." + "SFTP." + sftpKey + ".User");
        String pass = config().getString(env + "." + "SFTP." + sftpKey + ".Pass");
        int port = Integer.parseInt(config().getString(env + "." + "SFTP." + sftpKey + ".Port"));
        String result = "empty", error = "";
        String needFile = sftpLocation + sftpFileName;
        LogUtil.log("Server: '" + server + "' Searching File: " + needFile);
        try {
            JSch jsch = new JSch();
            jsch.getSession(user, server, port);
            Session sshSession = jsch.getSession(user, server, port);
            sshSession.setPassword(pass);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);

            sshSession.connect();
            Channel channel = sshSession.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;

            if (result.equals("empty")) {
                try {
                    sftp.rm(needFile);
                } catch (SftpException e) {
                    result = "empty";
                    error = e.getLocalizedMessage();
                } catch (Exception e) {
                    result = "empty";
                    error = e.getLocalizedMessage();
                }
            }

            sftp.disconnect();
            sshSession.disconnect();
            result = "success";
            if (!error.equals("")) {
                LogUtil.log("File Not Found. Error: " + error);
            } else {
                LogUtil.log("File deletion success!. " + sftpFileName);
            }
        } catch (JSchException e) {
            LogUtil.log("Delete file '" + sftpFileName + "' failed. Error: " + e.getMessage());
        }

        return result;
    }

    public static String sftpDeleteFileIfExistsByExtension(String sftpLocation, String sftpFileExt, String sftpKey) {
        ChannelSftp sftp = new ChannelSftp();
        String env = config().getString("test.environment");
        String server = config().getString(env + "." + "SFTP." + sftpKey + ".Server");
        String user = config().getString(env + "." + "SFTP." + sftpKey + ".User");
        String pass = config().getString(env + "." + "SFTP." + sftpKey + ".Pass");
        int port = Integer.parseInt(config().getString(env + "." + "SFTP." + sftpKey + ".Port"));
        String result = "empty", error = "";
        String needFile = "";
        LogUtil.log("Server: '" + server + "' Searching File: " + needFile);
        try {
            JSch jsch = new JSch();
            jsch.getSession(user, server, port);
            Session sshSession = jsch.getSession(user, server, port);
            sshSession.setPassword(pass);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);

            sshSession.connect();
            Channel channel = sshSession.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;

            Vector<ChannelSftp.LsEntry> files = new Vector<>();
            files = sftp.ls(sftpLocation);
            for (ChannelSftp.LsEntry entry : files) {
                if (entry.getFilename().endsWith(sftpFileExt)) {
                    needFile = sftpLocation + entry.getFilename();

                    if (result.equals("empty")) {
                        try {
                            sftp.rm(needFile);
                        } catch (SftpException e) {
                            result = "empty";
                            error = e.getLocalizedMessage();
                        } catch (Exception e) {
                            result = "empty";
                            error = e.getLocalizedMessage();
                        }
                    }
                    if (!error.equals("")) {
                        LogUtil.log("File Not Found. Error: " + error);
                    } else {
                        LogUtil.log("File deletion success!. " + needFile);
                    }
                }
            }

            sftp.disconnect();
            sshSession.disconnect();
            result = "success";

        } catch (JSchException e) {
            LogUtil.log("Delete file with extension '" + sftpFileExt + "' failed. Error: " + e.getMessage());
        } catch (SftpException e) {
            LogUtil.log("Delete file with extension '" + sftpFileExt + "' failed. Error: " + e.getMessage());
        }

        return result;
    }

    public static String sftpFSReadFile(String sftpLocation, String sftpFileName, String sftpKey) {
        ChannelSftp sftp = new ChannelSftp();
        String env = config().getString("test.environment");
        String server = config().getString(env + "." + "SFTP." + sftpKey + ".Server");
        String user = config().getString(env + "." + "SFTP." + sftpKey + ".User");
        String pass = config().getString(env + "." + "SFTP." + sftpKey + ".Pass");

        String result = "empty", error = "";
        String needFile = sftpLocation + sftpFileName;
        LogUtil.log("Server: '" + server + "' Searching File: " + needFile);
        try {
            JSch jsch = new JSch();
            jsch.getSession(user, server, 22);
            Session sshSession = jsch.getSession(user, server, 22);
            sshSession.setPassword(pass);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);

            sshSession.connect();
            Channel channel = sshSession.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;

            for (int i = 1; i <= 3; i++) {
                if (result.equals("empty")) {
                    try {
                        InputStream inStream = sftp.get(needFile);
                        result = IOUtils.toString(inStream, StandardCharsets.UTF_8);
                    } catch (SftpException e) {
                        result = "empty";
                        error = e.getLocalizedMessage();
                    } catch (IOException e) {
                        result = "empty";
                        error = e.getLocalizedMessage();
                    }
                }

                if (result.equals("empty")) {
                    Util.sleep(3000);
                } else {
                    break;
                }

                i++;
            }

            sftp.disconnect();
            sshSession.disconnect();

            if (!error.equals("")) {
                LogUtil.log("File Not Found. Error: " + error);
                result = "FAILED";
            } else {
                LogUtil.log("File Found!. " + sftpFileName);
                if (!result.equals("")) {
                    LogUtil.logAttachment("Click here to see the file: " + sftpFileName, result);
                }
            }
        } catch (JSchException e) {
            assertThat("Read file '" + sftpFileName + "' failed. Error: " + e.getMessage(), false);
        }

        return result;
    }

    public static List<String> sftpReadAllFileNames(String sftpLocation, String sftpKey) {
        List<String> list = new ArrayList<>();
        ChannelSftp sftp = new ChannelSftp();
        String env = config().getString("test.environment");
        String server = config().getString(env + "." + "SFTP." + sftpKey + ".Server");
        String user = config().getString(env + "." + "SFTP." + sftpKey + ".User");
        String pass = config().getString(env + "." + "SFTP." + sftpKey + ".Pass");
        LogUtil.log("BFR Location: " + sftpLocation);
        try {
            JSch jsch = new JSch();
            jsch.getSession(user, server, 22);
            Session sshSession = jsch.getSession(user, server, 22);
            sshSession.setPassword(pass);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);

            sshSession.connect();
            Channel channel = sshSession.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;

            Vector<ChannelSftp.LsEntry> files = new Vector<>();
            files = sftp.ls(sftpLocation);
            for (ChannelSftp.LsEntry entry : files) {
                if (!entry.getFilename().equals(".") && !entry.getFilename().equals("..")) {
                    list.add(entry.getFilename());
                }
            }
        } catch (Exception e) {
            assertThat("Exception: " + e.getMessage(), false);
        }

        return list;
    }

    public static String sftpExecuteFile(String sftpLocation, String sftpFileName, String sftpKey, String parameter) {
        ChannelSftp sftp = new ChannelSftp();
        String env = config().getString("test.environment");
        String server = config().getString(env + "." + "SFTP." + sftpKey + ".Server");
        String user = config().getString(env + "." + "SFTP." + sftpKey + ".User");
        String pass = config().getString(env + "." + "SFTP." + sftpKey + ".Pass");

        String result = "empty";
        String commands = sftpLocation + sftpFileName + " " + parameter;
        LogUtil.log("SFTP Location: " + sftpLocation + ", Script FileName: " + sftpFileName + ", Parameter: " + parameter);
        LogUtil.log("Server: '" + server + "' Executing command: " + commands);
        try {
            JSch jsch = new JSch();
            jsch.getSession(user, server, 22);
            Session sshSession = jsch.getSession(user, server, 22);
            sshSession.setPassword(pass);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);

            sshSession.connect();
            Channel channel = sshSession.openChannel("sftp");
            channel = sshSession.openChannel("exec");

            ((ChannelExec) channel).setCommand(commands);
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(System.err);

            InputStream in = channel.getInputStream();
            channel.connect();
            /*
            byte[] tmp = new byte[1024];

            for (int loop = 1; loop <= 3; loop++) {
                while ( in.available() > 0 ) {
                    int i = in.read(tmp, 0, 1024);
                    if ( i < 0 ) break;
                    result = new String(tmp, 0, i);
                }
                if (channel.isClosed() ) {
                    break;
                }
                Util.sleep(1000);
            }
            */

            sftp.disconnect();
            sshSession.disconnect();
            LogUtil.log(sftpFileName + " Script Execution success. Please check the logs for more details..");
        } catch (Exception e) {
            assertThat("Script Execution failed. Error: " + e.getMessage(), false);
        }

        return result;
    }

    public static String sftpCreateDirectory(String sftpLocation, String sftpNewFolder, String sftpKey) {
        ChannelSftp sftp = new ChannelSftp();
        String env = config().getString("test.environment");
        String server = config().getString(env + "." + "SFTP." + sftpKey + ".Server");
        String user = config().getString(env + "." + "SFTP." + sftpKey + ".User");
        String pass = config().getString(env + "." + "SFTP." + sftpKey + ".Pass");

        String result = "empty", error = "";
        String needFolder = sftpLocation + sftpNewFolder;
        String reqFolders = sftpLocation;
        LogUtil.log("Server: '" + server + "' Creating Folder: " + needFolder);
        try {
            JSch jsch = new JSch();
            jsch.getSession(user, server, 22);
            Session sshSession = jsch.getSession(user, server, 22);
            sshSession.setPassword(pass);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);

            sshSession.connect();
            Channel channel = sshSession.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;

            if (result.equals("empty")) {
                try {
                    SftpATTRS attrs = null;
                    try {
                        attrs = sftp.stat(needFolder);
                    } catch (Exception e) {
                    }
                    if (attrs != null) {
                        result = "Directory already exists IsDir=" + attrs.isDir();
                    } else {
                        String[] subfolders = sftpNewFolder.split("/");
                        for (int i = 0; i <= subfolders.length - 1; i++) {
                            if (!subfolders[i].equals("")) {
                                reqFolders = reqFolders + subfolders[i] + "/";
                                SftpATTRS subAttrs = null;
                                try {
                                    subAttrs = sftp.stat(reqFolders);
                                } catch (Exception e) {
                                }
                                if (subAttrs == null) {
                                    sftp.mkdir(reqFolders);
                                }
                            }
                        }
                        result = "Folder creation success!. - " + reqFolders;
                    }
                } catch (Exception e) {
                    result = "Error in creating directory: " + needFolder;
                    error = e.getLocalizedMessage();
                }
            }

            sftp.disconnect();
            sshSession.disconnect();

            if (!error.equals("")) {
                LogUtil.log("Folder creation failure. Error: " + error);
            } else {
                LogUtil.log(result);
            }
        } catch (JSchException e) {
            LogUtil.log("Create folder '" + needFolder + "' failed. Error: " + e.getMessage());
        }

        return result;
    }

    public static HashMap<String, String> sftpGetLatestFileAndTimefromDir(String sftpLocation, String sftpKey) {
        HashMap<String, String> map = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        ChannelSftp sftp = new ChannelSftp();
        String env = config().getString("test.environment");
        String server = config().getString(env + "." + "SFTP." + sftpKey + ".Server");
        String user = config().getString(env + "." + "SFTP." + sftpKey + ".User");
        String pass = config().getString(env + "." + "SFTP." + sftpKey + ".Pass");
        LogUtil.log("BFR Location: " + sftpLocation);
        try {
            JSch jsch = new JSch();
            jsch.getSession(user, server, 22);
            Session sshSession = jsch.getSession(user, server, 22);
            sshSession.setPassword(pass);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);

            sshSession.connect();
            Channel channel = sshSession.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;

            Vector<ChannelSftp.LsEntry> files = new Vector<>();
            files = sftp.ls(sftpLocation);
            Date lastModifiedTime = null;
            for (ChannelSftp.LsEntry entry : files) {
                if (!entry.getFilename().equals(".") && !entry.getFilename().equals("..")) {
                    SftpATTRS attrs = entry.getAttrs();
                    String fileLastModified = sdf.format(new Date(attrs.getMTime() * 1000L));
                    Date fileModifiedTime = sdf.parse(fileLastModified);
                    if (lastModifiedTime == null || fileModifiedTime.after(lastModifiedTime)) {
                        lastModifiedTime = fileModifiedTime;
                        map.put("FileName", entry.getFilename());
                        map.put("ModifiedTime", fileLastModified);
                    }
                }
            }
        } catch (Exception e) {
            assertThat("Exception: " + e.getMessage(), false);
        }

        return map;
    }

    public static HashMap<String, String> sftpAttachLatestFilesFromDir(String sftpLocation, String sftpKey, String lastModifiedFileTime, HashMap<String, String> map) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        ChannelSftp sftp = new ChannelSftp();
        String env = config().getString("test.environment");
        String server = config().getString(env + "." + "SFTP." + sftpKey + ".Server");
        String user = config().getString(env + "." + "SFTP." + sftpKey + ".User");
        String pass = config().getString(env + "." + "SFTP." + sftpKey + ".Pass");
        LogUtil.log("BFR Location: " + sftpLocation);
        try {
            JSch jsch = new JSch();
            jsch.getSession(user, server, 22);
            Session sshSession = jsch.getSession(user, server, 22);
            sshSession.setPassword(pass);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);

            sshSession.connect();
            Channel channel = sshSession.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;

            Vector<ChannelSftp.LsEntry> files = new Vector<>();
            files = sftp.ls(sftpLocation);
            Date lastModifiedTime = sdf.parse(lastModifiedFileTime);
            for (ChannelSftp.LsEntry entry : files) {
                if (!entry.getFilename().equals(".") && !entry.getFilename().equals("..")) {
                    SftpATTRS attrs = entry.getAttrs();
                    String fileLastModified = sdf.format(new Date(attrs.getMTime() * 1000L));
                    Date fileModifiedTime = sdf.parse(fileLastModified);
                    if (fileModifiedTime.after(lastModifiedTime)) {
                        map.put(entry.getFilename(), sftpReadFile(sftpLocation, entry.getFilename(), sftpKey));
                        try {
                            sftp.chmod(0777, sftpLocation + entry.getFilename());
                        } catch (SftpException e) {
                            LogUtil.log("Warning: Unable to change file permissions. " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            assertThat("Exception: " + e.getMessage(), false);
        }

        return map;
    }

    public static String sftpFileRename(String sftpLocation, String sftpFileName, String sftpKey, String newFileName) {
        ChannelSftp sftp = new ChannelSftp();
        String env = config().getString("test.environment");
        String server = config().getString(env + "." + "SFTP." + sftpKey + ".Server");
        String user = config().getString(env + "." + "SFTP." + sftpKey + ".User");
        String pass = config().getString(env + "." + "SFTP." + sftpKey + ".Pass");

        String result = "empty", error = "";
        String needFile = sftpLocation + sftpFileName;
        String newFile = sftpLocation + newFileName;
        LogUtil.log("Server: '" + server + "' Searching File: " + needFile);
        try {
            JSch jsch = new JSch();
            jsch.getSession(user, server, 22);
            Session sshSession = jsch.getSession(user, server, 22);
            sshSession.setPassword(pass);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);

            sshSession.connect();
            Channel channel = sshSession.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;

            for (int i = 1; i <= 3; i++) {
                if (result.equals("empty")) {
                    try {
                        InputStream inStream = sftp.get(needFile);
                        result = IOUtils.toString(inStream, StandardCharsets.UTF_8);
                        if (!result.equals("empty")) {
                            sftp.rename(needFile, newFile);
                            inStream = sftp.get(newFile);
                            result = IOUtils.toString(inStream, StandardCharsets.UTF_8);
                        }
                    } catch (SftpException e) {
                        result = "empty";
                        error = e.getLocalizedMessage();
                    } catch (IOException e) {
                        result = "empty";
                        error = e.getLocalizedMessage();
                    }
                }

                if (result.equals("empty")) {
                    Util.sleep(3000);
                } else {
                    break;
                }

                i++;
            }

            sftp.disconnect();
            sshSession.disconnect();

            if (!error.equals("")) {
                assertThat("File Not Found. Error: " + error, false);
            } else {
                LogUtil.log("File '" + sftpFileName + "' is found and renamed to '" + newFileName + "'");

                if (!result.equals("")) {
                    LogUtil.logAttachment("Click here to see the file: " + sftpFileName, result);
                }
            }
        } catch (JSchException e) {
            assertThat("Read file '" + sftpFileName + "' failed. Error: " + e.getMessage(), false);
        }

        return result;
    }

    public static List<String> sftpReadAllFileNamesIncludingSubfolders(String sftpLocation, String sftpKey) {
        List<String> list = new ArrayList<>();
        ChannelSftp sftp = new ChannelSftp();
        String env = config().getString("test.environment");
        String server = config().getString(env + "." + "SFTP." + sftpKey + ".Server");
        String user = config().getString(env + "." + "SFTP." + sftpKey + ".User");
        String pass = config().getString(env + "." + "SFTP." + sftpKey + ".Pass");
        LogUtil.log("BFR Location: " + sftpLocation);
        try {
            JSch jsch = new JSch();
            jsch.getSession(user, server, 22);
            Session sshSession = jsch.getSession(user, server, 22);
            sshSession.setPassword(pass);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);

            sshSession.connect();
            Channel channel = sshSession.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;

            list = sftpRecursiveReadFileNames(list, sftp, sftpLocation);

            sftp.disconnect();
            sshSession.disconnect();
        } catch (Exception e) {
            assertThat("Exception: " + e.getMessage(), false);
        }

        return list;
    }

    private static List<String> sftpRecursiveReadFileNames(List<String> fileList, ChannelSftp sftp, String startFolder) {
        if (!startFolder.endsWith("/")) startFolder += "/";
        Vector<ChannelSftp.LsEntry> files;
        try {
            files = sftp.ls(startFolder);
            for (ChannelSftp.LsEntry entry : files) {
                if (!entry.getFilename().equals(".") && !entry.getFilename().equals("..")) {
                    if (entry.getAttrs().isDir()) {
                        fileList = sftpRecursiveReadFileNames(fileList, sftp, startFolder + entry.getFilename());
                    } else {
                        fileList.add(entry.getFilename());
                    }
                }
            }
        } catch (Exception e) {
            assertThat("Exception: " + e.getMessage(), false);
        }

        return fileList;
    }
}
