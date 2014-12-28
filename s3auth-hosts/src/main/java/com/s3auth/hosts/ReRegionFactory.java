package com.s3auth.hosts;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.jcabi.dynamo.Credentials;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.retry.ReRegion;

public class ReRegionFactory implements RegionFactory {
    /**
     * JCabi-Dynamo access credentials.
     */
    private final Credentials credentials;

    @Override
    public Region createRegion() {
        return new ReRegion(new Region.Simple(credentials));
    }

    @Override
    public AmazonDynamoDB aws() {
        return credentials.aws();
    }

    public ReRegionFactory(final Credentials credentials) {
        this.credentials = credentials;
    }
}
