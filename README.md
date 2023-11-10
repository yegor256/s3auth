<img src="https://www.s3auth.com/images/logo.svg" width="200px" height="35px"/>

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![DevOps By Rultor.com](http://www.rultor.com/b/yegor256/s3auth)](http://www.rultor.com/p/yegor256/s3auth)
[![We recommend IntelliJ IDEA](https://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![mvn](https://github.com/yegor256/s3auth/actions/workflows/mvn.yml/badge.svg)](https://github.com/yegor256/s3auth/actions/workflows/mvn.yml)
[![Availability at SixNines](https://www.sixnines.io/b/9dcb)](https://www.sixnines.io/h/9dcb)
[![PDD status](http://www.0pdd.com/svg?name=yegor256/s3auth)](http://www.0pdd.com/p?name=yegor256/s3auth)
[![codecov](https://codecov.io/gh/yegor256/s3auth/branch/master/graph/badge.svg)](https://codecov.io/gh/yegor256/s3auth)
[![Hits-of-Code](https://hitsofcode.com/github/yegor256/s3auth)](https://hitsofcode.com/view/github/yegor256/s3auth)
![Lines of code](https://img.shields.io/tokei/lines/github/yegor256/s3auth)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/yegor256/s3auth/blob/master/LICENSE.txt)

[s3auth.com](http://www.s3auth.com) is a Basic HTTP Auth gateway
in front of your private Amazon S3 bucket. Read this blog post
for a more detailed explanation: [Basic HTTP Auth for S3 Buckets](http://www.yegor256.com/2014/04/21/s3-http-basic-auth.html).

Point your `test.example.com` CNAME to `relay.s3auth.com`,
and register the domain in [s3auth.com](http://www.s3auth.com) web panel.
You will be able to access bucket's content in a browser with an HTTP basic auth.
Your bucket will be accessible using your Amazon IAM credentials
and with custom user/password pairs in your `.htpasswd` file
(similar to Apache HTTP Server).

For example, try [http://maven.s3auth.com/](http://maven.s3auth.com/)
(with username `s3auth` and password `s3auth`).
You will access content of Amazon S3 bucket `maven.s3auth.com`,
which is not readable anonymously otherwise.

## How to contribute

Fork repository, make changes, send us a pull request. We will review
your changes and apply them to the `master` branch shortly, provided
they don't violate our quality standards. To avoid frustration, before
sending us your pull request please run full Maven build:

```
$ mvn clean install -Pqulice
```

To avoid build errors use JDK >= 1.7 and Maven >= 3.1.1

To run it locally:

```
$ mvn clean install -Phit-refresh -Dport=8080
```

You will be able to open it at `http://localhost:8080`
