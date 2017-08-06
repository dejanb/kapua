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
package org.eclipse.kapua.service.event;

import javax.xml.bind.annotation.XmlRegistry;

import org.eclipse.kapua.locator.KapuaLocator;

@XmlRegistry
public class KapuaEventXmlRegistry {


    private final KapuaLocator locator = KapuaLocator.getInstance();
    private final KapuaEventFactory kapuaEventFactory = locator.getFactory(KapuaEventFactory.class);

    /**
     * Creates a new kapuaEvent instance
     * 
     * @return
     */
    public KapuaEvent newKapuaEvent() {
        return kapuaEventFactory.newEntity(null);
    }

    /**
     * Creates a new kapuaEvent creator instance
     * 
     * @return
     */
    public KapuaEventCreator newKapuaEventCreator() {
        return kapuaEventFactory.newCreator(null);
    }

    /**
     * Creates a new kapuaEvent list result instance
     * 
     * @return
     */
    public KapuaEventListResult newKapuaEventListResult() {
        return kapuaEventFactory.newListResult();
    }

    public KapuaEventQuery newQuery() {
        return kapuaEventFactory.newQuery(null);
    }

}