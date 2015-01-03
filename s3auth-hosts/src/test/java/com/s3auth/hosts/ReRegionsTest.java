/**
 * Copyright (c) 2012-2014, s3auth.com
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

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.jcabi.dynamo.Credentials;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.retry.ReRegion;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test class for ReRegions.
 * @author Dmitri Pisarenko (dp@altruix.co)
 * @version $Id$
 */
public final class ReRegionsTest {
    /**
     * This test verifies that the reRegions.create method returns a non-null
     *  ReRegion.
     */
    @Test
    public void createCreatesReRegion() {
        final Credentials credentials = Mockito.mock(Credentials.class);
        final ReRegions reRegions = new ReRegions(credentials);
        final Region region = reRegions.create();
        Assert.assertNotNull(region);
        Assert.assertTrue(region instanceof ReRegion);
    }

    /**
     * This test verifies that ReRegions.aws returns the result of the
     *  credentials.aws call.
     */
    @Test
    public void awsReturnsCredentialsAws() {
        final AmazonDynamoDB aws = Mockito.mock(AmazonDynamoDB.class);
        final Credentials credentials = Mockito.mock(Credentials.class);
        Mockito.when(credentials.aws()).thenReturn(aws);
        final ReRegions reRegions = new ReRegions(credentials);
        Assert.assertSame(aws, reRegions.aws());
    }
}
