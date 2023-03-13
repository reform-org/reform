import { parseArgs } from "node:util";
import { Peer } from "./lib.js";
import readline from "readline";

const rl = readline.createInterface({
	input: process.stdin,
	output: process.stdout,
});

console.log(
	"usage: npm run spawn-test-instances -- <count> <url>",
);

if (process.env.SELENIUM_BROWSER === undefined) {
	throw new Error("Please run `export SELENIUM_BROWSER=chrome` or so");
}

const count = process.argv[2]
const url = process.argv[3]

console.log(`Starting ${count} instances on ${url}`);

let peers = await Promise.all(
	[...Array(Number(count))].map(async () => {
		return await Peer.create(false);
	}),
);

try {
	const it = rl[Symbol.asyncIterator]();
	while (true) {
		console.log("CONNECTING ALL");
		await Promise.all(peers.map((peer) => peer.driver.get(url!)));

		await peers
			.map((v) => Promise.resolve(v))
			.reduce(async (prev, curr) => {
				await (await prev).connectTo(await curr);
				await (await curr).driver.sleep(100);
				return await curr;
			});
		console.log("ALL CONNECTED");

		await it.next();
	}
} finally {
	await Promise.allSettled(peers.map((peer) => peer.driver.quit()));
}
