-- Gert Wohlgemuth --
-- this is a simple implementation of the similarity search used by BinBase as postgres stored procedure --
-- please be aware that it is rather slow and should only be used to debug some internal details or to make specific queries against binbase --
-- but you should not depend on it for production use --

-- calculates a relative spectra
create or replace function createRelavieSpectra(spectra float8[]) returns float8[] AS $$

DECLARE
    result float8[1000];
    array_len int;
    maxValue float8 := 0;

BEGIN

    array_len = array_upper(spectra,1);

    for i in 1 .. array_len
    LOOP
            IF spectra[i] is not null and spectra[i] > maxValue
            THEN
               maxValue := spectra[i];
            END IF;
    END LOOP;

    for i in 1 .. array_len
    LOOP
        if spectra[i] is not null
        then
            result[i] := spectra[i]/maxValue * 100;
        end if;
    END LOOP;

    RETURN result;

END;
$$ LANGUAGE plpgsql;

-- converts a spectra string to a float array for further use --
create or replace function convertSpectra(spectra text) returns float8[] AS $$

DECLARE
    result float8[1000];
    spectraSplit text[];
    ionSplit text[];
    array_len int;

    ion int;
    value float8;

BEGIN

    spectraSplit = regexp_split_to_array(spectra,' ');
    array_len = array_upper(spectraSplit,1);

    for i in 1 .. array_len
    LOOP
            ionSplit = regexp_split_to_array(spectraSplit[i],':');

           ion := ionSplit[1];
           value := ionSplit[2];


            result[ion] = value;

    END LOOP;

    RETURN result;

END;
$$ LANGUAGE plpgsql;

--calculates the similarity between two massspecs

create or replace function calculateSimilarity(unknown text, library text) returns float8 AS $$

DECLARE
    result float8;
    sameIons int[];
    sameSpectraRelativeValuesunknown float8[];
    sameSpectraAbsoluteValuesunknown float8[];

    sameSpectraRelativeValueslibrary float8[];
    sameSpectraAbsoluteValueslibrary float8[];

    unknownSpectra float8[];
    unknownSpectraRel float8[];

    librarySpectra float8[];
    librarySpectraRel float8[];

    unknownSpectraLength int :=0;

    f1 float8 := 0;
    f2 float8 := 0;

    lib float8 := 0;
    unk float8 := 0;

    sqrt1 float8 := 0;
    summ float8 := 0;
    summ4 float8 := 0;
    summ2 float8 := 0;
    summ3 float8 := 0;

    array_len int;
    sameIon int;

BEGIN

    unknownSpectra = convertSpectra(unknown);
    unknownSpectraRel = createRelavieSpectra(unknownSpectra);

    librarySpectra = convertSpectra(library);
    librarySpectraRel = createRelavieSpectra(librarySpectra);

    array_len = 1000;

    sameIon = 0;

    for i in 1 .. array_len
    LOOP
        -- this will contain all the identical ions --
        IF unknownSpectra[i] is not null and librarySpectra[i] is not null
        then
            sameIons[sameIon] = i;
            sameSpectraRelativeValuesunknown[sameIon] = unknownSpectraRel[i];
            sameSpectraAbsoluteValuesunknown[sameIon] = unknownSpectra[i];
            sameSpectraRelativeValueslibrary[sameIon] = librarySpectraRel[i];
            sameSpectraAbsoluteValueslibrary[sameIon] = librarySpectra[i];
            sameIon = sameIon + 1;
        END IF;

    END LOOP;


    -- calculate f1 --
    for i in 1 .. sameIon
    LOOP
        -- this will contain all the identical ions --
        IF sameIons[i] is not null
        then
            sqrt1 = sqrt(sameSpectraRelativeValueslibrary[i] * sameSpectraRelativeValuesunknown[i]);
            summ4 = summ4 + (sqrt1 * sameIons[i]);

            IF i > 0
            THEN
                unk = sameSpectraRelativeValuesunknown[i]/sameSpectraRelativeValuesunknown[i-1];
                lib = sameSpectraRelativeValueslibrary[i]/sameSpectraRelativeValueslibrary[i-1];

                if unk <= lib
                then
                    summ = summ + (unk/lib);
                else
                    summ = summ + (lib/unk);
                end if;
            END IF;
        END IF;
    END LOOP;

    unknownSpectraLength = 0;

    for i in 1 .. array_len
    LOOP
        IF librarySpectra[i] is not null and librarySpectra[i] > 0
        then
            summ2 = summ2 + (librarySpectraRel[i] * i);
        END IF;

        IF unknownSpectra[i] is not null and unknownSpectra[i] > 0
        then
            unknownSpectraLength = unknownSpectraLength + 1;
            summ3 = summ3 + (unknownSpectraRel[i] * i);
        END IF;
    END LOOP;

    f1 = summ4 / sqrt(summ2 * summ3);
    f2 = 1.0/sameIon * summ;

    result = (1000.0/(unknownSpectraLength + sameIon))*((unknownSpectraLength * f1) + (sameIon * f2));

    RETURN result;

END;
$$ LANGUAGE plpgsql;


