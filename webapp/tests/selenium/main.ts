import { Builder, By, checkedLocator, Condition, until, WebDriver, WebElement } from 'selenium-webdriver';
import chrome from 'selenium-webdriver/chrome.js';
import firefox from 'selenium-webdriver/firefox.js';
import { Chance } from 'chance';
import { Driver } from 'selenium-webdriver/chrome';
import { strict as assert } from 'node:assert';

// The code in here (especially the selenium communication) needs to be optimized well because it will be run a lot in tests.

// TODO FIXME formatting and eslint with types

const Actions = Object.freeze({
    CREATE_PEER: Symbol("CREATE_PEER"),
    DELETE_PEER: Symbol("DELETE_PEER"),
    CREATE_PROJECT: Symbol("CREATE PROJECT"),
    CONNECT_TO_PEER: Symbol("CONNECT_TO_PEER"),
    RELOAD: Symbol("RELOAD"),
});

interface Mergeable {
    merge(other: ThisType<this>): ThisType<this>
}

class LastWriterWins<T> implements Mergeable {
    value: T
    time: Date

    constructor(value: T, time: Date) {
        this.value = value
        this.time = time
    }

    merge(other: LastWriterWins<T>): LastWriterWins<T> {
        if (this.time > other.time) {
            return this
        } else {
            return other
        }
    }
}

class PosNegCounter implements Mergeable {
    value: Map<string, number>

    constructor(value: Map<string, number>) {
        this.value = value
    }

    merge(other: PosNegCounter): PosNegCounter {
        let map = new Map()
        for (let [key, value] of [...this.value.entries(), ...other.value.entries()]) {
            map.set(key, Math.max(map.get(key) || 0, value))
        }
        return new PosNegCounter(map)
    }
}

class Project implements Mergeable {
    name: LastWriterWins<string>
    maxHours: PosNegCounter
    account: LastWriterWins<string>

    constructor(name: LastWriterWins<string>, maxHours: PosNegCounter, account: LastWriterWins<string>) {
        this.name = name
        this.maxHours = maxHours
        this.account = account
    }

    public static create(replicaId: string, name: string, maxHours: number, account: string) {
        return new Project(
            new LastWriterWins(name, new Date()),
            new PosNegCounter(new Map([[replicaId, maxHours]])),
            new LastWriterWins(account, new Date()))
    }

    merge(other: Project): Project {
        return new Project(
            this.name.merge(other.name),
            this.maxHours.merge(other.maxHours),
            this.account.merge(other.account))
    }
}

class MergeableList<T> implements Mergeable {
    value: Map<string, T>

    constructor(value: Map<string, T>) {
        this.value = value
    }

    merge(other: MergeableList<T>): MergeableList<T> {
        let map = new Map()
        for (let [key, value] of [...this.value.entries(), ...other.value.entries()]) {
            map.set(key, map.get(key) ? map.get(key).merge(value) : value)
        }
        return new MergeableList(map)
    }
}

class Peer {
    id: string
    driver: WebDriver
    projects: MergeableList<Project>
    connectedTo: Peer[]

    constructor(id: string, driver: WebDriver, projects: MergeableList<Project>) {
        this.id = id
        this.driver = driver
        this.projects = projects
        this.connectedTo = []
    }

    public static async create() {
        let driver = new Builder()
            .forBrowser('chrome')
            .setChromeOptions(new chrome.Options()
                .windowSize({ width: 2000, height: 750 })
                .headless()
            )
            //.setFirefoxOptions(/* ... */)
            .build();

        await driver.manage().setTimeouts({
            script: 10000,
        })

        let id = (await driver.getSession()).getId();
/*
        const cdpConnection = await driver.createCDPConnection('page');
        await driver.onLogEvent(cdpConnection, function (event) {
            console.log(`[${id}] ${event['args'][0]['value']}`);
        });
        await driver.onLogException(cdpConnection, function (event) {
            console.log(`[${id}] ${event['exceptionDetails']}`);
        })*/

        return new Peer(
            id,
            driver,
            new MergeableList(new Map()))
    }
}

