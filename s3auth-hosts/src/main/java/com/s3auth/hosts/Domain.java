/*
 * Copyright (c) 2012-2022, Yegor Bugayenko
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

import com.jcabi.aspects.Immutable;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

/**
 * Configuration of a single domain.
 *
 * <p>Implementation must be immutable and thread-safe.
 *
 * @since 0.0.1
 */
@Domain.Valid
@Immutable
public interface Domain {

    /**
     * Name of domain.
     * @return Full name of domain
     * @see <a href="http://en.wikipedia.org/wiki/Domain_name">Domain Name</a>
     */
    String name();

    /**
     * Key.
     * @return AWS key
     */
    String key();

    /**
     * Secret key.
     * @return AWS secret key
     */
    String secret();

    /**
     * Bucket name.
     * @return S3 bucket name
     */
    String bucket();

    /**
     * Region of S3 bucket.
     * @return Region name/endpoint, e.g. "s3-us-west-1"
     * @see <a href="http://docs.amazonwebservices.com/general/latest/gr/rande.html#s3_region">S3 Regions</a>
     */
    String region();

    /**
     * Syslog host and port of domain.
     * @return Syslog host and port
     */
    String syslog();

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = Domain.Validator.class)
    @Documented
    @interface Valid {
        /**
         * Message of the validation error.
         */
        String message() default "invalid domain";
        /**
         * Groups.
         */
        Class<?>[] groups() default { };
        /**
         * Payload.
         */
        Class<? extends Payload>[] payload() default { };
    }

    /**
     * Validator of Domain.
     */
    @SuppressWarnings("PMD.NPathComplexity")
    class Validator implements ConstraintValidator<Domain.Valid, Domain> {
        @Override
        public void initialize(final Domain.Valid annotation) {
            //nothing to do
        }
        // @checkstyle CyclomaticComplexity (60 lines)
        // @checkstyle ExecutableStatementCount (60 lines)
        @Override
        public boolean isValid(final Domain domain,
            final ConstraintValidatorContext ctx) {
            boolean isValid = true;
            if (domain.name() == null) {
                ctx.buildConstraintViolationWithTemplate(
                    "domain name is mandatory and can't be NULL"
                ).addConstraintViolation();
                isValid = false;
            } else if (!domain.name().matches("\\s*[a-zA-Z0-9\\-\\.]+\\s*")) {
                ctx.buildConstraintViolationWithTemplate(
                    String.format("invalid domain name '%s'", domain.name())
                ).addPropertyNode("name").addConstraintViolation();
                isValid = false;
            }
            if (domain.key() == null) {
                ctx.buildConstraintViolationWithTemplate(
                    "AWS key is mandatory and can't be NULL"
                ).addConstraintViolation();
                isValid = false;
            } else if (!domain.key().matches("\\s*[A-Z0-9]{20}\\s*")) {
                ctx.buildConstraintViolationWithTemplate(
                    String.format("invalid AWS key '%s'", domain.key())
                ).addPropertyNode("key").addConstraintViolation();
                isValid = false;
            }
            if (domain.region() == null) {
                ctx.buildConstraintViolationWithTemplate(
                    "AWS S3 region is mandatory and can't be NULL"
                ).addConstraintViolation();
                isValid = false;
            } else if (!domain.region().matches("s3[a-z0-9\\-]*")) {
                ctx.buildConstraintViolationWithTemplate(
                    String.format("invalid AWS S3 region '%s'", domain.region())
                ).addPropertyNode("region").addConstraintViolation();
                isValid = false;
            }
            if (domain.secret() == null) {
                ctx.buildConstraintViolationWithTemplate(
                    "AWS secret key is mandatory and can't be NULL"
                ).addConstraintViolation();
                isValid = false;
            } else if (!domain.secret()
                .matches("\\s*[a-zA-Z0-9\\+/]{40}\\s*")) {
                ctx.buildConstraintViolationWithTemplate(
                    String.format("invalid AWS secret '%s'", domain.secret())
                ).addPropertyNode("secret").addConstraintViolation();
                isValid = false;
            }
            if (domain.syslog() != null
                && !domain.syslog()
                    .matches("\\s*[a-zA-Z0-9\\-\\.]+(:\\d+)?\\s*")
            ) {
                ctx.buildConstraintViolationWithTemplate(
                    String.format("invalid syslog host '%s'", domain.syslog())
                ).addPropertyNode("syslog").addConstraintViolation();
                isValid = false;
            }
            return isValid;
        }
    }

}
