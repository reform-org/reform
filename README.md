# reform

## Prerequisites

You should make sure that the following components are pre-installed on your machine:

 - [Node.js](https://nodejs.org/en/download/)
 - [sbt](https://www.scala-sbt.org/)

## Development

Cleaning the project:
```
rm -R ~/.cache/scalablytyped
sbt clean
```

Warning: Firefox is not able to load the Source maps, use Chromium.

Install node modules:
```
npm install
```

Install pre-commit hook (recommended)

```bash
ln -sr pre-commit.sh .git/hooks/pre-commit
```
For Windows: (run in elevated commandprompt)
```bash
cmd /c mklink ".git\\hooks\\pre-commit" "..\\..\\pre-commit.sh"
```

Then start the Scala.js build server with:
```bash
sbt ~fastLinkJS
```

In another window start the web dev server with:
```bash
npm run dev
```

If you want to run the selenium tests you need to run:
```bash
VITE_SELENIUM=true npm run dev
```

To start the always-on server
```bash
sbt webappJVM/run
```

Then open linked instances in your browser:

```
npm run spawn-test-instances -- --count 2 --url http://localhost:5173/
```

Or connect them manually, but use a temp dir:

```
setsid -f chromium --user-data-dir=$(mktemp -d) http://localhost:5173/
```

Dependency tree:
```
sbt dependencyBrowseTreeHTML
```

## Testing

```
sbt "~Test / fastLinkJS"
```

In a separate window:
```
npm run test
```

```bash
sbt webappJS/test
```

## Deployment

```bash
# netlify
#curl -fLo coursier https://github.com/coursier/launchers/raw/master/coursier && chmod +x coursier && ./coursier setup --yes && ~/.local/share/coursier/bin/sbt fastLinkJS && npm ci && npm run build

#curl -fLo coursier https://github.com/coursier/launchers/raw/master/coursier && chmod +x coursier && ./coursier setup --yes && ~/.local/share/coursier/bin/sbt coverage webappJVM/test webappJVM/coverageReport

# TODO maybe this works with netlify and can install other java version?
curl -s "https://get.sdkman.io" | bash && source ~/.sdkman/bin/sdkman-init.sh && sdk install java && sdk install sbt && sbt fastLinkJS && npm ci && npm run build


curl -s "https://get.sdkman.io" | bash && source ~/.sdkman/bin/sdkman-init.sh && sdk install java && sdk install sbt && sbt coverage webappJVM/test webappJVM/coverageReport

```
