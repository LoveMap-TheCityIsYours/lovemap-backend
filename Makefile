VERSION := $(file < VERSION.txt)
LOVEMAP_DO_PW := $(file < distribution/do-docker/password)

run-deps:
	docker-compose -f ./distribution/local/docker-compose-deps.yml up

update-version:
	sed -i -r "s/version = ".+"/version = \"${VERSION}\"/" build.gradle.kts
	sed -i -r "s/lovemap-backend:.+/lovemap-backend:${VERSION}/" distribution/k8s-prod/lovemap-deployment.yaml
	sed -i '2s/.*/ARG JAR_FILE=build\/libs\/lovemap-backend-${VERSION}.jar/' distribution/docker/Dockerfile
	sed -i '3s/.*/ARG JAR_FILE=build\/libs\/lovemap-backend-${VERSION}.jar/' distribution/do-docker/Dockerfile

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
docker-push-google:
	docker tag lovemap-backend:${VERSION} us-east1-docker.pkg.dev/smackmap/lovemap-docker/lovemap-backend:${VERSION}
	docker push us-east1-docker.pkg.dev/smackmap/lovemap-docker/lovemap-backend:${VERSION}

docker-push-do:
	docker tag lovemap-backend:${VERSION} registry.digitalocean.com/lovemap-registry/lovemap-backend:${VERSION}
	docker push registry.digitalocean.com/lovemap-registry/lovemap-backend:${VERSION}

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

publish-k8s-prod: update-version build-jar docker-build docker-push-google deploy-k8s-prod

copy-do-prod:
	curl --insecure --user lovemap:${LOVEMAP_DO_PW} -T distribution/do-docker/docker-restart.sh sftp://64.226.90.190/home/lovemap/docker-restart.sh
	curl --insecure --user lovemap:${LOVEMAP_DO_PW} -T VERSION.txt sftp://64.226.90.190/home/lovemap/VERSION.txt

# useful for later:
# https://dnssec-analyzer.verisignlabs.com/lovemap.app
# https://dnsviz.net/d/lovemap.app/analyze/
# docker exec -it 1cd7b92fc688 /bin/sh
# docker login registry.digitalocean.com
print-do-commands:
	echo "ssh lovemap@64.226.90.190"
	echo ${LOVEMAP_DO_PW}
	echo "sudo ./docker-restart.sh"

publish-do-prod: update-version build-jar docker-build docker-push-do copy-do-prod print-do-commands



















