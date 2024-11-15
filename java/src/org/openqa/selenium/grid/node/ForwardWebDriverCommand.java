// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.grid.node;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static org.openqa.selenium.remote.HttpSessionId.getSessionId;
import static org.openqa.selenium.remote.http.Contents.asJson;

import java.util.Map;
import org.openqa.selenium.internal.Require;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.remote.http.HttpHandler;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;

class ForwardWebDriverCommand implements HttpHandler {

  private final Node node;

  ForwardWebDriverCommand(Node node) {
    this.node = Require.nonNull("Node", node);
  }

  public boolean matches(HttpRequest req) {
    return getSessionId(req.getUri())
        .map(id -> node.isSessionOwner(new SessionId(id)))
        .orElse(false);
  }

  @Override
  public HttpResponse execute(HttpRequest req) {
    if (matches(req)) {
      return node.executeWebDriverCommand(req);
    }
    return new HttpResponse()
        .setStatus(HTTP_NOT_FOUND)
        .setContent(
            asJson(
                Map.of(
                    "value",
                    Map.of(
                        "error", "invalid session id",
                        "message",
                            "Cannot find session with id: "
                                + getSessionId(req.getUri()).orElse(null),
                        "stacktrace", ""))));
  }
}
