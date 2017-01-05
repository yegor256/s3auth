/**
 * Copyright (c) 2012-2017, s3auth.com
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
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.S3ResponseMetadata;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.BucketCrossOriginConfiguration;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.BucketLoggingConfiguration;
import com.amazonaws.services.s3.model.BucketNotificationConfiguration;
import com.amazonaws.services.s3.model.BucketPolicy;
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
import com.amazonaws.services.s3.model.DeleteBucketCrossOriginConfigurationRequest;
import com.amazonaws.services.s3.model.DeleteBucketLifecycleConfigurationRequest;
import com.amazonaws.services.s3.model.DeleteBucketPolicyRequest;
import com.amazonaws.services.s3.model.DeleteBucketRequest;
import com.amazonaws.services.s3.model.DeleteBucketTaggingConfigurationRequest;
import com.amazonaws.services.s3.model.DeleteBucketWebsiteConfigurationRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.DeleteVersionRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetBucketAclRequest;
import com.amazonaws.services.s3.model.GetBucketLocationRequest;
import com.amazonaws.services.s3.model.GetBucketPolicyRequest;
import com.amazonaws.services.s3.model.GetBucketWebsiteConfigurationRequest;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ListBucketsRequest;
import com.amazonaws.services.s3.model.ListMultipartUploadsRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ListPartsRequest;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.MultipartUploadListing;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Owner;
import com.amazonaws.services.s3.model.PartListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.s3.model.RestoreObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.SetBucketAclRequest;
import com.amazonaws.services.s3.model.SetBucketCrossOriginConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketLifecycleConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketLoggingConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketNotificationConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketPolicyRequest;
import com.amazonaws.services.s3.model.SetBucketTaggingConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketWebsiteConfigurationRequest;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.amazonaws.services.s3.model.VersionListing;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;

/**
 * AmazonS3 Mock (without Mockito).
 *
 * @author Carlos Alexandro Becker (caarlos0@gmail.com)
 * @version $Id$
 * @checkstyle ThrowsCount (1000 lines)
 * @checkstyle RedundantThrows (1000 lines)
 * @checkstyle ParameterName (1000 lines)
 * @checkstyle DesignForExtension (1000 lines)
 * @checkstyle FinalParameters (1000 lines)
 * @checkstyle MethodName (1000 lines)
 * @checkstyle ParameterNumber (1000 lines)
 * @checkstyle BracketsStructure (1000 lines)
 * @checkstyle LineLength (1000 lines)
 * @checkstyle ClassFanOutComplexity (1000 lines)
 * @checkstyle MethodCountCheck (1000 lines)
 */
@SuppressWarnings("PMD")
public class MkAmazonS3 implements AmazonS3 {
    @Override
    public void abortMultipartUpload(AbortMultipartUploadRequest arg0)
        throws AmazonClientException, AmazonServiceException {
    }

    @Override
    public void changeObjectStorageClass(String arg0, String arg1,
        StorageClass arg2) throws AmazonClientException, AmazonServiceException {
    }

    @Override
    public CompleteMultipartUploadResult completeMultipartUpload(
        CompleteMultipartUploadRequest arg0) throws AmazonClientException,
        AmazonServiceException {
        return null;
    }

    @Override
    public CopyObjectResult copyObject(CopyObjectRequest arg0)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public CopyObjectResult copyObject(String arg0, String arg1, String arg2,
        String arg3) throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public CopyPartResult copyPart(CopyPartRequest arg0)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public Bucket createBucket(CreateBucketRequest arg0)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public Bucket createBucket(String arg0) throws AmazonClientException,
        AmazonServiceException {
        return null;
    }

    @Override
    public Bucket createBucket(String arg0, Region arg1)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public Bucket createBucket(String arg0, String arg1)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public void deleteBucket(DeleteBucketRequest arg0)
        throws AmazonClientException, AmazonServiceException {
    }

