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
package org.eclipse.kapua.commons.event.bus;

import org.eclipse.kapua.service.event.EventBusListener;
import org.eclipse.kapua.service.event.KapuaEvent;

/**
 * @since 0.3.0
 */
public interface EventBus {

    public void publish(String address, KapuaEvent event) throws EventBusException;

    public void subscribe(String address, EventBusListener eventListener) throws EventBusException;
}
