/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.social.services.rest;

import org.exoplatform.services.rest.ContainerResponseWriter;
import org.exoplatform.services.rest.impl.ContainerRequest;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.EnvironmentContext;
import org.exoplatform.services.rest.impl.InputHeadersMap;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.rest.tools.DummyContainerResponseWriter;
import org.exoplatform.services.test.mock.MockHttpServletRequest;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;

/**
 * AbstractResourceTest.java <br />
 * Provides <code>service</code> method to test rest service.
 * @author <a href="http://hoatle.net">hoatle</a>
 * @since Mar 3, 2010
 */
public abstract class AbstractResourceTest extends AbstractRestTest {

  /**
   * gets response with provided writer
   * @param method
   * @param requestURI
   * @param baseURI
   * @param headers
   * @param data
   * @param writer
   * @return
   * @throws Exception
   */
  public ContainerResponse service(String method,
                                   String requestURI,
                                   String baseURI,
                                   Map<String, List<String>> headers,
                                   byte[] data,
                                   ContainerResponseWriter writer) throws Exception {

    if (headers == null) {
      headers = new MultivaluedMapImpl();
    }

    ByteArrayInputStream in = null;
    if (data != null) {
      in = new ByteArrayInputStream(data);
    }

    EnvironmentContext envctx = new EnvironmentContext();
    HttpServletRequest httpRequest = new MockHttpServletRequest("",
                                                                in,
                                                                in != null ? in.available() : 0,
                                                                method,
                                                                headers);
    envctx.put(HttpServletRequest.class, httpRequest);
    EnvironmentContext.setCurrent(envctx);
    ContainerRequest request = new ContainerRequest(method,
                                                    new URI(requestURI),
                                                    new URI(baseURI),
                                                    in,
                                                    new InputHeadersMap(headers));
    ContainerResponse response = new ContainerResponse(writer);
    requestHandler.handleRequest(request, response);
    return response;
  }

  /**
   * gets response without provided writer
   * @param method
   * @param requestURI
   * @param baseURI
   * @param headers
   * @param data
   * @return
   * @throws Exception
   */
  public ContainerResponse service(String method,
                                   String requestURI,
                                   String baseURI,
                                   MultivaluedMap<String, String> headers,
                                   byte[] data) throws Exception {
    return service(method, requestURI, baseURI, headers, data, new DummyContainerResponseWriter());
  }
}
