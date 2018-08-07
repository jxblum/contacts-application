#!/bin/bash

apacheGeodeVersion=${1:-1.6.0}
apacheGeodeSpringVersion=${2:-4.3.14.RELEASE}
jacksonVersion=${3:-2.9.6}
springVersion=${4:-5.0.8.RELEASE}
springHateoasVersion=${5:-0.25.0.RELEASE}
springLdapVersion=${6:-2.3.2.RELEASE}
springSecurityVersion=${7:-5.0.7.RELEASE}
springShellVersion=${8:-1.2.0.RELEASE}

if ! [[ -d $GEODE_HOME ]]; then
  echo "GEODE_HOME must be set"
  return 1
fi

function backup() {

  # Backup existing Spring libs in current working directory
  for springJar in `find $PWD -name "spring-*.jar"`
  do
    cp $springJar "$springJar.bak"
  done

}

function autoUpgradeGeodeLib() {

  cd $GEODE_HOME/lib
  echo "Current working directory is $PWD"

  backup

  cp ~/.m2/repository/org/springframework/spring-aop/${springVersion}/spring-aop-${springVersion}.jar $GEODE_HOME/lib/spring-aop-${apacheGeodeSpringVersion}.jar
  cp ~/.m2/repository/org/springframework/spring-beans/${springVersion}/spring-beans-${springVersion}.jar $GEODE_HOME/lib/spring-beans-${apacheGeodeSpringVersion}.jar
  cp ~/.m2/repository/org/springframework/spring-context/${springVersion}/spring-context-${springVersion}.jar $GEODE_HOME/lib/spring-context-${apacheGeodeSpringVersion}.jar
  cp ~/.m2/repository/org/springframework/spring-core/${springVersion}/spring-core-${springVersion}.jar $GEODE_HOME/lib/spring-core-${apacheGeodeSpringVersion}.jar
  cp ~/.m2/repository/org/springframework/spring-expression/${springVersion}/spring-expression-${springVersion}.jar $GEODE_HOME/lib/spring-expression-${apacheGeodeSpringVersion}.jar
  cp ~/.m2/repository/org/springframework/spring-web/${springVersion}/spring-web-${springVersion}.jar $GEODE_HOME/lib/spring-web-${apacheGeodeSpringVersion}.jar

  return 0

}

function autoUpgradeGeodeDeveloperRestApi() {

  cd $GEODE_HOME/tools/Extensions
  echo "Current working directory is $PWD"

  if [ -f "./geode-web-api-${apacheGeodeVersion}.war" ]; then

    echo "Upgrading Apache Geode Developer REST API..."

    # Backup WAR
    cp geode-web-api-${apacheGeodeVersion}.war geode-web-api-${apacheGeodeVersion}.war.bak

    # Extract WAR
    mkdir -p geode-web-api-war
    cd geode-web-api-war
    jar -xf ../geode-web-api-${apacheGeodeVersion}.war
    cd ..

    # Remove all existing Spring libs
    rm -Rf `find $PWD/geode-web-api-war -name "spring-*.jar" | grep -v "spring-plugin"`

    # Copy expected Spring lib versions to WAR
    cp ~/.m2/repository/org/springframework/spring-aop/${springVersion}/spring-aop-${springVersion}.jar $PWD/geode-web-api-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/spring-aspects/${springVersion}/spring-aspects-${springVersion}.jar $PWD/geode-web-api-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/spring-beans/${springVersion}/spring-beans-${springVersion}.jar $PWD/geode-web-api-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/spring-context/${springVersion}/spring-context-${springVersion}.jar $PWD/geode-web-api-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/spring-core/${springVersion}/spring-core-${springVersion}.jar $PWD/geode-web-api-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/spring-expression/${springVersion}/spring-expression-${springVersion}.jar $PWD/geode-web-api-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/spring-oxm/${springVersion}/spring-oxm-${springVersion}.jar $PWD/geode-web-api-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/spring-web/${springVersion}/spring-web-${springVersion}.jar $PWD/geode-web-api-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/spring-webmvc/${springVersion}/spring-webmvc-${springVersion}.jar $PWD/geode-web-api-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/hateoas/spring-hateoas/${springHateoasVersion}/spring-hateoas-${springHateoasVersion}.jar $PWD/geode-web-api-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/ldap/spring-ldap-core/${springLdapVersion}/spring-ldap-core-${springLdapVersion}.jar $PWD/geode-web-api-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/security/spring-security-core/${springSecurityVersion}/spring-security-core-${springSecurityVersion}.jar $PWD/geode-web-api-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/security/spring-security-config/${springSecurityVersion}/spring-security-config-${springSecurityVersion}.jar $PWD/geode-web-api-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/security/spring-security-web/${springSecurityVersion}/spring-security-web-${springSecurityVersion}.jar $PWD/geode-web-api-war/WEB-INF/lib

    # Repackage WAR
    jar -cf geode-web-api-${apacheGeodeVersion}.war -C geode-web-api-war *
    rm -Rf geode-web-api-war

  fi

  return 0

}

