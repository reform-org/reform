import "./fast-selenium.js";
import {
	Builder,
	By,
	Condition,
	until,
	WebDriver,
} from "selenium-webdriver";
import chrome from "selenium-webdriver/chrome.js";
import firefox from "selenium-webdriver/firefox.js";
import safari from "selenium-webdriver/safari.js";
import { Chance } from "chance";
import { strict as assert } from "node:assert";

export let seed = new Chance().integer();
//let seed = 5475712614006784;
export var chance = new Chance(seed);
console.log(`The seed is: ${chance.seed}`);

// The code in here (especially the selenium communication) needs to be optimized well because it will be run a lot in tests.

// TODO FIXME formatting and eslint with types

export const Actions = Object.freeze({
	CREATE_PEER: Symbol("CREATE_PEER"),
	DELETE_PEER: Symbol("DELETE_PEER"),
	CREATE_PROJECT: Symbol("CREATE PROJECT"),
	CONNECT_TO_PEER: Symbol("CONNECT_TO_PEER"),
	RELOAD: Symbol("RELOAD"),
});

interface Mergeable {
	merge(other: ThisType<this>): ThisType<this>;
}

class LastWriterWins<T> implements Mergeable {
	value: T;
	time: Date;

	constructor(value: T, time: Date) {
		this.value = value;
		this.time = time;
	}

	merge(other: LastWriterWins<T>): LastWriterWins<T> {
		if (this.time > other.time) {
			return this;
		} else {
			return other;
		}
	}
}

class PosNegCounter implements Mergeable {
	value: Map<string, number>;

	constructor(value: Map<string, number>) {
		this.value = value;
	}

	merge(other: PosNegCounter): PosNegCounter {
		let map = new Map();
		for (let [key, value] of [
			...this.value.entries(),
			...other.value.entries(),
		]) {
			map.set(key, Math.max(map.get(key) || 0, value));
		}
		return new PosNegCounter(map);
	}
}

class Project implements Mergeable {
	name: LastWriterWins<string>;
	maxHours: PosNegCounter;
	account: LastWriterWins<string>;

	constructor(
		name: LastWriterWins<string>,
		maxHours: PosNegCounter,
		account: LastWriterWins<string>,
	) {
		this.name = name;
		this.maxHours = maxHours;
		this.account = account;
	}

	public static create(
		replicaId: string,
		name: string,
		maxHours: number,
		account: string,
	) {
		return new Project(
			new LastWriterWins(name, new Date()),
			new PosNegCounter(new Map([[replicaId, maxHours]])),
			new LastWriterWins(account, new Date()),
		);
	}

	merge(other: Project): Project {
		return new Project(
			this.name.merge(other.name),
			this.maxHours.merge(other.maxHours),
			this.account.merge(other.account),
		);
	}
}

class MergeableList<T> implements Mergeable {
	value: Map<string, T>;

	constructor(value: Map<string, T>) {
		this.value = value;
	}

	merge(other: MergeableList<T>): MergeableList<T> {
		let map = new Map();
		for (let [key, value] of [
			...this.value.entries(),
			...other.value.entries(),
		]) {
			map.set(key, map.get(key) ? map.get(key).merge(value) : value);
		}
		return new MergeableList(map);
	}
}

export class Peer {
	id: string;
	driver: WebDriver;
	projects: MergeableList<Project>;
	connectedTo: Peer[];

	constructor(id: string, driver: WebDriver, projects: MergeableList<Project>) {
		this.id = id;
		this.driver = driver;
		this.projects = projects;
		this.connectedTo = [];
	}

