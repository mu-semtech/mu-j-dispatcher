FROM ubuntu-java

MAINTAINER Langens Jonathan <jonathan.langens@tenforce.com>

ADD . /app

ENTRYPOINT cd app && javac com/tenforce/mu_semtech/mu_j_dispatcher/*.java && java com.tenforce.mu_semtech.mu_j_dispatcher.Proxy

EXPOSE 80
