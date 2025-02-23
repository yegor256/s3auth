/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link Domain}.
 * @since 0.0.1
 */
final class DomainTest {

    @Test
    void acceptsValidSyslog() {
        final ConstraintValidatorContext ctx =
            Mockito.mock(ConstraintValidatorContext.class);
        final ConstraintViolationBuilder builder =
            Mockito.mock(ConstraintViolationBuilder.class);
        Mockito.doReturn(builder).when(ctx)
            .buildConstraintViolationWithTemplate(Mockito.anyString());
        Mockito.doReturn(Mockito.mock(NodeBuilderCustomizableContext.class))
            .when(builder).addPropertyNode(Mockito.anyString());
        final Domain domain = new DomainMocker().init()
            .withSyslog("sys-log.s3auth.com:514").mock();
        MatcherAssert.assertThat(
            new Domain.Validator().isValid(domain, ctx),
            Matchers.is(true)
        );
    }

    @Test
    void rejectsInvalidSyslog() {
        final ConstraintValidatorContext ctx =
            Mockito.mock(ConstraintValidatorContext.class);
        final ConstraintViolationBuilder builder =
            Mockito.mock(ConstraintViolationBuilder.class);
        Mockito.doReturn(builder).when(ctx)
            .buildConstraintViolationWithTemplate(Mockito.anyString());
        Mockito.doReturn(Mockito.mock(NodeBuilderCustomizableContext.class))
            .when(builder).addPropertyNode(Mockito.anyString());
        final Domain domain = new DomainMocker().init()
            .withSyslog("!?@?#.com:ba14").mock();
        MatcherAssert.assertThat(
            new Domain.Validator().isValid(domain, ctx),
            Matchers.is(false)
        );
    }

}
