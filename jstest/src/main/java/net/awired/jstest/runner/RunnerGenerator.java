package net.awired.jstest.runner;

import java.io.InputStream;
import java.io.InputStreamReader;
import net.awired.jscoverage.instrumentation.JsInstrumentor;
import net.awired.jstest.resource.ResourceResolver;
import net.awired.jstest.server.RunnerResourceHandler;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import com.google.common.io.CharStreams;

public class RunnerGenerator {

    private HtmlResourceTranformer htmlResourceTranformer = new HtmlResourceTranformer();

    public String generate(AmdRunnerType runnerType, TestType testType, ResourceResolver resolver) {
        InputStream templateStream = getClass().getResourceAsStream(runnerType.getTemplate());
        if (templateStream == null) {
            throw new RuntimeException("Cannot found runner template : " + runnerType.getTemplate());
        }

        StringTemplate template;
        try {
            template = new StringTemplate(CharStreams.toString(new InputStreamReader(templateStream, "UTF-8")),
                    DefaultTemplateLexer.class);
        } catch (Exception e) {
            throw new RuntimeException("Cannot parse template " + runnerType.getTemplate(), e);
        }

        replaceTemplateVars(template, testType, resolver);
        return template.toString();
    }

    public void replaceTemplateVars(StringTemplate template, TestType testType, ResourceResolver resolver) {
        template.setAttribute("testResources", buildTestResources(testType, resolver));
        template.setAttribute("sources", buildSources(resolver));
        template.setAttribute("tests", buildTests(resolver));
    }

    private String buildTestResources(TestType testType, ResourceResolver resolver) {
        StringBuilder res = new StringBuilder();
        for (String resource : testType.getTesterResources()) {
            appendSourceTag(res, resolver.getResource(ResourceResolver.SRC_RESOURCE_PREFIX + resource));
        }
        appendSourceTag(
                res,
                getClass().getResourceAsStream(
                        RunnerResourceHandler.RUNNER_RESOURCE_PATH + testType.getTesterManager()),
                testType.getTesterManager());
        appendSourceTag(res, getClass().getResourceAsStream(JsInstrumentor.JSCOV_FILE), JsInstrumentor.JSCOV_FILE);
        return res.toString();
    }

}