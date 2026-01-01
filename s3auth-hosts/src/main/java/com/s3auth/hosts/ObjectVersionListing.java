/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
import com.jcabi.xml.XSL;
import com.jcabi.xml.XSLDocument;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.zip.CRC32;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directives;
import org.xembly.ImpossibleModificationException;
import org.xembly.Xembler;

/**
 * XML S3 Object Version Listing.
 *
 * @since 0.0.1
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "content")
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
