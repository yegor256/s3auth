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
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
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
import java.util.LinkedList;
import java.util.List;
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
 * XML Directory Listing.
 * @author Carlos Miranda (miranda.cma@gmail.com)
 * @version $Id$
 * @checkstyle ClassDataAbstraction (200 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "content")
@Loggable(Loggable.DEBUG)
final class DirectoryListing implements Resource {
    /**
     * The XSL used for transforming the output.
     */
    private static final XSLDocument XSL;
    static {
        try {
            XSL = new XSLDocument(
                DirectoryListing.class.getResourceAsStream("directory.xsl")
            );
        } catch (final IOException ex) {
            throw new IllegalStateException("Cannot get XSL document", ex);
        }
    }

    /**
     * Byte representation of transformed data.
     */
    private final transient byte[] content;

    /**
     * Public constructor.
     * @param client Amazon S3 client
     * @param bucket Bucket name
     * @param key The S3 object key
     */
    DirectoryListing(@NotNull final AmazonS3 client,
        @NotNull final String bucket, @NotNull final String key) {
        ObjectListing listing = client.listObjects(
            new ListObjectsRequest().withDelimiter("/").withPrefix(key)
                .withBucketName(bucket)
        );
        final List<S3ObjectSummary> objects =
            new LinkedList<S3ObjectSummary>();
        objects.addAll(listing.getObjectSummaries());
        while (listing.isTruncated()) {
            listing = client.listNextBatchOfObjects(listing);
            objects.addAll(listing.getObjectSummaries());
        }
        // @checkstyle LineLength (2 lines)
        final Directives dirs = new Directives()
            .add("directory").attr("prefix", key);
        for (final String prefix : listing.getCommonPrefixes()) {
            dirs.add("commonPrefix").set(prefix).up();
        }
        for (final S3ObjectSummary object : objects) {
            dirs.add("object")
                .add("path")
                .set(object.getKey()).up()
                .add("size")
                .set(Long.toString(object.getSize())).up()
                .up();
        }
        try {
            this.content = XSL.transform(
                new XMLDocument(new Xembler(dirs).xml())
            ).toString().getBytes(Charsets.UTF_8);
        } catch (final ImpossibleModificationException ex) {
            throw new IllegalStateException(
                "Unable to generate directory listing", ex
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
        headers.add(header(HttpHeaders.CONTENT_TYPE, this.contentType()));
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

    @Override
    public String contentType() {
        return "application/xhtml+xml";
    }

    @Override
    public void close() {
        // nothing to do
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
