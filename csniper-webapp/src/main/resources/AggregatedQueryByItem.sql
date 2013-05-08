SELECT itemId, correct, wrong, incomplete,
	IF(userRatio < :userThreshold,
		'incomplete',
		IF((confidence < :confidenceThreshold) OR (correct = wrong),
			'disputed',
			IF(correct > wrong,
				'correct',
				'wrong'
	))) AS vote,
	confidence,
	userRatio,
	users
FROM (
	SELECT
		drtbl.id AS itemId,
		correct,
		wrong,
		(:usersL - correct - wrong) AS incomplete,
		(ABS(correct - wrong) / GREATEST(correct, wrong)) AS confidence,
		((correct + wrong) / :usersL) AS userRatio,
		users
	FROM (
		SELECT 
			i.id, 
			SUM(CASE WHEN r.result = 'Correct' THEN 1 ELSE 0 END) AS correct, 
			SUM(CASE WHEN r.result = 'Wrong' THEN 1 ELSE 0 END) AS wrong,
			GROUP_CONCAT(CONCAT(r.userId,'#',r.result)) AS users
		FROM EvaluationItem AS i 
		JOIN EvaluationResult AS r 
		ON i.id = r.item_id 
		WHERE i.id IN (:itemIds) 
		AND userId IN (:users) 
		GROUP BY i.id
	) AS drtbl
	HAVING correct + wrong > 0
) AS drtbl2