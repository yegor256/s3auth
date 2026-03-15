<?xml version="1.0" encoding="UTF-8"?>
<!--
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns="http://www.w3.org/1999/xhtml" version="1.0" exclude-result-prefixes="xs">
  <xsl:output method="html" doctype-system="about:legacy-compat" encoding="UTF-8" indent="yes"/>
  <xsl:include href="/xsl/layout.xsl"/>
  <xsl:template name="head">
    <title>
      <xsl:text>s3auth</xsl:text>
    </title>
  </xsl:template>
  <xsl:template name="content">
    <h1>
      <xsl:text>Amazon S3 HTTP Basic Auth Gateway</xsl:text>
    </h1>
    <p>
      <xsl:text>Login </xsl:text>
      <a>
        <xsl:attribute name="href">
          <xsl:value-of select="/page/links/link[@rel='takes:github']/@href"/>
        </xsl:attribute>
        <xsl:text>via GitHub</xsl:text>
      </a>
    </p>
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
      <span class="tt">
        <xsl:text>http://maven.s3auth.com/</xsl:text>
      </span>
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
  </xsl:template>
</xsl:stylesheet>
