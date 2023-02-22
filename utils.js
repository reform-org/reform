import { computePosition, autoUpdate } from '@floating-ui/dom';

export const usesTurn = async (connection) => {
	try {
		const stats = await connection.getStats();
		let selectedLocalCandidate;
		for (const { type, state, localCandidateId } of stats.values())
			if (
				type === "candidate-pair" &&
				state === "succeeded" &&
				localCandidateId
			) {
				selectedLocalCandidate = localCandidateId;
				break;
			}
		return (
			!!selectedLocalCandidate &&
			stats.get(selectedLocalCandidate)?.candidateType === "relay"
		);
	} catch (error) {
		// TODO FIXME potentially a firefox bug
		// DOMException: An attempt was made to use an object that is not, or is no longer, usable
		console.error(error);
		return false;
	}
};

export const downloadJson = (name, text) => {
	const elem = document.createElement("a");
	elem.setAttribute("href", `data:text/json;charset=utf-8,${encodeURIComponent(text)}`);
	elem.setAttribute("download", name);
	elem.style.display = "none";
	document.body.appendChild(elem);
	elem.click();
	document.body.removeChild(elem);
};

const waitForElement = (selector) => {
	return new Promise(resolve => {
		if (document.querySelector(selector)) {
			return resolve(document.querySelector(selector));
		}

		const observer = new MutationObserver(mutations => {
			if (document.querySelector(selector)) {
				resolve(document.querySelector(selector));
				observer.disconnect();
			}
		});

		observer.observe(document.body, {
			childList: true,
			subtree: true
		});
	});
};

export const createPopper = async (trigger, element) => {
	await Promise.all([waitForElement(trigger), waitForElement(element)]);
	let referenceEl = document.querySelector(trigger);
	let floatingEl = document.querySelector(element);
	const cleanup = autoUpdate(referenceEl, floatingEl, () => {
		computePosition(referenceEl, floatingEl).then(({ x, y }) => {
			Object.assign(floatingEl.style, {
				position: `absolute`,
				left: `${x}px`,
				top: `${y}px`,
			});
		});
	});
	// TODO FIXME call cleanup
};

export const isSelenium = import.meta.env.VITE_SELENIUM == "true";