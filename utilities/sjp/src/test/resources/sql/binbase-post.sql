SELECT setval ('spectra_id',CAST ((	SELECT MAX(spectra_id) 
									FROM spectra) AS INTEGER)); 
SELECT setval ('bin_id',CAST ((	SELECT MAX(a) 
								FROM (	SELECT MAX(bin_id) AS a 
										FROM bin 
										UNION 	SELECT MAX(bin_id) AS a 
												FROM virtual_bin 
									)
									b) AS INTEGER)) ; 
SELECT setval ('sample_id',CAST ((	SELECT MAX(sample_id) 
									FROM samples) AS INTEGER)) ; 
SELECT setval ('link_id',CAST ((SELECT MAX(a) 
								FROM (	SELECT MAX(result_id) AS a 
										FROM result_link 
										UNION 	SELECT MAX(id) AS a 
												FROM sample_info 
												UNION 	SELECT MAX(key_id) AS a 
														FROM meta_key 
									)
									b) AS INTEGER)) ; 
SELECT setval ('comment_id',CAST ((	SELECT MAX(id) 
									FROM comments) AS INTEGER)) ; 
SELECT setval ('type_id',CAST ((SELECT MAX(id) 
								FROM TYPE) AS INTEGER)) ; 
SELECT setval ('result_id',CAST ((	SELECT MAX(result_id) 
									FROM RESULT) AS INTEGER)) ; 
SELECT setval ('hibernate_sequence',CAST (( (	SELECT MAX(a) 
												FROM (	SELECT MAX(id) AS a 
														FROM bin_compare 
														UNION 	SELECT MAX( group_id) AS a 
																FROM bin_group 
																UNION 	SELECT MAX( id) AS a 
																		FROM bin_ratio 
																		UNION 	SELECT MAX ( id ) AS a 
																				FROM classification 
																				UNION 	SELECT MAX ( id ) AS a 
																						FROM reference 
																						UNION 	SELECT MAX ( id ) AS a 
																								FROM reference_class 
																								UNION 	SELECT MAX ( id ) AS a 
																										FROM STRUCTURE 
																										UNION 	SELECT MAX ( id ) AS a 
																												FROM substance_classes 
																												UNION 	SELECT MAX ( id ) AS a 
																														FROM STRUCTURE 
																														UNION 	SELECT MAX ( "specId" ) AS a 
																																FROM library_spec 
																																UNION 	SELECT MAX ( id ) AS a 
																																		FROM library 
													)
													b 
	)
	) AS INTEGER));