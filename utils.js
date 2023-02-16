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
		console.error(error)
		return false
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