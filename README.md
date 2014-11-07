cssfx
=====

CSSFX enhances developper productivity by providing CSS reloading functionnality in your running application.

While developping you can run your JavaFX application, modify some CSS sources in your prefered editor, hit save button (or CTLR+S or CMD-S) and your JavaFX application is modified in real time.

[![CSSFX YouTube demo](http://img.youtube.com/vi/RELKg32xEWU/0.jpg)](http://www.youtube.com/watch?v=RELKg32xEWU)

## Project coordinates
__Web__

http://www.fxmisc.org/cssfx

__Maven__

    <dependency>
      <groupId>org.fxmisc.cssfx</groupId>
      <artifactId>cssfx</artifactId>
      <version>0.0.1-SNAPSHOT</version>
    </dependency>

## Usages

### Embedded

Starting monitoring CSS changes in development is as simple as adding one line in your application code.

    CSSFX.start()

Doing so CSSFX will start to track every CSS resource that will be declared on any Scene or Parent in your application. This monitoring will be active for all the Stage that your application will use.  

### Embedded with homemade configuration

TODO explain uriToFile concept & usage

### As an external application

TODO

### As a java agent

TODO

### Logging is CSSFX

CSSFX comes with a mini logging framework.

CSSFX support different properties to change default logging behavior

| System Property | Description |
|:----------:|:------------------|
|`cssfx.log`|activates CSSFX logging|
|`cssfx.log.level`|set the logging level to use, possible values `NONE ERROR WARN INFO DEBUG`, default is `INFO`|
|`cssfx.log.type`|set the type of "appender" to use, possible values `none console jul`, default is `console` |

You can also register your own LoggerFactory.

```java
CSSFXLogger.setLoggerFactory((loggerName) -> (level, message, args) -> {
    System.out.println("I log by myself, original message: " + String.format(message, args));
});
```


