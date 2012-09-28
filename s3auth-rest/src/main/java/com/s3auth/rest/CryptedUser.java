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
package com.s3auth.rest;

import com.rexsl.core.Manifests;
import com.s3auth.hosts.User;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.URI;
import javax.validation.constraints.NotNull;
import org.apache.commons.codec.binary.Base32;

/**
 * Crypted user.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
public final class CryptedUser implements User {

    /**
     * Security salt.
     */
    private static final String SALT = Manifests.read("S3Auth-SecuritySalt");

    /**
     * Base32 encoder/decoder.
     */
    private static final Base32 CODER = new Base32(80, new byte[] {}, true);

    /**
     * The user.
     */
    private final transient User user;

    /**
     * Thrown by {@link #valueOf(String)} if we can't decrypt.
     */
    public static class DecryptionException extends Exception {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 0x7529FA781EDA1479L;
        /**
         * Public ctor.
         * @param cause The cause of it
         */
        public DecryptionException(@NotNull final String cause) {
            super(cause);
        }
        /**
         * Public ctor.
         * @param cause The cause of it
         */
        public DecryptionException(@NotNull final Throwable cause) {
            super(cause);
        }
    }

    /**
     * Public ctor.
     * @param usr The user to encapsulate
     */
    public CryptedUser(@NotNull final User usr) {
        this.user = usr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String identity() {
        return this.user.identity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return this.user.name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI photo() {
        return this.user.photo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ByteArrayOutputStream data = new ByteArrayOutputStream();
        try {
            final DataOutputStream stream = new DataOutputStream(data);
            stream.writeUTF(this.identity());
            stream.writeUTF(this.name());
            stream.writeUTF(this.photo().toString());
            stream.writeUTF(CryptedUser.SALT);
        } catch (java.io.IOException ex) {
            throw new IllegalArgumentException(ex);
        }
        return CryptedUser.CODER.encodeToString(
            CryptedUser.xor(data.toByteArray())
        );
    }

    /**
     * Decrypt.
     * @param txt The text to decrypt
     * @return Instance of the class
     * @throws CryptedUser.DecryptionException If can't decrypt
     */
    public static CryptedUser valueOf(final String txt)
        throws CryptedUser.DecryptionException {
        if (txt == null) {
            throw new CryptedUser.DecryptionException("text can't be NULL");
        }
        final byte[] bytes = CryptedUser.CODER.decode(txt);
        final DataInputStream stream = new DataInputStream(
            new ByteArrayInputStream(CryptedUser.xor(bytes))
        );
        try {
            final String identity = stream.readUTF();
            final String name = stream.readUTF();
            final String photo = stream.readUTF();
            if (!CryptedUser.SALT.equals(stream.readUTF())) {
                throw new CryptedUser.DecryptionException("invalid salt");
            }
            return new CryptedUser(
                new User() {
                    @Override
                    public String identity() {
                        return identity;
                    }
                    @Override
                    public String name() {
                        return name;
                    }
                    @Override
                    public URI photo() {
                        return URI.create(photo);
                    }
                }
            );
        } catch (java.io.IOException ex) {
            throw new CryptedUser.DecryptionException(ex);
        }
    }

    /**
     * XOR array of bytes.
     * @param input The input to XOR
     * @return Encrypted output
     */
    private static byte[] xor(final byte[] input) {
        final byte[] output = new byte[input.length];
        final byte[] secret = Manifests.read("S3Auth-SecurityKey").getBytes();
        if (secret.length == 0) {
            throw new IllegalStateException("empty security key");
        }
        int spos = 0;
        for (int pos = 0; pos < input.length; ++pos) {
            output[pos] = (byte) (input[pos] ^ secret[spos]);
            ++spos;
            if (spos >= secret.length) {
                spos = 0;
            }
        }
        return output;
    }

}
