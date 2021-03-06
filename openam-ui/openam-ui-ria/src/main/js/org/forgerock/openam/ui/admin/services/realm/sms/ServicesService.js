/**
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

define("org/forgerock/openam/ui/admin/services/realm/sms/ServicesService", [
    "jquery",
    "lodash",
    "org/forgerock/commons/ui/common/main/AbstractDelegate",
    "org/forgerock/commons/ui/common/util/Constants",
    "org/forgerock/openam/ui/admin/services/SMSServiceUtils",
    "org/forgerock/openam/ui/common/models/JSONSchema",
    "org/forgerock/openam/ui/common/models/JSONValues",
    "org/forgerock/openam/ui/common/util/Promise",
    "org/forgerock/openam/ui/common/util/RealmHelper"
], ($, _, AbstractDelegate, Constants, SMSServiceUtils, JSONSchema, JSONValues, Promise, RealmHelper) => {
    /**
     * @exports org/forgerock/openam/ui/admin/services/realm/sms/ServicesService
     */
    const obj = new AbstractDelegate(`${Constants.host}/${Constants.context}/json`);
    const scopedByRealm = function (realm, path) {
        let encodedRealm = "";

        if (realm !== "/") {
            encodedRealm = RealmHelper.encodeRealm(realm);
        }

        return `${encodedRealm}/realm-config/${path}`;
    };
    const getServiceSchema = function (realm, type) {
        return obj.serviceCall({
            url: scopedByRealm(realm, `services/${type}?_action=schema`),
            headers: { "Accept-API-Version": "protocol=1.0,resource=1.0" },
            type: "POST"
        });
    };
    const getServiceSubSchema = function (realm, serviceType, subSchemaType) {
        return obj.serviceCall({
            url: scopedByRealm(realm, `services/${serviceType}/${subSchemaType}?_action=schema`),
            headers: { "Accept-API-Version": "protocol=1.0,resource=1.0" },
            type: "POST"
        }).then((data) => SMSServiceUtils.sanitizeSchema(data));
    };

    obj.instance = {
        getAll (realm) {
            return obj.serviceCall({
                url: scopedByRealm(realm, "services?_queryFilter=true"),
                headers: { "Accept-API-Version": "protocol=1.0,resource=1.0" }
            }).then((response) => _.sortBy(response.result, "name"));
        },
        get (realm, type) {
            function getInstance () {
                return obj.serviceCall({
                    url: scopedByRealm(realm, `services/${type}`),
                    headers: { "Accept-API-Version": "protocol=1.0,resource=1.0" }
                });
            }

            function getName () {
                return obj.serviceCall({
                    url: scopedByRealm(realm, "services?_action=getAllTypes"),
                    headers: { "Accept-API-Version": "protocol=1.0,resource=1.0" },
                    type: "POST"
                }).then((all) => _.findWhere(all.result, { "_id": type }).name);
            }


            function getSubSchemaTypes () {
                return obj.serviceCall({
                    url: scopedByRealm(realm, `services/${type}?_action=getAllTypes`),
                    headers: { "Accept-API-Version": "protocol=1.0,resource=1.0" },
                    type: "POST"
                });
            }

            return Promise.all([getServiceSchema(realm, type), getInstance(), getName(), getSubSchemaTypes()])
                .then((data) => ({
                    schema: SMSServiceUtils.sanitizeSchema(data[0][0]),
                    values: data[1][0],
                    name:  data[2],
                    subSchemaTypes: data[3][0].result
                }));
        },
        getInitialState (realm, type) {
            function getTemplate () {
                return obj.serviceCall({
                    url: scopedByRealm(realm, `services/${type}?_action=template`),
                    headers: { "Accept-API-Version": "protocol=1.0,resource=1.0" },
                    type: "POST"
                });
            }

            return Promise.all([getServiceSchema(realm, type), getTemplate()]).then((response) => ({
                schema: new JSONSchema(response[0][0]),
                values: new JSONValues(response[1][0])
            }));
        },
        remove (realm, types) {
            if (!_.isArray(types)) {
                types = [types];
            }

            const promises = _.map(types, (type) => {
                obj.serviceCall({
                    url: scopedByRealm(realm, `services/${type}`),
                    headers: { "Accept-API-Version": "protocol=1.0,resource=1.0" },
                    type: "DELETE"
                });
            });

            return Promise.all(promises);
        },
        update (realm, type, data) {
            return obj.serviceCall({
                url: scopedByRealm(realm, `services/${type}`),
                headers: { "Accept-API-Version": "protocol=1.0,resource=1.0" },
                type: "PUT",
                data: JSON.stringify(data)
            });
        },
        create (realm, type, data) {
            return obj.serviceCall({
                url: scopedByRealm(realm, `services/${type}?_action=create`),
                headers: { "Accept-API-Version": "protocol=1.0,resource=1.0" },
                type: "POST",
                data: JSON.stringify(data)
            });
        }
    };

    obj.type = {
        getCreatables (realm) {
            const creatableTypes = obj.serviceCall({
                url: scopedByRealm(realm, "services?_action=getCreatableTypes"),
                headers: { "Accept-API-Version": "protocol=1.0,resource=1.0" },
                type: "POST"
            });

            return creatableTypes.then((response) => _.sortBy(response.result, "name"));
        },
        subSchema: {
            type: {
                getCreatables (realm, serviceType) {
                    return obj.serviceCall({
                        url: scopedByRealm(realm, `services/${serviceType}?_action=getCreatableTypes`),
                        headers: { "Accept-API-Version": "protocol=1.0,resource=1.0" },
                        type: "POST"
                    }).then((response) => _.sortBy(response.result, "name"));
                }
            },
            instance: {
                getAll (realm, serviceType) {
                    return obj.serviceCall({
                        url: scopedByRealm(realm, `services/${serviceType}?_action=nextdescendents`),
                        headers: { "Accept-API-Version": "protocol=1.0,resource=1.0" },
                        type: "POST"
                    }).then((response) => _.sortBy(response.result, "_id"));
                },
                get (realm, serviceType, subSchemaType, subSchemaInstance) {
                    function getInstance () {
                        return obj.serviceCall({
                            url: scopedByRealm(realm, `services/${serviceType}/${subSchemaType}/${subSchemaInstance}`),
                            headers: { "Accept-API-Version": "protocol=1.0,resource=1.0" }
                        });
                    }

                    return $.when(getServiceSubSchema(realm, serviceType, subSchemaType), getInstance())
                        .then((subSchema, values) => ({ schema: subSchema, values: values[0] }));
                },

                getInitialState (realm, serviceType, subSchemaType) {
                    function getTemplate (serviceType, subSchemaType) {
                        return obj.serviceCall({
                            url: scopedByRealm(realm, `services/${serviceType}/${subSchemaType}?_action=template`),
                            headers: { "Accept-API-Version": "protocol=1.0,resource=1.0" },
                            type: "POST"
                        });
                    }

                    return $.when(
                        getServiceSubSchema(realm, serviceType, subSchemaType),
                        getTemplate(serviceType, subSchemaType)
                    ).then((subSchema, values) => ({ subSchema, values: values[0] }));
                },

                remove (realm, serviceType, subSchemaType, subSchemaInstance) {
                    return obj.serviceCall({
                        url: scopedByRealm(realm, `services/${serviceType}/${subSchemaType}/${subSchemaInstance}`),
                        headers: { "Accept-API-Version": "protocol=1.0,resource=1.0" },
                        type: "DELETE"
                    });
                },

                update (realm, serviceType, subSchemaType, subSchemaInstance, data) {
                    return obj.serviceCall({
                        url: scopedByRealm(realm, `services/${serviceType}/${subSchemaType}/${subSchemaInstance}`),
                        headers: { "Accept-API-Version": "protocol=1.0,resource=1.0" },
                        type: "PUT",
                        data: JSON.stringify(data)
                    });
                },

                create (realm, serviceType, subSchemaType, data) {
                    return obj.serviceCall({
                        url: scopedByRealm(realm, `services/${serviceType}/${subSchemaType}?_action=create`),
                        headers: { "Accept-API-Version": "protocol=1.0,resource=1.0" },
                        type: "POST",
                        data: JSON.stringify(data)
                    });
                }
            }
        }
    };

    return obj;
});
