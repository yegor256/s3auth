<?xml version="1.0"?>
<!--
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
 -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns="http://www.w3.org/1999/xhtml" version="2.0" exclude-result-prefixes="xs">
    <xsl:output method="xml" omit-xml-declaration="yes"/>
    <xsl:include href="/xsl/layout.xsl"/>
    <xsl:template name="head">
        <title>
            <xsl:text>login</xsl:text>
        </title>
    </xsl:template>
    <xsl:template name="content">
        <h1>
            <xsl:text>Amazon S3 HTTP Basic Auth Gateway</xsl:text>
        </h1>
        <p>
            <xsl:text>s3auth.com is a </xsl:text>
            <a href="http://en.wikipedia.org/wiki/Basic_access_authentication">
                <xsl:text>Basic HTTP Auth</xsl:text>
            </a>
            <xsl:text> gateway in front of your private </xsl:text>
            <a href="http://aws.amazon.com/s3/">
                <xsl:text>Amazon S3</xsl:text>
            </a>
            <xsl:text> bucket.</xsl:text>
        </p>
        <p>
            <xsl:text>Point your </xsl:text>
            <span class="tt">
                <xsl:text>test.example.com</xsl:text>
            </span>
            <xsl:text> CNAME to </xsl:text>
            <span class="tt">
                <xsl:text>relay.s3auth.com</xsl:text>
            </span>
            <xsl:text>, and register the domain here. You will be able to
            access bucket's content in a browser with HTTP basic auth.
            Your bucket will be accessible using your </xsl:text>
            <a href="http://aws.amazon.com/iam/">
                <xsl:text>Amazon IAM</xsl:text>
            </a>
            <xsl:text> credentials and with custom user/password pairs in your </xsl:text>
            <span class="tt">
                <xsl:text>.htpasswd</xsl:text>
            </span>
            <xsl:text> file (similar to Apache HTTP Server).</xsl:text>
        </p>
        <p>
            <xsl:text>For example, try </xsl:text>
            <a href="http://s3auth:s3auth@maven.s3auth.com/">maven.s3auth.com</a>
            <xsl:text> (with username </xsl:text>
            <span class="tt">
                <xsl:text>s3auth</xsl:text>
            </span>
            <xsl:text> and password </xsl:text>
            <span class="tt">
                <xsl:text>s3auth</xsl:text>
            </span>
            <xsl:text>). You will access content of Amazon S3 bucket </xsl:text>
            <span class="tt">
                <xsl:text>maven.s3auth.com</xsl:text>
            </span>
            <xsl:text>, which is not readable anonymously otherwise.</xsl:text>
        </p>
        <p>
            <xsl:text>To start, please </xsl:text>
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="/page/links/link[@rel='login']/@href"/>
                </xsl:attribute>
                <xsl:text>login via Facebook</xsl:text>
            </a>
            <xsl:text>!</xsl:text>
        </p>
    </xsl:template>
</xsl:stylesheet>