	async createProject() {
		console.log(`[${this.id}] create project`);
		// only on mobile:
		// let dropdown = await random_peer.driver.findElement(By.id("dropdown-button"))
		// await dropdown.click()
		if (process.env.SELENIUM_BROWSER === "safari") {
			await this.driver.wait(until.elementLocated(
				By.css(".navbar-center a[href='/projects']"),
			), 10000);
			await this.driver.sleep(500)
		}
		await (await this.driver.findElement(
			By.css(".navbar-center a[href='/projects']"),
		)).click();

		let projectNameInput = await this.driver.findElement(
			By.css("input[placeholder='Name']"),
		);
		let maxHoursInput = await this.driver.findElement(
			By.css("input[placeholder='Max Hours']"),
		);
		let accountInput = await this.driver.findElement(
			By.css("input[placeholder='Account']"),
		);
		let addProjectButton = await this.driver.findElement(
			By.xpath(`//button[text()="Add Entity"]`),
		);

		let projectName = chance.animal();
		let maxHours = chance.integer({ min: 1, max: 10 });
		let account = chance.name();
		await projectNameInput.sendKeys(projectName);
		await maxHoursInput.sendKeys(maxHours);
		await accountInput.sendKeys(account);

		await addProjectButton.click();

		let addedProject = await this.driver.wait(async () => {
			let ids = await Promise.all(
				(
					await this.driver.findElements(By.css("tr[data-id]"))
				).map((e) => e.getAttribute("data-id")),
			);
			let oldIds = new Set([...this.projects.value.keys()]);

			let addedProjects = new Set([...ids].filter((x) => !oldIds.has(x)));
			if (addedProjects.size !== 1) {
				console.error(
					"couldn't identify which project was added",
					addedProjects,
				);
				return false;
			}
			return [...addedProjects.values()][0];
		}, 1000);
		if (addedProject === false) {
			throw new Error("couldn't identify which project was added");
		}

		this.projects.value.set(
			addedProject,
			Project.create(this.id, projectName, maxHours, account),
		);
	}

	async goToWebRTCPage() {
		try {
			let dropDown = await this.driver.findElement(
				By.css("div.dropdown")
			);
			await dropDown.click();
		} catch (ignoreNotMobile) {
		}
		await this.driver.sleep(100);

		let webrtcButton = await this.driver.findElement(
			By.css("a[href='/webrtc']"),
		);
		await webrtcButton.click();
	}

	async connectTo(other: Peer) {
		console.log(`[${this.id}, ${other.id}] connect`);
		let [[offerInput, submitOffer], [offer, answerInput, answerSubmit]] =
			await Promise.all([
				(async () => {
					let driver = this.driver;

					await this.goToWebRTCPage();

					let clientButton = await driver.findElement(
						By.xpath(`//button[text()="Client"]`),
					);
					await clientButton.click();

					let textarea = await driver.findElement(By.css("textarea"));

					let submitOffer = await driver.findElement(
						By.xpath(`//button[text()="Connect to host using token"]`),
					);

					return [textarea, submitOffer];
				})(),
				(async () => {
					let driver = other.driver;

					await other.goToWebRTCPage();

					let hostButton = await driver.findElement(
						By.xpath(`//button[text()="Host"]`),
					);
					await hostButton.click();

					let offer = await driver.findElement(By.css("div.overflow-x-auto"));
					await driver.wait(until.elementTextMatches(offer, /.+/), 1000);
					let value = await offer.getText();
					//console.log(`got offer: ${value}`)

					let answerInput = await driver.findElement(By.css("textarea"));

					let answerSubmit = await driver.findElement(
						By.xpath(`//button[text()="Connect to client using token"]`),
					);

					return [value, answerInput, answerSubmit];
				})(),
			]);
		await offerInput.sendKeys(offer);
		await submitOffer.click();

		let answer = await this.driver.findElement(By.css("div.overflow-x-auto"));
		await this.driver.wait(until.elementTextMatches(answer, /.+/), 1000);
		let value = await answer.getText();
		//console.log(`got answer: ${value}`)

		await answerInput.sendKeys(value);
		await answerSubmit.click();

		await Promise.all(
			[this, other].map(async (peer) => {
				await peer.driver.wait(
					until.elementLocated(By.xpath(`//h2[text()="Connected"]`)),
				);
			}),
		);

		this.connectedTo.push(other);
		other.connectedTo.push(this);
	}

