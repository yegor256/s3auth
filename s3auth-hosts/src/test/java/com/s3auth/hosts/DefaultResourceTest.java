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
package com.s3auth.hosts;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Random;
import org.apache.http.client.methods.HttpGet;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link DefaultResource}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class DefaultResourceTest {

    /**
     * DefaultResource can build headers.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void getsHeadersFromAmazonObject() throws Exception {
        final S3Object object = Mockito.mock(S3Object.class);
        final ObjectMetadata meta = Mockito.mock(ObjectMetadata.class);
        Mockito.doReturn(meta).when(object).getObjectMetadata();
        Mockito.doReturn(1L).when(meta).getContentLength();
        final Resource res = new DefaultResource(object, Range.ENTIRE);
        MatcherAssert.assertThat(
            res.headers(),
            Matchers.hasItem("Content-Length: 1")
        );
    }

    /**
     * DefaultResource can write to output stream.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void writesFromAmazonObjectToOutputStream() throws Exception {
        final S3ObjectInputStream stream =
            Mockito.mock(S3ObjectInputStream.class);
        Mockito.doReturn(-1).when(stream).read(Mockito.any(byte[].class));
        final S3Object object = Mockito.mock(S3Object.class);
        Mockito.doReturn(stream).when(object).getObjectContent();
        MatcherAssert.assertThat(
            ResourceMocker.toString(new DefaultResource(object, Range.ENTIRE)),
            Matchers.equalTo("")
        );
    }

    /**
     * DefaultResource can write a real input stream to output stream.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void writesInputToOutputStream() throws Exception {
        final int size = 100 * 1024;
        final byte[] data = new byte[size];
        final Random random = new Random();
        for (int pos = 0; pos < size; ++pos) {
            data[pos] = (byte) random.nextInt();
        }
        final S3ObjectInputStream stream = new S3ObjectInputStream(
            new ByteArrayInputStream(data),
            new HttpGet()
        );
        final S3Object object = Mockito.mock(S3Object.class);
        Mockito.doReturn(stream).when(object).getObjectContent();
        MatcherAssert.assertThat(
            ResourceMocker.toByteArray(
                new DefaultResource(object, Range.ENTIRE)
            ),
            Matchers.equalTo(data)
        );
    }

    /**
     * DefaultResource can throw when failed to read.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = IOException.class)
    public void throwsWhenFailedToRead() throws Exception {
        final S3ObjectInputStream stream =
            Mockito.mock(S3ObjectInputStream.class);
        Mockito.doThrow(new IOException("oops"))
            .when(stream).read(Mockito.any(byte[].class));
        final S3Object object = Mockito.mock(S3Object.class);
        Mockito.doReturn(stream).when(object).getObjectContent();
        MatcherAssert.assertThat(
            ResourceMocker.toString(new DefaultResource(object, Range.ENTIRE)),
            Matchers.equalTo("")
        );
    }

}