    @Override
    public void deleteBucket(String arg0) throws AmazonClientException,
        AmazonServiceException {
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
    public void deleteBucketPolicy(String arg0) throws AmazonClientException,
        AmazonServiceException {
    }

    @Override
    public void deleteBucketPolicy(DeleteBucketPolicyRequest arg0)
        throws AmazonClientException, AmazonServiceException {
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
        throws AmazonClientException, AmazonServiceException {
    }

    @Override
    public void deleteBucketWebsiteConfiguration(
        DeleteBucketWebsiteConfigurationRequest arg0)
        throws AmazonClientException, AmazonServiceException {
    }

    @Override
    public void deleteObject(DeleteObjectRequest arg0)
        throws AmazonClientException, AmazonServiceException {
    }

    @Override
    public void deleteObject(String arg0, String arg1)
        throws AmazonClientException, AmazonServiceException {
    }

    @Override
    public DeleteObjectsResult deleteObjects(DeleteObjectsRequest arg0)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public void deleteVersion(DeleteVersionRequest arg0)
        throws AmazonClientException, AmazonServiceException {
    }

    @Override
    public void deleteVersion(String arg0, String arg1, String arg2)
        throws AmazonClientException, AmazonServiceException {
    }

    @Override
    public boolean doesBucketExist(String arg0) throws AmazonClientException,
        AmazonServiceException {
        return false;
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
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public AccessControlList getBucketAcl(GetBucketAclRequest arg0)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public BucketCrossOriginConfiguration getBucketCrossOriginConfiguration(
        String arg0) {
        return null;
    }

    @Override
    public BucketLifecycleConfiguration getBucketLifecycleConfiguration(
        String arg0) {
        return null;
    }

    @Override
    public String getBucketLocation(String arg0) throws AmazonClientException,
        AmazonServiceException {
        return null;
    }

    @Override
    public String getBucketLocation(GetBucketLocationRequest arg0)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public BucketLoggingConfiguration getBucketLoggingConfiguration(String arg0)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public BucketNotificationConfiguration getBucketNotificationConfiguration(
        String arg0) throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public BucketPolicy getBucketPolicy(String arg0)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public BucketPolicy getBucketPolicy(GetBucketPolicyRequest arg0)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public BucketTaggingConfiguration getBucketTaggingConfiguration(String arg0) {
        return null;
    }

    @Override
    public BucketVersioningConfiguration getBucketVersioningConfiguration(
        String arg0) throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public BucketWebsiteConfiguration getBucketWebsiteConfiguration(String arg0)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public BucketWebsiteConfiguration getBucketWebsiteConfiguration(
        GetBucketWebsiteConfigurationRequest arg0)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public S3ResponseMetadata getCachedResponseMetadata(
        AmazonWebServiceRequest arg0) {
        return null;
    }

    @Override
    public S3Object getObject(GetObjectRequest arg0)
        throws AmazonClientException, AmazonServiceException {
        return new MkS3Object();
    }

    @Override
    public S3Object getObject(String arg0, String arg1)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public ObjectMetadata getObject(GetObjectRequest arg0, File arg1)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public AccessControlList getObjectAcl(String arg0, String arg1)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public AccessControlList getObjectAcl(String arg0, String arg1, String arg2)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public ObjectMetadata getObjectMetadata(GetObjectMetadataRequest arg0)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public ObjectMetadata getObjectMetadata(String arg0, String arg1)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public Owner getS3AccountOwner() throws AmazonClientException,
        AmazonServiceException {
        return null;
    }

    @Override
    public InitiateMultipartUploadResult initiateMultipartUpload(
        InitiateMultipartUploadRequest arg0) throws AmazonClientException,
        AmazonServiceException {
        return null;
    }

    @Override
    public List<Bucket> listBuckets() throws AmazonClientException,
        AmazonServiceException {
        return null;
    }

    @Override
    public List<Bucket> listBuckets(ListBucketsRequest arg0)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public MultipartUploadListing listMultipartUploads(
        ListMultipartUploadsRequest arg0) throws AmazonClientException,
        AmazonServiceException {
        return null;
    }

    @Override
    public ObjectListing listNextBatchOfObjects(ObjectListing arg0)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public VersionListing listNextBatchOfVersions(VersionListing arg0)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public ObjectListing listObjects(String arg0) throws AmazonClientException,
        AmazonServiceException {
        return null;
    }

    @Override
    public ObjectListing listObjects(ListObjectsRequest arg0)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public ObjectListing listObjects(String arg0, String arg1)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public PartListing listParts(ListPartsRequest arg0)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public VersionListing listVersions(ListVersionsRequest arg0)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public VersionListing listVersions(String arg0, String arg1)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public VersionListing listVersions(String arg0, String arg1, String arg2,
        String arg3, String arg4, Integer arg5) throws AmazonClientException,
        AmazonServiceException {
        return null;
    }

    @Override
    public PutObjectResult putObject(PutObjectRequest arg0)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public PutObjectResult putObject(String arg0, String arg1, File arg2)
        throws AmazonClientException, AmazonServiceException {
        return null;
    }

    @Override
    public PutObjectResult putObject(String arg0, String arg1,
        InputStream arg2, ObjectMetadata arg3) throws AmazonClientException,
        AmazonServiceException {
        return null;
    }

    @Override
    public void restoreObject(RestoreObjectRequest arg0)
        throws AmazonServiceException {
    }

    @Override
    public void restoreObject(String arg0, String arg1, int arg2)
        throws AmazonServiceException {
    }

    @Override
    public void enableRequesterPays(final String bucket)
        throws AmazonServiceException, AmazonClientException {
    }

    @Override
    public void disableRequesterPays(final String bucket)
        throws AmazonServiceException, AmazonClientException {
    }

    @Override
    public boolean isRequesterPaysEnabled(final String bucket)
        throws AmazonServiceException, AmazonClientException {
        return false;
    }

    @Override
    public void setBucketAcl(SetBucketAclRequest arg0)
        throws AmazonClientException, AmazonServiceException {
    }

    @Override
    public void setBucketAcl(String arg0, AccessControlList arg1)
        throws AmazonClientException, AmazonServiceException {
    }

    @Override
    public void setBucketAcl(String arg0, CannedAccessControlList arg1)
        throws AmazonClientException, AmazonServiceException {
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
        throws AmazonClientException, AmazonServiceException {
    }

    @Override
    public void setBucketNotificationConfiguration(
        SetBucketNotificationConfigurationRequest arg0)
        throws AmazonClientException, AmazonServiceException {
    }

    @Override
    public void setBucketNotificationConfiguration(String arg0,
        BucketNotificationConfiguration arg1) throws AmazonClientException,
        AmazonServiceException {
    }

    @Override
    public void setBucketPolicy(SetBucketPolicyRequest arg0)
        throws AmazonClientException, AmazonServiceException {
    }

    @Override
    public void setBucketPolicy(String arg0, String arg1)
        throws AmazonClientException, AmazonServiceException {
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
        throws AmazonClientException, AmazonServiceException {
    }

    @Override
    public void setBucketWebsiteConfiguration(
        SetBucketWebsiteConfigurationRequest arg0)
        throws AmazonClientException, AmazonServiceException {
    }

    @Override
    public void setBucketWebsiteConfiguration(String arg0,
        BucketWebsiteConfiguration arg1) throws AmazonClientException,
        AmazonServiceException {
    }

    @Override
    public void setEndpoint(String arg0) {
    }

    @Override
    public void setObjectAcl(String arg0, String arg1, AccessControlList arg2)
        throws AmazonClientException, AmazonServiceException {
    }

    @Override
    public void setObjectAcl(String arg0, String arg1,
        CannedAccessControlList arg2) throws AmazonClientException,
        AmazonServiceException {
    }

    @Override
    public void setObjectAcl(String arg0, String arg1, String arg2,
        AccessControlList arg3) throws AmazonClientException,
        AmazonServiceException {
    }

    @Override
    public void setObjectAcl(String arg0, String arg1, String arg2,
        CannedAccessControlList arg3) throws AmazonClientException,
        AmazonServiceException {
    }

    @Override
    public void setObjectRedirectLocation(String arg0, String arg1, String arg2)
        throws AmazonClientException, AmazonServiceException {
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
        throws AmazonClientException, AmazonServiceException {
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
