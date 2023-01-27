import { Peer } from "./lib.js";
import { assert, describe, expect, it, test } from "vitest";

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

describe.concurrent("safari-incompatible", () => {
	it("connects", async () => {
		let peers = await startPeers(2);
		await loadPage(peers);
		await peers[0].connectTo(peers[1]);
		await quitPeers(peers);
	});
});
