/**
 * Copyright (c) 2012, s3auth.com
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
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
@Domain.Valid
public interface Domain {

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = Domain.Validator.class)
    @Documented
    public @interface Valid {
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
    class Validator implements ConstraintValidator<Domain.Valid, Domain> {
        /**
         * {@inheritDoc}
         */
        @Override
        public void initialize(final Domain.Valid annotation) {
            //nothing to do
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isValid(final Domain domain,
            final ConstraintValidatorContext ctx) {
            boolean isValid = true;
            if (domain.name() == null) {
                ctx.buildConstraintViolationWithTemplate(
                    "domain name is mandatory and can't be NULL"
                ).addConstraintViolation();
                isValid = false;
            } else if (!domain.name().matches("[a-zA-Z0-9\\-\\.]+")) {
                ctx.buildConstraintViolationWithTemplate(
                    String.format("invalid domain name '%s'", domain.name())
                ).addNode("name").addConstraintViolation();
                isValid = false;
            }
            if (domain.key() == null) {
                ctx.buildConstraintViolationWithTemplate(
                    "AWS key is mandatory and can't be NULL"
                ).addConstraintViolation();
                isValid = false;
            } else if (!domain.key().matches("[A-Z0-9]{20}")) {
                ctx.buildConstraintViolationWithTemplate(
                    String.format("invalid AWS key '%s'", domain.key())
                ).addNode("key").addConstraintViolation();
                isValid = false;
            }
            if (domain.secret() == null) {
                ctx.buildConstraintViolationWithTemplate(
                    "AWS secret key is mandatory and can't be NULL"
                ).addConstraintViolation();
                isValid = false;
            } else if (!domain.secret().matches("[a-zA-Z0-9\\+/]{40}")) {
                ctx.buildConstraintViolationWithTemplate(
                    String.format("invalid AWS secret '%s'", domain.secret())
                ).addNode("secret").addConstraintViolation();
                isValid = false;
            }
            return isValid;
        }
    }

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

}
