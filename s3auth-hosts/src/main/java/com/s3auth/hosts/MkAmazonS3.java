/*
 * Copyright (c) 2012-2023, Yegor Bugayenko
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

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.S3ResponseMetadata;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.BucketAccelerateConfiguration;
import com.amazonaws.services.s3.model.BucketCrossOriginConfiguration;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.BucketLoggingConfiguration;
import com.amazonaws.services.s3.model.BucketNotificationConfiguration;
import com.amazonaws.services.s3.model.BucketPolicy;
import com.amazonaws.services.s3.model.BucketReplicationConfiguration;
import com.amazonaws.services.s3.model.BucketTaggingConfiguration;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.BucketWebsiteConfiguration;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.CopyPartRequest;
import com.amazonaws.services.s3.model.CopyPartResult;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.DeleteBucketAnalyticsConfigurationRequest;
import com.amazonaws.services.s3.model.DeleteBucketAnalyticsConfigurationResult;
import com.amazonaws.services.s3.model.DeleteBucketCrossOriginConfigurationRequest;
import com.amazonaws.services.s3.model.DeleteBucketEncryptionRequest;
import com.amazonaws.services.s3.model.DeleteBucketEncryptionResult;
import com.amazonaws.services.s3.model.DeleteBucketIntelligentTieringConfigurationRequest;
import com.amazonaws.services.s3.model.DeleteBucketIntelligentTieringConfigurationResult;
import com.amazonaws.services.s3.model.DeleteBucketInventoryConfigurationRequest;
import com.amazonaws.services.s3.model.DeleteBucketInventoryConfigurationResult;
import com.amazonaws.services.s3.model.DeleteBucketLifecycleConfigurationRequest;
import com.amazonaws.services.s3.model.DeleteBucketMetricsConfigurationRequest;
import com.amazonaws.services.s3.model.DeleteBucketMetricsConfigurationResult;
import com.amazonaws.services.s3.model.DeleteBucketOwnershipControlsRequest;
import com.amazonaws.services.s3.model.DeleteBucketOwnershipControlsResult;
import com.amazonaws.services.s3.model.DeleteBucketPolicyRequest;
import com.amazonaws.services.s3.model.DeleteBucketReplicationConfigurationRequest;
import com.amazonaws.services.s3.model.DeleteBucketRequest;
import com.amazonaws.services.s3.model.DeleteBucketTaggingConfigurationRequest;
import com.amazonaws.services.s3.model.DeleteBucketWebsiteConfigurationRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectTaggingRequest;
import com.amazonaws.services.s3.model.DeleteObjectTaggingResult;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.DeletePublicAccessBlockRequest;
import com.amazonaws.services.s3.model.DeletePublicAccessBlockResult;
import com.amazonaws.services.s3.model.DeleteVersionRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetBucketAccelerateConfigurationRequest;
import com.amazonaws.services.s3.model.GetBucketAclRequest;
import com.amazonaws.services.s3.model.GetBucketAnalyticsConfigurationRequest;
import com.amazonaws.services.s3.model.GetBucketAnalyticsConfigurationResult;
import com.amazonaws.services.s3.model.GetBucketCrossOriginConfigurationRequest;
import com.amazonaws.services.s3.model.GetBucketEncryptionRequest;
import com.amazonaws.services.s3.model.GetBucketEncryptionResult;
import com.amazonaws.services.s3.model.GetBucketIntelligentTieringConfigurationRequest;
import com.amazonaws.services.s3.model.GetBucketIntelligentTieringConfigurationResult;
import com.amazonaws.services.s3.model.GetBucketInventoryConfigurationRequest;
import com.amazonaws.services.s3.model.GetBucketInventoryConfigurationResult;
import com.amazonaws.services.s3.model.GetBucketLifecycleConfigurationRequest;
import com.amazonaws.services.s3.model.GetBucketLocationRequest;
import com.amazonaws.services.s3.model.GetBucketLoggingConfigurationRequest;
import com.amazonaws.services.s3.model.GetBucketMetricsConfigurationRequest;
import com.amazonaws.services.s3.model.GetBucketMetricsConfigurationResult;
import com.amazonaws.services.s3.model.GetBucketNotificationConfigurationRequest;
import com.amazonaws.services.s3.model.GetBucketOwnershipControlsRequest;
import com.amazonaws.services.s3.model.GetBucketOwnershipControlsResult;
import com.amazonaws.services.s3.model.GetBucketPolicyRequest;
import com.amazonaws.services.s3.model.GetBucketPolicyStatusRequest;
import com.amazonaws.services.s3.model.GetBucketPolicyStatusResult;
import com.amazonaws.services.s3.model.GetBucketReplicationConfigurationRequest;
import com.amazonaws.services.s3.model.GetBucketTaggingConfigurationRequest;
import com.amazonaws.services.s3.model.GetBucketVersioningConfigurationRequest;
import com.amazonaws.services.s3.model.GetBucketWebsiteConfigurationRequest;
import com.amazonaws.services.s3.model.GetObjectAclRequest;
import com.amazonaws.services.s3.model.GetObjectLegalHoldRequest;
import com.amazonaws.services.s3.model.GetObjectLegalHoldResult;
import com.amazonaws.services.s3.model.GetObjectLockConfigurationRequest;
import com.amazonaws.services.s3.model.GetObjectLockConfigurationResult;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRetentionRequest;
import com.amazonaws.services.s3.model.GetObjectRetentionResult;
import com.amazonaws.services.s3.model.GetObjectTaggingRequest;
import com.amazonaws.services.s3.model.GetObjectTaggingResult;
import com.amazonaws.services.s3.model.GetPublicAccessBlockRequest;
import com.amazonaws.services.s3.model.GetPublicAccessBlockResult;
import com.amazonaws.services.s3.model.GetS3AccountOwnerRequest;
import com.amazonaws.services.s3.model.HeadBucketRequest;
import com.amazonaws.services.s3.model.HeadBucketResult;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ListBucketAnalyticsConfigurationsRequest;
import com.amazonaws.services.s3.model.ListBucketAnalyticsConfigurationsResult;
import com.amazonaws.services.s3.model.ListBucketIntelligentTieringConfigurationsRequest;
import com.amazonaws.services.s3.model.ListBucketIntelligentTieringConfigurationsResult;
import com.amazonaws.services.s3.model.ListBucketInventoryConfigurationsRequest;
import com.amazonaws.services.s3.model.ListBucketInventoryConfigurationsResult;
import com.amazonaws.services.s3.model.ListBucketMetricsConfigurationsRequest;
import com.amazonaws.services.s3.model.ListBucketMetricsConfigurationsResult;
import com.amazonaws.services.s3.model.ListBucketsRequest;
import com.amazonaws.services.s3.model.ListMultipartUploadsRequest;
import com.amazonaws.services.s3.model.ListNextBatchOfObjectsRequest;
import com.amazonaws.services.s3.model.ListNextBatchOfVersionsRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ListPartsRequest;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.MultipartUploadListing;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Owner;
import com.amazonaws.services.s3.model.PartListing;
import com.amazonaws.services.s3.model.PresignedUrlDownloadRequest;
import com.amazonaws.services.s3.model.PresignedUrlDownloadResult;
import com.amazonaws.services.s3.model.PresignedUrlUploadRequest;
import com.amazonaws.services.s3.model.PresignedUrlUploadResult;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.s3.model.RestoreObjectRequest;
import com.amazonaws.services.s3.model.RestoreObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.SelectObjectContentRequest;
import com.amazonaws.services.s3.model.SelectObjectContentResult;
import com.amazonaws.services.s3.model.SetBucketAccelerateConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketAclRequest;
import com.amazonaws.services.s3.model.SetBucketAnalyticsConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketAnalyticsConfigurationResult;
import com.amazonaws.services.s3.model.SetBucketCrossOriginConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketEncryptionRequest;
import com.amazonaws.services.s3.model.SetBucketEncryptionResult;
import com.amazonaws.services.s3.model.SetBucketIntelligentTieringConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketIntelligentTieringConfigurationResult;
import com.amazonaws.services.s3.model.SetBucketInventoryConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketInventoryConfigurationResult;
import com.amazonaws.services.s3.model.SetBucketLifecycleConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketLoggingConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketMetricsConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketMetricsConfigurationResult;
import com.amazonaws.services.s3.model.SetBucketNotificationConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketOwnershipControlsRequest;
import com.amazonaws.services.s3.model.SetBucketOwnershipControlsResult;
import com.amazonaws.services.s3.model.SetBucketPolicyRequest;
import com.amazonaws.services.s3.model.SetBucketReplicationConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketTaggingConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketWebsiteConfigurationRequest;
import com.amazonaws.services.s3.model.SetObjectAclRequest;
import com.amazonaws.services.s3.model.SetObjectLegalHoldRequest;
import com.amazonaws.services.s3.model.SetObjectLegalHoldResult;
import com.amazonaws.services.s3.model.SetObjectLockConfigurationRequest;
import com.amazonaws.services.s3.model.SetObjectLockConfigurationResult;
import com.amazonaws.services.s3.model.SetObjectRetentionRequest;
import com.amazonaws.services.s3.model.SetObjectRetentionResult;
import com.amazonaws.services.s3.model.SetObjectTaggingRequest;
import com.amazonaws.services.s3.model.SetObjectTaggingResult;
import com.amazonaws.services.s3.model.SetPublicAccessBlockRequest;
import com.amazonaws.services.s3.model.SetPublicAccessBlockResult;
import com.amazonaws.services.s3.model.SetRequestPaymentConfigurationRequest;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.amazonaws.services.s3.model.VersionListing;
import com.amazonaws.services.s3.model.WriteGetObjectResponseRequest;
import com.amazonaws.services.s3.model.WriteGetObjectResponseResult;
import com.amazonaws.services.s3.model.analytics.AnalyticsConfiguration;
import com.amazonaws.services.s3.model.intelligenttiering.IntelligentTieringConfiguration;
import com.amazonaws.services.s3.model.inventory.InventoryConfiguration;
import com.amazonaws.services.s3.model.metrics.MetricsConfiguration;
import com.amazonaws.services.s3.model.ownership.OwnershipControls;
import com.amazonaws.services.s3.waiters.AmazonS3Waiters;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;

/**
 * AmazonS3 Mock (without Mockito).
 *
 * @since 0.0.1
 * @checkstyle ThrowsCount (10000 lines)
 * @checkstyle RedundantThrows (10000 lines)
 * @checkstyle ParameterName (10000 lines)
 * @checkstyle DesignForExtension (10000 lines)
 * @checkstyle FinalParameters (10000 lines)
 * @checkstyle MethodName (10000 lines)
 * @checkstyle ParameterNumber (10000 lines)
 * @checkstyle BracketsStructure (10000 lines)
 * @checkstyle LineLength (10000 lines)
 * @checkstyle ClassFanOutComplexity (10000 lines)
 * @checkstyle MethodCountCheck (10000 lines)
 * @checkstyle FileLengthCheck (10000 lines)
 */
