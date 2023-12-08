# topgenius

[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=xmlet_topgenius&metric=coverage)](https://sonarcloud.io/component_measures?id=xmlet_topgenius&metric=Coverage)

Web Application comparing 3 different approaches for template views: [Handlebars](https://handlebarsjs.com/), [HtmlFlow](https://htmlflow.org/) and [ReactJS](https://reactjs.org/).

![topgenius architecture](https://raw.githubusercontent.com/xmlet/topgenius/master/webserver/src/main/resources/public/img/TopGeniusArch.png)


## Run 

To build and run topgenius web app locally do this:

```
$ ./gradlew clean stage
$ java -jar webserver/build/libs/topgenius-1.0.jar
```

Topgenius app should now be running on http://localhost:3000/.

## JMeter

We include in `jmeter` folder the scripts used to run performance tests of topgenius
web application comparing the three view engine approaches.

**Notice:** 
* You should have Jmeter installed **with the Selenium/WebDriver plug-in**.
* These JMX files are configured considering chromedriver is accessible via `/opt/homebrew/bin/chromedriver`.
* Update chromedriver PATH according to your installation.

Next we present an example of the command to run the test script of HtmlFlow:

```
jmeter -n -t TopGenius-SSR-Chrome.jmx -l <output-filename>.jtl -Jtemplate=htmlflow -Jcountry=australia -Jlimit=5000
```

You may change the following parameters:
* `template` - `htmlflow` or `handlebars`
* `country` - any country
* `limit` - An integer to a **maximum value of 10000**

To test React you should use a different script to detect page load completion due to the use of CSR (client-side rendering).
```
jmeter -n -t TopGenius-React-Chrome.jmx -l <output-filename>.jtl -Jlimit=5000
```


Running these tests with JDK version 11, SE Runtime Environment 18.9 (build 11+28), 
and ChromeDriver 74.0.3729.6 we got the following results:

![topgenius results](https://raw.githubusercontent.com/xmlet/topgenius/master/webserver/src/main/resources/public/img/fig05-chart-perf.png)