VERSION := $(file < VERSION.txt)

run-deps:
	docker-compose -f ./distribution/local/docker-compose-deps.yml up

update-version:
	sed -i -r "s/version = ".+"/version = \"${VERSION}\"/" build.gradle.kts
	sed -i -r "s/lovemap-backend:.+/lovemap-backend:${VERSION}/" distribution/k8s-prod/lovemap-deployment.yaml

build-jar:
	cp gradlew gradlewUnix
	dos2unix gradlewUnix
	./gradlewUnix clean build
	rm gradlewUnix

docker-build:
	docker build -f distribution/docker/Dockerfile --build-arg JAR_FILE=./build/libs/lovemap-backend-${VERSION}.jar -t lovemap-backend:${VERSION} .

docker-push-local:
	docker tag lovemap-backend attilapalfi92/lovemap-backend
	docker push attilapalfi92/lovemap-backend

# cd /home/attila/.docker
# cat key.json | docker login -u _json_key --password-stdin https://europe-central2-docker.pkg.dev
docker-push:
	docker tag lovemap-backend:${VERSION} us-east1-docker.pkg.dev/smackmap/lovemap-docker/lovemap-backend:${VERSION}
	docker push us-east1-docker.pkg.dev/smackmap/lovemap-docker/lovemap-backend:${VERSION}

docker-compose:
	docker-compose -f ./distribution/docker/docker-compose.yml up

deploy-k8s-local:
	kubectl config use-context docker-desktop
	kubectl apply -f distribution/k8s-local

port-forward-k8s-local:
	kubectl -n default port-forward service/lovemap 8090:8090

deploy-k8s-prod:
	kubectl config use-context gke_smackmap_us-east1_lovemap-us-east-1
	kubectl apply -f distribution/k8s-prod

publish-prod: update-version build-jar docker-build docker-push deploy-k8s-prod