async function check(peers: Peer[]) {
    if (peers.length === 0) {
        return;
    }

    // emulate sync
    for (let i = 0; i < peers.length; i++) {
        for (let peer of peers) {
            for (let remote of peer.connectedTo) {
                //console.log(`sync between ${peer.id} and ${remote.id}`)
                // TODO FIXME the push somewhere down breaks this
                let newProjects1 = peer.projects.merge(remote.projects)
                let newProjects2 = peer.projects.merge(remote.projects)

                peer.projects = newProjects1
                remote.projects = newProjects2
            }
        }
    }
    
    let condition = new Condition<boolean>("all peers are fully synced", async () => {
        console.log("condition");
        let results = await Promise.all(peers.map<Promise<number>>(async peer => {
            let projectsButton = await peer.driver.findElement(By.css(".navbar-center a[href='/projects']"))
            await projectsButton.click()

            let projects = await peer.driver.findElements(By.css("tbody tr[data-id]"));

            let expectedProjects = [...peer.projects.value.entries()].map(([k, v]) => {
                return [k, v.name.value, [...v.maxHours.value.values()].reduce((partialSum, a) => partialSum + a, 0), v.account.value]
            }).sort((a, b) => a[0].toString().localeCompare(b[0].toString()))

            let actualProjects = (await Promise.all(projects.map(async project => {
                let id = await project.getAttribute("data-id");
                let tds = await project.findElements(By.css("td"))
                let name = await tds[0].getText()
                let maxHours = Number.parseInt(await tds[1].getText())
                let account = await tds[2].getText()

                return [id, name, maxHours, account]
            }))).sort((a, b) => a[0].toString().localeCompare(b[0].toString()))

            try {
                assert.deepEqual(actualProjects, expectedProjects);
                return 1;
            } catch (error) {
                console.error(error)
                console.log("should try again")
                return 0;
            }
        }))
        let result = results.reduce((prev, curr) => prev + curr);
        if (result === peers.length) {
            return true;
        } else {
            return null;
        }
    });

    let result = await peers[0].driver.wait(condition, 10000, "waiting for peers synced timed out")
    assert.strictEqual(result, true)
}

