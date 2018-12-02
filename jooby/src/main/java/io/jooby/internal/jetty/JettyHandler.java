/**
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *    Copyright 2014 Edgar Espina
 */
package io.jooby.internal.jetty;

import io.jooby.Route;
import io.jooby.Router;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JettyHandler extends AbstractHandler {
  private final Router router;

  public JettyHandler(Router router) {
    this.router = router;
  }

  @Override public void handle(String target, Request request, HttpServletRequest servletRequest,
      HttpServletResponse response) throws IOException, ServletException {
    JettyContext context = new JettyContext(request, router.worker(), router.errorHandler(), router.tmpdir());
    Router.Match match = router.match(context);
    handleMatch(target, request, response, context, router, match.route());
  }

  protected void handleMatch(String target, Request request, HttpServletResponse response,
      JettyContext context, Router router, Route route) throws IOException, ServletException {
    Route.Handler handler = route.pipeline();
    if (route.gzip() && acceptGzip(request.getHeader("Accept-Encoding"))) {
      /** Gzip: */
      GzipHandler jettyGzip = new GzipHandler();
      jettyGzip.setHandler(gzipCall(handler, context));
      jettyGzip.handle(target, request, request, request.getResponse());
    } else {
      handler.execute(context);
    }
  }

  private boolean acceptGzip(String value) {
    return value != null && value.contains("gzip");
  }

  private static Handler gzipCall(Route.Handler handler, JettyContext ctx) {
    return new AbstractHandler() {
      @Override public void handle(String target, Request baseRequest, HttpServletRequest request,
          HttpServletResponse response) {
        handler.execute(ctx);
      }
    };
  }
}
