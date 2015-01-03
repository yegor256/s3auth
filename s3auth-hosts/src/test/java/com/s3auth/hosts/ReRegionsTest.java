package com.s3auth.hosts;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.jcabi.dynamo.Credentials;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.retry.ReRegion;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class ReRegionsTest {
    @Test
    public void createCreatesReRegion() {
        final Credentials credentials = Mockito.mock(Credentials.class);
        final ReRegions reRegions = new ReRegions(credentials);
        final Region region = reRegions.create();
        Assert.assertNotNull(region);
        Assert.assertTrue(region instanceof ReRegion);
    }

    @Test
    public void awsReturnsCredentialsAws() {
        final AmazonDynamoDB aws = Mockito.mock(AmazonDynamoDB.class);
        final Credentials credentials = Mockito.mock(Credentials.class);
        Mockito.when(credentials.aws()).thenReturn(aws);
        final ReRegions reRegions = new ReRegions(credentials);
        Assert.assertSame(aws, reRegions.aws());
    }
}
