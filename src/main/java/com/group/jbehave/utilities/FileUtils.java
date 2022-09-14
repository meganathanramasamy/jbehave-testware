package com.group.jbehave.utilities;

import com.group.bdd.framework.LogUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.group.bdd.framework.Asserts.assertThat;

public class FileUtils {
    private static final Logger LOG = LogManager.getLogger(FileUtils.class);

    public String readTheFile(String fileName, String extenstion) {
        String generateXML = "";
        String filePath = new File("src/main/resources").getAbsoluteFile().toString();

        File file = new File(filePath + "/input_files/" + fileName + extenstion);
        try {
            BufferedReader reader;
            reader = new BufferedReader(new FileReader(file.toString()));
            String line;
            while ((line = reader.readLine()) != null) {
                generateXML = generateXML + line + "\r\n";
            }
            reader.close();
        } catch (IOException e) {
            assertThat("Error: " + e, false);
        }

        return generateXML;
    }

    public static String readTheSchemaFile(String fileName) {
        String generateXML = "";
        String filePath = new File("src/main/resources").getAbsoluteFile().toString();

        File file = new File(filePath + "/schemas/" + fileName);
        try {
            BufferedReader reader;
            reader = new BufferedReader(new FileReader(file.toString()));
            String line;
            while ((line = reader.readLine()) != null) {
                generateXML = generateXML + line + "\r\n";
            }
            reader.close();
        } catch (IOException e) {
            assertThat("Error: " + e, false);
        }

        return generateXML;
    }

    public Map<Integer, String> readFileNames(String path) {
        Map<Integer, String> filesList = new HashMap<>();
        String filePath = new File("src/main/resources").getAbsoluteFile().toString();
        File folder = new File(filePath + "/input_files/" + path);
        int i = 1;
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                filesList.put(i, file.getName());
            }
            i++;
        }
        return filesList;
    }

    public static String readTheFileFromAbsPath(String fileName) {
        String message = "";
        try {
            BufferedReader reader;
            reader = new BufferedReader(new FileReader(fileName.toString()));
            String line;
            while ((line = reader.readLine()) != null) {
                message = message + line + "\n";
            }
            reader.close();
        } catch (IOException e) {
            assertThat("Error: " + e, false);
        }
        return message;
    }

    public static String readtextFileToString(String filePath) {
        String pathStr = System.getProperty("user.dir") + "\\" + filePath + ".txt";
        return readTheFileFromAbsPath(pathStr);
    }

    public static void deleteLogFile(String logFileName) {
        String pathStr = System.getProperty("user.dir") + "\\" + logFileName + ".txt";
        Path path = Paths.get(pathStr);
        try {
            Files.delete(path);
        } catch (IOException e) {
            LogUtil.log("Unable to delete file: " + logFileName);
        }
    }

    public static LinkedList listFilesAndFilesSubDirectories(String directoryName, LinkedList list) {
        File directory = new File(directoryName);
        File[] fList = directory.listFiles();
        for (File propFile : fList) {
            if (propFile.isFile()) {
                list.add(propFile.getAbsolutePath());
            } else if (propFile.isDirectory()) {
                listFilesAndFilesSubDirectories(propFile.toString(), list);
            }
        }
        return list;
    }

    public static void writeFile(String outputFileName, String fileContents) {
        String file = new File("src/main/resources").getAbsoluteFile().toString();
        LOG.info("Writing the output outputFileName: " + outputFileName);
        LOG.info("Writing the output fileContents: " + fileContents);
        try {
            FileWriter fstream = new FileWriter(file + "/output_files/" + outputFileName + ".xml");
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(fileContents);
            out.close();
            LOG.info("Writing the output file: " + fstream.toString());
        } catch (Exception e) {
            LOG.info(e.getLocalizedMessage().toString());
        }
    }

    public static void copyFiles(String inputPath, String outFile) {
        try {
            File inputFile = new File(inputPath);
            FileInputStream instream = new FileInputStream(inputFile);
            OutputStream bos = new FileOutputStream(outFile);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = instream.read(buffer, 0, 8192)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            bos.close();
            LogUtil.log("File is copied into BFR Path!.");

        } catch (Exception e) {
            LogUtil.log("Error: " + e);
            assertThat("Error: " + e, false);
        }
    }

    public static void writeTextFile(String outPath, String message) {
        File file = new File(outPath);

        try (FileOutputStream fop = new FileOutputStream(file)) {
            file.setExecutable(true, false);
            file.setReadable(true, false);
            file.setWritable(true, false);
            if (!file.exists()) {
                file.createNewFile();
            }

            byte[] contentInBytes = message.getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();

            LogUtil.log("File is copied into BFR Path!. ==> " + outPath);

        } catch (IOException e) {
            assertThat("Error: " + e, false);
        }
    }

    public static String findFileExists(String directory, String fileName, String noFound, String processingTime) {
        File file = new File(directory + "\\" + fileName);
        boolean isFound = false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        try {
            Date currDateTime = sdf.parse(processingTime);
            for (int loop = 1; loop <= 10; loop++) {
                if (file.exists()) {
                    String fileLastModified = sdf.format(file.lastModified());
                    Date lastModifiedTime = sdf.parse(fileLastModified);
                    if (lastModifiedTime.after(currDateTime)) {
                        LogUtil.log("File: " + fileName + " Exists in directory: " + directory);
                        isFound = true;
                        break;
                    } else {
                        isFound = false;
                    }
                    LogUtil.log("New File Not Found. Waiting for another 30 Seconds");
                    Util.sleep(30000);
                } else {
                    isFound = false;
                    break;
                }
            }

            if (isFound == false) {
                LogUtil.log("File: " + fileName + " NOT Exists in directory: " + directory);
                noFound = noFound + ", " + fileName;
            }

            if (noFound.startsWith(",")) {
                noFound = noFound.substring(1);
            }
        } catch (Exception e) {
            assertThat("Error while searching file. " + e.getMessage(), false);
        }
        return noFound;
    }

    public static void createZipFromDirectory(String srcDir, String zipFile) {
        try {
            File zip = new File(zipFile);
            if (!zip.exists()) {
                zip.createNewFile();
            }

            byte[] buffer = new byte[1024];
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);
            File dir = new File(srcDir);
            File[] files = dir.listFiles();

            for (int i = 0; i < files.length; i++) {
                LOG.info("Adding file: " + files[i].getName());
                FileInputStream fis = new FileInputStream(files[i]);
                zos.putNextEntry(new ZipEntry(files[i].getName()));
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
                fis.close();
            }
            zos.close();
        } catch (IOException ioe) {
            LOG.info("Error creating zip file" + ioe);
        }
    }

    public static void deleteFiles(String filePath) {
        File folder = new File(filePath);
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                String fileName = file.getName();
                boolean del = file.delete();
                LOG.info(fileName + " : got deleted ? " + del);
            } else if (file.isDirectory()) {
                deleteFiles(file.getAbsolutePath());
            }
        }
    }
}
