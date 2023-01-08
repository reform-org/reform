# reform

## Prerequisites

You should make sure that the following components are pre-installed on your machine:

 - [Node.js](https://nodejs.org/en/download/)
 - [sbt](https://www.scala-sbt.org/)

## Development

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

Then open `http://localhost:5173/` in your browser.

```
setsid -f chromium --user-data-dir=$(mktemp -d) http://localhost:5173/
```

## Testing

```
sbt "~Test / fastLinkJS"
```

In a separate window:
```
npm run test
```
