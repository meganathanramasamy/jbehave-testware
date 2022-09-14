package com.group.jbehave;

import com.group.bdd.framework.BddRunner;
import com.group.bdd.framework.SequenceBddRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static com.group.bdd.framework.ConfigLoader.config;

public class JbehaveTest extends BddRunner {
    private static final Logger LOG = LogManager.getLogger(JbehaveTest.class);
    SequenceBddRunner sequenceStory = new SequenceBddRunner();

    @Override
    protected List<String> storyPaths() {
        return getFeatures();
    }

    public JbehaveTest() throws Throwable {
        String seqMeta = config().getString("bdd.seqMetaFilter");
        if (!seqMeta.equals("")) {
            LOG.info("Testing=" + config().getString("bdd.seqMetaFilter"));
            sequenceStory.run();
        }
    }
}