@SuppressWarnings({"PMD", "deprecation", "serial"})
public class MkAmazonS3 implements AmazonS3, Serializable {
    @Override
    public void abortMultipartUpload(AbortMultipartUploadRequest arg0)
        throws AmazonClientException {
    }

    @Override
    public void changeObjectStorageClass(String arg0, String arg1,
        StorageClass arg2) throws AmazonClientException {
    }

    @Override
    public CompleteMultipartUploadResult completeMultipartUpload(
        CompleteMultipartUploadRequest arg0) throws AmazonClientException {
        return null;
    }

    @Override
    public CopyObjectResult copyObject(CopyObjectRequest arg0)
        throws AmazonClientException {
        return null;
    }

    @Override
    public CopyObjectResult copyObject(String arg0, String arg1, String arg2,
        String arg3) throws AmazonClientException {
        return null;
    }

    @Override
    public CopyPartResult copyPart(CopyPartRequest arg0)
        throws AmazonClientException {
        return null;
    }

    @Override
    public Bucket createBucket(CreateBucketRequest arg0)
        throws AmazonClientException {
        return null;
    }

    @Override
    public Bucket createBucket(String arg0) throws AmazonClientException {
        return null;
    }

    @Override
    public Bucket createBucket(String arg0, Region arg1)
        throws AmazonClientException {
        return null;
    }

