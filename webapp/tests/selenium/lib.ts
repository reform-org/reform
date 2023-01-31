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

//export let seed = new Chance().integer();
export let seed = 7196640142163968;
export var chance = new Chance(seed);
console.log(`The seed is: ${chance.seed}`);

// The code in here (especially the selenium communication) needs to be optimized well because it will be run a lot in tests.

// TODO FIXME formatting and eslint with types

export const Actions = Object.freeze({
	CREATE_PEER: Symbol("CREATE_PEER"),
	DELETE_PEER: Symbol("DELETE_PEER"),
	CREATE_PROJECT: Symbol("CREATE PROJECT"),
	EDIT_PROJECT: Symbol("EDIT_PROJECT"),
	CONNECT_TO_PEER: Symbol("CONNECT_TO_PEER"),
	BAD_NETWORK: Symbol("BAD_NETWORK"),
	GOOD_NETWORK: Symbol("GOOD_NETWORK"),
	RELOAD: Symbol("RELOAD"),
});

interface Mergeable {
	merge(other: ThisType<this>): ThisType<this>;
}

type ReplicaId = string
type LogicalClock = number

function compare(left: Map<ReplicaId, LogicalClock>, right: Map<ReplicaId, LogicalClock>): number {
	let allReplicaIds = [...left.keys(), ...right.keys()]
	let smaller = false
	let greater = false
	for (let replicaId of allReplicaIds) {
		let l = left.get(replicaId) || 0
		let r = right.get(replicaId) || 0
		if (l < r) {
			smaller = true;
		}
		if (r < l) {
			greater = true;
		}
	}
	return (smaller && !greater) ? 1 : ((greater && !smaller) ? -1 : 0)
}

class MultiValueRegister<T> implements Mergeable {
	values: Map<Map<ReplicaId, LogicalClock>, T>

	constructor(values: Map<Map<ReplicaId, LogicalClock>, T>) {
		this.values = values
	}

	value(): T | null {
		if (this.values.size != 1) {
			console.log("multiple values")
			return null
		}
		return this.values.values().next().value
	}

	set(replicaId: string, value: T): MultiValueRegister<T> {
		let allReplicaIds = [...this.values.keys()].flatMap(v => [...v.keys(), replicaId])
		let clock = allReplicaIds.map(r => {
			let max = [...this.values.keys()].flatMap(z => {
				let v = z.get(r)
				return v ? [v] : [] 
			}).reduce((a, b) => {
				return Math.max(a, b)
			}, 0)
			if (r == replicaId) {
				return [r, max+1] as const
			} else {
				return [r, max] as const
			}
		})
		return new MultiValueRegister(new Map([[new Map(clock), value]]))
	}

	merge(other: MultiValueRegister<T>): MultiValueRegister<T> {
		console.log("this", this)
		console.log("other", other)
		let all = [
			...this.values.entries(),
			...other.values.entries(),
		];
		let result: [Map<string, number>, T][] = []
		for (let i = 0; i < all.length; i++) {
			let greatest = true;
			for (let j = 0; j < all.length; j++) {
				if (i == j) continue;
				if (compare(all[i][0], all[j][0]) > 0) {
					greatest = false
				}
			}
			if (greatest) {
				if (!result.includes(all[i])) {
					result.push(all[i])
				}
			}
		}
		console.log("merged", result)
		return new MultiValueRegister(new Map(result))
	}
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
	name: MultiValueRegister<string>;
	maxHours: MultiValueRegister<number>;
	account: MultiValueRegister<string>;