function autoUpgradeGeodeManagementRestApi() {

  cd $GEODE_HOME/tools/Extensions
  echo "Current working directory is $PWD"

  if [ -f "./geode-web-${apacheGeodeVersion}.war" ]; then

    echo "Upgrading Apache Geode Management REST API..."

    # Backup WAR
    cp geode-web-${apacheGeodeVersion}.war geode-web-${apacheGeodeVersion}.war.bak

    # Extract WAR
    mkdir -p geode-web-war
    cd geode-web-war
    jar -xf ../geode-web-${apacheGeodeVersion}.war
    cd ..

    # Remove all existing Spring libs
    rm -Rf `find $PWD/geode-web-war -name "spring-*.jar"`

    # Copy expected Spring lib versions to WAR
    cp ~/.m2/repository/org/springframework/spring-aop/${springVersion}/spring-aop-${springVersion}.jar $PWD/geode-web-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/spring-aspects/${springVersion}/spring-aspects-${springVersion}.jar $PWD/geode-web-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/spring-beans/${springVersion}/spring-beans-${springVersion}.jar $PWD/geode-web-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/spring-context/${springVersion}/spring-context-${springVersion}.jar $PWD/geode-web-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/spring-core/${springVersion}/spring-core-${springVersion}.jar $PWD/geode-web-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/spring-expression/${springVersion}/spring-expression-${springVersion}.jar $PWD/geode-web-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/spring-oxm/${springVersion}/spring-oxm-${springVersion}.jar $PWD/geode-web-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/spring-web/${springVersion}/spring-web-${springVersion}.jar $PWD/geode-web-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/spring-webmvc/${springVersion}/spring-webmvc-${springVersion}.jar $PWD/geode-web-war/WEB-INF/lib

    # Repackage WAR
    jar -cf geode-web-${apacheGeodeVersion}.war -C geode-web-war *
    rm -Rf geode-web-war

  fi

  return 0

}


function autoUpgradeGeodePulse() {

  cd $GEODE_HOME/tools/Pulse
  echo "Current working directory is $PWD"

  if [ -f "./geode-pulse-${apacheGeodeVersion}.war" ]; then

    echo "Upgrading Apache Geode Pulse..."

    # Backup WAR
    cp geode-pulse-${apacheGeodeVersion}.war geode-pulse-${apacheGeodeVersion}.war.bak

    # Extract WAR
    mkdir -p geode-pulse-war
    cd geode-pulse-war
    jar -xf ../geode-pulse-${apacheGeodeVersion}.war
    cd ..

    # Remove all existing Spring libs
    rm -Rf `find $PWD/geode-pulse-war -name "spring-*.jar"`
    rm -Rf `find $PWD/geode-pulse-war -name "jackson-*.jar"`

    # Copy expected Spring lib versions to WAR
    cp ~/.m2/repository/org/springframework/spring-aop/${springVersion}/spring-aop-${springVersion}.jar $PWD/geode-pulse-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/spring-beans/${springVersion}/spring-beans-${springVersion}.jar $PWD/geode-pulse-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/spring-context/${springVersion}/spring-context-${springVersion}.jar $PWD/geode-pulse-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/spring-core/${springVersion}/spring-core-${springVersion}.jar $PWD/geode-pulse-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/spring-expression/${springVersion}/spring-expression-${springVersion}.jar $PWD/geode-pulse-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/spring-oxm/${springVersion}/spring-oxm-${springVersion}.jar $PWD/geode-pulse-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/spring-tx/${springVersion}/spring-tx-${springVersion}.jar $PWD/geode-pulse-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/spring-web/${springVersion}/spring-web-${springVersion}.jar $PWD/geode-pulse-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/spring-webmvc/${springVersion}/spring-webmvc-${springVersion}.jar $PWD/geode-pulse-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/hateoas/spring-hateoas/${springHateoasVersion}/spring-hateoas-${springHateoasVersion}.jar $PWD/geode-pulse-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/security/spring-security-core/${springSecurityVersion}/spring-security-core-${springSecurityVersion}.jar $PWD/geode-pulse-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/security/spring-security-config/${springSecurityVersion}/spring-security-config-${springSecurityVersion}.jar $PWD/geode-pulse-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/security/spring-security-ldap/${springSecurityVersion}/spring-security-ldap-${springSecurityVersion}.jar $PWD/geode-pulse-war/WEB-INF/lib
    cp ~/.m2/repository/org/springframework/security/spring-security-web/${springSecurityVersion}/spring-security-web-${springSecurityVersion}.jar $PWD/geode-pulse-war/WEB-INF/lib
    cp ~/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/${jacksonVersion}/jackson-annotations-${jacksonVersion}.jar $PWD/geode-pulse-war/WEB-INF/lib
    cp ~/.m2/repository/com/fasterxml/jackson/core/jackson-core/${jacksonVersion}/jackson-core-${jacksonVersion}.jar $PWD/geode-pulse-war/WEB-INF/lib
    cp ~/.m2/repository/com/fasterxml/jackson/core/jackson-databind/${jacksonVersion}/jackson-databind-${jacksonVersion}.jar $PWD/geode-pulse-war/WEB-INF/lib

    # Repackage WAR
    jar -cf geode-pulse-${apacheGeodeVersion}.war -C geode-pulse-war *
    rm -Rf geode-pulse-war

  fi

  return 0

}