    @Override
    public Bucket createBucket(String arg0, String arg1)
        throws AmazonClientException {
        return null;
    }

    @Override
    public void deleteBucket(DeleteBucketRequest arg0)
        throws AmazonClientException {
    }

    @Override
    public void deleteBucket(String arg0) throws AmazonClientException {
    }

    @Override
    public void deleteBucketCrossOriginConfiguration(String arg0) {
    }

    @Override
    public void deleteBucketCrossOriginConfiguration(
        DeleteBucketCrossOriginConfigurationRequest arg0) {
    }

    @Override
    public void deleteBucketLifecycleConfiguration(String arg0) {
    }

    @Override
    public void deleteBucketLifecycleConfiguration(
        DeleteBucketLifecycleConfigurationRequest arg0) {
    }

    @Override
    public void deleteBucketPolicy(String arg0) throws AmazonClientException {
    }

    @Override
    public void deleteBucketPolicy(DeleteBucketPolicyRequest arg0)
        throws AmazonClientException {
    }

    @Override
    public void deleteBucketTaggingConfiguration(String arg0) {
    }

    @Override
    public void deleteBucketTaggingConfiguration(
        DeleteBucketTaggingConfigurationRequest arg0) {
    }

    @Override
    public void deleteBucketWebsiteConfiguration(String arg0)
        throws AmazonClientException {
    }

    @Override
    public void deleteBucketWebsiteConfiguration(
        DeleteBucketWebsiteConfigurationRequest arg0)
        throws AmazonClientException {
    }

    @Override
    public void deleteObject(DeleteObjectRequest arg0)
        throws AmazonClientException {
    }

    @Override
    public void deleteObject(String arg0, String arg1)
        throws AmazonClientException {
    }

    @Override
    public DeleteObjectsResult deleteObjects(DeleteObjectsRequest arg0)
        throws AmazonClientException {
        return null;
    }

    @Override
    public void deleteVersion(DeleteVersionRequest arg0)
        throws AmazonClientException {
    }

    @Override
    public void deleteVersion(String arg0, String arg1, String arg2)
        throws AmazonClientException {
    }

    @Override
    public boolean doesBucketExist(String arg0) throws AmazonClientException {
        return false;
    }

    @Override
    public boolean doesBucketExistV2(String s) throws SdkClientException {
        return false;
    }

    @Override
    public HeadBucketResult headBucket(HeadBucketRequest headBucketRequest) throws SdkClientException {
        return null;
    }

    @Override
    public URL generatePresignedUrl(GeneratePresignedUrlRequest arg0)
        throws AmazonClientException {
        return null;
    }

    @Override
    public URL generatePresignedUrl(String arg0, String arg1, Date arg2)
        throws AmazonClientException {
        return null;
    }

