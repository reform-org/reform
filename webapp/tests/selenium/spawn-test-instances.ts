import { parseArgs } from "node:util";
import { Peer } from "./lib.js";
import readline from "readline";

const rl = readline.createInterface({
	input: process.stdin,
	output: process.stdout,
});

console.log(
	"usage: npm run spawn-test-instances -- [--count count] [--url ...]",
);

const m = process.version.match(/(\d+)\.(\d+)\.(\d+)/)!;
const [major, minor, patch] = m.slice(1).map((_) => parseInt(_));

if (!(major > 18 || (major == 18 && minor >= 11))) {
	console.warn(`\x1b[41m\x1b[30m
┌──────────────────────────────────────────────────────────────────┐
│                                                                  │
│    NodeJS is less than v18.11.0, this version is unsupported!    │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘\x1b[0m`);
}

let {
	values: { count, url },
} = parseArgs({
	options: {
		count: {
			type: "string",
			default: "2",
			short: "c",
		},
		url: {
			type: "string",
			default: "http://localhost:5173/",
		},
	},
});

if (process.env.SELENIUM_BROWSER === undefined) {
	throw new Error("Please run `export SELENIUM_BROWSER=chrome` or so");
}

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
