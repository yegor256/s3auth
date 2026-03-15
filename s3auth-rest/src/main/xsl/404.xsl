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
      <xsl:text>page not found</xsl:text>
    </title>
  </xsl:template>
  <xsl:template name="content">
    <p>
      <xsl:text>Sorry, this page is absent.</xsl:text>
    </p>
  </xsl:template>
</xsl:stylesheet>
