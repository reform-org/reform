import { parseArgs } from "node:util";
import { By, until } from "selenium-webdriver";
import { Peer } from "./lib.js";

let peer = await Peer.create(false)
await peer.driver.get("http://localhost:5173/webrtc-compression")
await peer.driver.wait(until.elementTextIs(await peer.driver.findElement(By.id("webrtc-compression-count")), "400"))
let element = await peer.driver.findElement(By.id("webrtc-compression"))
console.log(await element.getText())