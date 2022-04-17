
run-deps:
	docker-compose -f ./distribution/local/docker-compose.yml up

build-docker:
    cp gradlew gradlewUnix
    dos2unix gradlewUnix
    ./gradlewUnix build && java -jar ./build/libs/smackmap-backend-0.0.1.jar
    rm gradlewUnix
