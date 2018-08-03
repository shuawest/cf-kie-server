package com.redhat.solutions.fsi.samples;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudException;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloud.service.common.MysqlServiceInfo;
import org.springframework.cloud.service.common.OracleServiceInfo;
import org.springframework.cloud.service.common.PostgresqlServiceInfo;
import org.springframework.cloud.service.common.SqlServerServiceInfo;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpringApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Log logger = LogFactory.getLog(SpringApplicationContextInitializer.class);

    private static final Map<Class<? extends ServiceInfo>, String> serviceTypeToProfileName = new HashMap<>();
    private static final List<String> validLocalProfiles =
            Arrays.asList("mysql", "postgres", "sqlserver", "oracle");

    static {
        serviceTypeToProfileName.put(PostgresqlServiceInfo.class, "postgres");
        serviceTypeToProfileName.put(MysqlServiceInfo.class, "mysql");
        serviceTypeToProfileName.put(OracleServiceInfo.class, "oracle");
        serviceTypeToProfileName.put(SqlServerServiceInfo.class, "sqlserver");
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        logger.info("Initializing cloud servicess");

        Cloud cloud = getCloud();

        ConfigurableEnvironment appEnvironment = applicationContext.getEnvironment();

        validateActiveProfiles(appEnvironment);

        addCloudProfile(cloud, appEnvironment);
    }

    private void addCloudProfile(Cloud cloud, ConfigurableEnvironment appEnvironment) {
        if (cloud == null) {
            return;
        }

        List<String> profiles = new ArrayList<>();

        List<ServiceInfo> serviceInfos = cloud.getServiceInfos();

        logger.info("Found serviceInfos: " + StringUtils.collectionToCommaDelimitedString(serviceInfos));

        for (ServiceInfo serviceInfo : serviceInfos) {
            if (serviceTypeToProfileName.containsKey(serviceInfo.getClass())) {
                profiles.add(serviceTypeToProfileName.get(serviceInfo.getClass()));
            }
        }

        if (profiles.size() > 1) {
            throw new IllegalStateException(
                    "Only one service of the following types may be bound to this application: " +
                            serviceTypeToProfileName.values().toString() + ". " +
                            "These services are bound to the application: [" +
                            StringUtils.collectionToCommaDelimitedString(profiles) + "]");
        }

        if (profiles.size() > 0) {
            appEnvironment.addActiveProfile(profiles.get(0));
            appEnvironment.addActiveProfile(profiles.get(0) + "-cloud");
        }
    }

    private Cloud getCloud() {
        try {
            CloudFactory cloudFactory = new CloudFactory();
            return cloudFactory.getCloud();
        } catch (CloudException ce) {
            return null;
        }
    }

    private void validateActiveProfiles(ConfigurableEnvironment appEnvironment) {
        List<String> serviceProfiles = Stream.of(appEnvironment.getActiveProfiles())
                .filter(validLocalProfiles::contains)
                .collect(Collectors.toList());

        if (serviceProfiles.size() > 1) {
            throw new IllegalStateException("Only one active Spring profile may be set among the following: " +
                    validLocalProfiles.toString() + ". " +
                    "These profiles are active: [" +
                    StringUtils.collectionToCommaDelimitedString(serviceProfiles) + "]");
        }
    }

}