    @Override
    public URL generatePresignedUrl(String arg0, String arg1, Date arg2,
        HttpMethod arg3) throws AmazonClientException {
        return null;
    }

    @Override
    public AccessControlList getBucketAcl(String arg0)
        throws AmazonClientException {
        return null;
    }

    @Override
    public AccessControlList getBucketAcl(GetBucketAclRequest arg0)
        throws AmazonClientException {
        return null;
    }

    @Override
    public BucketCrossOriginConfiguration getBucketCrossOriginConfiguration(
        String arg0) {
        return null;
    }

    @Override
    public BucketCrossOriginConfiguration getBucketCrossOriginConfiguration(GetBucketCrossOriginConfigurationRequest getBucketCrossOriginConfigurationRequest) {
        return null;
    }

    @Override
    public BucketLifecycleConfiguration getBucketLifecycleConfiguration(
        String arg0) {
        return null;
    }

    @Override
    public BucketLifecycleConfiguration getBucketLifecycleConfiguration(GetBucketLifecycleConfigurationRequest getBucketLifecycleConfigurationRequest) {
        return null;
    }

    @Override
    public String getBucketLocation(String arg0) throws AmazonClientException {
        return null;
    }

    @Override
    public String getBucketLocation(GetBucketLocationRequest arg0)
        throws AmazonClientException {
        return null;
    }

    @Override
    public BucketLoggingConfiguration getBucketLoggingConfiguration(String arg0)
        throws AmazonClientException {
        return null;
    }

    @Override
    public BucketLoggingConfiguration getBucketLoggingConfiguration(GetBucketLoggingConfigurationRequest getBucketLoggingConfigurationRequest) throws SdkClientException {
        return null;
    }

    @Override
    public BucketNotificationConfiguration getBucketNotificationConfiguration(
        String arg0) throws AmazonClientException {
        return null;
    }

    @Override
    public BucketNotificationConfiguration getBucketNotificationConfiguration(GetBucketNotificationConfigurationRequest getBucketNotificationConfigurationRequest) throws SdkClientException {
        return null;
    }

    @Override
    public BucketPolicy getBucketPolicy(String arg0)
        throws AmazonClientException {
        return null;
    }

    @Override
    public BucketPolicy getBucketPolicy(GetBucketPolicyRequest arg0)
        throws AmazonClientException {
        return null;
    }

    @Override
    public BucketTaggingConfiguration getBucketTaggingConfiguration(String arg0) {
        return null;
    }

    @Override
    public BucketTaggingConfiguration getBucketTaggingConfiguration(GetBucketTaggingConfigurationRequest getBucketTaggingConfigurationRequest) {
        return null;
    }

    @Override
    public BucketVersioningConfiguration getBucketVersioningConfiguration(
        String arg0) throws AmazonClientException {
        return null;
    }

    @Override
    public BucketVersioningConfiguration getBucketVersioningConfiguration(GetBucketVersioningConfigurationRequest getBucketVersioningConfigurationRequest) throws SdkClientException {
        return null;
    }

    @Override
    public BucketWebsiteConfiguration getBucketWebsiteConfiguration(String arg0)
        throws AmazonClientException {
        return null;
    }

    @Override
    public BucketWebsiteConfiguration getBucketWebsiteConfiguration(
        GetBucketWebsiteConfigurationRequest arg0)
        throws AmazonClientException {
        return null;
    }

    @Override
    public S3ResponseMetadata getCachedResponseMetadata(
        AmazonWebServiceRequest arg0) {
        return null;
    }

    @Override
    public S3Object getObject(GetObjectRequest arg0)
        throws AmazonClientException {
        return new MkS3Object();
    }

    @Override
    public S3Object getObject(String arg0, String arg1)
        throws AmazonClientException {
        return null;
    }

    @Override
    public ObjectMetadata getObject(GetObjectRequest arg0, File arg1)
        throws AmazonClientException {
        return null;
    }

    @Override
    public String getObjectAsString(String s, String s1) throws SdkClientException {
        return null;
    }

    @Override
    public GetObjectTaggingResult getObjectTagging(GetObjectTaggingRequest getObjectTaggingRequest) {
        return null;
    }

    @Override
    public SetObjectTaggingResult setObjectTagging(SetObjectTaggingRequest setObjectTaggingRequest) {
        return null;
    }

    @Override
    public DeleteObjectTaggingResult deleteObjectTagging(DeleteObjectTaggingRequest deleteObjectTaggingRequest) {
        return null;
    }

    @Override
    public AccessControlList getObjectAcl(String arg0, String arg1)
        throws AmazonClientException {
        return null;
    }

    @Override
    public AccessControlList getObjectAcl(String arg0, String arg1, String arg2)
        throws AmazonClientException {
        return null;
    }

    @Override
    public AccessControlList getObjectAcl(GetObjectAclRequest getObjectAclRequest) throws SdkClientException {
        return null;
    }

    @Override
    public ObjectMetadata getObjectMetadata(GetObjectMetadataRequest arg0)
        throws AmazonClientException {
        return null;
    }

