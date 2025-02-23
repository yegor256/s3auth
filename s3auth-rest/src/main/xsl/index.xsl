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
      <xsl:text>Domains Under Management</xsl:text>
    </h1>
    <xsl:choose>
      <xsl:when test="/page/domains/domain">
        <p>
          <xsl:text>Your registered domains:</xsl:text>
        </p>
        <ul>
          <xsl:apply-templates select="/page/domains/domain"/>
        </ul>
      </xsl:when>
      <xsl:otherwise>
        <p>
          <xsl:text>You haven't registered any domains yet.</xsl:text>
        </p>
      </xsl:otherwise>
    </xsl:choose>
    <p>
      <xsl:text>Register new domain using the form:</xsl:text>
    </p>
    <form method="post">
      <xsl:attribute name="action">
        <xsl:value-of select="/page/links/link[@rel='add']/@href"/>
      </xsl:attribute>
      <p>
        <label>
          <xsl:text>Host name, for example </xsl:text>
          <span class="tt">
            <xsl:text>bucket-1.example.com</xsl:text>
          </span>
          <xsl:text>:</xsl:text>
        </label>
        <input name="host"/>
        <label>
          <xsl:text>AWS key, exactly 20 symbols (</xsl:text>
          <a href="https://docs.aws.amazon.com/IAM/latest/UserGuide/Using_SettingUpUser.html">
            <xsl:text>where do I get it?</xsl:text>
          </a>
          <xsl:text>):</xsl:text>
        </label>
        <input name="key" size="30" maxlength="20"/>
        <label>
          <xsl:text>AWS secret key, exactly 40 symbols:</xsl:text>
        </label>
        <input name="secret" size="40" maxlength="40"/>
        <label>
          <xsl:text>Bucket name:</xsl:text>
        </label>
        <input name="bucket" size="45" maxlength="250"/>
        <label>
          <xsl:text>Optional AWS S3 endpoint (</xsl:text>
          <a href="https://docs.amazonwebservices.com/general/latest/gr/rande.html#s3_region">
            <xsl:text>what is it?</xsl:text>
          </a>
          <xsl:text>), for example </xsl:text>
          <span class="tt">
            <xsl:text>s3-ap-southeast-1</xsl:text>
          </span>
          <xsl:text> or </xsl:text>
          <span class="tt">
            <xsl:text>s3</xsl:text>
          </span>
          <xsl:text>:</xsl:text>
        </label>
        <input name="region" value="s3" size="25" maxlength="30"/>
        <label>
          <xsl:text>Optional syslog UDP end-point:</xsl:text>
        </label>
        <input name="syslog" value="syslog.s3auth.com:514" size="40" maxlength="200"/>
        <input class="submit" type="submit" value=""/>
      </p>
    </form>
    <h1>
      <xsl:text>Instructions</xsl:text>
    </h1>
    <p>
      <xsl:text>1. Point a CNAME DNS record of your domain to </xsl:text>
      <span class="tt">
        <xsl:text>relay.s3auth.com</xsl:text>
      </span>
      <xsl:text> and wait for DNS to propagate.</xsl:text>
    </p>
    <p>
      <xsl:text>2. Create a new user in Amazon IAM and attach new custom policy to it (</xsl:text>
      <a href="https://docs.aws.amazon.com/IAM/latest/UserGuide/ManagingPolicies.html">
        <xsl:text>how?</xsl:text>
      </a>
      <xsl:text>):</xsl:text>
    </p>
    <pre><![CDATA[{
  "Statement": [
    {
      "Effect": "Allow",
      "Action": ["s3:GetObject", "s3:GetBucketWebsite"],
      "Resource": [
        "arn:aws:s3:::bucket-1.example.com/*",
        "arn:aws:s3:::bucket-2.example.com/*"
      ]
    }
  ]
}]]></pre>
    <p>
      <xsl:text>3. Register your domain, IAM key, and IAM secret key here, using the form above.</xsl:text>
    </p>
    <p>
      <xsl:text>4. Generate </xsl:text>
      <span class="tt">
        <xsl:text>.htpasswd</xsl:text>
      </span>
      <xsl:text> file in </xsl:text>
      <a href="https://httpd.apache.org/docs/2.2/misc/password_encryptions.html">
        <xsl:text>Apache HTTP Server format</xsl:text>
      </a>
      <xsl:text> using </xsl:text>
      <a href="https://httpd.apache.org/docs/2.2/programs/htpasswd.html">
        <xsl:text>htpasswd</xsl:text>
      </a>
      <xsl:text> tool:</xsl:text>
    </p>
    <pre><![CDATA[$ htpasswd -nbs user password]]></pre>
    <p>
      <xsl:text>5. Upload it to the root of your bucket.</xsl:text>
    </p>
    <p>
      <xsl:text>6. Upload your files to the bucket and access them in any web browser
                using the credentials specified in the </xsl:text>
      <span class="tt">
        <xsl:text>.htpasswd</xsl:text>
      </span>
      <xsl:text> file.</xsl:text>
    </p>
    <h1>
      <xsl:text>Troubleshooting</xsl:text>
    </h1>
    <p>
      <xsl:text>If you can't authorize yourself with a username/password combination configured in </xsl:text>
      <span class="tt">
        <xsl:text>.htpasswd</xsl:text>
      </span>
      <xsl:text> and on every attempt browser says "try again" and asks for credentials, </xsl:text>
      <xsl:text>there are two possible causes:</xsl:text>
    </p>
    <ol>
      <li>
        <xsl:text>Password is wrong. Encode it with </xsl:text>
        <a href="https://aspirine.org/htpasswd_en.html">
          <xsl:text>this online tool</xsl:text>
        </a>
        <xsl:text> and try again in 10 minutes (not earlier).</xsl:text>
      </li>
      <li>
        <xsl:text>S3 permissions are not granted and we simply can't read your </xsl:text>
        <span class="tt">
          <xsl:text>.htpasswd</xsl:text>
        </span>
        <xsl:text> file. Make sure your IAM user has permission policy attached, as explained above.</xsl:text>
      </li>
    </ol>
    <p>
      <xsl:text>In order to investigate further and see what the system knows about your </xsl:text>
      <span class="tt">
        <xsl:text>.htpasswd</xsl:text>
      </span>
      <xsl:text> file make an HTTP request from command line:</xsl:text>
    </p>
    <pre><![CDATA[$ curl -H "Authorization: Basic am9lOnNlY3JldA==" http://maven.s3auth.com/index.html
maven.s3auth.com with .htpasswd(3 user(s) updated 2min ago)]]></pre>
    <p>
      <xsl:text>The output contains information from the relay:</xsl:text>
      <xsl:text> how many users discovered in the file and when was it retrieved last time from S3 bucket.</xsl:text>
      <span class="tt">
        <xsl:text>am9lOnNlY3JldA==</xsl:text>
      </span>
      <xsl:text> is a </xsl:text>
      <a href="https://en.wikipedia.org/wiki/Base64">
        <xsl:text>Base64 encoded</xsl:text>
      </a>
      <xsl:text> version of </xsl:text>
      <span class="tt">
        <xsl:text>joe:secret</xsl:text>
      </span>
      <xsl:text>, where </xsl:text>
      <span class="tt">
        <xsl:text>joe</xsl:text>
      </span>
      <xsl:text> is a user name and </xsl:text>
      <span class="tt">
        <xsl:text>secret</xsl:text>
      </span>
      <xsl:text> is a password (this is how </xsl:text>
      <a href="https://en.wikipedia.org/wiki/Basic_access_authentication">
        <xsl:text>HTTP Basic Authentication</xsl:text>
      </a>
      <xsl:text> works).</xsl:text>
    </p>
    <p>
      <xsl:text>Another way to check is to open </xsl:text>
      <span class="tt">
        <xsl:text>http://&lt;your-domain&gt;/.htpasswd</xsl:text>
      </span>
      <xsl:text> in a browser (no authorization required).</xsl:text>
      <xsl:text> If everything is fine you'll see how many bytes are there in the file</xsl:text>
      <xsl:text> retrieved from your S3 bucket. Otherwise you'll get an exception stacktrace, which </xsl:text>
      <xsl:text> should help to understand the problem.</xsl:text>
    </p>
    <p>
      <xsl:text>If any other problems don't hesitate to submit a ticket to </xsl:text>
      <a href="https://github.com/yegor256/s3auth/issues">
        <xsl:text>github</xsl:text>
      </a>
      <xsl:text>, we'll do our best to help.</xsl:text>
    </p>
  </xsl:template>
  <xsl:template match="domain">
    <li>
      <a>
        <xsl:attribute name="href">
          <xsl:text>https://</xsl:text>
          <xsl:value-of select="name"/>
          <xsl:text>/</xsl:text>
        </xsl:attribute>
        <xsl:value-of select="name"/>
      </a>
      <xsl:text> | </xsl:text>
      <a>
        <xsl:attribute name="href">
          <xsl:value-of select="links/link[@rel='remove']/@href"/>
        </xsl:attribute>
        <xsl:text>delete</xsl:text>
      </a>
      <ul>
        <li>
          <xsl:text>key: </xsl:text>
          <span class="tt">
            <xsl:value-of select="key"/>
          </span>
        </li>
        <li>
          <xsl:text>secret: </xsl:text>
          <span class="tt">
            <xsl:value-of select="secret"/>
          </span>
        </li>
        <li>
          <xsl:text>region: </xsl:text>
          <span class="tt">
            <xsl:value-of select="region"/>
          </span>
        </li>
        <li>
          <xsl:text>bucket: </xsl:text>
          <span class="tt">
            <xsl:value-of select="bucket"/>
          </span>
        </li>
        <li>
          <xsl:text>7-days traffic: </xsl:text>
          <xsl:choose>
            <xsl:when test="stats/bytesTransferred &gt; 1024 * 1024">
              <xsl:value-of select="format-number(stats/bytesTransferred div (1024 * 1024), '#.0')"/>
              <xsl:text> Mb</xsl:text>
            </xsl:when>
            <xsl:when test="stats/bytesTransferred &gt; 1024">
              <xsl:value-of select="format-number(stats/bytesTransferred div 1024, '#.0')"/>
              <xsl:text> Kb</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="stats/bytesTransferred"/>
              <xsl:text> bytes</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
        </li>
      </ul>
    </li>
  </xsl:template>
</xsl:stylesheet>
