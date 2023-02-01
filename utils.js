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
