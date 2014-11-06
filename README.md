cssfx
=====

CSSFX enhances developper productivity by providing CSS reloading functionnality in your running application.

While developping you can run your JavaFX application, modify some CSS sources in your prefered editor, hit save button (or CTLR+S or CMD-S) and your JavaFX application is modified in real time.

## Project coordinates
__Web__

http://www.fxmisc.org/cssfx

__Maven__

    <dependency>
      <groupId>org.fxmisc.cssfx</groupId>
      <artifactId>cssfx</artifactId>
    </dependency>

## Usages

### Embedded

Starting monitoring CSS changes in development is as simple as adding one line in your application code.

    CSSFX.monitor(scene).start()

### Embedded with homemade configuration

TODO explain uriToFile concept & usage

### As an external application

TODO

### As a java agent

TODO