    @Override
    public ObjectMetadata getObjectMetadata(String arg0, String arg1)
        throws AmazonClientException {
        return null;
    }

    @Override
    public Owner getS3AccountOwner() throws AmazonClientException {
        return null;
    }

    @Override
    public Owner getS3AccountOwner(GetS3AccountOwnerRequest getS3AccountOwnerRequest) throws SdkClientException {
        return null;
    }

    @Override
    public InitiateMultipartUploadResult initiateMultipartUpload(
        InitiateMultipartUploadRequest arg0) throws AmazonClientException {
        return null;
    }

    @Override
    public List<Bucket> listBuckets() throws AmazonClientException {
        return null;
    }

    @Override
    public List<Bucket> listBuckets(ListBucketsRequest arg0)
        throws AmazonClientException {
        return null;
    }

    @Override
    public MultipartUploadListing listMultipartUploads(
        ListMultipartUploadsRequest arg0) throws AmazonClientException {
        return null;
    }

    @Override
    public ObjectListing listNextBatchOfObjects(ObjectListing arg0)
        throws AmazonClientException {
        return null;
    }

    @Override
    public ObjectListing listNextBatchOfObjects(ListNextBatchOfObjectsRequest listNextBatchOfObjectsRequest) throws SdkClientException {
        return null;
    }

    @Override
    public VersionListing listNextBatchOfVersions(VersionListing arg0)
        throws AmazonClientException {
        return null;
    }

    @Override
    public VersionListing listNextBatchOfVersions(ListNextBatchOfVersionsRequest listNextBatchOfVersionsRequest) throws SdkClientException {
        return null;
    }

    @Override
    public ObjectListing listObjects(String arg0) throws AmazonClientException {
        return null;
    }

    @Override
    public ObjectListing listObjects(ListObjectsRequest arg0)
        throws AmazonClientException {
        return null;
    }

    @Override
    public ListObjectsV2Result listObjectsV2(String s) throws SdkClientException {
        return null;
    }

    @Override
    public ListObjectsV2Result listObjectsV2(String s, String s1) throws SdkClientException {
        return null;
    }

    @Override
    public ListObjectsV2Result listObjectsV2(ListObjectsV2Request listObjectsV2Request) throws SdkClientException {
        return null;
    }

    @Override
    public ObjectListing listObjects(String arg0, String arg1)
        throws AmazonClientException {
        return null;
    }

    @Override
    public PartListing listParts(ListPartsRequest arg0)
        throws AmazonClientException {
        return null;
    }

    @Override
    public VersionListing listVersions(ListVersionsRequest arg0)
        throws AmazonClientException {
        return null;
    }

    @Override
    public VersionListing listVersions(String arg0, String arg1)
        throws AmazonClientException {
        return null;
    }

    @Override
    public VersionListing listVersions(String arg0, String arg1, String arg2,
        String arg3, String arg4, Integer arg5) throws AmazonClientException {
        return null;
    }

    @Override
    public PutObjectResult putObject(PutObjectRequest arg0)
        throws AmazonClientException {
        return null;
    }

    @Override
    public PutObjectResult putObject(String arg0, String arg1, File arg2)
        throws AmazonClientException {
        return null;
    }

    @Override
    public PutObjectResult putObject(String arg0, String arg1,
        InputStream arg2, ObjectMetadata arg3) throws AmazonClientException {
        return null;
    }

    @Override
    public PutObjectResult putObject(String s, String s1, String s2) throws SdkClientException {
        return null;
    }

    @Override
    public void restoreObject(RestoreObjectRequest arg0)
        throws AmazonServiceException {
    }

    @Override
    public RestoreObjectResult restoreObjectV2(RestoreObjectRequest restoreObjectRequest) throws AmazonServiceException {
        return null;
    }

    @Override
    @Deprecated
    public void restoreObject(String s, String s1, int i) throws AmazonServiceException {

    }

    @Override
    public void enableRequesterPays(final String bucket)
        throws AmazonClientException {
    }

    @Override
    public void disableRequesterPays(final String bucket)
        throws AmazonClientException {
    }

    @Override
    public boolean isRequesterPaysEnabled(final String bucket)
        throws AmazonClientException {
        return false;
    }

    @Override
    public void setRequestPaymentConfiguration(SetRequestPaymentConfigurationRequest setRequestPaymentConfigurationRequest) {
    }

    @Override
    public void setBucketReplicationConfiguration(String s, BucketReplicationConfiguration bucketReplicationConfiguration) throws SdkClientException {
    }

    @Override
    public void setBucketReplicationConfiguration(SetBucketReplicationConfigurationRequest setBucketReplicationConfigurationRequest) throws SdkClientException {
    }

    @Override
    public BucketReplicationConfiguration getBucketReplicationConfiguration(String s) throws SdkClientException {
        return null;
    }

    @Override
    public BucketReplicationConfiguration getBucketReplicationConfiguration(GetBucketReplicationConfigurationRequest getBucketReplicationConfigurationRequest) throws SdkClientException {
        return null;
    }

    @Override
    public void deleteBucketReplicationConfiguration(String s) throws SdkClientException {
    }

