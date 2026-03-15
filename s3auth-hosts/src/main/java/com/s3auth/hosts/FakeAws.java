/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.GetBucketAclRequest;
import software.amazon.awssdk.services.s3.model.GetBucketAclResponse;
import software.amazon.awssdk.services.s3.model.GetBucketLocationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketLocationResponse;
import software.amazon.awssdk.services.s3.model.GetBucketVersioningRequest;
import software.amazon.awssdk.services.s3.model.GetBucketVersioningResponse;
import software.amazon.awssdk.services.s3.model.GetBucketWebsiteRequest;
import software.amazon.awssdk.services.s3.model.GetBucketWebsiteResponse;
import software.amazon.awssdk.services.s3.model.GetObjectAclRequest;
import software.amazon.awssdk.services.s3.model.GetObjectAclResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

/**
 * Fake AWS S3 client implementation for testing.
 *
 * <p>Implements only the methods required by the test suite with stub
 * implementations, all other methods throw UnsupportedOperationException.
 *
 * @since 0.0.1
 * @checkstyle ClassFanOutComplexityCheck (500 lines)
 */
@SuppressWarnings({"PMD", "serial"})
public final class FakeAws implements S3Client, Serializable {

    @Override
    public String serviceName() {
        return "s3";
    }

    @Override
    public void close() {
        // nothing to close
    }

    @Override
    public ResponseInputStream<GetObjectResponse> getObject(
        final GetObjectRequest request) {
        final String txt = "TXT";
        final InputStream stream = new ByteArrayInputStream(
            txt.getBytes(StandardCharsets.UTF_8)
        );
        return new ResponseInputStream<>(
            GetObjectResponse.builder()
                .contentLength((long) txt.length())
                .build(),
            AbortableInputStream.create(stream)
        );
    }

    @Override
    public ListObjectsResponse listObjects(final ListObjectsRequest request) {
        return ListObjectsResponse.builder().build();
    }

    @Override
    public ListObjectsV2Response listObjectsV2(
        final ListObjectsV2Request request) {
        return ListObjectsV2Response.builder().build();
    }

    @Override
    public ListObjectVersionsResponse listObjectVersions(
        final ListObjectVersionsRequest request) {
        return ListObjectVersionsResponse.builder().build();
    }

    @Override
    public GetBucketWebsiteResponse getBucketWebsite(
        final GetBucketWebsiteRequest request) {
        return GetBucketWebsiteResponse.builder().build();
    }

    @Override
    public PutObjectResponse putObject(
        final PutObjectRequest request, final RequestBody body) {
        return PutObjectResponse.builder().build();
    }

    @Override
    public DeleteObjectResponse deleteObject(final DeleteObjectRequest request) {
        return DeleteObjectResponse.builder().build();
    }

    @Override
    public DeleteObjectsResponse deleteObjects(
        final DeleteObjectsRequest request) {
        return DeleteObjectsResponse.builder().build();
    }

    @Override
    public CopyObjectResponse copyObject(final CopyObjectRequest request) {
        return CopyObjectResponse.builder().build();
    }

    @Override
    public CreateBucketResponse createBucket(final CreateBucketRequest request) {
        return CreateBucketResponse.builder().build();
    }

    @Override
    public DeleteBucketResponse deleteBucket(final DeleteBucketRequest request) {
        return DeleteBucketResponse.builder().build();
    }

    @Override
    public HeadBucketResponse headBucket(final HeadBucketRequest request) {
        return HeadBucketResponse.builder().build();
    }

    @Override
    public HeadObjectResponse headObject(final HeadObjectRequest request) {
        return HeadObjectResponse.builder().build();
    }

    @Override
    public ListBucketsResponse listBuckets(final ListBucketsRequest request) {
        return ListBucketsResponse.builder().build();
    }

    @Override
    public ListBucketsResponse listBuckets() {
        return ListBucketsResponse.builder().build();
    }

    @Override
    public GetBucketAclResponse getBucketAcl(final GetBucketAclRequest request) {
        return GetBucketAclResponse.builder().build();
    }

    @Override
    public GetBucketLocationResponse getBucketLocation(
        final GetBucketLocationRequest request) {
        return GetBucketLocationResponse.builder().build();
    }

    @Override
    public GetBucketVersioningResponse getBucketVersioning(
        final GetBucketVersioningRequest request) {
        return GetBucketVersioningResponse.builder().build();
    }

    @Override
    public GetObjectAclResponse getObjectAcl(final GetObjectAclRequest request) {
        return GetObjectAclResponse.builder().build();
    }

    @Override
    public CreateMultipartUploadResponse createMultipartUpload(
        final CreateMultipartUploadRequest request) {
        return CreateMultipartUploadResponse.builder().build();
    }

    @Override
    public UploadPartResponse uploadPart(
        final UploadPartRequest request, final RequestBody body) {
        return UploadPartResponse.builder().build();
    }

    @Override
    public CompleteMultipartUploadResponse completeMultipartUpload(
        final CompleteMultipartUploadRequest request) {
        return CompleteMultipartUploadResponse.builder().build();
    }

    @Override
    public AbortMultipartUploadResponse abortMultipartUpload(
        final AbortMultipartUploadRequest request) {
        return AbortMultipartUploadResponse.builder().build();
    }

}
