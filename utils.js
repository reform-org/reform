export const usesTurn = async (connection) => {
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