    @Override
    public void deleteBucketReplicationConfiguration(DeleteBucketReplicationConfigurationRequest deleteBucketReplicationConfigurationRequest) throws SdkClientException {
    }

    @Override
    public boolean doesObjectExist(String s, String s1) throws SdkClientException {
        return false;
    }

    @Override
    public BucketAccelerateConfiguration getBucketAccelerateConfiguration(String s) throws SdkClientException {
        return null;
    }

    @Override
    public BucketAccelerateConfiguration getBucketAccelerateConfiguration(GetBucketAccelerateConfigurationRequest getBucketAccelerateConfigurationRequest) throws SdkClientException {
        return null;
    }

    @Override
    public void setBucketAccelerateConfiguration(String s, BucketAccelerateConfiguration bucketAccelerateConfiguration) throws SdkClientException {
    }

    @Override
    public void setBucketAccelerateConfiguration(SetBucketAccelerateConfigurationRequest setBucketAccelerateConfigurationRequest) throws SdkClientException {
    }

    @Override
    public DeleteBucketMetricsConfigurationResult deleteBucketMetricsConfiguration(String s, String s1) throws SdkClientException {
        return null;
    }

    @Override
    public DeleteBucketMetricsConfigurationResult deleteBucketMetricsConfiguration(DeleteBucketMetricsConfigurationRequest deleteBucketMetricsConfigurationRequest) throws SdkClientException {
        return null;
    }

    @Override
    public GetBucketMetricsConfigurationResult getBucketMetricsConfiguration(String s, String s1) throws SdkClientException {
        return null;
    }

    @Override
    public GetBucketMetricsConfigurationResult getBucketMetricsConfiguration(GetBucketMetricsConfigurationRequest getBucketMetricsConfigurationRequest) throws SdkClientException {
        return null;
    }

    @Override
    public SetBucketMetricsConfigurationResult setBucketMetricsConfiguration(String s, MetricsConfiguration metricsConfiguration) throws SdkClientException {
        return null;
    }

    @Override
    public SetBucketMetricsConfigurationResult setBucketMetricsConfiguration(SetBucketMetricsConfigurationRequest setBucketMetricsConfigurationRequest) throws SdkClientException {
        return null;
    }

    @Override
    public ListBucketMetricsConfigurationsResult listBucketMetricsConfigurations(ListBucketMetricsConfigurationsRequest listBucketMetricsConfigurationsRequest) throws SdkClientException {
        return null;
    }

    @Override
    public DeleteBucketOwnershipControlsResult deleteBucketOwnershipControls(DeleteBucketOwnershipControlsRequest deleteBucketOwnershipControlsRequest) throws SdkClientException {
        return null;
    }

    @Override
    public GetBucketOwnershipControlsResult getBucketOwnershipControls(GetBucketOwnershipControlsRequest getBucketOwnershipControlsRequest) throws SdkClientException {
        return null;
    }

    @Override
    public SetBucketOwnershipControlsResult setBucketOwnershipControls(String s, OwnershipControls ownershipControls) throws SdkClientException {
        return null;
    }

    @Override
    public SetBucketOwnershipControlsResult setBucketOwnershipControls(SetBucketOwnershipControlsRequest setBucketOwnershipControlsRequest) throws SdkClientException {
        return null;
    }

    @Override
    public DeleteBucketAnalyticsConfigurationResult deleteBucketAnalyticsConfiguration(String s, String s1) throws SdkClientException {
        return null;
    }

    @Override
    public DeleteBucketAnalyticsConfigurationResult deleteBucketAnalyticsConfiguration(DeleteBucketAnalyticsConfigurationRequest deleteBucketAnalyticsConfigurationRequest) throws SdkClientException {
        return null;
    }

    @Override
    public GetBucketAnalyticsConfigurationResult getBucketAnalyticsConfiguration(String s, String s1) throws SdkClientException {
        return null;
    }

    @Override
    public GetBucketAnalyticsConfigurationResult getBucketAnalyticsConfiguration(GetBucketAnalyticsConfigurationRequest getBucketAnalyticsConfigurationRequest) throws SdkClientException {
        return null;
    }

    @Override
    public SetBucketAnalyticsConfigurationResult setBucketAnalyticsConfiguration(String s, AnalyticsConfiguration analyticsConfiguration) throws SdkClientException {
        return null;
    }

    @Override
    public SetBucketAnalyticsConfigurationResult setBucketAnalyticsConfiguration(SetBucketAnalyticsConfigurationRequest setBucketAnalyticsConfigurationRequest) throws SdkClientException {
        return null;
    }

    @Override
    public ListBucketAnalyticsConfigurationsResult listBucketAnalyticsConfigurations(ListBucketAnalyticsConfigurationsRequest listBucketAnalyticsConfigurationsRequest) throws SdkClientException {
        return null;
    }

    @Override
    public DeleteBucketIntelligentTieringConfigurationResult deleteBucketIntelligentTieringConfiguration(String s, String s1) throws SdkClientException {
        return null;
    }

