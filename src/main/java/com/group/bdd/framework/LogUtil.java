package com.group.bdd.framework;

import com.group.bdd.framework.web.BrowserDriver;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.annotations.Step;
import ru.yandex.qatools.allure.events.MakeAttachmentEvent;
import ru.yandex.qatools.allure.events.StepFinishedEvent;
import ru.yandex.qatools.allure.events.StepStartedEvent;

import ru.yandex.qatools.allure.model.Description;
import ru.yandex.qatools.allure.model.DescriptionType;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;


public final class LogUtil {
    public static boolean isnestedLog = false;
    private static final Logger LOG = Logger.getLogger(LogUtil.class);

    private LogUtil() {
    }

    @Step("{0}")
    public static void log(final String message) {
        LOG.info("Logger : " + message);

        StepStartedEvent event = new StepStartedEvent(message);
        Description description = new Description();
        description.setValue(message);
        description.setType(DescriptionType.MARKDOWN);
        event.withTitle(message);

        Allure.LIFECYCLE.fire(event);
        Allure.LIFECYCLE.fire(new StepFinishedEvent());
    }

    public static void logAttachment(String fileName, String text) {
        LOG.info("Logger : " + text);
        Allure.LIFECYCLE.fire(new MakeAttachmentEvent(text.getBytes(), fileName, "text/plain"));
        //AllureReporter.addTextAttachment(fileName, text);
    }

    public static void logAttachmentHTML(String fileName, String text) {
        LOG.info("Logger : " + text);
        Allure.LIFECYCLE.fire(new MakeAttachmentEvent(text.getBytes(), fileName, "text/html"));
        //AllureReporter.addTextAttachment(fileName, text);
    }

    public static void logAttachmentJson(String fileName, String text) {
        LOG.info("Logger : " + text);
        Allure.LIFECYCLE.fire(new MakeAttachmentEvent(text.getBytes(), fileName, "text/json"));
        //AllureReporter.addTextAttachment(fileName, text);
    }

    public static void logAttachmentXML(String fileName, String text) {
        LOG.info("Logger : " + text);
        text = format(text);
        Allure.LIFECYCLE.fire(new MakeAttachmentEvent(text.getBytes(), fileName, "text/xml"));
    }

    public static void logAttachmentZip(String fileName, File text) {
        LOG.info("Logger : " + text);
        try {
            byte[] data = FileUtils.readFileToByteArray(text);
            Allure.LIFECYCLE.fire(new MakeAttachmentEvent(data, fileName, "application/zip"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ;

    }

    @Step("{0}")
    public static void nestedLogStart(final String message) {
        isnestedLog = true;
        LOG.info("Logger : " + message);
        StepStartedEvent event = new StepStartedEvent(message);
        Description description = new Description();
        description.setValue(message);
        description.setType(DescriptionType.MARKDOWN);
        event.withTitle(message);
        Allure.LIFECYCLE.fire(event);
    }

    public static void nestedLogClose() {
        isnestedLog = false;
        Allure.LIFECYCLE.fire(new StepFinishedEvent());
    }

    public static void logCSVAttachment(String fileName, String text) {
        LOG.info("Logger : " + text);
        Allure.LIFECYCLE.fire(new MakeAttachmentEvent(text.getBytes(), fileName, "text/csv"));
        //AllureReporter.addTextAttachment(fileName, text);
    }

    private static Document parseXmlFile(String in) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String format(String xml) {
        try {
            final InputSource src = new InputSource(new StringReader(xml));
            final Node document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src).getDocumentElement();
            final Boolean keepDeclaration = Boolean.valueOf(xml.startsWith("<?xml"));
            final Document domDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            String encoding = domDoc.getXmlEncoding();
            if (encoding == null) {
                encoding = "UTF-8";
            }
            final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");

            LSOutput lsOutput = impl.createLSOutput();
            lsOutput.setEncoding(encoding);

            final LSSerializer writer = impl.createLSSerializer();
            writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
            writer.getDomConfig().setParameter("xml-declaration", keepDeclaration);

            Writer stringWriter = new StringWriter();
            lsOutput.setCharacterStream(stringWriter);
            writer.write(document, lsOutput);
            return stringWriter.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void attachScreenshot(String message) {
        if (BrowserDriver.hasInstance()) {
            Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000)).takeScreenshot(BrowserDriver.getDriver());
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ImageIO.write(fpScreenshot.getImage(), "jpg", bos);
                Allure.LIFECYCLE.fire(new MakeAttachmentEvent(bos.toByteArray(), message + " at " + getTimeStamp(), "Image/png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void takeScreenshot(String message) {
        try {
            final byte[] screenshot = ((TakesScreenshot) BrowserDriver.getDriver()).getScreenshotAs(OutputType.BYTES);
            Allure.LIFECYCLE.fire(new MakeAttachmentEvent(screenshot, message + " at " + getTimeStamp(), "Image/png"));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void saveScreenshots(String name) {
        TakesScreenshot tt = (TakesScreenshot) BrowserDriver.getDriver();
        File src = tt.getScreenshotAs(OutputType.FILE);
        File dest = new File("src/../target/screenhots/" + name + ".png");
        try {
            FileUtils.copyFile(src, dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getTimeStamp() {
        return new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
    }

    public static void logVideoAttachment(String fileName, byte[] video) {
        Allure.LIFECYCLE.fire(new MakeAttachmentEvent(video, fileName, "video/mp4"));
    }

    public static void attachScreenshotOnWebElement(String message, WebElement element) {

        if (BrowserDriver.hasInstance()) {
            Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(100)).takeScreenshot(BrowserDriver.getDriver(), element);
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ImageIO.write(fpScreenshot.getImage(), "png", bos);
                Allure.LIFECYCLE.fire(new MakeAttachmentEvent(bos.toByteArray(), message + " at " + getTimeStamp(), "Image/png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
