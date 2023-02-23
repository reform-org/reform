import { createPopper as createPopperImpl } from '@popperjs/core';
import { flip, preventOverflow } from '@popperjs/core/lib';

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

const popperInstances = [];

export const cleanPopper = async () => {
	for (let popperInstance of popperInstances) {
		popperInstance.destroy();
	}
};

export const createPopper = async (trigger, element, placement) => {
	await Promise.all([waitForElement(trigger), waitForElement(element)]);
	let ref = document.querySelector(trigger);
	let popper = document.querySelector(element);

	document.addEventListener("click", e => {
		if (!(e.target.isSameNode(ref) || e.target.isSameNode(popper) || ref.contains(e.target) || popper.contains(e.target)) && ref.parentNode.classList.contains("dropdown-open")) {
			ref.click();
		}
	});

	let popperInstance = createPopperImpl(ref, popper, {
		placement,
		modifiers: [preventOverflow, flip, {
			name: 'computeStyles',
			options: {
				adaptive: true,
				roundOffsets: ({ x, y }) => {
					if (placement === "bottom-start") return { x: 0, y };
					return { x, y };
				},
			},
		},
			/*{
				name: 'offset',
				options: {
					offset: ({ placement, reference, popper }) => {
						if (placement === "bottom-start") return [-popper.left, 0];
						return [0, 0];
					},
				},
			}*/],
	}
	);
	popperInstances.push(popperInstance);
	await waitForElement(".page-scroll-container");
	popperInstance.update();
};

export const isSelenium = import.meta.env.VITE_SELENIUM == "true";