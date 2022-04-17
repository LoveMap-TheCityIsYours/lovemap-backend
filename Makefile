
run-deps:
	docker-compose -f ./distribution/local/docker-compose-deps.yml up

build-jar:
	cp gradlew gradlewUnix
	dos2unix gradlewUnix
	./gradlewUnix clean build && java -jar ./build/libs/smackmap-backend-0.0.1.jar
	rm gradlewUnix

build-docker:
	docker build -f distribution/docker/Dockerfile --build-arg JAR_FILE=./build/libs/smackmap-backend-0.0.1-SNAPSHOT.jar -t attilapalfi92/smackmap-backend .
	docker push attilapalfi92/smackmap-backend

run-docker:
	docker run -p 8090:8090 -t attilapalfi92/smackmap-backend

run-docker-compose:
	docker-compose -f ./distribution/docker/docker-compose.yml up
