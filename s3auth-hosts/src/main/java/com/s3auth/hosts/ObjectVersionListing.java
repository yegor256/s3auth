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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSLDocument;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Date;
import java.util.zip.CRC32;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.Charsets;
import org.xembly.Directives;
import org.xembly.ImpossibleModificationException;
import org.xembly.Xembler;

/**
 * XML S3 Object Version Listing.
 * @author Carlos Miranda (miranda.cma@gmail.com)
 * @version $Id$
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "content")
@Loggable(Loggable.DEBUG)
final class ObjectVersionListing implements Resource {
    /**
     * The XSL used for transforming the output.
     */
    private static final XSLDocument XSL;
    static {
        try {
            XSL = new XSLDocument(
                ObjectVersionListing.class.getResourceAsStream("versions.xsl")
            );
        } catch (final IOException ex) {
            throw new IllegalStateException("Cannot get XSL document", ex);
        }
    }
    /**
     * Byte representation of XML data.
     */
    private final transient byte[] content;

    /**
     * Public constructor.
     * @param client Amazon S3 client
     * @param bucket Bucket name
     * @param key The S3 object key
     */
    ObjectVersionListing(@NotNull final AmazonS3 client,
        @NotNull final String bucket, @NotNull final String key) {
        VersionListing listing = client.listVersions(
            new ListVersionsRequest().withPrefix(key).withBucketName(bucket)
        );
        final ImmutableList.Builder<S3VersionSummary> versions =
            ImmutableList.builder();
        versions.addAll(listing.getVersionSummaries());
        while (listing.isTruncated()) {
            listing = client.listNextBatchOfVersions(listing);
            versions.addAll(listing.getVersionSummaries());
        }
        // @checkstyle LineLength (2 lines)
        final Directives dirs = new Directives()
            .add("versions").attr("object", key);
        for (final S3VersionSummary version : versions.build()) {
            dirs.add("version")
                .attr("key", version.getKey())
                .set(version.getVersionId()).up();
        }
        try {
            this.content = XSL.transform(
                new XMLDocument(new Xembler(dirs).xml())
            ).toString().getBytes(Charsets.UTF_8);
        } catch (final ImpossibleModificationException ex) {
            throw new IllegalStateException(
                "Unable to generate version listing", ex
            );
        }
    }

    @Override
    public int status() {
        return HttpURLConnection.HTTP_OK;
    }

    @Override
    public long writeTo(final OutputStream stream) throws IOException {
        stream.write(this.content);
        return this.content.length;
    }

    @Override
    public Collection<String> headers() throws IOException {
        final ImmutableSet.Builder<String> headers = ImmutableSet.builder();
        headers.add(header(HttpHeaders.CONTENT_TYPE, "application/xhtml+xml"));
        headers.add(
            header(
                HttpHeaders.CONTENT_LENGTH,
                String.valueOf(this.content.length)
            )
        );
        return headers.build();
    }

    @Override
    public String etag() {
        final CRC32 crc = new CRC32();
        crc.update(this.content);
        return Long.toHexString(crc.getValue());
    }
    @Override
    public Date lastModified() {
        return new Date();
    }

    /**
     * Create a HTTP header from name and value.
     * @param name Name of the header
     * @param value The value
     * @return Full HTTP header string
     */
    @NotNull
    private static String header(@NotNull final String name,
        @NotNull final String value) {
        return String.format("%s: %s", name, value);
    }
}
