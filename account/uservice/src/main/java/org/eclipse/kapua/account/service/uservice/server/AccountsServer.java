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
package org.eclipse.kapua.account.service.uservice.server;

import javax.xml.bind.JAXBContext;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.account.service.uservice.messaging.AccountsVertx;
import org.eclipse.kapua.account.service.uservice.rest.AccountsResource;
import org.eclipse.kapua.account.service.uservice.rest.JaxbContextResolver;
import org.eclipse.kapua.commons.rest.api.KapuaSerializableBodyReader;
import org.eclipse.kapua.commons.rest.api.KapuaSerializableBodyWriter;
import org.eclipse.kapua.commons.util.xml.JAXBContextProvider;
import org.eclipse.kapua.commons.util.xml.XmlUtil;
import org.jboss.resteasy.plugins.server.vertx.VertxRequestHandler;
import org.jboss.resteasy.plugins.server.vertx.VertxResteasyDeployment;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.HttpEndpoint;

public class AccountsServer extends AbstractVerticle {

    private ServiceDiscovery discovery;
    private VertxResteasyDeployment deployment;

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx(new VertxOptions());
        vertx.deployVerticle(new AccountsServer());
    }

    @Override
    public void start() {

        vertx.executeBlocking(future ->{

            final String host = System.getProperty("host", "localhost");
            final int port = Integer.parseInt(System.getProperty("port", "8080"));

            // Create discovery service
            discovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setBackendConfiguration(config()));

            deployment = createRestEndpoint(host, port);

            // Publish services on ServiceDiscovery
            publishRestEndpoint(host, port);
            publishEventBusService();

            future.complete();
        },
        res -> {
            if (res.succeeded()) {
                System.out.println("Startup succeeded!");
            } else {
                res.cause().printStackTrace();
            }
        });
    }

    @Override
    public void stop() {

        // Stop the deployment
        if (deployment != null) {
        }

        // Close discovery servuce
        if (discovery != null) {
            discovery.close();
        }
    }

    private VertxResteasyDeployment createRestEndpoint(final String host, final int port) {

        VertxResteasyDeployment deployment = new VertxResteasyDeployment();
        final JaxbContextResolver jaxbContextResolver = new JaxbContextResolver();

        deployment.start();
        deployment.getProviderFactory().register(jaxbContextResolver);
        deployment.getProviderFactory().register(new KapuaSerializableBodyWriter());
        deployment.getProviderFactory().register(new KapuaSerializableBodyReader());
        deployment.getRegistry().addPerInstanceResource(AccountsResource.class);

        XmlUtil.setContextProvider(new JAXBContextProvider() {

            @Override
            public JAXBContext getJAXBContext() throws KapuaException {
                return jaxbContextResolver.getContext(JAXBContext.class);
            }

        });

        // Start the front end server using the Jax-RS controller
        VertxRequestHandler vertxRequestHandler = new VertxRequestHandler(vertx, deployment);
        vertx.createHttpServer()
               .requestHandler(vertxRequestHandler)
               .listen(port,
                        ar -> {
                            if (ar.succeeded()) {
                                System.out.println("Server started on port "
                                        + ar.result().actualPort());
                            } else {
                                ar.cause().printStackTrace();
                            }
                        });

        return deployment;
    }

    private void publishRestEndpoint(final String host, final int port) { 
        Record record = HttpEndpoint.createRecord("kapua-account-api", host, port, "/");
        discovery.publish(record, ar -> {
            if (ar.succeeded()) {
                System.out.println("Service published : " + record.getName());
            } else {
                ar.cause().printStackTrace();
            }
        });
    }

    private void publishEventBusService() {

        //(Need Proxy generation) Register the service proxy on the event bus
        //AccountsVertx accountsService = new AccountsVertxImpl();
        //MessageConsumer<JsonObject> consumer = ProxyHelper.registerService(AccountsVertx.class, vertx, accountsService, AccountsVertx.ADDRESS);

        // Publish it in the discovery infrastructure
        Record record = EventBusService.createRecord("kapua-account-msg", AccountsVertx.ADDRESS, AccountsVertx.class);
        discovery.publish(record, ar -> {
            if (ar.succeeded()) {
                // publication succeeded
                System.out.println("Service published : " + record.getName());
            } else {
                ar.cause().printStackTrace();
            }
        });
    }
}
