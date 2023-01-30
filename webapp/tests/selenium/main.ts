import { Actions, chance, check, Peer, seed } from "./lib.js";
import { strict as assert } from "node:assert";
import browserstack from "browserstack-local";
import { promisify } from 'util'

export async function run() {
	let actions;
	let peers: Peer[];
	if (process.env.SELENIUM_BROWSER === "safari" || process.env.BROWSERSTACK_ACCESS_KEY) {
		actions = chance.n(
			() => chance.weighted([Actions.CREATE_PROJECT, Actions.RELOAD], [10, 10]),
			200,
		);
		let peer = await Peer.create(true);
		await peer.driver.get("http://localhost:5173/");
		peers = [peer];
	} else {
		actions = chance.n(
			() =>
				chance.weighted(
					[
						Actions.CREATE_PEER,
						Actions.DELETE_PEER,
						Actions.CREATE_PROJECT,
						Actions.EDIT_PROJECT,
						Actions.CONNECT_TO_PEER,
						Actions.RELOAD,
					],
					[10, 10, 20, 20, 20, 10],
				),
			200,
		);
		peers = [];
	}

	//actions = [Actions.CREATE_PEER, Actions.CREATE_PROJECT, Actions.CREATE_PROJECT, Actions.EDIT_PROJECT];

	try {
		for (let action of actions) {
			switch (action) {
				case Actions.CREATE_PEER: {
					let peer = await Peer.create(false);
					await peer.driver.get("http://localhost:5173/");
					peers.push(peer);
					console.log(`[${peer.id}] peer created`);
					break;
				}
				case Actions.CREATE_PROJECT: {
					if (peers.length === 0) {
						continue;
					}
					let random_peer: Peer = chance.pickone(peers);
					await random_peer.createProject();
					break;
				}
				case Actions.EDIT_PROJECT: {
					if (peers.length === 0) {
						continue;
					}
					let random_peer: Peer = chance.pickone(peers);
					if (random_peer.projects.value.size == 0) {
						continue;
					}					
					let randomProject = chance.pickone([...random_peer.projects.value.keys()]);
					await random_peer.editProject(randomProject);
					break;
				}
				case Actions.CONNECT_TO_PEER: {
					if (peers.length < 2) {
						continue;
					}
					let peersToConnect = chance.pickset(peers, 2);
					await peersToConnect[0].connectTo(peersToConnect[1]);

					//console.log("Connected")

					break;
				}
				case Actions.RELOAD: {
					if (peers.length === 0) {
						continue;
					}
					let random_peer: Peer = chance.pickone(peers);
					console.log(`[${random_peer.id}] reload`);

					await random_peer.driver.navigate().refresh();

					random_peer.connectedTo = [];
					for (let peer of peers) {
						// TODO does this work?
						peer.connectedTo = peer.connectedTo.filter(
							(p) => p !== random_peer,
						);
						if (peer.connectedTo.includes(random_peer)) {
							assert.fail("internal failure");
						}
					}

					//console.log(peers)

					break;
				}
				case Actions.DELETE_PEER: {
					if (peers.length === 0) {
						continue;
					}
					let random_peer: Peer = chance.pickone(peers);
					console.log(`[${random_peer.id}] delete peer`);

					await random_peer.driver.quit();

					random_peer.connectedTo = [];
					for (let peer of peers) {
						peer.connectedTo = peer.connectedTo.filter(
							(p) => p !== random_peer,
						);
					}

					peers = peers.filter((p) => p !== random_peer);

					break;
				}
				default: {
					throw new Error("unhandled");
				}
			}
			await check(peers);
		}

		if (process.env.BROWSERSTACK_ACCESS_KEY) {
			await peers[0].driver.executeScript(
				'browserstack_executor: {"action": "setSessionStatus", "arguments": {"status":"passed","reason": "Success!"}}',
			);
		}

		await Promise.all(peers.map((peer) => peer.driver.close()));

		console.log("DONE");
	} catch (error) {
		console.error(error);
		console.log("seed: ", seed);

		if (process.env.BROWSERSTACK_ACCESS_KEY) {
			await peers[0].driver.executeScript(
				'browserstack_executor: {"action": "setSessionStatus", "arguments": {"status":"failed","reason": "Failed!"}}',
			);
		}

		//await Promise.allSettled(peers.map((peer) => peer.driver.close()));

		throw error;
	}
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

if (process.env.BROWSERSTACK_ACCESS_KEY) {
	await start()
}

await run()

if (process.env.BROWSERSTACK_ACCESS_KEY) {
	await stop()
}
