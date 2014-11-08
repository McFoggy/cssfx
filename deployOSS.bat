@ECHO OFF

mvn -Psign clean deploy -Dgpg.passphrase=%1