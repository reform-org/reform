# reform

## Prerequisites

You should make sure that the following components are pre-installed on your machine:

 - [Node.js](https://nodejs.org/en/download/)

## Setup

```bash
git clone https://github.com/reform-org/reform
cd reform
git submodule update --init
cd fun-pack
npm ci
cd ..
sbt fullOptJS
```

## Development

Install pre-commit hook (recommended)

```bash
ln -srf pre-commit.sh .git/hooks/pre-commit
```
For Windows: (run in elevated commandprompt)
```bash
cmd /c mklink ".git\\hooks\\pre-commit" "..\\..\\pre-commit.sh"
```

The pre-commit hook starts an sbt build server for better performance which you can stop with the following command:
```bash
sbt --client shutdown
```

You can also start or use that server with
```bash
sbt --client
```
yourself.

To develop the application run
```sh
sbt dev
```

Then open `http://localhost:12345` in your browser.

This sbt-task will start webpack dev server, compile your code each time it changes and auto-reload the page.  
Webpack dev server will stop automatically when you stop the `dev` task
(e.g by hitting `Enter` in the sbt shell while you are in `dev` watch mode).

If you existed ungracefully and your webpack dev server is still open (check with `ps -aef | grep -v grep | grep webpack`),
you can close it by running `fastOptJS::stopWebpackDevServer` in sbt.
