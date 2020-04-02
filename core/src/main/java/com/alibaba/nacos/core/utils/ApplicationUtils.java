/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.alibaba.nacos.common.utils.IoUtils;
import com.sun.management.OperatingSystemMXBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.Resource;

import static com.alibaba.nacos.core.utils.Constants.FUNCTION_MODE_PROPERTY_NAME;
import static com.alibaba.nacos.core.utils.Constants.STANDALONE_MODE_PROPERTY_NAME;
import static org.apache.commons.lang3.CharEncoding.UTF_8;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class ApplicationUtils implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static ApplicationContext applicationContext;
    private static ConfigurableEnvironment environment;

    public static String getId() {
        return applicationContext.getId();
    }

    public static String getApplicationName() {
        return applicationContext.getApplicationName();
    }

    public static String getDisplayName() {
        return applicationContext.getDisplayName();
    }

    public static long getStartupDate() {
        return applicationContext.getStartupDate();
    }

    public static ApplicationContext getParent() {
        return applicationContext.getParent();
    }

    public static AutowireCapableBeanFactory getAutowireCapableBeanFactory()
            throws IllegalStateException {
        return applicationContext.getAutowireCapableBeanFactory();
    }

    public static BeanFactory getParentBeanFactory() {
        return applicationContext.getParentBeanFactory();
    }

    public static boolean containsLocalBean(String name) {
        return applicationContext.containsLocalBean(name);
    }

    public static boolean containsBeanDefinition(String beanName) {
        return applicationContext.containsLocalBean(beanName);
    }

    public static int getBeanDefinitionCount() {
        return applicationContext.getBeanDefinitionCount();
    }

    public static String[] getBeanDefinitionNames() {
        return applicationContext.getBeanDefinitionNames();
    }

    public static String[] getBeanNamesForType(ResolvableType type) {
        return applicationContext.getBeanNamesForType(type);
    }

    public static String[] getBeanNamesForType(Class<?> type) {
        return applicationContext.getBeanNamesForType(type);
    }

    public static String[] getBeanNamesForType(Class<?> type,
                                               boolean includeNonSingletons, boolean allowEagerInit) {
        return applicationContext.getBeanNamesForType(type, includeNonSingletons,
                allowEagerInit);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
        return applicationContext.getBeansOfType(type);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> type,
                                                    boolean includeNonSingletons, boolean allowEagerInit) throws BeansException {
        return applicationContext.getBeansOfType(type, includeNonSingletons,
                allowEagerInit);
    }

    public static String[] getBeanNamesForAnnotation(
            Class<? extends Annotation> annotationType) {
        return applicationContext.getBeanNamesForAnnotation(annotationType);
    }

    public static Map<String, Object> getBeansWithAnnotation(
            Class<? extends Annotation> annotationType) throws BeansException {
        return applicationContext.getBeansWithAnnotation(annotationType);
    }

    public static <A extends Annotation> A findAnnotationOnBean(String beanName,
                                                                Class<A> annotationType) throws NoSuchBeanDefinitionException {
        return applicationContext.findAnnotationOnBean(beanName, annotationType);
    }

    public static Object getBean(String name) throws BeansException {
        return applicationContext.getBean(name);
    }

    public static <T> T getBean(String name, Class<T> requiredType)
            throws BeansException {
        return applicationContext.getBean(name, requiredType);
    }

    public static Object getBean(String name, Object... args) throws BeansException {
        return applicationContext.getBean(name, args);
    }

    public static <T> T getBean(Class<T> requiredType) throws BeansException {
        return applicationContext.getBean(requiredType);
    }

    public static <T> T getBean(Class<T> requiredType, Object... args)
            throws BeansException {
        return applicationContext.getBean(requiredType, args);
    }

    public static <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType) {
        return applicationContext.getBeanProvider(requiredType);
    }

    public static <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType) {
        return applicationContext.getBeanProvider(requiredType);
    }

    public static boolean containsBean(String name) {
        return applicationContext.containsBean(name);
    }

    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return applicationContext.isSingleton(name);
    }

    public static boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
        return applicationContext.isPrototype(name);
    }

    public static boolean isTypeMatch(String name, ResolvableType typeToMatch)
            throws NoSuchBeanDefinitionException {
        return applicationContext.isTypeMatch(name, typeToMatch);
    }

    public static boolean isTypeMatch(String name, Class<?> typeToMatch)
            throws NoSuchBeanDefinitionException {
        return applicationContext.isTypeMatch(name, typeToMatch);
    }

    public static Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        return applicationContext.getType(name);
    }

    public static String[] getAliases(String name) {
        return applicationContext.getAliases(name);
    }

    public static void publishEvent(Object event) {
        applicationContext.publishEvent(event);
    }

    public static String getMessage(String code, Object[] args, String defaultMessage,
                                    Locale locale) {
        return applicationContext.getMessage(code, args, defaultMessage, locale);
    }

    public static String getMessage(String code, Object[] args, Locale locale)
            throws NoSuchMessageException {
        return applicationContext.getMessage(code, args, locale);
    }

    public static String getMessage(MessageSourceResolvable resolvable, Locale locale)
            throws NoSuchMessageException {
        return applicationContext.getMessage(resolvable, locale);
    }

    public static Resource[] getResources(String locationPattern) throws IOException {
        return applicationContext.getResources(locationPattern);
    }

    public static Resource getResource(String location) {
        return applicationContext.getResource(location);
    }

    public static ClassLoader getClassLoader() {
        return applicationContext.getClassLoader();
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static ConfigurableEnvironment getEnvironment() {
        return environment;
    }

    public static String[] getActiveProfiles() {
        return environment.getActiveProfiles();
    }

    public static String[] getDefaultProfiles() {
        return environment.getDefaultProfiles();
    }

    public static boolean acceptsProfiles(String... strings) {
        return environment.acceptsProfiles(strings);
    }

    public static boolean acceptsProfiles(Profiles profiles) {
        return environment.acceptsProfiles(profiles);
    }

    public static boolean containsProperty(String key) {
        return environment.containsProperty(key);
    }

    public static String getProperty(String key) {
        return environment.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return environment.getProperty(key, defaultValue);
    }

    public static <T> T getProperty(String key, Class<T> targetType) {
        return environment.getProperty(key, targetType);
    }

    public static <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        return environment.getProperty(key, targetType, defaultValue);
    }

    public static String getRequiredProperty(String key) throws IllegalStateException {
        return environment.getRequiredProperty(key);
    }

    public static <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
        return environment.getRequiredProperty(key, targetType);
    }

    public static String resolvePlaceholders(String text) {
        return environment.resolvePlaceholders(text);
    }

    public static String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
        return environment.resolveRequiredPlaceholders(text);
    }

    public static final String STANDALONE_MODE_ALONE = "standalone";
    public static final String STANDALONE_MODE_CLUSTER = "cluster";

    public static final String FUNCTION_MODE_CONFIG = "config";
    public static final String FUNCTION_MODE_NAMING = "naming";

    /**
     * The key of nacos home.
     */
    public static final String NACOS_HOME_KEY = "nacos.home";

    /**
     * nacos local ip
     */
    public static final String LOCAL_IP = InetUtils.getSelfIp();

    private static Boolean isStandalone = null;

    private static String functionModeType = null;

    /**
     * Standalone mode or not
     */
    public static boolean getStandaloneMode() {
        if (Objects.isNull(isStandalone)) {
            isStandalone = Boolean.getBoolean(STANDALONE_MODE_PROPERTY_NAME);
        }
        return isStandalone;
    }

    /**
     * server
     */
    public static String getFunctionMode() {
        if (StringUtils.isEmpty(functionModeType)) {
            functionModeType = System.getProperty(FUNCTION_MODE_PROPERTY_NAME);
        }
        return functionModeType;
    }

    public static String getNacosHome() {
        String nacosHome = System.getProperty(NACOS_HOME_KEY);
        if (StringUtils.isBlank(nacosHome)) {
            nacosHome = Paths.get(System.getProperty("user.home"), "nacos").toString();
        }
        return nacosHome;
    }

    private static OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory
            .getOperatingSystemMXBean();

    public static List<String> getIPsBySystemEnv(String key) {
        String env = getSystemEnv(key);
        List<String> ips = new ArrayList<>();
        if (StringUtils.isNotEmpty(env)) {
            ips = Arrays.asList(env.split(","));
        }
        return ips;
    }

    public static String getSystemEnv(String key) {
        return System.getenv(key);
    }

    public static float getLoad() {
        return (float) operatingSystemMXBean.getSystemLoadAverage();
    }

    public static float getCPU() {
        return (float) operatingSystemMXBean.getSystemCpuLoad();
    }

    public static float getMem() {
        return (float) (1 - (double) operatingSystemMXBean.getFreePhysicalMemorySize() / (double) operatingSystemMXBean
                .getTotalPhysicalMemorySize());
    }

    public static String getConfFilePath() {
        return Paths.get(getNacosHome(), "conf").toString();
    }

    private static String getClusterConfFilePath() {
        return Paths.get(getNacosHome(), "conf", "cluster.conf").toString();
    }

    public static List<String> readClusterConf() throws IOException {
        try (Reader reader = new InputStreamReader(new FileInputStream(new File(getClusterConfFilePath())),
                StandardCharsets.UTF_8)) {
            return analyzeClusterConf(reader);
        }
    }

    public static List<String> analyzeClusterConf(Reader reader) throws IOException {
        List<String> instanceList = new ArrayList<String>();
        List<String> lines = IoUtils.readLines(reader);
        String comment = "#";
        for (String line : lines) {
            String instance = line.trim();
            if (instance.startsWith(comment)) {
                // # it is ip
                continue;
            }
            if (instance.contains(comment)) {
                // 192.168.71.52:8848 # Instance A
                instance = instance.substring(0, instance.indexOf(comment));
                instance = instance.trim();
            }
            int multiIndex = instance.indexOf(Constants.COMMA_DIVISION);
            if (multiIndex > 0) {
                // support the format: ip1:port,ip2:port  # multi inline
                instanceList.addAll(Arrays.asList(instance.split(Constants.COMMA_DIVISION)));
            } else {
                //support the format: 192.168.71.52:8848
                instanceList.add(instance);
            }
        }
        return instanceList;
    }

    public static void writeClusterConf(String content) throws IOException {
        IoUtils.writeStringToFile(new File(getClusterConfFilePath()), content, UTF_8);
    }

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        applicationContext = context;
        environment = context.getEnvironment();
    }

    public static void injectEnvironment(ConfigurableEnvironment environment) {
        ApplicationUtils.environment = environment;
    }
}

