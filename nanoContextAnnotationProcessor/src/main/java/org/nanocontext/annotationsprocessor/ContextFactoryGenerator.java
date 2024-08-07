package org.nanocontext.annotationsprocessor;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.IOException;
import java.io.Writer;

/**
 *
 */
public class ContextFactoryGenerator {
    private final Configuration cfg;

    public ContextFactoryGenerator() throws IOException {
        cfg = new Configuration();

        cfg.setClassLoaderForTemplateLoading(this.getClass().getClassLoader(), "");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
    }

    public void writeContextFactory(
            final ContextFactoryDataModel contextDataModel,
            final Writer moduleWriter)
            throws IOException, TemplateException {
        if (contextDataModel != null) {
            Template template = cfg.getTemplate("ContextFactory.ftl");
            template.process(contextDataModel, moduleWriter);
        }
    }
}