    @Override
    public DeleteBucketIntelligentTieringConfigurationResult deleteBucketIntelligentTieringConfiguration(DeleteBucketIntelligentTieringConfigurationRequest deleteBucketIntelligentTieringConfigurationRequest) throws SdkClientException {
        return null;
    }

    @Override
    public GetBucketIntelligentTieringConfigurationResult getBucketIntelligentTieringConfiguration(String s, String s1) throws SdkClientException {
        return null;
    }

    @Override
    public GetBucketIntelligentTieringConfigurationResult getBucketIntelligentTieringConfiguration(GetBucketIntelligentTieringConfigurationRequest getBucketIntelligentTieringConfigurationRequest) throws SdkClientException {
        return null;
    }

    @Override
    public SetBucketIntelligentTieringConfigurationResult setBucketIntelligentTieringConfiguration(String s, IntelligentTieringConfiguration intelligentTieringConfiguration) throws SdkClientException {
        return null;
    }

    @Override
    public SetBucketIntelligentTieringConfigurationResult setBucketIntelligentTieringConfiguration(SetBucketIntelligentTieringConfigurationRequest setBucketIntelligentTieringConfigurationRequest) throws SdkClientException {
        return null;
    }

    @Override
    public ListBucketIntelligentTieringConfigurationsResult listBucketIntelligentTieringConfigurations(ListBucketIntelligentTieringConfigurationsRequest listBucketIntelligentTieringConfigurationsRequest) throws SdkClientException {
        return null;
    }

    @Override
    public DeleteBucketInventoryConfigurationResult deleteBucketInventoryConfiguration(String s, String s1) throws SdkClientException {
        return null;
    }

    @Override
    public DeleteBucketInventoryConfigurationResult deleteBucketInventoryConfiguration(DeleteBucketInventoryConfigurationRequest deleteBucketInventoryConfigurationRequest) throws SdkClientException {
        return null;
    }

    @Override
    public GetBucketInventoryConfigurationResult getBucketInventoryConfiguration(String s, String s1) throws SdkClientException {
        return null;
    }

    @Override
    public GetBucketInventoryConfigurationResult getBucketInventoryConfiguration(GetBucketInventoryConfigurationRequest getBucketInventoryConfigurationRequest) throws SdkClientException {
        return null;
    }

    @Override
    public SetBucketInventoryConfigurationResult setBucketInventoryConfiguration(String s, InventoryConfiguration inventoryConfiguration) throws SdkClientException {
        return null;
    }

    @Override
    public SetBucketInventoryConfigurationResult setBucketInventoryConfiguration(SetBucketInventoryConfigurationRequest setBucketInventoryConfigurationRequest) throws SdkClientException {
        return null;
    }

    @Override
    public ListBucketInventoryConfigurationsResult listBucketInventoryConfigurations(ListBucketInventoryConfigurationsRequest listBucketInventoryConfigurationsRequest) throws SdkClientException {
        return null;
    }

    @Override
    public DeleteBucketEncryptionResult deleteBucketEncryption(String s) throws SdkClientException {
        return null;
    }

    @Override
    public DeleteBucketEncryptionResult deleteBucketEncryption(DeleteBucketEncryptionRequest deleteBucketEncryptionRequest) throws SdkClientException {
        return null;
    }

    @Override
    public GetBucketEncryptionResult getBucketEncryption(String s) throws SdkClientException {
        return null;
    }

    @Override
    public GetBucketEncryptionResult getBucketEncryption(GetBucketEncryptionRequest getBucketEncryptionRequest) throws SdkClientException {
        return null;
    }

    @Override
    public SetBucketEncryptionResult setBucketEncryption(SetBucketEncryptionRequest setBucketEncryptionRequest) throws SdkClientException {
        return null;
    }

    @Override
    public SetPublicAccessBlockResult setPublicAccessBlock(SetPublicAccessBlockRequest setPublicAccessBlockRequest) {
        return null;
    }

    @Override
    public GetPublicAccessBlockResult getPublicAccessBlock(GetPublicAccessBlockRequest getPublicAccessBlockRequest) {
        return null;
    }

    @Override
    public DeletePublicAccessBlockResult deletePublicAccessBlock(DeletePublicAccessBlockRequest deletePublicAccessBlockRequest) {
        return null;
    }

    @Override
    public GetBucketPolicyStatusResult getBucketPolicyStatus(GetBucketPolicyStatusRequest getBucketPolicyStatusRequest) {
        return null;
    }

    @Override
    public SelectObjectContentResult selectObjectContent(SelectObjectContentRequest selectObjectContentRequest) throws SdkClientException {
        return null;
    }

    @Override
    public SetObjectLegalHoldResult setObjectLegalHold(SetObjectLegalHoldRequest setObjectLegalHoldRequest) {
        return null;
    }

    @Override
    public GetObjectLegalHoldResult getObjectLegalHold(GetObjectLegalHoldRequest getObjectLegalHoldRequest) {
        return null;
    }

    @Override
    public SetObjectLockConfigurationResult setObjectLockConfiguration(SetObjectLockConfigurationRequest setObjectLockConfigurationRequest) {
        return null;
    }