	constructor(
		name: MultiValueRegister<string>,
		maxHours: MultiValueRegister<number>,
		account: MultiValueRegister<string>,
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
			new MultiValueRegister(new Map([[new Map([[replicaId, 1]]), name]])),
			new MultiValueRegister(new Map([[new Map([[replicaId, 1]]), maxHours]])),
			new MultiValueRegister(new Map([[new Map([[replicaId, 1]]), account]])),
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

	async editProject(projectId: string) {
		console.log(`[${this.id}] edit project ${projectId}`);

		let row = await this.driver.findElement(By.css(`tr[data-id='${projectId}']`))

		let editProjectButton = await row.findElement(
			By.xpath(`.//button[text()="Edit"]`),
		);
		await editProjectButton.click()

		let projectNameInput = await row.findElement(
			By.css("input[placeholder='Name']"),
		);
		let maxHoursInput = await row.findElement(
			By.css("input[placeholder='Max Hours']"),
		);
		let accountInput = await row.findElement(
			By.css("input[placeholder='Account']"),
		);

		let projectName = chance.animal();
		let maxHours = chance.integer({ min: 1, max: 10 });
		let account = chance.name();

		await projectNameInput.clear();
		await projectNameInput.sendKeys(projectName);

		await maxHoursInput.clear();
		await maxHoursInput.sendKeys(maxHours);

		await accountInput.clear()
		await accountInput.sendKeys(account);

		await (await row.findElement(By.xpath('//button[text()="Save edit"]'))).click()

		let oldProject = this.projects.value.get(projectId)!;
		let newProject = new Project(
			oldProject.name.set(this.id, projectName),
			oldProject.maxHours.set(this.id, maxHours),
			oldProject.account.set(this.id, account))

		this.projects.value.set(
			projectId,
			newProject,
		);
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
			By.xpath(`.//button[text()="Add Entity"]`),
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
			let webrtcButton = await this.driver.findElement(
				By.css(".navbar-start a[href='/webrtc']"),
			);
			await webrtcButton.click();
		} catch (ignoreNotMobile) {
			let webrtcButton = await this.driver.findElement(
				By.css(".navbar-center a[href='/webrtc']"),
			);
			await webrtcButton.click();
		}
	}

	async connectTo(other: Peer) {
		console.log(`[${this.id}, ${other.id}] connect`);
		let [[offerInput, submitOffer], [offer, answerInput, answerSubmit]] =
			await Promise.all([
				(async () => {
					let driver = this.driver;

					await this.goToWebRTCPage();

					let clientButton = await driver.findElement(
						By.xpath(`.//button[text()="Client"]`),
					);
					await clientButton.click();

					let textarea = await driver.findElement(By.css("textarea"));

					let submitOffer = await driver.findElement(
						By.xpath(`.//button[text()="Connect to host using token"]`),
					);

					return [textarea, submitOffer];
				})(),
				(async () => {
					let driver = other.driver;

					await other.goToWebRTCPage();

					let hostButton = await driver.findElement(
						By.xpath(`.//button[text()="Host"]`),
					);
					await hostButton.click();

					let offer = await driver.findElement(By.css("div.overflow-x-auto"));
					await driver.wait(until.elementTextMatches(offer, /.+/), 1000);
					let value = await offer.getText();
					//console.log(`got offer: ${value}`)

					let answerInput = await driver.findElement(By.css("textarea"));

					let answerSubmit = await driver.findElement(
						By.xpath(`.//button[text()="Connect to client using token"]`),
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
					until.elementLocated(By.xpath(`.//h2[text()="Connected"]`)),
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
								v.name.value(),
								v.maxHours.value(),
								v.account.value(),
							];
						})
						.sort((a, b) => a[0]?.toString().localeCompare(b[0]?.toString()||"") || -1);

					let actualProjects = (
						await Promise.all(
							projects.map(async (project) => {
								let id = await project.getAttribute("data-id");
								let tds = await project.findElements(By.css("td"));


								let nameConflicted = (await tds[0].findElements(By.css(".tooltip-error"))).length == 1
								let name = await tds[0].getText();
								let maxHoursConflicted = (await tds[1].findElements(By.css(".tooltip-error"))).length == 1
								let maxHours = Number.parseInt(await tds[1].getText());
								let accountConflicted = (await tds[2].findElements(By.css(".tooltip-error"))).length == 1
								let account = await tds[2].getText();



								return [id, nameConflicted ? null : name, maxHoursConflicted ? null : maxHours, accountConflicted ? null : account];
							}),
						)
					).sort((a, b) => a[0]?.toString().localeCompare(b[0]?.toString()||"") || -1);

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
