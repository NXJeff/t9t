package com.arvatosystems.t9t.jdp;

import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.dp.Startup;
import de.jpaw.util.ExceptionUtil;

@Startup(101)
public class ShowVersions {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShowVersions.class);
    private static final String UNKNOWN      = "UNKNOWN";
    private static String xarName            = UNKNOWN; // WAR or JAR name
    private static String applicationVersion = UNKNOWN; // version (tag or SNAPSHOT)
    private static String buildNumber        = UNKNOWN; // Jenkins build number
    private static String scmRevision        = UNKNOWN; // git commit tag

    // started by Jdp initialization
    public static void onStartup() {
        try {
            // just an info thing - don't let the application fail if anything does not work in here
            InputStream inputStream = Init.class.getResourceAsStream("/META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest(inputStream);
            Attributes attributes = manifest.getMainAttributes();
            xarName            = attributes.getValue("Implementation-Title");
            applicationVersion = attributes.getValue("Implementation-Version");
            buildNumber        = attributes.getValue("Implementation-Build-Number");
            scmRevision        = attributes.getValue("Implementation-SCM-Revision");

            LOGGER.info("Running {} of version {}, created by Jenkins build {}, last commit = {}",
                xarName, applicationVersion, buildNumber, scmRevision);
        } catch (Exception e) {
            LOGGER.error("Exception during revision check: {}", ExceptionUtil.causeChain(e));
        }
    }

    // boilerplate below...
    public static String getApplicationVersion() {
        return applicationVersion;
    }

    public static String getScmRevision() {
        return scmRevision;
    }

    public static String getBuildNumber() {
        return buildNumber;
    }

    public static String getXarName() {
        return xarName;
    }
}