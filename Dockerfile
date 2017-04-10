FROM coco/dropwizardbase-internal:v1.0.3

ADD . /

RUN apk --update add git libstdc++ wget \
  && cd /tmp \
  && wget -q https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.21-r2/glibc-2.21-r2.apk \
  && wget -q https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.21-r2/glibc-bin-2.21-r2.apk \
  && apk add --allow-untrusted glibc-2.21-r2.apk glibc-bin-2.21-r2.apk \
  && /usr/glibc/usr/bin/ldconfig /lib /usr/glibc/usr/lib \
  && cd / \
  && HASH=$(git log -1 --pretty=format:%H) \
  && TAG=$(git tag -l --contains $HASH) \
  && VERSION=${TAG:-untagged} \
  && mvn versions:set -DnewVersion=$VERSION \
  && mvn install -Dbuild.git.revision=$HASH -Djava.net.preferIPv4Stack=true \
  && rm target/document-store-api-*-sources.jar \
  && mv target/document-store-api-*.jar document-store-api.jar \
  && apk del go git \
  && rm -rf /var/cache/apk/* /target* /root/.m2/* /tmp/*.apk

EXPOSE 8080 8081

CMD exec java $JAVA_OPTS \
         -Ddw.server.applicationConnectors[0].port=8080 \
         -Ddw.server.adminConnectors[0].port=8081 \
         -Ddw.mongo.addresses=$MONGO_ADDRESSES \
         -Ddw.cacheTtl=$CACHE_TTL \
		 -Ddw.apiHost=$API_HOST \
		 -Ddw.logging.appenders[0].logFormat="%-5p [%d{ISO8601, GMT}] %c: %X{transaction_id} %replace(%m%n[%thread]%xEx){'\n', '|'}%nopex%n" \
		 -jar document-store-api.jar server config.yaml
