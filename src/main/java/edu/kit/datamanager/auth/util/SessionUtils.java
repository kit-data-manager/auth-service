/*
 * Copyright 2017 Karlsruhe Institute of Technology.
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
package edu.kit.datamanager.auth.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class SessionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionUtils.class);

    /**
     * Helper method to obtain a session from a request. The method tries to
     * obtain a sessionId either from the 'Session-Id' header or from a cookie
     * with the key 'sessionId'. Afterwards, the session for the provided id is
     * obtained from a local database. If obtaining the sessionId or session
     * fails or returns no value, this method may return 'null' if the flag
     * 'allowNullResult' is set TRUE, or throws a WebApplicationException,
     * either with HTTP status BAD_REQUEST (if no sessionId was found) or
     * NOT_FOUND (if an unknows session id was provided).
     *
     * This method is intended to be used only by the auth service for helping
     * while obtaining session, e.g. for GET, PATCH or DELETE operations.
     *
     * @param resource The resource providing access to session information.
     * @param allowNullResult If TRUE, 'null' is allowed to be returned if no
     * session(Id) was found. Otherwise, a runtime exception is thrown.
     *
     * @return The session for the provided sessionId or 'null', if returning
     * 'null' is allowed.
     *
     * @throws ServiceException If the session could not be obtained.
     */
//    public static Session getSessionFromRequest(AbstractBaseResource<? extends Session> resource, boolean allowNullResult) throws ServiceException {
//        LOGGER.trace("Trying to find session in headers.");
//        String sid = ServiceUtil.getSessionIdFromResource(resource);
//        if (sid == null) {
//            if (allowNullResult) {
//                //this call is expected to return 'null' as it is used during POST. This, return 'null' instead of throwing an exception.
//                LOGGER.trace("No session identifier found in header or cookie. 'allowNullResult' is set {}. Returning 'null'.", allowNullResult);
//                return null;
//            }
//            LOGGER.error("No session identifier found in header or cookie. 'allowNullResult' is set {}. Returning HTTP BAD_REQUEST (400).", allowNullResult);
//            //   sessionId not provided: return 400
//            throw new WebApplicationException(Response.Status.BAD_REQUEST);
//        }
//
//        LOGGER.debug("Getting session for session identifier '{}'.", sid);
//        Session session = resource.getResourceById(sid);
//
//        if (session == null || Session.NO_SESSION.equals(session)) {
//            if (allowNullResult) {
//                //this call is expected to return 'null' as it is used during POST. This, return 'null' instead of throwing an exception.
//                LOGGER.trace("No session found for session identifier {}. 'allowNullResult' is set {}. Returning 'null'.", sid, allowNullResult);
//                return null;
//            }
//            //   session does not exist: return 404
//            LOGGER.error("No session found for session identifier. 'allowNullResult' is set {}. Returning HTTP NOT_FOUND (404).", sid, allowNullResult);
//            throw new WebApplicationException(Response.Status.NOT_FOUND);
//        }
//        LOGGER.trace("Returning session {} for session identifier {}.", session, sid);
//        return session;
//    }

    /**
     * Helper method to obtain a session from a request. Call {@link #getSessionFromRequest(edu.kit.dama.service.AbstractBaseResource, boolean)
     * } with 'allowNullResult' == false.
     *
     * @param resource The resource providing access to session information.
     *
     * @return The session for the provided sessionId.
     *
     * @throws ServiceException If the session could not be obtained.
     */
//    public static Session getSessionFromRequest(AbstractBaseResource<? extends Session> resource) throws ServiceException {
//        return getSessionFromRequest(resource, false);
//    }
}
