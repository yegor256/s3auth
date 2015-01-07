/*
 * @todo #200 Migrate old tests from rexsl/scripts to casperjs
 */
casper.test.begin(
    'Home page can be rendered',
    function (test) {
        casper.start(
            casper.cli.get('home'),
            function () {
                test.assertHttpStatus(200);
            }
        );
        casper.run(
            function () {
                test.done();
            }
        );
    }
);