/*******************************************************************************
 * Copyright (c) 2011, 2017 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.commons.locator.guice;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.eclipse.kapua.KapuaErrorCodes;
import org.eclipse.kapua.KapuaRuntimeException;
import org.eclipse.kapua.commons.locator.ComponentLocator;
import org.eclipse.kapua.commons.util.ResourceUtils;
import org.eclipse.kapua.locator.guice.inject.InjectorRegistry;
import org.eclipse.kapua.locator.inject.LocatorConfig;
import org.eclipse.kapua.locator.inject.LocatorConfigurationException;
import org.eclipse.kapua.locator.inject.MessageListenersPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.PrivateModule;

/**
 * @since 0.3.0
 *
 */
public class ComponentLocatorImpl extends ComponentLocator {

    private static final Logger logger = LoggerFactory.getLogger(ComponentLocatorImpl.class);

    private static final String ROOT_INJECTOR_NAME = "rootLocatorInjector";
    private static final String COMMONS_INJECTOR_NAME = "commonsLocatorInjector";
    private static final String SERVICE_RESOURCE = "locator.xml";

    public ComponentLocatorImpl() {

        URL locatorConfigURL = null;
        try {

            Injector rootInjector = Guice.createInjector(new PrivateModule() {

                private MessageListenersPoolImpl messageListenersPool;

                @Override
                protected void configure() {

                    messageListenersPool = new MessageListenersPoolImpl();
                    bind(MessageListenersPool.class).toInstance(messageListenersPool);
                    expose(MessageListenersPool.class);
                }

            });

            InjectorRegistry.add(ROOT_INJECTOR_NAME, rootInjector);

            // Find locator configuration file
            List<URL> locatorConfigurations = Arrays.asList(ResourceUtils.getResource(SERVICE_RESOURCE));
            if (locatorConfigurations.isEmpty()) {
                return;
            }

            // Read configurations from resource files
            locatorConfigURL = locatorConfigurations.get(0);
            LocatorConfig locatorConfig;
            locatorConfig = LocatorConfig.fromURL(locatorConfigURL);

            MessageListenersPool msgComponentsPool = rootInjector.getInstance(MessageListenersPool.class);
            Injector injector = rootInjector.createChildInjector(new ComponentsModule(msgComponentsPool, locatorConfig));
            InjectorRegistry.add(COMMONS_INJECTOR_NAME, injector);
            logger.info("Created injector {}", COMMONS_INJECTOR_NAME);
        } catch (LocatorConfigurationException e) {
            throw new KapuaRuntimeException(KapuaErrorCodes.INTERNAL_ERROR, e, "Cannot load " + locatorConfigURL);
        } catch (ConfigurationException e) {
            throw new KapuaRuntimeException(KapuaErrorCodes.INTERNAL_ERROR, e, "Cannot load " + locatorConfigURL);
        } catch (Throwable e) {
            throw new KapuaRuntimeException(KapuaErrorCodes.INTERNAL_ERROR, e, "Cannot load " + locatorConfigURL);
        }
    }

    @Override
    public <T> boolean hasBinding(Class<T> clazz) {
        try {
            Injector injector = InjectorRegistry.get(COMMONS_INJECTOR_NAME);
            injector.getBinding(clazz);
            return true;
        } catch (ConfigurationException e) {
            return false;
        }
    }

    @Override
    public <T> T getComponent(Class<T> superOrImplClass) {
        try {
            Injector injector = InjectorRegistry.get(COMMONS_INJECTOR_NAME);
            return injector.getInstance(superOrImplClass);
        } catch (ConfigurationException e) {
            throw new KapuaRuntimeException(KapuaErrorCodes.INTERNAL_ERROR, e, "Cant get instance of " + superOrImplClass);
        }
    }
}
