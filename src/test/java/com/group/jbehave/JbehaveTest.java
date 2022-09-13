package com.group.jbehave;

import com.group.bdd.framework.BddRunner;
import com.group.bdd.framework.SequenceBddRunner;
import org.apache.log4j.Logger;

import java.util.List;

import static com.group.bdd.framework.ConfigLoader.config;

public class JbehaveTest extends BddRunner {
    final static Logger LOG = Logger.getLogger(JbehaveTest.class);
    SequenceBddRunner sequenceStory = new SequenceBddRunner();

    @Override
    protected List<String> storyPaths() {
        return getFeatures();
    }

    public JbehaveTest() throws Throwable {
        setLogs();

        String seqMeta = config().getString("bdd.seqMetaFilter");
        if (!seqMeta.equals("")) {
            LOG.info("Testing=" + config().getString("bdd.seqMetaFilter"));
            sequenceStory.run();
        }
    }

    public void setLogs() {
        Logger.getLogger("org.openqa.selenium").setLevel(org.apache.log4j.Level.OFF);
        Logger.getLogger("org.apache.http.headers").setLevel(org.apache.log4j.Level.OFF);
        Logger.getLogger("org.apache.http.wire").setLevel(org.apache.log4j.Level.OFF);
        Logger.getLogger("httpclient.wire.content").setLevel(org.apache.log4j.Level.OFF);

        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "ERROR");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "ERROR");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "ERROR");
    }

}