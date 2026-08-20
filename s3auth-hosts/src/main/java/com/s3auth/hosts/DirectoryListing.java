/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

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
import java.time.Instant;
import java.util.ArrayList;
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
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * XML Directory Listing.
 * @since 0.0.1
 */
@Immutable
@Loggable(Loggable.DEBUG)
final class DirectoryListing implements Resource {

    /**
     * The STYLESHEET used for transforming the output.
     */
    private static final XSL STYLESHEET = XSLDocument.make(
        DirectoryListing.class.getResourceAsStream("directory.xsl")
    );

    /**
     * Transformed XML content of the listing.
     */
    @Immutable.Array
    private final transient byte[] content;

    /**
     * Private ctor, content fetched by {@link #fetch}.
     * @param cnt The already-fetched content
     */
    private DirectoryListing(final byte[] cnt) {
        this.content = cnt;
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
            DirectoryListing.header(
                HttpHeaders.CONTENT_TYPE,
                this.contentType()
            )
        );
        headers.add(
            DirectoryListing.header(
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
        return Date.from(Instant.now());
    }

    @Override
    public String contentType() {
        return "text/html";
    }

    @Override
    public void close() {
        // nothing to do
    }

    @Override
    public String toString() {
        return String.format("DirectoryListing(%d bytes)", this.content.length);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.content);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof DirectoryListing
            && Arrays.equals(this.content, ((DirectoryListing) obj).content);
    }

    /**
     * Fetch a directory listing from S3.
     * @param clnt Amazon S3 client
     * @param bckt Bucket name
     * @param name The S3 object key
     * @return Directory listing resource
     */
    static DirectoryListing fetch(@NotNull final S3Client clnt,
        @NotNull final String bckt, @NotNull final String name) {
        return new DirectoryListing(
            DirectoryListing.transform(
                name, DirectoryListing.paginate(clnt, bckt, name)
            )
        );
    }

    private static Listing paginate(final S3Client clnt,
        final String bckt, final String key) {
        final Collection<S3Object> objects = new ArrayList<>(0);
        final Collection<String> prefixes = new ArrayList<>(0);
        String token = null;
        boolean more = true;
        while (more) {
            final ListObjectsRequest.Builder builder = ListObjectsRequest.builder()
                .delimiter("/")
                .prefix(key)
                .bucket(bckt);
            if (token != null) {
                builder.marker(token);
            }
            final ListObjectsResponse listing = clnt.listObjects(builder.build());
            objects.addAll(listing.contents());
            for (final CommonPrefix prefix : listing.commonPrefixes()) {
                prefixes.add(prefix.prefix());
            }
            more = listing.isTruncated();
            if (more) {
                token = listing.nextMarker();
                if (token == null && !listing.contents().isEmpty()) {
                    token = listing.contents().get(listing.contents().size() - 1).key();
                }
                more = token != null;
            }
        }
        return new Listing(objects, prefixes);
    }

    private static byte[] transform(final String key, final Listing listing) {
        final Directives dirs = new Directives()
            .add("directory").attr("prefix", key);
        for (final String prefix : listing.prefixes()) {
            dirs.add("commonPrefix").set(prefix).up();
        }
        for (final S3Object object : listing.objects()) {
            dirs.add("object")
                .add("path")
                .set(object.key()).up()
                .add("size")
                .set(Long.toString(object.size())).up()
                .up();
        }
        try {
            return DirectoryListing.STYLESHEET.transform(
                new XMLDocument(new Xembler(dirs).xml())
            ).toString().getBytes(StandardCharsets.UTF_8);
        } catch (final ImpossibleModificationException ex) {
            throw new IllegalStateException(
                "Unable to generate directory listing", ex
            );
        }
    }

    @NotNull
    private static String header(@NotNull final String name,
        @NotNull final String value) {
        return String.format("%s: %s", name, value);
    }
}
