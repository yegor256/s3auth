/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Immutable;
import com.jcabi.urn.URN;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

/**
 * Single user.
 *
 * <p>Implementation must be immutable and thread-safe.
 *
 * @since 0.0.1
 */
@User.Valid
@Immutable
public interface User {

    /**
     * Anonymous User.
     */
    User ANONYMOUS = new User() {
        @Override
        public URN identity() {
            return URN.create("urn:anonymous:0");
        }

        @Override
        public String name() {
            return "Mr. Anonymous";
        }

        @Override
        public URI photo() {
            return URI.create("https://www.s3auth.com/images/unknown.png");
        }
    };

    /**
     * Unique name of it.
     * @return Unique name as URN
     */
    URN identity();

    /**
     * Full name to display.
     * @return Full name
     */
    String name();

    /**
     * Photo.
     * @return URL of the image
     */
    URI photo();

    /**
     * Valid User.
     *
     * @since 0.0.1
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = User.Validator.class)
    @Documented
    @interface Valid {
        /**
         * Message of the validation error.
         * @return Message
         */
        String message() default "invalid user";

        /**
         * Groups.
         * @return Groups
         */
        Class<?>[] groups() default { };

        /**
         * Payload.
         * @return Payload
         */
        Class<? extends Payload>[] payload() default { };
    }

    /**
     * Validator of User.
     *
     * @since 0.0.1
     */
    class Validator implements ConstraintValidator<User.Valid, User> {
        @Override
        public void initialize(final User.Valid annotation) {
            // nothing to do
        }

        @Override
        public boolean isValid(final User user,
            final ConstraintValidatorContext ctx) {
            boolean valid = true;
            final String nid = user.identity().nid();
            if (!"facebook".equals(nid)
                && !"google".equals(nid)
                && !"github".equals(nid)
                && !"test".equals(nid)) {
                ctx.buildConstraintViolationWithTemplate(
                    String.format("invalid NID of URN: %s", user.identity())
                ).addPropertyNode("identity").addConstraintViolation();
                valid = false;
            }
            if (!user.identity().nss().matches("\\d+")) {
                ctx.buildConstraintViolationWithTemplate(
                    String.format("invalid NSS of URN: %s", user.identity())
                ).addPropertyNode("identity").addConstraintViolation();
                valid = false;
            }
            return valid;
        }
    }

}
