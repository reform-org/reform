import { Peer } from "./lib.js";
import { afterAll, assert, beforeAll, describe, expect, it, test } from "vitest";
import browserstack from "browserstack-local";
import { promisify } from 'util'

let headless = true;

async function startPeers(count: number) {
	return await Promise.all(
		[...Array(Number(count))].map(async () => {
			let peer = await Peer.create(headless);
			return peer;
		}),
	);
}

async function loadPage(peers: Peer[]) {
	await Promise.all(
		peers.map(async (peer) => peer.driver.get("http://localhost:5173/")),
	);
}

async function quitPeers(peers: Peer[]) {
	await Promise.allSettled(peers.map((peer) => peer.driver.quit()));
}

const bs_local = new browserstack.Local();
const start = () => {
	console.log("start")
	return new Promise<void>((resolve, reject) => {
		bs_local.start({}, (error) => {
			console.log(error)
			resolve()
		})
	})
}
const stop = () => {
	console.log("stop")
	return new Promise<void>((resolve, reject) => {
		bs_local.stop(() => {
			resolve()
		})
	})
}

beforeAll(async () => {
	if (process.env.BROWSERSTACK_ACCESS_KEY) {
		await start()
	}
})

afterAll(async () => {
	if (process.env.BROWSERSTACK_ACCESS_KEY) {
		await stop()
	}
})

// tests that can also run on mac:
describe.concurrent("safari-compatible", () => {
	it("creates project", async () => {
		let [peer] = await startPeers(1);
		await loadPage([peer]);
		await peer.createProject();
		await quitPeers([peer]);
	});

	it("loads page", async () => {
		let [peer] = await startPeers(1);
		await loadPage([peer]);
		await quitPeers([peer]);
	});
});

describe
	.skipIf(process.env.SELENIUM_BROWSER === "safari")
	.concurrent("safari-incompatible", () => {
		it("connects", async () => {
			let peers = await startPeers(2);
			await loadPage(peers);
			await peers[0].connectTo(peers[1]);
			await quitPeers(peers);
		});
	});
