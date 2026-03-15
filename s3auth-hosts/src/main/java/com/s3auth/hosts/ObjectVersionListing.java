/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XSL;
import com.jcabi.xml.XSLDocument;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.zip.CRC32;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import org.xembly.Directives;
import org.xembly.ImpossibleModificationException;
import org.xembly.Xembler;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsResponse;
import software.amazon.awssdk.services.s3.model.ObjectVersion;

/**
 * XML S3 Object Version Listing.
 *
 * @since 0.0.1
 */
@Immutable
@Loggable(Loggable.DEBUG)
final class ObjectVersionListing implements Resource {

    /**
     * The STYLESHEET used for transforming the output.
     */
    private static final XSL STYLESHEET = XSLDocument.make(
        ObjectVersionListing.class.getResourceAsStream("versions.xsl")
    );

    /**
     * Byte representation of XML data.
     */
    @Immutable.Array
    private final transient byte[] content;

    /**
     * Public constructor.
     * @param client Amazon S3 client
     * @param bucket Bucket name
     * @param key The S3 object key
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    ObjectVersionListing(@NotNull final S3Client client,
        @NotNull final String bucket, @NotNull final String key) {
        final ImmutableList.Builder<ObjectVersion> versions =
            ImmutableList.builder();
        String token = null;
        String vtoken = null;
        do {
            final ListObjectVersionsRequest.Builder builder =
                ListObjectVersionsRequest.builder()
                    .prefix(key)
                    .bucket(bucket);
            if (token != null) {
                builder.keyMarker(token);
            }
            if (vtoken != null) {
                builder.versionIdMarker(vtoken);
            }
            final ListObjectVersionsResponse listing =
                client.listObjectVersions(builder.build());
            versions.addAll(listing.versions());
            if (listing.isTruncated()) {
                token = listing.nextKeyMarker();
                vtoken = listing.nextVersionIdMarker();
            } else {
                token = null;
                vtoken = null;
            }
        } while (token != null);
        final Directives dirs = new Directives()
            .add("versions").attr("object", key);
        for (final ObjectVersion version : versions.build()) {
            dirs.add("version")
                .attr("key", version.key())
                .set(version.versionId()).up();
        }
        try {
            this.content = ObjectVersionListing.STYLESHEET.transform(
                new XMLDocument(new Xembler(dirs).xml())
            ).toString().getBytes(StandardCharsets.UTF_8);
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
    public Collection<String> headers() {
        final ImmutableSet.Builder<String> headers = ImmutableSet.builder();
        headers.add(
            ObjectVersionListing.header(
                HttpHeaders.CONTENT_TYPE,
                this.contentType()
            )
        );
        headers.add(
            ObjectVersionListing.header(
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

    @Override
    public String toString() {
        return String.format("ObjectVersionListing(%d bytes)", this.content.length);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.content);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof ObjectVersionListing
            && Arrays.equals(this.content, ((ObjectVersionListing) obj).content);
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
