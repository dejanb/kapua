/*******************************************************************************
 * Copyright (c) 2017 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.device.registry;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.core.ServiceModule;
import org.eclipse.kapua.commons.event.bus.EventBusManager;
import org.eclipse.kapua.commons.event.service.EventStoreHouseKeeperJob;
import org.eclipse.kapua.commons.event.service.internal.KapuaEventStoreServiceImpl;
import org.eclipse.kapua.commons.event.service.internal.ServiceMap;
import org.eclipse.kapua.commons.jpa.EntityManagerFactory;
import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.service.device.registry.connection.DeviceConnectionService;
import org.eclipse.kapua.service.device.registry.internal.DeviceEntityManagerFactory;
import org.eclipse.kapua.service.event.KapuaEventBus;
import org.eclipse.kapua.service.event.KapuaEventStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KapuaProvider
public class DeviceServiceModule implements ServiceModule {
    //TODO make sense to create an abstract module to handle the housekeeper executor start and stop?

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceServiceModule.class);

    private final static int MAX_WAIT_LOOP_ON_SHUTDOWN = 30;
    private final static long WAIT_TIME = 1000;

    @Inject
    private DeviceConnectionService deviceConnectionService;
    @Inject
    private DeviceRegistryService deviceRegistryService;

    private KapuaEventStoreService kapuaEventStoreService;

    private ScheduledExecutorService houseKeeperScheduler;
    private ScheduledFuture<?> houseKeeperHandler;
    private EventStoreHouseKeeperJob houseKeeperJob;

    @Override
    public void start() throws KapuaException {

        KapuaEventBus eventbus = EventBusManager.getInstance();

        // Listen to upstream service events

        String upEvAccountDeviceRegistryAddress = KapuaDeviceRegistrySettings.getInstance().getString(KapuaDeviceRegistrySettingKeys.ACCOUNT_DEVICE_REGISTRY_UPSTREAM_EVENT_ADDRESS);
        //the event bus implicitly will add event. as prefix for each publish/subscribe
        eventbus.subscribe(upEvAccountDeviceRegistryAddress, deviceRegistryService); 

        String upEvAccountDeviceConnectionAddress = KapuaDeviceRegistrySettings.getInstance().getString(KapuaDeviceRegistrySettingKeys.ACCOUNT_DEVICE_CONNECTION_UPSTREAM_EVENT_ADDRESS);
        //the event bus implicitly will add event. as prefix for each publish/subscribe
        eventbus.subscribe(upEvAccountDeviceConnectionAddress, deviceConnectionService); 

        String upEvAuthorizationDeviceRegistryAddress = KapuaDeviceRegistrySettings.getInstance().getString(KapuaDeviceRegistrySettingKeys.AUTHORIZATION_DEVICE_REGISTRY_UPSTREAM_EVENT_ADDRESS);
        //the event bus implicitly will add event. as prefix for each publish/subscribe
        eventbus.subscribe(upEvAuthorizationDeviceRegistryAddress, deviceRegistryService); 

        // Event store listener
        EntityManagerFactory entityManagerFactory = DeviceEntityManagerFactory.instance();
        kapuaEventStoreService = new KapuaEventStoreServiceImpl(entityManagerFactory);

        //the event bus implicitly will add event. as prefix for each publish/subscribe
        List<String> servicesNames = KapuaDeviceRegistrySettings.getInstance().getList(String.class, KapuaDeviceRegistrySettingKeys.DEVICE_SERVICES_NAMES);

        String serviceInternalEventAddress = KapuaDeviceRegistrySettings.getInstance().getString(KapuaDeviceRegistrySettingKeys.DEVICE_INTERNAL_EVENT_ADDRESS);
        eventbus.subscribe(String.format("%s.%s", serviceInternalEventAddress, serviceInternalEventAddress), kapuaEventStoreService);

        //register events to the RaiseKapuaEventInterceptor
        ServiceMap.registerServices(serviceInternalEventAddress, servicesNames);

        // Start the House keeper
        houseKeeperScheduler = Executors.newScheduledThreadPool(1);
        houseKeeperJob = new EventStoreHouseKeeperJob(entityManagerFactory, eventbus, serviceInternalEventAddress, servicesNames);
        // Start time can be made random from 0 to 30 seconds
        houseKeeperHandler = houseKeeperScheduler.scheduleAtFixedRate(houseKeeperJob, 30, 30, TimeUnit.SECONDS);
    }

    @Override
    public void stop() throws KapuaException {
        if (houseKeeperJob!=null) {
            houseKeeperJob.stop();
        }
        int waitLoop = 0;
        while(houseKeeperHandler.isDone()) {
            try {
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException e) {
                //do nothing
            }
            if (waitLoop++ > MAX_WAIT_LOOP_ON_SHUTDOWN) {
                LOGGER.warn("Cannot cancel the house keeper task afeter a while!");
                break;
            }
        }
        if (houseKeeperScheduler != null) {
            houseKeeperScheduler.shutdown();
        }
    }
}
