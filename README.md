# reform

Reform allows managing hiwi contracts.
Contract data is stored and procressed in your browser
and distributed peer to peer.

## Prerequisites

You should make sure that the following components are pre-installed on your machine:

 - [Node.js](https://nodejs.org/en/download/)
 - [sbt](https://www.scala-sbt.org/)

You will also need a browser supporting selenium.
We recommend chromium
```
export SELENIUM_BROWSER=chrome 
```

Optionally, install the pre-commit hook (recommended)
```bash
ln -sr pre-commit.sh .git/hooks/pre-commit
```
For Windows: (run in elevated commandprompt)
```bash
cmd /c mklink ".git\\hooks\\pre-commit" "..\\..\\pre-commit.sh"
```

## Developing the peer (frontend)

In the directory `peer` first load environment variables.
```
cp env.example .env
$EDITOR .env
export $(cat .env)
```

Cleaning the project:
```
sbt clean
```

Warning: Firefox is not able to load the Source maps, use Chromium.

Install node modules:
```
npm install
```

Compile to javascript:
```bash
sbt ~fastLinkJS
```

In another terminal start the web dev server: 
```bash
npm run dev
```

To start the always-online-peer (headless peer running inside the jvm)
```bash
sbt reformJVM/run
```

Open peered instances of the frontend in your browser:

```
npm run spawn-test-instances -- 2 http://localhost:5173/
```

Developing using a temp dir:

```
setsid -f chromium --user-data-dir=$(mktemp -d) http://localhost:5173/
```

Show dependency tree:
```
sbt dependencyBrowseTreeHTML
```

## Testing

Tun javascript tests:
```
npm run test
```

Run jvm tests:

```bash
sbt test
```

If you want to run the selenium tests you need to run:
```bash
npm run selenium
```

## Developing SSO Login

We need to control the exact whitelisted url.
This can be done with ssh reverse port forwarding:

```bash
ssh -vvv -N -R 43547:localhost:3000 reform.st.informatik.tu-darmstadt.de
```

Then develop in the `discovery` folder.

## Deployment

Using podman-compose-git
```
export DOCKER_HOST="unix://$XDG_RUNTIME_DIR/podman/podman.sock"

podman-compose --env-file .env.podman --project-name traefik --file docker-compose-local-traefik.yml down
podman-compose --env-file .env.podman --project-name traefik --file docker-compose-local-traefik.yml up --pull --build --remove-orphans

podman-compose --env-file .env.podman --project-name reform --file docker-compose.yml down
podman-compose --env-file .env.podman --project-name reform --file docker-compose.yml up --pull --build --remove-orphans
podman-compose --env-file .env.podman --project-name reform --file docker-compose.yml exec reform-discovery npm run user:add
```

The application is available on http://reform.localhost:8888/ by default

## Build inside docker

Building the peer

```bash
docker-compose -f docker-compose-dev.yml up -d --build peer-dev
docker run --rm -v "$PWD/dist:/app/dist" reform-peer-dev:latest npm run build # Might also be called reform_peer-dev:latest
docker-compose -f docker-compose-dev.yml down
```
