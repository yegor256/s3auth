#!/usr/bin/env bash

# SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
# SPDX-License-Identifier: MIT

set -e -o pipefail

cd "$(dirname "$0")"
cp /code/home/assets/s3auth/settings.xml .
trap 'git reset HEAD~1 && rm settings.xml' EXIT
git add settings.xml
git commit -m 'settings.xml for heroku' --no-verify
git push heroku master -f
