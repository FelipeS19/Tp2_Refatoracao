package org.sammancoaching;

import org.sammancoaching.dependencies.Config;
import org.sammancoaching.dependencies.Emailer;
import org.sammancoaching.dependencies.Logger;
import org.sammancoaching.dependencies.Project;

public class Pipeline {
    private final Config config;
    private final Emailer emailer;
    private final Logger log;

    public Pipeline(Config config, Emailer emailer, Logger log) {
        this.config = config;
        this.emailer = emailer;
        this.log = log;
    }

    public void run(Project project) {
        boolean testsPassed = runTests(project);
        boolean deploySuccessful = runDeployment(project, testsPassed);
        sendEmailIfNeeded(testsPassed, deploySuccessful);
    }

    private boolean runTests(Project project) {
        if (!project.hasTests()) {
            log.info("No tests");
            return true;
        }

        if ("success".equals(project.runTests())) {
            log.info("Tests passed");
            return true;
        } else {
            log.error("Tests failed");
            return false;
        }
    }

    private boolean runDeployment(Project project, boolean testsPassed) {
        if (!testsPassed) {
            return false;
        }

        if ("success".equals(project.deploy())) {
            log.info("Deployment successful");
            return true;
        } else {
            log.error("Deployment failed");
            return false;
        }
    }

    private void sendEmailIfNeeded(boolean testsPassed, boolean deploySuccessful) {
        if (!config.sendEmailSummary()) {
            log.info("Email disabled");
            return;
        }

        log.info("Sending email");

        if (!testsPassed) {
            emailer.send("Tests failed");
        } else if (deploySuccessful) {
            emailer.send("Deployment completed successfully");
        } else {
            emailer.send("Deployment failed");
        }
    }
}