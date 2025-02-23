<?xml version="1.0"?>
<!--
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
  <xsl:template match="/page">
    <html lang="en">
      <head>
        <meta charset="UTF-8"/>
        <meta name="description" content="HTTP Basic Authentication gateway for Amazon S3 buckets"/>
        <meta name="keywords" content="S3 Basic Auth, Amazon S3 Auth, S3 Auth, S3 HTTP Auth, S3 Basic Authentication"/>
        <meta name="author" content="s3auth.com"/>
        <link rel="stylesheet" type="text/css" media="all" href="/css/layout.css?{version/revision}"/>
        <link rel="icon" type="image/gif" href="/images/favicon.ico?{version/revision}"/>
        <link href="//netdna.bootstrapcdn.com/font-awesome/4.0.3/css/font-awesome.css" rel="stylesheet"/>
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
              <img src="/images/logo.svg" alt="s3auth.com logo" style="width:20em;height:3.4em;"/>
            </a>
          </p>
          <xsl:apply-templates select="identity"/>
          <xsl:apply-templates select="flash"/>
          <xsl:call-template name="content"/>
          <p style="border-top: 1px solid #ccc; margin-top: 2em; padding-top: 1em;">
            <xsl:text>s3auth.com is an open source project, hosted at </xsl:text>
            <a href="https://github.com/yegor256/s3auth">
              <xsl:text>Github</xsl:text>
            </a>
            <xsl:text> and </xsl:text>
            <a href="https://www.heroku.com">
              <xsl:text>Heroku</xsl:text>
            </a>
            <xsl:text>. We are not affiliated anyhow with </xsl:text>
            <a href="https://aws.amazon.com/">
              <xsl:text>Amazon Web Services</xsl:text>
            </a>
            <xsl:text>, although we provide a service for their clients.</xsl:text>
            <xsl:text> The service is absolutely free of charge, since it is sponsored by </xsl:text>
            <a href="https://www.zerocracy.com/">
              <xsl:text>Zerocracy</xsl:text>
            </a>
            <!--
            <xsl:text> (a </xsl:text>
            <a href="http://www.aws-partner-directory.com/PartnerDirectory/PartnerDetail?Name=TechnoPark+Corp.">
              <xsl:text>Standard Technology Partner</xsl:text>
            </a>
            <xsl:text> of AWS)</xsl:text>
            -->
            <xsl:text>. </xsl:text>
            <xsl:text>See also terms of use, privacy policy and license agreement at </xsl:text>
            <a href="/license">
              <xsl:text>LICENSE.txt</xsl:text>
            </a>
            <xsl:text>.</xsl:text>
            <xsl:text> This website is using </xsl:text>
            <a href="https://www.takes.org/">
              <xsl:text>Takes</xsl:text>
            </a>
            <xsl:text> framework.</xsl:text>
          </p>
          <p>
            <a href="http://www.sixnines.io/h/9dcb">
              <img src="http://www.sixnines.io/b/9dcb?style=flat" alt="sixnines badge"/>
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
  <xsl:template match="identity">
    <p>
      <img style="width: 2.5em; height: 2.5em;">
        <xsl:attribute name="src">
          <xsl:value-of select="photo|picture|avatar"/>
        </xsl:attribute>
        <xsl:attribute name="alt">
          <xsl:value-of select="name|login"/>
        </xsl:attribute>
      </img>
      <xsl:text> </xsl:text>
      <xsl:value-of select="name|login"/>
      <xsl:text> </xsl:text>
      <i style="font-size: 1.5em;">
        <xsl:attribute name="class">
          <xsl:text>fa </xsl:text>
          <xsl:choose>
            <xsl:when test="starts-with(urn, 'urn:facebook:')">
              <xsl:text>fa-facebook-square</xsl:text>
            </xsl:when>
            <xsl:when test="starts-with(urn, 'urn:google:')">
              <xsl:text>fa-google-plus-square</xsl:text>
            </xsl:when>
            <xsl:when test="starts-with(urn, 'urn:github:')">
              <xsl:text>fa-github-square</xsl:text>
            </xsl:when>
          </xsl:choose>
        </xsl:attribute>
        <xsl:comment>icon</xsl:comment>
      </i>
      <xsl:text> </xsl:text>
      <a>
        <xsl:attribute name="href">
          <xsl:value-of select="/page/links/link[@rel='takes:logout']/@href"/>
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
        <xsl:value-of select="level"/>
      </xsl:attribute>
      <xsl:value-of select="message"/>
    </div>
  </xsl:template>
</xsl:stylesheet>
