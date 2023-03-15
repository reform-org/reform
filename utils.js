import { createPopper as createPopperImpl } from '@popperjs/core';
import { flip, preventOverflow } from '@popperjs/core/lib';
import { DateTime, Duration, Settings, Info } from 'luxon';

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

const popperInstances = new Map();

export const cleanPopper = async (trigger) => {
	popperInstances.get(trigger)?.destroy()
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
	let ref = document.querySelector(trigger);
	let popper = document.querySelector(element);

	document.addEventListener("click", e => {
		if (!ref || !popper || !ref.parentNode) return;
		if (!(e.target.isSameNode(ref) || e.target.isSameNode(popper) || ref.contains(e.target) || popper.contains(e.target)) && ref.parentNode.classList.contains("dropdown-open")) {
			ref.click(); // TODO FIXME cleanPopper here
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

	popperInstances.set(trigger, popperInstance);
};

const intersectionObservers = [];

export const stickyButton = async (trigger, element, toggleClass) => {
	const observer = new IntersectionObserver(entries => {
		if (document.querySelector(element)) {
			if (entries.every(entry => entry.isIntersecting)) {
				document.querySelector(element).classList.add(toggleClass)
			} else {
				document.querySelector(element).classList.remove(toggleClass)
			}
		}
	})

	intersectionObservers.push(observer)
	observer.observe(document.querySelector(trigger))
}

export const cleanStickyButtons = () => {
	intersectionObservers.forEach(o => o.disconnect())
}

export const toGermanDate = (/** @type {number} */ input) => {
	return DateTime.fromMillis(Number(input)).setLocale("de").toFormat("dd.LL.yyyy");
};

export const DateTimeFromISO = (/** @type {string} */ input) => {
	return DateTime.fromISO(input).toMillis().toString();
};

export const toHumanMonth = (index) => {
	return Info.months()[index - 1]
}

export const getMonth = (input) => {
	return DateTime.fromMillis(Number(input)).month
}

export const toMilliseconds = (month, year) => {
	return DateTime.fromObject({ month, year }).toMillis
}

export const getYear = (input) => {
	return DateTime.fromMillis(Number(input)).year
}

export const toYYYYMMDD = (input) => {
	return DateTime.fromMillis(Number(input)).toISODate();
};

export const dateDiffDays = (a, b) => {
	return Math.ceil(DateTime.fromMillis(Number(b)).diff(DateTime.fromMillis(Number(a)), "days").toObject().days) || 0;
};

export const dateDiffMonth = (a, b) => {
	return Math.ceil(DateTime.fromMillis(Number(b)).diff(DateTime.fromMillis(Number(a)), "month").toObject().months) || 0;
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