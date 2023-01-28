import { parseArgs } from "node:util";
import { By, until } from "selenium-webdriver";
import { Peer } from "./lib.js";

/*
mkdir webrtc-compression
cd webrtc-compression

# repeat this a few times
SELENIUM_BROWSER=chrome node --loader ts-node/esm webapp/tests/selenium/webrtc-compression.ts >> ../chrome
SELENIUM_BROWSER=firefox node --loader ts-node/esm webapp/tests/selenium/webrtc-compression.ts >> ../firefox

split --lines=10 ../chrome ../firefox
zstd --train * -o ../dictionary

*/

let peer = await Peer.create(false)
await peer.driver.get("http://localhost:5173/webrtc-compression")
await peer.driver.wait(until.elementTextIs(await peer.driver.findElement(By.id("webrtc-compression-count")), "400"))
let element = await peer.driver.findElement(By.id("webrtc-compression"))
console.log(await element.getText())