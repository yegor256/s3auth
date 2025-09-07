#!/usr/bin/env bash

# SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
# SPDX-License-Identifier: MIT

set -e

cd "$(dirname "$0")"
cp /code/home/assets/s3auth/settings.xml .
git add settings.xml
git commit -m 'settings.xml for dokku'
trap 'git reset HEAD~1 && rm settings.xml' EXIT
git push dokku master -f