	public static async create(headless: boolean) {
		let chromeOptions = new chrome.Options().windowSize({
			width: 1200,
			height: 750,
		});

		if (headless) {
			chromeOptions = chromeOptions.headless();
		}

		let firefoxOptions = new firefox.Options().windowSize({
			width: 1200,
			height: 750,
		});

		if (headless) {
			firefoxOptions = firefoxOptions.headless();
		}

		const capabilities: Record<string, {}> = {
			chrome: {
				"bstack:options": {
					local: "true",
					debug: "true",
					consoleLogs: "info",
					os: "OS X",
					osVersion: "Ventura",
					browserVersion: "109",
					buildName: `${new Date()}`,
					sessionName: "Chrome",
				},
				browserName: "Chrome",
			},
			firefox: {
				"bstack:options": {
					local: "true",
					debug: "true",
					consoleLogs: "info",
					os: "OS X",
					osVersion: "Ventura",
					browserVersion: "109",
					buildName: `${new Date()}`,
					sessionName: "Firefox",
				},
				browserName: "Firefox",
			},
			safari: {
				"bstack:options": {
					local: "true",
					debug: "true",
					consoleLogs: "info",
					os: "OS X",
					osVersion: "Ventura",
					browserVersion: "16",
					buildName: `${new Date()}`,
					sessionName: "Safari",
				},
				browserName: "Safari",
			},
		};

		let driver = new Builder()
			.withCapabilities(capabilities[process.env.SELENIUM_BROWSER!])
			.setChromeOptions(chromeOptions)
			.setFirefoxOptions(firefoxOptions)
			.setSafariOptions(new safari.Options())
			.build();

		await driver
			.manage()
			.window()
			.setRect({ width: 1200, height: 750 });

		let id = (await driver.getSession()).getId();

		return new Peer(id, driver, new MergeableList(new Map()));
	}
}

export async function check(peers: Peer[]) {
	if (peers.length === 0) {
		return;
	}

	// emulate sync
	for (let i = 0; i < peers.length; i++) {
		for (let peer of peers) {
			for (let remote of peer.connectedTo) {
				let newProjects1 = peer.projects.merge(remote.projects);
				let newProjects2 = peer.projects.merge(remote.projects);

				peer.projects = newProjects1;
				remote.projects = newProjects2;
			}
		}
	}

	let condition = new Condition<boolean>(
		"all peers are fully synced",
		async () => {
			console.log("condition");
			let results = await Promise.all(
				peers.map<Promise<number>>(async (peer) => {
					if (process.env.SELENIUM_BROWSER === "safari") {
						await peer.driver.wait(until.elementLocated(
							By.css(".navbar-center a[href='/projects']"),
						), 10000);
						await peer.driver.sleep(500)
					}
					await (await peer.driver.findElement(
						By.css(".navbar-center a[href='/projects']"),
					)).click();

					let projects = await peer.driver.findElements(
						By.css("tbody tr[data-id]"),
					);

					let expectedProjects = [...peer.projects.value.entries()]
						.map(([k, v]) => {
							return [
								k,
								v.name.value,
								[...v.maxHours.value.values()].reduce(
									(partialSum, a) => partialSum + a,
									0,
								),
								v.account.value,
							];
						})
						.sort((a, b) => a[0].toString().localeCompare(b[0].toString()));

					let actualProjects = (
						await Promise.all(
							projects.map(async (project) => {
								let id = await project.getAttribute("data-id");
								let tds = await project.findElements(By.css("td"));
								let name = await tds[0].getText();
								let maxHours = Number.parseInt(await tds[1].getText());
								let account = await tds[2].getText();

								return [id, name, maxHours, account];
							}),
						)
					).sort((a, b) => a[0].toString().localeCompare(b[0].toString()));

					try {
						assert.deepEqual(actualProjects, expectedProjects);
						return 1;
					} catch (error) {
						console.error(error);
						console.log("should try again");
						return 0;
					}
				}),
			);
			let result = results.reduce((prev, curr) => prev + curr);
			if (result === peers.length) {
				return true;
			} else {
				return null;
			}
		},
	);

	let result = await peers[0].driver.wait(
		condition,
		20000,
		"waiting for peers synced timed out",
	);
	assert.strictEqual(result, true);
}
