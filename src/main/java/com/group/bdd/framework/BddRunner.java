package com.group.bdd.framework;

import com.github.valfirst.jbehave.junit.monitoring.JUnitReportingRunner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.failures.PassingUponPendingStep;
import org.jbehave.core.failures.RethrowingFailure;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.AbsolutePathCalculator;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.reporters.FreemarkerViewGenerator;
import org.jbehave.core.reporters.PrintStreamStepdocReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.*;
import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

@RunWith(JUnitReportingRunner.class)
public abstract class BddRunner extends JUnitStories {

    private Configuration configuration;
    static String packageName = ConfigLoader.config().getString("bdd.steps.package");
    
    private int threads = Integer.parseInt(ConfigLoader.config().getString("bdd.thread"));

	private static final Logger LOG = LogManager.getLogger(BddRunner.class);
	
	@AfterClass
	public static void cleanUp(){
		AllureReporter.generateReport();
		//AllureReporter.copyTargetFolderToReportBackupFolder();
	}
	
    public BddRunner() {
        super();
        configuration = new Configuration(){
        };
        
        configuration.useFailureStrategy(new RethrowingFailure());
        configuration.useKeywords(new LocalizedKeywords(Locale.ENGLISH));
        configuration.usePathCalculator(new AbsolutePathCalculator());
        configuration.useParameterControls(new ParameterControls());
       
        /*configuration.useParameterConverters(new ParameterConverters()
				.addConverters(
						new DateConverter(new SimpleDateFormat(ConfigLoader.config().getString("bdd.date.format","dd-MM-yyyy"))),
						new MapParameterConverter()));
        configuration.useParanamer(new NullParanamer());*/
        configuration.usePendingStepStrategy(new PassingUponPendingStep());
        configuration.useStepCollector(new MarkUnmatchedStepsAsPending());
        configuration.useStepdocReporter(new PrintStreamStepdocReporter());
        configuration.useStepFinder(new StepFinder());
        configuration.useStepMonitor(new SilentStepMonitor());
//        configuration.useStepPatternParser(new RegexPrefixCapturingPatternParser());
        configuration.useStoryControls(new StoryControls());
//        configuration.useStoryParser(new RegexStoryParser(configuration.keywords()));
        configuration.useStoryPathResolver(new UnderscoredCamelCaseResolver());
        configuration.useViewGenerator(new FreemarkerViewGenerator());
        configuration.useStoryLoader(new LoadFromClasspath());
        configuration.storyControls().useStoryMetaPrefix("story_").useScenarioMetaPrefix("scenario_");        
        configuration.useStoryReporterBuilder(new StoryReporterBuilder().withReporters(new AllureReporter()));        
//        configuration.storyReporterBuilder().withMultiThreading(true).multiThreading();
        configuration.useStepPatternParser(new RegexPrefixCapturingPatternParser(
				ConfigLoader.config().getString("bdd.parameter.prefix","$"))); // use '%' instead of '$' to identify parameters
        
        Embedder embedder = configuredEmbedder();            
        embedder.useMetaFilters(asList(ConfigLoader.config().getString("bdd.metaFilter")));
                    
        ExecutorService executorService = Executors.newFixedThreadPool(threads);       
		embedder.useExecutorService(executorService);

        EmbedderControls embedderControls = configuredEmbedder().embedderControls();
        
        embedderControls.doBatch(true);
        embedderControls.doGenerateViewAfterStories(true);
        embedderControls.doIgnoreFailureInStories(false);
        embedderControls.doIgnoreFailureInView(false);
        embedderControls.doSkip(false);
        embedderControls.doVerboseFailures(false);
        embedderControls.doVerboseFiltering(false);
        embedderControls.useStoryTimeouts(ConfigLoader.config().getString("bdd.times.out"));
        embedderControls.threads();
        embedderControls.useThreads(1);
        
        JUnitReportingRunner.recommendedControls(embedder);                
    }

    @Override
    public Configuration configuration() {
        return configuration;
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
    	return new InstanceStepsFactory(configuration(), discoverSteps(packageName));
    }
    private static final List stepClasses = Lists.newArrayList(); 
    private List<?> discoverSteps(String packageName){
    	if (stepClasses.isEmpty()){

			Reflections reflections = new Reflections(packageName);
			Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(StorySteps.class);
			for (Class<?> logic : annotated){
				try {
					LOG.info("packageName" + packageName);
					LOG.info("Found steps class { " + logic + " }");
					stepClasses.add(logic.newInstance());
				} catch (Exception e) {
					LOG.error("Injecting logic for { " + logic + " } failed due to { "+ e +" }");
				}
			}
		}
		return stepClasses;
	}

	protected List<String> getFeatures() {
		Embedder embedder = configuredEmbedder();
		embedder.useMetaFilters(asList(ConfigLoader.config().getString("bdd.metaFilter")));
		
		String story = ConfigLoader.config().getString("jbehave.story");
        List<String> stories = new StoryFinder().findPaths(codeLocationFromClass(codeLocation()),
                "**/features/**" + story, "");
        
        LOG.info("stories" + stories);
		return stories;
	}

	private List<String> asList(String testing) {
		List<String> lst = Arrays.asList(testing.split(";"));
		return lst;
	}

	private Class<?> codeLocation(){
		LOG.info("packageName" + packageName);
		if (discoverSteps(packageName).isEmpty()){
			return getCallerClass();
		} else {
			return discoverSteps(packageName).get(0).getClass();
		}
	}

	public static Class<?> getCallerClass()  {
		StackTraceElement[] st = Thread.currentThread().getStackTrace();
		StackTraceElement rawFQN = Iterables.find(Lists.newArrayList(st), new Predicate<StackTraceElement>() {

			@Override
			public boolean apply(StackTraceElement input) {
				return ! (input.toString().contains("internal") ||
						input.toString().startsWith("java.lang")
				);
			}
		});
		try {
			String fqn = rawFQN.toString().split("\\(")[0].split("<")[0];
			return Class.forName(fqn.substring(0, fqn.lastIndexOf('.')));
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
}