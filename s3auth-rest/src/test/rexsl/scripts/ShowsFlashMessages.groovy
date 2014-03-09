/**
 * Copyright (c) 2012, s3auth.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the s3auth.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.s3auth.rest.rexsl.scripts

import com.jcabi.http.Request
import com.jcabi.http.request.JdkRequest
import com.jcabi.http.response.RestResponse
import com.jcabi.http.response.XmlResponse
import com.jcabi.http.wire.CookieOptimizingWire
import com.jcabi.manifests.Manifests
import com.rexsl.page.auth.AuthInset
import com.s3auth.hosts.UserMocker
import com.s3auth.rest.RestUser
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import org.hamcrest.Matchers

Manifests.append(new File(rexsl.basedir, 'target/test-classes/META-INF/MANIFEST.MF'))

String cookie() {
    def user = new RestUser(new UserMocker().withIdentity('urn:facebook:777').mock())
    'Rexsl-Auth=' + AuthInset.encrypt(
        user.asIdentity(),
        Manifests.read('S3Auth-SecurityKey')
    )
}
def host = 'test-2.s3auth.com'

def home = new JdkRequest(rexsl.home)
    .through(CookieOptimizingWire)
    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
    .header(HttpHeaders.COOKIE, this.cookie())
    .fetch().as(RestResponse)
    .assertStatus(HttpURLConnection.HTTP_OK)
    .as(XmlResponse)
if (!home.xml().nodes("//domain[name='${host}']").isEmpty()) {
    home.rel("//domain[name='${host}']/links/link[@rel='remove']/@href")
        .fetch().as(RestResponse)
        .assertStatus(HttpURLConnection.HTTP_SEE_OTHER)
}

home.rel('/page/links/link[@rel="add"]/@href')
    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
    .body()
    .formParam('host', host)
    .formParam('key', 'AAAAAAAAAAAAAAAAAAAA')
    .formParam('secret', 'BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB')
    .back()
    .method(Request.POST)
    .fetch()
    .as(RestResponse)
    .assertStatus(HttpURLConnection.HTTP_SEE_OTHER)
    .assertHeader(
        HttpHeaders.SET_COOKIE,
        Matchers.hasItem(Matchers.containsString('Rexsl-Flash'))
    )
    .follow()
    .method(Request.GET)
    .reset(HttpHeaders.CONTENT_TYPE)
    .body().set('').back()
    .fetch()
    .as(XmlResponse)
    .assertXPath('/page/flash[level="INFO"]')
    .assertXPath('/page/flash/message')
    .rel('/page/links/link[@rel="home"]/@href')
    .fetch()
    .as(XmlResponse)
    .assertXPath('/page[not(flash)]')
