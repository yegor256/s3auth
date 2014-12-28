package com.s3auth.hosts;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.jcabi.aspects.Immutable;
import com.jcabi.dynamo.Region;

@Immutable
public interface RegionFactory {
    Region createRegion();

    AmazonDynamoDB aws();
}
