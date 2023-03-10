import { createPopper as createPopperImpl } from '@popperjs/core';
import { flip, preventOverflow } from '@popperjs/core/lib';
import { DateTime, Duration, Settings } from 'luxon';

Settings.defaultLocale = "en"

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

export const downloadFile = (name, text, type) => {
	const elem = document.createElement("a");
	elem.setAttribute("href", `${type};charset=utf-8,${encodeURIComponent(text)}`);
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

const popperInstances = [];
const popperIntervals = [];

export const cleanPopper = async () => {
	for (let popperInstance of popperInstances) {
		popperInstance.destroy();
	}

	for (let i of popperIntervals) {
		clearInterval(i);
	}
};

const sameWidth = {
	name: "sameWidth",
	enabled: true,
	phase: "beforeWrite",
	requires: ["computeStyles"],
	fn: ({ state }) => {
		state.styles.popper.width = `${state.rects.reference.width}px`;
	},
	effect: ({ state }) => {
		state.elements.popper.style.width = `${state.elements.reference.offsetWidth
			}px`;
	}
};

export const createPopper = async (trigger, element, placement, sameWidthAsRef) => {
	await Promise.all([waitForElement(trigger), waitForElement(element)]);
	let ref = document.querySelector(trigger);
	let popper = document.querySelector(element);

	document.addEventListener("click", e => {
		if (!ref || !popper || !ref.parentNode) return;
		if (!(e.target.isSameNode(ref) || e.target.isSameNode(popper) || ref.contains(e.target) || popper.contains(e.target)) && ref.parentNode.classList.contains("dropdown-open")) {
			ref.click();
		}
	});

	const modifiers = [
		preventOverflow,
		flip,
		{
			name: 'computeStyles',
			options: {
				adaptive: false,
			},
		},
	];

	if (sameWidthAsRef) modifiers.push(sameWidth);

	let popperInstance = createPopperImpl(ref, popper, {
		placement,
		strategy: 'fixed',
		modifiers
	});

	popperInstances.push(popperInstance);
	popperIntervals.push(setInterval(function () {
		popperInstance.forceUpdate();
	}, 100));
};

const resizeObservers = [];

export const stickyButton = async (trigger, element, toggleClass) => {
	await Promise.all([waitForElement(trigger), waitForElement(element)]);
	const observer = new IntersectionObserver(entries => {
		if (entries.every(entry => entry.isIntersecting)) {
			document.querySelector(element).classList.add(toggleClass)
		} else {
			document.querySelector(element).classList.remove(toggleClass)
		}
	})

	resizeObservers.push(observer)
	observer.observe(document.querySelector(trigger))
}

export const cleanStickyButtons = () => {
	resizeObservers.forEach(o => o.disconnect())
}

export const toGermanDate = (/** @type {number} */ input) => {
	return DateTime.fromMillis(Number(input)).setLocale("de").toFormat("dd.LL.yyyy");
};

export const DateTimeFromISO = (/** @type {string} */ input) => {
	return DateTime.fromISO(input).toMillis().toString();
};

export const toYYYYMMDD = (input) => {
	return DateTime.fromMillis(Number(input)).toISODate();
};

export const dateDiffDays = (a, b) => {
	return Math.ceil(Math.abs(DateTime.fromMillis(Number(a)).diff(DateTime.fromMillis(Number(b)), "days").toObject().days)) || 0;
};

export const dateDiffMonth = (a, b) => {
	return Math.ceil(Math.abs(DateTime.fromMillis(Number(a)).diff(DateTime.fromMillis(Number(b)), "month").toObject().months)) || 0;
};

export const dateDiffHumanReadable = (a, b) => {
	let rawObject = DateTime.fromMillis(Number(a)).diff(DateTime.fromMillis(Number(b)), ["years", "month", "days"]).toObject();
	let cleanedObject = {}

	for (let key in rawObject) {
		if (rawObject[key] !== 0) cleanedObject[key] = Math.abs(rawObject[key])
	}

	return Duration.fromObject(cleanedObject).toHuman()
};

const formatter = new Intl.NumberFormat('de-DE', {
	style: 'currency',
	currency: 'EUR'
});

export const toMoneyString = (input) => {
	return formatter.format(input);
};