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
    <xsl:template match="/">
        <!-- see http://stackoverflow.com/questions/3387127 -->
        <xsl:text disable-output-escaping="yes">&lt;!DOCTYPE html&gt;</xsl:text>
        <xsl:apply-templates select="page"/>
    </xsl:template>
    <xsl:template match="page">
        <html lang="en">
            <head>
                <meta charset="UTF-8"/>
                <meta name="description" content="HTTP Basic Authentication gateway for Amazon S3 buckets"/>
                <meta name="keywords" content="S3 Basic Auth, Amazon S3 Auth, S3 Auth, S3 HTTP Auth, S3 Basic Authentication"/>
                <meta name="author" content="s3auth.com"/>
                <link rel="stylesheet" type="text/css" media="all">
                    <xsl:attribute name="href">
                        <xsl:text>/css/layout.css?</xsl:text>
                        <xsl:value-of select="/page/version/revision"/>
                    </xsl:attribute>
                </link>
                <link rel="icon" type="image/gif">
                    <xsl:attribute name="href">
                        <xsl:text>http://img.s3auth.com/favicon.ico?</xsl:text>
                        <xsl:value-of select="/page/version/revision"/>
                    </xsl:attribute>
                </link>
                <xsl:call-template name="head"/>
                <script type="text/javascript"><![CDATA[
                  var _gaq = _gaq || [];
                  _gaq.push(['_setAccount', 'UA-1963507-21']);
                  _gaq.push(['_trackPageview']);
                  (function() {
                    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
                    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
                    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
                  })();
                ]]></script>
            </head>
            <body>
                <xsl:apply-templates select="version"/>
                <div id="content">
                    <p>
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="/page/links/link[@rel='home']/@href"/>
                            </xsl:attribute>
                            <img src="http://img.s3auth.com/logo.png" alt="s3auth.com logo" style="width: 180px; height: 32px;"/>
                        </a>
                    </p>
                    <xsl:apply-templates select="user"/>
                    <xsl:apply-templates select="flash"/>
                    <xsl:call-template name="content"/>
                    <p style="border-top: 1px solid #ccc; margin-top: 2em; padding-top: 1em;">
                        <xsl:text>s3auth.com is an open source project, hosted at </xsl:text>
                        <a href="https://github.com/yegor256/s3auth">
                            <xsl:text>github</xsl:text>
                        </a>
                        <xsl:text>. We are not affiliated anyhow with </xsl:text>
                        <a href="http://aws.amazon.com/">
                            <xsl:text>Amazon Web Services</xsl:text>
                        </a>
                        <xsl:text>, although we provide service for their clients.</xsl:text>
                        <xsl:text> The service is absolutely free of charge, since it is sponsored by </xsl:text>
                        <a href="http://www.tpc2.com/">
                            <xsl:text>tpc2.com</xsl:text>
                        </a>
                        <xsl:text> (a </xsl:text>
                        <a href="https://aws.amazon.com/solution-providers/isv/technopark-corp">
                            <xsl:text>Standard Technology Partner</xsl:text>
                        </a>
                        <xsl:text> of AWS). </xsl:text>
                        <xsl:text>Sea also terms of use, privacy policy and license agreement at </xsl:text>
                        <a href="/misc/LICENSE.txt">
                            <xsl:text>LICENSE.txt</xsl:text>
                        </a>
                        <xsl:text>.</xsl:text>
                        <xsl:text> This website is using </xsl:text>
                        <a href="http://www.rexsl.com/">
                            <xsl:text>ReXSL</xsl:text>
                        </a>
                        <xsl:text>, Java RESTful development framework.</xsl:text>
                    </p>
                    <p>
                        <a href="https://aws.amazon.com/solution-providers/isv/technopark-corp">
                            <img src="http://img.s3auth.com/apn-logo.png" alt="AWS Partner Network Logo" style="width: 8%;"/>
                        </a>
                    </p>
                </div>
            </body>
        </html>
    </xsl:template>
    <xsl:template name="millis">
        <xsl:param name="millis" as="xs:integer"/>
        <xsl:choose>
            <xsl:when test="$millis &gt; 1000">
                <xsl:value-of select="format-number($millis div 1000, '0.0')"/>
                <xsl:text>s</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="format-number($millis, '#')"/>
                <xsl:text>ms</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="user">
        <p>
            <img style="width: 25px; height: 25px;">
                <xsl:attribute name="src">
                    <xsl:value-of select="photo"/>
                </xsl:attribute>
                <xsl:attribute name="alt">
                    <xsl:value-of select="name"/>
                </xsl:attribute>
            </img>
            <xsl:text> </xsl:text>
            <xsl:value-of select="name"/>
            <img style="margin-left: 0.5em;" alt="account type">
                <xsl:attribute name="src">
                    <xsl:text>http://img.s3auth.com/icons/</xsl:text>
                    <xsl:choose>
                        <xsl:when test="starts-with(identity, 'urn:facebook:')">
                            <xsl:text>facebook</xsl:text>
                        </xsl:when>
                        <xsl:when test="starts-with(identity, 'urn:google:')">
                            <xsl:text>google</xsl:text>
                        </xsl:when>
                    </xsl:choose>
                    <xsl:text>-small.png</xsl:text>
                </xsl:attribute>
            </img>
            <xsl:text> </xsl:text>
            <a>
                <xsl:attribute name="href">
                    <xsl:value-of select="/page/links/link[@rel='logout']/@href"/>
                </xsl:attribute>
                <xsl:text>logout</xsl:text>
            </a>
        </p>
    </xsl:template>
    <xsl:template match="version">
        <div id="version">
            <xsl:value-of select="name"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="revision"/>
            <xsl:text> </xsl:text>
            <xsl:call-template name="millis">
                <xsl:with-param name="millis" select="/page/millis"/>
            </xsl:call-template>
        </div>
    </xsl:template>
    <xsl:template match="flash">
        <div id="flash">
            <xsl:attribute name="class">
                <xsl:value-of select="color"/>
            </xsl:attribute>
            <xsl:value-of select="message"/>
        </div>
    </xsl:template>
</xsl:stylesheet>
