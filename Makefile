
run-deps:
	docker-compose -f ./distribution/local/docker-compose-deps.yml up

build-jar:
	cp gradlew gradlewUnix
	dos2unix gradlewUnix
	./gradlewUnix clean build && java -jar ./build/libs/lovemap-backend-0.0.1.jar || true
	rm gradlewUnix

docker-build:
	docker build -f distribution/docker/Dockerfile --build-arg JAR_FILE=./build/libs/lovemap-backend-0.0.1-SNAPSHOT.jar -t lovemap-backend .

docker-push-local:
	docker tag lovemap-backend attilapalfi92/lovemap-backend
	docker push attilapalfi92/lovemap-backend

# cd /home/attila/.docker
# cat key.json | docker login -u _json_key --password-stdin https://europe-central2-docker.pkg.dev
docker-push:
	docker tag lovemap-backend europe-central2-docker.pkg.dev/smackmap/smackmap-docker/lovemap-backend
	docker push europe-central2-docker.pkg.dev/smackmap/smackmap-docker/lovemap-backend

docker-compose:
	docker-compose -f ./distribution/docker/docker-compose.yml up

deploy-k8s-local:
	kubectl config use-context docker-desktop
	kubectl apply -f distribution/k8s-local

port-forward-k8s-local:
	kubectl -n default port-forward service/lovemap 8090:8090

deploy-k8s-prod:
	kubectl config use-context gke_smackmap_europe-central2_smackmap-autopilot-cluster
	kubectl apply -f distribution/k8s-prod

publish-prod: build-jar docker-build docker-push deploy-k8s-prod
