function manualUpgrade() {

  cp ~/.m2/repository/org/springframework/spring-aop/${springVersion}/spring-aop-${springVersion}.jar .
  cp ~/.m2/repository/org/springframework/spring-aspects/${springVersion}/spring-aspects-${springVersion}.jar .
  cp ~/.m2/repository/org/springframework/spring-beans/${springVersion}/spring-beans-${springVersion}.jar .
  cp ~/.m2/repository/org/springframework/spring-context/${springVersion}/spring-context-${springVersion}.jar .
  cp ~/.m2/repository/org/springframework/spring-context-indexer/${springVersion}/spring-context-indexer-${springVersion}.jar .
  cp ~/.m2/repository/org/springframework/spring-context-support/${springVersion}/spring-context-support-${springVersion}.jar .
  cp ~/.m2/repository/org/springframework/spring-core/${springVersion}/spring-core-${springVersion}.jar .
  cp ~/.m2/repository/org/springframework/spring-expression/${springVersion}/spring-expression-${springVersion}.jar .
  cp ~/.m2/repository/org/springframework/spring-instrument/${springVersion}/spring-instrument-${springVersion}.jar .
  cp ~/.m2/repository/org/springframework/spring-instrument-tomcat/${springVersion}/spring-instrument-tomcat-${springVersion}.jar .
  cp ~/.m2/repository/org/springframework/spring-jcl/${springVersion}/spring-jcl-${springVersion}.jar .
  cp ~/.m2/repository/org/springframework/spring-jdbc/${springVersion}/spring-jdbc-${springVersion}.jar .
  cp ~/.m2/repository/org/springframework/spring-jms/${springVersion}/spring-jms-${springVersion}.jar .
  cp ~/.m2/repository/org/springframework/spring-messaging/${springVersion}/spring-messaging-${springVersion}.jar .
  cp ~/.m2/repository/org/springframework/spring-orm/${springVersion}/spring-orm-${springVersion}.jar .
  cp ~/.m2/repository/org/springframework/spring-oxm/${springVersion}/spring-oxm-${springVersion}.jar .
  cp ~/.m2/repository/org/springframework/spring-test/${springVersion}/spring-test-${springVersion}.jar .
  cp ~/.m2/repository/org/springframework/spring-tx/${springVersion}/spring-tx-${springVersion}.jar .
  cp ~/.m2/repository/org/springframework/spring-web/${springVersion}/spring-web-${springVersion}.jar .
  cp ~/.m2/repository/org/springframework/spring-webflux/${springVersion}/spring-webflux-${springVersion}.jar .
  cp ~/.m2/repository/org/springframework/spring-webmvc/${springVersion}/spring-webmvc-${springVersion}.jar .
  cp ~/.m2/repository/org/springframework/spring-webmvc-portlet/${springVersion}/spring-webmvc-portlet-${springVersion}.jar .
  cp ~/.m2/repository/org/springframework/spring-websocket/${springVersion}/spring-websocket-${springVersion}.jar .

}

autoUpgradeGeodeLib
autoUpgradeGeodeDeveloperRestApi
autoUpgradeGeodeManagementRestApi
autoUpgradeGeodePulse

cd $GEODE_HOME