async function run() {
    let seed = new Chance().integer()
    //let seed = 5475712614006784;
    var chance = new Chance(seed);
    console.log(`The seed is: ${chance.seed}`)

    let actions = chance.n(() => chance.weighted([Actions.CREATE_PEER, Actions.DELETE_PEER, Actions.CREATE_PROJECT, Actions.CONNECT_TO_PEER, Actions.RELOAD], [10, 10, 20, 20, 10]), 200)
    //let actions = [Actions.CREATE_PEER, Actions.CREATE_PEER, Actions.CONNECT_TO_PEER, Actions.CREATE_PROJECT]

    let peers: Peer[] = [];
    try {
        for (let action of actions) {
            switch (action) {
                case Actions.CREATE_PEER: {
                    let peer = await Peer.create();
                    peer.driver.get("http://localhost:5173/")
                    peers.push(peer)
                    console.log(`[${peer.id}] peer created`)
                    break;
                }
                case Actions.CREATE_PROJECT: {
                    if (peers.length === 0) {
                        continue;
                    }
                    let random_peer: Peer = chance.pickone(peers)
                    console.log(`[${random_peer.id}] create project`)
                    // only on mobile:
                    // let dropdown = await random_peer.driver.findElement(By.id("dropdown-button"))
                    // await dropdown.click()
                    let projectsButton = await random_peer.driver.findElement(By.css(".navbar-center a[href='/projects']"))
                    await projectsButton.click()

                    let projectNameInput = await random_peer.driver.findElement(By.css("input[placeholder='New Project Name']"))
                    let maxHoursInput = await random_peer.driver.findElement(By.css("input[placeholder='0']"))
                    let accountInput = await random_peer.driver.findElement(By.css("input[placeholder='Some account']"))
                    let addProjectButton = await random_peer.driver.findElement(By.xpath(`//button[text()="Add Project"]`))

                    let projectName = chance.animal()
                    let maxHours = chance.integer({ min: 1, max: 10 })
                    let account = chance.name()
                    await projectNameInput.sendKeys(projectName)
                    await maxHoursInput.sendKeys(maxHours)
                    await accountInput.sendKeys(account)

                    /*
                    let addedProjectRow = await random_peer.driver.executeAsyncScript<WebElement>(function () {
                        var callback = arguments[arguments.length - 1];
                        const observer = new MutationObserver((value) => {
                            value.forEach(mutation => {
                                // seems like outwatch is not updating these properly so we get multiple mutations
                                console.log(mutation)
                                observer.disconnect()
                                callback(mutation.addedNodes[0])
                            })
                        });
                        observer.observe(document.querySelector("tbody")!, { childList: true });
                        console.log("hi")
                        document.querySelector<HTMLButtonElement>("#add-project-button")!.click()
                        console.log("jo")
                    })

                    let addedProject = await addedProjectRow.getAttribute("data-id")
                    */

                    await addProjectButton.click()

                    let addedProject = await random_peer.driver.wait(async () => {
                        let ids = await Promise.all((await random_peer.driver.findElements(By.css("tr[data-id]"))).map(e => e.getAttribute("data-id")));
                        let oldIds = new Set([...random_peer.projects.value.keys()])
    
                        let addedProjects = new Set([...ids].filter(x => !oldIds.has(x)))
                        if (addedProjects.size !== 1) {
                            console.error("couldn't identify which project was added", addedProjects)
                            return false
                        }
                        return [...addedProjects.values()][0]
                    }, 1000)
                    if (addedProject === false) {
                        throw new Error("couldn't identify which project was added")
                    }

                    random_peer.projects.value.set(addedProject, Project.create(random_peer.id, projectName, maxHours, account))

                    break;
                }
                case Actions.CONNECT_TO_PEER: {
                    if (peers.length < 2) {
                        continue;
                    }
                    let peersToConnect = chance.pickset(peers, 2)
                    console.log(`[${peersToConnect[0].id}, ${peersToConnect[1].id}] connect`)

                    let [[offerInput, submitOffer], [offer, answerInput, answerSubmit]] = await Promise.all([
                        (async () => {
                            let driver = peersToConnect[0].driver

                            let webrtcButton = await driver.findElement(By.css(".navbar-center a[href='/webrtc']"))
                            await webrtcButton.click()

                            let clientButton = await driver.findElement(By.xpath(`//button[text()="Client"]`))
                            await clientButton.click()

                            let textarea = await driver.findElement(By.css("textarea"))

                            let submitOffer = await driver.findElement(By.xpath(`//button[text()="Connect to host using token"]`))

                            return [textarea, submitOffer]
                        })(),
                        (async () => {
                            let driver = peersToConnect[1].driver

                            let webrtcButton = await driver.findElement(By.css(".navbar-center a[href='/webrtc']"))
                            await webrtcButton.click()

                            let hostButton = await driver.findElement(By.xpath(`//button[text()="Host"]`))
                            await hostButton.click()

                            let offer = await driver.findElement(By.css("div.overflow-x-auto"))
                            await driver.wait(until.elementTextMatches(offer, /.+/), 1000)
                            let value = await offer.getText()
                            //console.log(`got offer: ${value}`)

                            let answerInput = await driver.findElement(By.css("textarea"))

                            let answerSubmit = await driver.findElement(By.xpath(`//button[text()="Connect to client using token"]`))

                            return [value, answerInput, answerSubmit]
                        })()
                    ])
                    await offerInput.sendKeys(offer)
                    await submitOffer.click()

                    let answer = await peersToConnect[0].driver.findElement(By.css("div.overflow-x-auto"))
                    await peersToConnect[0].driver.wait(until.elementTextMatches(answer, /.+/), 1000)
                    let value = await answer.getText()
                    //console.log(`got answer: ${value}`)

                    await answerInput.sendKeys(value)
                    await answerSubmit.click()

                    await Promise.all(peersToConnect.map(async (peer) => {
                        await peer.driver.wait(until.elementLocated(By.xpath(`//h2[text()="Connected"]`)))
                    }))

                    peersToConnect[0].connectedTo.push(peersToConnect[1])
                    peersToConnect[1].connectedTo.push(peersToConnect[0])

                    //console.log("Connected")

                    break;
                }
                case Actions.RELOAD: {
                    if (peers.length === 0) {
                        continue;
                    }
                    let random_peer: Peer = chance.pickone(peers)
                    console.log(`[${random_peer.id}] reload`)

                    await random_peer.driver.navigate().refresh()

                    random_peer.connectedTo = []
                    for (let peer of peers) {
                        // TODO does this work?
                        peer.connectedTo = peer.connectedTo.filter(p => p !== random_peer)
                        if (peer.connectedTo.includes(random_peer)) {
                            assert.fail("internal failure")
                        }
                    }

                    //console.log(peers)

                    break;
                }
                case Actions.DELETE_PEER: {
                    if (peers.length === 0) {
                        continue;
                    }
                    let random_peer: Peer = chance.pickone(peers)
                    console.log(`[${random_peer.id}] delete peer`)

                    await random_peer.driver.quit()

                    random_peer.connectedTo = []
                    for (let peer of peers) {
                        peer.connectedTo = peer.connectedTo.filter(p => p !== random_peer)
                    }

                    peers = peers.filter(p => p !== random_peer)

                    break;
                }
                default: {
                    throw new Error("unhandled")
                }
            }
            await check(peers)
        }

        await Promise.all(peers.map(peer => peer.driver.close()))

        console.log("DONE")
    } catch (error) {
        console.log("seed: ", seed)
        throw error
    }
}

run()