    @Override
    public GetObjectLockConfigurationResult getObjectLockConfiguration(GetObjectLockConfigurationRequest getObjectLockConfigurationRequest) {
        return null;
    }

    @Override
    public SetObjectRetentionResult setObjectRetention(SetObjectRetentionRequest setObjectRetentionRequest) {
        return null;
    }

    @Override
    public GetObjectRetentionResult getObjectRetention(GetObjectRetentionRequest getObjectRetentionRequest) {
        return null;
    }

    @Override
    public WriteGetObjectResponseResult writeGetObjectResponse(WriteGetObjectResponseRequest writeGetObjectResponseRequest) {
        return null;
    }

    @Override
    public PresignedUrlDownloadResult download(PresignedUrlDownloadRequest presignedUrlDownloadRequest) {
        return null;
    }

    @Override
    public void download(PresignedUrlDownloadRequest presignedUrlDownloadRequest, File file) {
    }

    @Override
    public PresignedUrlUploadResult upload(PresignedUrlUploadRequest presignedUrlUploadRequest) {
        return null;
    }

    @Override
    public void shutdown() {
    }

    @Override
    public Region getRegion() {
        return null;
    }

    @Override
    public String getRegionName() {
        return null;
    }

    @Override
    public URL getUrl(String s, String s1) {
        return null;
    }

    @Override
    public AmazonS3Waiters waiters() {
        return null;
    }

    @Override
    public void setBucketAcl(SetBucketAclRequest arg0)
        throws AmazonClientException {
    }

    @Override
    public void setBucketAcl(String arg0, AccessControlList arg1)
        throws AmazonClientException {
    }

    @Override
    public void setBucketAcl(String arg0, CannedAccessControlList arg1)
        throws AmazonClientException {
    }

    @Override
    public void setBucketCrossOriginConfiguration(
        SetBucketCrossOriginConfigurationRequest arg0) {
    }

    @Override
    public void setBucketCrossOriginConfiguration(String arg0,
        BucketCrossOriginConfiguration arg1) {
    }

    @Override
    public void setBucketLifecycleConfiguration(
        SetBucketLifecycleConfigurationRequest arg0) {
    }

    @Override
    public void setBucketLifecycleConfiguration(String arg0,
        BucketLifecycleConfiguration arg1) {
    }

    @Override
    public void setBucketLoggingConfiguration(
        SetBucketLoggingConfigurationRequest arg0)
        throws AmazonClientException {
    }

    @Override
    public void setBucketNotificationConfiguration(
        SetBucketNotificationConfigurationRequest arg0)
        throws AmazonClientException {
    }

    @Override
    public void setBucketNotificationConfiguration(String arg0,
        BucketNotificationConfiguration arg1) throws AmazonClientException {
    }

    @Override
    public void setBucketPolicy(SetBucketPolicyRequest arg0)
        throws AmazonClientException {
    }

    @Override
    public void setBucketPolicy(String arg0, String arg1)
        throws AmazonClientException {
    }

    @Override
    public void setBucketTaggingConfiguration(
        SetBucketTaggingConfigurationRequest arg0) {
    }

    @Override
    public void setBucketTaggingConfiguration(String arg0,
        BucketTaggingConfiguration arg1) {
    }

    @Override
    public void setBucketVersioningConfiguration(
        SetBucketVersioningConfigurationRequest arg0)
        throws AmazonClientException {
    }

    @Override
    public void setBucketWebsiteConfiguration(
        SetBucketWebsiteConfigurationRequest arg0)
        throws AmazonClientException {
    }

    @Override
    public void setBucketWebsiteConfiguration(String arg0,
        BucketWebsiteConfiguration arg1) throws AmazonClientException {
    }

    @Override
    public void setEndpoint(String arg0) {
    }

    @Override
    public void setObjectAcl(String arg0, String arg1, AccessControlList arg2)
        throws AmazonClientException {
    }

    @Override
    public void setObjectAcl(String arg0, String arg1,
        CannedAccessControlList arg2) throws AmazonClientException {
    }

    @Override
    public void setObjectAcl(String arg0, String arg1, String arg2,
        AccessControlList arg3) throws AmazonClientException {
    }

    @Override
    public void setObjectAcl(String arg0, String arg1, String arg2,
        CannedAccessControlList arg3) throws AmazonClientException {
    }

    @Override
    public void setObjectAcl(SetObjectAclRequest setObjectAclRequest) throws SdkClientException {
    }

    @Override
    public void setObjectRedirectLocation(String arg0, String arg1, String arg2)
        throws AmazonClientException {
    }

    @Override
    public void setRegion(com.amazonaws.regions.Region arg0)
        throws IllegalArgumentException {
    }

    @Override
    public void setS3ClientOptions(S3ClientOptions arg0) {
    }

    @Override
    public UploadPartResult uploadPart(UploadPartRequest arg0)
        throws AmazonClientException {
        return null;
    }

    public static class MkS3Object extends S3Object {
        @Override
        public S3ObjectInputStream getObjectContent() {
            return new S3ObjectInputStream(
                IOUtils.toInputStream("TXT"),
                new HttpGet()
            );
        }
    }

}
