FROM up-registry.ft.com/coco/dropwizardbase

ADD . /

RUN apk --update add git \
  && HASH=$(git log -1 --pretty=format:%H) \
  && BUILD_NUMBER=$(cat buildnum.txt) \
  && BUILD_URL=$(cat buildurl.txt) \
  && echo "DEBUG Jenkins job url: $BUILD_URL" \
  && mvn install -Dbuild.git.revision=$HASH -Dbuild.number=$BUILD_NUMBER -Dbuild.url=$BUILD_URL -Djava.net.preferIPv4Stack=true \
  && rm target/document-store-api-*-sources.jar \
  && mv target/document-store-api-*.jar app.jar \
  && apk del go git \
  && rm -rf /var/cache/apk/* /target* /root/.m2/*

EXPOSE 8080 8081

CMD java -Ddw.server.applicationConnectors[0].port=8080 \
         -Ddw.server.adminConnectors[0].port=8081 \
         -Ddw.mongo.addresses=$MONGO_ADDRESSES \
		 -Ddw.apiHost=$API_HOST \
		 -Ddw.logging.appenders[0].logFormat="%-5p [%d{ISO8601, GMT}] %c: %X{transaction_id} %replace(%m%n[%thread]%xEx){'\n', '|'}%nopex%n" \
		 -jar app.jar server config.yaml
