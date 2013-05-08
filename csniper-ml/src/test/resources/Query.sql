# set these in the cross validation task application
# the JdbcReader cannot execute multiple queries
#SET @collectionId = 'BNC';
#SET @itemType = 'NP-preposing';
#SET @users = 'bartsch,dodinh,eckart,rado,schroeder,weber';
#SET @userThreshold = 0;
#SET @confidenceThreshold = 0;

SELECT 
	i.id, 
	i.coveredText AS cas_text, 
	i.collectionId AS cas_metadata_collection_id, 
	CONCAT(i.documentId, '-', i.beginOffset, '-', i.endOffset) AS cas_metadata_document_id, 
	'en' AS cas_metadata_language, 
	# count correct
	SUM(CASE WHEN r.result = 'Correct' THEN 1 ELSE 0 END) AS correct, 
	# count wrong
	SUM(CASE WHEN r.result = 'Wrong' THEN 1 ELSE 0 END) AS wrong, 
	# compare and get result; unfortunately we cannot use column aliases here, and vars (@varname) will have the value of last row
	CASE SIGN(SUM(CASE WHEN r.result = 'Correct' THEN 1 WHEN r.result = 'Wrong' THEN -1 ELSE 0 END))
		WHEN 1 THEN 'correct'
		WHEN -1 THEN 'wrong'
		WHEN 0 THEN 'disputed'
	END AS cas_metadata_title 
FROM EvaluationItem AS i 
JOIN EvaluationResult AS r 
ON i.id = r.item_id 
# only for specified corpus
WHERE i.collectionId = @collectionId
# only for specified type
AND i.type = @itemType 
# only for specified users 
AND FIND_IN_SET(userId, @users) > 0  
GROUP BY i.id 
# stay above confidence threshold
HAVING (ABS(correct - wrong) / GREATEST(correct, wrong)) >= @confidenceThreshold 
# stay above user threshold
AND ((correct + wrong) / (LENGTH(@users) - LENGTH(REPLACE(@users, ',', '')))) >= @userThreshold;