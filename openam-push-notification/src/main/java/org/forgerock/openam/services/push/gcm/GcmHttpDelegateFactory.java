/*
* The contents of this file are subject to the terms of the Common Development and
* Distribution License (the License). You may not use this file except in compliance with the
* License.
*
* You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
* specific language governing permission and limitations under the License.
*
* When distributing Covered Software, include this CDDL Header Notice in each file and include
* the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
* Header, with the fields enclosed by brackets [] replaced by your own identifying
* information: "Portions copyright [year] [name of copyright owner]".
*
* Copyright 2016 ForgeRock AS.
*/
package org.forgerock.openam.services.push.gcm;

import com.google.inject.Key;
import com.google.inject.name.Names;
import com.sun.identity.shared.debug.Debug;
import org.forgerock.guice.core.InjectorHolder;
import org.forgerock.http.HttpApplicationException;
import org.forgerock.http.handler.HttpClientHandler;
import org.forgerock.json.resource.Router;
import org.forgerock.openam.services.push.PushNotificationDelegateFactory;
import org.forgerock.openam.services.push.PushNotificationException;
import org.forgerock.openam.services.push.PushNotificationServiceConfig;
import org.forgerock.util.Options;

/**
 * Produces GcmHttpDelegates matching the PushNotificationServiceFactory interface.
 */
public class GcmHttpDelegateFactory implements PushNotificationDelegateFactory {

    private final static Key<Router> KEY = Key.get(Router.class, Names.named("CrestRealmRouter"));

    private final Debug debug;
    private final GcmMessageResource messageResource;
    private final Router router;

    /**
     * Default constructor sets the debug so passing into produced delegates.
     */
    public GcmHttpDelegateFactory() {
        debug = Debug.getInstance("frPush");
        messageResource = InjectorHolder.getInstance(GcmMessageResource.class);
        router = InjectorHolder.getInstance(KEY);
    }

    @Override
    public GcmHttpDelegate produceDelegateFor(PushNotificationServiceConfig config) throws PushNotificationException {

        HttpClientHandler handler;
        Options options = Options.defaultOptions();

        try {
            handler = new HttpClientHandler(options);
        } catch (HttpApplicationException e) {
            throw new PushNotificationException("Unable to generate HTTP client for the GcmHttpDelegate.", e);
        }

        return new GcmHttpDelegate(handler, config, debug, router, messageResource);
    }

}
