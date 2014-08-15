<img src="http://img.s3auth.com/logo.png" width="200px" height="35px"/>

[![Made By Teamed.io](http://img.teamed.io/btn.svg)](http://www.teamed.io)
[![DevOps By Rultor.com](http://www.rultor.com/b/yegor256/s3auth)](http://www.rultor.com/p/yegor256/s3auth)

[![Build Status](https://travis-ci.org/yegor256/s3auth.svg?branch=master)](https://travis-ci.org/yegor256/s3auth)

## Amazon S3 HTTP Basic Auth Gateway

[s3auth.com](http://www.s3auth.com) is a Basic HTTP Auth gateway
in front of your private Amazon S3 bucket.

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

Continuous integration environment is here: http://www.rultor/s/s3auth

[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/yegor256/s3auth/trend.png)](https://bitdeli.com/free "Bitdeli Badge")

