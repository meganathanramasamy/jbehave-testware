package com.group.jbehave;

import com.group.bdd.framework.BddRunner;
import com.group.bdd.framework.SequenceBddRunner;

import java.util.List;

import static com.group.bdd.framework.ConfigLoader.config;

public class JbehaveTest extends BddRunner {
    SequenceBddRunner sequenceStory = new SequenceBddRunner();

    @Override
    protected List<String> storyPaths() {
        return getFeatures();
    }

    public JbehaveTest() throws Throwable {
        if (!config().getString("bdd.seqMetaFilter").equals("")) {
            sequenceStory.run();
        }
    }
}