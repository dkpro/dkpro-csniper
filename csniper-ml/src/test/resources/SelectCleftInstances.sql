# making the row counter "permanent", so we can use it in the WHERE clause
SELECT * FROM (
  # incrementing the row counter
  SELECT @n:=@n+1 AS counter, results.* FROM (
    # the actual query
    SELECT ei.coveredText AS cas_text,
           ei.collectionId AS cas_metadata_collection_id,
           CONCAT(ei.documentId, '-', ei.beginOffset, '-', ei.endOffset) AS cas_metadata_document_id,
           er.result AS cas_metadata_title,
           'en' AS cas_metadata_language
    FROM EvaluationResult AS er 

    # join er with itself to compare result values
    LEFT JOIN EvaluationResult AS er2 ON er.item_id = er2.item_id

    # and with ei to get the text and meta info
    LEFT JOIN EvaluationItem AS ei ON ei.id = er.item_id

    # only get Clefts
    WHERE ei.type = 'It-cleft' AND er.result != '' AND er2.result != ''

    # only get 1 line per item
    GROUP BY er.item_id

    # only get items for which the results coincide
    # only get correct or wrong
    # only get items rated more than once
    HAVING MIN(er.result) = MAX(er.result)
           AND (er.result = 'Correct' OR er.result = 'Wrong')
           AND COUNT(er.result) > 1 
  ) 
  AS results JOIN (SELECT @n:=0) AS c # JOIN instead of "set @n:=0;" at the beginning
) 
AS cresults
