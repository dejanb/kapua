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
package org.eclipse.kapua.commons.core;

import java.util.List;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.event.bus.EventBusBundle;
import org.eclipse.kapua.commons.locator.ComponentLocator;
import org.eclipse.kapua.locator.KapuaLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container hosts a Kapua application and provides the means to configure it
 * and manage its lifecycle.
 * 
 * @since 0.3.0
 */
public abstract class Container implements ContainerContext {

    private final static Logger logger = LoggerFactory.getLogger(Container.class);

    private ContainerConfig configuration;

    public Container() {
        this(new ContainerConfig());
    }

    public Container(ContainerConfig configuration) {
        this.configuration = configuration;
        // Init the locator
        ComponentLocator.getInstance();
        KapuaLocator.getInstance();
    }

    public final void startup() throws KapuaException {
        logger.info("Startup...");
        if (getComponentLocator().hasBinding(ServiceBundleProvider.class)) {
            ServiceBundleProvider bundleProvider = getComponentLocator().getComponent(ServiceBundleProvider.class);
            
            for (ServiceBundle service : bundleProvider.getBundles()) {
                if (service instanceof EventBusBundle) {
                    EventBusBundle eventBusBundle = (EventBusBundle) service;
                    eventBusBundle.start();
                }
            }
            
            for (ServiceBundle service : bundleProvider.getBundles()) {
                if (!(service instanceof EventBusBundle)) {
                    service.start();
                }
            }
        }

        List<LifecyleListener> listeners = configuration.getImplementationsOf(LifecyleListener.class);
        for (LifecyleListener listener : listeners) {
            listener.onStartup(this);
        }
        logger.info("Startup...DONE");
    }

    public final ComponentLocator getComponentLocator() {
        return ComponentLocator.getInstance();
    }

    public final void shutdown() throws KapuaException {
        logger.info("Shutdown...");
        List<LifecyleListener> listeners = configuration.getImplementationsOf(LifecyleListener.class);
        for (int i = listeners.size() - 1; i >= 0; i--) {
            listeners.get(i).onShutdown(this);
        }

        if (getComponentLocator().hasBinding(ServiceBundleProvider.class)) {
            ServiceBundleProvider bundleProvider = getComponentLocator().getComponent(ServiceBundleProvider.class);
            for (ServiceBundle service : bundleProvider.getBundles()) {
                service.stop();
            }
        }
        logger.info("Shutdown...DONEs");
    }
}
