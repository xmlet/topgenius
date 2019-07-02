# topgenius

[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=xmlet_topgenius&metric=coverage)](https://sonarcloud.io/component_measures?id=xmlet_topgenius&metric=Coverage)

Web Application comparing 3 different approaches for template views: [Handlebars](https://handlebarsjs.com/), [HtmlFlow](https://htmlflow.org/) and [ReactJS](https://reactjs.org/).

![topgenius architecture](https://raw.githubusercontent.com/xmlet/topgenius/master/webserver/src/main/resources/public/img/TopGeniusArch.png)


## Run 

To build and run topgenius web app locally do this:

```
$ ./gradlew stage
$ ./gradlew run
```

Topgenius app should now be running on http://localhost:3000/.

## JMeter

We include in `jmeter` folder the scripts used to run performance tests of topgenius
web application comparing the three view engine approaches.
By default, these scripts use a 5000 tracks workload.
We may change this value to evaluate other scenarios.  

Next we present an example of the command to run the test script of HtmlFlow:

```
$ jmeter -n -t TopGenius-HtmlFlow-Chrome.jmx -l <output-filename>.jtl
```

Running these tests with JDK version 11, SE Runtime Environment 18.9 (build 11+28), 
and ChromeDriver 74.0.3729.6 we got the following results:

![topgenius results](https://raw.githubusercontent.com/xmlet/topgenius/master/webserver/src/main/resources/public/img/fig05-chart-perf.png)